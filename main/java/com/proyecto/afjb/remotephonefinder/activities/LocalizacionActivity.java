package com.proyecto.afjb.remotephonefinder.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Coordenada;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LocalizacionActivity extends AppCompatActivity implements View.OnClickListener {
    MapView mapView;
    Marker marker;
    MapboxMap mapboxMap;
    ProgressBar progressBarMapa;
    Button btnEnfocar;
    TextView textViewUltimaLocalizacion;
    TextView textViewFechaHora;

    SensorManager mSensorManager;
    float mAccel;
    float mAccelCurrent;
    float mAccelLast;

    boolean hayLocalizaciones;
    int idDispositivo;
    MyTimerTask myTask;
    Timer myTimer;
    String urlCadena;
    Double latitud, longitud;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.tokenMapBox));
        setContentView(R.layout.activity_localizacion);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        progressBarMapa = findViewById(R.id.progressBarMapa);
        textViewUltimaLocalizacion = findViewById(R.id.textViewUltimaLocalizacion);
        textViewFechaHora = findViewById(R.id.textViewFechaHora);
        btnEnfocar = findViewById(R.id.btnEnfocar);
        btnEnfocar.setOnClickListener(this);
        idDispositivo = getIntent().getExtras().getInt("idDispositivo");
        hayLocalizaciones = false;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        urlCadena = getString(R.string.servicio_listar_coordenadas);
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 12) {
                enfocarMapa();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void inicializarMapa(){
        Log.d("CHECKKKK","yes");

        double tilt = 10, zoom = Double.parseDouble(sharedPreferences.getString("selectedZoom", "10")),latitudCamara = latitud,longitudCamara = longitud;
        if(mapboxMap != null && mapboxMap.getCameraPosition() != null) {
            tilt = mapboxMap.getCameraPosition().tilt;
            zoom = mapboxMap.getCameraPosition().zoom;
            latitudCamara = mapboxMap.getCameraPosition().target.getLatitude();
            longitudCamara = mapboxMap.getCameraPosition().target.getLongitude();
        }

        cargarMapa(latitudCamara,longitudCamara,zoom,tilt);
        progressBarMapa.setVisibility(View.GONE);
        btnEnfocar.setVisibility(View.VISIBLE);
        textViewUltimaLocalizacion.setVisibility(View.VISIBLE);
        textViewFechaHora.setVisibility(View.VISIBLE);
    }

    public void cargarMapa(double latitudCamara,double longitudCamara, double zoom, double tilt){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitudCamara,longitudCamara))
                .zoom(zoom)
                .tilt(tilt)
                .build();

        mapView.getMapAsync(mapboxMap -> {
            this.mapboxMap = mapboxMap;

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),
                    1000);

            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);
            mapboxMap.getUiSettings().setCompassMargins(80,180,0,0);

            if(marker!=null)
                marker.remove();
            marker = mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitud, longitud))
                    .title("Localización")
                    .snippet("Aquí se encuentra el dispositivo"));
        });
    }

    public void enfocarMapa(){
        if(latitud!=null && longitud!=null) {
            double tilt, zoom;
            tilt = mapboxMap.getCameraPosition().tilt;
            zoom = mapboxMap.getCameraPosition().zoom;
            cargarMapa(latitud, longitud, zoom, tilt);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnEnfocar:
                enfocarMapa();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        //Creamos una clase TimerTask y la ejecutamos cada X segundos para ir actualizando las coordenadas
        myTask = new MyTimerTask(urlCadena);
        myTimer = new Timer();
        myTimer.schedule(myTask, 0, Integer.parseInt(sharedPreferences.getString("selectedTimeLapse", "3500")));
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if(myTimer!=null)
            myTimer.cancel();
        mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    class MyTimerTask extends TimerTask {
        MyAsyncTask atask;
        String urlCadena;

        public MyTimerTask(String urlCadena){
            this.urlCadena = urlCadena;
        }
        final class MyAsyncTask extends AsyncTask<String,Void,Coordenada> {
            String urlCadena;

            public MyAsyncTask(String urlCadena) {
                this.urlCadena = urlCadena;
            }

            @Override
            protected void onPostExecute(Coordenada coordenada) {
                if(coordenada != null) {
                    hayLocalizaciones = true;
                    latitud = coordenada.getLatitud();
                    longitud = coordenada.getLongitud();
                    textViewFechaHora.setText(coordenada.getFecha());
                    inicializarMapa();
                }
                else if(!hayLocalizaciones){
                    myTimer.cancel();
                    Toast.makeText(LocalizacionActivity.this,"No hay localizaciones para este dispositivo",Toast.LENGTH_LONG).show();
                    LocalizacionActivity.this.finish();
                }
            }

            @Override
            protected Coordenada doInBackground(String... strings) {
                Coordenada coordenada = null;
                double latitud,longitud;
                String fecha;

                final int TIME_OUT = 3500;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(urlCadena + "?id_dispositivo="+idDispositivo)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    JSONObject respuestaJSON = new JSONObject(response.body().string());
                    int resultJSON = respuestaJSON.getInt("estado");
                    if (resultJSON == 1) {
                        latitud = respuestaJSON.getJSONArray("coordenadas").getJSONObject(0).getDouble("latitud");
                        longitud = respuestaJSON.getJSONArray("coordenadas").getJSONObject(0).getDouble("longitud");
                        fecha = respuestaJSON.getJSONArray("coordenadas").getJSONObject(0).getString("registrotiempo");
                        coordenada = new Coordenada(latitud,longitud,fecha);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return coordenada;
            }
        }

        public void run() {
            atask = new MyAsyncTask(urlCadena);
            atask.execute();
        }
    }
}
