package com.proyecto.afjb.remotephonefinder.adaptadores;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Dispositivo;
import com.proyecto.afjb.remotephonefinder.entidades.InstruccionesEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RVAdaptadorAcciones extends RecyclerView.Adapter<RVAdaptadorAcciones.AccionViewHolder>{

    Context context;
    int idUsuario;
    List<Dispositivo> dispositivos;

    public RVAdaptadorAcciones(Context context, int idUsuario, List<Dispositivo> dispositivos) {
        this.context = context;
        this.idUsuario = idUsuario;
        this.dispositivos = dispositivos;
    }

    public class AccionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        List<Dispositivo> dispositivos;
        CardView cv;
        TextView nombreDispositivo;
        TextView modeloDispositivo;
        ImageView btnRing;
        ImageView btnFoto;
        ImageView btnBloquear;

        AccionViewHolder(View itemView, List<Dispositivo> dispositivos) {
            super(itemView);
            this.dispositivos = dispositivos;
            cv = (CardView)itemView.findViewById(R.id.cv);
            nombreDispositivo = itemView.findViewById(R.id.nombre_dispositivo);
            modeloDispositivo = itemView.findViewById(R.id.modelo_dispositivo);
            btnRing = itemView.findViewById(R.id.btnRing);
            btnFoto = itemView.findViewById(R.id.btnFoto);
            btnBloquear = itemView.findViewById(R.id.btnBloquear);

            btnRing.setOnClickListener(this);
            btnFoto.setOnClickListener(this);
            btnBloquear.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String urlCadena = context.getString(R.string.servicio_insertar_acciones);
            Servicio servicio;
            switch (v.getId()) {
                case R.id.btnRing:
                    servicio = new Servicio(urlCadena,InstruccionesEnum.HACER_SONAR,idUsuario,0,
                            Integer.parseInt(btnRing.getContentDescription()+""));
                    servicio.execute();
                    break;
                case R.id.btnFoto:
                    servicio = new Servicio(urlCadena,InstruccionesEnum.SACAR_FOTO,idUsuario,0,
                            Integer.parseInt(btnFoto.getContentDescription()+""));
                    servicio.execute();
                    break;
                case R.id.btnBloquear:
                    servicio = new Servicio(urlCadena,InstruccionesEnum.BLOQUEAR,idUsuario,0,
                            Integer.parseInt(btnBloquear.getContentDescription()+""));
                    servicio.execute();
                    break;
            }
        }

        public class Servicio extends AsyncTask<String,Void,Integer> {
            String urlCadena;
            int idAccion;
            int idUsuario;
            int idDispositivoOrigen;
            int idDispositivoDestino;

            public Servicio(String urlCadena, int idAccion,int idUsuario,
                            int idDispositivoOrigen, int idDispositivoDestino) {
                this.urlCadena = urlCadena;
                this.idAccion = idAccion;
                this.idUsuario = idUsuario;
                this.idDispositivoOrigen = idDispositivoOrigen;
                this.idDispositivoDestino = idDispositivoDestino;
            }

            @Override
            protected void onPostExecute(Integer resultado) {
                if(resultado == 1)
                    Toast.makeText(context, "Orden enviada con éxito", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "Error al enviar la orden", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Integer doInBackground(String... strings) {
                Integer resultado = -1;
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                final int TIME_OUT = 3500;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                        .build();

                try{
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("id_accion", idAccion);
                    jsonParam.put("id_usuario", idUsuario);
                    jsonParam.put("disp_origen", idDispositivoOrigen);
                    jsonParam.put("disp_destino", idDispositivoDestino);

                    RequestBody body = RequestBody.create(JSON, jsonParam.toString());
                    Request request = new Request.Builder()
                            .url(urlCadena)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    JSONObject respuestaJSON = new JSONObject(response.body().string());

                    int resultJSON = respuestaJSON.getInt("estado");

                    resultado = resultJSON;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return resultado;
            }
        }
    }

    @NonNull
    @Override
    public AccionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_dispositivo_acciones,
                viewGroup, false);
        return new AccionViewHolder(v,dispositivos);
    }

    @Override
    public void onBindViewHolder(@NonNull AccionViewHolder dispositivoViewHolder, int i) {
        dispositivoViewHolder.nombreDispositivo.setText(dispositivos.get(i).getNombre());
        dispositivoViewHolder.modeloDispositivo.setText(dispositivos.get(i).getModelo());
        dispositivoViewHolder.btnRing.setContentDescription(dispositivos.get(i).getId()+"");
        dispositivoViewHolder.btnFoto.setContentDescription(dispositivos.get(i).getId()+"");
        dispositivoViewHolder.btnBloquear.setContentDescription(dispositivos.get(i).getId()+"");
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