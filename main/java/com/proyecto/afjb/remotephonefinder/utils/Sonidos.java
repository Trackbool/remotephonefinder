package com.proyecto.afjb.remotephonefinder.utils;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class Sonidos {
    Activity activity;
    SoundPool soundPool;
    MediaPlayer mediaPlayer;
    HashMap<String,Integer> sonidos;

    public Sonidos(Activity activity){
        this.activity = activity;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attributes)
                .build();
        sonidos = new HashMap<>();
    }

    public void anyadirSonido(String nombre, int sonido){
        if(!sonidos.containsKey(nombre)) {
            Integer carga = soundPool.load(activity, sonido, 1);
            sonidos.put(nombre, carga);
        }
    }

    public void reproducirSonido(String nombre, int loop){
        if(sonidos.containsKey(nombre)) {
            soundPool.play(sonidos.get(nombre), 1, 1, 1, loop, 1);
        }
    }
    public void cancelarSonido(String nombre){
        if(sonidos.containsKey(nombre)) {
            Integer sonido = sonidos.get(nombre);
            if(sonido != null)
                soundPool.stop(sonido);
        }
    }
}
