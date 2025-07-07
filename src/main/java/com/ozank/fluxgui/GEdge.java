package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;

import java.util.Objects;

public class GEdge implements Comparable<GEdge>{
    private long weight;
    private int id;
    private String speciesName;
    private int source;
    private int target;

    public GEdge(int source, int target, String speciesName, long weight, int id) {
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

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public int getId() { return id; }

    public String getSpeciesName(){
        return speciesName;
    }

    @SmartLabelSource
    public String getDisplayDistance() {
        /* If the above annotation is not present, the toString()
        will be used as the edge label. */
        if (weight == 0) {
            return "";
        } else {
            return weight + " x " + speciesName;
        }
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
