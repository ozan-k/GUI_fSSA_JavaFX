package com.ozank.fluxgui;

import com.ozank.simulator.TrajectoryState;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.*;
import java.util.stream.Collectors;

public class PlotScene {
    private final Stage dialogStage;

    public PlotScene(Map<String, Integer> speciesMap,
                     Set<String> plotSpecies,
                     Map<String,Color> colorMap,
                     String plotTimeUnit,
                     List<TrajectoryState> trajectory,
                     double startTime,
                     double endTime) {
        dialogStage = new Stage();
        //dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.initModality(Modality.NONE);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        //Setting title to the Stage
        dialogStage.setTitle("Time series plot");

        trajectory = filterTrajectory(trajectory,startTime,endTime);
        int max = getMax(trajectory);

        //Defining the x axis
        NumberAxis xAxis = new NumberAxis(startTime,endTime, endTime/6);

        xAxis.setLabel("Time" + plotTimeUnit);

        //Defining the y axis

        NumberAxis yAxis = new NumberAxis   (0, max+(max/10), max/6);
        yAxis.setLabel("Counts");

        //Creating the line chart
        LineChart linechart = new LineChart(xAxis, yAxis);
        linechart.setCreateSymbols(false);

        int interval = (trajectory.size() / 2000)+1;
        int count = interval;

        for (String modelSpecies : plotSpecies) {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series series = new XYChart.Series();
            //Setting the name to the line (series)
            series.setName(modelSpecies);
            //series.setName("No of schools in an year");
            for (TrajectoryState state : trajectory) {
                if (count == interval) {
                    series.getData().add(
                            new XYChart.Data(
                                    state.getTime(),
                                    state.getState()[speciesMap.get(modelSpecies)]
                            )
                    );
                    // System.out.println(state.getState()[1]);
                    count = 0;
                }
                count++;
            }
            //Setting the data to Line chart
            linechart.getData().add(series);
        }

        // https://stackoverflow.com/questions/48663754/javafx-linechart-dynamically-change-legend-color
        int lineCount = 0;
        StringBuilder builder = new StringBuilder("");
        for (String modelSpecies : plotSpecies){
            lineCount++;
            builder.append(String.format("CHART_COLOR_" + lineCount + ": %s ;\n",format(colorMap.get(modelSpecies))));
        }
        linechart.setStyle(builder.toString());
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        VBox vb = new VBox();
        vb.setFillWidth(true);
        HBox hb = new HBox();
        hb.getChildren().add(linechart);
        hb.setFillHeight(true);
        vb.getChildren().add(hb);

        HBox.setHgrow(linechart, Priority.ALWAYS);
        VBox.setVgrow(hb, Priority.ALWAYS);

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

        exportButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
            fileChooser.getExtensionFilters().addAll(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(bPane.getScene().getWindow());
            if (file != null) {
                String fileName = file.getName();
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, file.getName().length()).toLowerCase();
                WritableImage image = linechart.snapshot(new SnapshotParameters(), null);
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

        //Adding scene to the stage
        dialogStage.setScene(scene);
        dialogStage.centerOnScreen();
        dialogStage.setResizable(true);
        //Displaying the contents of the stage
        dialogStage.show();
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

    private String format(Color c) {
        int r = (int) (255 * c.getRed()) ;
        int g = (int) (255 * c.getGreen()) ;
        int b = (int) (255 * c.getBlue()) ;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private List<TrajectoryState> filterTrajectory(List<TrajectoryState> trajectory,
                                                   double startTime,
                                                   double endTime){
        return trajectory.stream().filter(state -> (state.getTime()> startTime && state.getTime() < endTime)).collect(Collectors.toList());
    }


}
