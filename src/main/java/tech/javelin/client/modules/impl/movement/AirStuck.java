package tech.javelin.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import tech.javelin.base.events.impl.player.EventMove;
import tech.javelin.base.events.impl.server.EventPacket;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;
import tech.javelin.client.modules.api.setting.impl.ModeSetting;
import tech.javelin.utility.game.other.MessageUtil;

@ModuleAnnotation(
   name = "AirStuck",
   category = Category.MOVEMENT,
   description = "Замораживает игрока в воздухе"
)
public class AirStuck extends Module {
   public static final AirStuck INSTANCE = new AirStuck();
   private final ModeSetting mode = new ModeSetting("Режим", new String[0]);
   private final ModeSetting.Value normal;
   private final ModeSetting.Value lonygrief;
   private final BooleanSetting freezeSetting;
   private Vec3d freezepoziciya;
   private boolean freezeezoshka;
   private long timemessagge;

   public AirStuck() {
      this.normal = (new ModeSetting.Value(this.mode, "Обычный")).select();
      this.lonygrief = new ModeSetting.Value(this.mode, "LonyGrief");
      this.freezeSetting = new BooleanSetting("Отменять движение", false);
      this.freezepoziciya = Vec3d.ZERO;
      this.freezeezoshka = false;
      this.timemessagge = 0L;
   }

   public void onEnable() {
      this.freezeezoshka = false;
      this.timemessagge = 0L;
      if (mc.player != null && this.normal.isSelected()) {
         this.freezepoziciya = mc.player.getPos();
         this.freezeezoshka = true;
      }

      super.onEnable();
   }

   @EventTarget
   public void onMove(EventMove e) {
      if (mc.player != null) {
         if (this.lonygrief.isSelected() && !this.freezeezoshka) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.timemessagge >= 10000L) {
               MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§c Ждём момента когда начнем падать");
               this.timemessagge = currentTime;
            }

            if (mc.player.fallDistance > 0.0F && mc.player.getVelocity().y < 0.0D) {
               this.freezepoziciya = mc.player.getPos();
               this.freezeezoshka = true;
               MessageUtil.displayMessage(MessageUtil.LogLevel.INFO, "§a Начали фризиться");
            }
         }

         if (this.freezeezoshka) {
            e.setMovePos(Vec3d.ZERO);
         }

      }
   }

   @EventTarget
   public void onPacket(EventPacket e) {
      if (this.freezeezoshka && this.freezeSetting.isEnabled() && e.getPacket() instanceof PlayerMoveC2SPacket) {
         e.cancel();
      }

   }
}
