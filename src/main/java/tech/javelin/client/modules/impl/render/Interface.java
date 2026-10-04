package tech.javelin.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.Vector2f;
import tech.javelin.Javelin;
import tech.javelin.base.events.impl.input.EventMouse;
import tech.javelin.base.events.impl.input.EventSetScreen;
import tech.javelin.base.events.impl.other.EventWindowResize;
import tech.javelin.base.events.impl.player.EventUpdate;
import tech.javelin.base.events.impl.render.EventHudRender;
import tech.javelin.client.hud.elements.component.InformationComponent;
import tech.javelin.client.hud.elements.component.KeybindsComponent;
import tech.javelin.client.hud.elements.component.NotifyComponent;
import tech.javelin.client.hud.elements.component.PotionsComponent;
import tech.javelin.client.hud.elements.component.StaffComponent;
import tech.javelin.client.hud.elements.component.TargetHudComponent;
import tech.javelin.client.hud.elements.component.WatermarkComponent;
import tech.javelin.client.hud.elements.draggable.DraggableHudElement;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.MultiBooleanSetting;
import tech.javelin.utility.math.MathUtil;
import tech.javelin.utility.render.display.Render2DUtil;
import tech.javelin.utility.render.display.base.CustomDrawContext;
import tech.javelin.utility.render.display.base.GuiUtil;

@ModuleAnnotation(
   name = "HUD",
   category = Category.RENDER,
   description = "Интерфейс Клиента"
)
public final class Interface extends Module {
   public static final Interface INSTANCE = new Interface();
   private final MultiBooleanSetting elementsSetting = MultiBooleanSetting.create("Элементы", List.of("Ватермарка", "Эффекты", "Модераторы", "Уведомления", "Информация", "Бинды", "Таргет худ"));
   private final List<DraggableHudElement> elements = new ArrayList();
   private DraggableHudElement draggingElement = null;
   private float dragOffsetX;
   private float dragOffsetY;
   long init = 0L;

   private Interface() {
      this.addElement(new WatermarkComponent("Watermark", 0.0F, 0.0F, 960.0F, 495.5F, 10.0F, 10.0F, DraggableHudElement.Align.TOP_LEFT));
      this.addElement(new PotionsComponent("Potions", 0.0F, 0.0F, 960.0F, 495.5F, 119.15234F, 73.0F, DraggableHudElement.Align.TOP_LEFT));
      this.addElement(new StaffComponent("Staff", 0.0F, 0.0F, 960.0F, 495.5F, 10.0F, 73.0F, DraggableHudElement.Align.TOP_LEFT));
      NotifyComponent notifyComponent = new NotifyComponent("Notify", 181.80615F, 135.5F, 960.0F, 495.5F, 157.03516F, -72.5F, DraggableHudElement.Align.CENTER);
      this.addElement(notifyComponent);
      Javelin.getInstance().getNotifyManager().setNotifyComponent(notifyComponent);
      this.addElement(new InformationComponent("Information", 0.0F, 0.0F, 960.0F, 495.5F, 10.0F, 41.5F, DraggableHudElement.Align.TOP_LEFT));
      this.addElement(new KeybindsComponent("Keybinds", 349.0F, 0.0F, 960.0F, 495.5F, -122.0F, 73.0F, DraggableHudElement.Align.TOP_RIGHT));
      this.addElement(new TargetHudComponent("TargetHUD", 166.5F, 128.5F, 960.0F, 495.5F, 0.0F, 31.75F, DraggableHudElement.Align.CENTER));
   }

   public void onEnable() {
      this.init = System.currentTimeMillis();
      super.onEnable();
   }

   public JsonObject save() {
      JsonObject object = super.save();
      JsonObject propertiesObject = new JsonObject();
      Iterator var3 = this.elements.iterator();

      while(var3.hasNext()) {
         DraggableHudElement element = (DraggableHudElement)var3.next();
         propertiesObject.add(element.getName(), element.save());
      }

      object.add("HudElements", propertiesObject);
      return object;
   }

