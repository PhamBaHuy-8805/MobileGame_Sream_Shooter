package com.example.screamshooter.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.screamshooter.R;
import com.example.screamshooter.game.GameEngine;
import com.example.screamshooter.utils.Constants;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private boolean isPlaying = false;
    private SurfaceHolder holder;

    private Bitmap backgroundBitmap;
    private Paint paint;
    private GameEngine gameEngine;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        holder = getHolder();
        holder.addCallback(this);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(false);

        try {
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.space_background);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopLoop();
    }

    public void startLoop() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopLoop() {
        isPlaying = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameThread = null;
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            long startTime = System.currentTimeMillis();

            if (gameEngine != null) {
                gameEngine.update();
                drawFrame();
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            long sleepTime = Constants.FRAME_TIME_MS - timeTaken;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void drawFrame() {
        if (!holder.getSurface().isValid()) return;

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            try {
                // 1. Vẽ Background
                if (backgroundBitmap != null) {
                    canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), paint);
                } else {
                    canvas.drawColor(android.graphics.Color.BLACK);
                }

                // 2. Vẽ Game Engine Entities
                if (gameEngine != null) {
                    gameEngine.draw(canvas);
                }

            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
