package com.example.screamshooter.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.WindowManager;

public class SensorHelper implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final OnTiltListener listener;
    private final WindowManager windowManager;

    public interface OnTiltListener {
        void onTilt(float tiltX);
    }

    public SensorHelper(Context context, OnTiltListener listener) {

        this.listener = listener;

        sensorManager = (SensorManager)
                context.getSystemService(Context.SENSOR_SERVICE);

        windowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
            );
        } else {
            accelerometer = null;
        }
    }

    public void register() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }

    public void unregister() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];

        float tiltX;

        int rotation = Surface.ROTATION_0;

        if (windowManager != null) {
            rotation = windowManager
                    .getDefaultDisplay()
                    .getRotation();
        }

        switch (rotation) {

            case Surface.ROTATION_90:
                tiltX = -y;
                break;

            case Surface.ROTATION_270:
                tiltX = y;
                break;

            case Surface.ROTATION_180:
                tiltX = -x;
                break;

            default:
                tiltX = x;
                break;
        }

        if (listener != null) {
            listener.onTilt(tiltX);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}