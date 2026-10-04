package tech.javelin.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.PlayerInput;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.input.EventKey;
import tech.javelin.base.events.impl.other.EventTickMovement;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.KeySetting;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.client.modules.impl.movement.AutoSprint;
import tech.javelin.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "ServerHelper",
   category = Category.MISC,
   description = ""
)
public final class ServerHelper extends Module {
   public static final ServerHelper INSTANCE = new ServerHelper();
   private final ModeSetting server = new ModeSetting("Сервер", new String[]{"ReallyWorld", "LonyGrief"});
   private final KeySetting antiFly = new KeySetting("Клавиша юза анти-полета", () -> {
      return this.server.is("ReallyWorld");
   });
   private boolean useAntiFly;

   @EventTarget
   private void onKey(EventKey e) {
      if (mc.currentScreen == null) {
         if (e.getAction() == 1) {
            if (e.getKeyCode() == this.antiFly.getKeyCode()) {
               this.useAntiFly = true;
            }

         }
      }
   }

   @EventTarget
   @Native
   private void onTick(EventTickMovement e) {
      if (this.useAntiFly) {
         this.useAntiFly = false;
         int slot = PlayerInventoryUtil.find((Item)Items.FIREWORK_STAR, 9, 45);
         int slotHotbar = PlayerInventoryUtil.find((Item)Items.FIREWORK_STAR, 0, 8);
         if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_STAR) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
         } else {
            boolean wasSprinting;
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

               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, slotHotbar, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
               mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, slotHotbar, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }

            if (slotHotbar == -1 && slot != -1) {
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

               mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
               mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
               mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
               if (wasSprinting) {
                  mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
               }
            }

         }
      }
   }
}
