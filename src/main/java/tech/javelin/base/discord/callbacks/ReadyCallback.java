package tech.javelin.base.discord.callbacks;

import com.sun.jna.Callback;
import tech.javelin.base.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
   void apply(DiscordUser var1);
}
