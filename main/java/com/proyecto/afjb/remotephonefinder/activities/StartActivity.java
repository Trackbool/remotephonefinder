package com.proyecto.afjb.remotephonefinder.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.proyecto.afjb.remotephonefinder.R;

public class StartActivity extends AppCompatActivity {

    ImageView imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.raw.start_image);
        imagen = findViewById(R.id.imageView);
        imagen.setImageBitmap(bmp);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }

        }, 1400);

    }
}
