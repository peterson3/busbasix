package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by tiago on 02/12/2017.
 */

public class Line {
    public List<Agency> agencies ;
    public String color ;
    public String name ;
    public String short_name ;
    public Vehicle vehicle ;
    public String url ;

    public Line(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.getString("name");
        this.short_name = jsonObject.getString("short_name");
    }
}
