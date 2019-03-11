package com.proyecto.afjb.remotephonefinder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.proyecto.afjb.remotephonefinder.entidades.InstruccionesEnum;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentMain;
import com.proyecto.afjb.remotephonefinder.servicios.ServicioEnviarLocalizacion;
import com.proyecto.afjb.remotephonefinder.servicios.ServicioMarcarResuelto;
import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.utils.Sonidos;
import com.proyecto.afjb.remotephonefinder.utils.Tareas;
import com.proyecto.afjb.remotephonefinder.entidades.Accion;
import com.proyecto.afjb.remotephonefinder.entidades.Coordenada;
import com.proyecto.afjb.remotephonefinder.entidades.Usuario;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentAcciones;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentAnyadirDispositivo;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentContactos;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentDispositivos;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentImagenes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentDispositivos.OnFragmentInteractionListener,
        FragmentAnyadirDispositivo.OnFragmentInteractionListener,
        FragmentContactos.OnFragmentInteractionListener,
        FragmentAcciones.OnFragmentInteractionListener,
        FragmentImagenes.OnFragmentInteractionListener,
        FragmentMain.OnFragmentInteractionListener{

    Usuario usuarioActivo;
    Coordenada coordenada;
    Sonidos sonidos;
    Tareas tareas;
    TimerObtenerAcciones myTask;
    Timer myTimer;
    LocationManager lm;
    private SharedPreferences sharedPreferences;

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sonidos = new Sonidos(this);
        sonidos.anyadirSonido("notificacionFoto", R.raw.notificacion_foto);
        sonidos.anyadirSonido("notificacionBloquear", R.raw.notificacion_bloquear);

        usuarioActivo = (Usuario) getIntent().getSerializableExtra("usuarioActivo");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, FragmentMain.newInstance(getUsuarioActivo()))
                .addToBackStack(null)
                .commit();

        if(usuarioActivo != null && !usuarioActivo.getReferenciaDispositivoActual().equals("")) {
            tareas = new Tareas(this);
            myTask = new TimerObtenerAcciones(getString(R.string.servicio_listar_acciones));
            myTimer = new Timer();
            myTimer.schedule(myTask, 0, 3200);
        }

        iniciarGPS();
    }

    private void iniciarGPS(){
        if(usuarioActivo.getReferenciaDispositivoActual() != "") {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Long.parseLong(sharedPreferences.getString("selectedTimeLapse","3500")),
                        Float.parseFloat(sharedPreferences.getString("selectedMinDistance","0")), locationListener);

            } else {
                Toast.makeText(this, "No se está enviando señal GPS", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.preferencias) {
            startActivity(new Intent(this,PreferenciasActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean
    onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dispositivos) {
            FragmentDispositivos test = (FragmentDispositivos) getSupportFragmentManager().findFragmentByTag("FRAGMENT_DISPOSITIVOS");
            if ( !(test != null && test.isVisible()) ) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                transaction.replace(R.id.container, FragmentDispositivos.newInstance(usuarioActivo), "FRAGMENT_DISPOSITIVOS")
                        .addToBackStack(null)
                        .commit();
            }
        }
        else if (id == R.id.nav_contactos) {
            FragmentContactos test = (FragmentContactos) getSupportFragmentManager().findFragmentByTag("FRAGMENT_CONTACTOS");
            if ( !(test != null && test.isVisible()) ) {
                FragmentTransaction contactos = getSupportFragmentManager().beginTransaction();
                contactos.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                contactos.replace(R.id.container, FragmentContactos.newInstance(), "FRAGMENT_CONTACTOS")
                        .addToBackStack(null)
                        .commit();
            }
        }
        else if (id == R.id.nav_acciones) {
            FragmentAcciones test = (FragmentAcciones) getSupportFragmentManager().findFragmentByTag("FRAGMENT_ACCIONES");
            if ( !(test != null && test.isVisible()) ) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                transaction.replace(R.id.container, FragmentAcciones.newInstance(usuarioActivo), "FRAGMENT_ACCIONES")
                        .addToBackStack(null)
                        .commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //Cuando se detecta una localización, se llama al servicio para enviarla
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double latitud = location.getLatitude();
            double longitud = location.getLongitude();
            coordenada = new Coordenada(latitud,longitud);

            ServicioEnviarLocalizacion servicio = new ServicioEnviarLocalizacion(
                    MainActivity.this, getString(R.string.servicio_enviar_localizacion), coordenada);
            servicio.execute();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //Timer que va comprobando las acciones a ejecutar
    class TimerObtenerAcciones extends TimerTask {
        MainActivity.TimerObtenerAcciones.ServicioListarAcciones atask;
        String urlCadena;

        public TimerObtenerAcciones(String urlCadena){
            this.urlCadena = urlCadena;
        }
        final class ServicioListarAcciones extends AsyncTask<String,Void,ArrayList<Accion>> {
            String urlCadena;

            public ServicioListarAcciones(String urlCadena) {
                this.urlCadena = urlCadena;
            }

            @Override
            protected void onPostExecute(ArrayList<Accion> acciones) {
                String urlServicio = getString(R.string.servicio_marcar_accion_resuelta);
                Log.d("Cadena",acciones.size()+"");
                for(int i=0;i<acciones.size();i++){
                    Log.d("Cadena",acciones.get(i).toString());
                    switch(acciones.get(i).getInstruccion()){
                        case InstruccionesEnum.HACER_SONAR:
                            tareas.hacerSonar(10000);
                            break;
                        case InstruccionesEnum.SACAR_FOTO:
                            if(sharedPreferences.getBoolean("activeSound",true)) {
                                sonidos.reproducirSonido("notificacionFoto", 0);
                            }
                            tareas.sacarFoto(acciones.get(i));
                            break;
                        case InstruccionesEnum.BLOQUEAR:
                            if(sharedPreferences.getBoolean("activeSound",true)) {
                                sonidos.reproducirSonido("notificacionBloquear", 0);
                            }
                            tareas.bloquear();
                            break;
                        default:
                            break;
                    }
                    ServicioMarcarResuelto servicio = new ServicioMarcarResuelto(
                            urlServicio,acciones.get(i).getId());
                    servicio.execute();
                }
            }

            @Override
            protected ArrayList<Accion> doInBackground(String... strings) {
                ArrayList<Accion> acciones = new ArrayList<>();
                String fechaAccion;
                int id,instruccion,idUsuario,dispOrigen,dispDestino;

                final int TIME_OUT = 3500;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(urlCadena + "?ref="+usuarioActivo.getReferenciaDispositivoActual())
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    JSONObject respuestaJSON = new JSONObject(response.body().string());
                    int resultJSON = respuestaJSON.getInt("estado");
                    if (resultJSON == 1) {
                        JSONArray array = respuestaJSON.getJSONArray("acciones");
                        for(int i=0; i<array.length();i++) {
                            id = Integer.parseInt(array.getJSONObject(i)
                                    .getString("id"));
                            instruccion = array.getJSONObject(i)
                                    .getInt("idAccion");
                            idUsuario = Integer.parseInt(array.getJSONObject(i)
                                    .getString("usuario"));
                            dispOrigen = Integer.parseInt(array.getJSONObject(i)
                                    .getString("disporigen"));
                            dispDestino = Integer.parseInt(array.getJSONObject(i)
                                    .getString("dispdestino"));
                            fechaAccion = array.getJSONObject(i)
                                    .getString("fecha");

                            Accion accion = new Accion(
                                    id,instruccion,idUsuario,dispOrigen,dispDestino,fechaAccion);
                            acciones.add(accion);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return acciones;
            }
        }

        public void run() {
            atask = new ServicioListarAcciones(urlCadena);
            atask.execute();
        }
    }
    @Override
    public void onStop(){
        super.onStop();
    }
}
