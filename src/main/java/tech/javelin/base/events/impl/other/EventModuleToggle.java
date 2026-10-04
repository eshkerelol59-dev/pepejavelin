package tech.javelin.base.events.impl.other;

import com.darkmagician6.eventapi.events.Event;
import lombok.Generated;
import tech.javelin.client.modules.api.Module;

public class EventModuleToggle implements Event {
   private final Module module;
   private final boolean enabled;

   @Generated
   public EventModuleToggle(Module module, boolean enabled) {
      this.module = module;
      this.enabled = enabled;
   }

   @Generated
   public Module getModule() {
      return this.module;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }
}
