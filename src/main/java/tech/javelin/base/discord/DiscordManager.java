package tech.javelin.base.discord;

import java.io.IOException;
import lombok.Generated;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.discord.utils.DiscordEventHandlers;
import tech.javelin.base.discord.utils.DiscordRPC;
import tech.javelin.base.discord.utils.DiscordRichPresence;
import tech.javelin.base.discord.utils.RPCButton;
import tech.javelin.utility.render.display.BufferUtil;

public class DiscordManager {
   private final DiscordManager.DiscordDaemonThread discordDaemonThread = new DiscordManager.DiscordDaemonThread();
   private boolean running = true;
   private DiscordManager.DiscordInfo info = new DiscordManager.DiscordInfo("Unknown", "", "");
   private Identifier avatarId;

   public DiscordManager() {
      this.initRPC();
   }

   @Native
   private void initRPC() {
      try {
         DiscordEventHandlers handlers = (new DiscordEventHandlers.Builder()).ready((user) -> {
            Javelin.getInstance().getDiscordManager().setInfo(new DiscordManager.DiscordInfo(user.username, "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png", user.userId));
            Javelin.getInstance().getDiscordManager().setInfo(new DiscordManager.DiscordInfo(user.username, "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png", user.userId));
            DiscordRichPresence richPresence = (new DiscordRichPresence.Builder()).setStartTimestamp(System.currentTimeMillis() / 1000L).setDetails("USER » vorkis").setState("UID » 1").setLargeImage("logo").setButtons(RPCButton.create("Buy Client", "https://javelinclient.fun"), RPCButton.create("Discord", "https://discord.gg/hYgEF3gYzX")).build();
            DiscordRPC.INSTANCE.Discord_UpdatePresence(richPresence);
         }).build();
         DiscordRPC.INSTANCE.Discord_Initialize("1375824160957403177", handlers, true, "");
         this.discordDaemonThread.start();
      } catch (Exception var2) {
         this.running = false;
      }

   }

   @Native
   public void stopRPC() {
      try {
         DiscordRPC.INSTANCE.Discord_Shutdown();
      } catch (Exception var2) {
      }

      this.running = false;
   }

   @Native
   public void load() throws IOException {
      if (this.avatarId == null && !this.info.avatarUrl.isEmpty()) {
         this.avatarId = BufferUtil.registerDynamicTexture("avatar-", BufferUtil.getHeadFromURL(this.info.avatarUrl));
      }

   }

   @Generated
   public void setRunning(boolean running) {
      this.running = running;
   }

   @Generated
   public void setInfo(DiscordManager.DiscordInfo info) {
      this.info = info;
   }

   @Generated
   public void setAvatarId(Identifier avatarId) {
      this.avatarId = avatarId;
   }

   @Generated
   public DiscordManager.DiscordDaemonThread getDiscordDaemonThread() {
      return this.discordDaemonThread;
   }

   @Generated
   public boolean isRunning() {
      return this.running;
   }

   @Generated
   public DiscordManager.DiscordInfo getInfo() {
      return this.info;
   }

   @Generated
   public Identifier getAvatarId() {
      return this.avatarId;
   }

   private class DiscordDaemonThread extends Thread {
      @Native
      public void run() {
         this.setName("Discord-RPC");

         try {
            while(Javelin.getInstance().getDiscordManager().isRunning()) {
               DiscordRPC.INSTANCE.Discord_RunCallbacks();
               DiscordManager.this.load();
               Thread.sleep(15000L);
            }
         } catch (Exception var2) {
            DiscordManager.this.stopRPC();
         }

         super.run();
      }
   }

   public static record DiscordInfo(String userName, String avatarUrl, String userId) {
      public DiscordInfo(String userName, String avatarUrl, String userId) {
         this.userName = userName;
         this.avatarUrl = avatarUrl;
         this.userId = userId;
      }

      public String userName() {
         return this.userName;
      }

      public String avatarUrl() {
         return this.avatarUrl;
      }

      public String userId() {
         return this.userId;
      }
   }
}
