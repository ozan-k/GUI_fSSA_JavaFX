package com.ozank.dataElements;

import com.ozank.viewElements.TripleIndex;

public class IntervalFluxSet {
    private Matrix<TripleIndex> matrix;
    private double timeStamp;
    private long max;

    public IntervalFluxSet(Matrix<TripleIndex> matrix, double timeStamp) {
        this.matrix = matrix;
        this.timeStamp = timeStamp;
        this.max = getMaxFluxValue();
    }

    public IntervalFluxSet(Matrix<TripleIndex> matrix, double timeStamp, long max) {
        this.matrix = matrix;
        this.timeStamp = timeStamp;
        this.max = max;
    }

    public Matrix<TripleIndex> getMatrix() {
        return matrix;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public long getMax(){ return max; }

    private long getMaxFluxValue(){
        long result = 0;
        for (long i : matrix.getMatrix().values()){
            if (i > result){
                result = i;
            }
        }
        return result;
    }

    public void printIntervalFluxSet(){
        System.out.println("Time: " + timeStamp);
        System.out.println("Max: " + max);
        System.out.println("Fluxes:");
        for (TripleIndex t : matrix.keySet()){
            System.out.println(t + " ~> " + matrix.get(t));
        }
    }
}
