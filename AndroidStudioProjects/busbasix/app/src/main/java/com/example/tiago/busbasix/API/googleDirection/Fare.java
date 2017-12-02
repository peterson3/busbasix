package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 02/12/2017.
 */

public class Fare {
    public String currency;
    public String text;
    public double value;

    public Fare(JSONObject jsonObject) throws JSONException {

        this.currency = jsonObject.getString("currency");
        this.text = jsonObject.getString("text");
        this.value = jsonObject.getDouble("value");

    }
}
