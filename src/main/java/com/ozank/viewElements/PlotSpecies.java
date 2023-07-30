package com.ozank.viewElements;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlotSpecies implements Comparable<PlotSpecies>{

    private static Set<String> plotSpecies = new HashSet<>();
    private static Map<String,Color> colorMap = new HashMap<>();
    private String name;
    //private String choice;
    private ComboBox<String> choiceComboBox;
    private ColorPicker colorPicker = new ColorPicker();

    public PlotSpecies(String name, String color){
        this.name = name;
        this.choiceComboBox = new ComboBox<>(FXCollections.observableArrayList("Yes","No"));
        this.choiceComboBox.setValue("Yes");
        this.colorPicker.setValue(Color.web(color));

        colorMap.put(name,Color.web(color));
        plotSpecies.add(name);

        choiceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = choiceComboBox.getValue();
                if (value.equals("Yes")){
                    plotSpecies.add(name);
                    colorPicker.setVisible(true);
                } else if (value.equals("No")){
                    plotSpecies.remove(name);
                    colorPicker.setVisible(false);
                }
            }
        });

        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
                colorMap.put(name,t1);
            }
        });
    }

    public String getName() {
        return name;
    }

    public ComboBox<String> getChoiceComboBox() {
        return choiceComboBox;
    }

    public static Set<String> getPlotSpecies() {
        return plotSpecies;
    }

    public ColorPicker getColorPicker(){
        return colorPicker;
    }

    public static void clearColorMap(){
        colorMap.clear();
    }

    public static Map<String,Color> getColorMap(){
        return colorMap;
    }

    @Override
    public int compareTo(PlotSpecies o) {
        return CharSequence.compare(getName(), o.getName());
    }

}