   public void load(JsonObject object) {
      super.load(object);
      if (object.has("HudElements") && object.get("HudElements").isJsonObject()) {
         JsonObject propertiesObject = object.getAsJsonObject("HudElements");
         Iterator var3 = this.elements.iterator();

         while(var3.hasNext()) {
            DraggableHudElement element = (DraggableHudElement)var3.next();
            String key = element.getName();
            if (propertiesObject.has(key) && propertiesObject.get(key).isJsonObject()) {
               element.load(propertiesObject.getAsJsonObject(key));
            }
         }
      }

   }

   private void addElement(DraggableHudElement element) {
      this.elements.add(element);
   }

   @EventTarget
   public void onRender(EventHudRender event) {
      if (!(mc.currentScreen instanceof ChatScreen) && this.draggingElement != null) {
         this.draggingElement.release();
         this.draggingElement = null;
      }

      CustomDrawContext ctx = event.getContext();
      float width = (float)mc.getWindow().getWidth() / this.getCustomScale();
      float height = (float)mc.getWindow().getHeight() / this.getCustomScale();
      if (!mc.options.hudHidden) {
         Iterator var5 = this.elements.iterator();

         while(var5.hasNext()) {
            DraggableHudElement element = (DraggableHudElement)var5.next();
            if (this.shouldRender(element)) {
               try {
                  element.render(ctx);
               } catch (Exception var10) {
                  var10.printStackTrace();
               }

               if (this.draggingElement != element && System.currentTimeMillis() - this.init < 5000L) {
                  element.windowResized(width, height);
               }
            }
         }
      }

      if (mc.currentScreen instanceof ChatScreen && this.draggingElement != null) {
         Vector2f mousePos = GuiUtil.getMouse((double)this.getCustomScale());
         double mouseX = (double)mousePos.getX();
         double mouseY = (double)mousePos.getY();
         this.draggingElement.set(ctx, (float)mouseX - this.dragOffsetX, (float)mouseY - this.dragOffsetY, this, width, height);
      }

   }

   private boolean shouldRender(DraggableHudElement element) {
      int index = this.elements.indexOf(element);
      return index >= 0 && index < this.elementsSetting.getBooleanSettings().size() ? ((MultiBooleanSetting.Value)this.elementsSetting.getBooleanSettings().get(index)).isEnabled() : false;
   }

   @EventTarget
   public void onMouse(EventMouse event) {
      if (!(mc.currentScreen instanceof ChatScreen)) {
         this.draggingElement.release();
         this.draggingElement = null;
      } else {
         Vector2f mousePos = GuiUtil.getMouse((double)this.getCustomScale());
         double mouseX = (double)mousePos.getX();
         double mouseY = (double)mousePos.getY();
         if (event.getAction() == 1 && event.getButton() == 0) {
            List<DraggableHudElement> reversedElements = new ArrayList(this.elements);
            Collections.reverse(reversedElements);
            Iterator var8 = reversedElements.iterator();

            while(var8.hasNext()) {
               DraggableHudElement element = (DraggableHudElement)var8.next();
               if (this.shouldRender(element) && element.isMouseOver(mouseX, mouseY)) {
                  this.draggingElement = element;
                  this.dragOffsetX = (float)mouseX - element.getX();
                  this.dragOffsetY = (float)mouseY - element.getY();
                  break;
               }
            }
         } else if (event.getAction() == 0) {
            this.draggingElement.release();
            this.draggingElement = null;
         }

      }
   }

   public float getCustomScale() {
      return 2.0F;
   }

