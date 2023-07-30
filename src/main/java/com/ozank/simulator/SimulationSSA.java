package com.ozank.simulator;

import com.ozank.fluxgui.Controller;
import com.ozank.fluxgui.GraphEdge;
import com.ozank.fluxgui.ProgressForm;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimulationSSA {
    Random r = new Random();
    private final SimulationModel model;
    private final Matrix<PairIndex> matrixM;
    private final Matrix<TripleIndex> matrixF;
    private Matrix<TripleIndex> matrixF_interval;
    private final BigDecimal[] aj;
    private final int[] state;
    private final int[] state_y;
    private double time;
    private int stepCount;

    private final ReactionComponent[] reactionsLeft;
    private final ReactionComponent[] reactionsRight;
    private final double[] reactionRates;
    private final DependentReactions[] reactionDependencies;
    private final List<TrajectoryState> trajectory;
    private final int every;
    private final double interval;
    private double nextIntervalEnd;
    private final boolean trackIntervals;
    private final ArrayList<IntervalFluxSet> intervalFluxes;
    private boolean updateTrajectoryFlag;

    public SimulationSSA(SimulationModel model,int every,double interval){
        this.every = every;
        this.interval = interval;
        this.model = model;
        this.updateTrajectoryFlag = every != 0;
        this.trackIntervals = interval != 0;

        int[] initialState = model.getState();
        state = Arrays.copyOf(initialState,initialState.length);
        state_y = Arrays.copyOf(initialState,initialState.length);

        reactionsLeft = model.getLeft();
        reactionsRight = model.getRight();
        reactionRates = model.getReactionRates();
        reactionDependencies = model.getReactionDependencies();

        aj = new BigDecimal[reactionsLeft.length];
        aj[0] = new BigDecimal(0);
        for (int i = 1; i<reactionsLeft.length;i++) {
            aj[i] = computePropensity(i);
            aj[0] =  aj[0].add(aj[i]);
        }

        stepCount = 0;
        time = 0;
        nextIntervalEnd = interval;

        intervalFluxes = new ArrayList<>();
        trajectory = new ArrayList<>();
        updateTrajectory();

        matrixM = new Matrix<>();
        matrixF = new Matrix<>();
        matrixF_interval = new Matrix<>();
        for (int i=0;i<state.length;i++){
            matrixM.set(new PairIndex(i,0),state[i]);
        }
    }

    public SimulationSSA(SimulationModel model){
        this(model,1,0);
    }

    public static int combinatorial(int setSize,int subsetSize){
        if (setSize==0){ return 0;}
        int min = Math.min(subsetSize,setSize-subsetSize);
        int numerator = 1;
        int denominator = 1;
        for (int i=0;i<min;i++){
            numerator*=(setSize-i);
            denominator*=i+1;
        }
        return numerator/denominator;
    }

    private BigDecimal computePropensity(int i){
        int moleculeCount;
        double result = reactionRates[i];
        ReactionComponent left = reactionsLeft[i];
        for (Integer moleculeIndex : left.keySet()){
            moleculeCount = state[moleculeIndex];
            if (moleculeCount==0) { return new BigDecimal(0); }
            result *= combinatorial(moleculeCount,left.get(moleculeIndex));
        }
        return new BigDecimal(result);
    }

    private int computeNextReaction(){
        double random = r.nextDouble() * aj[0].doubleValue();
        double sum =0;
        for (int i=1;i< aj.length;i++){
            sum+=aj[i].doubleValue();
            if (sum > random){
                return i;
            }
        }
        return 0;
    }

    private double computeTau(){
        return ( 1 / aj[0].doubleValue() ) * Math.log( 1.0 / r.nextDouble() );
    }


    private void updatePropensities(DependentReactions reactions){
        reactions.getDependentReactions()
                .stream()
                .forEach(i-> {
                    aj[0] = aj[0].subtract(aj[i]);
                    aj[i] = computePropensity(i);
                    aj[0] = aj[0].add(aj[i]);
                });
    }

    private void updateState(int reactionIndex){
        ReactionComponent left = reactionsLeft[reactionIndex];
        ReactionComponent right = reactionsRight[reactionIndex];
        for (int i : left.keySet()){ state[i] -= left.get(i); }
        for (int i : right.keySet()){state[i] += right.get(i);}
    }

    private void updateTrajectory(){
        int[] newState = Arrays.copyOf(state,state.length);
        trajectory.add(new TrajectoryState(time,newState));
    }

    private int getReactionOrigin(int speciesIndex){
        int random = r.nextInt(state_y[speciesIndex]);
        random++;
        int sum =0;
        for (int i=0;i<reactionsLeft.length;i++){
            PairIndex p = new PairIndex(speciesIndex,i);
            if (matrixM.containsKey(p)){
                sum+= matrixM.get(p);
                if (sum>=random){
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateFluxes(int reactionIndex){
        ReactionComponent left = reactionsLeft[reactionIndex];
        for (Integer speciesIndex : left.keySet()){
            for (int i=0;i< left.get(speciesIndex);i++){
                int sourceReaction = getReactionOrigin(speciesIndex);
                matrixM.decrement(new PairIndex(speciesIndex,sourceReaction));
                state_y[speciesIndex]--;
                matrixF.increment(new TripleIndex(sourceReaction,reactionIndex,speciesIndex));
                matrixF_interval.increment(new TripleIndex(sourceReaction,reactionIndex,speciesIndex));
            }
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ReactionComponent right = reactionsRight[reactionIndex];
        for (Integer speciesIndex : right.keySet()){
            matrixM.add(new PairIndex(speciesIndex,reactionIndex),right.get(speciesIndex));
            state_y[speciesIndex] += right.get(speciesIndex);
        }
    }

    private void simulationStep(){
        stepCount++;
        time += computeTau();
        int mu = computeNextReaction();
        updateState(mu);
        updatePropensities(reactionDependencies[mu]);
        updateFluxes(mu);
        if (updateTrajectoryFlag) {
            if (stepCount % every==0) {
                updateTrajectory();
            }
        }
        if (trackIntervals && time > nextIntervalEnd){
            intervalFluxes.add(new IntervalFluxSet(matrixF_interval,time));
            nextIntervalEnd += interval;
            matrixF_interval = new Matrix<>();
        }
    }

    public void simulateWithStepNumber(int n){
        for (int i=0; i<n; i++){
            if (aj[0].doubleValue() == 0) {
                break;
            } else {
                simulationStep();
            }
        }
    };

    public void simulateWithTimeLimit(double endTime, boolean timeSeries, Controller controller)  {
        updateTrajectoryFlag = timeSeries;
        ProgressForm pForm = new ProgressForm();
        Task<Void> task = new Task() {
            @Override
            public Void call() //throws InterruptedException
            {
                int i =1;
                double timeInterval = endTime/100;
                double nextTime = timeInterval;
                while(time <endTime){
                    if (aj[0].doubleValue() == 0) {
                        break;
                    } else {
                        if (time > nextTime){
                            i++;
                            nextTime = i*timeInterval;
                            updateProgress(i, 100);
                        }
                        simulationStep();
                    }
                }
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                Platform.runLater(() -> {
                   controller.initiateFluxControls();
                });
                return null;
            }
        };
        pForm.activateProgressBar(task);
        task.setOnSucceeded(event -> {
            pForm.getDialogStage().close();
        });
        pForm.getDialogStage().show();
        Thread thread = new Thread(task);
        thread.start();
    }

    public Matrix<PairIndex> getM(){
        return matrixM;
    }

    public Matrix<TripleIndex> getF(){
        return matrixF;
    }

    public List<TrajectoryState> getTrajectory() {
        return trajectory;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void writeToFile(String filePath){
        // "src/main/resources/files-write-names.txt"
        String content = "Time, " +   String.join(", ",model.getMoleculesList()) + "\n";
        content = content + trajectory.stream().map(x->x.toString()).reduce("", String::concat);
        Path path = Path.of(filePath);
        try {
            Files.writeString(path, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void printPropensities(){
        System.out.println("Reaction propensities");
        for (int i=0; i< aj.length;i++){
            System.out.println(i + " : " + aj[i]);
        }
        System.out.println();
    }

    public void printTrajectory(){
        for (TrajectoryState ts : trajectory){
            System.out.print(ts);
        }
    }

    public void printSimulationState(){
        String result = IntStream.of(state)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
        System.out.println(time + "," + result + "\n");
        printPropensities();
    }

    public void printFluxes(){
        for (TripleIndex t : matrixF.keySet()){
            System.out.println(t.toString(model.getMoleculesList()) + " : " + matrixF.get(t));
        }
    }

    public void printFluxes(Matrix<TripleIndex> m){
        for (TripleIndex t : m.keySet()){
            System.out.println(t.toString(model.getMoleculesList()) + " : " + matrixF.get(t));
        }
    }

    public void printIntervalFluxes(){
        for (IntervalFluxSet m : intervalFluxes){
            System.out.println(m.getTimeStamp());
            for (TripleIndex t : m.getMatrix().keySet()){
                System.out.println(t.toString(model.getMoleculesList()) + " : " + matrixF.get(t));
            }
            System.out.println();
        }
    }

    public Matrix<TripleIndex> getMatrixF(){
        return matrixF;
    }

    public List<IntervalFluxSet> getIntervalFluxes(){
        return intervalFluxes;
    }
    public void tester(){
    }

    public SimulationModel getModel(){
        return model;
    }

    private String matrixToJson(Matrix<TripleIndex> matrix){
        boolean semafor = false;
        StringBuilder s = new StringBuilder("[\n\t");
        for (TripleIndex t : matrix.keySet()){
            if (semafor) { s.append(", \n\t");}
            semafor = true;
            s.append("{\"source\": " + t.getA() + ", ");
            s.append("\"target\": " + t.getB() + ", ");
            s.append("\"species\": \"" + model.getMoleculesList().get(t.getC()) + "\", ");
            s.append("\"flux\": " + matrix.get(t) + "}");
        }
        s.append(" ]");
        return s.toString();
    }

    private String intervalFluxToJson(IntervalFluxSet fluxSet){
        StringBuilder s = new StringBuilder("\n{ \"time\": " + fluxSet.getTimeStamp() + ",\n");
        s.append("  \"fluxes\": " + matrixToJson(fluxSet.getMatrix()) + "}");
        return s.toString();
    }

    private String allIntervalFluxesToJson(){
        boolean semafor = false;
        StringBuilder s = new StringBuilder("\n\"interval\": [");
        for (IntervalFluxSet f : intervalFluxes){
            if (semafor) { s.append(",\n"); }
            s.append(intervalFluxToJson(f));
            semafor = true;
        }
        s.append("]");
        return s.toString();
    }

    public String allFluxesToJson(){
        StringBuilder s = new StringBuilder("{ \"complete\": ");
        s.append(matrixToJson(matrixF));
        s.append(", ");
        s.append(allIntervalFluxesToJson());
        s.append("}");
        return s.toString();
    }

}
