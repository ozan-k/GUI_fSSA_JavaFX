package com.ozank.fluxgui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;


/**
 * This class provides zooming and panning for a JavaFX node.
 * <br/>
 * It shows the zoom level with a slider control and reacts to mouse scrolls and
 * mouse dragging.
 * <br/>
 * The content node is out forward in the z-index, so it can react to mouse
 * events first. The node should consume any event not meant to propagate to
 * this pane.
 *
 * @author brunomnsilva
 */
public class MySplitContainer extends SplitPane {

    private final DoubleProperty scaleFactorProperty = new ReadOnlyDoubleWrapper(1);
    private final Node contentLeft;

    private final Node contentRight;

    private static final double MIN_SCALE = 1;
    private static final double MAX_SCALE = 5;
    private static final double SCROLL_DELTA = 0.25;

    public MySplitContainer(Node content1,Node content2) {
        if (content1 == null || content2 == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        this.contentLeft = content1;
        this.contentRight = content2;

        contentLeft.toFront();
        super.getItems().addAll(contentLeft,contentRight);
        enablePanAndZoom();
    }


    public void setContentPivot(double x, double y) {
        contentLeft.setTranslateX(contentLeft.getTranslateX() - x);
        contentLeft.setTranslateY(contentLeft.getTranslateY() - y);
    }

    public static double boundValue(double value, double min, double max) {

        if (Double.compare(value, min) < 0) {
            return min;
        }

        if (Double.compare(value, max) > 0) {
            return max;
        }

        return value;
    }

    private void enablePanAndZoom() {

        setOnScroll((ScrollEvent event) -> {

            double direction = event.getDeltaY() >= 0 ? 1 : -1;

            double currentScale = scaleFactorProperty.getValue();
            double computedScale = currentScale + direction * SCROLL_DELTA;

            computedScale = boundValue(computedScale, MIN_SCALE, MAX_SCALE);

            if (currentScale != computedScale) {

                contentLeft.setScaleX(computedScale);
                contentLeft.setScaleY(computedScale);

                if (computedScale == 1) {
                    contentLeft.setTranslateX(-getTranslateX());
                    contentLeft.setTranslateY(-getTranslateY());
                } else {
                    scaleFactorProperty.setValue(computedScale);

                    Bounds bounds = contentLeft.localToScene(contentLeft.getBoundsInLocal());
                    double f = (computedScale / currentScale) - 1;
                    double dx = (event.getX() - (bounds.getWidth() / 2 + bounds.getMinX()));
                    double dy = (event.getY() - (bounds.getHeight() / 2 + bounds.getMinY()));

                    setContentPivot(f * dx, f * dy);
                }

            }
            //do not propagate event
            event.consume();
        });

        final DragContext sceneDragContext = new DragContext();

        setOnMousePressed((MouseEvent event) -> {

            if (event.isSecondaryButtonDown()) {
                getScene().setCursor(Cursor.MOVE);

                sceneDragContext.mouseAnchorX = event.getX();
                sceneDragContext.mouseAnchorY = event.getY();

                sceneDragContext.translateAnchorX = contentLeft.getTranslateX();
                sceneDragContext.translateAnchorY = contentLeft.getTranslateY();
            }

        });

        setOnMouseReleased((MouseEvent event) -> {
            getScene().setCursor(Cursor.DEFAULT);
        });

        setOnMouseDragged((MouseEvent event) -> {
            if (event.isSecondaryButtonDown()) {

                contentLeft.setTranslateX(sceneDragContext.translateAnchorX + event.getX() - sceneDragContext.mouseAnchorX);
                contentLeft.setTranslateY(sceneDragContext.translateAnchorY + event.getY() - sceneDragContext.mouseAnchorY);
            }
        });

    }

    public DoubleProperty scaleFactorProperty() {
        return scaleFactorProperty;
    }

    private static class DragContext {

        double mouseAnchorX;
        double mouseAnchorY;

        double translateAnchorX;
        double translateAnchorY;

    }

}

