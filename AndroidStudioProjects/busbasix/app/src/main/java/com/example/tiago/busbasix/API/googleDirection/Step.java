package com.example.tiago.busbasix.API.googleDirection;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 02/12/2017.
 */

public class Step {

    public Distance distance;
    public Duration duration;
    public Location end_location;
    public String html_instructions;
    public Polyline polyline;
    public List<Step> steps;
    public Location start_location;
    public String travel_mode;
    public String maneuver;
    public TransitDetails transit_details;


    public Step(JSONObject jsonObject) throws JSONException {

        try{
            JSONObject transit_details_object =  jsonObject.getJSONObject("transit_details");
            this.transit_details = new TransitDetails(transit_details_object);
        }
        catch (JSONException ex){
            this.transit_details = null;
        }


    }
}
