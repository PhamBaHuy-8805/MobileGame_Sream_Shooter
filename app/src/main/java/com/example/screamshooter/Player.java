package com.example.screamshooter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Player {

    private Bitmap shipBitmap;
    private Bitmap engineBitmap;
    private Bitmap engineEffectBitmap;

    private float x;
    private float y;

    // Kích thước thân tàu
    private final float shipWidth = 100;
    private final float shipHeight = 100;

    // Animation tia lửa
    private final int FRAME_WIDTH = 48;
    private final int FRAME_HEIGHT = 48;
    private final int TOTAL_FRAMES = 3;

    private int currentFrame = 0;

    private long lastFrameTime = 0;
    private final long FRAME_DELAY = 100;

    private Paint paint;

    public Player(Context context, float x, float y) {

        this.x = x;
        this.y = y;

        // Thân tàu
        shipBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.player_ship
        );

        // Động cơ
        engineBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.player_engine
        );

        // Tia lửa
        engineEffectBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.engine_effect_idle
        );

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Giữ pixel art sắc nét
        paint.setFilterBitmap(false);
    }

    // =========================
    // UPDATE
    // =========================

    public void update() {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFrameTime >= FRAME_DELAY) {

            currentFrame++;

            if (currentFrame >= TOTAL_FRAMES) {
                currentFrame = 0;
            }

            lastFrameTime = currentTime;
        }
    }

    // =========================
    // DRAW
    // =========================

    public void draw(Canvas canvas) {

        // --------------------------------
        // 1. TIA LỬA - nằm phía sau
        // --------------------------------

        Rect effectSource = new Rect(
                currentFrame * FRAME_WIDTH,
                0,
                (currentFrame + 1) * FRAME_WIDTH,
                FRAME_HEIGHT
        );

        // Tia lửa nằm phía dưới động cơ
        float effectWidth = 55;
        float effectHeight = 55;

        float effectX = x + (shipWidth - effectWidth) / 2f;
        float effectY = y + 78;

        RectF effectDestination = new RectF(
                effectX,
                effectY,
                effectX + effectWidth,
                effectY + effectHeight
        );

        canvas.drawBitmap(
                engineEffectBitmap,
                effectSource,
                effectDestination,
                paint
        );


        // --------------------------------
        // 2. ĐỘNG CƠ
        // --------------------------------

        float engineWidth = 45;
        float engineHeight = 45;

        float engineX = x + (shipWidth - engineWidth) / 2f;
        float engineY = y + 65;

        RectF engineDestination = new RectF(
                engineX,
                engineY,
                engineX + engineWidth,
                engineY + engineHeight
        );

        canvas.drawBitmap(
                engineBitmap,
                null,
                engineDestination,
                paint
        );


        // --------------------------------
        // 3. THÂN TÀU
        // --------------------------------

        RectF shipDestination = new RectF(
                x,
                y,
                x + shipWidth,
                y + shipHeight
        );

        canvas.drawBitmap(
                shipBitmap,
                null,
                shipDestination,
                paint
        );
    }

    // =========================
    // GET / SET
    // =========================

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}