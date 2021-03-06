package com.proyecto.afjb.remotephonefinder.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.adaptadores.RVAdaptadorAcciones;
import com.proyecto.afjb.remotephonefinder.entidades.Dispositivo;
import com.proyecto.afjb.remotephonefinder.entidades.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class FragmentAcciones extends Fragment{
    private OnFragmentInteractionListener mListener;
    private Usuario usuarioActivo;
    private ProgressBar progressBar;
    private RecyclerView rv;

    public FragmentAcciones() {

    }

    public static FragmentAcciones newInstance(Usuario usuarioActivo) {
        FragmentAcciones fragment = new FragmentAcciones();
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
        return inflater.inflate(R.layout.fragment_acciones, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        progressBar = getView().findViewById(R.id.progressBarDispositivos);
        rv = getView().findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        String urlCadena = getString(R.string.servicio_listar_dispositivos);
        if(usuarioActivo !=null) {
            Servicio servicio = new Servicio(urlCadena, usuarioActivo.getId());
            servicio.execute();
        }else{
            Log.e("ERROR","El usuario activo es NULL en el fragment");
        }
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class Servicio extends AsyncTask<String,Void,ArrayList<Dispositivo>> {
        String urlCadena;
        int idUsuario;

        public Servicio(String urlCadena, int idUsuario) {
            this.urlCadena = urlCadena;
            this.idUsuario = idUsuario;
        }

        @Override
        protected void onPostExecute(ArrayList<Dispositivo> dispositivos) {
            progressBar.setVisibility(View.GONE);
            if(dispositivos.size()>0) {
                RVAdaptadorAcciones adapter = new RVAdaptadorAcciones(getContext(), usuarioActivo.getId(), dispositivos);
                rv.setAdapter(adapter);
                rv.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(getContext(),"No se han encontrado dispositivos", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected ArrayList<Dispositivo> doInBackground(String... strings) {
            ArrayList<Dispositivo> dispositivos = new ArrayList<>();
            final int TIME_OUT = 3500;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(urlCadena + "?id_usuario="+idUsuario)
                    .build();

            try {
                Response result = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(result.body().string());
                if(respuestaJSON.getInt("estado") == 1) {
                    JSONArray resultJSON = respuestaJSON.getJSONArray("dispositivos");
                    int id;
                    String referencia, nombre, modelo, fechaRegistro;
                    for (int i = 0; i < resultJSON.length(); i++) {
                        id = resultJSON.getJSONObject(i).
                                getInt("id");
                        referencia = resultJSON.getJSONObject(i).
                                getString("referencia");
                        nombre = resultJSON.getJSONObject(i).
                                getString("nombre");
                        modelo = resultJSON.getJSONObject(i).
                                getString("modelo");
                        fechaRegistro = resultJSON.getJSONObject(i).
                                getString("fecharegistro");
                        dispositivos.add(new Dispositivo(
                                id,referencia, nombre, modelo, fechaRegistro));
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (JSONException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            }
            return dispositivos;
        }
    }
}
