package com.example.screamshooter.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.screamshooter.R;
import com.example.screamshooter.audio.AudioSkillDetector;
import com.example.screamshooter.audio.SoundManager;
import com.example.screamshooter.data.AchievementManager;
import com.example.screamshooter.data.FirebaseScoreRepository;
import com.example.screamshooter.data.UpgradeManager;
import com.example.screamshooter.models.Bullet;
import com.example.screamshooter.models.Enemy;
import com.example.screamshooter.models.ParticleEffect;
import com.example.screamshooter.models.PlayerShip;
import com.example.screamshooter.models.StarPickup;
import com.example.screamshooter.sensors.AccelerometerManager;
import com.example.screamshooter.utils.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine implements
        AccelerometerManager.OnTiltListener,
        AudioSkillDetector.OnSkillTriggerListener {

    public enum GameState {
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    public interface GameEngineListener {
        void onGameOver(int finalScore);
        void onEnergyFull();
        void onSkillActivated();
    }

    private final Context context;
    private final GameEngineListener listener;

    private final float screenWidth;
    private final float screenHeight;

    private PlayerShip playerShip;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<ParticleEffect> particles = new ArrayList<>();
    private final List<StarPickup> stars = new ArrayList<>();
    private final Random random = new Random();

    private int score = 0;
    private int energy = 0;
    private int starsCollected = 0;
    private volatile GameState gameState = GameState.RUNNING;

    private long lastAutoFireTime = 0;
    private long lastEnemySpawnTime = 0;
    private long enemySpawnInterval = 1200;

    private boolean isSkillActive = false;
    private long skillEndTime = 0;

    private final AccelerometerManager accelerometerManager;
    private final AudioSkillDetector audioSkillDetector;
    private final FirebaseScoreRepository scoreRepository;
    private final UpgradeManager upgradeManager;
    private final AchievementManager achievementManager;
    private final SoundManager soundManager;

    private final long autoFireIntervalMs;
    private final long skillDurationMs;
    private final float bulletSpeedMultiplier;
    private final float magnetRadius;

    private final Paint hudPaint;
    private final Paint energyBarPaint;

    public GameEngine(Context context, float screenWidth, float screenHeight,
                      GameEngineListener listener) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.listener = listener;

        this.upgradeManager = new UpgradeManager(context);
        this.achievementManager = new AchievementManager(context);
        this.scoreRepository = new FirebaseScoreRepository(context);
        this.soundManager = new SoundManager(context);

        this.autoFireIntervalMs = upgradeManager.getAutoFireIntervalMs();
        this.skillDurationMs = upgradeManager.getSkillDurationMs();
        this.bulletSpeedMultiplier = upgradeManager.getBulletSpeedMultiplier();
        this.magnetRadius = upgradeManager.getMagnetRadius();

        this.playerShip = new PlayerShip(
                context, screenWidth, screenHeight, upgradeManager.getMaxHealth());

        this.accelerometerManager = new AccelerometerManager(context, this);
        this.audioSkillDetector = new AudioSkillDetector(context, this);

        this.hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.hudPaint.setTextSize(40f);
        this.hudPaint.setColor(Color.WHITE);

        this.energyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        resetGame();
    }

    public void startSensors() {
        if (accelerometerManager != null) {
            accelerometerManager.start();
        }
        if (audioSkillDetector != null
                && energy >= Constants.MAX_ENERGY
                && !isSkillActive) {
            audioSkillDetector.startListening();
        }
    }

    public void stopSensors() {
        if (accelerometerManager != null) {
            accelerometerManager.stop();
        }
        if (audioSkillDetector != null) {
            audioSkillDetector.stopListening();
        }
    }

    public void release() {
        stopSensors();
        if (soundManager != null) {
            soundManager.release();
        }
    }

    public void resetGame() {
        score = 0;
        energy = 0;
        starsCollected = 0;
        isSkillActive = false;
        bullets.clear();
        enemies.clear();
        particles.clear();
        stars.clear();

        lastAutoFireTime = 0;
        lastEnemySpawnTime = 0;
        enemySpawnInterval = 1200;

        if (playerShip != null) {
            playerShip.resetHealth();
            playerShip.setX((screenWidth - playerShip.getWidth()) / 2f);
        }

        gameState = GameState.RUNNING;
    }

    @Override
    public void onTilt(float tiltX) {
        if (gameState != GameState.RUNNING || playerShip == null) {
            return;
        }

        float moveSpeed = 10f;
        float newX = playerShip.getX() - tiltX * moveSpeed;
        playerShip.setX(newX);
        playerShip.clamp(screenWidth);
    }

    @Override
    public void onSkillTriggered(float currentDb) {
        if (energy >= Constants.MAX_ENERGY && !isSkillActive) {
            activateSkill();
        }
    }

    public void activateSkill() {
        isSkillActive = true;
        skillEndTime = System.currentTimeMillis() + skillDurationMs;

        for (Enemy enemy : enemies) {
            score += enemy.getScoreValue();
            particles.add(new ParticleEffect(
                    enemy.getX() + enemy.getWidth() / 2f,
                    enemy.getY() + enemy.getHeight() / 2f,
                    15
            ));
            maybeDropStar(enemy);
        }
        enemies.clear();

        energy = 0;
        if (audioSkillDetector != null) {
            audioSkillDetector.stopListening();
        }

        soundManager.playSkill();

        if (listener != null) {
            listener.onSkillActivated();
        }
    }

    public void update() {
        if (gameState != GameState.RUNNING) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (isSkillActive && currentTime >= skillEndTime) {
            isSkillActive = false;
        }

        // 1. Cập nhật Player
        if (playerShip != null) {
            playerShip.update();
        }

        // 2. Tự động bắn đạn
        if (currentTime - lastAutoFireTime >= autoFireIntervalMs) {
            if (playerShip != null) {
                float bulletX = playerShip.getX() + playerShip.getWidth() / 2f;
                float bulletY = playerShip.getY();
                bullets.add(new Bullet(
                        bulletX,
                        bulletY,
                        isSkillActive,
                        bulletSpeedMultiplier
                ));
                lastAutoFireTime = currentTime;
                soundManager.playShoot();
            }
        }

        // 3. Sinh quái vật ngẫu nhiên
        if (currentTime - lastEnemySpawnTime >= enemySpawnInterval) {
            enemies.add(new Enemy(context, screenWidth, screenHeight));
            lastEnemySpawnTime = currentTime;

            if (enemySpawnInterval > 500) {
                enemySpawnInterval -= 2;
            }
        }

        // 4. Cập nhật Đạn
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();
            if (bullet.isOffScreen()) {
                bulletIterator.remove();
            }
        }

        // 5. Cập nhật Quái vật
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update();

            if (enemy.isOffScreen(screenHeight)) {
                enemyIterator.remove();
            }
        }

        // 6. Xử lý va chạm Đạn - Quái vật
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            RectF bulletBounds = bullet.getBounds();

            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                RectF enemyBounds = enemy.getBounds();

                if (RectF.intersects(bulletBounds, enemyBounds)) {
                    if (!bullet.isSkillBullet()) {
                        bullets.remove(i);
                    }

                    if (enemy.takeDamage(bullet.isSkillBullet() ? 3 : 1)) {
                        enemies.remove(j);
                        score += enemy.getScoreValue();
                        soundManager.playExplosion();

                        if (energy < Constants.MAX_ENERGY) {
                            energy += Constants.ENERGY_PER_KILL;
                            if (energy >= Constants.MAX_ENERGY) {
                                energy = Constants.MAX_ENERGY;
                                audioSkillDetector.startListening();
                                if (listener != null) {
                                    listener.onEnergyFull();
                                }
                            }
                        }

                        particles.add(new ParticleEffect(
                                enemy.getX() + enemy.getWidth() / 2f,
                                enemy.getY() + enemy.getHeight() / 2f,
                                10
                        ));
                        maybeDropStar(enemy);
                    }
                    break;
                }
            }
        }

        // 7. Cập nhật Sao và nhặt sao
        if (playerShip != null && !playerShip.isDead()) {
            float targetX = playerShip.getX() + playerShip.getWidth() / 2f;
            float targetY = playerShip.getY() + playerShip.getHeight() / 2f;
            RectF playerBounds = playerShip.getBounds();

            Iterator<StarPickup> starIterator = stars.iterator();
            while (starIterator.hasNext()) {
                StarPickup star = starIterator.next();
                star.update(targetX, targetY, magnetRadius);

                if (star.isOffScreen(screenHeight)) {
                    starIterator.remove();
                    continue;
                }

                if (RectF.intersects(star.getBounds(), playerBounds)) {
                    starIterator.remove();
                    starsCollected++;
                    score += Constants.STAR_SCORE_VALUE;
                    soundManager.playStar();
                    particles.add(new ParticleEffect(
                            star.getBounds().centerX(),
                            star.getBounds().centerY(),
                            6
                    ));
                }
            }
        }

        // 8. Xử lý va chạm Quái vật - Phi thuyền
        if (playerShip != null && !playerShip.isDead()) {
            RectF playerBounds = playerShip.getBounds();
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (RectF.intersects(playerBounds, enemy.getBounds())) {
                    particles.add(new ParticleEffect(
                            enemy.getX() + enemy.getWidth() / 2f,
                            enemy.getY() + enemy.getHeight() / 2f,
                            15
                    ));
                    enemies.remove(j);
                    soundManager.playExplosion();

                    if (isSkillActive) {
                        score += enemy.getScoreValue(); // Khiên bảo vệ tiêu diệt quái và cộng điểm
                    } else {
                        playerShip.takeDamage(1);
                        if (playerShip.isDead()) {
                            triggerGameOver();
                            break;
                        }
                    }
                }
            }
        }

        // 9. Cập nhật Hiệu ứng hạt
        Iterator<ParticleEffect> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            ParticleEffect pe = particleIterator.next();
            pe.update();
            if (pe.isDead()) {
                particleIterator.remove();
            }
        }
    }

    private void maybeDropStar(Enemy enemy) {
        if (random.nextFloat() < 0.35f) {
            stars.add(new StarPickup(
                    enemy.getX() + enemy.getWidth() / 2f,
                    enemy.getY() + enemy.getHeight() / 2f
            ));
        }
    }

    /**
     * Kết thúc lượt chơi hiện tại (ví dụ khi người chơi chọn về trang chủ),
     * ghi nhận điểm/thành tích rồi thông báo để mở màn tổng kết.
     */
    public synchronized void finishRun() {
        if (gameState == GameState.GAME_OVER) {
            return;
        }
        triggerGameOver();
    }

    private synchronized void triggerGameOver() {
        if (gameState == GameState.GAME_OVER) {
            return;
        }

        gameState = GameState.GAME_OVER;
        stopSensors();
        soundManager.playGameOver();

        String playerName = context.getString(R.string.default_player_name);
        achievementManager.recordRun(score, starsCollected);
        scoreRepository.submitScore(playerName, score);

        if (listener != null) {
            listener.onGameOver(score);
        }
    }

    public void draw(Canvas canvas) {
        for (Bullet bullet : bullets) {
            bullet.draw(canvas);
        }

        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }

        for (StarPickup star : stars) {
            star.draw(canvas);
        }

        if (playerShip != null) {
            playerShip.draw(canvas, isSkillActive);
        }

        for (ParticleEffect pe : particles) {
            pe.draw(canvas);
        }

        if (isSkillActive) {
            Paint skillOverlayPaint = new Paint();
            skillOverlayPaint.setColor(Color.parseColor("#3300FFFF"));
            canvas.drawRect(0, 0, screenWidth, screenHeight, skillOverlayPaint);

            hudPaint.setColor(Color.CYAN);
            hudPaint.setTextSize(48f);
            hudPaint.setFakeBoldText(true);
            canvas.drawText(
                    context.getString(R.string.hud_skill_active),
                    screenWidth / 2f - 300f,
                    180f,
                    hudPaint
            );
        }

        drawHUD(canvas);
    }

    private void drawHUD(Canvas canvas) {
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(42f);
        hudPaint.setFakeBoldText(true);
        canvas.drawText(
                context.getString(R.string.hud_score, score),
                40f, 70f, hudPaint
        );

        if (playerShip != null) {
            hudPaint.setColor(Color.RED);
            StringBuilder hpStr = new StringBuilder(context.getString(R.string.hud_hp));
            for (int i = 0; i < playerShip.getCurrentHealth(); i++) {
                hpStr.append("❤️ ");
            }
            canvas.drawText(hpStr.toString(), 40f, 120f, hudPaint);
        }

        hudPaint.setColor(Color.parseColor("#FFD700"));
        hudPaint.setTextSize(34f);
        canvas.drawText(
                context.getString(R.string.hud_stars, starsCollected),
                40f, 170f, hudPaint
        );

        float barWidth = 300f;
        float barHeight = 30f;
        float barLeft = screenWidth - barWidth - 40f;
        float barTop = 40f;

        energyBarPaint.setStyle(Paint.Style.STROKE);
        energyBarPaint.setColor(Color.WHITE);
        energyBarPaint.setStrokeWidth(4f);
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight,
                energyBarPaint);

        energyBarPaint.setStyle(Paint.Style.FILL);
        if (energy >= Constants.MAX_ENERGY) {
            energyBarPaint.setColor(Color.YELLOW);
        } else {
            energyBarPaint.setColor(Color.parseColor("#00CCFF"));
        }
        float currentBarWidth = (energy / (float) Constants.MAX_ENERGY) * barWidth;
        canvas.drawRect(barLeft, barTop, barLeft + currentBarWidth,
                barTop + barHeight, energyBarPaint);

        hudPaint.setTextSize(28f);
        if (energy >= Constants.MAX_ENERGY) {
            hudPaint.setColor(Color.YELLOW);
            canvas.drawText(
                    context.getString(R.string.hud_skill_ready),
                    barLeft - 220f,
                    barTop + barHeight + 35f,
                    hudPaint
            );
        } else {
            hudPaint.setColor(Color.LTGRAY);
            canvas.drawText(
                    context.getString(R.string.hud_energy, energy),
                    barLeft,
                    barTop + barHeight + 35f,
                    hudPaint
            );
        }
    }

    public void pauseGame() {
        if (gameState == GameState.RUNNING) {
            gameState = GameState.PAUSED;
            stopSensors();
        }
    }

    public void resumeGame() {
        if (gameState == GameState.PAUSED) {
            gameState = GameState.RUNNING;
            startSensors();
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getScore() {
        return score;
    }

    public int getStarsCollected() {
        return starsCollected;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isSkillActive() {
        return isSkillActive;
    }
}
