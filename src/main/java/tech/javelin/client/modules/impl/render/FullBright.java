package tech.javelin.client.modules.impl.render;

import tech.javelin.client.modules.api.Category;
import tech.javelin.client.modules.api.Module;
import tech.javelin.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(
   name = "FullBright",
   category = Category.RENDER,
   description = "Максимальное освещение"
)
public class FullBright extends Module {
   public static final FullBright INSTANCE = new FullBright();
}