   public org.joml.Vector2f getNearest(float x, float y) {
      float minDeltaX = Float.MAX_VALUE;
      float minDeltaY = Float.MAX_VALUE;
      float thoroughness = 0.0F;
      org.joml.Vector2f nearest = new org.joml.Vector2f(-1.0F, -1.0F);
      Iterator var7 = this.elements.iterator();

      float minX;
      float minY;
      float deltaX;
      float deltaY;
      while(var7.hasNext()) {
         DraggableHudElement s = (DraggableHudElement)var7.next();
         if (!s.equals(this.draggingElement)) {
            minX = s.getX();
            minY = s.getY();
            deltaX = s.getX() + s.getWidth();
            deltaY = s.getY() + s.getHeight();
            float tempXC = s.getX() + s.getWidth() / 2.0F;
            float tempYC = s.getY() + s.getHeight() / 2.0F;
            float nearestX = this.getNearest(minX, deltaX, tempXC, x);
            float nearestY = this.getNearest(minY, deltaY, tempYC, y);
            float nearestDeltaX = MathUtil.goodSubtract(nearestX, x);
            float nearestDeltaY = MathUtil.goodSubtract(nearestY, y);
            if (nearestDeltaX < minDeltaX) {
               minDeltaX = nearestDeltaX;
               if (nearestDeltaX < thoroughness) {
                  nearest.x = nearestX;
               }
            }

            if (nearestDeltaY < minDeltaY) {
               minDeltaY = nearestDeltaY;
               if (nearestDeltaY < thoroughness) {
                  nearest.y = nearestY;
               }
            }
         }
      }

      if (nearest.x == -1.0F || nearest.y == -1.0F) {
         float tempXA = (float)mc.getWindow().getScaledWidth() / 2.0F;
         float tempYA = (float)mc.getWindow().getScaledHeight() / 2.0F;
         minX = this.getNearest(tempXA, tempXA, tempXA, x);
         minY = this.getNearest(tempYA, tempYA, tempYA, y);
         deltaX = MathUtil.goodSubtract(minX, x);
         deltaY = MathUtil.goodSubtract(minY, y);
         if (deltaX < minDeltaX && deltaX < thoroughness) {
            nearest.x = minX;
         }

         if (deltaY < minDeltaY && deltaY < thoroughness) {
            nearest.y = minY;
         }
      }

      return nearest;
   }

   public float getNearest(float a, float b, float c, float target) {
      float nearest = a;
      if (MathUtil.goodSubtract(b, target) < MathUtil.goodSubtract(a, target)) {
         nearest = b;
      }

      if (MathUtil.goodSubtract(c, target) < MathUtil.goodSubtract(nearest, target)) {
         nearest = c;
      }

      return nearest;
   }

   public boolean isEnableScoreBar() {
      return false;
   }

   public boolean isEnableHotBar() {
      return false;
   }

   public boolean isEnableTab() {
      return false;
   }

   @EventTarget
   public void resize(EventWindowResize eventWindowResize) {
      float width = (float)mc.getWindow().getWidth() / this.getCustomScale();
      float height = (float)mc.getWindow().getHeight() / this.getCustomScale();
      Iterator var4 = this.elements.iterator();

      while(var4.hasNext()) {
         DraggableHudElement element = (DraggableHudElement)var4.next();
         element.windowResized(width, height);
      }

   }

   @EventTarget
   public void update(EventUpdate eventUpdate) {
      if (Render2DUtil.glowCache.size() > 400) {
         Render2DUtil.glowCache.values().removeIf((v) -> {
            if (v.tick()) {
               v.destroy();
               return true;
            } else {
               return false;
            }
         });
      }

      Iterator var2 = this.elements.iterator();

      while(var2.hasNext()) {
         DraggableHudElement draggableHudElement = (DraggableHudElement)var2.next();
         draggableHudElement.tick();
      }

   }

   @EventTarget
   public void screenEvent(EventSetScreen event) {
      if (event.getScreen() instanceof ChatScreen) {
         this.init = System.currentTimeMillis();
      }

   }

   public int getGlowRadius() {
      return 10;
   }
}
