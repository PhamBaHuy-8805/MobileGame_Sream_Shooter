package com.example.screamshooter.ui.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.screamshooter.R;
import com.example.screamshooter.data.model.PlayerProfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameView extends SurfaceView
        implements Runnable {

    private Thread gameThread;

    private volatile boolean isPlaying = false;

    private volatile float tiltX = 0;

    private final AtomicBoolean skillRequested =
            new AtomicBoolean(false);

    private final AtomicBoolean gameEnded =
            new AtomicBoolean(false);

    private final SurfaceHolder surfaceHolder;

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Context context;

    private final PlayerProfile profile;

    private final String matchOwnerUid;

    private final String matchId =
            UUID.randomUUID().toString();

    private final Random random = new Random();

    private float playerX;
    private float playerY;

    private float screenWidth;
    private float screenHeight;

    private float shipSize;
    private float enemySize;
    private float bulletWidth;
    private float bulletHeight;

    private int score = 0;
    private int energy = 0;
    private int kills = 0;
    private int lives;

    private long earnedCoins = 0;

    private final ArrayList<Bullet> bullets =
            new ArrayList<>();

    private final ArrayList<Enemy> enemies =
            new ArrayList<>();

    private long lastEnemySpawnTime = 0;
    private long lastBulletTime = 0;

    private final Bitmap playerBitmap;
    private final Bitmap enemyBitmap;
    private final Bitmap bulletBitmap;
    private final Bitmap bgBitmap;
    private final Bitmap skillBitmap;

    private boolean isSkillActive = false;

    private int skillEffectFrames = 0;

    private static final int TOTAL_SKILL_FRAMES = 35;

    public GameView(
            Context context,
            float screenWidth,
            float screenHeight,
            PlayerProfile profile,
            String matchOwnerUid
    ) {

        super(context);

        this.context = context;
        this.profile = profile;
        this.matchOwnerUid = matchOwnerUid;

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.lives = profile.getStartingLives();

        surfaceHolder = getHolder();

        playerBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.myship
        );

        enemyBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.enemyship
        );

        bulletBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.mybeam
        );

        bgBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.background
        );

        skillBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.skill
        );

        updateScreenDimensions(
                screenWidth,
                screenHeight
        );
    }

    private void updateScreenDimensions(
            float width,
            float height
    ) {

        screenWidth = width;
        screenHeight = height;

        shipSize = height * 0.15f;

        enemySize = height * 0.12f;

        bulletWidth = height * 0.025f;

        bulletHeight = height * 0.06f;

        playerX = width / 2f;

        playerY = height - shipSize * 0.75f;
    }

    @Override
    protected void onSizeChanged(
            int w,
            int h,
            int oldw,
            int oldh
    ) {

        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {

            updateScreenDimensions(w, h);
        }
    }

    @Override
    public void run() {

        long previousTime =
                SystemClock.elapsedRealtime();

        while (isPlaying && !gameEnded.get()) {

            long frameStart =
                    SystemClock.elapsedRealtime();

            float deltaTime = Math.min(
                    (frameStart - previousTime) / 1000f,
                    0.05f
            );

            previousTime = frameStart;

            if (surfaceHolder.getSurface().isValid()) {

                update(deltaTime);

                renderGame();
            }

            long frameTime =
                    SystemClock.elapsedRealtime()
                            - frameStart;

            long sleepTime =
                    Math.max(1, 16 - frameTime);

            try {

                Thread.sleep(sleepTime);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                break;
            }
        }
    }

    private void update(float deltaTime) {

        if (gameEnded.get()) {
            return;
        }

        long now = SystemClock.elapsedRealtime();

        // Di chuyển nhân vật.

        playerX -= tiltX
                * screenHeight
                * 0.12f
                * profile.getMovementMultiplier()
                * deltaTime;

        float halfShip = shipSize / 2f;

        playerX = Math.max(
                halfShip,
                Math.min(
                        screenWidth - halfShip,
                        playerX
                )
        );

        // Tốc độ bắn phụ thuộc cấp nâng cấp.

        if (now - lastBulletTime >=
                profile.getFireInterval()) {

            bullets.add(
                    new Bullet(
                            playerX,
                            playerY - halfShip
                    )
            );

            lastBulletTime = now;
        }

        // Sinh kẻ địch.

        if (now - lastEnemySpawnTime >= 1000) {

            float halfEnemy = enemySize / 2f;

            float availableWidth = Math.max(
                    0,
                    screenWidth - enemySize
            );

            float enemyX = halfEnemy
                    + random.nextFloat()
                    * availableWidth;

            enemies.add(
                    new Enemy(
                            enemyX,
                            -halfEnemy
                    )
            );

            lastEnemySpawnTime = now;
        }

        // Cập nhật đạn.

        Iterator<Bullet> bulletIterator =
                bullets.iterator();

        while (bulletIterator.hasNext()) {

            Bullet bullet = bulletIterator.next();

            bullet.y -= screenHeight
                    * 1.2f
                    * deltaTime;

            if (bullet.y < -bulletHeight) {

                bulletIterator.remove();
            }
        }

        // Cập nhật kẻ địch.

        Iterator<Enemy> enemyIterator =
                enemies.iterator();

        while (enemyIterator.hasNext()) {

            Enemy enemy = enemyIterator.next();

            enemy.y += screenHeight
                    * 0.4f
                    * deltaTime;

            if (enemy.y > screenHeight) {

                enemyIterator.remove();

                lives--;

                if (lives <= 0) {

                    lives = 0;

                    triggerGameOver();

                    return;
                }
            }
        }

        // Kiểm tra đạn trúng kẻ địch.

        bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {

            Bullet bullet = bulletIterator.next();

            Iterator<Enemy> eIterator =
                    enemies.iterator();

            while (eIterator.hasNext()) {

                Enemy enemy = eIterator.next();

                boolean hitX =
                        Math.abs(bullet.x - enemy.x)
                                < (enemySize + bulletWidth)
                                / 2f;

                boolean hitY =
                        Math.abs(bullet.y - enemy.y)
                                < (enemySize + bulletHeight)
                                / 2f;

                if (hitX && hitY) {

                    bulletIterator.remove();

                    eIterator.remove();

                    rewardKill(true);

                    break;
                }
            }
        }

        // Xử lý yêu cầu kích hoạt kỹ năng
        // trên chính game thread.

        if (skillRequested.getAndSet(false)) {

            activateSkill();
        }

        if (isSkillActive) {

            skillEffectFrames--;

            if (skillEffectFrames <= 0) {

                isSkillActive = false;
            }
        }
    }

    private void rewardKill(boolean chargeEnergy) {

        kills++;

        score += 100;

        earnedCoins += profile.getCoinsPerKill();

        if (chargeEnergy) {

            energy = Math.min(
                    100,
                    energy + profile.getEnergyPerKill()
            );
        }
    }

    private void activateSkill() {

        if (energy < 100 || isSkillActive) {
            return;
        }

        energy = 0;

        // Giữ phần thưởng kỹ năng gốc.

        score += 1000;

        // Mỗi kẻ địch bị kỹ năng tiêu diệt
        // đều được tính điểm và Coin.

        for (int i = 0; i < enemies.size(); i++) {

            rewardKill(false);
        }

        enemies.clear();

        isSkillActive = true;

        skillEffectFrames = TOTAL_SKILL_FRAMES;
    }

    private void renderGame() {

        if (!surfaceHolder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = null;

        try {

            canvas = surfaceHolder.lockCanvas();

            if (canvas == null) {
                return;
            }

            canvas.drawColor(Color.BLACK);

            drawBitmap(
                    canvas,
                    bgBitmap,
                    0,
                    0,
                    screenWidth,
                    screenHeight
            );

            for (Bullet bullet : bullets) {

                drawBitmap(
                        canvas,
                        bulletBitmap,
                        bullet.x - bulletWidth / 2f,
                        bullet.y - bulletHeight / 2f,
                        bulletWidth,
                        bulletHeight
                );
            }

            for (Enemy enemy : enemies) {

                drawBitmap(
                        canvas,
                        enemyBitmap,
                        enemy.x - enemySize / 2f,
                        enemy.y - enemySize / 2f,
                        enemySize,
                        enemySize
                );
            }

            drawBitmap(
                    canvas,
                    playerBitmap,
                    playerX - shipSize / 2f,
                    playerY - shipSize / 2f,
                    shipSize,
                    shipSize
            );

            if (isSkillActive) {

                drawSkill(canvas);
            }

            drawHud(canvas);

        } finally {

            if (canvas != null) {

                surfaceHolder.unlockCanvasAndPost(
                        canvas
                );
            }
        }
    }

    private void drawBitmap(
            Canvas canvas,
            Bitmap bitmap,
            float x,
            float y,
            float width,
            float height
    ) {

        if (bitmap == null ||
                width <= 0 ||
                height <= 0) {

            return;
        }

        RectF destination = new RectF(
                x,
                y,
                x + width,
                y + height
        );

        canvas.drawBitmap(
                bitmap,
                null,
                destination,
                null
        );
    }

    private void drawSkill(Canvas canvas) {

        float progress = 1f
                - (float) skillEffectFrames
                / TOTAL_SKILL_FRAMES;

        float size = Math.max(
                screenWidth,
                screenHeight
        ) * progress * 1.5f;

        drawBitmap(
                canvas,
                skillBitmap,
                (screenWidth - size) / 2f,
                (screenHeight - size) / 2f,
                size,
                size
        );

        paint.setColor(Color.YELLOW);

        paint.setTextSize(
                screenHeight * 0.06f
        );

        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(
                context.getString(R.string.ultimate_skill),
                screenWidth / 2f,
                screenHeight * 0.35f,
                paint
        );

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawHud(Canvas canvas) {

        float textSize = screenHeight * 0.045f;

        float margin = screenHeight * 0.035f;

        float lineHeight = textSize * 1.5f;

        paint.setTextSize(textSize);

        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(Color.WHITE);

        canvas.drawText(
                context.getString(R.string.score, score),
                margin,
                margin + textSize,
                paint
        );

        paint.setColor(Color.rgb(255, 71, 87));

        canvas.drawText(
                context.getString(R.string.lives, lives),
                margin,
                margin + textSize + lineHeight,
                paint
        );

        paint.setColor(Color.rgb(255, 215, 0));

        canvas.drawText(
                context.getString(
                        R.string.match_coins,
                        earnedCoins
                ),
                margin,
                margin + textSize + lineHeight * 2,
                paint
        );

        paint.setColor(Color.CYAN);

        canvas.drawText(
                context.getString(
                        R.string.energy,
                        energy
                ),
                margin,
                margin + textSize + lineHeight * 3,
                paint
        );

        paint.setTextAlign(Paint.Align.RIGHT);

        paint.setColor(Color.WHITE);

        canvas.drawText(
                context.getString(
                        R.string.match_kills,
                        kills
                ),
                screenWidth - margin,
                margin + textSize,
                paint
        );

        paint.setTextAlign(Paint.Align.LEFT);
    }

    public synchronized void resume() {

        if (isPlaying || gameEnded.get()) {
            return;
        }

        isPlaying = true;

        gameThread = new Thread(this);

        gameThread.start();
    }

    public synchronized void pause() {

        isPlaying = false;

        if (gameThread != null) {

            try {

                gameThread.join();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

            gameThread = null;
        }
    }

    public void updatePlayerPosition(float tiltX) {

        this.tiltX = tiltX;
    }

    public void triggerSkill() {

        skillRequested.set(true);
    }

    private void triggerGameOver() {

        if (!gameEnded.compareAndSet(false, true)) {
            return;
        }

        isPlaying = false;

        Intent intent = new Intent(
                context,
                GameOverActivity.class
        );

        intent.putExtra("FINAL_SCORE", score);

        intent.putExtra("FINAL_KILLS", kills);

        intent.putExtra("EARNED_COINS", earnedCoins);

        intent.putExtra("MATCH_ID", matchId);

        intent.putExtra("MATCH_OWNER_UID", matchOwnerUid);

        if (context instanceof Activity) {

            Activity activity = (Activity) context;

            activity.runOnUiThread(() -> {

                if (!activity.isFinishing()) {

                    activity.startActivity(intent);

                    activity.finish();
                }
            });
        }
    }

    private static class Bullet {

        float x, y;

        Bullet(float x, float y) {

            this.x = x;
            this.y = y;
        }
    }

    private static class Enemy {

        float x, y;

        Enemy(float x, float y) {

            this.x = x;
            this.y = y;
        }
    }
}