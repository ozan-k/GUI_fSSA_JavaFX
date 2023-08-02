package com.ozank.viewElements;

import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class MyLineChart extends LineChart {

    private Polygon polygon;
    public MyLineChart(Axis axis, Axis axis1) {
        super(axis, axis1);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
    }

    public void addPolygon(double x1p,double x2p,double y2p){
        double x1 = getXAxis().getDisplayPosition(x1p);
        double y1 = getYAxis().getDisplayPosition(0);
        double x2 = getXAxis().getDisplayPosition(x2p);
        double y2 = getYAxis().getDisplayPosition(y2p);
        polygon = new Polygon();
        polygon.getPoints().addAll(new Double[]{
                x1,y1,
                x1,y2,
                x2,y2,
                x2,y1
        });
        polygon.setOpacity(0.2);
        getPlotChildren().add(polygon);
        //polygon.toFront();
        polygon.setFill(Color.GRAY);
    }

    public void removePolygon(){
        getPlotChildren().remove(polygon);
    }

}
