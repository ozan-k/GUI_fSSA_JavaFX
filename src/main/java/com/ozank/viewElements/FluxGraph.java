package com.ozank.viewElements;

import com.ozank.fluxgui.GReaction;
import com.ozank.fluxgui.GraphEdge;
import com.ozank.simulator.Matrix;
import com.ozank.simulator.SimulationModel;
import com.ozank.simulator.SimulationSSA;
import com.ozank.simulator.TripleIndex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FluxGraph {
    private HashMap<TripleIndex, GraphEdge> graph = new HashMap<>();
    private Set<GReaction> allReactions = new HashSet<>();
    public FluxGraph(Matrix<TripleIndex>fluxMatrix, SimulationModel model){
        for (TripleIndex t : fluxMatrix.keySet()) {
            int sourceId = t.getA();
            int targetId = t.getB();
            int moleculeId = t.getC();
            String moleculeName = model.getMoleculesList().get(moleculeId);
            String sourceName = model.getReactionNames()[sourceId];
            String targetName = model.getReactionNames()[targetId];
            int flux = fluxMatrix.get(t);
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
