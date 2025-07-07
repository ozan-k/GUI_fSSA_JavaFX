package com.ozank.dataElements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Matrix<T> {
    private final Map<T,Long> matrix;

    public Matrix(){
        matrix = new HashMap<>();
    }

    private Matrix(Map<T,Long> matrix){
        this.matrix = matrix;
    }

    public Map<T, Long> getMatrix() {
        return matrix;
    }

    public long get(T p){
        return matrix.get(p);
    }

    public void set(T p, long value){

        matrix.put(p,value);
    }

    public boolean containsKey(T p){
        return matrix.containsKey(p);
    }
    public void increment(T p){
        long value = matrix.getOrDefault(p,0L);
        matrix.put(p, value+1);
    }
    public void put(T p, long addedValue){
        long value = matrix.getOrDefault(p,0L);
        matrix.put(p, value + addedValue);
    }

    public void remove(T p,long removedValue){
        long value = matrix.getOrDefault(p,0L);
        matrix.put(p, value - removedValue);
    }

    public void decrement(T p){
        if (!matrix.containsKey(p)){
            throw new IllegalArgumentException();
        }
        long value = matrix.get(p);
        if (value==0) {
            throw new IllegalArgumentException();
        }
        matrix.put(p,value-1);
    }

    public void matrixAddition(Matrix<T> otherMatrix){
        for (T index : otherMatrix.keySet()){
            put(index,otherMatrix.get(index));
        }
    }

    public void matrixSubtraction(Matrix<T> otherMatrix){
        for (T index : otherMatrix.keySet()){
            remove(index,otherMatrix.get(index));
            if (matrix.get(index)==0) { matrix.remove(index); }
        }
    }

    public void divideByScalar(long denominator){
        for (T index : matrix.keySet()){
            matrix.put(index,matrix.get(index)/denominator);
        }
    }

    public Matrix<T> copy(){
        Map<T,Long> result = new HashMap<>();
        for (T key : matrix.keySet()){
            result.put(key,matrix.get(key));
        }
        return new Matrix(result);
    }

    public void clear(){
        matrix.clear();
    }

    public Set<T> keySet(){
        return matrix.keySet();
    }

    public long maxValue(){
        return Collections.max(matrix.values());
    }

    public void printMatrix(){
        for (T key : matrix.keySet()){
            System.out.println(key + " : " + matrix.get(key));
        }
    }
}
