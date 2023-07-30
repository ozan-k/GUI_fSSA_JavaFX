package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.containers.ContentZoomPane;
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.*;
import com.ozank.simulator.SimulationSSA;
import com.ozank.simulator.TripleIndex;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URI;
import java.net.URL;
import java.util.*;

public class CompleteFluxScene {

    private HashMap<TripleIndex, GraphEdge> graph = new HashMap<>();

    private Set<GReaction> allReactions = new HashSet<>();

    public CompleteFluxScene(SimulationSSA simulation) {
        for (TripleIndex t : simulation.getF().keySet()) {
            int sourceId = t.getA();
            int targetId = t.getB();
            int moleculeId = t.getC();
            String moleculeName = simulation.getModel().getMoleculesList().get(moleculeId);
            String sourceName = simulation.getModel().getReactionNames()[sourceId];
            String targetName = simulation.getModel().getReactionNames()[targetId];
            int flux = simulation.getF().get(t);
            allReactions.add(new GReaction(sourceId,sourceName));
            allReactions.add(new GReaction(targetId,targetName));
            graph.put(t, new GraphEdge(sourceId, targetId, sourceName, targetName, moleculeId, moleculeName, flux));
        }
    }

    public void draw(HashSet<String> fluxMolecules,
                     HashSet<Integer> fluxReactions,
                     HashMap<String, Color> moleculesColorMap,
                     Map<Integer,Color> reactionsColorMap,
                     Map<String,Integer> cutOffMap,
                     int cutOffValue) {
        Digraph<GReaction, GEdge> edges = new DigraphEdgeList<>();
        Map<Integer,Vertex<GReaction>> vertexMap = new HashMap<>();
        int sourceId;
        int targetId;
        Vertex vertex;
        Set<GEdge> graphEdges = new HashSet<>();
        for (GraphEdge edge : getFilteredGraph(fluxMolecules, fluxReactions, cutOffMap, cutOffValue)) {
            sourceId = edge.getSourceId();
            targetId = edge.getTargetId();
            if (!vertexMap.containsKey(sourceId)) {
                vertex = edges.insertVertex(new GReaction(sourceId, edge.getSourceName()));
                vertexMap.put(sourceId, vertex);
            }
            if (!vertexMap.containsKey(targetId)) {
                vertex = edges.insertVertex(new GReaction(targetId, edge.getTargetName()));
                vertexMap.put(targetId, vertex);
            }
            GEdge fluxEdge = new GEdge(sourceId, targetId, edge.getSpeciesName(), edge.getFlux(), edge.getSpeciesId());
            graphEdges.add(fluxEdge);
            Vertex<GReaction> sourceVertex = vertexMap.get(sourceId);
            Vertex<GReaction> targetVertex = vertexMap.get(targetId);
            edges.insertEdge(sourceVertex, targetVertex, fluxEdge);
        }

        String customProps = "edge.label = true" + "\n" + "edge.arrow = true";
        SmartGraphProperties properties = new SmartGraphProperties(customProps);

        URI uri;
        try {
            URL url = this.getClass().getClassLoader().getResource("smartgraph.css");
            uri = url.toURI();
        } catch (Throwable ex) {
            throw new Error("Unexpected exception", ex);
        }

        SmartGraphPanel<GReaction, GEdge> graphView = new SmartGraphPanel<>(
                edges,
                properties,
                new SmartCircularSortedPlacementStrategy(),
                uri);

        for (int id : vertexMap.keySet()){
            String fill = toRGBCode(reactionsColorMap.get(id));
            String stroke = (fill.equals("#B1DFF7") ? "#61B5F1" :
                    toRGBCode(reactionsColorMap.get(id).darker()));
            String style = "-fx-stroke: " + stroke +
                    "; -fx-fill: " + fill + ";";
            graphView.getStylableVertex(vertexMap.get(id)).setStyle(style);
        }

        int edgeWeight;
        for (GEdge edge : graphEdges) {
            edgeWeight = edge.getWeight();
            Color c = moleculesColorMap.get(edge.getSpeciesName());
            String edgeStyle = "-fx-stroke: " + toRGBCode(c) +";";
            SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
            stylableEdge.setStyle("-fx-stroke-width: " + mapFluxesToWidths(edgeWeight) +";" + edgeStyle);
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
        //fluxStage.setMinHeight(500);
        //fluxStage.setMinWidth(600);
        Scene scene = new Scene(new MyZoomContainer(graphView),800,600); //1024, 768);
                //new Scene(new SmartGraphDemoContainer(graphView),800,600); //1024, 768);
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



//    newPosition(yPosition*factor,(double) newVal,40);
    private double newPosition(double axisPosition,double stageAxisExtend,double offset){
        if (axisPosition < offset){
            return offset;
        } else if (stageAxisExtend-axisPosition < offset){
            return stageAxisExtend - offset;
        } else {
            return axisPosition;
        }
    }

    public ArrayList<GraphEdge> getFilteredGraph(HashSet<String> includedMolecules,
                                                 HashSet<Integer> includedReactions,
                                                 Map<String,Integer> cutOffMap,
                                                 int cutOffValue) {
        ArrayList<GraphEdge> filteredGraph = new ArrayList<>();
        for (GraphEdge e : this.graph.values()) {
            if (includedMolecules.contains(e.getSpeciesName()) &&
                    includedReactions.contains(e.getSourceId()) &&
                    includedReactions.contains(e.getTargetId()) &&
                    e.getFlux() >= cutOffMap.get(e.getSpeciesName()) &&
                    e.getFlux() >= cutOffValue
            ) {
                    filteredGraph.add(e);
            }
        }
        return filteredGraph;
    }

    private Integer mapFluxesToWidths(Integer weight) {
        return ((int) Math.log10(weight)) + 1;
    }

    public HashMap<TripleIndex, GraphEdge> getGraph() {
        return graph;
    }

    public Set<GReaction> getAllReactions(){
        return allReactions;
    }

    private static String toRGBCode( Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

    private static String toRGBCodeDarker( Color color )
    {
        Color c = color.darker();
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

}
 /*
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        VBox vb = new VBox();
        vb.setFillWidth(true);
        HBox hb = new HBox();
        //hb.getChildren().add(linechart);
        hb.setFillHeight(true);
        vb.getChildren().add(hb);

        //HBox.setHgrow(linechart, Priority.ALWAYS);
        //VBox.setVgrow(hb, Priority.ALWAYS);

        GridPane topGridPane = new GridPane();
        Label dummyLabel = new Label("");
        Button exportButton = new Button("Export");
        exportButton.prefWidth(60);
        topGridPane.add(dummyLabel,0,0);
        topGridPane.add(exportButton,2,0);
        topGridPane.setHgap(5);
        topGridPane.setPadding(new Insets(10, 10, 0, 10));
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setFillWidth(true);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setFillWidth(true);
        topGridPane.getColumnConstraints().addAll(column1, column2);

        BorderPane bPane = new BorderPane();
        bPane.setTop(topGridPane);
        bPane.setCenter(vb);
        exportButton.requestFocus();
        Scene scene = new Scene(bPane);
*/