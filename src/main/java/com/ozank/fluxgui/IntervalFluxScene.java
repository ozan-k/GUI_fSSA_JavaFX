package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.example.City;
import com.brunomnsilva.smartgraph.graph.*;
import com.brunomnsilva.smartgraph.graphview.*;
import com.ozank.dataElements.IntervalFluxSet;
import com.ozank.dataElements.Matrix;
import com.ozank.dataElements.ProcessedFluxes;
import com.ozank.dataElements.TimeSeries;
import com.ozank.viewElements.*;
import javafx.concurrent.Task;
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
import javafx.stage.*;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.channels.SeekableByteChannel;
import java.util.*;

public class IntervalFluxScene {
    private final HashMap<TripleIndex, GraphEdge> graph;
    private final Set<GReaction> allReactions;
    private final double intervalSize;
    private List<Double> times = new ArrayList<>();
    private List<Matrix<TripleIndex>> intervalFluxes = new ArrayList<>();
    private HashMap<Double, Integer> indexes = new HashMap<>();
    private long maxFluxWeight = 0;
    private int maxSpeciesCount;
    private XYChart.Series currentIntervalMarker;

    private final int[] intervalState = {0};
    private Slider slider;
    private SmartGraphPanel<GReaction, GEdge> graphView;
    private Button forwardButton;
    private Button backButton;
    private Map<TripleIndex, GEdge> graphEdgeMap;
    private ArrayList<BufferedImage> images;

    // fluxReactions, fluxMolecules


    public IntervalFluxScene(ProcessedFluxes fluxes, TimeSeries timeSeries, Map<String, Integer> speciesMap) {
        maxSpeciesCount = getMax(timeSeries.getTimeSeries());
        Set<TripleIndex> allFluxes = fluxes.getMatrixF().keySet();
        int k = 0;
        for (IntervalFluxSet fluxSet : fluxes.getIntervalFluxes()) {
            times.add(fluxSet.getTimeStamp());
            for (TripleIndex t : allFluxes) {
                if (!fluxSet.getMatrix().keySet().contains(t)) {
                    fluxSet.getMatrix().put(t, 0);
                }
            }
            intervalFluxes.add(fluxSet.getMatrix());
            indexes.put(fluxSet.getTimeStamp(), k);
            if (fluxSet.getMax() > maxFluxWeight) {
                maxFluxWeight = fluxSet.getMax();
            }
            k++;
        }
        intervalSize = fluxes.getIntervalFluxes().stream()
                .mapToDouble(s -> s.getTimeStamp())
                .min()
                .orElse(timeSeries.getEndTime());
        FluxGraph data = new FluxGraph(intervalFluxes.get(0), speciesMap);
        graph = data.getGraph();
        allReactions = data.getAllReactions();
    }

