package com.proyecto.afjb.remotephonefinder.entidades;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dispositivo {
    int id;
    String referencia;
    String nombre;
    String modelo;
    Calendar fechaRegistro;

    public Dispositivo(int id, String referencia,String nombre,String modelo,String fechaRegistro){
        this.id = id;
        this.referencia = referencia;
        this.nombre = nombre;
        this.modelo = modelo;
        setFechaRegistro(fechaRegistro);
    }
    public Dispositivo(int id, String referencia,String nombre,String modelo){
        this.id = id;
        this.referencia = referencia;
        this.nombre = nombre;
        this.modelo = modelo;
    }
    public Dispositivo(String referencia,String nombre,String modelo,String fechaRegistro){
        this.referencia = referencia;
        this.nombre = nombre;
        this.modelo = modelo;
        setFechaRegistro(fechaRegistro);
    }
    public Dispositivo(String referencia,String nombre,String modelo){
        this.referencia = referencia;
        this.nombre = nombre;
        this.modelo = modelo;
    }
    public void setId(int id){this.id = id;}
    public int getId(){return id;}
    public void setReferencia(String referencia){
        this.referencia = referencia;
    }
    public String getReferencia(){
        return referencia;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    public String getNombre(){
        return nombre;
    }
    public void setModelo(String modelo) {
        this.modelo = modelo;
    }
    public String getModelo(){
        return modelo;
    }
    public void setFechaRegistro(String fechaRegistro){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(fechaRegistro);
        } catch (ParseException e) {
            date = new Date();
            Log.e("DateError","Formato inválido: "+fechaRegistro);
        }
        this.fechaRegistro = Calendar.getInstance();
        this.fechaRegistro.setTime(date);
    }
    public String getFechaRegistro(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(fechaRegistro.getTime());
    }

    @Override
    public String toString() {
        return "[Referencia: "+getReferencia()+",Nombre: "+getNombre()+"," +
                "Modelo: "+getModelo()+",FechaRegistro: "+getFechaRegistro()+"]";
    }
}
