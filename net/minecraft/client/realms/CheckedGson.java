package net.minecraft.client.realms;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CheckedGson {
   private final Gson GSON = new Gson();

   public String toJson(RealmsSerializable serializable) {
      return this.GSON.toJson(serializable);
   }

   public String toJson(JsonElement json) {
      return this.GSON.toJson(json);
   }

   @Nullable
   public RealmsSerializable fromJson(String json, Class type) {
      return (RealmsSerializable)this.GSON.fromJson(json, type);
   }
}
