package com.ozank.fluxgui;

import com.ozank.simulation.Datasource;
import com.ozank.simulation.ModelSpecies;
import com.ozank.simulation.Simulation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    Simulation simulation;
    List<String> modelSpecies;
    ObservableList<ModelSpecies> modelSpeciesData = FXCollections.observableArrayList();

    @FXML
    private TextArea modelCodeArea;

    @FXML
    private Spinner fontSpinner;

    @FXML
    private VBox plotSelectVBox;

    @FXML
    private CheckBox timeSeriesCheckBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fontSpinner.valueProperty().addListener((obs,old,newFontValue)->
                modelCodeArea.setStyle("-fx-font-size: " + newFontValue.toString() + "pt;"));
        modelCodeArea.setText("// this is a comment line\n" +
                "reactions\n" +
                "r1 : predator -> : 100;\n" +
                "r2 :  prey -> prey + prey : 300;\n" +
                "r3 : predator + prey -> predator + predator: 1;\n" +
                "\n" +
                "initial state\n" +
                "predator: 100;\n" +
                "prey: 100;\n\n" +
                "end time 0.75;\n\n" +
                "scaling factor 10;\n\n" +
                "interval 0.25;\n");
    }

    @FXML
    protected void onRunButtonClick() {
        String model_code = modelCodeArea.getText();
        simulation = new Simulation(model_code);
        modelSpecies = simulation.getMolecules();
        if (timeSeriesCheckBox.isSelected()) {
            displayModelParticipantsAndSelect();
        }
    }

    @FXML
    protected void onResetButtonClick() {
        modelSpecies = null;
        simulation = null;
        modelCodeArea.setText("");
        modelSpeciesData = FXCollections.observableArrayList();
        plotSelectVBox.getChildren().clear();
    }


    private void displayModelParticipantsAndSelect() {
        for (String name : modelSpecies) {
            modelSpeciesData.add(new ModelSpecies(name));
        }
        FXCollections.sort(modelSpeciesData);
        TableView tableView = new TableView<>();
        TableColumn plotCol = new TableColumn("Include");
        TableColumn nameCol = new TableColumn("Name");
        tableView.getColumns().addAll(plotCol, nameCol);
        plotCol.setCellValueFactory(new PropertyValueFactory<ModelSpecies, ComboBox<String>>("choiceComboBox"));
        nameCol.setCellValueFactory(new PropertyValueFactory<ModelSpecies, String>("name"));
        tableView.setItems(modelSpeciesData);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        VBox tableViewVbox = new VBox();
        GridPane controls = new GridPane();
        TextField searchTextField = new TextField();
        Button plotButton = new Button("Plot");
        plotButton.prefWidth(60);
        searchTextField.setPromptText("Search");
        controls.add(searchTextField,0,0);
        controls.add(plotButton,1,0);

        controls.setHgap(5);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setFillWidth(true);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setFillWidth(true);
        controls.getColumnConstraints().addAll(column1, column2);

        tableViewVbox.getChildren().add(controls);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            tableView.setItems(filterModelSpeciesData(newValue));
        });
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Label moleculesTitle = new Label("Model Species");
        plotSelectVBox.getChildren().add(tableViewVbox);
        tableViewVbox.setAlignment(Pos.CENTER);
        tableViewVbox.getChildren().addAll(moleculesTitle, tableView);
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


}