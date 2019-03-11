package com.proyecto.afjb.remotephonefinder.adaptadores;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.proyecto.afjb.remotephonefinder.R;

import java.util.List;

public class RVAdaptadorImagenes extends RecyclerView.Adapter<RVAdaptadorImagenes.ImagenViewHolder>{

    List<String> imagenes;

    public RVAdaptadorImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public class ImagenViewHolder extends RecyclerView.ViewHolder{
        List<String> imagenes;
        CardView cv;
        ImageView imageViewFoto;

        ImagenViewHolder(View itemView, List<String> imagenes) {
            super(itemView);
            this.imagenes = imagenes;
            cv = (CardView) itemView.findViewById(R.id.cv);
            imageViewFoto = itemView.findViewById(R.id.foto);
        }
    }

    @NonNull
    @Override
    public ImagenViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_imagenes,
                viewGroup, false);
        return new ImagenViewHolder(v,imagenes);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagenViewHolder imagenViewHolder, int i) {
        byte[] decodedString = Base64.decode(imagenes.get(i), Base64.DEFAULT);
        Bitmap imagen = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imagenViewHolder.imageViewFoto.setImageBitmap(imagen);
    }

    @Override
    public int getItemCount() {
        return imagenes.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}