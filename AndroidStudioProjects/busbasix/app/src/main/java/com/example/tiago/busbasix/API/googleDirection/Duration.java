package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 02/12/2017.
 */

public class Duration {
    public String text;
    public int value;

    public Duration(){

    }
    public Duration(JSONObject jsonObject) throws JSONException {

        this.text = jsonObject.getString("text");
        this.value = jsonObject.getInt("value");
    }
}
