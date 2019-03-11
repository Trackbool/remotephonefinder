package com.proyecto.afjb.remotephonefinder.servicios;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServicioMarcarResuelto extends AsyncTask<String,Void,String> {
    String urlCadena;
    int idAccion;

    public ServicioMarcarResuelto(String urlCadena, int idAccion){
        this.urlCadena = urlCadena;
        this.idAccion = idAccion;
    }

    @Override
    protected void onPostExecute(String resultado) {

    }

    @Override
    protected String doInBackground(String... strings) {
        String resultado = "No se ha podido conectar";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urlCadena + "?id_accion="+idAccion)
                .build();

        try{
            Response response = client.newCall(request).execute();
            JSONObject respuestaJSON = new JSONObject(response.body().string());

            int resultJSON = respuestaJSON.getInt("estado");

            if (resultJSON == 1) {
                resultado = "Tarea actualizada correctamente";

            } else if (resultJSON == 2) {
                resultado = "La tarea no pudo actualizarse";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultado;
    }
}