package com.ozank.dataElements;

public class FluxItem {
    Integer source;
    Integer target;
    Long weight;
    String molecule;

    public Integer getSource() {
        return source;
    }

    public Integer getTarget() {
        return target;
    }

    public Long getWeight() {
        return weight;
    }

    public String getMolecule() {
        return molecule;
    }

    @Override
    public String toString() {
        return "FluxItem{" +
                "source=" + source +
                ", target=" + target +
                ", weight=" + weight +
                ", molecule='" + molecule + '\'' +
                '}';
    }
}
