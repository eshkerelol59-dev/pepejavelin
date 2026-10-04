package tech.javelin.client.modules.impl.player;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.block.Block;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "AutoTool",
   category = Category.PLAYER,
   description = "При копании берет лучший предмет"
)
public final class AutoTool extends Module {
   public static final AutoTool INSTANCE = new AutoTool();
   private int previousSlot = -1;

   private AutoTool() {
   }

   @EventTarget
   @Native
   public void onUpdate(EventUpdate event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && !mc.player.isCreative()) {
         if (mc.interactionManager.isBreakingBlock() && this.previousSlot == -1) {
            this.previousSlot = mc.player.getInventory().selectedSlot;
         }

         if (mc.interactionManager.isBreakingBlock()) {
            int toolSlot = this.findOptimalTool();
            if (toolSlot != -1) {
               mc.player.getInventory().selectedSlot = toolSlot;
            }
         } else if (this.previousSlot != -1) {
            mc.player.getInventory().selectedSlot = this.previousSlot;
            this.previousSlot = -1;
         }

      } else {
         this.previousSlot = -1;
      }
   }

   private int findOptimalTool() {
      if (mc.player != null && mc.world != null) {
         HitResult var2 = mc.crosshairTarget;
         if (var2 instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult)var2;
            Block block = mc.world.getBlockState(blockHitResult.getBlockPos()).getBlock();
            return this.findTool(block);
         } else {
            return -1;
         }
      } else {
         return 0;
      }
   }

   private int findTool(Block block) {
      int bestSlot = -1;
      float bestSpeed = 1.0F;

      for(int i = 0; i < 9; ++i) {
         float speed = this.getMiningSpeed(i, block);
         if (speed > bestSpeed) {
            bestSpeed = speed;
            bestSlot = i;
         }
      }

      return bestSlot;
   }

   private float getMiningSpeed(int slot, Block block) {
      return mc.player == null ? 0.0F : mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(block.getDefaultState());
   }

   public void onDisable() {
      this.previousSlot = -1;
      super.onDisable();
   }
}
