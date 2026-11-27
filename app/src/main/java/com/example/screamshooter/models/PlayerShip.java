package com.example.screamshooter.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.example.screamshooter.R;

public class PlayerShip {

    private Bitmap shipBitmap;
    private Bitmap skillBitmap;
    private Bitmap engineBitmap;
    private Bitmap engineEffectBitmap;

    private float x;
    private float y;

    private float width = 120;
    private float height = 120;

    private int maxHealth = 3;
    private int currentHealth = 3;

    // Animation tia lửa
    private final int FRAME_WIDTH = 48;
    private final int FRAME_HEIGHT = 48;
    private final int TOTAL_FRAMES = 3;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private final long FRAME_DELAY = 100;

    private Paint paint;

    public PlayerShip(Context context, float screenWidth, float screenHeight, int maxHealth) {
        this.width = screenWidth * 0.14f;
        this.height = this.width;

        this.maxHealth = Math.max(1, maxHealth);
        this.currentHealth = this.maxHealth;

        this.x = (screenWidth - width) / 2f;
        this.y = screenHeight - height - 120f;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(false);

        try {
            shipBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_ship);
            skillBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_ship_skill);
            engineBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.player_engine);
            engineEffectBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.engine_effect_idle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= FRAME_DELAY) {
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES;
            lastFrameTime = currentTime;
        }
    }

    public void draw(Canvas canvas, boolean isSkillActive) {
        // 1. Tia lửa động cơ
        if (engineEffectBitmap != null) {
            Rect effectSource = new Rect(
                    currentFrame * FRAME_WIDTH,
                    0,
                    (currentFrame + 1) * FRAME_WIDTH,
                    FRAME_HEIGHT
            );

            float effectW = width * 0.5f;
            float effectH = height * 0.5f;
            float effectX = x + (width - effectW) / 2f;
            float effectY = y + height * 0.75f;

            RectF effectDst = new RectF(effectX, effectY, effectX + effectW, effectY + effectH);
            canvas.drawBitmap(engineEffectBitmap, effectSource, effectDst, paint);
        }

        // 2. Động cơ
        if (engineBitmap != null) {
            float engineW = width * 0.45f;
            float engineH = height * 0.45f;
            float engineX = x + (width - engineW) / 2f;
            float engineY = y + height * 0.6f;

            RectF engineDst = new RectF(engineX, engineY, engineX + engineW, engineY + engineH);
            canvas.drawBitmap(engineBitmap, null, engineDst, paint);
        }

        // 3. Thân phi thuyền (Sử dụng player_ship_skill.png khi bật khiên kỹ năng, ngược lại dùng player_ship.png)
        Bitmap currentShipBitmap = (isSkillActive && skillBitmap != null) ? skillBitmap : shipBitmap;
        if (currentShipBitmap != null) {
            RectF shipDst = new RectF(x, y, x + width, y + height);
            canvas.drawBitmap(currentShipBitmap, null, shipDst, paint);
        } else {
            Paint shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shipPaint.setColor(Color.CYAN);
            canvas.drawRect(x, y, x + width, y + height, shipPaint);
        }

        // 4. Hiệu ứng vòng khiên bảo vệ (Shield Aura) khi skill kích hoạt
        if (isSkillActive) {
            Paint shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shieldPaint.setStyle(Paint.Style.STROKE);
            shieldPaint.setColor(Color.parseColor("#00E5FF")); // Xanh Cyan sáng
            shieldPaint.setStrokeWidth(8f);
            canvas.drawCircle(x + width / 2f, y + height / 2f, width * 0.75f, shieldPaint);
        }
    }

    public void clamp(float screenWidth) {
        if (x < 0) x = 0;
        if (x + width > screenWidth) x = screenWidth - width;
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public int getMaxHealth() { return maxHealth; }
    public void takeDamage(int amount) {
        this.currentHealth -= amount;
        if (this.currentHealth < 0) this.currentHealth = 0;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public void resetHealth() {
        this.currentHealth = maxHealth;
    }
}
