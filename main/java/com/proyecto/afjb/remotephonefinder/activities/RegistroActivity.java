package com.proyecto.afjb.remotephonefinder.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.proyecto.afjb.remotephonefinder.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

    EditText usuario, password, confirmarPass, email, telefono;
    Button registro, cancelar;

    String urlCadena = "http://www.domain.com/proyectoMultimedia/" +
            "registro_usuario.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        usuario = findViewById(R.id.usuario);
        password = findViewById(R.id.password);
        confirmarPass = findViewById(R.id.confirmarPass);
        email = findViewById(R.id.email);
        telefono = findViewById(R.id.telefono);

        registro = findViewById(R.id.registrar);
        cancelar = findViewById(R.id.cancelar);


        registro.setOnClickListener(this);
        cancelar.setOnClickListener(this);
    }

    private boolean validarCampos() {
        boolean valid = false;
        if(!(usuario.getText()+"").equals("")) {
            if (!(password.getText()+"").equals("")) {
                if (!(confirmarPass.getText()+"").equals("") && (confirmarPass.getText()+"").equals((password.getText()+""))) {
                    if (!(email.getText()+"").equals("")){
                        if (!(telefono.getText()+"").equals("")){
                            valid = true;
                        }
                        else
                            Toast.makeText(this, "El campo teléfono no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "El campo email no puede estar vacío", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "El campo contraseña no puede estar vacío", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,"El campo usuario no puede estar vacío",Toast.LENGTH_SHORT).show();


        return valid;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registrar:
                if (validarCampos()) {
                    Servicio servicio = new Servicio(urlCadena, usuario.getText() + "", password.getText()+"", email.getText()+"", telefono.getText()+"");
                    servicio.execute();
                }

                break;
            case R.id.cancelar:
                finish();
                break;
        }
    }



    public class Servicio extends AsyncTask<String,Void,Boolean> {
        String urlCadena, usuario, password, email, telefono;

        public Servicio(String urlCadena, String usuario, String password, String email, String telefono){
            this.urlCadena = urlCadena;
            this.usuario = usuario;
            this.password = password;
            this.email = email;
            this.telefono = telefono;
        }

        @Override
        protected void onPostExecute(Boolean registrado) {
            if(registrado){
                Toast.makeText(RegistroActivity.this,"Usuario registrado",Toast.LENGTH_LONG).show();
                finish();
            }
            else {
                Toast.makeText(RegistroActivity.this,"Hubo un error en el registro",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            final int TIME_OUT = 3500;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();

            Boolean registrado = false;

            try{
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("usuario", usuario);
                jsonParam.put("password", password);
                jsonParam.put("email", email);
                jsonParam.put("telefono", telefono);

                RequestBody body = RequestBody.create(JSON, jsonParam.toString());
                Request request = new Request.Builder()
                        .url(urlCadena)
                        .post(body)
                        .build();

                Response result = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(result.body().string());
                int estado = respuestaJSON.getInt("estado");

                if (estado == 1)
                    registrado = true;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return registrado;
        }
    }
}
