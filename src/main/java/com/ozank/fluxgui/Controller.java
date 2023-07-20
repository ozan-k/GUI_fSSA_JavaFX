package com.ozank.fluxgui;

import com.ozank.lexerParser.ModelBuilder;
import com.ozank.simulation.Simulation;
import com.ozank.simulator.SimulationModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    Simulation simulation;
    List<String> molecules;

    @FXML
    private TextArea modelCodeArea;

    @FXML
    private Spinner fontSpinner;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fontSpinner.valueProperty().addListener((obs,old,newFontValue)->
                modelCodeArea.setStyle("-fx-font-size: " + newFontValue.toString() + "pt;"));
        modelCodeArea.setText("// this is a comment line\n" +
                "     reactions\n" +
                "r1 : predator -> : 100;\n" +
                "r2 :  prey -> prey + prey : 300;\n" +
                "// and another comment line\n" +
                "r3 : predator + prey -> predator + predator: 1;\n" +
                "\n" +
                "     initial state\n" +
                "     predator: 100;\n" +
                "     prey: 100;\n" +
                "// here is another comment line\n" +
                "     end time\n" +
                "0.75;\n" +
                "scaling factor 10;\n" +
                "interval 0.25;");
    }

    @FXML
    protected void onRunButtonClick() {
        String model_code = modelCodeArea.getText();
        Simulation simulation = new Simulation(model_code);
        molecules = simulation.getMolecules();
    }

    @FXML
    protected void onResetButtonClick() {
    }

}