package com.example.screamshooter.models;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

public class Enemy {

    private float x;
    private float y;
    private float width;
    private float height;

    private float speedY;
    private int type; // 1 = Thường, 2 = Nhanh, 3 = Trâu
    private int scoreValue;
    private int health;

    private Paint paint;
    private static final Random random = new Random();

    public Enemy(float screenWidth, float screenHeight) {
        // Ngẫu nhiên loại quái
        int rand = random.nextInt(100);
        if (rand < 60) {
            this.type = 1; // 60% Quái thường
            this.width = screenWidth * 0.12f;
            this.height = width;
            this.speedY = 6f + random.nextFloat() * 4f;
            this.scoreValue = 10;
            this.health = 1;
        } else if (rand < 85) {
            this.type = 2; // 25% Quái nhanh
            this.width = screenWidth * 0.09f;
            this.height = width;
            this.speedY = 12f + random.nextFloat() * 6f;
            this.scoreValue = 20;
            this.health = 1;
        } else {
            this.type = 3; // 15% Quái trâu
            this.width = screenWidth * 0.16f;
            this.height = width;
            this.speedY = 4f + random.nextFloat() * 3f;
            this.scoreValue = 50;
            this.health = 3;
        }

        this.x = random.nextFloat() * (screenWidth - width);
        this.y = -height;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void update() {
        y += speedY;
    }

    public void draw(Canvas canvas) {
        // Vẽ đồ họa quái vật tùy theo loại
        switch (type) {
            case 1:
                paint.setColor(Color.parseColor("#FF4444")); // Đỏ
                break;
            case 2:
                paint.setColor(Color.parseColor("#FFBB33")); // Vàng cam
                break;
            case 3:
                paint.setColor(Color.parseColor("#AA66CC")); // Tím
                break;
        }

        RectF rect = new RectF(x, y, x + width, y + height);
        canvas.drawRoundRect(rect, 16, 16, paint);

        // Vẽ hiệu ứng mắt quái vật
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.WHITE);
        float eyeRadius = width * 0.15f;
        canvas.drawCircle(x + width * 0.3f, y + height * 0.4f, eyeRadius, eyePaint);
        canvas.drawCircle(x + width * 0.7f, y + height * 0.4f, eyeRadius, eyePaint);

        eyePaint.setColor(Color.BLACK);
        canvas.drawCircle(x + width * 0.3f, y + height * 0.4f, eyeRadius * 0.5f, eyePaint);
        canvas.drawCircle(x + width * 0.7f, y + height * 0.4f, eyeRadius * 0.5f, eyePaint);
    }

    public boolean isOffScreen(float screenHeight) {
        return y > screenHeight;
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public boolean takeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
