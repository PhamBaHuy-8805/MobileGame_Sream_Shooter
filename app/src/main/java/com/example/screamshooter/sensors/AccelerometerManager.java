package com.example.screamshooter.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerManager implements SensorEventListener {

    public interface OnTiltListener {
        void onTilt(float tiltX);
    }

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private OnTiltListener listener;

    private float filteredTiltX = 0f;
    private static final float ALPHA = 0.2f; // Low-pass filter coefficient for smoothing

    public AccelerometerManager(Context context, OnTiltListener listener) {
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public void start() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float rawTiltX = event.values[0]; // Trục X gia tốc

            // Lọc mượt tránh rung lắc
            filteredTiltX = filteredTiltX + ALPHA * (rawTiltX - filteredTiltX);

            if (listener != null) {
                listener.onTilt(filteredTiltX);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
