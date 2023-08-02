package com.ozank.simulator;

public class IntervalFluxSet {
    private Matrix<TripleIndex> matrix;
    private double timeStamp;
    private int max;

    public IntervalFluxSet(Matrix<TripleIndex> matrix, double timeStamp) {
        this.matrix = matrix;
        this.timeStamp = timeStamp;
        this.max = getMaxFluxValue();
    }

    public Matrix<TripleIndex> getMatrix() {
        return matrix;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public int getMax(){ return max; }

    private int getMaxFluxValue(){
        int result = 0;
        for (int i : matrix.getMatrix().values()){
            if (i > result){
                result = i;
            }
        }
        return result;
    }

}
