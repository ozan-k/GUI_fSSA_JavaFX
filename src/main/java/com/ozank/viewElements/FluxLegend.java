package com.ozank.viewElements;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class FluxLegend {
    long maxFluxWeight;
    private final Stage legendStage;

    public FluxLegend(long maxFluxWeight) {
        //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        this.maxFluxWeight = maxFluxWeight;

        legendStage = new Stage(StageStyle.UTILITY);
        //fluxStage.initStyle(StageStyle.UTILITY);
        legendStage.initModality(Modality.NONE);
        legendStage.setTitle("Flux Legend");

        long[] vals = {1,2,10};
        String[] legendLabels = {
                Long.toString(maxFluxWeight),
                Long.toString(maxFluxWeight/2),
                Long.toString(maxFluxWeight/10) };

        // Create the graph
        Graph<String, String> g = new GraphEdgeList<>();
        Vertex<String> v1 = g.insertVertex("1");
        Vertex<String> v2 = g.insertVertex("2");
        Vertex<String> v3 = g.insertVertex("3");
        Vertex<String> v4 = g.insertVertex("4");
        Vertex<String> v5 = g.insertVertex("5");
        Vertex<String> v6 = g.insertVertex("6");
        g.insertEdge("1","2",legendLabels[0]);
        g.insertEdge("3","4",legendLabels[1]);
        g.insertEdge("5","6",legendLabels[2]);

        URI uri2;
        try {
            URL url = this.getClass().getClassLoader().getResource("legend.css");
            uri2 = url.toURI();
        } catch (NullPointerException | URISyntaxException ex) {
            throw new Error("Unexpected exception", ex);
        }

        String customProps2 = "edge.label = true" + "\n" + "edge.arrow = false" +
                "\n" + "vertex.label = false" + "\n" + "vertex.radius = 1";
        SmartGraphProperties properties2 = new SmartGraphProperties(customProps2);

        SmartPlacementStrategy initialPlacement = new SmartCircularSortedPlacementStrategy();
        SmartGraphPanel<String, String> legendGraph = new SmartGraphPanel<>(
                g,
                properties2,
                initialPlacement,
                uri2);

        legendGraph.setAutomaticLayout(false);

        for (int i=0; i<3; i++){
            String edgeStyle = "-fx-stroke-width: " + mapFluxesToWidths(maxFluxWeight/vals[i])+";";
            SmartStylableNode stylableEdge = legendGraph.getStylableEdge(legendLabels[i]);
            stylableEdge.setStyle(edgeStyle);
            //SmartGraphEdgeLine edgeLine = (SmartGraphEdgeLine) stylableEdge;
            //SmartArrow stylableArrow = edgeLine.getAttachedArrow();
            //stylableArrow.setStyle(edgeStyle + "-fx-opacity: 1.0; visibility: visible ;");
        }
        Button exportButton = new Button("Export");
        BorderPane legendBorderPane = new BorderPane();
        legendBorderPane.setCenter(legendGraph);
        legendBorderPane.setBottom(exportButton);
        BorderPane.setAlignment(exportButton, Pos.BOTTOM_LEFT);
        exportButton.prefWidth(150);

        Scene legendScene = new Scene(legendBorderPane, 150, 230);
        legendStage.setScene(legendScene);
        legendStage.show();
        legendGraph.init();



        legendGraph.setVertexPosition(v1, 50, 40);
        legendGraph.setVertexPosition(v2, 100, 40);
        legendGraph.setVertexPosition(v3, 50, 70);
        legendGraph.setVertexPosition(v4, 100, 70);
        legendGraph.setVertexPosition(v5, 50, 100);
        legendGraph.setVertexPosition(v6, 100, 100);

        exportButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fileChooser.getExtensionFilters().addAll(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(legendBorderPane.getScene().getWindow());
            if (file != null) {
                String fileName = file.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, file.getName().length()).toLowerCase();
                WritableImage image = legendGraph.snapshot(new SnapshotParameters(), null);
                if (fileExtension.equals("png")){
                    //WritableImage image = scene.snapshot(null);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    private int mapFluxesToWidths(long fluxValue) {
        if (fluxValue == 0) {
            return 0;
        }
        long window = ((maxFluxWeight + 1) / 10 == 0 ? 1 : (maxFluxWeight + 1) / 10);
        return (int) (fluxValue / window) + 2;
    }

    public void close(){
        legendStage.close();
    }
}
