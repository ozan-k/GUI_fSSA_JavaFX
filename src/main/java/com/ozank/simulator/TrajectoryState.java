package com.ozank.simulator;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrajectoryState {
    private double time;
    private int[] state;

    public TrajectoryState(double time, int[] state) {
        this.time = time;
        this.state = state;
    }

    @Override
    public String toString(){
        String result = IntStream.of(state)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
        return time + ", " + result + "\n";
    }

}
