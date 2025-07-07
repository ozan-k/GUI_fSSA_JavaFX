package com.ozank.dataElements;

import com.ozank.viewElements.TrajectoryState;

import java.util.*;

public class TimeSeries {
    private List<TrajectoryState> timeSeries = new ArrayList<>();
    private Map<String,Integer> speciesMap = new HashMap<>();

    public TimeSeries(ArrayList<String> timeSeriesList) {
        List<String> species = List.of(timeSeriesList.get(0).split(","));
        for (int i=1; i < species.size() ; i++){
                speciesMap.put(species.get(i),i);
        }
        timeSeriesList.remove(0);
        for (String snapshot : timeSeriesList){
            String[] items = snapshot.split(",");
            double time = Double.parseDouble(items[0]);
            int[] state = Arrays.stream(items,1,items.length).mapToInt(Integer::parseInt).toArray();
            timeSeries.add(new TrajectoryState(time,state));
        }
    }

    public List<TrajectoryState> getTimeSeries() {
        return timeSeries;
    }

    public Map<String, Integer> getSpeciesMap() {
        return speciesMap;
    }

    public double getEndTime(){
        return timeSeries.get(timeSeries.size() - 1).getTime();
    }

    public void printTimeSeries(){
        for (TrajectoryState s : timeSeries ) {
            System.out.print(s.getTime());
            System.out.print(" ");
            for (int element : s.getState()) {
                System.out.print(element);
                System.out.print(" ");
            }
            System.out.println();
        }
     }

    public void printSpeciesMap(){
        for (String s : speciesMap.keySet()){
            System.out.println(s + " " + speciesMap.get(s));
        }
    }


}
