package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;

public class GEdge implements Comparable<GEdge>{
    private int weight;
    private int id;
    private String speciesName;
    private int source;
    private int target;

    public GEdge(int source, int target, String speciesName, int weight, int id) {
        this.source = source;
        this.target = target;
        this.speciesName = speciesName;
        this.weight = weight;
        this.id = id;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    public int getId() { return id; }

    public String getSpeciesName(){
        return speciesName;
    }

    @SmartLabelSource
    public String getDisplayDistance() {
        /* If the above annotation is not present, the toString()
        will be used as the edge label. */
        return weight + " x " + speciesName;
    }

    @Override
    public String toString() {
        return "Weight{" + "weight=" + weight +
                ",id = " + id + ",molecule = " +
                speciesName + '}';
    }

    @Override
    public int compareTo(GEdge o) {
        return CharSequence.compare(speciesName, o.getSpeciesName());
    }
}
