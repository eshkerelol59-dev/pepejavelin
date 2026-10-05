package tech.javelin.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.events.impl.other.EventGameUpdate;
import tech.javelin.base.events.impl.other.EventTick;
import tech.javelin.base.events.impl.other.EventTickMovement;
import tech.javelin.base.events.impl.player.EventMoveInput;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.base.player.AttackUtil;
import tech.javelin.base.request.ScriptManager;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.javelin.client.modules.api.setting.impl.NumberSetting;
import tech.javelin.client.modules.impl.movement.AutoSprint;
import tech.javelin.utility.component.FreeLookComponent;
import tech.javelin.utility.component.RotationComponent;
import tech.javelin.utility.game.player.MovingUtil;
import tech.javelin.utility.game.player.PlayerInventoryUtil;
import tech.javelin.utility.game.player.RaytracingUtil;
import tech.javelin.utility.game.player.rotation.Rotation;
import tech.javelin.utility.game.player.rotation.RotationUtil;
import tech.javelin.utility.math.MultipointUtils;
import tech.javelin.utility.math.Timer;
import tech.javelin.utility.predict.PredictUtils;

@ModuleAnnotation(
        name = "Aura",
        category = Category.COMBAT,
        description = "Автоматически бьет цель"
)
public final class Aura extends Module {
    public static final Aura INSTANCE = new Aura();
    private final MultiBooleanSetting targetTypeSetting = MultiBooleanSetting.create("Атаковать", List.of("Игроков", "Мобов", "Животных"));
    public final ModeSetting rotationMode = new ModeSetting("Ротация", new String[0]);
    private final ModeSetting.Value funTime;
    private final ModeSetting.Value spookyTime;
    private final ModeSetting.Value spookyTimeV2;
    private final ModeSetting.Value intpol;
    private final ModeSetting.Value slimeWorld;
    private final ModeSetting.Value reallyWorld;
    private final ModeSetting.Value lonyJir;
    private final ModeSetting.Value neuro;
    private final ModeSetting.Value hvh;
    private final ModeSetting.Value shard;
    private final ModeSetting.Value sloth;
    private final ModeSetting.Value legit;
    private final ModeSetting.Value vanilla;

    public final ModeSetting typeSprint;
    public final ModeSetting throughBlocks;
    public final ModeSetting typeOtvodka;
    public final BooleanSetting smartcriticals;
    public final BooleanSetting dynamicCrits;
    public final BooleanSetting maceCrits;
    public final BooleanSetting autoSword;
    public final BooleanSetting maxDamage;

    private final ModeSetting correction;
    private final ModeSetting.Value correctionFocus;
    private final ModeSetting.Value correctionGood;
    private final ModeSetting.Value correctionNone;
    private final NumberSetting distance;
    private final NumberSetting distanceRotation;
    private final BooleanSetting shieldBreak;
    private final BooleanSetting legitSwap;
    private final BooleanSetting raycastCheck;
    private final BooleanSetting predictOnElytra;
    public final NumberSetting predict;
    public final BooleanSetting critsOnlyWithSpace;
    private LivingEntity target;
    private float acceleration;
    private boolean isBack;
    private final Timer hurtTimer;
    private final Timer critDelayTimer;
    private long critDelay = 0L;
    private final ScriptManager.ScriptTask script;
    private int lastSlot;
    private boolean sprintResetArmed;
    public float lastYaw;
    public float lastPitch;

