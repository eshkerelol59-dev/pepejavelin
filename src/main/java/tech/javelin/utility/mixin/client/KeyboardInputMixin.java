package tech.javelin.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.javelin.base.events.impl.player.EventMoveInput;

@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin extends Input {
   @Shadow
   @Final
   private GameOptions field_3902;

   @Unique
   private float abobaGetMovementMultiplier(boolean positive, boolean negative) {
      if (positive == negative) {
         return 0.0F;
      } else {
         return positive ? 1.0F : -1.0F;
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At(
   value = "FIELD",
   target = "Lnet/minecraft/client/input/KeyboardInput;playerInput:Lnet/minecraft/util/PlayerInput;",
   ordinal = 0,
   shift = Shift.AFTER
)},
      cancellable = true
   )
   public void injectInputEvent(CallbackInfo ci) {
      EventMoveInput event = new EventMoveInput(this.playerInput, this.abobaGetMovementMultiplier(this.playerInput.forward(), this.playerInput.backward()), this.abobaGetMovementMultiplier(this.playerInput.left(), this.playerInput.right()));
      EventManager.call(event);
      if (!event.isCancelled()) {
         this.movementForward = event.getForward();
         this.movementSideways = event.getStrafe();
         this.playerInput = new PlayerInput(this.movementForward > 0.0F, this.movementForward < 0.0F, this.movementSideways > 0.0F, this.movementSideways < 0.0F, this.field_3902.jumpKey.isPressed(), this.field_3902.sneakKey.isPressed(), this.field_3902.sprintKey.isPressed());
         ci.cancel();
      }
   }
}
