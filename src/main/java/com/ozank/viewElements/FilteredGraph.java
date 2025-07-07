package com.ozank.viewElements;

import com.ozank.fluxgui.GraphEdge;

import java.util.*;

public class FilteredGraph {
    private ArrayList<GraphEdge> filteredGraph = new ArrayList<>();

    public FilteredGraph(HashMap<TripleIndex,GraphEdge> graph,
                         HashSet<String> includedMolecules,
                         HashSet<Integer> includedReactions,
                         Map<String, Integer> cutOffMap,
                         int cutOffValue) {
        for (GraphEdge e : graph.values()) {
            if (includedMolecules.contains(e.getSpeciesName()) &&
                    includedReactions.contains(e.getSourceId()) &&
                    includedReactions.contains(e.getTargetId()) &&
                    e.getFlux() >= cutOffMap.get(e.getSpeciesName()) &&
                    e.getFlux() >= cutOffValue
            ) {
                filteredGraph.add(e);
            }
        }
    }

    public ArrayList<GraphEdge> getFilteredGraph() {
        return filteredGraph;
    }
}
