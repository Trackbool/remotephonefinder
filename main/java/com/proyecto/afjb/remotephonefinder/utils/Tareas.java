package com.proyecto.afjb.remotephonefinder.utils;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.proyecto.afjb.remotephonefinder.R;
import com.proyecto.afjb.remotephonefinder.activities.MainActivity;
import com.proyecto.afjb.remotephonefinder.entidades.Accion;
import com.proyecto.afjb.remotephonefinder.servicios.ServicioEnviarImagen;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Tareas {
    private static final int ADMIN_INTENT = 15;
    private static final String description = "Permisos de administrador";
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private boolean isAdmin;
    private Ringtone r;

    Activity activity;

    public Tareas(Activity activity) {
        this.activity = activity;
        isAdmin = false;
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(activity, notification);
    }

    public void bloquear() {
        activarAdmin();
        isAdmin = mDevicePolicyManager.isAdminActive(mComponentName);
        if (isAdmin) {
            mDevicePolicyManager.lockNow();
        } else {
            Toast.makeText(activity, "No has activado los permisos de administrador", Toast.LENGTH_SHORT).show();
        }
    }

    public void hacerSonar(int ringDelay) {
        if (!r.isPlaying()) {
            r.play();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    r.stop();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, ringDelay);
        }
    }

    public void sacarFoto(Accion accion) {
        int index = getFrontCameraId();
        if (index == -1) {
            Toast.makeText(activity, "No se encontró cámara frontal", Toast.LENGTH_LONG).show();
        } else {
            try {
                Camera mCamera = Camera.open(index);
                try {
                    mCamera.setPreviewTexture(new SurfaceTexture(10));
                } catch (IOException e1) {
                    Log.d("IMAGENSTRING", "Entra");
                }

                Camera.Parameters params = mCamera.getParameters();
                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                Camera.Size size = sizes.get(0);
                for (int i = 0; i < sizes.size(); i++) {
                    if (sizes.get(i).width > size.width)
                        size = sizes.get(i);
                }
                params.setPictureSize(size.width, size.height);
                params.setRotation(270);
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                params.setPictureFormat(ImageFormat.JPEG);
                mCamera.setParameters(params);
                mCamera.startPreview();

                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    try {
                        mCamera.takePicture(
                                null, null, null, (data, camera) -> {
                                    Toast.makeText(activity, "Foto tomada", Toast.LENGTH_LONG).show();
                                    String imagenString = Base64.encodeToString(data, Base64.NO_WRAP);
                                    String urlCadena = activity.getString(R.string.servicio_insertar_imagen);
                                    new ServicioEnviarImagen(urlCadena, accion, imagenString).execute();

                                    camera.stopPreview();
                                    if (camera != null) {
                                        camera.release();
                                    }
                                });
                    } catch (RuntimeException e) {
                        Toast.makeText(activity, "Cámara no disponible", Toast.LENGTH_SHORT).show();
                    }
                }, 800);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "Cámara no disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getFrontCameraId() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) return i;
        }
        return -1;
    }

    private void activarAdmin() {
        if (!isAdmin) {
            mDevicePolicyManager = (DevicePolicyManager) activity.getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            mComponentName = new ComponentName(activity, MyAdminReceiver.class);

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, description);
            activity.startActivityForResult(intent, ADMIN_INTENT);
            isAdmin = true;
        }
    }

    private void desactivarAdmin() {
        if (isAdmin) {
            mDevicePolicyManager.removeActiveAdmin(mComponentName);
            isAdmin = false;
        }
    }
}
