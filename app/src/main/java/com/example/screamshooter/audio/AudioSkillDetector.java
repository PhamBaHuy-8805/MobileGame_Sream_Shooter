package com.example.screamshooter.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;

import com.example.screamshooter.utils.Constants;
import com.example.screamshooter.utils.PermissionUtils;

public class AudioSkillDetector {

    public interface OnSkillTriggerListener {
        void onSkillTriggered(float currentDb);
    }

    private static final int SAMPLE_RATE = 44100;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    private final Context context;
    private final OnSkillTriggerListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private float currentDb = 0f;

    public AudioSkillDetector(Context context, OnSkillTriggerListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public synchronized void startListening() {
        if (isRecording) return;

        if (!PermissionUtils.hasRecordAudioPermission(context)) {
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (bufferSize <= 0) return;

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return;
            }

            audioRecord.startRecording();
            isRecording = true;

            recordingThread = new Thread(() -> {
                short[] buffer = new short[bufferSize];
                while (isRecording && audioRecord != null) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        double sum = 0;
                        for (int i = 0; i < read; i++) {
                            sum += buffer[i] * buffer[i];
                        }
                        double rms = Math.sqrt(sum / read);

                        // Quy đổi biên độ RMS sang dB SPL xấp xỉ
                        if (rms > 0) {
                            currentDb = (float) (20 * Math.log10(rms) + 20); // Scale chuẩn dB
                        } else {
                            currentDb = 0;
                        }

                        // Kiểm tra vượt ngưỡng kích hoạt (default > 70 dB)
                        if (currentDb >= Constants.AUDIO_TRIGGER_THRESHOLD_DB) {
                            final float db = currentDb;
                            mainHandler.post(() -> {
                                if (listener != null) {
                                    listener.onSkillTriggered(db);
                                }
                            });
                        }
                    }

                    try {
                        Thread.sleep(80); // Lấy mẫu chu kỳ 80ms
                    } catch (InterruptedException ignored) {
                    }
                }
            });

            recordingThread.start();

        } catch (SecurityException | IllegalStateException e) {
            e.printStackTrace();
            stopListening();
        }
    }

    public synchronized void stopListening() {
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
            recordingThread = null;
        }
        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
    }

    public float getCurrentDb() {
        return currentDb;
    }

    public boolean isListening() {
        return isRecording;
    }
}
