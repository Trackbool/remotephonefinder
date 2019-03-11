package com.proyecto.afjb.remotephonefinder.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.adaptadores.RVContactosAdaptador;
import com.proyecto.afjb.remotephonefinder.entidades.Contacto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FragmentContactos extends Fragment {

    private OnFragmentInteractionListener mListener;
    private RecyclerView rv;

    public FragmentContactos() {
        // Required empty public constructor
    }

    public static FragmentContactos newInstance() {
        FragmentContactos fragment = new FragmentContactos();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contactos, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        rv = getView().findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArrayList<Contacto> contactos = getContacts();

        String urlCadena = "http://adrianfa.000webhostapp.com/proyectoMultimedia/" +
                "obtener_telefonos.php";

        Servicio servicio = new Servicio(urlCadena, contactos);
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


    public ArrayList<Contacto> getContacts(){

        String[] projection = new String[] {ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        String selectionClause = ContactsContract.Data.MIMETYPE + "='" +
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
                + ContactsContract.CommonDataKinds.Phone.NUMBER + " IS NOT NULL";


        String sortOrder = ContactsContract.Data.DISPLAY_NAME + " ASC";

        Cursor cursor = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selectionClause, null, sortOrder);

        ArrayList<Contacto> contactos = new ArrayList<>();

        while (cursor.moveToNext()) {
            contactos.add(new Contacto(cursor.getString(0), cursor.getString(1)));
        }

        return contactos;
    }



    public class Servicio extends AsyncTask<String,Void,ArrayList<Contacto>> {
        String urlCadena;
        ArrayList<Contacto> contactos;

        public Servicio(String urlCadena, ArrayList<Contacto> contactos) {
            this.urlCadena = urlCadena;
            this.contactos = contactos;
        }

        @Override
        protected void onPostExecute(ArrayList<Contacto> contactosUso) {
            RVContactosAdaptador adapter = new RVContactosAdaptador(contactosUso);
            rv.setAdapter(adapter);
        }

        @Override
        protected ArrayList<Contacto> doInBackground(String... strings) {
            ArrayList<String> resultado = new ArrayList<>();
            ArrayList<Contacto> contactosUso = new ArrayList<>();
            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .url(urlCadena)
                    .build();

            try {
                Response result = client.newCall(request).execute();
                JSONObject respuestaJSON = new JSONObject(result.body().string());
                if(respuestaJSON.getInt("estado") == 1) {
                    JSONArray resultJSON = respuestaJSON.getJSONArray("telefonos");

                    String tlf;
                    for (int i = 0; i < resultJSON.getJSONArray(0).length(); i++) {
                        tlf = resultJSON.getJSONArray(0).getJSONObject(i).
                                getString("telefono").replace(" ","");
                        tlf.substring(tlf.length()-9);
                        resultado.add(tlf);
                    }

                    for (Contacto c: contactos) {
                        tlf = c.getTelefono().replace(" ", "");
                        if(tlf.length()>=9) {
                            tlf = tlf.substring(tlf.length() - 9);

                            if (resultado.contains(tlf)) {
                                contactosUso.add(c);
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (IOException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            } catch (JSONException e) {
                Log.e("PROYECTO_ERROR", e.getMessage());
            }
            return contactosUso;
        }
    }
}
