package com.example.screamshooter.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Bullet {

    private float x;
    private float y;
    private float width;
    private float height;
    private float speedY;
    private boolean isSkillBullet;

    private Paint paint;

    public Bullet(float startX, float startY, boolean isSkillBullet) {
        this.x = startX;
        this.y = startY;
        this.isSkillBullet = isSkillBullet;

        if (isSkillBullet) {
            this.width = 16f;
            this.height = 48f;
            this.speedY = -35f;
        } else {
            this.width = 10f;
            this.height = 30f;
            this.speedY = -24f;
        }

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void update() {
        y += speedY;
    }

    public void draw(Canvas canvas) {
        if (isSkillBullet) {
            paint.setColor(Color.parseColor("#00EEEE")); // Cyan rực rỡ
        } else {
            paint.setColor(Color.parseColor("#FFD700")); // Vàng kim
        }

        RectF rect = new RectF(x - width / 2f, y, x + width / 2f, y + height);
        canvas.drawRoundRect(rect, 8, 8, paint);
    }

    public boolean isOffScreen() {
        return y + height < 0;
    }

    public RectF getBounds() {
        return new RectF(x - width / 2f, y, x + width / 2f, y + height);
    }

    public boolean isSkillBullet() {
        return isSkillBullet;
    }
}
