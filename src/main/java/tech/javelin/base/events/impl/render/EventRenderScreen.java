package tech.javelin.base.events.impl.render;

import com.darkmagician6.eventapi.events.Event;
import lombok.Generated;
import tech.javelin.utility.render.display.base.UIContext;

public class EventRenderScreen implements Event {
   private final UIContext context;

   @Generated
   public UIContext getContext() {
      return this.context;
   }

   @Generated
   public EventRenderScreen(UIContext context) {
      this.context = context;
   }
}
