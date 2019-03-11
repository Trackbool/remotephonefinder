package com.proyecto.afjb.remotephonefinder.entidades;

import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Usuario implements Serializable {
    int id;
    String usuario;
    String email;
    Calendar fechaAlta;
    String referenciaDispositivoActual;

    public Usuario(int id,String usuario,String email,String fechaAlta){
        this.id = id;
        this.usuario = usuario;
        this.email = email;
        setFechaAlta(fechaAlta);
        referenciaDispositivoActual = "";
    }

    public Usuario(int id, String usuario, String email, String fechaAlta, String referenciaDispositivoActual){
        this.id = id;
        this.usuario = usuario;
        this.email = email;

        setFechaAlta(fechaAlta);
        this.referenciaDispositivoActual = referenciaDispositivoActual;
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void setUsuario(String usuario){
        this.usuario = usuario;
    }
    public String getUsuario(){
        return usuario;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
    public void setFechaAlta(String fechaAlta){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(fechaAlta);
        } catch (ParseException e) {
            date = new Date();
            Log.e("DateError","Formato inválido: "+fechaAlta);
        }
        this.fechaAlta = Calendar.getInstance();
        this.fechaAlta.setTime(date);
    }
    public String getFechaAlta(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(fechaAlta.getTime());
    }
    public String getReferenciaDispositivoActual() {
        return referenciaDispositivoActual;
    }
    public void setReferenciaDispositivoActual(String referenciaDispositivoActual) {
        this.referenciaDispositivoActual = referenciaDispositivoActual;
    }
    @Override
    public String toString(){
        return "[Id: "+getId()+",Usuario: "+getUsuario()+",Email: "+getEmail()+"," +
                "FechaAlta: "+getFechaAlta()+"]";
    }
}
