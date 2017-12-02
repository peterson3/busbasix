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

public class Leg {
    public Distance distance ;
    public Duration duration ;
    public String end_address ;
    public Location end_location;
    public String start_address ;
    public Location start_location ;
    public List<Step> steps ;
    public List<Object> traffic_speed_entry ;
    public List<Object> via_waypoint ;
    public ArrivalTime arrival_time ;
    public DepartureTime departure_time;

    public Leg(JSONObject jsonObject) throws JSONException {

        this.steps = new ArrayList<Step>();
        JSONArray steps_response = jsonObject.getJSONArray("steps");
        for (int i=0; i<steps_response.length(); i++){
            JSONObject step_object = steps_response.getJSONObject(i);
            Step step = new Step(step_object);
            this.steps.add(step);
        }

    }
}

