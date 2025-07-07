package com.ozank.viewElements;

import com.ozank.dataElements.Matrix;
import com.ozank.fluxgui.GReaction;
import com.ozank.fluxgui.GraphEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FluxGraph {
    private HashMap<TripleIndex, GraphEdge> graph = new HashMap<>();
    private Set<GReaction> allReactions = new HashSet<>();
    private Map<Integer,String> moleculesList = new HashMap<>();

    public FluxGraph(Matrix<TripleIndex> fluxMatrix, Map<String,Integer> speciesMap){
        for (String s : speciesMap.keySet()){ moleculesList.put(speciesMap.get(s),s); }
        for (TripleIndex t : fluxMatrix.keySet()) {
            int sourceId = t.getA();
            int targetId = t.getB();
            int moleculeId = t.getC();
            String moleculeName = moleculesList.get(moleculeId);
            String sourceName = String.valueOf(sourceId);
            String targetName = String.valueOf(targetId);
            long flux = fluxMatrix.get(t);
            allReactions.add(new GReaction(sourceId,sourceName));
            allReactions.add(new GReaction(targetId,targetName));
            graph.put(t, new GraphEdge(sourceId, targetId, sourceName, targetName, moleculeId, moleculeName, flux));
        }
    }

    public HashMap<TripleIndex, GraphEdge> getGraph() {
        return graph;
    }

    public Set<GReaction> getAllReactions() {
        return allReactions;
    }
}
