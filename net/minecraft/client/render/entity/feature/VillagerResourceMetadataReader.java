package net.minecraft.client.render.entity.feature;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.JsonHelper;

@Environment(EnvType.CLIENT)
public class VillagerResourceMetadataReader implements ResourceMetadataReader {
   public VillagerResourceMetadata fromJson(JsonObject jsonObject) {
      return new VillagerResourceMetadata(VillagerResourceMetadata.HatType.from(JsonHelper.getString(jsonObject, "hat", "none")));
   }

   public String getKey() {
      return "villager";
   }

   // $FF: synthetic method
   public Object fromJson(final JsonObject json) {
      return this.fromJson(json);
   }
}
