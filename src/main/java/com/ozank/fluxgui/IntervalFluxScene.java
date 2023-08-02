package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import com.ozank.simulator.*;
import com.ozank.viewElements.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

public class IntervalFluxScene {
    private final HashMap<TripleIndex, GraphEdge> graph;
    private final Set<GReaction> allReactions;
    private final double intervalSize;
    private List<Double> times = new ArrayList<>();
    private List<Matrix<TripleIndex>> intervalFluxes = new ArrayList<>();
    private HashMap<Double,Integer> indexes = new HashMap<>();
    private int maxFluxWeight = 0;
    private int maxSpeciesCount;
    private XYChart.Series currentIntervalMarker;

    public IntervalFluxScene(SimulationSSA simulation) {
        maxSpeciesCount = getMax(simulation.getTrajectory());
        Set<TripleIndex> allFluxes = simulation.getMatrixF().keySet();
        int k = 0;
        for (IntervalFluxSet fluxSet : simulation.getIntervalFluxes()){
            times.add(fluxSet.getTimeStamp());
            for ( TripleIndex t : allFluxes){
                if (!fluxSet.getMatrix().keySet().contains(t)){
                    fluxSet.getMatrix().put(t,0);
                }
            }
            intervalFluxes.add(fluxSet.getMatrix());
            indexes.put(fluxSet.getTimeStamp(),k);
            if (fluxSet.getMax()> maxFluxWeight) { maxFluxWeight = fluxSet.getMax(); }
            k++;
        }
        intervalSize = simulation.getInterval();
        FluxGraph data = new FluxGraph(intervalFluxes.get(0), simulation.getModel());
        graph = data.getGraph();
        allReactions = data.getAllReactions();

    }

    public void draw(HashSet<String> fluxMolecules,
                     HashSet<Integer> fluxReactions,
                     HashMap<String, Color> moleculesColorMap,
                     Map<Integer,Color> reactionsColorMap,
                     Map<String,Integer> cutOffMap,
                     int cutOffValue,
                     // ~~~~~~~~~~~~~~~~~~~~~~~~~
                     MyLineChart lineChart) {
        FilteredGraph filteredGraph = new FilteredGraph(graph,fluxMolecules, fluxReactions, cutOffMap, cutOffValue);
        FluxDigraphData digraphData = new FluxDigraphData(filteredGraph.getFilteredGraph());
        Digraph<GReaction, GEdge> edges = digraphData.getEdges();
        Map<Integer, Vertex<GReaction>> vertexMap = digraphData.getVertexMap();
        Set<GEdge> graphEdges = digraphData.getGraphEdgeSet();
        Map<TripleIndex,GEdge> graphEdgeMap = digraphData.getGraphEdgesMap();

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
            SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
            Color color = moleculesColorMap.get(edge.getSpeciesName());
            updateEdgeWeight(stylableEdge, color, edgeWeight);

            String edgeStyle = "-fx-stroke: " + FluxDigraphData.toRGBCode(color) +";";

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
        VBox controlAndPlotVBox = new VBox();
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Slider slider = new Slider(intervalSize, intervalSize *times.size(), intervalSize);
        slider.setShowTickMarks(true);
        // enable the Labels
        slider.setShowTickLabels(true);
        // set Major tick unit
        slider.setMajorTickUnit(intervalSize);
        slider.setBlockIncrement(intervalSize);
        slider.setPadding(new Insets(20, 10, 10, 10));

        slider.valueProperty().addListener((obs,old,val)->{
            double value = (double) val;
            int lower = (int) (value/ intervalSize);
            if ((intervalSize /2) < (value - lower * intervalSize)){
                slider.setValue(intervalSize *(lower+1));
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(lower),graphView, graphEdgeMap, moleculesColorMap,fluxReactions, fluxMolecules);
            } else {
                slider.setValue(intervalSize *lower);
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(lower-1),graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
            }
        });

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    