    private Aura() {
        this.funTime = (new ModeSetting.Value(this.rotationMode, "FunTime")).select();
        this.spookyTime = new ModeSetting.Value(this.rotationMode, "SpookyTime");
        this.spookyTimeV2 = new ModeSetting.Value(this.rotationMode, "SpookyTimeV2");
        this.intpol = new ModeSetting.Value(this.rotationMode, "Intpol");
        this.slimeWorld = new ModeSetting.Value(this.rotationMode, "SlimeWorld");
        this.reallyWorld = new ModeSetting.Value(this.rotationMode, "ReallyWorld");
        this.lonyJir = new ModeSetting.Value(this.rotationMode, "LonyGrief");
        this.neuro = new ModeSetting.Value(this.rotationMode, "Neuro");
        this.hvh = new ModeSetting.Value(this.rotationMode, "HvH");
        this.shard = new ModeSetting.Value(this.rotationMode, "Shard");
        this.sloth = new ModeSetting.Value(this.rotationMode, "Sloth");
        this.legit = new ModeSetting.Value(this.rotationMode, "Legit");
        this.vanilla = new ModeSetting.Value(this.rotationMode, "Vanilla");

        this.typeSprint = new ModeSetting("Тип спринта", new String[]{"Packet", "Silent", "Legit", "Off"});
        this.throughBlocks = new ModeSetting("Бить через блоки", new String[]{"Умная", "Выключено", "Неполноценные", "Полноценные"});
        this.typeOtvodka = new ModeSetting("Тип отводки", new String[]{"Камера", "Плавная", "Space-Times", "Off"});
        this.smartcriticals = new BooleanSetting("Умные криты", true);
        this.dynamicCrits = new BooleanSetting("Динам криты", true);
        this.maceCrits = new BooleanSetting("Крит булавы", true);
        this.autoSword = new BooleanSetting("Авто-меч", true);
        this.maxDamage = new BooleanSetting("Макс дамаг", false);

        this.correction = new ModeSetting("Коррекция", new String[0]);
        this.correctionFocus = new ModeSetting.Value(this.correction, "Фокус");
        this.correctionGood = (new ModeSetting.Value(this.correction, "Свободная")).select();
        this.correctionNone = new ModeSetting.Value(this.correction, "Нет");
        this.distance = new NumberSetting("Дистанция", 3.0F, 0.5F, 6.0F, 0.1F, "Дистанция атаки");
        this.distanceRotation = new NumberSetting("Дистанция аима", 0.1F, 0.0F, 6.0F, 0.1F);
        this.shieldBreak = new BooleanSetting("Ломать щит", true);
        BooleanSetting var10005 = this.shieldBreak;
        Objects.requireNonNull(var10005);
        this.legitSwap = new BooleanSetting("Легитно ломать", true, var10005::isEnabled);
        this.raycastCheck = new BooleanSetting("Проверка на наведение", false);
        this.predictOnElytra = new BooleanSetting("Перегонять противника", true);
        this.predict = new NumberSetting("Насколько перегонять", 2.0F, 1.0F, 4.0F, 0.1F);
        this.critsOnlyWithSpace = new BooleanSetting("Только с пробелом", true);
        this.target = null;
        this.hurtTimer = new Timer();
        this.critDelayTimer = new Timer();
        this.script = new ScriptManager.ScriptTask();
        this.lastSlot = -1;
    }

