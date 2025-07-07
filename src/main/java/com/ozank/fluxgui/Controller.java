package com.ozank.fluxgui;

import com.google.gson.Gson;
import com.ozank.dataElements.FluxData;
import com.ozank.dataElements.ProcessedFluxes;
import com.ozank.dataElements.TimeSeries;
import com.ozank.viewElements.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    Map<String,Integer> speciesMap;
    ProcessedFluxes fluxes;
    TimeSeries timeSeries;

    ObservableList<PlotSpecies> plotSpeciesData = FXCollections.observableArrayList();
    ObservableList<FluxSpecies> fluxSpeciesData = FXCollections.observableArrayList();
    ObservableList<FluxReaction> fluxReactionsData = FXCollections.observableArrayList();

    Button plotButton;
    Button plotCompleteFluxGraphButton;
    Button plotIntervalFluxGraphButton;

    private  String[] colors = {"#fd7f6f", "#7eb0d5", "#98D087", "#bd7ebe", "#ffb55a", "#00C6CF", "#beb9db", "#fdcce5", "#417285"};
    @FXML
    private HBox controlHBox;

    @FXML
    private VBox fluxReactionsVBox;

    @FXML
    private VBox fluxMoleculesVBox;

    @FXML
    private Button resetButton;

    @FXML
    private Button openButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetButton.setDisable(true);
    }

    @FXML
    protected void onOpenButtonClick() throws IOException {
            //Opening a dialog box
            resetButton.setDisable(false);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open");
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Flux files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            //Show save file dialog
            File file = fileChooser.showOpenDialog(controlHBox.getScene().getWindow());
            if (file != null) {
                String filePath = file.getAbsolutePath();
                Gson gson = new Gson();
                // Read JSON from a file
                try (Reader reader = new FileReader(filePath)) {
                    FluxData fluxData = gson.fromJson(reader, FluxData.class);
                    timeSeries = new TimeSeries(fluxData.getTimeSeries());
                    speciesMap = timeSeries.getSpeciesMap();
                    configurePlotParticipants();
                    fluxes = new ProcessedFluxes(fluxData.getFluxes(),fluxData.getReactions(),speciesMap);
                    initiateFluxControls();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            openButton.setDisable(true);
    }

    @FXML
    protected void onResetButtonClick() throws InterruptedException {
        reset();
    }

    private void configurePlotParticipants() {
        // https://www.heavy.ai/blog/12-color-palettes-for-telling-better-stories-with-your-data

        int k = 0;
        for (String name : speciesMap.keySet()) {
            plotSpeciesData.add(new PlotSpecies(name,colors[k % 9]));
            k++;
        }
        FXCollections.sort(plotSpeciesData);
    }


    public void initiateFluxControls(){
        CompleteFluxScene fluxScene = new CompleteFluxScene(fluxes,speciesMap);
        for (GReaction gReaction : fluxScene.getAllReactions()) {
            String reaction = fluxes.getReactions().get(gReaction.getId()).replaceFirst("~.*","");
            String rate = fluxes.getReactions().get(gReaction.getId()).replaceFirst(".*~ rate: ","");
            fluxReactionsData.add(new FluxReaction(gReaction.getId(),gReaction.getName(),"#B1DFF7",reaction,rate));
        }

        FXCollections.sort(fluxReactionsData);
        TableView fluxReactionsTableView = new TableView<>();
        TableColumn plotCol = new TableColumn("Include");
        TableColumn nameCol = new TableColumn("Id");
        TableColumn colorPickerCol = new TableColumn("Color");
        TableColumn reactionCol = new TableColumn("Reaction");
        TableColumn rateCol = new TableColumn("Rate");

        plotCol.setPrefWidth(75);
        plotCol.setResizable(false);
        colorPickerCol.setPrefWidth(120);
        colorPickerCol.setResizable(false);
        reactionCol.setPrefWidth(300);
        reactionCol.setResizable(true);
        reactionCol.setPrefWidth(100);
        reactionCol.setResizable(true);
        fluxReactionsTableView.getColumns().addAll(plotCol, nameCol, colorPickerCol,reactionCol,rateCol);
        plotCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, ComboBox<String>>("choiceComboBox"));
        nameCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, String>("name"));
        colorPickerCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, ColorPicker>("colorPicker"));
        reactionCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, String>("reaction"));
        rateCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, String>("rate"));

        fluxReactionsTableView.setItems(fluxReactionsData);
        fluxReactionsTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Label fluxReactionsTitle = new Label("Flux Graph Reactions");
        TextField fluxReactionsSearchTextField = new TextField();
        fluxReactionsSearchTextField.setPromptText("Search");
        fluxReactionsVBox.setAlignment(Pos.CENTER);
        fluxReactionsVBox.getChildren().addAll(fluxReactionsTitle, fluxReactionsSearchTextField,fluxReactionsTableView);
        fluxReactionsTableView.prefHeightProperty().bind(fluxReactionsVBox.heightProperty().multiply(0.94));

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        int k = 0;
        for (String name : speciesMap.keySet()) {
            fluxSpeciesData.add(new FluxSpecies(name,colors[k % 9]));
            k++;
        }

        FXCollections.sort(fluxSpeciesData);
        TableView fluxMoleculesTableView = new TableView<>();
        TableColumn plotColFluxMolecules = new TableColumn("Include");
        TableColumn nameColFluxMolecules = new TableColumn("Name");
        TableColumn colorPickerColFluxMolecules = new TableColumn("Color");
        TableColumn fluxCutOffColFluxMolecules = new TableColumn("Cut-off");
        plotColFluxMolecules.setPrefWidth(75);
        plotColFluxMolecules.setResizable(false);
        colorPickerColFluxMolecules.setPrefWidth(120);
        colorPickerColFluxMolecules.setResizable(false);
        fluxCutOffColFluxMolecules.setPrefWidth(100);
        fluxCutOffColFluxMolecules.setResizable(false);

        fluxMoleculesTableView.getColumns().addAll(plotColFluxMolecules, nameColFluxMolecules, colorPickerColFluxMolecules,fluxCutOffColFluxMolecules);
        plotColFluxMolecules.setCellValueFactory(new PropertyValueFactory<PlotSpecies, ComboBox<String>>("choiceComboBox"));
        nameColFluxMolecules.setCellValueFactory(new PropertyValueFactory<PlotSpecies, String>("name"));
        colorPickerColFluxMolecules.setCellValueFactory(new PropertyValueFactory<PlotSpecies, ColorPicker>("colorPicker"));
        fluxCutOffColFluxMolecules.setCellValueFactory(new PropertyValueFactory<PlotSpecies, Spinner<Integer>>("cutOffSpinner"));
        fluxMoleculesTableView.setItems(fluxSpeciesData);
        fluxMoleculesTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        fluxMoleculesTableView.prefHeightProperty().bind(fluxReactionsVBox.heightProperty().multiply(0.94));
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        GridPane fluxMoleculesControls = new GridPane();
        fluxMoleculesControls.setPadding(new Insets(1, 1, 1, 1));
        TextField fluxMoleculesSearchTextField = new TextField();
        Label cutOffLabel = new Label("  All cut-off");
        cutOffLabel.prefWidth(100);
        TextField allCutOffTextField = new TextField();
        allCutOffTextField.prefWidth(50);
        allCutOffTextField.setPromptText("default is 0");

        fluxMoleculesSearchTextField.setPromptText("Search");
        fluxMoleculesControls.add(fluxMoleculesSearchTextField,0,0);
        fluxMoleculesControls.add(cutOffLabel,1,0);
        fluxMoleculesControls.add(allCutOffTextField,2,0);
        fluxMoleculesControls.setHgap(5);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setFillWidth(true);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPrefWidth(60);
        column2.setFillWidth(true);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPrefWidth(80);
        column3.setFillWidth(true);
        fluxMoleculesControls.getColumnConstraints().addAll(column1, column2, column3);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        plotButton = new Button("Plot");
        plotCompleteFluxGraphButton = new Button("Flux Graph");
        plotIntervalFluxGraphButton = new Button("Interval Flux Graph");
        controlHBox.getChildren().addAll(plotButton,plotCompleteFluxGraphButton,plotIntervalFluxGraphButton);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Label fluxMoleculesTitle = new Label("Flux Graph Species");
        fluxMoleculesVBox.setAlignment(Pos.CENTER);
        fluxMoleculesVBox.getChildren().addAll(fluxMoleculesTitle, fluxMoleculesControls,fluxMoleculesTableView);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        fluxReactionsSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            fluxReactionsTableView.setItems(filterFluxReactionsData(newValue));
        });

        fluxMoleculesSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            fluxMoleculesTableView.setItems(filterFluxSepeciesData(newValue));
        });

        plotButton.setOnAction(event -> {
            LineChartFactory lineChartFactory = new LineChartFactory(
                    speciesMap,
                    FluxSpecies.getFluxGraphSpecies(),
                    FluxSpecies.getColorMap(),
                    "",
                    timeSeries.getTimeSeries(),0,
                    timeSeries.getEndTime());
            MyLineChart lineChart = lineChartFactory.getLineChart();
            new PlotScene(lineChart);
        });

        plotCompleteFluxGraphButton.setOnAction(event -> {
            int cutOffValue = checkAllCutOffTextField(allCutOffTextField.getText());
            if (cutOffValue >= 0) {
                fluxScene.draw(FluxSpecies.getFluxGraphSpecies(),
                        FluxReaction.getFluxReactions(),
                        FluxSpecies.getColorMap(),
                        FluxReaction.getColorMap(),
                        FluxSpecies.getCutOffMap(),
                        cutOffValue);
            }
        });

        plotIntervalFluxGraphButton.setOnAction(event -> {
            IntervalFluxScene intervalFluxScene = new IntervalFluxScene(fluxes,timeSeries,speciesMap);

            LineChartFactory lineChartFactory = new LineChartFactory(
                    speciesMap,
                    FluxSpecies.getFluxGraphSpecies(),
                    FluxSpecies.getColorMap(),
                    "",
                    timeSeries.getTimeSeries(),0,
                    timeSeries.getEndTime());
            MyLineChart lineChart = lineChartFactory.getLineChart();
            int cutOffValue = checkAllCutOffTextField(allCutOffTextField.getText());

            if (cutOffValue >= 0) {
                intervalFluxScene.draw(FluxSpecies.getFluxGraphSpecies(),
                        FluxReaction.getFluxReactions(),
                        FluxSpecies.getColorMap(),
                        FluxReaction.getColorMap(),
                        FluxSpecies.getCutOffMap(),
                        cutOffValue,
                        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                        lineChart);
            }
        });
    }

    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public ObservableList<PlotSpecies> filterPlotSpeciesData(String searchText) {
        ObservableList<PlotSpecies> result = FXCollections.observableArrayList();
        for (PlotSpecies m : plotSpeciesData) {
            if (m.getName()
                    .substring(0, Math.min(m.getName().length(),searchText.length()))
                    .equals(searchText)) {
                result.add(m);
            }
        }
        return result;
    }

    public ObservableList<FluxSpecies> filterFluxSepeciesData(String searchText) {
        ObservableList<FluxSpecies> result = FXCollections.observableArrayList();
        for (FluxSpecies m : fluxSpeciesData) {
            if (m.getName()
                    .substring(0, Math.min(m.getName().length(),searchText.length()))
                    .equals(searchText)) {
                result.add(m);
            }
        }
        return result;
    }

    public ObservableList<FluxReaction> filterFluxReactionsData(String searchText) {
        ObservableList<FluxReaction> result = FXCollections.observableArrayList();
        for (FluxReaction m : fluxReactionsData) {
            if (m.getName()
                    .substring(0, Math.min(m.getName().length(),searchText.length()))
                    .equals(searchText)) {
                result.add(m);
            }
        }
        return result;
    }

    private boolean checkPlotBeginEnd(String startTime, String endTime){
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setAlertType(Alert.AlertType.WARNING);
        double startTimeNumber;
        double endTimeNumber;
        if (!isDouble(startTime)) {
            a.setContentText("Plot start time must be a number.");
            a.show();
            return false;
        } else {
            startTimeNumber = Double.parseDouble(startTime);
        }
        if (!isDouble(endTime)) {
            a.setContentText("Plot end time must be a number.");
            a.show();
            return false;
        } else {
            endTimeNumber = Double.parseDouble(endTime);
        }
        if (startTimeNumber < 0){
            a.setContentText("Plot start time must be positive.");
            a.show();
            return false;
        }
        if (startTimeNumber >= endTimeNumber){
            a.setContentText("Plot start time must be smaller than plot end time.");
            a.show();
            return false;
        }
        return true;
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int checkAllCutOffTextField(String cutOffString){
        if (isInteger(cutOffString)){
            return Integer.parseInt(cutOffString);
        } else {
            if (cutOffString.equals("")){
                return 0;
            } else {
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setAlertType(Alert.AlertType.WARNING);
                a.setContentText("\"" + cutOffString + "\"" + "is not an integer. "+
                        "Cut-off value must be a positive integer.");
                a.show();
                return -1;
            }
        }
    }

    private void reset(){
        speciesMap = null;
        fluxes = null;
        timeSeries = null;
        plotSpeciesData = FXCollections.observableArrayList();
        fluxReactionsData = FXCollections.observableArrayList();
        fluxSpeciesData = FXCollections.observableArrayList();
        fluxReactionsVBox.getChildren().clear();
        fluxMoleculesVBox.getChildren().clear();
        controlHBox.getChildren().remove(plotButton);
        controlHBox.getChildren().remove(plotCompleteFluxGraphButton);
        controlHBox.getChildren().remove(plotIntervalFluxGraphButton);
        plotButton = null;
        plotCompleteFluxGraphButton = null;
        plotIntervalFluxGraphButton = null;
        PlotSpecies.clearColorMap();
        openButton.setDisable(false);
        resetButton.setDisable(true);
        FluxSpecies.resetFluxGraphSpecies();
        FluxSpecies.resetColorMap();
        FluxSpecies.resetCutOffMap();
    }
}
