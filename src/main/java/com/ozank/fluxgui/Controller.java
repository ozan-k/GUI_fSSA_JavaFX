package com.ozank.fluxgui;

import com.ozank.lexerParser.ModelBuilder;
import com.ozank.simulator.*;
import com.ozank.viewElements.FluxSpecies;
import com.ozank.viewElements.FluxReaction;
import com.ozank.viewElements.PlotSpecies;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class Controller implements Initializable {
    SimulationSSA simulation;
    Map<String,Integer> speciesMap;
    //List<String> modelSpecies;

    ObservableList<PlotSpecies> plotSpeciesData = FXCollections.observableArrayList();
    ObservableList<FluxSpecies> fluxSpeciesData = FXCollections.observableArrayList();
    ObservableList<FluxReaction> fluxReactionsData = FXCollections.observableArrayList();

    @FXML
    private TextArea modelCodeArea;

    @FXML
    private Spinner fontSpinner;

    @FXML
    private VBox plotSelectVBox;

    @FXML
    private CheckBox timeSeriesCheckBox;

    @FXML
    private VBox fluxReactionsVBox;

    @FXML
    private VBox fluxMoleculesVBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        modelCodeArea.setStyle("-fx-font-size:10pt;");
        fontSpinner.valueProperty().addListener((obs,old,newFontValue)->
                modelCodeArea.setStyle("-fx-font-size: " + newFontValue.toString() + "pt;"));
        modelCodeArea.setText(getSampleModel());
    }

    private void reset(){
        speciesMap = null;
        simulation = null;
        plotSpeciesData = FXCollections.observableArrayList();
        fluxReactionsData = FXCollections.observableArrayList();
        fluxSpeciesData = FXCollections.observableArrayList();
        plotSelectVBox.getChildren().clear();
        fluxReactionsVBox.getChildren().clear();
        fluxMoleculesVBox.getChildren().clear();
        PlotSpecies.clearColorMap();
    }

    @FXML
    protected void onRunButtonClick() throws InterruptedException {
        reset();
        String model_code = modelCodeArea.getText();
        ModelBuilder builder = new ModelBuilder(model_code);
        SimulationModel model = new SimulationModel(builder.getReactions(),builder.getInitialState());
        int every = builder.getEvery();
        double interval = builder.getInterval();
        simulation = new SimulationSSA(model,every,interval);
        speciesMap = model.getSpeciesMap();
        if (timeSeriesCheckBox.isSelected()) {
            displayPlotParticipantsAndSelect();
            simulation.simulateWithTimeLimit(builder.getEndTime(),true,this);
        } else {
            displayPlotParticipantsAndSelect();
            simulation.simulateWithTimeLimit(builder.getEndTime(),false,this);
        }
    }

    @FXML
    protected void onResetButtonClick() {
        reset();
        modelCodeArea.setText(getSampleModel());
    }

    private void displayPlotParticipantsAndSelect() {
        // https://www.heavy.ai/blog/12-color-palettes-for-telling-better-stories-with-your-data
        String[] colors = {"#fd7f6f", "#7eb0d5", "#b2e061", "#bd7ebe", "#ffb55a", "#ffee65", "#beb9db", "#fdcce5", "#8bd3c7"};
        int k = 0;
        for (String name : speciesMap.keySet()) {
            plotSpeciesData.add(new PlotSpecies(name,colors[k % 9]));
            k++;
        }
        FXCollections.sort(plotSpeciesData);
        TableView tableView = new TableView<>();
        TableColumn plotCol = new TableColumn("Include");
        TableColumn nameCol = new TableColumn("Name");
        TableColumn colorPickerCol = new TableColumn("Color");
        plotCol.setPrefWidth(75);
        plotCol.setResizable(false);
        colorPickerCol.setPrefWidth(120);
        colorPickerCol.setResizable(false);
        tableView.getColumns().addAll(plotCol, nameCol, colorPickerCol);
        plotCol.setCellValueFactory(new PropertyValueFactory<PlotSpecies, ComboBox<String>>("choiceComboBox"));
        nameCol.setCellValueFactory(new PropertyValueFactory<PlotSpecies, String>("name"));
        colorPickerCol.setCellValueFactory(new PropertyValueFactory<PlotSpecies, ColorPicker>("colorPicker"));
        tableView.setItems(plotSpeciesData);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        GridPane controls = new GridPane();
        controls.setPadding(new Insets(1, 1, 1, 1));
        TextField searchTextField = new TextField();
        Button plotButton = new Button("Plot");
        plotButton.prefWidth(60);
        Button exportButton = new Button("Export");
        exportButton.prefWidth(60);

        searchTextField.setPromptText("Search");
        controls.add(searchTextField,0,0);
        controls.add(plotButton,1,0);
        controls.add(exportButton,2,0);
        controls.setHgap(5);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setFillWidth(true);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setFillWidth(true);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setFillWidth(true);
        controls.getColumnConstraints().addAll(column1, column2, column3);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        GridPane plotControls = new GridPane();
        TextField plotTimeUnitTextField = new TextField();
        TextField startTimeTextField = new TextField();
        TextField endTimeTextField = new TextField();

        plotTimeUnitTextField.setPromptText("Plot time unit");
        startTimeTextField.setPromptText("Plot start time");
        endTimeTextField.setPromptText("Plot end time");
        plotControls.add(plotTimeUnitTextField,0,0);
        plotControls.add(startTimeTextField,1,0);
        plotControls.add(endTimeTextField,2,0);
        plotControls.setHgap(5);
        plotControls.setPadding(new Insets(1, 1, 1, 1));

        ColumnConstraints column4 = new ColumnConstraints();
        column4.setFillWidth(true);
        column4.setHgrow(Priority.ALWAYS);
        ColumnConstraints column5 = new ColumnConstraints();
        column5.setFillWidth(true);
        column5.setHgrow(Priority.ALWAYS);
        ColumnConstraints column6 = new ColumnConstraints();
        column6.setFillWidth(true);
        column6.setHgrow(Priority.ALWAYS);
        plotControls.getColumnConstraints().addAll(column4, column5, column6);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Label moleculesTitle = new Label("Plot Species");
        plotSelectVBox.setAlignment(Pos.CENTER);
        plotSelectVBox.getChildren().addAll(controls,moleculesTitle, tableView,plotControls);

        plotButton.setOnAction(event -> {
            //System.out.println(ModelSpecies.getPlotModelSpecies());
            //System.out.println(speciesMap.keySet());
            double endTime = getEndTime(simulation.getTrajectory());
            String startTimeString = (startTimeTextField.getText().isEmpty() ?
                            "0" : startTimeTextField.getText() );
            String endTimeString = (endTimeTextField.getText().isEmpty() ?
                    Double.toString(endTime)
                    : endTimeTextField.getText() );

            if (checkPlotBeginEnd(startTimeString,endTimeString)) {
                String timeUnit = (plotTimeUnitTextField.getText().isEmpty() ? "" :
                        " (" + plotTimeUnitTextField.getText() + ")");
                PlotScene pScene = new PlotScene(speciesMap,
                        PlotSpecies.getPlotSpecies(),
                        PlotSpecies.getColorMap(),
                        timeUnit,
                        simulation.getTrajectory(),
                        Double.parseDouble(startTimeString),
                        Double.parseDouble(endTimeString));
            }
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            tableView.setItems(filterPlotSpeciesData(newValue));
        });

        exportButton.setOnAction(event -> {
                //Opening a dialog box
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save");
                //Set extension filter for text files
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TSV files (*.tsv)", "*.tsv");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(plotSelectVBox.getScene().getWindow());
                if (file != null) {
                    // System.out.println(file);
                    simulation.writeToFile(file.toString());
                }
        });
    }

    public void initiateFluxControls(){
        CompleteFluxScene fluxScene = new CompleteFluxScene(simulation);
        for (GReaction gReaction : fluxScene.getAllReactions()) {
            fluxReactionsData.add(new FluxReaction(gReaction.getId(),gReaction.getName(),"#B1DFF7"));
        }
        FXCollections.sort(fluxReactionsData);
        TableView fluxReactionsTableView = new TableView<>();
        TableColumn plotCol = new TableColumn("Include");
        //TableColumn idCol = new TableColumn("Id");
        TableColumn nameCol = new TableColumn("Name");
        TableColumn colorPickerCol = new TableColumn("Color");
        plotCol.setPrefWidth(75);
        plotCol.setResizable(false);
        colorPickerCol.setPrefWidth(120);
        colorPickerCol.setResizable(false);
        fluxReactionsTableView.getColumns().addAll(plotCol, nameCol, colorPickerCol);
        plotCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, ComboBox<String>>("choiceComboBox"));
        //idCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, String>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, String>("name"));
        colorPickerCol.setCellValueFactory(new PropertyValueFactory<FluxReaction, ColorPicker>("colorPicker"));
        fluxReactionsTableView.setItems(fluxReactionsData);
        fluxReactionsTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Label fluxReactionsTitle = new Label("Flux Graph Reactions");
        TextField fluxReactionsSearchTextField = new TextField();
        fluxReactionsSearchTextField.setPromptText("Search");
        fluxReactionsVBox.setAlignment(Pos.CENTER);
        fluxReactionsVBox.getChildren().addAll(fluxReactionsTitle, fluxReactionsSearchTextField,fluxReactionsTableView);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        for (String name : speciesMap.keySet()) {
            fluxSpeciesData.add(new FluxSpecies(name,"#9BD087"));
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

        GridPane fluxControls = new GridPane();
        Button plotCompleteFluxGraphButton = new Button("Flux Graph");
        plotCompleteFluxGraphButton.setMaxWidth(Double.MAX_VALUE);
        Button plotIntervalFluxGraphButton = new Button("Interval Flux Graph");
        plotIntervalFluxGraphButton.setMaxWidth(Double.MAX_VALUE);
        Button explortFluxesButton = new Button("Export");
        explortFluxesButton.setMaxWidth(Double.MAX_VALUE);

        fluxControls.add(plotCompleteFluxGraphButton,0,0);
        fluxControls.add(plotIntervalFluxGraphButton,1,0);
        fluxControls.add(explortFluxesButton,2,0);
        fluxControls.setHgap(5);
        fluxControls.setPadding(new Insets(1, 1, 1, 1));

        ColumnConstraints column4 = new ColumnConstraints();
        column4.setFillWidth(true);
        column4.setHgrow(Priority.ALWAYS);
        ColumnConstraints column5 = new ColumnConstraints();
        column5.setFillWidth(true);
        column5.setHgrow(Priority.ALWAYS);
        ColumnConstraints column6 = new ColumnConstraints();
        column6.setFillWidth(true);
        column6.setHgrow(Priority.ALWAYS);
        fluxControls.getColumnConstraints().addAll(column4, column5, column6);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        Label fluxMoleculesTitle = new Label("Flux Graph Species");
        fluxMoleculesVBox.setAlignment(Pos.CENTER);
        fluxMoleculesVBox.getChildren().addAll(fluxMoleculesTitle, fluxMoleculesControls,fluxMoleculesTableView,fluxControls);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        fluxReactionsSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            fluxReactionsTableView.setItems(filterFluxReactionsData(newValue));
        });

        fluxMoleculesSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            fluxMoleculesTableView.setItems(filterFluxSepeciesData(newValue));
        });

        plotCompleteFluxGraphButton.setOnAction(event -> {
           // System.out.println(FluxSpecies.getFluxGraphSpecies().getClass());
            String cutOffString = allCutOffTextField.getText();
            boolean semafor =false;
            int cutOffValue = 0;
            if (isInteger(cutOffString)){
                cutOffValue = Integer.parseInt(cutOffString);
                semafor = true;
            } else {
                if (cutOffString.equals("")){
                    semafor = true;
                } else {
                    Alert a = new Alert(Alert.AlertType.NONE);
                    a.setAlertType(Alert.AlertType.WARNING);
                    a.setContentText("\"" + cutOffString + "\"" + "is not an integer. "+
                            "Cut-off value must be a positive integer.");
                    a.show();
                }
            }
            if (semafor) {
                fluxScene.draw(FluxSpecies.getFluxGraphSpecies(),
                        FluxReaction.getFluxReactions(),
                        FluxSpecies.getColorMap(),
                        FluxReaction.getColorMap(),
                        FluxSpecies.getCutOffMap(),
                        cutOffValue);
            }
        });

        explortFluxesButton.setOnAction(event -> {
            //Opening a dialog box
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save");
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(fluxMoleculesVBox.getScene().getWindow());
            if (file != null) {
                // System.out.println(file);
                saveTextToFile(simulation.allFluxesToJson(),file);
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

    private String getSampleModel(){
        return "// this is a comment\n" +
                "reactions\n" +
                "r1 : predator -> : 100;\n" +
                "r2 :  prey -> prey + prey : 300;\n" +
                "r3 : predator + prey -> predator + predator: 1;\n" +
                "\n" +
                "initial state\n" +
                "predator: 100;\n" +
                "prey: 100;\n\n" +
                "end time 0.75;\n\n" +
                "every 2;\n\n" +
                "//scaling factor 10;\n\n" +
                "interval 0.05;\n";
    }

    private double getEndTime(List< TrajectoryState > trajectory){
        return trajectory.get(trajectory.size() - 1).getTime();
    }

}




