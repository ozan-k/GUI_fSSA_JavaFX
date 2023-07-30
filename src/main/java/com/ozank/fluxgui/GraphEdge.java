package com.ozank.fluxgui;

public class GraphEdge implements Comparable<GraphEdge> {
    private int sourceId;
    private int targetId;
    private String sourceName;
    private String targetName;
    private int speciesId;
    private String speciesName;
    private int flux;


    public GraphEdge(int sourceId,
                     int targetId,
                     String sourceName,
                     String targetName,
                     int speciesId,
                     String speciesName,
                     int flux) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.speciesId = speciesId;
        this.speciesName = speciesName;
        this.flux = flux;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public int getFlux() {
        return flux;
    }


    @Override
    public String toString() {
        return "GraphEdge{" + sourceName +
                " (" + sourceId +
                ") --" +
                "[" + speciesName + " (" +
                speciesId + ") x " +
                flux + "]--> " +
                targetName + " (" +
                targetId +") }";
    }

    @Override
    public int compareTo(GraphEdge o) {
        int v1 = Math.abs(this.flux);
        int v2 = Math.abs(o.getFlux());
        if (v1>v2){
            return -1;
        } else if (v2> v1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        return sourceId * sourceName.hashCode() +
                targetId * targetName.hashCode() +
                speciesId * speciesName.hashCode() + flux;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GraphEdge))
            return false;
        GraphEdge other = (GraphEdge) obj;
        return  sourceId == other.getSourceId() &&
                targetId == other.getTargetId() &&
                sourceName.equals(other.getSourceName()) &&
                targetName.equals(other.getTargetName()) &&
                speciesId == other.getSpeciesId() &&
                speciesName.equals(other.getSpeciesName()) &&
                flux == other.getFlux();
    }
}
