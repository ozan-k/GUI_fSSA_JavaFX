package com.ozank.viewElements;

import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.ozank.fluxgui.GEdge;
import com.ozank.fluxgui.GReaction;
import com.ozank.fluxgui.GraphEdge;
import javafx.scene.paint.Color;

import java.util.*;

public class FluxDigraphData {
    private Digraph<GReaction, GEdge> edges = new DigraphEdgeList<>();
    private Map<TripleIndex,GEdge> graphEdgesMap = new HashMap<>();
    private Set<GEdge> graphEdgeSet = new HashSet<>();
    private Map<Integer, Vertex<GReaction>> vertexMap = new HashMap<>();

    public FluxDigraphData(ArrayList<GraphEdge> filteredGraph){
        int sourceId;
        int targetId;
        Vertex vertex;
        for (GraphEdge edge : filteredGraph) {
            sourceId = edge.getSourceId();
            targetId = edge.getTargetId();
            if (!vertexMap.containsKey(sourceId)) {
                vertex = edges.insertVertex(new GReaction(sourceId, edge.getSourceName()));
                vertexMap.put(sourceId, vertex);
            }
            if (!vertexMap.containsKey(targetId)) {
                vertex = edges.insertVertex(new GReaction(targetId, edge.getTargetName()));
                vertexMap.put(targetId, vertex);
            }
            GEdge fluxEdge = new GEdge(sourceId, targetId, edge.getSpeciesName(), edge.getFlux(), edge.getSpeciesId());
            graphEdgesMap.put(new TripleIndex(sourceId,targetId,edge.getSpeciesId()),fluxEdge);
            graphEdgeSet.add(fluxEdge);
            Vertex<GReaction> sourceVertex = vertexMap.get(sourceId);
            Vertex<GReaction> targetVertex = vertexMap.get(targetId);
            edges.insertEdge(sourceVertex, targetVertex, fluxEdge);
        }
    }

    public Digraph<GReaction, GEdge> getEdges() {
        return edges;
    }

    public Map<Integer, Vertex<GReaction>> getVertexMap() {
        return vertexMap;
    }

    public Map<TripleIndex,GEdge> getGraphEdgesMap() {
        return graphEdgesMap;
    }

    public Set<GEdge> getGraphEdgeSet() {
        return graphEdgeSet;
    }

    public static String toRGBCode(Color color )
    {
        return String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );
    }

    public static Integer mapFluxesToWidths(Integer weight) {
        return ((int) Math.log10(weight)) + 1;
    }
}
