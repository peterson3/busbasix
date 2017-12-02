package com.example.tiago.busbasix.API.googleDirection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 02/12/2017.
 */

public class TransitDetails {

    public Stop arrival_stop ;
    public ArrivalTime arrival_time ;
    public Stop departure_stop ;
    public DepartureTime departure_time ;
    public String headsign ;
    public int headway ;
    public Line line ;
    public int num_stops ;

    public TransitDetails(JSONObject jsonObject) throws JSONException {

        JSONObject line_object =  jsonObject.getJSONObject("line");
        this.line = new Line(line_object);
    }
}
