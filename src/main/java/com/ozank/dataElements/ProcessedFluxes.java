package com.ozank.dataElements;

import com.ozank.viewElements.TripleIndex;

import java.util.*;

public class ProcessedFluxes {
    private final ArrayList<IntervalFluxSet> intervalFluxes = new ArrayList<>();
    private final Matrix<TripleIndex> matrixF = new Matrix<TripleIndex>();
    private final HashMap<Integer,String> reactions = new HashMap<>();
    public ProcessedFluxes(
            HashMap<String, ArrayList<FluxItem>> fluxes,
            HashMap<String, String> reactions,
            Map<String,Integer> speciesMap) {
            for (String s : fluxes.keySet()){
                if (s.equals("complete")){
                    for (FluxItem f : fluxes.get(s)){
                        TripleIndex t = new TripleIndex(f.getSource(),f.getTarget(),speciesMap.get(f.getMolecule()));
                        matrixF.put(t,f.getWeight());
                    }
                } else {
                    long max = 0L;
                    Matrix<TripleIndex> thisIntervalMatrixF = new Matrix<>();
                    double timeStamp = Double.parseDouble(s);
                    for (FluxItem f : fluxes.get(s)){
                        TripleIndex t = new TripleIndex(f.getSource(),f.getTarget(),speciesMap.get(f.getMolecule()));
                        thisIntervalMatrixF.put(t,f.getWeight());
                        if (f.getWeight() > max){
                            max = f.getWeight();
                        }
                    }
                    IntervalFluxSet ifs = new IntervalFluxSet(thisIntervalMatrixF, timeStamp, max);
                    intervalFluxes.add(ifs);
                }
            }
            Collections.sort(intervalFluxes, Comparator.comparing(IntervalFluxSet::getTimeStamp));
            for (String s : reactions.keySet()){
                this.reactions.put(Integer.parseInt(s),reactions.get(s));
            }
    }

    public ArrayList<IntervalFluxSet> getIntervalFluxes() {
        return intervalFluxes;
    }

    public Matrix<TripleIndex> getMatrixF() {
        return matrixF;
    }

    public HashMap<Integer, String> getReactions() {
        return reactions;
    }

    public void printIntervalFluxes(){
        for (IntervalFluxSet i : intervalFluxes){
            i.printIntervalFluxSet();
            System.out.println();
        }
    }
}
