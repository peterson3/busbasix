package com.example.tiago.busbasix.API.googleDirection;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 02/12/2017.
 */

public class Direction {
    public List<GeocodedWaypoint> geocoded_waypoints;
    public List<Route> routes;
    public String status;

    public Direction(JSONObject jsonObject) throws JSONException {

        this.status = jsonObject.getString("status");
        if (       this.status == "NOT_FOUND"
                || this.status == "ZERO_RESULTS"
                || this.status == "MAX_WAYPOINTS_EXCEEDED"
                || this.status == "INVALID_REQUEST"
                || this.status == "OVER_QUERY_LIMIT"
                || this.status == "REQUEST_DENIED"
                || this.status == "UNKNOWN_ERROR"
                )
        {
            return;
        }

        this.geocoded_waypoints = new ArrayList<GeocodedWaypoint>();
        JSONArray geocoded_waypoints_response = jsonObject.getJSONArray("geocoded_waypoints");
        for (int i=0; i<geocoded_waypoints_response.length(); i++){
            JSONObject geocoded_waypoints_object = geocoded_waypoints_response.getJSONObject(i);
            GeocodedWaypoint geocodedWaypoint = new GeocodedWaypoint(geocoded_waypoints_object);
            geocoded_waypoints.add(geocodedWaypoint);
        }


        this.routes = new ArrayList<Route>();
        JSONArray routes_response = jsonObject.getJSONArray("routes");
        for (int i=0; i<routes_response.length(); i++){
            JSONObject route_object = routes_response.getJSONObject(i);
            Route route = new Route(route_object);
            routes.add(route);
        }

    }
}
