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
import com.proyecto.afjb.remotephonefinder.adaptadores.RVAdaptadorImagenes;
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


public class FragmentImagenes extends Fragment{
    private OnFragmentInteractionListener mListener;
    private Usuario usuarioActivo;
    private int idDispositivo;
    private ProgressBar progressBar;
    private RecyclerView rv;

    public FragmentImagenes() {

    }

    public static FragmentImagenes newInstance(Usuario usuarioActivo, int idDispositivo) {
        FragmentImagenes fragment = new FragmentImagenes();
        Bundle bundle = new Bundle();
        bundle.putSerializable("usuarioActivo",usuarioActivo);
        bundle.putInt("idDispositivo",idDispositivo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioActivo = (Usuario)getArguments().getSerializable("usuarioActivo");
            idDispositivo = (int)getArguments().getInt("idDispositivo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_imagenes, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        progressBar = getView().findViewById(R.id.progressBarImagenes);
        rv = getView().findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        Toast.makeText(getContext(),"Cargando imágenes...", Toast.LENGTH_LONG);
        String urlCadena = getString(R.string.servicio_listar_imagenes);
        Servicio servicio = new Servicio(urlCadena,usuarioActivo.getId(),idDispositivo);
        servicio.execute();
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

    public class Servicio extends AsyncTask<String,Void,ArrayList<String>> {
        String urlCadena;
        int idDispositivo;
        int idUsuario;

        public Servicio(String urlCadena, int idUsuario, int idDispositivo) {
            this.urlCadena = urlCadena;
            this.idUsuario = idUsuario;
            this.idDispositivo = idDispositivo;
        }

        @Override
        protected void onPostExecute(ArrayList<String> imagenes) {
            progressBar.setVisibility(View.GONE);
            try {
                if (imagenes.size() > 0) {
                    RVAdaptadorImagenes adapter = new RVAdaptadorImagenes(imagenes);
                    rv.setAdapter(adapter);
                    rv.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "No se han encontrado imágenes", Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){} //Catch para manejar que el contexto sea NULl
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> imagenes = new ArrayList<>();
            final int TIME_OUT = 7500;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(urlCadena + "?id_usuario="+idUsuario+"&id_dispositivo="+idDispositivo)
                    .build();

            try {
                Response result = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(result.body().string());
                if(respuestaJSON.getInt("estado") == 1) {
                    JSONArray resultJSON = respuestaJSON.getJSONArray("imagenes");
                    String imagen;
                    for (int i = 0; i < resultJSON.length(); i++) {
                        imagen = resultJSON.getJSONObject(i).
                                getString("imagen");
                        imagenes.add(imagen);
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (JSONException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            }
            return imagenes;
        }
    }
}
