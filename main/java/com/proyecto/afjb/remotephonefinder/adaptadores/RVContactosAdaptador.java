package com.proyecto.afjb.remotephonefinder.adaptadores;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.entidades.Contacto;

import java.util.List;

public class RVContactosAdaptador extends RecyclerView.Adapter<RVContactosAdaptador.ContactosViewHolder> {

    List<Contacto> contactos;

    public RVContactosAdaptador(List<Contacto> contactos) {
        this.contactos = contactos;
    }

    public static class ContactosViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView nombre;
        TextView telefono;

        ContactosViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            nombre = itemView.findViewById(R.id.nombre);
            telefono = itemView.findViewById(R.id.telefono);
        }
    }

    @NonNull
    @Override
    public ContactosViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_contactos,
                viewGroup, false);
        return new ContactosViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactosViewHolder contactosViewHolder, int i) {
        contactosViewHolder.nombre.setText(contactos.get(i).getNombre());
        contactosViewHolder.telefono.setText(contactos.get(i).getTelefono());
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}