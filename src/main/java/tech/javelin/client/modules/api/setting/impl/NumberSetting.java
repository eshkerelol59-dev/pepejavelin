package tech.javelin.client.modules.api.setting.impl;

import com.google.gson.JsonObject;
import java.util.function.Supplier;
import lombok.Generated;
import tech.javelin.client.modules.api.setting.Setting;

public class NumberSetting extends Setting {
   private final String description;
   private float current;
   private final float min;
   private final float max;
   private final float increment;
   private NumberSetting.Test edit;

   public NumberSetting(String name, float value, float min, float max, float increment, NumberSetting.Test edit) {
      super(name);
      this.min = min;
      this.max = max;
      this.current = value;
      this.increment = increment;
      this.edit = edit;
      this.description = "";
   }

   public NumberSetting(String name, float value, float min, float max, float increment, String description) {
      super(name);
      this.min = min;
      this.max = max;
      this.current = value;
      this.increment = increment;
      this.description = description;
   }

   public NumberSetting(String name, float value, float min, float max, float increment) {
      super(name);
      this.min = min;
      this.max = max;
      this.current = value;
      this.increment = increment;
      this.description = "";
   }

   public NumberSetting(String name, float value, float min, float max, float increment, Supplier<Boolean> visible) {
      super(name);
      this.min = min;
      this.max = max;
      this.current = value;
      this.increment = increment;
      this.setVisible(visible);
      this.description = "";
   }

   public void setCurrent(float current) {
      float old = this.current;
      this.current = current;
      if (this.edit != null) {
         this.edit.apply(old, current);
      }

   }

   public void safe(JsonObject propertiesObject) {
      propertiesObject.addProperty(String.valueOf(this.name), this.getCurrent());
   }

   public void load(JsonObject propertiesObject) {
      this.setCurrent(propertiesObject.get(String.valueOf(this.name)).getAsFloat());
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public float getCurrent() {
      return this.current;
   }

   @Generated
   public float getMin() {
      return this.min;
   }

   @Generated
   public float getMax() {
      return this.max;
   }

   @Generated
   public float getIncrement() {
      return this.increment;
   }

   @Generated
   public NumberSetting.Test getEdit() {
      return this.edit;
   }

   public interface Test {
      void apply(float var1, float var2);
   }
}
