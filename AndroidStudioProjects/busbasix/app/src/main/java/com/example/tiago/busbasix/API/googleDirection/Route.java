package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 11/11/2017.
 */

public class Route {

    public Bounds bounds;
    public String copyrights;
    public List<Leg> legs;
    public OverviewPolyline overview_polyline;
    public String summary;
    public List<String> warnings;
    public List<Object> waypoint_order;
    public Fare fare;

    public Route(JSONObject jsonObject) throws JSONException {

        this.summary = jsonObject.getString("summary");

        this.warnings = new ArrayList<String>();
        JSONArray warnings = jsonObject.getJSONArray("warnings");
        for (int i=0; i<warnings.length(); i++){
            String warning = warnings.getString(i);
            this.warnings.add(warning);
        }

        this.copyrights = jsonObject.getString("copyrights");


        this.fare = new Fare(jsonObject.getJSONObject("fare"));

        this.legs = new ArrayList<Leg>();
        JSONArray legs_response = jsonObject.getJSONArray("legs");
        for (int i=0; i<legs_response.length(); i++){
            JSONObject leg_object = legs_response.getJSONObject(i);
            Leg leg = new Leg(leg_object);
            legs.add(leg);
        }

        this.overview_polyline = new OverviewPolyline(jsonObject.getJSONObject("overview_polyline"));

    }


}