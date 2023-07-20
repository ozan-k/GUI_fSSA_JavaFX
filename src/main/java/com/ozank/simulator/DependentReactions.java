package com.ozank.simulator;

import java.util.HashSet;
import java.util.Set;

public class DependentReactions {
    private final Set<Integer> dependentReactions;

    public DependentReactions() {
        this.dependentReactions = new HashSet<>();
    }

    public Set<Integer> getDependentReactions() {
        return dependentReactions;
    }

    public void add(Integer reactionIndex){
        dependentReactions.add(reactionIndex);
    }

    @Override
    public String toString() {
        return dependentReactions.toString();
    }
}
