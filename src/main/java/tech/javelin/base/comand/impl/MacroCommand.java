package tech.javelin.base.comand.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.nexusguard.protection.annotations.Native;
import tech.javelin.Javelin;
import tech.javelin.base.comand.api.CommandAbstract;
import tech.javelin.base.comand.impl.args.CommandArgumentType;
import tech.javelin.base.comand.impl.args.MacroArgumentType;
import tech.javelin.base.comand.impl.args.MacroRemoveArgumentType;
import tech.javelin.base.macro.Macro;
import tech.javelin.utility.game.other.MessageUtil;
import tech.javelin.utility.render.display.Keyboard;

public class MacroCommand extends CommandAbstract {
   public MacroCommand() {
      super("macro");
   }

   @Native
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("add").then(arg("bind", MacroArgumentType.create()).then(arg("text", CommandArgumentType.create()).executes((context) -> {
         String bind = (String)context.getArgument("bind", String.class);
         String text = (String)context.getArgument("text", String.class);
         String var10000;
         if (Keyboard.getKeyCode(bind) != -1) {
            var10000 = String.valueOf(Formatting.GRAY);
            MessageUtil.displayInfo(var10000 + "Для клавиши " + String.valueOf(Formatting.WHITE) + bind.toUpperCase() + String.valueOf(Formatting.GRAY) + " добавлен макрос с текстом " + String.valueOf(Formatting.WHITE) + text);
            Javelin.getInstance().getMacroManager().add(new Macro(Keyboard.getKeyCode(bind), text));
         } else {
            var10000 = String.valueOf(Formatting.GRAY);
            MessageUtil.displayInfo(var10000 + "Клавиша " + String.valueOf(Formatting.WHITE) + bind.toUpperCase() + String.valueOf(Formatting.GRAY) + " не найдена");
         }

         return 1;
      }))));
      builder.then(literal("remove").then(arg("bind", MacroRemoveArgumentType.create()).executes((context) -> {
         String bind = (String)context.getArgument("bind", String.class);
         String var10000;
         if (Javelin.getInstance().getMacroManager().getItems().stream().anyMatch((macro) -> {
            return macro.getBind() == Keyboard.getKeyCode(bind);
         })) {
            if (Keyboard.getKeyCode(bind) != -1) {
               var10000 = String.valueOf(Formatting.GRAY);
               MessageUtil.displayInfo(var10000 + "С клавиши " + String.valueOf(Formatting.WHITE) + bind.toUpperCase() + String.valueOf(Formatting.GRAY) + " удален макрос");
               Javelin.getInstance().getMacroManager().getItems().stream().filter((macro) -> {
                  return macro.getBind() == Keyboard.getKeyCode(bind);
               }).forEach((macro) -> {
                  Javelin.getInstance().getMacroManager().removeMacro(macro);
               });
            } else {
               var10000 = String.valueOf(Formatting.GRAY);
               MessageUtil.displayInfo(var10000 + "Клавиша " + String.valueOf(Formatting.WHITE) + bind.toUpperCase() + String.valueOf(Formatting.GRAY) + " не найдена");
            }
         } else {
            var10000 = String.valueOf(Formatting.GRAY);
            MessageUtil.displayInfo(var10000 + "Макроса привязанного к клавише " + String.valueOf(Formatting.WHITE) + bind + String.valueOf(Formatting.GRAY) + " не существует");
         }

         return 1;
      })));
      builder.then(literal("list").executes((commandContext) -> {
         StringBuilder stringBuilder = new StringBuilder();
         Iterator var2 = Javelin.getInstance().getMacroManager().getItems().iterator();

         while(var2.hasNext()) {
            Macro macro = (Macro)var2.next();
            String var10001 = String.valueOf(Formatting.GRAY);
            stringBuilder.append("\n" + var10001 + macro.getText()).append(String.valueOf(Formatting.WHITE) + " [").append(Keyboard.getKeyName(macro.getBind())).append("]");
         }

         if (stringBuilder.isEmpty()) {
            MessageUtil.displayInfo(String.valueOf(Formatting.GRAY) + "Нема");
         } else {
            MessageUtil.displayInfo(stringBuilder);
         }

         return 1;
      }));
   }
}