        Button backButton = new Button("Back");
        Button forwardButton = new Button("Forward");
        Button exportButton = new Button("Export");
        GridPane controls = getcontrolsGridPane(backButton,forwardButton,exportButton);
        lineChart.setPadding(new Insets(20, 30, 10, 0));
        controlAndPlotVBox.getChildren().addAll(lineChart,slider,controls);

        final int[] intervalState = {0};



        //lineChart.getData().remove(currentIntervalMarker);
        // https://www.tutorialspoint.com/how-to-color-the-plotted-area-of-a-javafx-xy-chart

        backButton.setOnAction(event -> {
            if (intervalState[0] > 0) {
                intervalState[0]--;
                slider.setValue(intervalSize * (intervalState[0] + 1));
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(intervalState[0]), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
            }
            if (intervalState[0] ==0){ backButton.setDisable(true); }
            if (intervalState[0] > 0){ backButton.setDisable(false);}
            forwardButton.setDisable(false);
            lineChart.removePolygon();
            lineChart.addPolygon(intervalState[0]*intervalSize,(intervalState[0]+1)*intervalSize,maxSpeciesCount + maxSpeciesCount/10);
        });

        forwardButton.setOnAction(event -> {
            if (intervalState[0]+1 < times.size()) {
                intervalState[0]++;
                slider.setValue(intervalSize * (intervalState[0] + 1));
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(intervalState[0]), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
            }
            if (intervalState[0] ==times.size()-1){ forwardButton.setDisable(true); }
            if (intervalState[0] < times.size()-1){ forwardButton.setDisable(false);}
            backButton.setDisable(false);
            lineChart.removePolygon();
            lineChart.addPolygon(intervalState[0]*intervalSize,(intervalState[0]+1)*intervalSize,maxSpeciesCount + maxSpeciesCount/10);
        });

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Stage fluxStage = new Stage(StageStyle.DECORATED);
        //fluxStage.initStyle(StageStyle.UTILITY);
        fluxStage.initModality(Modality.NONE);
        fluxStage.setTitle("Flux Graph");

        Scene scene = new Scene(new MySplitContainer(graphView,controlAndPlotVBox),800,600); //1024, 768);
        fluxStage.setScene(scene);
        fluxStage.show();
        graphView.init();


        lineChart.addPolygon(0,intervalSize,maxSpeciesCount + maxSpeciesCount/10);

        //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        exportButton.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fileChooser.getExtensionFilters().addAll(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(scene.getWindow());
            if (file != null) {
                String fileName = file.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, file.getName().length()).toLowerCase();
                WritableImage image = graphView.snapshot(new SnapshotParameters(), null);
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



