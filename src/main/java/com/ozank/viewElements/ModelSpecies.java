package com.ozank.viewElements;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelSpecies implements Comparable<ModelSpecies>{

    private static Set<String> plotModelSpecies = new HashSet<>();
    private static Map<String,Color> colorMap = new HashMap<>();
    private String name;
    //private String choice;
    private ComboBox<String> choiceComboBox;
    private ColorPicker colorPicker = new ColorPicker();

    public ModelSpecies(String name,String color){
        this.name = name;
        this.choiceComboBox = new ComboBox<>(FXCollections.observableArrayList("Yes","No"));
        this.choiceComboBox.setValue("Yes");
        this.colorPicker.setValue(Color.web(color));

        colorMap.put(name,Color.web(color));
        plotModelSpecies.add(name);

        choiceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = choiceComboBox.getValue();
                if (value.equals("Yes")){
                    plotModelSpecies.add(name);
                    colorPicker.setVisible(true);
                } else if (value.equals("No")){
                    plotModelSpecies.remove(name);
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
            //colorMap.put(name,newValue);


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
    public int compareTo(ModelSpecies o) {
        return CharSequence.compare(getName(), o.getName());
    }

    static boolean isValidHexaCode(String str)
    {
        if (str.charAt(0) != '#')
            return false;

        if (!(str.length() == 4 || str.length() == 7))
            return false;

        for (int i = 1; i < str.length(); i++)
            if (!((str.charAt(i) >= '0' && str.charAt(i) <= 9)
                    || (str.charAt(i) >= 'a' && str.charAt(i) <= 'f')
                    || (str.charAt(i) >= 'A' || str.charAt(i) <= 'F')))
                return false;

        return true;
    }

}
