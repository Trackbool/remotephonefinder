package com.proyecto.afjb.remotephonefinder.entidades;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Accion implements Serializable {
    int id;
    int instruccion;
    int idUsuario;
    int dispOrigen;
    int dispDestino;
    Calendar fecha;

    public Accion(int id, int instruccion, int idUsuario, int dispOrigen, int dispDestino){
        this.id = id;
        this.instruccion = instruccion;
        this.idUsuario = idUsuario;
        this.dispOrigen = dispOrigen;
        this.dispDestino = dispDestino;
    }

    public Accion(int id, int instruccion, int idUsuario, int dispOrigen, int dispDestino, String fecha){
        this.id = id;
        this.instruccion = instruccion;
        this.idUsuario = idUsuario;
        this.dispOrigen = dispOrigen;
        this.dispDestino = dispDestino;
        setFecha(fecha);
    }

    public void setId(int id){ this.id = id; }
    public int getId(){ return id; }
    public int getInstruccion() {
        return instruccion;
    }

    public void setInstruccion(int instruccion) {
        this.instruccion = instruccion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getDispOrigen() {
        return dispOrigen;
    }

    public void setDispOrigen(int dispOrigen) {
        this.dispOrigen = dispOrigen;
    }

    public int getDispDestino() {
        return dispDestino;
    }

    public void setDispDestino(int dispDestino) {
        this.dispDestino = dispDestino;
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
        return "Accion{" +
                "instruccion='" + instruccion + '\'' +
                ", idUsuario=" + idUsuario +
                ", dispOrigen=" + dispOrigen +
                ", dispDestino=" + dispDestino +
                ", fecha=" + getFecha() +
                '}';
    }
}
