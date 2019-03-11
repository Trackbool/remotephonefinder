package com.proyecto.afjb.remotephonefinder.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.proyecto.afjb.remotephonefinder.utils.Permission;
import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextUsuario,editTextPassword;
    Button btnLogin, btnRegistro;
    TextView textViewResultado;
    ProgressBar progressBarLogin;
    Servicio servicio;
    String refDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarPermisos();

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnRegistro = findViewById(R.id.btnRegistro);
        btnRegistro.setOnClickListener(this);
        textViewResultado = findViewById(R.id.textViewResultado);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    private void inicializarPermisos(){
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(READ_PHONE_STATE);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(READ_CONTACTS);
        permissions.add(CALL_PHONE);
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);

        Permission permission = new Permission(this);
        permission.askPermissions(permissions);
    }

    public void logearse(String usuario, String password) {
        String urlCadena = getString(R.string.servicio_comprobar_login);
        servicio = new Servicio(urlCadena,usuario,password);
        servicio.execute();
        btnLogin.setEnabled(false);
        progressBarLogin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.btnLogin:
                String usuario = editTextUsuario.getText() + "";
                String password = editTextPassword.getText() + "";

                logearse(usuario,password);
                break;
            case R.id.btnRegistro:
                Intent intent = new Intent(this, RegistroActivity.class);
                startActivity(intent);
                break;
        }

    }

    public class Servicio extends AsyncTask<String,Void,Usuario>{
        String urlCadena, usuario, password;

        public Servicio(String urlCadena, String usuario, String password){
            this.urlCadena = urlCadena;
            this.usuario = usuario;
            this.password = password;
        }

        @Override
        protected void onPostExecute(Usuario usuarioActivo) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) LoginActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
                refDispositivo = tm.getDeviceId();
            }else{
                refDispositivo = "";
            }
            try {
                if (usuarioActivo != null) {
                    usuarioActivo.setReferenciaDispositivoActual(refDispositivo);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("usuarioActivo", usuarioActivo);
                    startActivity(intent);
                    finish();
                } else {
                    progressBarLogin.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    textViewResultado.setText("Credenciales erróneas");
                }
            }catch(Exception e){} //Catch para manejar que el contexto sea NULL
        }

        @Override
        protected Usuario doInBackground(String... strings) {
            final int TIME_OUT = 3500;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();
            Usuario usuarioActivo = null;

            Request request = new Request.Builder()
                    .url(urlCadena + "?usuario=" + usuario + "&password=" + password)
                    .build();

            try{
                Response result = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(result.body().string());
                int estado = respuestaJSON.getInt("estado");
                JSONArray resultJSON = respuestaJSON.getJSONArray("usuario");
                int id;
                String usuario,email,fechaAlta;
                if (resultJSON.length() == 1) {
                    id = resultJSON.getJSONObject(0)
                            .getInt("id");
                    usuario = resultJSON.getJSONObject(0)
                            .getString("usuario");
                    email = resultJSON.getJSONObject(0)
                            .getString("email");
                    fechaAlta = resultJSON.getJSONObject(0)
                            .getString("fechaalta");

                    usuarioActivo = new Usuario(id, usuario, email, fechaAlta);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return usuarioActivo;
        }
    }
}