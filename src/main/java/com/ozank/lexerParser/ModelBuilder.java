package com.ozank.lexerParser;

import com.ozank.simulator.Reaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelBuilder {
    private final List<Reaction> reactions = new ArrayList<>();
    private final Map<String,Integer> initialState = new HashMap<>();
    private double endTime;
    private double scalingFactor = 1;
    private int every = 1;
    private double interval = 0;

    public ModelBuilder(String file) {
        file = file.replaceAll("//.*?(\\n|\\z)","");
        file  = file.replaceAll("\\s*","");
        String regex = "reactions(.*)initialstate(.*)endtime(.+?);(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(file);
        if (m.find()) {
            setOptionalParameters(m.group(4));
            parseReactions(m.group(1));
            parseInitialState(m.group(2));
            try {
                endTime = Double.parseDouble(m.group(3));
            } catch (IndexOutOfBoundsException e){
                System.out.println(e);
            }
        }
    }

    private void setOptionalParameters(String optionalParameters){
        String regex = ".*every(.+?);.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(optionalParameters);
        if (m.find()) {
            every = Integer.parseInt(m.group(1));
        }
        // -------------------------------------
        regex = ".*scalingfactor(.+?);.*";
        pattern = Pattern.compile(regex);
        m = pattern.matcher(optionalParameters);
        if (m.find()){
            scalingFactor = Double.parseDouble(m.group(1));
        }
        // -------------------------------------
        regex = ".*interval(.+?);.*";
        pattern = Pattern.compile(regex);
        m = pattern.matcher(optionalParameters);
        if (m.find()){
            interval = Double.parseDouble(m.group(1));
        }
    }
    private void parseReactions(String reactionsString){
        String regex = "(.*):(.*)->(.*):(.*)";
        Pattern pattern = Pattern.compile(regex);
        for (String reaction: reactionsString.split(";")){
            Matcher m = pattern.matcher(reaction);
            if (m.find()) {
                String id = m.group(1);
                Map<String,Integer> left = organizeReactants(m.group(2));
                double length_factor = (double) left.values().stream().reduce(0, Integer::sum) -1;
                Map<String,Integer> right = organizeReactants(m.group(3));
                // https://en.wikipedia.org/wiki/Rate_equation
                double rate = Double.parseDouble(m.group(4)) / Math.pow(scalingFactor,length_factor);
                reactions.add(new Reaction(id,rate,left,right));
            }
        }
    }

    private Map<String,Integer> organizeReactants(String reactants){
        Map<String,Integer> result = new HashMap<>();
        if (reactants.equals("")){
            return result;
        }
        for (String s : reactants.split("\\+")){
            result.put(s,result.getOrDefault(s,0)+1);
        }
        return result;
    }

    private void parseInitialState(String initialStateString){
        String regex = "(.*):(.*)";
        Pattern pattern = Pattern.compile(regex);
        for (String string : initialStateString.split(";")){
            Matcher m = pattern.matcher(string);
            if (m.find()) {
                String molecule = m.group(1);
                Integer count;
                try {
                    count = (int) (Double.parseDouble(m.group(2)) * scalingFactor);
                } catch (Exception e){
                    throw new IllegalArgumentException();
                }
                if (initialState.containsKey(molecule)){
                    throw new IllegalArgumentException();
                } else {
                    initialState.put(molecule,count);
                }
            }
        }
    }

    public List<Reaction> getReactions(){
        return reactions;
    }

    public Map<String,Integer> getInitialState(){
        return initialState;
    }

    public double getEndTime(){
        return endTime;
    }

    public int getEvery(){
        return every;
    }

    public double getInterval(){
        return interval;
    }

}


//    let rec scale_reactions reac_list scaling_factor = match reac_list with [] -> [] |
//        (name,left,right,rate)::tail -> let length_factor = float_of_int ((List.length left) - 1)
//        in (name,left,right,rate /. ( scaling_factor ** length_factor ) )::(scale_reactions tail scaling_factor)


//for (String s : initialState.keySet()){
//        System.out.println(s + " : " + initialState.get(s));
//        }
//        for (Reaction r : reactions){
//            System.out.println(r);
//        }
//
//        System.out.println("");
//        System.out.println(endTime);