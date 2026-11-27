package com.example.screamshooter.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.screamshooter.R;

import java.util.Random;

public class Enemy {

    private float x;
    private float y;
    private float width;
    private float height;

    private float speedY;
    private final int scoreValue = 10;
    private int health = 1;

    private static Bitmap enemyBitmap;
    private Paint paint;
    private static final Random random = new Random();

    public Enemy(Context context, float screenWidth, float screenHeight) {
        if (enemyBitmap == null) {
            try {
                enemyBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy_ship);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.width = screenWidth * 0.12f;
        this.height = width;
        this.speedY = 7f + random.nextFloat() * 5f;

        this.x = random.nextFloat() * (screenWidth - width);
        this.y = -height;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(false);
    }

    public void update() {
        y += speedY;
    }

    public void draw(Canvas canvas) {
        if (enemyBitmap != null) {
            RectF rect = new RectF(x, y, x + width, y + height);
            canvas.drawBitmap(enemyBitmap, null, rect, paint);
        }
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
