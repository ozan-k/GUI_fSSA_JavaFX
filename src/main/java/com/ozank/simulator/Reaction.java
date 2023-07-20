package com.ozank.simulator;

import java.util.HashMap;
import java.util.Map;


public class Reaction {
    private final double rate;
    private final Map<String,Integer> reactants;
    private final Map<String,Integer> products;
    private final String id;


    public Reaction(String id, double rate,
                    Map<String,Integer> reactants,
                    Map<String,Integer> products) {
        this.id = id;
        this.rate = rate;
        this.reactants = reactants;
        this.products = products;
    }

    public Reaction(double rate,
                    Map<String,Integer> reactants,
                    Map<String,Integer> products) {
        this("",rate,reactants,products);
    }

    public String getId() { return id; }

    public double getRate() {
        return rate;
    }

    public Map<String,Integer> getReactants() {
        return reactants;
    }

    public Map<String,Integer> getProducts() {
        return products;
    }

    public Map<Integer,Integer> getReactantsAsIntegerMap(Map<String,Integer> map) {
        Map<Integer,Integer> reactantsAsIntegerMap = new HashMap<>();
        for (String item : reactants.keySet()){
            reactantsAsIntegerMap.put(map.get(item), reactants.get(item));
        }
        return reactantsAsIntegerMap;
    }

    public Map<Integer,Integer> getProductsAsIntegerMap(Map<String,Integer> map) {
        Map<Integer,Integer> productsAsIntegerMap = new HashMap<>();
        for (String item : products.keySet()){
            productsAsIntegerMap.put(map.get(item), products.get(item));
        }
        return productsAsIntegerMap;
    }

    @Override
    public String toString(){
        return id + " : " + reactants + " --> "  + products + " , " + rate;
    }
}
