package com.ozank.simulator;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationModel {
    private final int[] state;
    private final String[] reactionIds;
    private final double[] reactionRates;
    private final ReactionComponent[] left;
    private final ReactionComponent[] right;
    private final DependentReactions[] reactionDependencies;
    private final List<String> moleculesList;
    private final Map<String,Integer>  speciesMap;


    public SimulationModel(List<Reaction> reactions,
                           Map<String,Integer> initialState){
        int size = reactions.size()+1;

        reactionIds = new String[size];
        reactionRates = new double[size];
        left = new ReactionComponent[size];
        right = new ReactionComponent[size];

        moleculesList = computeMoleculesList(reactions,initialState);
        speciesMap = computeSpeciesMap(moleculesList);

        state = new int[speciesMap.size()];
        for (String p : initialState.keySet()){
            state[speciesMap.get(p)] = initialState.get(p);
        }

        int counter = 1;
        setInitAsReactionZero();
        for (Reaction r : reactions){
            reactionIds[counter] = r.getId();
            reactionRates[counter] = r.getRate();
            left[counter] = new ReactionComponent(r.getReactantsAsIntegerMap(speciesMap));
            right[counter] = new ReactionComponent(r.getProductsAsIntegerMap(speciesMap));
            counter++;
        }

        reactionDependencies = new DependentReactions[size];
        for (int i = 1; i < reactionRates.length; i++){
            Set<Integer> moleculesLeft = left[i].keySet();
            Set<Integer> moleculesRight = right[i].keySet();
            List<Set<Integer>> setList =
                    List.of(moleculesLeft, moleculesRight);
            Set<Integer> moleculesAll = setList.stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            reactionDependencies[i] = getReactionDependenciesOf(moleculesAll);
        }
    }


    private List<String> computeMoleculesList(List<Reaction> reactions,
                                              Map<String,Integer> initialState) {
        Set<String> molecules = new TreeSet<>();
        for (Reaction r : reactions) {
            molecules.addAll(getReactionComponentSpecies(r.getReactants()));
            molecules.addAll(getReactionComponentSpecies(r.getProducts()));
        }
        molecules.addAll(initialState.keySet());
        return molecules.stream().toList();
    }

    private  Map<String,Integer> computeSpeciesMap(List<String> molecules){
        Map<String,Integer> speciesMap = new HashMap<>();
        for (int i=0; i < molecules.size() ; i++){
            speciesMap.put(molecules.get(i),i);
        }
        return speciesMap;
    }

    private static Set<String> getReactionComponentSpecies(Map<String,Integer> moleculeMap){
        return moleculeMap.keySet();
    }


    private DependentReactions getReactionDependenciesOf(Set<Integer> molecules){
        DependentReactions result = new DependentReactions();
        for (int i =1; i<left.length; i++){
            if (left[i].keySet()
                    .stream()
                    .anyMatch(molecules::contains)
            ){
                result.add(i);
            }
        }
        return result;
    }

    // -----------------------------------------------

    private void setInitAsReactionZero(){
        reactionIds[0] = "init";
        reactionRates[0] = 0;
        left[0] = new ReactionComponent();
        Map<Integer,Integer> reactionZeroRight = new HashMap<>();
        for (int i = 0; i< state.length;i++){
            if (state[i] >0){
                int count = state[i];
                reactionZeroRight.put(i,count);
            }
        }
        right[0] = new ReactionComponent(reactionZeroRight);
    }

    // -----------------------------------------------

    public int[] getState() {
        return state;
    }
    public String[] getReactionIds(){ return reactionIds; }
    public double[] getReactionRates() {
        return reactionRates;
    }
    public ReactionComponent[] getLeft() {
        return left;
    }
    public ReactionComponent[] getRight() {
        return right;
    }
    public DependentReactions[] getReactionDependencies() {
        return reactionDependencies;
    }
    public List<String> getMoleculesList() {
        return moleculesList;
    }

    public Map<String, Integer> getSpeciesMap() {
        return speciesMap;
    }

    // -----------------------------------------------

    public void printState(){
        System.out.println("State");
        for (int i=0; i < state.length;i++){
            System.out.println(i + " : " + moleculesList.get(i) + " = " +state[i]);
        }
        System.out.println();
    }

    public void printReactions(){
        System.out.println("Model reactions");
        for (int i = 1; i<left.length;i++){
            System.out.println(i + " - " + reactionIds[i] + " : "
                    + left[i].toString(moleculesList)
                    + "--> "
                    + right[i].toString(moleculesList)
                    + ", " + reactionRates[i] );
        }
        System.out.println();
    }

    public void printDependencies(){
        System.out.println("Reaction dependencies");
        for (int i = 1; i<reactionDependencies.length;i++){
            System.out.println(i + " : " + reactionDependencies[i].toString());
        }
        System.out.println();
    }
}
