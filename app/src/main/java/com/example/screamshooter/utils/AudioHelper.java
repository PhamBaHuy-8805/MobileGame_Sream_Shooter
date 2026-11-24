package com.example.screamshooter.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.core.app.ActivityCompat;

public class AudioHelper {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private OnScreamListener listener;
    private static final double SCREAM_THRESHOLD_DB = 70.0; // Ngưỡng kích hoạt Skill (>70dB)

    public interface OnScreamListener {
        void onScreamDetected(double decibel);
    }

    public AudioHelper(Context context, OnScreamListener listener) {
        this.listener = listener;
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
        }
    }

    public void startListening() {
        if (audioRecord == null) return;
        try {
            audioRecord.startRecording();
            isRecording = true;
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int bufferSize = 1024;
                    short[] buffer = new short[bufferSize];
                    while (isRecording) {
                        int readSize = audioRecord.read(buffer, 0, bufferSize);
                        if (readSize > 0) {
                            double sum = 0;
                            for (int i = 0; i < readSize; i++) {
                                sum += buffer[i] * buffer[i];
                            }
                            double amplitude = Math.sqrt(sum / readSize);
                            double db = 20 * Math.log10(amplitude);
                            if (db > 0 && !Double.isInfinite(db) && !Double.isNaN(db)) {
                                if (db >= SCREAM_THRESHOLD_DB && listener != null) {
                                    listener.onScreamDetected(db);
                                }
                            }
                        }
                        try {
                            Thread.sleep(100); // Lấy mẫu định kỳ tránh quá tải CPU
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            recordingThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopListening() {
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}