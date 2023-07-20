package com.ozank.simulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReactionComponent {
    private Map<Integer,Integer> component = new HashMap<>();

    public ReactionComponent(){}
    public ReactionComponent(Map<Integer,Integer> component){
        this.component = component;
    }

    public Map<Integer, Integer> getComponent() {
        return component;
    }

    public Integer get(Integer key){
        return component.get(key);
    }

    public Set<Integer> keySet(){
        return component.keySet();
    }

    public String toString(List<String> moleculesList){
        String mapAsString = component.keySet().stream()
                .map(key -> "[" + moleculesList.get(key)  + "," + component.get(key) + "]")
                .collect(Collectors.joining(" + ", " ", " "));
        return mapAsString;
    }

}
