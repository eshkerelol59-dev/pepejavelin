package tech.javelin.client.modules.impl.combat.aura.rotation;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.RaytracingUtil;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;
import tech.javelin.utility.math.MultipointUtils;

/**
 * Intpol — гибрид: velocity-движок SpookyTime (скорость рождается из физики
 * мыши: разгон, торможение, инерция, связка осей) + человеческий слой старой
 * Intpol (плавающая точка, латентность, недоводы, дизеринг, усталость)
 * + флинч-реакция, профиль на цель и микро-осмотр в стойке.
 */
public class IntpolRotation implements RotationAura {

    // ==================== ПЕРЕКЛЮЧАТЕЛИ СЛОЁВ (диагностика флагов на альте) ====================
    // Если прилетел флаг — гаси слои по одному и тестируй, так находится виновник.
    private static final boolean LAYER_IMPULSE = true;     // замирание прицела перед ударом (200-250 мс)
    private static final boolean LAYER_AIM_LAG = true;     // EMA-запаздывание аима (~200 мс реакции)
    private static final boolean LAYER_JITTER = true;      // микро-джитер и подёргивания
    private static final boolean LAYER_FLINCH = true;      // рывок прицела при получении урона
    private static final boolean LAYER_LOOK_AROUND = true; // осмотр хитбокса стоячей цели
    private static final boolean LAYER_DRIFT = true;       // усталость + wobble-шум
    private static final boolean LAYER_PROFILE = true;     // свой «почерк» на каждую цель

    // ==================== СОСТОЯНИЕ ====================
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Random profileRng = new Random();
    private long sessionSalt = ThreadLocalRandom.current().nextLong();

    // движок
    private float yawVelocity;
    private float pitchVelocity;
    private float maxSpeed = 170.0f;
    private float easingAngle = 44.0f;
    private float acceleration = 10.0f;
    private float yawPitchCoupling = 0.0f;
    private float deadzone = 0.6f;
    private long lastRandomizeTime = 0L;
    private long randomizeInterval = 0L;
    private long startTime = 0L;
    private long alignedSince = 0L;
    private float alignedFactor = 0.0f;

    // импульс (замирание перед ударом)
    private float attackImpulse = 0.0f;
    private float impulseHoldYaw = 0.0f;
    private float impulseHoldPitch = 0.0f;
    private long impulseTime = 0L;
    private long holdDurationMillis = 0L;

    // человеческий слой
    private int lastTargetId = -1;
    private double aimFracX = 0.5;
    private double aimFracY = 0.7;
    private double aimFracZ = 0.5;
    private boolean needPickAim = true;
    private Vec3d lastAimPoint = null;
    private Vec3d smoothAim = null;
    private int reactionTicks = 0;
    private float errYaw = 0.0f;
    private float errPitch = 0.0f;
    private int errTicks = 0;
    private int nextErrChange = 20;
    private float twitchYaw = 0.0f;
    private float twitchPitch = 0.0f;
    private long tick = 0L;
    private float residualYaw = 0.0f;
    private float residualPitch = 0.0f;
    private long nextLookAroundTime = 0L;

    // усталость / сессия
    private float drift = 1.0f;
    private float driftTarget = 1.0f;
    private long nextDriftTime = 0L;
    private float seedA = 0.0f;
    private float seedB = 0.0f;
    private float yawBias = 1.0f;

    // флинч
    private int lastHurtTime = 0;
    private int flinchTicks = 0;
    private float flinchErrYaw = 0.0f;
    private float flinchErrPitch = 0.0f;
    private boolean flinchImpulsePending = false;

    // профиль на цель
    private float profileSpeedMul = 1.0f;
    private float profileLatencyMul = 1.0f;
    private float profileJitterMul = 1.0f;

    public IntpolRotation() {
        this.reseedSession();
    }

