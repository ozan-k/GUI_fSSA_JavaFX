package com.ozank.viewElements;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FluxSpecies implements Comparable<FluxSpecies>{

    private static HashSet<String> fluxGraphSpecies = new HashSet<>();
    private static HashMap<String,Color> colorMap = new HashMap<>();
    private static HashMap<String,Integer> cutOffMap = new HashMap<>();
    private String name;
    private ComboBox<String> choiceComboBox;
    private ColorPicker colorPicker = new ColorPicker();
    private Spinner<Integer> cutOffSpinner = new Spinner(0, Integer.MAX_VALUE,0);

    public FluxSpecies(String name, String color) {
        this.name = name;
        this.choiceComboBox = new ComboBox<>(FXCollections.observableArrayList("Yes", "No"));
        this.choiceComboBox.setValue("Yes");
        this.colorPicker.setValue(Color.web(color));
        this.colorPicker.prefWidth(60);
        //this.cutOffSpinner.setEditable(true);

        colorMap.put(name, Color.web(color));
        cutOffMap.put(name,0);
        fluxGraphSpecies.add(name);

        choiceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = choiceComboBox.getValue();
                if (value.equals("Yes")) {
                    fluxGraphSpecies.add(name);
                    colorPicker.setVisible(true);
                } else if (value.equals("No")) {
                    fluxGraphSpecies.remove(name);
                    colorPicker.setVisible(false);
                }
            }
        });

        colorPicker.valueProperty().addListener(
                (obs, oldValue, newValue) -> colorMap.put(name, newValue));

        cutOffSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                    cutOffMap.put(name, newValue);
                });

    }

    public String getName() {
        return name;
    }

    public ComboBox<String> getChoiceComboBox() {
        return choiceComboBox;
    }

    public ColorPicker getColorPicker(){
        return colorPicker;
    }

    public Spinner<Integer> getCutOffSpinner(){
        return cutOffSpinner;
    }

    public static void clearColorMap(){
        colorMap.clear();
    }

    public static void clearCutOffMap(){
        cutOffMap.clear();
    }

    public static HashMap<String,Color> getColorMap(){
        return colorMap;
    }

    public static HashMap<String,Integer> getCutOffMap(){
        return cutOffMap;
    }

    public static HashSet<String> getFluxGraphSpecies() {
        return fluxGraphSpecies;
    }

    public static void resetFluxGraphSpecies(){
        fluxGraphSpecies = new HashSet<>();
    }

    public static void resetColorMap(){
        colorMap = new HashMap<>();
    }

    public static void resetCutOffMap(){
        cutOffMap =  new HashMap<>();
    }

    @Override
    public int compareTo(FluxSpecies o) {
        return CharSequence.compare(getName(), o.getName());
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
