package com.example.tiago.busbasix.API.rioBus;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 02/12/2017.
 */

public class Spot {
    public double latitude;
    public double longitude;
    public Boolean returning;

    public Spot(JSONObject jsonObject) throws JSONException {

        this.latitude = jsonObject.getDouble("latitude");
        this.longitude = jsonObject.getDouble("longitude");
        this.returning = jsonObject.getBoolean("returning");

    }
}
