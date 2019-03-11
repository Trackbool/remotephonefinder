package com.proyecto.afjb.remotephonefinder.servicios;

import android.os.AsyncTask;
import android.util.Log;

import com.proyecto.afjb.remotephonefinder.activities.MainActivity;
import com.proyecto.afjb.remotephonefinder.entidades.Coordenada;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServicioEnviarLocalizacion extends AsyncTask<String, Void, String> {
    MainActivity activity;
    Coordenada coordenada;
    String urlCadena;

    public ServicioEnviarLocalizacion(MainActivity activity, String urlCadena, Coordenada coordenada) {
        this.activity = activity;
        this.coordenada = coordenada;
        this.urlCadena = urlCadena;
    }

    @Override
    protected void onPostExecute(String resultado) {
        Log.d("EnviarCoor",resultado);
    }

    @Override
    protected String doInBackground(String... strings) {
        String resultado = "No se ha podido establecer la conexión";

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final int TIME_OUT = 3500;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .build();

        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("latitud", coordenada.getLatitud());
            jsonParam.put("longitud", coordenada.getLongitud());
            jsonParam.put("ref_dispositivo", activity.getUsuarioActivo().getReferenciaDispositivoActual());

            RequestBody body = RequestBody.create(JSON, jsonParam.toString());
            Request request = new Request.Builder()
                    .url(urlCadena)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject respuestaJSON = new JSONObject(response.body().string());

            int resultJSON = respuestaJSON.getInt("estado");
            if (resultJSON == 1) {
                resultado = "Localización insertada correctamente";

            } else if (resultJSON == 2) {
                resultado = "La localización no pudo insertarse";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultado;
    }
}
