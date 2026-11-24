package com.example.screamshooter.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleEffect {

    private static class Particle {
        float x, y;
        float vx, vy;
        float radius;
        int alpha;
        int color;

        Particle(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            Random r = new Random();
            double angle = r.nextDouble() * Math.PI * 2;
            float speed = 3f + r.nextFloat() * 8f;
            this.vx = (float) (Math.cos(angle) * speed);
            this.vy = (float) (Math.sin(angle) * speed);
            this.radius = 4f + r.nextFloat() * 6f;
            this.alpha = 255;
        }

        void update() {
            x += vx;
            y += vy;
            alpha -= 12;
            if (alpha < 0) alpha = 0;
        }

        boolean isDead() {
            return alpha <= 0;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ParticleEffect(float x, float y, int count) {
        int[] colors = {
                Color.parseColor("#FF4444"),
                Color.parseColor("#FFBB33"),
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#00EEEE")
        };
        Random r = new Random();
        for (int i = 0; i < count; i++) {
            int color = colors[r.nextInt(colors.length)];
            particles.add(new Particle(x, y, color));
        }
    }

    public void update() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }

    public void draw(Canvas canvas) {
        for (Particle p : particles) {
            paint.setColor(p.color);
            paint.setAlpha(p.alpha);
            canvas.drawCircle(p.x, p.y, p.radius, paint);
        }
    }

    public boolean isDead() {
        return particles.isEmpty();
    }
}
