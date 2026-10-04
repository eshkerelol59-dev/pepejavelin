package tech.javelin.base.events.impl.player;

import lombok.Generated;
import net.minecraft.entity.Entity;
import tech.javelin.base.events.callables.EventCancellable;

public final class EventAttack extends EventCancellable {
   private final Entity target;

   @Generated
   public Entity getTarget() {
      return this.target;
   }

   @Generated
   public EventAttack(Entity target) {
      this.target = target;
   }
}
