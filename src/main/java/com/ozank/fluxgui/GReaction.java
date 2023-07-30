package com.ozank.fluxgui;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;

public class GReaction implements Comparable<GReaction>{
    private int id;
    private String name;

    public GReaction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @SmartLabelSource
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Reaction{" + "id=" + id + ", name=" + name + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GReaction))
            return false;
        GReaction other = (GReaction) obj;
        return name.equals(other.getName()) && id == other.getId();
    }

    @Override
    public int hashCode() {
        return id * name.hashCode();
    }

    @Override
    public int compareTo(GReaction o) {
        return Integer.compare(id,o.getId());
    }
}
