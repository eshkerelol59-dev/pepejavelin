package tech.javelin.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.base.events.impl.server.EventPacket;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;
import tech.javelin.client.modules.api.setting.impl.NumberSetting;
import tech.javelin.client.modules.impl.movement.AutoSprint;
import tech.javelin.utility.game.player.PlayerInventoryUtil;
import tech.javelin.utility.game.player.SimulatedPlayer;

@ModuleAnnotation(
   name = "AutoTotem",
   category = Category.COMBAT,
   description = "При условиях берет тотем в руку"
)
public final class AutoTotem extends Module {
   public static final AutoTotem INSTANCE = new AutoTotem();
   private final NumberSetting health = new NumberSetting("Здоровье", 4.0F, 0.0F, 20.0F, 0.5F);
   private final BooleanSetting elytra = new BooleanSetting("Элитры", true);
   private final NumberSetting elytraHealth;
   private final BooleanSetting fall;
   private final NumberSetting fallDistance;
   private final BooleanSetting crystals;
   private final NumberSetting healthWithBall;
   private int cooldownTicks;
   private ItemStack previousStack;

   private AutoTotem() {
      BooleanSetting var10008 = this.elytra;
      Objects.requireNonNull(var10008);
      this.elytraHealth = new NumberSetting("Здоровье на элитрах", 10.0F, 0.0F, 20.0F, 0.5F, var10008::isEnabled);
      this.fall = new BooleanSetting("Падение", true);
      var10008 = this.fall;
      Objects.requireNonNull(var10008);
      this.fallDistance = new NumberSetting("Количество блоков", 20.0F, 10.0F, 50.0F, 0.1F, var10008::isEnabled);
      this.crystals = new BooleanSetting("Кристалы", true);
      var10008 = this.fall;
      Objects.requireNonNull(var10008);
      this.healthWithBall = new NumberSetting("Здоровье с шаром", 10.0F, 0.0F, 20.0F, 0.5F, var10008::isEnabled);
      this.cooldownTicks = 0;
      this.previousStack = null;
   }

   @EventTarget
   @Native
   public void onPlayerTick(EventUpdate event) {
      if (mc.player != null && mc.world != null) {
         if (this.cooldownTicks > 0) {
            --this.cooldownTicks;
         }

         ItemStack current = mc.player.getOffHandStack().isEmpty() ? null : mc.player.getOffHandStack();
         Slot slot;
         boolean wasSprinting;
         if (this.shouldUseTotem()) {
            if (current == null || current.getItem() != Items.TOTEM_OF_UNDYING) {
               slot = PlayerInventoryUtil.getSlot(Items.TOTEM_OF_UNDYING);
               if (slot != null && !PlayerInventoryUtil.isServerScreen()) {
                  if (this.previousStack == null || this.previousStack.isEmpty()) {
                     this.previousStack = current;
                  }

                  wasSprinting = false;
                  if (mc.player.isSprinting()) {
                     mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     mc.player.setSprinting(false);
                     mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                     if (!AutoSprint.INSTANCE.isEnabled()) {
                        mc.options.sprintKey.setPressed(false);
                     }

                     wasSprinting = true;
                  }

                  this.swapToOffhand(slot);
                  if (wasSprinting) {
                     mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
                  }

                  this.cooldownTicks = 3;
               }
            }
         } else if (this.previousStack != null && this.cooldownTicks == 0 && !this.previousStack.isEmpty()) {
            slot = PlayerInventoryUtil.getSlot(this.previousStack);
            if (slot != null) {
               wasSprinting = false;
               if (mc.player.isSprinting()) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                  mc.player.setSprinting(false);
                  mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
                  if (!AutoSprint.INSTANCE.isEnabled()) {
                     mc.options.sprintKey.setPressed(false);
                  }

                  wasSprinting = true;
               }

               this.swapToOffhand(slot);
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }

               this.cooldownTicks = 3;
            }

            this.previousStack = null;
         }

      }
   }

   @EventTarget
   @Native
   private void onPacket(EventPacket e) {
      if (e.isSent()) {
      }

      Packet var3 = e.getPacket();
      if (var3 instanceof EntityStatusS2CPacket) {
         EntityStatusS2CPacket status = (EntityStatusS2CPacket)var3;
         if (status.getStatus() == 35) {
            if (status.getEntity(mc.world) != MinecraftClient.getInstance().player) {
               return;
            }

            this.cooldownTicks = 5;
         }
      }

   }

   @Native
   private void swapToOffhand(Slot slot) {
      PlayerInventoryUtil.swapHand(slot, Hand.OFF_HAND, false);
      PlayerInventoryUtil.closeScreen(true);
   }

   @Native
   private boolean shouldUseTotem() {
      if (!mc.player.isInCreativeMode() && !mc.player.isSpectator()) {
         float healthValue = mc.player.getHealth() + mc.player.getAbsorptionAmount();
         if (healthValue <= this.health.getCurrent()) {
            return true;
         } else if (this.fall.isEnabled() && mc.player.fallDistance >= this.fallDistance.getCurrent() && !mc.player.isGliding()) {
            return true;
         } else if (this.elytra.isEnabled() && mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA && healthValue <= this.elytraHealth.getCurrent()) {
            return true;
         } else {
            Iterator var2 = mc.world.getEntities().iterator();

            EndCrystalEntity crystal;
            do {
               do {
                  do {
                     do {
                        Entity entity;
                        do {
                           if (!var2.hasNext()) {
                              return false;
                           }

                           entity = (Entity)var2.next();
                        } while(!(entity instanceof EndCrystalEntity));

                        crystal = (EndCrystalEntity)entity;
                     } while(!this.crystals.isEnabled());
                  } while(!(mc.player.getEyePos().distanceTo(crystal.getBoundingBox().getCenter()) <= 5.0D));
               } while(!(mc.player.getY() >= crystal.getY()) && !(SimulatedPlayer.simulateLocalPlayer(1).pos.getY() >= crystal.getY()));
            } while(mc.player.getOffHandStack().getItem() == Items.PLAYER_HEAD && mc.player.getOffHandStack().hasEnchantments() && !(mc.player.getHealth() <= this.healthWithBall.getCurrent()));

            return true;
         }
      } else {
         return false;
      }
   }
}
