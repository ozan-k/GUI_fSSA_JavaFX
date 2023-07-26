package com.ozank.fluxgui;

import com.ozank.lexerParser.ModelBuilder;
import com.ozank.simulator.*;
import com.ozank.viewElements.Flux;
import com.ozank.viewElements.ModelSpecies;
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
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    SimulationSSA simulation;
    Map<String,Integer> speciesMap;
    //List<String> modelSpecies;

    ObservableList<ModelSpecies> modelSpeciesData = FXCollections.observableArrayList();

    @FXML
    private TextArea modelCodeArea;

    @FXML
    private Button fluxButton;

    @FXML
    private Spinner fontSpinner;

    @FXML
    private VBox plotSelectVBox;

    @FXML
    private CheckBox timeSeriesCheckBox;

    @FXML
    private VBox completeFluxesVBox;

    @FXML
    private CheckBox autoLayoutCheckBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        modelCodeArea.setStyle("-fx-font-size:10pt;");
        fontSpinner.valueProperty().addListener((obs,old,newFontValue)->
                modelCodeArea.setStyle("-fx-font-size: " + newFontValue.toString() + "pt;"));
        modelCodeArea.setText(getSampleModel());
        fluxButton.setDisable(true);
    }

    @FXML
    protected void onRunButtonClick() throws InterruptedException {
        speciesMap = null;
        modelSpeciesData = FXCollections.observableArrayList();
        plotSelectVBox.getChildren().clear();
        ModelSpecies.clearColorMap();
        String model_code = modelCodeArea.getText();
        ModelBuilder builder = new ModelBuilder(model_code);
        SimulationModel model = new SimulationModel(builder.getReactions(),builder.getInitialState());
        //model.printReactions();
        //model.printState();
        int every = builder.getEvery();
        double interval = builder.getInterval();
        simulation = new SimulationSSA(model,every,interval);
        speciesMap = model.getSpeciesMap();
        if (timeSeriesCheckBox.isSelected()) {
            displayModelParticipantsAndSelect();
            simulation.simulateWithTimeLimit(builder.getEndTime(),true);
        } else {
            displayModelParticipantsAndSelect();
            simulation.simulateWithTimeLimit(builder.getEndTime(),false);
        }
        fluxButton.setDisable(false);

    }

    @FXML
    protected void onFluxButtonClick() {
        plotCompleteFlux();
    }


    private void displayModelParticipantsAndSelect() {
        // https://www.heavy.ai/blog/12-color-palettes-for-telling-better-stories-with-your-data
        String[] colors = {"#fd7f6f", "#7eb0d5", "#b2e061", "#bd7ebe", "#ffb55a", "#ffee65", "#beb9db", "#fdcce5", "#8bd3c7"};
        int k = 0;
        for (String name : speciesMap.keySet()) {
            modelSpeciesData.add(new ModelSpecies(name,colors[k % 9]));
            k++;
        }
        FXCollections.sort(modelSpeciesData);
        TableView tableView = new TableView<>();
        TableColumn plotCol = new TableColumn("Include");
        TableColumn nameCol = new TableColumn("Name");
        TableColumn colorPickerCol = new TableColumn("Color");
        tableView.getColumns().addAll(plotCol, nameCol, colorPickerCol);
        plotCol.setCellValueFactory(new PropertyValueFactory<ModelSpecies, ComboBox<String>>("choiceComboBox"));
        nameCol.setCellValueFactory(new PropertyValueFactory<ModelSpecies, String>("name"));
        colorPickerCol.setCellValueFactory(new PropertyValueFactory<ModelSpecies, ColorPicker>("colorPicker"));
        tableView.setItems(modelSpeciesData);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        VBox tableViewVbox = new VBox();
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

        Label moleculesTitle = new Label("Model Species");
        plotSelectVBox.getChildren().add(tableViewVbox);
        tableViewVbox.setAlignment(Pos.CENTER);
        tableViewVbox.getChildren().add(controls);
        tableViewVbox.getChildren().addAll(moleculesTitle, tableView);
        tableViewVbox.getChildren().add(plotControls);

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
                        ModelSpecies.getPlotModelSpecies(),
                        ModelSpecies.getColorMap(),
                        timeUnit,
                        simulation.getTrajectory(),
                        Double.parseDouble(startTimeString),
                        Double.parseDouble(endTimeString));
            }
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            tableView.setItems(filterModelSpeciesData(newValue));
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

    public ObservableList<ModelSpecies> filterModelSpeciesData(String searchText) {
        ObservableList<ModelSpecies> result = FXCollections.observableArrayList();
        for (ModelSpecies m : modelSpeciesData) {
            if (m.getName()
                    .substring(0, Math.min(m.getName().length(),searchText.length()))
                    .equals(searchText)) {
                result.add(m);
            }
        }
        return result;
    }


    boolean checkPlotBeginEnd(String startTime, String endTime){
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

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
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
                "interval 0.25;\n";
    }
    private double getEndTime(List< TrajectoryState > trajectory){
        return trajectory.get(trajectory.size() - 1).getTime();
    }

    // ========================================================

    private static String styleSheet =
                    "graph {"+
                    "	padding: 60px;"+
                    "}" +
                    "node {"+
                    "shape: box;" +
                    "size: 15px, 20px;" +
                    "fill-mode: plain;   /* Default.          */" +
                    "fill-color: red;    /* Default is black. */" +
                    "stroke-mode: plain; /* Default is none.  */" +
                    "stroke-color: blue; /* Default is black. */" +
                    "}";

    public void plotCompleteFlux(){
        SimulationModel model = simulation.getModel();
        Map<String, Flux> edgeMap = new HashMap<>();

        // https://graphstream-project.org/doc/Tutorials/Getting-Started/
        // https://graphstream-project.org/doc/Tutorials/Graph-Visualisation/
        Graph graph  = new MultiGraph("completeFluxes");
        //graph.setAttribute("ui.stylesheet", "graph { fill-color: red; }");

        FxViewer viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        // viewer.disableAutoLayout();
        // Do some work ...
        viewer.enableAutoLayout();
        autoLayoutCheckBox.selectedProperty().addListener((event) -> {
            System.out.println(autoLayoutCheckBox.isSelected());
            if (!autoLayoutCheckBox.isSelected()){
                viewer.disableAutoLayout(); }
            else {
                viewer.enableAutoLayout();
            }
        });


        graph.setStrict(false);
        graph.setAutoCreate( true );
        for (TripleIndex t : simulation.getF().keySet()){
            String edgeId = t.toString();
            int a = t.getA();
            int b = t.getB();
            int c = t.getC();
            graph.addEdge(edgeId,Integer.toString(a),Integer.toString(b),true);
            int weight = simulation.getF().get(t);
            double edgeWeight = (double) Math.log10(weight);
            //styleSheet = styleSheet + "edge#"+ edgeId + "{size: "+ edgeWeight +";}";
            graph.getEdge(edgeId).setAttribute("ui.style","size: " + edgeWeight +";");
            edgeMap.put(edgeId, new Flux(
                    a,model.getReactionNames()[a],
                    b,model.getReactionNames()[b],
                    c,model.getMoleculesList().get(c),
                    weight));
        };

       for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }

        graph.setAttribute( "ui.antialias" );
        graph.setAttribute( "ui.quality" );
        graph.setAttribute( "ui.stylesheet", styleSheet );


        FxViewPanel v =  (FxViewPanel) viewer.addDefaultView( false ) ;
        completeFluxesVBox.getChildren().add(v);


    }


}