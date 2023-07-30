package com.ozank.viewElements;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.HashSet;

public class FluxReaction  implements Comparable<FluxReaction>{
    private static HashSet<Integer> fluxReactions = new HashSet<>();
    private static HashMap<Integer,Color> colorMap = new HashMap<>();
    private int id;
    private String name;
    private ComboBox<String> choiceComboBox;
    private ColorPicker colorPicker = new ColorPicker();

    public FluxReaction(int id, String name, String color) {
        this.id = id;
        this.name = name;
        this.choiceComboBox = new ComboBox<>(FXCollections.observableArrayList("Yes", "No"));
        this.choiceComboBox.setValue("Yes");
        this.colorPicker.setValue(Color.web(color));

        colorMap.put(id, Color.web(color));
        fluxReactions.add(id);

        choiceComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String value = choiceComboBox.getValue();
                if (value.equals("Yes")){
                    fluxReactions.add(id);
                    colorPicker.setVisible(true);
                } else if (value.equals("No")){
                    fluxReactions.remove(id);
                    colorPicker.setVisible(false);
                }
            }
        });

        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
                colorMap.put(id,t1);
            }
        });

    }

    public String getName() {
        return name;
    }

    public int getId(){ return id; }

    public ComboBox<String> getChoiceComboBox() {
        return choiceComboBox;
    }

    public ColorPicker getColorPicker(){
        return colorPicker;
    }

    public static void clearColorMap(){
        colorMap.clear();
    }

    public static HashMap<Integer,Color> getColorMap(){
        return colorMap;
    }

    public static HashSet<Integer> getFluxReactions() { return fluxReactions; }
    @Override
    public int compareTo(FluxReaction o) {
        return CharSequence.compare(getName(), o.getName());
    }
}
