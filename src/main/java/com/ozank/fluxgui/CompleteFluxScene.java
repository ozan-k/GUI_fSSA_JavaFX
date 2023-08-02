package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.*;
import com.ozank.simulator.SimulationSSA;
import com.ozank.simulator.TripleIndex;
import com.ozank.viewElements.FilteredGraph;
import com.ozank.viewElements.FluxDigraphData;
import com.ozank.viewElements.FluxGraph;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class CompleteFluxScene {
    private final HashMap<TripleIndex, GraphEdge> graph;
    private final Set<GReaction> allReactions;
    private int maxFluxWeight = 0;

    public CompleteFluxScene(SimulationSSA simulation) {
        FluxGraph data = new FluxGraph(simulation.getMatrixF(),simulation.getModel());
        graph = data.getGraph();
        allReactions = data.getAllReactions();
        maxFluxWeight = simulation.getMatrixF().maxValue();
    }

    public void draw(HashSet<String> fluxMolecules,
                     HashSet<Integer> fluxReactions,
                     HashMap<String, Color> moleculesColorMap,
                     Map<Integer,Color> reactionsColorMap,
                     Map<String,Integer> cutOffMap,
                     int cutOffValue) {
        FilteredGraph filteredGraph = new FilteredGraph(graph,fluxMolecules,fluxReactions,cutOffMap, cutOffValue);
        FluxDigraphData digraphData = new FluxDigraphData(filteredGraph.getFilteredGraph());
        Digraph<GReaction, GEdge> edges = digraphData.getEdges();
        Map<Integer,Vertex<GReaction>> vertexMap = digraphData.getVertexMap();
        Set<GEdge> graphEdges = digraphData.getGraphEdgeSet();

        String customProps = "edge.label = true" + "\n" + "edge.arrow = true";
        SmartGraphProperties properties = new SmartGraphProperties(customProps);

        URI uri;
        try {
            URL url = this.getClass().getClassLoader().getResource("smartgraph.css");
            uri = url.toURI();
        } catch (NullPointerException | URISyntaxException ex) {
            throw new Error("Unexpected exception", ex);
        }

        SmartGraphPanel<GReaction, GEdge> graphView = new SmartGraphPanel<>(
                edges,
                properties,
                new SmartCircularSortedPlacementStrategy(),
                uri);

        for (int id : vertexMap.keySet()){
            String fill = FluxDigraphData.toRGBCode(reactionsColorMap.get(id));
            String stroke = (fill.equals("#B1DFF7") ? "#61B5F1" :
                    FluxDigraphData.toRGBCode(reactionsColorMap.get(id).darker()));
            String style = "-fx-stroke: " + stroke +
                    "; -fx-fill: " + fill + ";";
            graphView.getStylableVertex(vertexMap.get(id)).setStyle(style);
        }

        int edgeWeight;
        for (GEdge edge : graphEdges) {
            edgeWeight = edge.getWeight();
            Color c = moleculesColorMap.get(edge.getSpeciesName());
            String edgeStyle = "-fx-stroke: " + FluxDigraphData.toRGBCode(c) +";" +
                    "-fx-stroke-width: " + mapFluxesToWidths(edgeWeight) +";";
            SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
            stylableEdge.setStyle(edgeStyle);
            if (stylableEdge instanceof SmartGraphEdgeLine<?,?>){
                SmartGraphEdgeLine edgeLine = (SmartGraphEdgeLine) stylableEdge;
                SmartArrow stylableArrow = edgeLine.getAttachedArrow();
                stylableArrow.setStyle(edgeStyle);
            }
            if (stylableEdge instanceof SmartGraphEdgeCurve<?,?>){
                SmartGraphEdgeCurve edgeCurve = (SmartGraphEdgeCurve) stylableEdge;
                SmartArrow stylableArrow = edgeCurve.getAttachedArrow();
                stylableArrow.setStyle(edgeStyle);
            }
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Stage fluxStage = new Stage(StageStyle.DECORATED);
        //fluxStage.initStyle(StageStyle.UTILITY);
        fluxStage.initModality(Modality.NONE);

        fluxStage.setTitle("Flux Graph");
        Scene scene = new Scene(new MyZoomContainer(graphView),800,600); //1024, 768);
        fluxStage.setScene(scene);
        fluxStage.show();
        graphView.init();

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double factor = (double) newVal / (double) oldVal;
            for (int id : vertexMap.keySet()){
                double xPosition = graphView.getVertexPositionX(vertexMap.get(id));
                double yPosition = graphView.getVertexPositionY(vertexMap.get(id));
                graphView.setVertexPosition(vertexMap.get(id),xPosition*factor, yPosition);
            }
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double factor = (double) newVal / (double) oldVal;
            for (int id : vertexMap.keySet()){
                double xPosition = graphView.getVertexPositionX(vertexMap.get(id));
                double yPosition = graphView.getVertexPositionY(vertexMap.get(id));
                double newYPosition = yPosition*factor;
                graphView.setVertexPosition(vertexMap.get(id),xPosition,newYPosition);
            }
        });
    }

    public Set<GReaction> getAllReactions(){
        return allReactions;
    }

    private int mapFluxesToWidths(int fluxValue){
        if (fluxValue == 0){
            return 0;
        }
        int window = (maxFluxWeight+1)/10;
        return (fluxValue/window)+1;
    }

}