    @Native
    private void breakShieldAndAttack() {
        boolean wasSwapped = false;
        boolean wasSwappedInventory = false;
        int slotHotbar = PlayerInventoryUtil.find((List)List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE), 0, 8);
        int slotInventory = PlayerInventoryUtil.find((List)List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE), 8, 35);
        if (slotHotbar != -1 && this.shieldBreak.isEnabled() && this.target.isBlocking()) {
            if (this.legitSwap.isEnabled()) {
                this.lastSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slotHotbar;
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
            }

            wasSwapped = true;
        }

        if (slotHotbar == -1 && slotInventory != -1 && this.shieldBreak.isEnabled() && this.target.isBlocking()) {
            if (this.legitSwap.isEnabled()) {
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                this.lastSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = 8;
            } else {
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
            }

            wasSwappedInventory = true;
        }

        mc.interactionManager.attackEntity(mc.player, this.target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (this.rotationMode.getValue() != null) {
            String currentModeName = this.rotationMode.getValue().getName();
            for (tech.javelin.client.modules.impl.combat.aura.RotationType type : tech.javelin.client.modules.impl.combat.aura.RotationType.values()) {
                if (currentModeName.equalsIgnoreCase(type.getName()) || currentModeName.equalsIgnoreCase(type.name())) {
                    type.getRotation().onAttack();
                    break;
                }
            }
        }
        if (wasSwapped) {
            if (this.legitSwap.isEnabled()) {
                Javelin.getInstance().getScriptManager().addTask(this.script);
                this.script.schedule(EventUpdate.class, (eventUpdate) -> {
                    mc.player.getInventory().selectedSlot = this.lastSlot;
                    this.lastSlot = -1;
                    return true;
                });
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }

        if (wasSwappedInventory) {
            if (this.legitSwap.isEnabled()) {
                Javelin.getInstance().getScriptManager().addTask(this.script);
                this.script.schedule(EventUpdate.class, (eventUpdate) -> {
                    mc.player.getInventory().selectedSlot = this.lastSlot;
                    mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                    mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
                    this.lastSlot = -1;
                    return true;
                });
            } else {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
            }
        }

    }

    @EventTarget
    public void onTick(EventTick e) {
        LivingEntity prevTarget = this.target;
        if (this.target == null || !this.isValid(this.target)) {
            this.target = this.updateTarget();
        }

        if (prevTarget != null && this.target == null && mc.player != null) {
            this.restoreSprintIfNeeded();
            if (this.typeOtvodka.is("Камера")) {
                float snapYaw = this.lastYaw != 0.0F ? this.lastYaw : mc.player.getYaw();
                float snapPitch = this.lastPitch != 0.0F ? this.lastPitch : mc.player.getPitch();
                mc.player.setYaw(snapYaw);
                mc.player.setPitch(snapPitch);
                FreeLookComponent.setFreeYaw(snapYaw);
                FreeLookComponent.setFreePitch(snapPitch);
                FreeLookComponent.setActive(false);
            } else if (this.typeOtvodka.is("Off")) {
                if (FreeLookComponent.getFreeYaw() != 0.0F) {
                    mc.player.setYaw(FreeLookComponent.getFreeYaw());
                    mc.player.setPitch(FreeLookComponent.getFreePitch());
                }
                FreeLookComponent.setActive(false);
            } else if (this.typeOtvodka.is("Плавная")) {
                float targetYaw = FreeLookComponent.getFreeYaw() != 0.0F ? FreeLookComponent.getFreeYaw() : mc.player.getYaw();
                float targetPitch = FreeLookComponent.getFreePitch() != 0.0F ? FreeLookComponent.getFreePitch() : mc.player.getPitch();
                float curYaw = this.lastYaw != 0.0F ? this.lastYaw : mc.player.getYaw();
                float curPitch = this.lastPitch != 0.0F ? this.lastPitch : mc.player.getPitch();
                float smoothYaw = MathHelper.lerp(0.5F, curYaw, targetYaw);
                float smoothPitch = MathHelper.lerp(0.5F, curPitch, targetPitch);
                mc.player.setYaw(smoothYaw);
                mc.player.setPitch(smoothPitch);
                FreeLookComponent.setFreeYaw(smoothYaw);
                FreeLookComponent.setFreePitch(smoothPitch);
                FreeLookComponent.setActive(false);
            } else {
                FreeLookComponent.setActive(false);
            }
        }

        if (this.target != null) {
            this.handleSprintResetBeforeCrit();

            if (this.canDynamicCrit() && this.isCanAttack() && this.hurtTimer.finished(458L) && !this.target.isBlocking()) {
                boolean wasSprinting = mc.player.isSprinting();
                boolean sprintPreReset = this.sprintResetArmed;
                if (wasSprinting && !sprintPreReset && (this.typeSprint.is("Packet") || this.typeSprint.is("Silent"))) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                    mc.player.setSprinting(false);
                }

                mc.interactionManager.attackEntity(mc.player, this.target);
                mc.player.swingHand(Hand.MAIN_HAND);
                if (this.rotationMode.getValue() != null) {
                    String currentModeName = this.rotationMode.getValue().getName();
                    for (tech.javelin.client.modules.impl.combat.aura.RotationType type : tech.javelin.client.modules.impl.combat.aura.RotationType.values()) {
                        if (currentModeName.equalsIgnoreCase(type.getName()) || currentModeName.equalsIgnoreCase(type.name())) {
                            type.getRotation().onAttack();
                            break;
                        }
                    }
                }

                if ((wasSprinting || sprintPreReset) && (this.typeSprint.is("Packet") || this.typeSprint.is("Silent"))) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
                    mc.player.setSprinting(true);
                }

                this.sprintResetArmed = false;
                this.critDelay = 0L;

                if (this.typeSprint.is("Legit") && AutoSprint.INSTANCE.isEnabled()) {
                    mc.options.sprintKey.setPressed(true);
                }

                this.hurtTimer.reset();
            }

        }
    }

    private boolean isCritCondition() {
        return !mc.player.isOnGround()
                && mc.player.getVelocity().y < 0.0D
                && !mc.player.isClimbing()
                && !mc.player.isTouchingWater();
    }

    /**
     * Динам криты: удар только когда крит гарантирован (падаем и спринт выключен),
     * перед каждым ударом — рандомное ожидание 60-70 мс.
     */
    private boolean canDynamicCrit() {
        if (!this.dynamicCrits.isEnabled()) {
            return true;
        }
        if (!this.isCritCondition() || mc.player.isSprinting()) {
            this.critDelay = 0L;
            return false;
        }
        if (this.critDelay == 0L) {
            this.critDelay = 60L + (long) (Math.random() * 11.0D);
            this.critDelayTimer.reset();
            return false;
        }
        return this.critDelayTimer.finished(this.critDelay);
    }

    private boolean isSprintResetType() {
        return this.typeSprint.is("Packet") || this.typeSprint.is("Silent") || this.typeSprint.is("Legit");
    }

    private void handleSprintResetBeforeCrit() {
        if (mc.player == null || this.target == null) {
            return;
        }

        if (this.sprintResetArmed) {
            float progress = mc.player.getAttackCooldownProgress(0.0F);
            int ticksToAttack = MathHelper.ceil((0.9F - progress) * mc.player.getAttackCooldownProgressPerTick());
            if (!this.isCritCondition() || ticksToAttack < 0) {
                this.restoreSprintIfNeeded();
            } else {
                mc.player.setSprinting(false);
                if (this.typeSprint.is("Legit")) {
                    mc.options.sprintKey.setPressed(false);
                }
            }
            return;
        }

        if (!this.smartcriticals.isEnabled() || !this.isSprintResetType()) {
            return;
        }

        if (!this.isCritCondition() || mc.player.getAttackCooldownProgress(0.0F) >= 0.9F) {
            return;
        }

        int ticksToAttack = MathHelper.ceil((0.9F - mc.player.getAttackCooldownProgress(0.0F)) * mc.player.getAttackCooldownProgressPerTick());
        if (ticksToAttack > 3) {
            return;
        }

        if (this.typeSprint.is("Legit")) {
            mc.options.sprintKey.setPressed(false);
            mc.player.setSprinting(false);
        } else if (mc.player.isSprinting()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }
        this.sprintResetArmed = true;
    }

    private void restoreSprintIfNeeded() {
        if (!this.sprintResetArmed) {
            return;
        }
        this.sprintResetArmed = false;
        if (mc.player == null) {
            return;
        }
        if (this.typeSprint.is("Legit")) {
            if (AutoSprint.INSTANCE.isEnabled()) {
                mc.options.sprintKey.setPressed(true);
            }
        } else if (!mc.player.isSprinting() && (this.typeSprint.is("Packet") || this.typeSprint.is("Silent"))) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
            mc.player.setSprinting(true);
        }
    }

    @EventTarget
    @Native
    public void onTickMovement(EventTickMovement e) {
        if (this.target != null) {
            if (this.target.isBlocking() && this.hurtTimer.finished(50L)) {
                if (mc.player.isSprinting() && !mc.player.isOnGround() && !mc.player.isSwimming()) {
                    mc.player.setSprinting(false);
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                    if (!AutoSprint.INSTANCE.isEnabled()) {
                        mc.options.sprintKey.setPressed(false);
                    }
                }

                this.breakShieldAndAttack();
                this.hurtTimer.reset();
            }

        }
    }

    @EventTarget
    @Native
    public void eventRotate(EventGameUpdate e) {
        if (this.target != null) {
            Javelin.getInstance().getModuleManager().setAcceleration(0.0F);
            if (this.rotationMode.getValue() != null) {
                String currentModeName = this.rotationMode.getValue().getName();
                for (tech.javelin.client.modules.impl.combat.aura.RotationType type : tech.javelin.client.modules.impl.combat.aura.RotationType.values()) {
                    if (currentModeName.equalsIgnoreCase(type.getName()) || currentModeName.equalsIgnoreCase(type.name())) {
                        type.getRotation().onRotation(this, this.target, new float[]{this.distance.getCurrent(), this.distanceRotation.getCurrent()}, this.isCanAttack());
                        return;
                    }
                }
            }

            // Vanilla fallback
            Box box = this.target.getBoundingBox();
            Vec3d eyes = mc.player.getEyePos();
            Vec3d point = box.getCenter();
            Rotation angle = RotationUtil.fromVec3d(point.subtract(eyes));

            float deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - this.lastYaw);
            float deltaPitch = angle.getPitch() - this.lastPitch;
            float smooth = this.lastYaw + deltaYaw;
            float newYaw = this.lastPitch + deltaPitch;
            smooth -= (smooth - this.lastYaw) % Rotation.gcd();
            newYaw -= (newYaw - this.lastPitch) % Rotation.gcd();

            Rotation smoothRot = new Rotation(smooth, newYaw);
            RotationComponent.update(new Rotation(smoothRot.getYaw(), smoothRot.getPitch()), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, false);
            this.lastYaw = smoothRot.getYaw();
            this.lastPitch = smoothRot.getPitch();
        }
    }

    private boolean isCanAttack() {
        if (mc.player.getAttackCooldownProgress(0.5F) < 0.9F) {
            return false;
        } else if (!AttackUtil.canAttack()) {
            return false;
        } else if (this.target instanceof PlayerEntity && this.predictOnElytra.isEnabled() && mc.player.isGliding() && this.target.isGliding() && mc.player.getEyePos().distanceTo(PredictUtils.predict(this.target, this.target.getPos(), this.predict.getCurrent())) > 3.0D && mc.player.getEyePos().distanceTo(this.target.getBoundingBox().getCenter()) > 3.0D) {
            return false;
        } else if ((!mc.player.isGliding() || !this.target.isGliding()) && mc.player.getEyePos().distanceTo(MultipointUtils.getNearestPoint(this.target, (double)this.distance.getCurrent())) > (double)this.distance.getCurrent()) {
            return false;
        } else {
            return !this.raycastCheck.isEnabled() || RaytracingUtil.rayTrace(mc.player.getRotationVector(), (double)this.distance.getCurrent(), this.target.getBoundingBox()) || mc.targetedEntity != null || mc.player.isGliding() || this.target.isGliding();
        }
    }

    private LivingEntity updateTarget() {
        List targets = new ArrayList();
        Iterator var2 = mc.world.getEntities().iterator();

        while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                if (this.isValid(living)) {
                    targets.add(living);
                }
            }
        }

        if (!targets.isEmpty() && this.isEnabled()) {
            targets.sort(Comparator.comparingDouble((LivingEntity entityx) -> {
                Rotation vec = Rotation.getRotations(entityx.getBoundingBox().getCenter());
                double dy = (double)Math.abs(MathHelper.wrapDegrees(vec.getYaw() - mc.player.getYaw()));
                double dp = (double)Math.abs(MathHelper.wrapDegrees(vec.getPitch() - mc.player.getPitch()));
                return dy + dp;
            }));
            return targets.isEmpty() ? null : (LivingEntity)targets.get(0);
        } else {
            return null;
        }
    }

    public boolean isValid(LivingEntity entity) {
        if (entity == mc.player) {
            return false;
        } else if (entity.isAlive() && !(entity.getHealth() <= 0.0F)) {
            if (mc.player.isAlive() && !(mc.player.getHealth() <= 0.0F)) {
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity)entity;
                    if (!this.targetTypeSetting.isEnable("Игроков")) {
                        return false;
                    }

                    if (Javelin.getInstance().getFriendManager().isFriend(entity.getName().getString())) {
                        return false;
                    }

                    if (AntiBot.INSTANCE.isBot(player)) {
                        return false;
                    }
                }

                if (!(entity instanceof PassiveEntity) && !(entity instanceof FishEntity) || this.targetTypeSetting.isEnable("Животных") && !Javelin.getInstance().getServerHandler().isPvp()) {
                    if (!(entity instanceof HostileEntity) && !(entity instanceof AmbientEntity) || this.targetTypeSetting.isEnable("Мобов") && !Javelin.getInstance().getServerHandler().isPvp()) {
                        if (mc.player.getEyePos().distanceTo(MultipointUtils.getNearestPoint(entity, (double)(this.distance.getCurrent() + this.distanceRotation.getCurrent()))) > (double)(mc.player.isGliding() ? 20.0F : this.distance.getCurrent() + this.distanceRotation.getCurrent())) {
                            return false;
                        } else {
                            return !(entity instanceof ArmorStandEntity);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @EventTarget
    private void setCorrection(EventMoveInput eventMoveInput) {
        if (!this.correctionNone.isSelected() && this.target != null) {
            if (this.correctionFocus.isSelected()) {
                MovingUtil.fixMovementFocus(eventMoveInput, mc.player.getYaw());
            } else {
                MovingUtil.fixMovementFree(eventMoveInput);
            }

        }
    }

    public LivingEntity getTarget() {
        return this.isEnabled() ? this.target : null;
    }

    public void onEnable() {
        this.target = null;
        super.onEnable();
    }

    public void onDisable() {
        Javelin.getInstance().getModuleManager().setAcceleration(0.0F);
        this.restoreSprintIfNeeded();
        if (mc.player != null) {
            if (this.typeOtvodka.is("Off")) {
                if (FreeLookComponent.getFreeYaw() != 0.0F) {
                    mc.player.setYaw(FreeLookComponent.getFreeYaw());
                    mc.player.setPitch(FreeLookComponent.getFreePitch());
                }
            } else if (this.typeOtvodka.is("Камера")) {
                if (this.lastYaw != 0.0F || this.lastPitch != 0.0F) {
                    mc.player.setYaw(this.lastYaw);
                    mc.player.setPitch(this.lastPitch);
                    FreeLookComponent.setFreeYaw(this.lastYaw);
                    FreeLookComponent.setFreePitch(this.lastPitch);
                }
            } else if (this.lastYaw != 0.0F || this.lastPitch != 0.0F) {
                mc.player.setYaw(this.lastYaw);
                mc.player.setPitch(this.lastPitch);
                FreeLookComponent.setFreeYaw(this.lastYaw);
                FreeLookComponent.setFreePitch(this.lastPitch);
            }
            FreeLookComponent.setActive(false);
        }
        if (this.rotationMode.getValue() != null) {
            String currentModeName = this.rotationMode.getValue().getName();
            for (tech.javelin.client.modules.impl.combat.aura.RotationType type : tech.javelin.client.modules.impl.combat.aura.RotationType.values()) {
                if (currentModeName.equalsIgnoreCase(type.getName()) || currentModeName.equalsIgnoreCase(type.name())) {
                    if (type.getRotation() instanceof tech.javelin.client.modules.impl.combat.aura.rotation.SpookytimeV2Rotation) {
                        ((tech.javelin.client.modules.impl.combat.aura.rotation.SpookytimeV2Rotation) type.getRotation()).hardReset();
                    }
                    if (type.getRotation() instanceof tech.javelin.client.modules.impl.combat.aura.rotation.IntpolRotation) {
                        ((tech.javelin.client.modules.impl.combat.aura.rotation.IntpolRotation) type.getRotation()).hardReset();
                    }
                    break;
                }
            }
        }
        this.target = null;
        super.onDisable();
    }
}