    public void draw(HashSet<String> fluxMolecules,
                     HashSet<Integer> fluxReactions,
                     HashMap<String, Color> moleculesColorMap,
                     Map<Integer, Color> reactionsColorMap,
                     Map<String, Integer> cutOffMap,
                     int cutOffValue,
                     // ~~~~~~~~~~~~~~~~~~~~~~~~~
                     MyLineChart lineChart) {
        FilteredGraph filteredGraph = new FilteredGraph(graph, fluxMolecules, fluxReactions, cutOffMap, cutOffValue);
        FluxDigraphData digraphData = new FluxDigraphData(filteredGraph.getFilteredGraph());
        Digraph<GReaction, GEdge> edges = digraphData.getEdges();
        Map<Integer, Vertex<GReaction>> vertexMap = digraphData.getVertexMap();
        Set<GEdge> graphEdges = digraphData.getGraphEdgeSet();
        graphEdgeMap = digraphData.getGraphEdgesMap();

        String customProps = "edge.label = true" + "\n" + "edge.arrow = true";
        SmartGraphProperties properties = new SmartGraphProperties(customProps);

        URI uri1;
        try {
            URL url1 = this.getClass().getClassLoader().getResource("smartgraph.css");
            uri1 = url1.toURI();
        } catch (NullPointerException | URISyntaxException ex) {
            throw new Error("Unexpected exception", ex);
        }

        graphView = new SmartGraphPanel<>(
                edges,
                properties,
                new SmartCircularSortedPlacementStrategy(),
                uri1);

        for (int id : vertexMap.keySet()) {
            String fill = FluxDigraphData.toRGBCode(reactionsColorMap.get(id));
            String stroke = (fill.equals("#B1DFF7") ? "#61B5F1" :
                    FluxDigraphData.toRGBCode(reactionsColorMap.get(id).darker()));
            String style = "-fx-stroke: " + stroke +
                    "; -fx-fill: " + fill + ";";
            graphView.getStylableVertex(vertexMap.get(id)).setStyle(style);
        }

        long edgeWeight;
        for (GEdge edge : graphEdges) {
            edgeWeight = edge.getWeight();
            SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
            Color color = moleculesColorMap.get(edge.getSpeciesName());
            updateEdgeWeight(stylableEdge, color, edgeWeight);
            String edgeStyle = "-fx-stroke-width: " + mapFluxesToWidths(edgeWeight) +
                    "; -fx-stroke: " + FluxDigraphData.toRGBCode(color) + ";";

            if (stylableEdge instanceof SmartGraphEdgeLine<?, ?>) {
                SmartGraphEdgeLine edgeLine = (SmartGraphEdgeLine) stylableEdge;
                SmartArrow stylableArrow = edgeLine.getAttachedArrow();
                updateEdgeArrow(stylableArrow, edgeWeight, edgeStyle);
            }
            if (stylableEdge instanceof SmartGraphEdgeCurve<?, ?>) {
                SmartGraphEdgeCurve edgeCurve = (SmartGraphEdgeCurve) stylableEdge;
                SmartArrow stylableArrow = edgeCurve.getAttachedArrow();
                updateEdgeArrow(stylableArrow, edgeWeight, edgeStyle);
            }
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BorderPane controlAndPlotBorderPane = new BorderPane();
        VBox controlVBox = new VBox();
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        slider = new Slider(intervalSize, intervalSize * times.size(), intervalSize);
        slider.setShowTickMarks(true);
        // enable the Labels
        slider.setShowTickLabels(true);
        // set Major tick unit
        slider.setMajorTickUnit(intervalSize);
        slider.setBlockIncrement(intervalSize);
        slider.setPadding(new Insets(20, 10, 10, 10));

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        backButton = new Button("Back");
        forwardButton = new Button("Forward");
        Button exportPlotButton = new Button("Export Plot");
        Button playButton = new Button("Play Movie");
        Button exportCurrentFluxButton = new Button("Export Current Graph");
        Button exportAllFluxesButton = new Button("Export All Graphs");

        GridPane controls = getcontrolsGridPane(
                backButton, forwardButton,
                exportPlotButton, playButton,
                exportCurrentFluxButton,exportAllFluxesButton);
        lineChart.setPadding(new Insets(20, 30, 10, 0));
        controlVBox.getChildren().addAll(slider, controls);

        slider.valueProperty().addListener((obs, old, val) -> {
            double value = (double) val;
            int lower = (int) (value / intervalSize);
            if ((intervalSize / 2) < (value - lower * intervalSize)) {
                slider.setValue(intervalSize * (lower + 1));
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(lower), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
                intervalState[0] = lower;
                lineChart.removePolygon();
                lineChart.addPolygon(intervalState[0] * intervalSize, (intervalState[0] + 1) * intervalSize, maxSpeciesCount + maxSpeciesCount / 10);
            } else {
                slider.setValue(intervalSize * lower);
                updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(lower - 1), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
                intervalState[0] = lower - 1;
                lineChart.removePolygon();
                lineChart.addPolygon(intervalState[0] * intervalSize, (intervalState[0] + 1) * intervalSize, maxSpeciesCount + maxSpeciesCount / 10);
            }
        });

        //lineChart.getData().remove(currentIntervalMarker);
        // https://www.tutorialspoint.com/how-to-color-the-plotted-area-of-a-javafx-xy-chart

        backButton.setOnAction(event -> {
            moveBack(lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
        });

        forwardButton.setOnAction(event -> {
            moveForward(lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
        });

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Stage fluxStage = new Stage(StageStyle.DECORATED);
        //fluxStage.initStyle(StageStyle.UTILITY);
        fluxStage.initModality(Modality.NONE);
        fluxStage.setTitle("Flux Graph");

        VBox empty = new VBox();
        empty.prefHeightProperty().bind(controlAndPlotBorderPane.heightProperty().multiply(0.15));
        controlAndPlotBorderPane.setTop(empty);
        controlAndPlotBorderPane.setCenter(lineChart);
        controlAndPlotBorderPane.setBottom(controlVBox);
        MySplitContainer splitContainer = new MySplitContainer(graphView, controlAndPlotBorderPane);
        Scene scene = new Scene(splitContainer, 800, 600); //1024, 768);
        fluxStage.setScene(scene);
        fluxStage.show();
        graphView.init();

        lineChart.addPolygon(0, intervalSize, maxSpeciesCount + maxSpeciesCount / 10);

        FluxLegend fluxLegend = new FluxLegend(maxFluxWeight);

        exportPlotButton.setOnAction(event -> {
            WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
            saveDialog(scene,image);
        });

        playButton.setOnAction(event -> {
            images = new ArrayList<>();
            while (intervalState[0] > 0) {
                moveBack(lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
            }
            delay(500, () -> {});
            delay(600, () -> {playForward(lineChart, moleculesColorMap, fluxReactions, fluxMolecules); });

        });

        graphView.widthProperty().addListener((obs, oldVal, newVal) -> {
            double factor = (double) newVal / (double) oldVal;
            for (int id : vertexMap.keySet()) {
                double xPosition = graphView.getVertexPositionX(vertexMap.get(id));
                double yPosition = graphView.getVertexPositionY(vertexMap.get(id));
                graphView.setVertexPosition(vertexMap.get(id), xPosition * factor, yPosition);
            }
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double factor = (double) newVal / (double) oldVal;
            for (int id : vertexMap.keySet()) {
                double xPosition = graphView.getVertexPositionX(vertexMap.get(id));
                double yPosition = graphView.getVertexPositionY(vertexMap.get(id));
                double newYPosition = yPosition * factor;
                graphView.setVertexPosition(vertexMap.get(id), xPosition, newYPosition);
            }
        });

        exportCurrentFluxButton.setOnAction(event -> {
            WritableImage image = graphView.snapshot(new SnapshotParameters(), null);
            saveDialog(scene, image); });

        exportAllFluxesButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            //directoryChooser.setInitialDirectory(new File("images"));
            File path = directoryChooser.showDialog(scene.getWindow());
            //System.out.println(path.getAbsolutePath());

            images = new ArrayList<>();
            while (intervalState[0] > 0) {
                moveBack(lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
            }
            delay(500, () -> {
                // capture the first frame after giving time to FX to update the scene.
                addImage(splitContainer);
            });
            delay(600, () -> {
                // capture the remaining frames.
                captureScreenShot(splitContainer, lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
            });
            delay((times.size() + 2) * 500L, () -> {
                try {
                    generateVideo(images,path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        fluxStage.setOnCloseRequest(event -> {
            fluxLegend.close();
        });

    }

    private void moveForward(MyLineChart lineChart,
                             HashMap<String, Color> moleculesColorMap,
                             HashSet<Integer> fluxReactions,
                             HashSet<String> fluxMolecules) {
        if (intervalState[0] + 1 < times.size()) {
            intervalState[0]++;
            slider.setValue(intervalSize * (intervalState[0] + 1));
            updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(intervalState[0]), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
        }
        if (intervalState[0] == times.size() - 1) {
            forwardButton.setDisable(true);
        }
        if (intervalState[0] < times.size() - 1) {
            forwardButton.setDisable(false);
        }
        backButton.setDisable(false);
        lineChart.removePolygon();
        lineChart.addPolygon(intervalState[0] * intervalSize, (intervalState[0] + 1) * intervalSize, maxSpeciesCount + maxSpeciesCount / 10);
    }

    private void moveBack(MyLineChart lineChart,
                          HashMap<String, Color> moleculesColorMap,
                          HashSet<Integer> fluxReactions,
                          HashSet<String> fluxMolecules) {
        if (intervalState[0] > 0) {
            intervalState[0]--;
            slider.setValue(intervalSize * (intervalState[0] + 1));
            updateEdgeWeightsOfIntervalGraph(intervalFluxes.get(intervalState[0]), graphView, graphEdgeMap, moleculesColorMap, fluxReactions, fluxMolecules);
        }
        if (intervalState[0] == 0) {
            backButton.setDisable(true);
        }
        if (intervalState[0] > 0) {
            backButton.setDisable(false);
        }
        forwardButton.setDisable(false);
        lineChart.removePolygon();
        lineChart.addPolygon(intervalState[0] * intervalSize, (intervalState[0] + 1) * intervalSize, maxSpeciesCount + maxSpeciesCount / 10);
    }

    private void playForward(MyLineChart lineChart,
                             HashMap<String, Color> moleculesColorMap,
                             HashSet<Integer> fluxReactions,
                             HashSet<String> fluxMolecules){
        if (intervalState[0] < times.size() - 1) { moveForward(lineChart, moleculesColorMap, fluxReactions, fluxMolecules); }
        delay(500, () -> {
            if (intervalState[0] < times.size() - 1) {
                playForward(lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
            }
        });
    }

    private void captureScreenShot(MySplitContainer splitContainer,
                                   MyLineChart lineChart,
                                   HashMap<String, Color> moleculesColorMap,
                                   HashSet<Integer> fluxReactions,
                                   HashSet<String> fluxMolecules) {
        if (intervalState[0] < times.size() - 1) { moveForward(lineChart, moleculesColorMap, fluxReactions, fluxMolecules); }
        delay(500, () -> {
            addImage(splitContainer);
            if (intervalState[0] < times.size() - 1) {
                captureScreenShot(splitContainer, lineChart, moleculesColorMap, fluxReactions, fluxMolecules);
            }
        });
    }

    private void addImage(MySplitContainer splitContainer) {
        WritableImage fxImage = splitContainer.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);
        images.add(bufferedImage);
    }

    private static GridPane getcontrolsGridPane(Button backButton, Button forwardButton,
                                                Button explortButton, Button generateMovieButton,
                                                Button exportCurrentFlux, Button exportAllFluxes) {
        GridPane controls = new GridPane();
        backButton.setMaxWidth(Double.MAX_VALUE);
        forwardButton.setMaxWidth(Double.MAX_VALUE);
        explortButton.setMaxWidth(Double.MAX_VALUE);
        generateMovieButton.setMaxWidth(Double.MAX_VALUE);
        exportCurrentFlux.setMaxWidth(Double.MAX_VALUE);
        exportAllFluxes.setMaxWidth(Double.MAX_VALUE);

        controls.add(backButton, 0, 0);
        controls.add(forwardButton, 1, 0);
        controls.add(explortButton, 0, 1);
        controls.add(generateMovieButton, 1, 1);
        controls.add(exportCurrentFlux,0,2);
        controls.add(exportAllFluxes,1,2);

        controls.setHgap(10);
        controls.setVgap(10);
        controls.setPadding(new Insets(10, 10, 10, 10));

        ColumnConstraints column4 = new ColumnConstraints();
        column4.setFillWidth(true);
        column4.setHgrow(Priority.ALWAYS);
        ColumnConstraints column5 = new ColumnConstraints();
        column5.setFillWidth(true);
        column5.setHgrow(Priority.ALWAYS);
        controls.getColumnConstraints().addAll(column4, column5);
        return controls;
    }

    private int getMax(List<TrajectoryState> trajectory) {
        int result = 0;
        for (TrajectoryState state : trajectory) {
            result = Math.max(result, Arrays.stream(state.getState())
                    .max()
                    .getAsInt());

        }
        return result;
    }

    private void updateEdgeWeight(SmartStylableNode stylableEdge, Color color, long newEdgeWeight) {
        String edgeStyle = "-fx-stroke-width: " + mapFluxesToWidths(newEdgeWeight) +
                "; -fx-stroke: " + FluxDigraphData.toRGBCode(color) + ";";
        stylableEdge.setStyle(edgeStyle);
        if (stylableEdge instanceof SmartGraphEdgeLine<?, ?>) {
            SmartGraphEdgeLine edgeLine = (SmartGraphEdgeLine) stylableEdge;
            SmartArrow stylableArrow = edgeLine.getAttachedArrow();
            updateEdgeArrow(stylableArrow, newEdgeWeight, edgeStyle);
        }
        if (stylableEdge instanceof SmartGraphEdgeCurve<?, ?>) {
            SmartGraphEdgeCurve edgeCurve = (SmartGraphEdgeCurve) stylableEdge;
            SmartArrow stylableArrow = edgeCurve.getAttachedArrow();
            updateEdgeArrow(stylableArrow, newEdgeWeight, edgeStyle);
        }
    }

    public void updateEdgeArrow(SmartArrow stylableArrow, long newEdgeWeight, String edgeStyle) {
        if (newEdgeWeight == 0) {
            String edgeStyleOpaque = edgeStyle + "-fx-opacity: 0; visibility: hidden;";
            stylableArrow.setStyle(edgeStyleOpaque);
        } else {
            stylableArrow.setStyle(edgeStyle + "-fx-opacity: 1.0; visibility: visible ;");
        }
    }

    private void updateEdgeWeight(SmartGraphPanel<GReaction, GEdge> graphView, GEdge edge, HashMap<String, Color> moleculesColorMap, long newEdgeWeight) {
        SmartStylableNode stylableEdge = graphView.getStylableEdge(edge);
        edge.setWeight(newEdgeWeight);
        Color color = moleculesColorMap.get(edge.getSpeciesName());
        updateEdgeWeight(stylableEdge, color, newEdgeWeight);
    }

    private void updateEdgeWeight(SmartGraphPanel<GReaction, GEdge> graphView,
                                  TripleIndex t,
                                  Map<TripleIndex, GEdge> graphEdgeMap,
                                  HashMap<String, Color> moleculesColorMap,
                                  long newEdgeWeight,
                                  HashSet<String> fluxMolecules) {
        GEdge edge = graphEdgeMap.get(t);
        if (edge != null && fluxMolecules.contains(edge.getSpeciesName())) {
            updateEdgeWeight(graphView, edge, moleculesColorMap, newEdgeWeight);
        }
    }

    private void updateEdgeWeightsOfIntervalGraph(Matrix<TripleIndex> matrix,
                                                  SmartGraphPanel<GReaction, GEdge> graphView,
                                                  Map<TripleIndex, GEdge> graphEdgeMap,
                                                  HashMap<String, Color> moleculesColorMap,
                                                  HashSet<Integer> fluxReactions,
                                                  HashSet<String> fluxMolecules) {
        for (TripleIndex t : matrix.keySet()) {
            if (fluxReactions.contains(t.getA()) && fluxReactions.contains(t.getB())) {
                updateEdgeWeight(graphView, t, graphEdgeMap, moleculesColorMap, matrix.get(t), fluxMolecules);
            }
        }
        graphView.update();
    }

    private String format(Color c) {
        int r = (int) (255 * c.getRed());
        int g = (int) (255 * c.getGreen());
        int b = (int) (255 * c.getBlue());

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private int mapFluxesToWidths(long fluxValue) {
        if (fluxValue == 0) {
            return 0;
        }
        long window = ((maxFluxWeight + 1) / 10 == 0 ? 1 : (maxFluxWeight + 1) / 10);
        return (int) (fluxValue / window) + 1;
    }

    public static void delay(long millis, Runnable continuation) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }

    private void saveDialog(Scene scene,WritableImage image){
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
            if (fileExtension.equals("png")) {
                //WritableImage image = scene.snapshot(null);
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void generateVideo(ArrayList<BufferedImage> images,File path) throws IOException {
        //https://github.com/jcodec/jcodec/blob/master/README.md
        //        SeekableByteChannel out = null;
        //        out = NIOUtils.writableFileChannel("/tmp/output.mp4");
        //        AWTSequenceEncoder encoder = new AWTSequenceEncoder(out, Rational.R(25, 1));

        for (int i = 0; i < times.size(); i++) {
            File file = new File(path, "graph" + i +".png");
            ImageIO.write(images.get(i), "png", file);
        }
    }
}


