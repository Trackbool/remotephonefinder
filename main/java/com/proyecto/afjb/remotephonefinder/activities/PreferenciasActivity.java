package com.proyecto.afjb.remotephonefinder.activities;

import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.proyecto.afjb.remotephonefinder.R;

public class PreferenciasActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_preferencias);
        addPreferencesFromResource(R.xml.preferencias);
    }
}
