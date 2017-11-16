package com.example.tiago.busbasix.API;

/**
 * Created by tiago on 11/11/2017.
 */

public class LinhaOnibus {
    private String identificador;
    private Rota rota;


    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Rota getRota() {
        return rota;
    }

    public void setRota(Rota rota) {
        this.rota = rota;
    }
}
