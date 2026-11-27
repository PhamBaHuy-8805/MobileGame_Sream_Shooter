package com.example.screamshooter.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class StarPickup {

    private float x;
    private float y;

    private final float radius = 18f;
    private final float fallSpeed = 2.8f;

    private final Paint paint;
    private final Path path;

    public StarPickup(float x, float y) {
        this.x = x;
        this.y = y;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FFD700"));

        path = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float currentRadius = (i % 2 == 0) ? radius : radius * 0.45f;
            float px = (float) (Math.cos(angle) * currentRadius);
            float py = (float) (Math.sin(angle) * currentRadius);

            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.close();
    }

    public void update(float targetX, float targetY, float magnetRadius) {
        if (magnetRadius > 0) {
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 1f && distance < magnetRadius) {
                float pull = 10f;
                x += dx / distance * pull;
                y += dy / distance * pull;
                return;
            }
        }

        y += fallSpeed;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(x, y);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    public boolean isOffScreen(float screenHeight) {
        return y - radius > screenHeight;
    }

    public RectF getBounds() {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }
}
