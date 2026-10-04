package tech.javelin.client.modules.impl.combat;

import com.darkmagician6.eventapi.EventTarget;
import lombok.Generated;
import net.minecraft.item.Items;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.events.impl.other.EventTickMovement;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.utility.game.player.PlayerInventoryUtil;

@ModuleAnnotation(
   name = "ClickPearl",
   category = Category.COMBAT,
   description = "Кидает перку по бинду"
)
public final class ClickPearl extends Module {
   private final ModeSetting mode = new ModeSetting("Мод", new String[]{"Хвх", "Легит"});
   public static final ClickPearl INSTANCE = new ClickPearl();
   private boolean ignore;
   private boolean use;

   public void onEnable() {
      this.use = true;
      super.onEnable();
   }

   @EventTarget
   @Native
   private void onTick(EventTickMovement e) {
      if (this.use) {
         this.setIgnore(true);
         if (this.mode.is("Хвх")) {
            PlayerInventoryUtil.swapAndUseHvH(Items.ENDER_PEARL);
         } else {
            PlayerInventoryUtil.swapAndUseLegit(Items.ENDER_PEARL);
         }

         this.setIgnore(false);
         this.use = false;
         this.toggle();
      }
   }

   @Generated
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ClickPearl)) {
         return false;
      } else {
         ClickPearl other = (ClickPearl)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else if (this.isIgnore() != other.isIgnore()) {
            return false;
         } else if (this.isUse() != other.isUse()) {
            return false;
         } else {
            Object this$mode = this.getMode();
            Object other$mode = other.getMode();
            if (this$mode == null) {
               if (other$mode != null) {
                  return false;
               }
            } else if (!this$mode.equals(other$mode)) {
               return false;
            }

            return true;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ClickPearl;
   }

   @Generated
   public int hashCode() {
      int PRIME = 1;
      int result = super.hashCode();
      result = result * 59 + (this.isIgnore() ? 79 : 97);
      result = result * 59 + (this.isUse() ? 79 : 97);
      Object $mode = this.getMode();
      result = result * 59 + ($mode == null ? 43 : $mode.hashCode());
      return result;
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public boolean isIgnore() {
      return this.ignore;
   }

   @Generated
   public boolean isUse() {
      return this.use;
   }

   @Generated
   public void setIgnore(boolean ignore) {
      this.ignore = ignore;
   }

   @Generated
   public void setUse(boolean use) {
      this.use = use;
   }

   @Generated
   public String toString() {
      String var10000 = String.valueOf(this.getMode());
      return "ClickPearl(mode=" + var10000 + ", ignore=" + this.isIgnore() + ", use=" + this.isUse() + ")";
   }
}
