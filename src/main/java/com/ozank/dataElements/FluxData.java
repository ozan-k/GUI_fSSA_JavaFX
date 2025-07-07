package com.ozank.dataElements;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class FluxData {
    private HashMap<String,ArrayList<FluxItem>> fluxes;
    private HashMap<String,String> reactions;

    @SerializedName("timeseries")
    private ArrayList<String> timeSeries;

    public HashMap<String, ArrayList<FluxItem>> getFluxes() {
        return fluxes;
    }

    public HashMap<String, String> getReactions() {
        return reactions;
    }

    public ArrayList<String> getTimeSeries() {
        return timeSeries;
    }

}
