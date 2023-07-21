package com.ozank.simulation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import java.util.HashSet;
import java.util.Set;

public class ModelSpecies implements Comparable<ModelSpecies>{

    private static Set<String> plotModelSpecies = new HashSet<>();
    private String name;
    //private String choice;
    private ComboBox<String> choiceComboBox;

    public ModelSpecies(String name){
        this.name = name;
    //    this.choice = "Yes";
        this.choiceComboBox = new ComboBox<>(FXCollections.observableArrayList("Yes","No"));
        this.choiceComboBox.setValue("Yes");
        choiceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = choiceComboBox.getValue();
                if (value.equals("Yes")){
                    plotModelSpecies.add(name);
                } else if (value.equals("No")){
                    plotModelSpecies.remove(name);
                }
            }
        });
    }

    public String getName() {
        return name;
    }

    public ComboBox<String> getChoiceComboBox() {
        return choiceComboBox;
    }

    public static Set<String> getPlotModelSpecies() {
        return plotModelSpecies;
    }

    @Override
    public int compareTo(ModelSpecies o) {
        return CharSequence.compare(getName(), o.getName());
    }


}
