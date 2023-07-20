package com.ozank.simulation;

import com.ozank.lexerParser.ModelBuilder;
import com.ozank.simulator.SimulationModel;
import com.ozank.simulator.SimulationSSA;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private List<String> molecules;
    public Simulation(String model_code){
        ModelBuilder builder = new ModelBuilder(model_code);
        SimulationModel model = new SimulationModel(builder.getReactions(),builder.getInitialState());
        // model.printReactions();
        // model.printState();
        int every = builder.getEvery();
        double interval = builder.getInterval();
        SimulationSSA simulation = new SimulationSSA(model,every,interval);
        simulation.simulateWithTimeLimit(builder.getEndTime());
        molecules = model.getMoleculesList();
    }

    public List<String> getMolecules() {
        return molecules;
    }

}
