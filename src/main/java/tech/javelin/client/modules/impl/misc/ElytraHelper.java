package tech.javelin.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.PlayerInput;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.input.EventKey;
import tech.javelin.base.events.impl.other.EventTick;
import tech.javelin.base.events.impl.other.EventTickMovement;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.KeySetting;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.impl.movement.AutoSprint;
import tech.javelin.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "ElytraHelper",
   description = "Помощник для элитр",
   category = Category.MISC
)
public final class ElytraHelper extends Module {
   public static final ElytraHelper INSTANCE = new ElytraHelper();
   private final KeySetting elytraSetting = new KeySetting("Кнопка свапа");
   private final KeySetting fireworkSetting = new KeySetting("Кнопка фейерверка");
   private final ModeSetting mode = new ModeSetting("Мод пуска феера", new String[]{"Хвх", "Легит"});
   private boolean swap;
   private boolean useFirework;

   @EventTarget
   public void onKey(EventKey e) {
      if (e.isKeyDown(this.elytraSetting.getKeyCode())) {
         this.swap = true;
      } else if (e.isKeyDown(this.fireworkSetting.getKeyCode()) && mc.player.isGliding()) {
         this.useFirework = true;
      }

   }

   @EventTarget
   @Native
   private void onTick(EventTick e) {
      if (this.swap) {
         int slotHotbar;
         boolean wasSprinting;
         int slotInventory;
         if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            slotHotbar = PlayerInventoryUtil.find((List)List.of(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE), 0, 8);
            if (slotHotbar != -1) {
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

               mc.interactionManager.clickSlot(0, 6, slotHotbar, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }

            slotInventory = PlayerInventoryUtil.find((List)List.of(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE), 0, 8);
            if (slotHotbar == -1 && slotInventory != -1) {
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

               mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
               mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, mc.player);
               mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }
         } else {
            slotHotbar = PlayerInventoryUtil.find((Item)Items.ELYTRA, 0, 8);
            if (slotHotbar != -1) {
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

               mc.interactionManager.clickSlot(0, 6, slotHotbar, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }

            slotInventory = PlayerInventoryUtil.find((Item)Items.ELYTRA, 0, 8);
            if (slotHotbar == -1 && slotInventory != -1) {
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

               mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
               mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, mc.player);
               mc.interactionManager.clickSlot(0, slotInventory, 8, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }
         }

         this.swap = false;
      }
   }

   @EventTarget
   @Native
   private void onTickMovement(EventTickMovement e) {
      if (this.useFirework) {
         if (this.mode.is("Хвх")) {
            PlayerInventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
         } else {
            PlayerInventoryUtil.swapAndUseLegit(Items.FIREWORK_ROCKET);
         }

         this.useFirework = false;
      }
   }

   private Slot chestPlate() {
      return ((ClientPlayerEntity)Objects.requireNonNull(mc.player)).getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) ? PlayerInventoryUtil.getSlot(List.of(Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE)) : PlayerInventoryUtil.getSlot(Items.ELYTRA);
   }
}
