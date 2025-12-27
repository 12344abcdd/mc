package net.minecraft.client.font;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;

@Environment(EnvType.CLIENT)
public enum FontFilterType implements StringIdentifiable {
   UNIFORM("uniform"),
   JAPANESE_VARIANTS("jp");

   public static final Codec CODEC = StringIdentifiable.createCodec(FontFilterType::values);
   private final String id;

   private FontFilterType(final String id) {
      this.id = id;
   }

   public String asString() {
      return this.id;
   }

   // $FF: synthetic method
   private static FontFilterType[] method_57030() {
      return new FontFilterType[]{UNIFORM, JAPANESE_VARIANTS};
   }

   @Environment(EnvType.CLIENT)
   public static class FilterMap {
      private final Map activeFilters;
      public static final Codec CODEC;
      public static final FilterMap NO_FILTER;

      public FilterMap(Map activeFilters) {
         this.activeFilters = activeFilters;
      }

      public boolean isAllowed(Set activeFilters) {
         for(Map.Entry entry : this.activeFilters.entrySet()) {
            if (activeFilters.contains(entry.getKey()) != (Boolean)entry.getValue()) {
               return false;
            }
         }

         return true;
      }

      public FilterMap apply(FilterMap activeFilters) {
         Map<FontFilterType, Boolean> map = new HashMap(activeFilters.activeFilters);
         map.putAll(this.activeFilters);
         return new FilterMap(Map.copyOf(map));
      }

      static {
         CODEC = Codec.unboundedMap(FontFilterType.CODEC, Codec.BOOL).xmap(FilterMap::new, (filterMap) -> filterMap.activeFilters);
         NO_FILTER = new FilterMap(Map.of());
      }
   }
}
