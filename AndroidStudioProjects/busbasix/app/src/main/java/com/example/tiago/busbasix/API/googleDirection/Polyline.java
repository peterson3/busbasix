package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 02/12/2017.
 */

public class Polyline {
    public String points;

    public Polyline(JSONObject jsonObject) throws JSONException {
        this.points = jsonObject.getString("points");
    }
}
