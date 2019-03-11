package com.proyecto.afjb.remotephonefinder.entidades;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Coordenada {
    double latitud;
    double longitud;
    Calendar fecha;

    public Coordenada(double latitud, double longitud){
        this.latitud = latitud;
        this.longitud = longitud;
        this.setFecha("0000-00-00 00:00:00");
    }
    public Coordenada(double latitud, double longitud, String fecha){
        this.latitud = latitud;
        this.longitud = longitud;
        this.setFecha(fecha);
    }

    public void setLatitud(double latitud){
        this.latitud = latitud;
    }
    public double getLatitud(){
        return latitud;
    }
    public void setLongitud(double longitud){
        this.longitud = longitud;
    }
    public double getLongitud(){
        return longitud;
    }

    public void setFecha(String fecha){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date;
        try {
            date = sdf.parse(fecha);
        } catch (ParseException e) {
            date = new Date();
            Log.e("DateError","Formato inválido: "+fecha);
        }
        this.fecha = Calendar.getInstance();
        this.fecha.setTime(date);
    }
    public String getFecha(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        return sdf.format(fecha.getTime());
    }

    @Override
    public String toString() {
        return "Coordenada{" +
                "latitud=" + latitud +
                ", longitud=" + longitud +
                ", fecha=" + fecha +
                '}';
    }
}
