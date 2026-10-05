package tech.javelin.client.modules.impl.combat.aura.rotation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tech.javelin.client.modules.impl.combat.Aura;
import tech.javelin.client.modules.impl.combat.aura.RotationAura;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Intpol — ротация на интерполяционных кривых сглаживания.
 * Каждый сегмент наводки проходит через случайную easing-функцию из большого
 * перемешанного паттерна, поэтому профиль ускорения постоянно различается.
 */
public class IntpolRotation implements RotationAura {

    public enum Easing {
        IN_SINE {
            public float apply(float x) { return (float) (1.0 - Math.cos(x * Math.PI / 2.0)); }
        },
        OUT_SINE {
            public float apply(float x) { return (float) Math.sin(x * Math.PI / 2.0); }
        },
        IN_OUT_SINE {
            public float apply(float x) { return (float) (-(Math.cos(Math.PI * x) - 1.0) / 2.0); }
        },
        IN_QUAD {
            public float apply(float x) { return x * x; }
        },
        OUT_QUAD {
            public float apply(float x) { return 1.0f - (1.0f - x) * (1.0f - x); }
        },
        IN_OUT_QUAD {
            public float apply(float x) { return x < 0.5f ? 2.0f * x * x : 1.0f - (float) Math.pow(-2.0 * x + 2.0, 2.0) / 2.0f; }
        },
        IN_CUBIC {
            public float apply(float x) { return x * x * x; }
        },
        OUT_CUBIC {
            public float apply(float x) { return 1.0f - (float) Math.pow(1.0 - x, 3.0); }
        },
        IN_OUT_CUBIC {
            public float apply(float x) { return x < 0.5f ? 4.0f * x * x * x : 1.0f - (float) Math.pow(-2.0 * x + 2.0, 3.0) / 2.0f; }
        },
        IN_QUART {
            public float apply(float x) { return x * x * x * x; }
        },
        OUT_QUART {
            public float apply(float x) { return 1.0f - (float) Math.pow(1.0 - x, 4.0); }
        },
        IN_OUT_QUART {
            public float apply(float x) { return x < 0.5f ? 8.0f * x * x * x * x : 1.0f - (float) Math.pow(-2.0 * x + 2.0, 4.0) / 2.0f; }
        },
        IN_QUINT {
            public float apply(float x) { return x * x * x * x * x; }
        },
        OUT_QUINT {
            public float apply(float x) { return 1.0f - (float) Math.pow(1.0 - x, 5.0); }
        },
        IN_OUT_QUINT {
            public float apply(float x) { return x < 0.5f ? 16.0f * x * x * x * x * x : 1.0f - (float) Math.pow(-2.0 * x + 2.0, 5.0) / 2.0f; }
        },
        IN_EXPO {
            public float apply(float x) { return x == 0.0f ? 0.0f : (float) Math.pow(2.0, 10.0 * x - 10.0); }
        },
        OUT_EXPO {
            public float apply(float x) { return x == 1.0f ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * x); }
        },
        IN_OUT_EXPO {
            public float apply(float x) {
                if (x == 0.0f) return 0.0f;
                if (x == 1.0f) return 1.0f;
                return x < 0.5f ? (float) Math.pow(2.0, 20.0 * x - 10.0) / 2.0f : (2.0f - (float) Math.pow(2.0, -20.0 * x + 10.0)) / 2.0f;
            }
        },
        IN_CIRC {
            public float apply(float x) { return 1.0f - (float) Math.sqrt(1.0 - x * x); }
        },
        OUT_CIRC {
            public float apply(float x) { return (float) Math.sqrt(1.0 - Math.pow(x - 1.0, 2.0)); }
        },
        IN_OUT_CIRC {
            public float apply(float x) {
                return x < 0.5f
                        ? (1.0f - (float) Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2.0f
                        : ((float) Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0f) / 2.0f;
            }
        },
        IN_BACK {
            public float apply(float x) { return (float) (2.70158 * Math.pow(x, 3.0) - 1.70158 * x * x); }
        },
        OUT_BACK {
            public float apply(float x) { return (float) (1.0 + 2.70158 * Math.pow(x - 1.0, 3.0) + 1.70158 * Math.pow(x - 1.0, 2.0)); }
        },
        IN_OUT_BACK {
            public float apply(float x) {
                float c2 = 2.59491f;
                return x < 0.5f
                        ? (float) (Math.pow(2.0 * x, 2.0) * ((c2 + 1.0) * 2.0 * x - c2)) / 2.0f
                        : (float) (Math.pow(2.0 * x - 2.0, 2.0) * ((c2 + 1.0) * (x * 2.0 - 2.0) + c2) + 2.0) / 2.0f;
            }
        },
        IN_ELASTIC {
            public float apply(float x) {
                if (x == 0.0f) return 0.0f;
                if (x == 1.0f) return 1.0f;
                return (float) (-Math.pow(2.0, 10.0 * x - 10.0) * Math.sin((x * 10.0 - 10.75) * (2.0 * Math.PI / 3.0)));
            }
        },
        OUT_ELASTIC {
            public float apply(float x) {
                if (x == 0.0f) return 0.0f;
                if (x == 1.0f) return 1.0f;
                return (float) (Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * (2.0 * Math.PI / 3.0))) + 1.0f;
            }
        },
        IN_OUT_ELASTIC {
            public float apply(float x) {
                if (x == 0.0f) return 0.0f;
                if (x == 1.0f) return 1.0f;
                return x < 0.5f
                        ? (float) (-(Math.pow(2.0, 20.0 * x - 10.0) * Math.sin((20.0 * x - 11.125) * (2.0 * Math.PI / 4.5)))) / 2.0f
                        : (float) ((Math.pow(2.0, -20.0 * x + 10.0) * Math.sin((20.0 * x - 11.125) * (2.0 * Math.PI / 4.5)))) / 2.0f + 1.0f;
            }
        },
        IN_BOUNCE {
            public float apply(float x) { return 1.0f - OUT_BOUNCE.apply(1.0f - x); }
        },
        OUT_BOUNCE {
            public float apply(float x) {
                float n1 = 7.5625f;
                float d1 = 2.75f;
                if (x < 1.0f / d1) return n1 * x * x;
                if (x < 2.0f / d1) return n1 * (x - 1.5f / d1) * (x - 1.5f / d1) + 0.75f;
                if (x < 2.5f / d1) return n1 * (x - 2.25f / d1) * (x - 2.25f / d1) + 0.9375f;
                return n1 * (x - 2.625f / d1) * (x - 2.625f / d1) + 0.984375f;
            }
        },
        IN_OUT_BOUNCE {
            public float apply(float x) {
                return x < 0.5f
                        ? (1.0f - OUT_BOUNCE.apply(1.0f - 2.0f * x)) / 2.0f
                        : (2.0f + OUT_BOUNCE.apply(2.0f * x - 2.0f)) / 2.0f;
            }
        };

