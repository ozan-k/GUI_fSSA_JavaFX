package com.ozank.viewElements;

public class Flux {
    private String sourceName;
    private int sourceId;
    private String targetName;
    private int targetId;
    private String fluxMoleculeName;
    private int fluxMoleculeId;
    private int fluxWeight;

    public Flux(int sourceId,
                String sourceName,
                int targetId,
                String targetName,
                int fluxMoleculeId,
                String fluxMoleculeName,
                int fluxWeight) {
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.targetId = targetId;
        this.fluxMoleculeName = fluxMoleculeName;
        this.fluxMoleculeId = fluxMoleculeId;
        this.fluxWeight = fluxWeight;
    }

    public String getSourceName() {
        return sourceName;
    }

    public int getSourceId() {
        return sourceId;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getTargetId() {
        return targetId;
    }

    public String getFluxMoleculeName() {
        return fluxMoleculeName;
    }

    public int getFluxMoleculeId() {
        return fluxMoleculeId;
    }

    public int getFluxWeight() {
        return fluxWeight;
    }
}
