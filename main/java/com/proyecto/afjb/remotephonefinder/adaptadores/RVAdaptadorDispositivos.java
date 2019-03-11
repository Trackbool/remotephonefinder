package com.proyecto.afjb.remotephonefinder.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.proyecto.afjb.remotephonefinder.activities.LocalizacionActivity;
import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Dispositivo;
import com.proyecto.afjb.remotephonefinder.entidades.Usuario;
import com.proyecto.afjb.remotephonefinder.fragments.FragmentImagenes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RVAdaptadorDispositivos extends RecyclerView.Adapter<RVAdaptadorDispositivos.DispositivoViewHolder>{
    Context context;
    Usuario usuarioActivo;
    List<Dispositivo> dispositivos;

    public RVAdaptadorDispositivos(Context context, Usuario usuarioActivo, List<Dispositivo> dispositivos) {
        this.context = context;
        this.usuarioActivo = usuarioActivo;
        this.dispositivos = dispositivos;
    }

    public class DispositivoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        List<Dispositivo> dispositivos;
        CardView cv;
        TextView nombreDispositivo;
        TextView modeloDispositivo;
        ImageView btnLocalizar;
        ImageView btnGaleria;
        ImageView btnEliminar;

        DispositivoViewHolder(View itemView, List<Dispositivo> dispositivos) {
            super(itemView);
            this.dispositivos = dispositivos;
            cv = (CardView)itemView.findViewById(R.id.cv);
            nombreDispositivo = itemView.findViewById(R.id.nombre_dispositivo);
            modeloDispositivo = itemView.findViewById(R.id.modelo_dispositivo);
            btnLocalizar = itemView.findViewById(R.id.btnLocalizar);
            btnGaleria = itemView.findViewById(R.id.btnGaleria);
            btnEliminar = itemView.findViewById(R.id.btnFoto);

            btnLocalizar.setOnClickListener(this);
            btnGaleria.setOnClickListener(this);
            btnEliminar.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnFoto:
                    String urlCadena = context.getString(R.string.servicio_borrar_dispositivo);
                    Servicio servicio = new Servicio(urlCadena,btnEliminar.getContentDescription()+"");
                    servicio.execute();
                    break;
                case R.id.btnGaleria:
                    FragmentImagenes test = (FragmentImagenes) ((FragmentActivity)context)
                            .getSupportFragmentManager().findFragmentByTag("FRAGMENT_IMAGENES");

                    if ( !(test != null && test.isVisible()) ) {

                        FragmentTransaction transaction = ((FragmentActivity)context)
                                .getSupportFragmentManager().beginTransaction();
                        //transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                        transaction.replace(R.id.container, FragmentImagenes.newInstance(
                                usuarioActivo,Integer.parseInt(
                                        btnGaleria.getContentDescription()+"")), "FRAGMENT_IMAGENES")
                                .addToBackStack(null)
                                .commit();
                    }
                    break;
                case R.id.btnLocalizar:
                    Intent intent = new Intent(v.getContext(), LocalizacionActivity.class);
                    intent.putExtra("idDispositivo", Integer.parseInt(
                            btnLocalizar.getContentDescription()+""));
                    v.getContext().startActivity(intent);
                    break;
            }
        }

        public class Servicio extends AsyncTask<String,Void,String> {
            String urlCadena;
            String idDispositivo;

            public Servicio(String urlCadena, String idDispositivo) {
                this.urlCadena = urlCadena;
                this.idDispositivo = idDispositivo;
            }

            @Override
            protected void onPostExecute(String resultado) {
                //Mostrar mensaje
                if(dispositivos.size() >= getAdapterPosition() && getAdapterPosition() >= 0) {
                    dispositivos.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), dispositivos.size());
                }
                Log.d("Dispositivo",resultado);
            }

            @Override
            protected String doInBackground(String... strings) {
                String resultado = "Error al conectar";
                final int TIME_OUT = 3500;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .build();

                Request request = new Request.Builder()
                        .url(urlCadena + "?id_dispositivo="+idDispositivo)
                        .build();

                try {
                    Response result = client.newCall(request).execute();
                    JSONObject respuestaJSON = new JSONObject(result.body().string());
                    if(respuestaJSON.getInt("estado") == 1) {
                        resultado = "Dispositivo eliminado correctamente";
                    }
                    else{
                        resultado = "No se pudo eliminar el dispositivo";
                    }
                } catch (MalformedURLException e) {
                    Log.e("PROYECTO_ERROR", e.getMessage());
                } catch (IOException e) {
                    Log.e("PROYECTO_ERROR", e.getMessage());
                } catch (JSONException e) {
                    Log.e("PROYECTO_ERROR", e.getMessage());
                }
                return resultado;
            }
        }
    }

    @NonNull
    @Override
    public DispositivoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_dispositivo,
                viewGroup, false);
        return new DispositivoViewHolder(v,dispositivos);
    }

    @Override
    public void onBindViewHolder(@NonNull DispositivoViewHolder dispositivoViewHolder, int i) {
        dispositivoViewHolder.nombreDispositivo.setText(dispositivos.get(i).getNombre());
        dispositivoViewHolder.modeloDispositivo.setText(dispositivos.get(i).getModelo());
        dispositivoViewHolder.btnLocalizar.setContentDescription(dispositivos.get(i).getId()+"");
        dispositivoViewHolder.btnGaleria.setContentDescription(dispositivos.get(i).getId()+"");
        dispositivoViewHolder.btnEliminar.setContentDescription(dispositivos.get(i).getId()+"");
    }

    @Override
    public int getItemCount() {
        return dispositivos.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}