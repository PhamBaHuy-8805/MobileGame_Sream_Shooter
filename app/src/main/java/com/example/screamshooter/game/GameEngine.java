package com.example.screamshooter.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.screamshooter.audio.AudioSkillDetector;
import com.example.screamshooter.data.FirebaseScoreRepository;
import com.example.screamshooter.models.Bullet;
import com.example.screamshooter.models.Enemy;
import com.example.screamshooter.models.ParticleEffect;
import com.example.screamshooter.models.PlayerShip;
import com.example.screamshooter.sensors.AccelerometerManager;
import com.example.screamshooter.utils.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameEngine implements AccelerometerManager.OnTiltListener, AudioSkillDetector.OnSkillTriggerListener {

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

    private float screenWidth;
    private float screenHeight;

    private PlayerShip playerShip;
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<ParticleEffect> particles = new ArrayList<>();

    private int score = 0;
    private int energy = 0;
    private GameState gameState = GameState.RUNNING;

    private long lastAutoFireTime = 0;
    private long lastEnemySpawnTime = 0;
    private long enemySpawnInterval = 1200; // ms

    // Trạng thái Skill
    private boolean isSkillActive = false;
    private long skillEndTime = 0;

    private final AccelerometerManager accelerometerManager;
    private final AudioSkillDetector audioSkillDetector;
    private final FirebaseScoreRepository scoreRepository;

    private Paint hudPaint;
    private Paint energyBarPaint;

    public GameEngine(Context context, float screenWidth, float screenHeight, GameEngineListener listener) {
        this.context = context;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.listener = listener;

        this.playerShip = new PlayerShip(context, screenWidth, screenHeight);
        this.scoreRepository = new FirebaseScoreRepository(context);

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
    }

    public void stopSensors() {
        if (accelerometerManager != null) {
            accelerometerManager.stop();
        }
        if (audioSkillDetector != null) {
            audioSkillDetector.stopListening();
        }
    }

    public void resetGame() {
        score = 0;
        energy = 0;
        isSkillActive = false;
        bullets.clear();
        enemies.clear();
        particles.clear();
        if (playerShip != null) {
            playerShip.resetHealth();
            playerShip.setX((screenWidth - playerShip.getWidth()) / 2f);
        }
        gameState = GameState.RUNNING;
        startSensors();
    }

    @Override
    public void onTilt(float tiltX) {
        if (gameState != GameState.RUNNING || playerShip == null) return;
        // TiltX > 0 -> nghiêng trái -> sang trái
        // TiltX < 0 -> nghiêng phải -> sang phải
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
        skillEndTime = System.currentTimeMillis() + Constants.SKILL_DURATION_MS;

        // Quét sạch toàn bộ quái hiện tại trên màn hình
        for (Enemy enemy : enemies) {
            score += enemy.getScoreValue();
            particles.add(new ParticleEffect(enemy.getX() + enemy.getWidth() / 2f, enemy.getY() + enemy.getHeight() / 2f, 15));
        }
        enemies.clear();

        // Reset Năng lượng
        energy = 0;
        if (audioSkillDetector != null) {
            audioSkillDetector.stopListening();
        }

        if (listener != null) {
            listener.onSkillActivated();
        }
    }

    public void update() {
        if (gameState != GameState.RUNNING) return;

        long currentTime = System.currentTimeMillis();

        // Kiểm tra hết thời gian Skill
        if (isSkillActive && currentTime >= skillEndTime) {
            isSkillActive = false;
        }

        // 1. Cập nhật Player
        if (playerShip != null) {
            playerShip.update();
        }

        // 2. Tự động bắn đạn (Auto-fire continuous)
        if (currentTime - lastAutoFireTime >= Constants.AUTO_FIRE_INTERVAL_MS) {
            if (playerShip != null) {
                float bulletX = playerShip.getX() + playerShip.getWidth() / 2f;
                float bulletY = playerShip.getY();
                bullets.add(new Bullet(bulletX, bulletY, isSkillActive));
                lastAutoFireTime = currentTime;
            }
        }

        // 3. Sinh quái vật ngẫu nhiên
        if (currentTime - lastEnemySpawnTime >= enemySpawnInterval) {
            enemies.add(new Enemy(screenWidth, screenHeight));
            lastEnemySpawnTime = currentTime;
            // Tăng dần độ khó theo thời gian
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

            // Quái trôi khỏi đáy màn hình
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
                    // Xóa đạn (nếu không phải đạn Skill xuyên phá)
                    if (!bullet.isSkillBullet()) {
                        bullets.remove(i);
                    }

                    // Gây sát thương quái
                    if (enemy.takeDamage(bullet.isSkillBullet() ? 3 : 1)) {
                        enemies.remove(j);
                        score += enemy.getScoreValue();

                        // Tích lũy năng lượng
                        if (energy < Constants.MAX_ENERGY) {
                            energy += Constants.ENERGY_PER_KILL;
                            if (energy >= Constants.MAX_ENERGY) {
                                energy = Constants.MAX_ENERGY;
                                // Mở mic lắng nghe tiếng hét kích hoạt Skill
                                audioSkillDetector.startListening();
                                if (listener != null) {
                                    listener.onEnergyFull();
                                }
                            }
                        }

                        // Hiệu ứng nổ hạt
                        particles.add(new ParticleEffect(enemy.getX() + enemy.getWidth() / 2f, enemy.getY() + enemy.getHeight() / 2f, 10));
                    }
                    break;
                }
            }
        }

        // 7. Xử lý va chạm Quái vật - Phi thuyền
        if (playerShip != null && !playerShip.isDead()) {
            RectF playerBounds = playerShip.getBounds();
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (RectF.intersects(playerBounds, enemy.getBounds())) {
                    particles.add(new ParticleEffect(enemy.getX() + enemy.getWidth() / 2f, enemy.getY() + enemy.getHeight() / 2f, 15));
                    enemies.remove(j);
                    playerShip.takeDamage(1);

                    if (playerShip.isDead()) {
                        triggerGameOver();
                        break;
                    }
                }
            }
        }

        // 8. Cập nhật Hiệu ứng hạt
        Iterator<ParticleEffect> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            ParticleEffect pe = particleIterator.next();
            pe.update();
            if (pe.isDead()) {
                particleIterator.remove();
            }
        }
    }

    private void triggerGameOver() {
        gameState = GameState.GAME_OVER;
        stopSensors();
        scoreRepository.submitScore("Phi Thuyền X", score);
        if (listener != null) {
            listener.onGameOver(score);
        }
    }

    public void draw(Canvas canvas) {
        // 1. Vẽ Đạn
        for (Bullet bullet : bullets) {
            bullet.draw(canvas);
        }

        // 2. Vẽ Quái vật
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }

        // 3. Vẽ Phi thuyền
        if (playerShip != null) {
            playerShip.draw(canvas);
        }

        // 4. Vẽ Hiệu ứng nổ hạt
        for (ParticleEffect pe : particles) {
            pe.draw(canvas);
        }

        // 5. Vẽ Hiệu ứng Skill đặc biệt
        if (isSkillActive) {
            Paint skillOverlayPaint = new Paint();
            skillOverlayPaint.setColor(Color.parseColor("#3300FFFF"));
            canvas.drawRect(0, 0, screenWidth, screenHeight, skillOverlayPaint);

            hudPaint.setColor(Color.CYAN);
            hudPaint.setTextSize(48f);
            hudPaint.setFakeBoldText(true);
            canvas.drawText("⚡ SUPER SCREAM SKILL ACTIVE ⚡", screenWidth / 2f - 300f, 180f, hudPaint);
        }

        // 6. Vẽ HUD (Điểm số, Máu, Thanh Năng lượng)
        drawHUD(canvas);
    }

    private void drawHUD(Canvas canvas) {
        // Điểm số
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(42f);
        hudPaint.setFakeBoldText(true);
        canvas.drawText("SCORE: " + score, 40f, 70f, hudPaint);

        // Máu (HP)
        if (playerShip != null) {
            hudPaint.setColor(Color.RED);
            StringBuilder hpStr = new StringBuilder("HP: ");
            for (int i = 0; i < playerShip.getCurrentHealth(); i++) {
                hpStr.append("❤️ ");
            }
            canvas.drawText(hpStr.toString(), 40f, 120f, hudPaint);
        }

        // Thanh Năng Lượng (Energy Bar)
        float barWidth = 300f;
        float barHeight = 30f;
        float barLeft = screenWidth - barWidth - 40f;
        float barTop = 40f;

        // Khung viền thanh năng lượng
        energyBarPaint.setStyle(Paint.Style.STROKE);
        energyBarPaint.setColor(Color.WHITE);
        energyBarPaint.setStrokeWidth(4f);
        canvas.drawRect(barLeft, barTop, barLeft + barWidth, barTop + barHeight, energyBarPaint);

        // Phần năng lượng tích lũy
        energyBarPaint.setStyle(Paint.Style.FILL);
        if (energy >= Constants.MAX_ENERGY) {
            energyBarPaint.setColor(Color.YELLOW);
        } else {
            energyBarPaint.setColor(Color.parseColor("#00CCFF"));
        }
        float currentBarWidth = (energy / (float) Constants.MAX_ENERGY) * barWidth;
        canvas.drawRect(barLeft, barTop, barLeft + currentBarWidth, barTop + barHeight, energyBarPaint);

        // Text trạng thái Skill
        hudPaint.setTextSize(28f);
        if (energy >= Constants.MAX_ENERGY) {
            hudPaint.setColor(Color.YELLOW);
            canvas.drawText("⚡ HÉT/LỚN TIẾNG ĐỂ KÍCH SKILL! (>70dB)", barLeft - 220f, barTop + barHeight + 35f, hudPaint);
        } else {
            hudPaint.setColor(Color.LTGRAY);
            canvas.drawText("NĂNG LƯỢNG: " + energy + "%", barLeft, barTop + barHeight + 35f, hudPaint);
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

    public GameState getGameState() { return gameState; }
    public int getScore() { return score; }
    public int getEnergy() { return energy; }
    public boolean isSkillActive() { return isSkillActive; }
}
