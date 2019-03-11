package com.proyecto.afjb.remotephonefinder.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Dispositivo;
import com.proyecto.afjb.remotephonefinder.entidades.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FragmentAnyadirDispositivo extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    private Usuario usuarioActivo;
    private Button btnAnyadir;
    private EditText editTextReferencia,editTextNombre,editTextModelo;
    private TextView textViewResultado;

    public FragmentAnyadirDispositivo() {

    }

    public static FragmentAnyadirDispositivo newInstance(Usuario usuarioActivo) {
        FragmentAnyadirDispositivo fragment = new FragmentAnyadirDispositivo();
        Bundle bundle = new Bundle();
        bundle.putSerializable("usuarioActivo",usuarioActivo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioActivo = (Usuario)getArguments().getSerializable("usuarioActivo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anyadir_dispositivo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        editTextReferencia = getView().findViewById(R.id.editTextReferencia);
        editTextNombre = getView().findViewById(R.id.editTextNombre);
        editTextModelo = getView().findViewById(R.id.editTextModelo);
        textViewResultado = getView().findViewById(R.id.textViewResultado);
        btnAnyadir = getView().findViewById(R.id.btnAnyadir);
        btnAnyadir.setOnClickListener(this);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAnyadir:
                Dispositivo dispositivo = new Dispositivo(String.valueOf(editTextReferencia.getText()),
                        String.valueOf(editTextNombre.getText()),String.valueOf(editTextModelo.getText()));

                String urlCadena = getString(R.string.servicio_insertar_dispositivo);

                if(usuarioActivo !=null) {
                    Servicio servicio = new Servicio(urlCadena, dispositivo, usuarioActivo.getId());
                    servicio.execute();
                }else{
                    Log.e("ERROR","El usuario activo es NULL en el fragment");
                }
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class Servicio extends AsyncTask<String,Void,String> {
        String urlCadena;
        Dispositivo dispositivo;
        int idUsuario;
        boolean exito;

        public Servicio(String urlCadena, Dispositivo dispositivo, int idUsuario) {
            this.urlCadena = urlCadena;
            this.dispositivo = dispositivo;
            this.idUsuario = idUsuario;
            exito = false;
        }

        @Override
        protected void onPostExecute(String resultado) {
            if(exito)
                textViewResultado.setTextColor(Color.BLACK);
            else
                textViewResultado.setTextColor(Color.RED);
            textViewResultado.setText(resultado);
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

            try{
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("referencia", dispositivo.getReferencia());
                jsonParam.put("nombre",dispositivo.getNombre());
                jsonParam.put("modelo", dispositivo.getModelo());
                jsonParam.put("id_usuario", idUsuario);

                RequestBody body = RequestBody.create(JSON, jsonParam.toString());
                Request request = new Request.Builder()
                        .url(urlCadena)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(response.body().string());

                int resultJSON = respuestaJSON.getInt("estado");

                if (resultJSON == 1) {
                    exito = true;
                    resultado = "Dispositivo insertado correctamente";

                } else if (resultJSON == 2) {
                    resultado = "El dispositivo no pudo insertarse";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return resultado;
        }
    }
}
