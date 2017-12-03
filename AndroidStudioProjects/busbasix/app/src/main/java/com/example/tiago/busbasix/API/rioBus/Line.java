package com.example.tiago.busbasix.API.rioBus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 02/12/2017.
 */

public class Line {
    public String line ;
    public String description ;
    public String agency ;
    public String keywords ;
    public List<Spot> spots ;

    public Line(JSONObject jsonObject) throws JSONException {

        this.line = jsonObject.getString("line");
        this.description = jsonObject.getString("description");
        this.agency = jsonObject.getString("agency");
        this.keywords = jsonObject.getString("keywords");

        this.spots = new ArrayList<Spot>();
        JSONArray spots_response = jsonObject.getJSONArray("spots");
        for (int i=0; i<spots_response.length(); i++){
            JSONObject spot_object = spots_response.getJSONObject(i);
            Spot spot = new Spot(spot_object);
            spots.add(spot);
        }
    }
}