        graphView.widthProperty().addListener((obs, oldVal, newVal) -> {
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

    private static GridPane getcontrolsGridPane(Button backButton, Button forwardButton,  Button explortButton) {
        GridPane controls = new GridPane();
        backButton.setMaxWidth(Double.MAX_VALUE);
        forwardButton.setMaxWidth(Double.MAX_VALUE);
        explortButton.setMaxWidth(Double.MAX_VALUE);

        controls.add(backButton,0,0);
        controls.add(forwardButton,1,0);
        controls.add(explortButton,2,0);
        controls.setHgap(5);
        controls.setPadding(new Insets(10, 1, 1, 1));

        ColumnConstraints column4 = new ColumnConstraints();
        column4.setFillWidth(true);
        column4.setHgrow(Priority.ALWAYS);
        ColumnConstraints column5 = new ColumnConstraints();
        column5.setFillWidth(true);
        column5.setHgrow(Priority.ALWAYS);
        ColumnConstraints column6 = new ColumnConstraints();
        column6.setFillWidth(true);
        column6.setHgrow(Priority.ALWAYS);
        controls.getColumnConstraints().addAll(column4, column5, column6);

        return controls;
    }

    private int getMax(List<TrajectoryState> trajectory){
        int result = 0;
        for (TrajectoryState state : trajectory){
            result = Math.max(result,Arrays.stream(state.getState())
                    .max()
                    .getAsInt());

        }
        return result;
    }

    private void updateEdgeWeight(SmartStylableNode stylableEdge, Color color, int newEdgeWeight){
        String edgeStyle = "-fx-stroke-width: " + mapFluxesToWidths(newEdgeWeight) +
                "; -fx-stroke: " + FluxDigraphData.toRGBCode(color) +";";
        stylableEdge.setStyle(edgeStyle);
        if (stylableEdge instanceof SmartGraphEdgeLine<?,?>){
            SmartGraphEdgeLine edgeLine = (SmartGraphEdgeLine) stylableEdge;
            SmartArrow stylableArrow = edgeLine.getAttachedArrow();
            updateEdgeArrow(stylableArrow,newEdgeWeight,edgeStyle);
        }
        if (stylableEdge instanceof SmartGraphEdgeCurve<?,?>){
            SmartGraphEdgeCurve edgeCurve = (SmartGraphEdgeCurve) stylableEdge;
            SmartArrow stylableArrow = edgeCurve.getAttachedArrow();
            updateEdgeArrow(stylableArrow,newEdgeWeight,edgeStyle);
        }
    }

    public void updateEdgeArrow(SmartArrow stylableArrow,int newEdgeWeight,String edgeStyle){
        if (newEdgeWeight == 0) {
            String edgeStyleOpaque = edgeStyle + "-fx-opacity: 0; visibility: hidden;";
            stylableArrow.setStyle(edgeStyleOpaque);
        } else {
            stylableArrow.setStyle(edgeStyle +  "-fx-opacity: 1.0; visibility: visible ;");
        }
    }

    private void updateEdgeWeight(SmartGraphPanel<GReaction, GEdge> graphView, GEdge edge, HashMap<String, Color> moleculesColorMap, int newEdgeWeight){
        SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
        edge.setWeight(newEdgeWeight);
        Color color = moleculesColorMap.get(edge.getSpeciesName());
        updateEdgeWeight(stylableEdge, color, newEdgeWeight);
    }

    private void updateEdgeWeight(SmartGraphPanel<GReaction, GEdge> graphView,
                                  TripleIndex t,
                                  Map<TripleIndex,GEdge> graphEdgeMap,
                                  HashMap<String, Color> moleculesColorMap,
                                  int newEdgeWeight,
                                  HashSet<String> fluxMolecules){
        GEdge edge = graphEdgeMap.get(t);
        if (edge != null && fluxMolecules.contains(edge.getSpeciesName())) {
            updateEdgeWeight(graphView, edge, moleculesColorMap, newEdgeWeight);
        }
    }

    private void updateEdgeWeightsOfIntervalGraph(Matrix<TripleIndex> matrix,
                                                  SmartGraphPanel<GReaction, GEdge> graphView,
                                                  Map<TripleIndex,GEdge> graphEdgeMap,
                                                  HashMap<String, Color> moleculesColorMap,
                                                  HashSet<Integer> fluxReactions,
                                                  HashSet<String> fluxMolecules){
        for (TripleIndex t : matrix.keySet()){
            if (fluxReactions.contains(t.getA()) && fluxReactions.contains(t.getB())){
                updateEdgeWeight(graphView, t, graphEdgeMap, moleculesColorMap, matrix.get(t), fluxMolecules);
            }
        }
        graphView.update();
    }

    private String format(Color c) {
        int r = (int) (255 * c.getRed()) ;
        int g = (int) (255 * c.getGreen()) ;
        int b = (int) (255 * c.getBlue()) ;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private int mapFluxesToWidths(int fluxValue){
        if (fluxValue == 0){
            return 0;
        }
        int window = (maxFluxWeight+1)/10;
        return (fluxValue/window)+1;
    }

}
