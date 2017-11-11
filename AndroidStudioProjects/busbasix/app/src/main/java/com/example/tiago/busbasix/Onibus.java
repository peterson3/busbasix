package com.example.tiago.busbasix;

import android.text.method.DateTimeKeyListener;

import com.google.android.gms.maps.model.LatLng;

import java.security.Timestamp;
import java.util.Date;

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

}
