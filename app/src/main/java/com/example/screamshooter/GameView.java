
package com.example.screamshooter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.View;

public class GameView extends View implements SensorEventListener {

    private Bitmap background;
    private Paint paint;
    private Player player;

    // =========================
    // SENSOR
    // =========================

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    // Tốc độ di chuyển thông thường
    private float moveSpeed = 8f;

    // =========================
    // GYROSCOPE - NÉ NHANH
    // =========================

    // Ngưỡng tốc độ xoay (rad/s)
    private static final float GYRO_THRESHOLD = 2.0f;

    // Khoảng thời gian hồi chiêu (ms)
    private static final long DODGE_COOLDOWN = 1200;

    // Thời gian hiệu lực né (ms)
    private static final long DODGE_DURATION = 250;

    private long lastDodgeTime = 0;
    private long dodgeEndTime = 0;

    // Hướng né: -1 = trái, 1 = phải
    private int dodgeDirection = 0;

    // Tốc độ né
    private float dodgeSpeed = 22f;

    public GameView(Context context) {
        super(context);

        paint = new Paint();
        paint.setFilterBitmap(false);

        // Background
        background = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.space_background
        );

        // Khởi tạo SensorManager
        sensorManager = (SensorManager)
                context.getSystemService(Context.SENSOR_SERVICE);

        // Accelerometer
        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
        );

        // Gyroscope
        gyroscope = sensorManager.getDefaultSensor(
                Sensor.TYPE_GYROSCOPE
        );

        // Đăng ký Accelerometer
        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }

        // Đăng ký Gyroscope
        if (gyroscope != null) {
            sensorManager.registerListener(
                    this,
                    gyroscope,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Vẽ background
        if (background != null) {
            canvas.drawBitmap(
                    background,
                    null,
                    new Rect(0, 0, getWidth(), getHeight()),
                    paint
            );
        }

        // Khởi tạo Player
        if (player == null) {
            float playerX = getWidth() / 2f - 50;
            float playerY = getHeight() - 180;

            player = new Player(
                    getContext(),
                    playerX,
                    playerY
            );
        }

        // Cập nhật trạng thái né
        updateDodge();

        // Animation động cơ
        player.update();

        // Vẽ tàu
        player.draw(canvas);

        // Vẽ frame tiếp theo
        postInvalidateOnAnimation();
    }

    // =========================
    // SENSOR EVENT
    // =========================

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (player == null) {
            return;
        }

        int sensorType = event.sensor.getType();

        // =========================
        // 1. ACCELEROMETER
        // =========================

        if (sensorType == Sensor.TYPE_ACCELEROMETER) {

            float tiltX = event.values[0];

            /*
             * Giữ cách điều khiển hiện tại:
             * tiltX > 0 -> sang trái
             * tiltX < 0 -> sang phải
             */

            float newX = player.getX() - tiltX * moveSpeed;

            newX = clampX(newX);
            player.setX(newX);
        }

        // =========================
        // 2. GYROSCOPE
        // =========================

        else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            // Tốc độ xoay quanh trục Z
            float rotationZ = event.values[2];

            long currentTime = SystemClock.elapsedRealtime();

            // Chỉ kích hoạt khi chưa né và đã hết hồi chiêu
            if (Math.abs(rotationZ) >= GYRO_THRESHOLD
                    && currentTime - lastDodgeTime >= DODGE_COOLDOWN
                    && currentTime >= dodgeEndTime) {

                // Đổi dấu nếu hướng né bị ngược trên máy thật
                dodgeDirection = rotationZ > 0 ? 1 : -1;

                lastDodgeTime = currentTime;
                dodgeEndTime = currentTime + DODGE_DURATION;
            }
        }
    }

    // =========================
    // CẬP NHẬT NÉ NHANH
    // =========================

    private void updateDodge() {

        if (player == null || dodgeDirection == 0) {
            return;
        }

        long currentTime = SystemClock.elapsedRealtime();

        if (currentTime < dodgeEndTime) {

            float newX = player.getX()
                    + dodgeDirection * dodgeSpeed;

            player.setX(clampX(newX));

        } else {
            dodgeDirection = 0;
        }
    }

    // =========================
    // GIỚI HẠN VỊ TRÍ
    // =========================

    private float clampX(float x) {
        float minX = 0;
        float maxX = Math.max(0, getWidth() - 100);

        if (x < minX) {
            return minX;
        }

        if (x > maxX) {
            return maxX;
        }

        return x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // =========================
    // GIẢI PHÓNG SENSOR
    // =========================

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
