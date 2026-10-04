package tech.javelin.client.modules.impl.misc;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Iterator;
import java.util.Locale;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.events.impl.server.EventPacket;
import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;
import tech.javelin.client.modules.api.setting.impl.BooleanSetting;

@ModuleAnnotation(
   name = "AutoAccept",
   category = Category.MISC,
   description = "Автоматически принимает телепортацию"
)
public final class AutoAccept extends Module {
   public static final AutoAccept INSTANCE = new AutoAccept();
   private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

   private AutoAccept() {
   }

   @EventTarget
   @Native
   public void onPacket(EventPacket event) {
      if (mc.player != null && mc.world != null) {
         if (event.isReceive()) {
            Packet var3 = event.getPacket();
            if (var3 instanceof GameMessageS2CPacket) {
               GameMessageS2CPacket packet = (GameMessageS2CPacket)var3;
               String raw = packet.content().getString().toLowerCase(Locale.ROOT);
               if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                  if (this.onlyFriend.isEnabled()) {
                     boolean yes = false;
                     Iterator var5 = Javelin.getInstance().getFriendManager().getItems().iterator();

                     while(var5.hasNext()) {
                        String friend = (String)var5.next();
                        if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                           yes = true;
                           break;
                        }
                     }

                     if (!yes) {
                        return;
                     }
                  }

                  mc.player.networkHandler.sendChatCommand("tpaccept");
               }
            }

         }
      }
   }
}
