package com.example.tiago.busbasix.API.busBasix;

import android.text.method.DateTimeKeyListener;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 22/10/2017.
 */

public class Onibus {

     private Date dataHora;
     private String ordem;
     private String linha;
     private LatLng latLong;
     private Float velocidade;
     private Integer direcao;

    public Onibus (){

    }

    public Onibus (JSONObject jsonObject) throws JSONException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
        Date convertedDate = new Date();
        convertedDate= dateFormat.parse(jsonObject.getString("DataHora"));
        this.setDataHora(convertedDate);
        this.setOrdem(jsonObject.getString("Ordem"));
        this.setLinha(jsonObject.getString("Linha"));
        this.setLatLong(new LatLng(Double.parseDouble(jsonObject.getString("Latitude")), Double.parseDouble(jsonObject.getString("Longitude"))));
        this.setVelocidade(Float.parseFloat(jsonObject.getString("Velocidade")));
        this.setDirecao(Integer.parseInt(jsonObject.getString("Direcao")));
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public String getOrdem() {
        return ordem;
    }

    public void setOrdem(String ordem) {
        this.ordem = ordem;
    }

    public String getLinha() {
        return linha;
    }

    public void setLinha(String linha) {
        this.linha = linha;
    }

    public LatLng getLatLong() {
        return latLong;
    }

    public void setLatLong(LatLng latLong) {
        this.latLong = latLong;
    }

    public Float getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Float velocidade) {
        this.velocidade = velocidade;
    }

    public Integer getDirecao() {
        return direcao;
    }

    public void setDirecao(Integer direcao) {
        this.direcao = direcao;
    }

    @Override
    public String toString(){
        return this.getLinha() + " | " + this.getOrdem() + " | " + this.getLinha() + " | " + this.getLatLong().latitude + " | " + this.getLatLong().longitude;
    }

}
