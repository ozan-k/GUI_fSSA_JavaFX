package com.ozank.viewElements;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class LineChartFactory {

    private MyLineChart lineChart;

    public LineChartFactory(Map<String, Integer> speciesMap,
                            Set<String> plotSpecies,
                            Map<String, Color> colorMap,
                            String plotTimeUnit,
                            List<TrajectoryState> trajectory,
                            double startTime,
                            double endTime){
        trajectory = filterTrajectory(trajectory,startTime,endTime);
        Set<Integer> plotSpeciesIndexes = new HashSet<>();
        for (String s : plotSpecies){ plotSpeciesIndexes.add(speciesMap.get(s)); }
        int max = getMax(trajectory, plotSpeciesIndexes);

        //Defining the x axis
        NumberAxis xAxis = new NumberAxis(startTime,endTime, endTime/6);

        xAxis.setLabel("Time" + plotTimeUnit);

        //Defining the y axis

        NumberAxis yAxis = new NumberAxis   (0, max+(max/10), max/6);
        yAxis.setLabel("Counts");

        //Creating the line chart
        lineChart = new MyLineChart(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        int interval = (trajectory.size() / 2000)+1;
        int count = interval;

        for (String modelSpecies : plotSpecies) {
            //Prepare XYChart.Series objects by setting data
            XYChart.Series series = new XYChart.Series();
            //Setting the name to the line (series)
            series.setName(modelSpecies);
            //series.setName("No of schools in an year");
            for (TrajectoryState state : trajectory) {
                if (count == interval) {
                    try {
                        series.getData().add(
                                new XYChart.Data(
                                        state.getTime(),
                                        state.getState()[speciesMap.get(modelSpecies) - 1]));
                    } catch(Exception e){
                        System.out.println("#" + modelSpecies + " " + speciesMap.get(modelSpecies));
                    }
                    count = 0;
                }
                count++;
            }
            //Setting the data to Line chart
            lineChart.getData().add(series);
        }


        // https://stackoverflow.com/questions/48663754/javafx-linechart-dynamically-change-legend-color
        int lineCount = 0;
        StringBuilder builder = new StringBuilder("");
        for (String modelSpecies : plotSpecies){
            lineCount++;
            builder.append(String.format("CHART_COLOR_" + lineCount + ": %s ;\n",format(colorMap.get(modelSpecies))));
        }
        lineChart.setStyle(builder.toString());
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    }

    private int getMax(List<TrajectoryState> trajectory, Set<Integer> plotSpeciesIndexes){
        int result = 0;
        for (TrajectoryState state : trajectory){
            // result = Math.max(result, Arrays.stream(state.getState()).max().getAsInt());
            result = Math.max(result, state.getFilteredStateMax(plotSpeciesIndexes));
        }
        return result;
    }

    private String format(Color c) {
        int r = (int) (255 * c.getRed()) ;
        int g = (int) (255 * c.getGreen()) ;
        int b = (int) (255 * c.getBlue()) ;

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private List<TrajectoryState> filterTrajectory(List<TrajectoryState> trajectory,
                                                   double startTime,
                                                   double endTime){
        return trajectory.stream().filter(state -> (state.getTime()> startTime && state.getTime() < endTime)).collect(Collectors.toList());
    }

    public MyLineChart getLineChart() {
        return lineChart;
    }
}
