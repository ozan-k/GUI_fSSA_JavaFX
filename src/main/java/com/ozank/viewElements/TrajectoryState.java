package com.ozank.viewElements;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrajectoryState {
    private double time;
    private int[] state;

    public TrajectoryState(double time, int[] state) {
        this.time = time;
        this.state = state;
    }

    public double getTime() {
        return time;
    }

    public int[] getState() {
        return state;
    }

    public int getFilteredStateMax(Set<Integer> plotSpeciesIndexes){
        int max = 0;
        for (Integer s : plotSpeciesIndexes) {
            max = Math.max(max,state[s-1]);
        }
        return max;
    }

    @Override
    public String toString(){
        String result = IntStream.of(state)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
        return time + ", " + result + "\n";
    }

}
