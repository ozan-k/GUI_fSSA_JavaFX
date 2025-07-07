package com.ozank.fluxgui;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;


public class PlotScene {
    private final Stage dialogStage;

    public PlotScene(LineChart linechart) {
        dialogStage = new Stage();
        //dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.initModality(Modality.NONE);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        //Setting title to the Stage
        dialogStage.setTitle("Time series plot");

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




}
