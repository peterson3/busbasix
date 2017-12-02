package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 02/12/2017.
 */

public class GeocodedWaypoint {

    public String geocoder_status ;
    public String place_id;
    public List<String> types;

    public GeocodedWaypoint(JSONObject jsonObject) throws JSONException {
        this.geocoder_status = jsonObject.getString("geocoder_status");
        this.place_id = jsonObject.getString("place_id");

        this.types = new ArrayList<String>();
        JSONArray types = jsonObject.getJSONArray("types");
        for (int i=0; i<types.length(); i++){
            String type = types.getString(i);
            this.types.add(type);
        }
    }
}
