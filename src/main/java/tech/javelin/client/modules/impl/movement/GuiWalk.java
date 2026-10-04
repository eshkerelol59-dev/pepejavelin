package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.base.events.impl.server.EventPacket;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.utility.game.player.PlayerInventoryComponent;
import tech.javelin.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "Gui Walk",
   category = Category.MOVEMENT,
   description = "Позволяет ходить в инвентаре"
)
public final class GuiWalk extends Module {
   public static final GuiWalk INSTANCE = new GuiWalk();
   private final ConcurrentLinkedQueue<ClickSlotC2SPacket> packets = new ConcurrentLinkedQueue();
   private boolean pause;
   private boolean wait1Tick;
   private int pendingRequests;
   private int waitingConfirmToSlot;

   private GuiWalk() {
   }

   @EventTarget
   @Native
   private void onPacket(EventPacket e) {
   }

   @EventTarget
   private void onTick(EventUpdate e) {
      if (!PlayerInventoryUtil.isServerScreen() && PlayerInventoryComponent.shouldSkipExecution()) {
         PlayerInventoryComponent.updateMoveKeys();
      }

   }
}
