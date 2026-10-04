package tech.javelin;

import java.io.File;
import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.base.autobuy.AutoBuyManager;
import tech.javelin.base.comand.CommandManager;
import tech.javelin.base.config.ConfigManager;
import tech.javelin.base.discord.DiscordManager;
import tech.javelin.base.filemanager.impl.FriendManager;
import tech.javelin.base.filemanager.impl.StaffManager;
import tech.javelin.base.macro.MacroManager;
import tech.javelin.base.modules.ModuleManager;
import tech.javelin.base.notify.NotifyManager;
import tech.javelin.base.repository.RCTRepository;
import tech.javelin.base.request.ScriptManager;
import tech.javelin.base.theme.ThemeManager;
import tech.javelin.base.waypoint.WaypointManager;
import tech.javelin.client.screens.menu.MenuScreen;
import tech.javelin.utility.game.server.ServerHandler;
import tech.javelin.utility.render.display.shader.DrawUtil;
import tech.javelin.utility.render.display.shader.GlProgram;

public enum Javelin implements ClientModInitializer {
   INSTANCE;

   public static final String NAME = "Javelin";
   public static final String VER = "";
   public static final String TYPE = "DEV";
   private static final String MOD_ID = "Javelin".toLowerCase();
   public static File DIRECTORY;
   private ModuleManager moduleManager;
   private ThemeManager themeManager;
   private MenuScreen menuScreen;
   private ScriptManager scriptManager;
   private ServerHandler serverHandler;
   private FriendManager friendManager;
   private MacroManager macroManager;
   private StaffManager staffManager;
   private AutoBuyManager autoBuyManager;
   private WaypointManager waypointManager;
   private NotifyManager notifyManager;
   private CommandManager commandManager;
   private ConfigManager configManager;
   private RCTRepository rctRepository;
   private DiscordManager discordManager;
   private boolean initialized = false;

   @Override
   public void onInitializeClient() {
      try {
         init();
      } catch (Exception e) {
         e.printStackTrace();
         throw e;
      }
   }

   @Native
   public void init() {
      if (initialized) {
         return;
      }
      initialized = true;
      
      try {
         DIRECTORY = new File(MinecraftClient.getInstance().runDirectory, "Javelin");
         if (!DIRECTORY.exists()) {
            DIRECTORY.mkdirs();
         }
         
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            getInstance().shutdown();
         }));
         
         this.friendManager = new FriendManager();
         this.macroManager = new MacroManager();
         this.staffManager = new StaffManager();
         this.notifyManager = new NotifyManager();
         this.serverHandler = new ServerHandler();
         this.rctRepository = new RCTRepository();
         this.themeManager = new ThemeManager();
         this.moduleManager = new ModuleManager();
         this.configManager = new ConfigManager();
         this.autoBuyManager = new AutoBuyManager();
         this.commandManager = new CommandManager();
         this.scriptManager = new ScriptManager();
         try {
            this.discordManager = new DiscordManager();
         } catch (Throwable e) {
            this.discordManager = null;
         }
         this.waypointManager = new WaypointManager();
         this.menuScreen = new MenuScreen();
         ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
               return Javelin.id("after_shader_load");
            }

            @Override
            public void reload(ResourceManager manager) {
               GlProgram.loadAndSetupPrograms();
            }
         });
         DrawUtil.initializeShaders();
      } catch (Exception e) {
         e.printStackTrace();
         throw new RuntimeException("Javelin initialization failed", e);
      }
   }

   @Native
   public void shutdown() {
      this.friendManager.save();
      this.staffManager.save();
      this.configManager.save();
      this.macroManager.save();
      if (this.discordManager != null) {
         this.discordManager.stopRPC();
      }

   }

   public static Identifier id(String path) {
      return Identifier.of("javelin", path);
   }

   public static Javelin getInstance() {
      return INSTANCE;
   }

   public RCTRepository getRCTRepository() {
      return this.rctRepository;
   }

   @Generated
   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   @Generated
   public ThemeManager getThemeManager() {
      return this.themeManager;
   }

   @Generated
   public MenuScreen getMenuScreen() {
      return this.menuScreen;
   }

   @Generated
   public ScriptManager getScriptManager() {
      return this.scriptManager;
   }

   @Generated
   public ServerHandler getServerHandler() {
      return this.serverHandler;
   }

   @Generated
   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   @Generated
   public MacroManager getMacroManager() {
      return this.macroManager;
   }

   @Generated
   public StaffManager getStaffManager() {
      return this.staffManager;
   }

   @Generated
   public AutoBuyManager getAutoBuyManager() {
      return this.autoBuyManager;
   }

   @Generated
   public WaypointManager getWaypointManager() {
      return this.waypointManager;
   }

   @Generated
   public NotifyManager getNotifyManager() {
      return this.notifyManager;
   }

   @Generated
   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public DiscordManager getDiscordManager() {
      return this.discordManager;
   }


   private static Javelin[] $values() {
      return new Javelin[]{INSTANCE};
   }
}
