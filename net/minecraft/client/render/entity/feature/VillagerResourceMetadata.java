package net.minecraft.client.render.entity.feature;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VillagerResourceMetadata {
   public static final VillagerResourceMetadataReader READER = new VillagerResourceMetadataReader();
   public static final String KEY = "villager";
   private final HatType hatType;

   public VillagerResourceMetadata(HatType hatType) {
      this.hatType = hatType;
   }

   public HatType getHatType() {
      return this.hatType;
   }

   @Environment(EnvType.CLIENT)
   public static enum HatType {
      NONE("none"),
      PARTIAL("partial"),
      FULL("full");

      private static final Map BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(HatType::getName, (hatType) -> hatType));
      private final String name;

      private HatType(final String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static HatType from(String name) {
         return (HatType)BY_NAME.getOrDefault(name, NONE);
      }

      // $FF: synthetic method
      private static HatType[] method_36924() {
         return new HatType[]{NONE, PARTIAL, FULL};
      }
   }
}