    // ==================== ГЛАВНЫЙ ЦИКЛ ====================

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || mc.world == null || target == null) {
            return;
        }
        long now = System.currentTimeMillis();
        ++this.tick;
        if (this.startTime == 0L) {
            this.startTime = now;
        }

        int targetId = target.getId();
        if (targetId != this.lastTargetId) {
            this.lastTargetId = targetId;
            this.onTargetChanged(target);
        }

        // === ДВИЖОК ===
        this.randomizeEngineParams(now);
        boolean impulseTriggered = this.updateImpulse(aura, target, ranges, canAttack, now);

        // === ЧЕЛОВЕЧЕСКИЙ СЛОЙ ===
        this.updateHumanizationState(now);
        if (this.reactionTicks > 0) {
            // латентность реакции на смену цели: прицел стоит на месте
            --this.reactionTicks;
            return;
        }

        float lastYaw = aura.lastYaw != 0.0f ? aura.lastYaw : mc.player.getYaw();
        float lastPitch = aura.lastPitch != 0.0f ? aura.lastPitch : mc.player.getPitch();

        Vec3d aimPoint = this.updateAimPoint(target, lastYaw, lastPitch, now);

        // свежие углы к точке считаем ДО заморозки: в тик срабатывания импульса
        // они сохраняются как точка замирания (иначе прицел улетает в мусорные нули)
        Rotation fresh = this.freshAngle(aimPoint);
        float flinchFactor = this.flinchTicks > 0 ? 1.0f : 0.0f;
        float freshYaw = fresh.getYaw() + this.errYaw + this.flinchErrYaw * flinchFactor;
        float freshPitch = MathHelper.clamp(fresh.getPitch() + this.errPitch + this.flinchErrPitch * flinchFactor, -89.5f, 89.5f);

        if (impulseTriggered) {
            this.impulseHoldYaw = freshYaw;
            this.impulseHoldPitch = freshPitch;
            this.needPickAim = true; // новая точка удара на следующий заход
        }

        boolean frozen = LAYER_IMPULSE && this.impulseTime > 0L && now - this.impulseTime < this.holdDurationMillis;
        float heldYaw = frozen ? this.impulseHoldYaw : freshYaw;
        float heldPitch = frozen ? this.impulseHoldPitch : freshPitch;

        this.updateAlignedFactor(target, ranges, now);
        this.integrateVelocities(aura, target, lastYaw, lastPitch, heldYaw, heldPitch, impulseTriggered);

        // === ВЫВОД ===
        this.emit(aura, lastYaw, lastPitch, frozen);
    }

    // ==================== ДВИЖОК (velocity-физика SpookyTime) ====================

    /** Профиль ускорения наводки постоянно различается: параметры движка перескакивают каждые 25-50 мс. */
    private void randomizeEngineParams(long now) {
        if (now - this.lastRandomizeTime > this.randomizeInterval) {
            this.lastRandomizeTime = now;
            this.randomizeInterval = (long) this.randomFloat(25.0f, 50.0f);
            this.maxSpeed = this.randomFloat(142.0f, 200.0f);
            this.easingAngle = this.randomFloat(34.0f, 54.0f);
            this.acceleration = this.randomFloat(7.0f, 14.0f);
            this.yawPitchCoupling = this.randomFloat(0.04f, 0.12f) * (this.random.nextBoolean() ? 1.0f : -1.0f);
            this.deadzone = this.randomFloat(0.4f, 0.9f);
        }
    }

    /** Импульс: при готовности атаки прицел замирает на цели на 200-250 мс — как у человека перед кликом. */
    private boolean updateImpulse(Aura aura, LivingEntity target, float[] ranges, boolean canAttack, long now) {
        if (canAttack) {
            double dist = mc.player.getEyePos().distanceTo(MultipointUtils.getNearestPoint(target, (double) ranges[0]));
            if (dist < (double) ranges[0]) {
                this.attackImpulse = this.randomFloat(1.9f, 2.3f);
                this.impulseTime = now;
                this.holdDurationMillis = (long) this.random.nextDouble(200.0, 250.0);
            }
        }

        if (this.attackImpulse > this.randomFloat(0.43f, 0.75f)) {
            this.attackImpulse = this.randomFloat(0.13f, 0.25f);
            return true;
        }
        return false;
    }

    /** Прицел уже на цели дольше 250 мс — скорость плавно гасится, чтобы не дёргаться по хитбоксу. */
    private void updateAlignedFactor(LivingEntity target, float[] ranges, long now) {
        if (RaytracingUtil.rayTrace(mc.player.getRotationVector(), (double) ranges[0] + 1.0, target.getBoundingBox())) {
            if (this.alignedSince == 0L) {
                this.alignedSince = now;
            }
        } else {
            this.alignedSince = 0L;
        }

        float targetFactor = MathHelper.clamp(((float) (this.alignedSince == 0L ? 0L : now - this.alignedSince) - 250.0f) / 300.0f, 0.0f, 1.0f);
        if (targetFactor >= this.alignedFactor) {
            this.alignedFactor = targetFactor;
        } else {
            this.alignedFactor = this.alignedFactor + (targetFactor - this.alignedFactor) * 0.2f;
        }
    }

    private void integrateVelocities(Aura aura, LivingEntity target, float lastYaw, float lastPitch,
                                     float heldYaw, float heldPitch, boolean impulseTriggered) {
        boolean airborneMace = aura.maceCrits.isEnabled()
                && !mc.player.isOnGround()
                && mc.player.getMainHandStack().isOf(Items.MACE);

        float yawError = MathHelper.wrapDegrees(heldYaw - lastYaw);
        float pitchError = heldPitch - lastPitch;
        float angularDistance = (float) Math.hypot(yawError, pitchError);
        boolean overlapsPlayer = target.getBoundingBox().intersects(mc.player.getBoundingBox());

        float rampUp = MathHelper.clamp((float) (System.currentTimeMillis() - this.startTime) / 220.0f, 0.0f, 1.0f);
        float speed = this.maxSpeed
                * MathHelper.clamp(angularDistance / this.easingAngle, 0.0f, 1.0f)
                * MathHelper.lerp(rampUp, 0.78f, 1.0f)
                * (1.0f - this.alignedFactor * (airborneMace ? 0.35f : 1.0f))
                * (impulseTriggered ? 1.25f : 1.0f)
                * (airborneMace ? 3.2f : 1.0f)
                * (mc.player.handSwingTicks > 0 && !airborneMace ? 0.5f : 1.0f)
                * this.humanSpeedMul();

        float accel = this.acceleration * (impulseTriggered ? 4.0f : 1.0f) * (airborneMace ? 2.6f : 1.0f);

        // асимметрия сторон: в «удобную» сторону поворот чуть быстрее (сид профиля цели)
        float yawAccel = accel * 1.2f * (yawError > 0.0f ? this.yawBias : (2.0f - this.yawBias));
        this.yawVelocity += MathHelper.clamp(
                speed * (angularDistance < 0.001f ? 0.0f : yawError / angularDistance) * (overlapsPlayer ? 0.5f : 1.0f) - this.yawVelocity,
                -yawAccel, yawAccel);

        // вниз целимся чуть медленнее, чем вверх; связка осей даёт диагональные довороты
        float pitchAccel = accel * 0.68f;
        this.pitchVelocity += MathHelper.clamp(
                speed * (pitchError < 0.0f ? 0.93f : 1.0f) * (angularDistance < 0.001f ? 0.0f : pitchError / angularDistance)
                        + this.yawVelocity * this.yawPitchCoupling - this.pitchVelocity,
                -pitchAccel, pitchAccel);

        // мягкие края: у ±90° скорость движения к краю плавно затухает
        float absPitch = Math.abs(lastPitch);
        if (absPitch > 80.0f && (lastPitch > 0.0f) == (pitchError > 0.0f)) {
            this.pitchVelocity *= MathHelper.clamp((89.5f - absPitch) / 9.5f, 0.05f, 1.0f);
        }

        this.yawVelocity = MathHelper.clamp(this.yawVelocity, -Math.abs(yawError), Math.abs(yawError));
        this.pitchVelocity = MathHelper.clamp(this.pitchVelocity,
                -Math.abs(pitchError) - Math.abs(this.yawVelocity * this.yawPitchCoupling),
                Math.abs(pitchError) + Math.abs(this.yawVelocity * this.yawPitchCoupling));

        if (angularDistance < this.deadzone) {
            this.yawVelocity = 0.0f;
            this.pitchVelocity = 0.0f;
        }

        // флинч: в момент получения урона прицел дёргает вверх (knockback-реакция)
        if (LAYER_FLINCH && this.flinchImpulsePending) {
            this.pitchVelocity -= 3.0f + this.random.nextFloat() * 3.0f;
            this.flinchImpulsePending = false;
        }
    }

    // ==================== ЧЕЛОВЕЧЕСКИЙ СЛОЙ ====================

    private void updateHumanizationState(long now) {
        // усталость: новая целевая скорость сессии каждые 8-30 с, дрейф к ней плавный
        if (LAYER_DRIFT && now >= this.nextDriftTime) {
            this.nextDriftTime = now + 8000L + (long) this.random.nextLong(22000L);
            this.driftTarget = 0.85f + this.random.nextFloat() * 0.3f;
        }
        this.drift += (this.driftTarget - this.drift) * 0.02f;

        // медленная ошибка прицеливания: недовод/перевод на 15-40 тиков
        if (++this.errTicks >= this.nextErrChange) {
            this.errTicks = 0;
            this.nextErrChange = 15 + this.random.nextInt(25);
            this.errYaw = (this.random.nextFloat() - 0.5f) * 2.2f;
            this.errPitch = (this.random.nextFloat() - 0.5f) * 1.1f;
        }

        // флинч: детект получения урона по rising edge hurtTime
        int hurt = mc.player.hurtTime;
        if (LAYER_FLINCH && hurt > 7 && this.lastHurtTime <= 7) {
            this.flinchTicks = 2 + this.random.nextInt(3);
            this.flinchErrYaw = (this.random.nextFloat() - 0.5f) * 2.0f;
            this.flinchErrPitch = (this.random.nextFloat() - 0.5f) * 1.0f;
            this.flinchImpulsePending = true;
        }
        this.lastHurtTime = hurt;
        if (this.flinchTicks > 0) {
            --this.flinchTicks;
        }

        // редкие затухающие подёргивания
        this.twitchYaw *= 0.6f;
        this.twitchPitch *= 0.6f;
        if (Math.abs(this.twitchYaw) < 0.01f) {
            this.twitchYaw = 0.0f;
        }
        if (Math.abs(this.twitchPitch) < 0.01f) {
            this.twitchPitch = 0.0f;
        }
        if (LAYER_JITTER && this.random.nextFloat() < 0.015f) {
            this.twitchYaw = (this.random.nextFloat() - 0.5f) * 2.0f;
            this.twitchPitch = (this.random.nextFloat() - 0.5f) * 1.0f;
        }
    }

    /** Полный конвейер точки прицеливания: осмотр в стойке -> EMA-лаг -> готовая точка. */
    private Vec3d updateAimPoint(LivingEntity target, float lastYaw, float lastPitch, long now) {
        if (this.needPickAim) {
            this.pickAimFractions(target);
            this.needPickAim = false;
        }

        // микро-осмотр: прицел на стоячей цели — медленно гуляет по хитбоксу с паузами
        if (LAYER_LOOK_AROUND && this.nextLookAroundTime > 0L && now >= this.nextLookAroundTime) {
            Rotation raw = this.freshAngle(this.currentAimPoint(target));
            float dist = (float) Math.hypot(MathHelper.wrapDegrees(raw.getYaw() - lastYaw), raw.getPitch() - lastPitch);
            if (dist < this.deadzone * 2.0f) {
                this.pickAimFractions(target);
                this.nextLookAroundTime = now + 600L + (long) (Math.random() * 900.0D);
            } else {
                this.nextLookAroundTime = now + 150L;
            }
        } else if (this.nextLookAroundTime == 0L) {
            this.nextLookAroundTime = now + 600L + (long) (Math.random() * 900.0D);
        }

        // EMA-лаг: человеческая задержка реакции на рывки цели, не мгновенный трекинг
        Vec3d freshAim = this.currentAimPoint(target);
        if (!LAYER_AIM_LAG) {
            return freshAim;
        }
        float k = MathHelper.clamp(0.22f * this.profileLatencyMul, 0.1f, 0.5f);
        this.smoothAim = this.smoothAim == null
                ? freshAim
                : this.smoothAim.add(freshAim.subtract(this.smoothAim).multiply(k));
        return this.smoothAim;
    }

    private float humanSpeedMul() {
        float mul = 1.0f;
        if (LAYER_DRIFT) {
            mul *= this.drift;
            mul *= 1.0f
                    + 0.08f * (float) Math.sin(this.tick * 0.13 + this.seedA)
                    + 0.05f * (float) Math.sin(this.tick * 0.047 + this.seedB);
        }
        if (LAYER_PROFILE) {
            mul *= this.profileSpeedMul;
        }
        return mul;
    }

    /** Свой «почерк» на каждую цель: сид из id цели + соль сессии. */
    private void onTargetChanged(LivingEntity target) {
        this.needPickAim = true;
        this.smoothAim = null;
        this.reactionTicks = 2 + this.random.nextInt(3);
        this.yawVelocity = 0.0f;
        this.pitchVelocity = 0.0f;
        this.alignedSince = 0L;
        this.alignedFactor = 0.0f;
        this.impulseTime = 0L;
        this.attackImpulse = 0.0f;
        this.nextLookAroundTime = 0L;
        this.twitchYaw = 0.0f;
        this.twitchPitch = 0.0f;

        if (LAYER_PROFILE) {
            this.profileRng.setSeed(this.sessionSalt ^ ((long) target.getId() * 2654435761L));
            this.profileSpeedMul = 0.9f + this.profileRng.nextFloat() * 0.2f;
            this.profileLatencyMul = 0.8f + this.profileRng.nextFloat() * 0.4f;
            this.profileJitterMul = 0.7f + this.profileRng.nextFloat() * 0.6f;
            float strength = 0.02f + this.profileRng.nextFloat() * 0.05f;
            this.yawBias = 1.0f + (this.profileRng.nextBoolean() ? strength : -strength);
        }
    }

    /**
     * Плавающая точка внутри хитбокса: в долях бокса (пересчёт каждый тик по
     * актуальному положению), зона корпус/голова, каждая новая точка гарантированно
     * отличается от предыдущей — удары не приходятся дважды в одно место.
     */
    private Vec3d currentAimPoint(LivingEntity target) {
        var box = target.getBoundingBox();
        return new Vec3d(
                box.minX + (box.maxX - box.minX) * this.aimFracX,
                box.minY + (box.maxY - box.minY) * this.aimFracY,
                box.minZ + (box.maxZ - box.minZ) * this.aimFracZ
        );
    }

    private Vec3d pointAt(LivingEntity target, double fx, double fy, double fz) {
        var box = target.getBoundingBox();
        return new Vec3d(
                box.minX + (box.maxX - box.minX) * fx,
                box.minY + (box.maxY - box.minY) * fy,
                box.minZ + (box.maxZ - box.minZ) * fz
        );
    }

    private void pickAimFractions(LivingEntity target) {
        for (int i = 0; i < 8; ++i) {
            double fx = 0.2 + 0.6 * this.random.nextDouble();
            double fy = 0.55 + 0.33 * this.random.nextDouble();
            double fz = 0.2 + 0.6 * this.random.nextDouble();
            Vec3d point = this.pointAt(target, fx, fy, fz);
            if (this.lastAimPoint == null || point.squaredDistanceTo(this.lastAimPoint) > 0.01) {
                this.aimFracX = fx;
                this.aimFracY = fy;
                this.aimFracZ = fz;
                this.lastAimPoint = point;
                return;
            }
        }

        this.aimFracX = 0.5;
        this.aimFracY = 0.7;
        this.aimFracZ = 0.5;
        this.lastAimPoint = this.pointAt(target, 0.5, 0.7, 0.5);
    }

    private Rotation freshAngle(Vec3d point) {
        return RotationUtil.fromVec3d(point.subtract(mc.player.getEyePos()));
    }

    private float randomFloat(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    // ==================== ВЫВОД ====================

    private void emit(Aura aura, float lastYaw, float lastPitch, boolean frozen) {
        float outYaw = lastYaw + this.yawVelocity;
        float outPitch = lastPitch + this.pitchVelocity;

        if (LAYER_JITTER && !frozen) {
            // микро-тряска через синусы разных частот + шум; к моменту удара стихает
            float swingProgress = mc.player.getHandSwingProgress(1.0f);
            float calm = 1.0f - swingProgress;
            float amp = (0.02f + this.random.nextFloat() * 0.02f) * calm * this.profileJitterMul;
            float slowSin = (float) Math.sin(this.tick * 0.7) + (float) Math.sin(this.tick * 0.31 + 1.7);
            outYaw += slowSin * amp * 0.5f + (this.random.nextFloat() - 0.5f) * amp;
            outPitch += (float) Math.sin(this.tick * 0.53 + 0.9) * amp * 0.4f + (this.random.nextFloat() - 0.5f) * amp;
            outPitch += (float) Math.sin(this.tick * 1.3) * MathHelper.clamp(mc.player.fallDistance * 0.01f, 0.0f, 0.15f) * calm;
            outYaw += this.twitchYaw;
            outPitch += this.twitchPitch;
        }

        // GCD-дизеринг: остаток копится между тиками, суб-GCD движение остаётся гладким
        float gcd = Rotation.gcd();
        this.residualYaw += outYaw - lastYaw;
        this.residualPitch += outPitch - lastPitch;
        float stepYaw = (float) Math.round(this.residualYaw / gcd) * gcd;
        float stepPitch = (float) Math.round(this.residualPitch / gcd) * gcd;
        this.residualYaw -= stepYaw;
        this.residualPitch -= stepPitch;

        float roundedYaw = lastYaw + stepYaw;
        float roundedPitch = MathHelper.clamp(lastPitch + stepPitch, -89.5f, 89.5f);

        if (Float.isNaN(roundedYaw) || Float.isNaN(roundedPitch)) {
            roundedYaw = lastYaw;
            roundedPitch = lastPitch;
        }

        Rotation rot = new Rotation(roundedYaw, roundedPitch);
        RotationComponent.update(rot, 360.0f, 360.0f, 360.0f, 360.0f, 0, 1, false);
        aura.lastYaw = rot.getYaw();
        aura.lastPitch = rot.getPitch();
    }

    @Override
    public void onAttack() {
        // новая точка удара после свинга — никогда не бьём дважды в одно место
        this.needPickAim = true;
        this.twitchYaw += (this.random.nextFloat() - 0.5f) * 0.6f;
        this.twitchPitch += (this.random.nextFloat() - 0.5f) * 0.3f;
    }

    private void reseedSession() {
        this.sessionSalt = ThreadLocalRandom.current().nextLong();
        this.seedA = this.random.nextFloat() * 6.28f;
        this.seedB = this.random.nextFloat() * 6.28f;
        this.drift = 1.0f;
        this.driftTarget = 1.0f;
        this.nextDriftTime = 0L;
        this.residualYaw = 0.0f;
        this.residualPitch = 0.0f;
        this.smoothAim = null;
        this.reactionTicks = 0;
        this.flinchTicks = 0;
        this.flinchImpulsePending = false;
    }

    public void hardReset() {
        this.lastTargetId = -1;
        this.clearRuntimeState();
        this.reseedSession();
        this.startTime = mc.player != null ? System.currentTimeMillis() : 0L;
    }

    private void clearRuntimeState() {
        this.needPickAim = true;
        this.lastAimPoint = null;
        this.yawVelocity = 0.0f;
        this.pitchVelocity = 0.0f;
        this.alignedSince = 0L;
        this.alignedFactor = 0.0f;
        this.impulseTime = 0L;
        this.attackImpulse = 0.0f;
        this.nextLookAroundTime = 0L;
        this.twitchYaw = 0.0f;
        this.twitchPitch = 0.0f;
        this.startTime = 0L;
    }
}
