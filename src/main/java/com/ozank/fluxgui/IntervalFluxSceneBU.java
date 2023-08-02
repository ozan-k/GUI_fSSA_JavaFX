package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URI;
import java.net.URL;

public class IntervalFluxSceneBU {
    private final Stage dialogStage;

    public IntervalFluxSceneBU(){
        Digraph<String, String> g = new DigraphEdgeList<>();

        g.insertVertex("A");
        g.insertVertex("B");
        g.insertVertex("C");
        g.insertVertex("D");
        g.insertVertex("E");
        g.insertVertex("F");

        g.insertEdge("A", "B", "AB");
        g.insertEdge("B", "A", "AB2");
        g.insertEdge("A", "C", "AC");
        g.insertEdge("A", "D", "AD");
        g.insertEdge("B", "C", "BC");
        g.insertEdge("C", "D", "CD");
        g.insertEdge("B", "E", "BE");
        g.insertEdge("F", "D", "DF");
        g.insertEdge("F", "D", "DF2");

        String customProps = "edge.label = true" + "\n" + "edge.arrow = true";
        SmartGraphProperties properties = new SmartGraphProperties(customProps);

        URI uri;
        try {
            URL url = this.getClass().getClassLoader().getResource("smartgraph.css");
            uri = url.toURI();
        } catch (Throwable ex) {
            throw new Error("Unexpected exception", ex);
        }

        SmartGraphPanel<String,String> graphView = new SmartGraphPanel<>(
                g,
                properties,
                new SmartCircularSortedPlacementStrategy(),
                uri);




        VBox vBox = new VBox();
        Label l = new Label("AAAAA");
        vBox.getChildren().add(l);

        Scene scene = new Scene(new MySplitContainer(graphView,vBox),800,600);


        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.initModality(Modality.NONE);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        dialogStage.setScene(scene);
        dialogStage.centerOnScreen();
        dialogStage.setResizable(true);
        //Displaying the contents of the stage
        dialogStage.show();

    }

}