        public abstract float apply(float x);
    }

    private static final Easing[] ALL_EASINGS = Easing.values();
    private static final int PATTERN_LENGTH = ALL_EASINGS.length * 8;

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final int[] pattern = new int[PATTERN_LENGTH];
    private int patternIndex = 0;

    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;
    private boolean initialized = false;
    private int lastTargetId = -1;
    private long tick = 0L;

    private double aimFracX = 0.5;
    private double aimFracY = 0.7;
    private double aimFracZ = 0.5;
    private boolean needPickAim = true;
    private Vec3d lastAimPoint = null;

    private float segStartYaw = 0.0f;
    private int segDurYaw = 1;
    private int segProgYaw = 0;
    private Easing segEaseYaw = Easing.IN_OUT_SINE;
    private float overYaw = 0.0f;

    private float segStartPitch = 0.0f;
    private int segDurPitch = 1;
    private int segProgPitch = 0;
    private Easing segEasePitch = Easing.IN_OUT_SINE;
    private float overPitch = 0.0f;

    private float errYaw = 0.0f;
    private float errPitch = 0.0f;
    private int errTicks = 0;
    private int nextErrChange = 20;

    private float twitchYaw = 0.0f;
    private float twitchPitch = 0.0f;

    public IntpolRotation() {
        this.ensurePattern();
    }

    @Override
    public void onRotation(Aura aura, LivingEntity target, float[] ranges, boolean canAttack) {
        if (mc.player == null || target == null) {
            return;
        }

        if (!this.initialized) {
            this.lastYaw = aura.lastYaw != 0 ? aura.lastYaw : mc.player.getYaw();
            this.lastPitch = aura.lastPitch != 0 ? aura.lastPitch : mc.player.getPitch();
            this.initialized = true;
            this.segProgYaw = this.segDurYaw;
            this.segProgPitch = this.segDurPitch;
        }

        ++this.tick;

        int currentTargetId = target.getId();
        if (currentTargetId != this.lastTargetId) {
            this.lastTargetId = currentTargetId;
            this.needPickAim = true;
            this.twitchYaw = 0.0f;
            this.twitchPitch = 0.0f;
            this.errYaw = 0.0f;
            this.errPitch = 0.0f;
            this.errTicks = 0;
            this.nextErrChange = 20;
        }

        ++this.errTicks;
        if (this.errTicks >= this.nextErrChange) {
            this.errTicks = 0;
            this.nextErrChange = 15 + this.random.nextInt(25);
            this.errYaw = (this.random.nextFloat() - 0.5f) * 2.2f;
            this.errPitch = (this.random.nextFloat() - 0.5f) * 1.1f;
        }

        // точка внутри хитбокса пересчитывается каждый тик — ротация едет за движущейся целью
        if (this.needPickAim) {
            this.pickAimFractions(target);
            this.needPickAim = false;
        }
        Vec3d aimPoint = this.currentAimPoint(target);
        double dist = mc.player.getEyePos().distanceTo(aimPoint);
        Rotation fresh = freshAngle(aimPoint);

        if (this.segProgYaw >= this.segDurYaw) {
            this.planYaw(fresh, dist);
        }
        if (this.segProgPitch >= this.segDurPitch) {
            this.planPitch(fresh, dist);
        }

        // цель могла сдвинуться с момента планирования — обновляем конечную точку сегмента
        float effTargetYaw = fresh.getYaw() + this.errYaw + this.overYaw;
        float effTargetPitch = MathHelper.clamp(fresh.getPitch() + this.errPitch + this.overPitch, -90.0f, 90.0f);

        float tYaw = MathHelper.clamp((float) (this.segProgYaw + 1) / (float) this.segDurYaw, 0.0f, 1.0f);
        float tPitch = MathHelper.clamp((float) (this.segProgPitch + 1) / (float) this.segDurPitch, 0.0f, 1.0f);

        float easedYaw = this.segEaseYaw.apply(tYaw);
        // по вертикали ограничиваем выброс кривой, чтобы прицел не уезжал в землю
        float easedPitch = MathHelper.clamp(this.segEasePitch.apply(tPitch), -0.15f, 1.15f);

        float outYaw = this.segStartYaw + MathHelper.wrapDegrees(effTargetYaw - this.segStartYaw) * easedYaw;
        float outPitch = this.segStartPitch + (effTargetPitch - this.segStartPitch) * easedPitch;

        // прогресс свинга: чем ближе удар, тем спокойнее тряска и точнее наводка
        float swingProgress = mc.player.getHandSwingProgress(1.0f);
        float calm = 1.0f - swingProgress;

        // микротряска через синусы разных частот + очень маленький шум
        float jitterAmp = (0.02f + this.random.nextFloat() * 0.02f) * calm;
        float slowSin = (float) Math.sin(this.tick * 0.7) + (float) Math.sin(this.tick * 0.31 + 1.7);
        outYaw += slowSin * jitterAmp * 0.5f + (this.random.nextFloat() - 0.5f) * jitterAmp;
        outPitch += (float) Math.sin(this.tick * 0.53 + 0.9) * jitterAmp * 0.4f + (this.random.nextFloat() - 0.5f) * jitterAmp;

        // фал дистанция добавляет дрожь по вертикали
        outPitch += (float) Math.sin(this.tick * 1.3) * MathHelper.clamp(mc.player.fallDistance * 0.01f, 0.0f, 0.15f) * calm;

        // редкие случайные подергивания, затухают со временем
        outYaw += this.twitchYaw;
        outPitch += this.twitchPitch;
        this.twitchYaw *= 0.6f;
        this.twitchPitch *= 0.6f;
        if (Math.abs(this.twitchYaw) < 0.01f) {
            this.twitchYaw = 0.0f;
        }
        if (Math.abs(this.twitchPitch) < 0.01f) {
            this.twitchPitch = 0.0f;
        }
        if (this.random.nextFloat() < 0.015f) {
            this.twitchYaw = (this.random.nextFloat() - 0.5f) * 2.0f;
            this.twitchPitch = (this.random.nextFloat() - 0.5f) * 1.0f;
        }

        float gcd = Rotation.gcd();
        float roundedYaw = this.lastYaw + (float) Math.round((outYaw - this.lastYaw) / gcd) * gcd;
        float roundedPitch = this.lastPitch + (float) Math.round((outPitch - this.lastPitch) / gcd) * gcd;

        // если угол считай что такой же как старый — оставляем старый
        if (Math.abs(MathHelper.wrapDegrees(roundedYaw - this.lastYaw)) < 0.02f) {
            roundedYaw = this.lastYaw;
        }
        if (Math.abs(roundedPitch - this.lastPitch) < 0.02f) {
            roundedPitch = this.lastPitch;
        }

        roundedPitch = MathHelper.clamp(roundedPitch, -90.0f, 90.0f);

        if (Float.isNaN(roundedYaw) || Float.isNaN(roundedPitch)) {
            roundedYaw = this.lastYaw;
            roundedPitch = this.lastPitch;
        }

        ++this.segProgYaw;
        ++this.segProgPitch;

        Rotation rot = new Rotation(roundedYaw, roundedPitch);
        RotationComponent.update(rot, 360.0f, 360.0f, 360.0f, 360.0f, 0, 1, false);
        aura.lastYaw = rot.getYaw();
        aura.lastPitch = rot.getPitch();
        this.lastYaw = rot.getYaw();
        this.lastPitch = rot.getPitch();
    }

    private Rotation freshAngle(Vec3d point) {
        return RotationUtil.fromVec3d(point.subtract(mc.player.getEyePos()));
    }

    private void planYaw(Rotation fresh, double dist) {
        this.segProgYaw = 0;
        this.segStartYaw = this.lastYaw;
        this.overYaw = 0.0f;

        float targetYaw = fresh.getYaw() + this.errYaw;
        float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
        float absDelta = Math.abs(deltaYaw);

        // при малой разнице — почти стоп, оставляем старый угол
        if (absDelta < 0.15f) {
            this.segDurYaw = 1;
            this.segEaseYaw = Easing.OUT_SINE;
            return;
        }

        // иногда прицел перекручивается — проскакивает цель и возвращается
        if (this.random.nextFloat() < 0.07f && absDelta > 8.0f) {
            this.overYaw = (4.0f + this.random.nextFloat() * 6.0f) * Math.signum(deltaYaw);
            this.segEaseYaw = this.random.nextBoolean() ? Easing.OUT_BACK : Easing.OUT_ELASTIC;
        } else {
            this.patternIndex = (this.patternIndex + 1) % this.pattern.length;
            this.segEaseYaw = ALL_EASINGS[this.pattern[this.patternIndex]];
        }

        float duration = this.segmentDuration(absDelta) / this.speedModifier(dist);
        this.segDurYaw = MathHelper.clamp(Math.round(duration), 1, 8);
    }

    private void planPitch(Rotation fresh, double dist) {
        this.segProgPitch = 0;
        this.segStartPitch = this.lastPitch;
        this.overPitch = 0.0f;

        float targetPitch = MathHelper.clamp(fresh.getPitch() + this.errPitch, -90.0f, 90.0f);
        float deltaPitch = targetPitch - this.lastPitch;
        float absDelta = Math.abs(deltaPitch);

        if (absDelta < 0.15f) {
            this.segDurPitch = 1;
            this.segEasePitch = Easing.OUT_SINE;
            return;
        }

        this.patternIndex = (this.patternIndex + 1) % this.pattern.length;
        this.segEasePitch = ALL_EASINGS[this.pattern[this.patternIndex]];

        // вертикальная скорость меньше горизонтальной — сегмент дольше
        float duration = this.segmentDuration(absDelta) / this.speedModifier(dist) * (1.3f + this.random.nextFloat() * 0.2f);
        this.segDurPitch = MathHelper.clamp(Math.round(duration), 1, 10);
    }

    /**
     * Разница между текущим углом и целью задает плавность:
     * большая разница — короче сегмент (ускорение), маленькая — длиннее (плавный доезд).
     */
    private float segmentDuration(float absDelta) {
        if (absDelta > 30.0f) {
            return 2.0f + this.random.nextFloat();
        }
        if (absDelta > 12.0f) {
            return 3.0f + this.random.nextFloat();
        }
        if (absDelta > 4.0f) {
            return 4.0f + this.random.nextFloat() * 1.5f;
        }
        return 5.0f + this.random.nextFloat() * 2.0f;
    }

    /**
     * Зависимость скорости от обычных переменных игрока:
     * дистанция до цели, горизонтальная скорость, фал дистанция и прогресс свинга.
     */
    private float speedModifier(double dist) {
        float factor = this.distanceSpeedFactor(dist);

        double hSpeed = Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
        factor *= 1.0f + MathHelper.clamp((float) hSpeed * 0.25f, 0.0f, 0.3f);

        factor *= 1.0f + Math.min(mc.player.fallDistance * 0.03f, 0.15f);
        factor *= 1.0f + mc.player.getHandSwingProgress(1.0f) * 0.2f;

        return MathHelper.clamp(factor, 0.5f, 2.0f);
    }

    /**
     * Слишком близко — скорость снижается, средняя дистанция — быстрее,
     * далеко — опять снижается. Кусочно-линейная интерполяция между зонами.
     */
    private float distanceSpeedFactor(double dist) {
        if (dist <= 2.2) {
            return 0.8f;
        }
        if (dist <= 3.0) {
            return (float) (0.8 + (1.2 - 0.8) * ((dist - 2.2) / 0.8));
        }
        if (dist <= 4.0) {
            return (float) (1.2 + (0.85 - 1.2) * ((dist - 3.0) / 1.0));
        }
        return 0.85f;
    }

    /**
     * Плавающая точка внутри хитбокса: хранится в долях бокса, поэтому каждый тик
     * пересчитывается по актуальному положению цели и не отстает при движении.
     * Каждая новая точка гарантированно отличается от предыдущей — удары не
     * приходятся дважды в одно место. Зона прицеливания — корпус/голова, не ноги.
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

    private void ensurePattern() {
        for (int i = 0; i < PATTERN_LENGTH; ++i) {
            this.pattern[i] = i % ALL_EASINGS.length;
        }
        for (int i = PATTERN_LENGTH - 1; i > 0; --i) {
            int j = this.random.nextInt(i + 1);
            int tmp = this.pattern[i];
            this.pattern[i] = this.pattern[j];
            this.pattern[j] = tmp;
        }
    }

    @Override
    public void onAttack() {
        // новая точка удара на следующий тик — никогда не бьем дважды в одно место
        this.needPickAim = true;
        this.errYaw += (this.random.nextFloat() - 0.5f) * 0.8f;
        this.errPitch += (this.random.nextFloat() - 0.5f) * 0.4f;
        this.patternIndex = (this.patternIndex + 1 + this.random.nextInt(5)) % this.pattern.length;
        this.twitchYaw += (this.random.nextFloat() - 0.5f) * 0.6f;
        this.twitchPitch += (this.random.nextFloat() - 0.5f) * 0.3f;
    }

    public void hardReset() {
        this.lastTargetId = -1;
        this.initialized = false;
        this.needPickAim = true;
        this.lastAimPoint = null;
        this.overYaw = 0.0f;
        this.overPitch = 0.0f;
        this.twitchYaw = 0.0f;
        this.twitchPitch = 0.0f;
        this.errYaw = 0.0f;
        this.errPitch = 0.0f;
        this.errTicks = 0;
        this.nextErrChange = 20;
        this.segProgYaw = this.segDurYaw;
        this.segProgPitch = this.segDurPitch;
        this.ensurePattern();
        if (mc.player != null) {
            this.lastYaw = mc.player.getYaw();
            this.lastPitch = mc.player.getPitch();
            this.initialized = true;
        }
    }
}
