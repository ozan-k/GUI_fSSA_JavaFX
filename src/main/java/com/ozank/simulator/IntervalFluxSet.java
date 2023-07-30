package com.ozank.simulator;

public class IntervalFluxSet {
    private Matrix<TripleIndex> matrix;
    private double timeStamp;

    public IntervalFluxSet(Matrix<TripleIndex> matrix, double timeStamp) {
        this.matrix = matrix;
        this.timeStamp = timeStamp;
    }

    public Matrix<TripleIndex> getMatrix() {
        return matrix;
    }

    public double getTimeStamp() {
        return timeStamp;
    }
}
