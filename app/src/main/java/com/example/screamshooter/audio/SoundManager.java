package com.example.screamshooter.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.screamshooter.data.SettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class SoundManager {

    private static final int SAMPLE_RATE = 22050;
    private static final int MAX_STREAMS = 10;

    private final Context context;
    private final SettingsManager settingsManager;
    private final SoundPool soundPool;

    private final int shootSound;
    private final int explosionSound;
    private final int starSound;
    private final int skillSound;
    private final int gameOverSound;

    private volatile boolean released = false;

    public SoundManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.context = appContext != null ? appContext : context;
        this.settingsManager = new SettingsManager(this.context);

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attributes)
                .build();

        shootSound = load("shoot.wav", 0.20, 950, 650, 60, false, 1.0);
        explosionSound = load("explosion.wav", 0.40, 220, 80, 180, true, 2.2);
        starSound = load("star.wav", 0.28, 1200, 1900, 110, false, 1.2);
        skillSound = load("skill.wav", 0.45, 300, 1000, 420, false, 0.5);
        gameOverSound = load("game_over.wav", 0.38, 520, 70, 650, false, 1.0);
    }

    private int load(String fileName, double amplitude, double startFreq, double endFreq,
                     int durationMs, boolean noise, double decayPower) {
        File file = new File(context.getCacheDir(), fileName);

        if (!file.exists() || file.length() <= 44) {
            short[] samples = generateSamples(
                    amplitude, startFreq, endFreq, durationMs, noise, decayPower
            );
            writeWav(file, samples);
        }

        return soundPool.load(file.getAbsolutePath(), 1);
    }

    private short[] generateSamples(double amplitude, double startFreq, double endFreq,
                                    int durationMs, boolean noise, double decayPower) {
        int sampleCount = Math.max(1, SAMPLE_RATE * durationMs / 1000);
        short[] samples = new short[sampleCount];
        Random random = new Random();
        double phase = 0;

        for (int i = 0; i < sampleCount; i++) {
            double progress = (double) i / sampleCount;
            double frequency = startFreq + (endFreq - startFreq) * progress;
            phase += 2 * Math.PI * frequency / SAMPLE_RATE;

            double wave = noise
                    ? random.nextDouble() * 2 - 1
                    : Math.sin(phase);

            double fadeIn = Math.min(1.0, i / (SAMPLE_RATE * 0.005));
            double envelope = Math.pow(1.0 - progress, decayPower) * fadeIn;

            samples[i] = (short) (wave * envelope * amplitude * Short.MAX_VALUE);
        }

        return samples;
    }

    private void writeWav(File file, short[] samples) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeAscii(out, "RIFF");
            writeIntLE(out, 36 + samples.length * 2);
            writeAscii(out, "WAVE");
            writeAscii(out, "fmt ");
            writeIntLE(out, 16);
            writeShortLE(out, 1);
            writeShortLE(out, 1);
            writeIntLE(out, SAMPLE_RATE);
            writeIntLE(out, SAMPLE_RATE * 2);
            writeShortLE(out, 2);
            writeShortLE(out, 16);
            writeAscii(out, "data");
            writeIntLE(out, samples.length * 2);

            for (short sample : samples) {
                out.write(sample & 0xFF);
                out.write((sample >> 8) & 0xFF);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAscii(FileOutputStream out, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            out.write(value.charAt(i));
        }
    }

    private void writeIntLE(FileOutputStream out, int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
    }

    private void writeShortLE(FileOutputStream out, int value) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    private void play(int soundId, float rate) {
        if (released || soundId == 0) {
            return;
        }

        float volume = settingsManager.getVolume() / 100f;
        if (volume <= 0f) {
            return;
        }

        soundPool.play(soundId, volume, volume, 1, 0, rate);
    }

    public void playShoot() {
        play(shootSound, 0.98f + (float) Math.random() * 0.06f);
    }

    public void playExplosion() {
        play(explosionSound, 0.95f + (float) Math.random() * 0.1f);
    }

    public void playStar() {
        play(starSound, 1f);
    }

    public void playSkill() {
        play(skillSound, 1f);
    }

    public void playGameOver() {
        play(gameOverSound, 1f);
    }

    public void release() {
        if (released) {
            return;
        }
        released = true;
        soundPool.release();
    }
}
