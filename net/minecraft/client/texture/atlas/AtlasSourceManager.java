package net.minecraft.client.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AtlasSourceManager {
   private static final BiMap SOURCE_TYPE_BY_ID = HashBiMap.create();
   public static final AtlasSourceType SINGLE;
   public static final AtlasSourceType DIRECTORY;
   public static final AtlasSourceType FILTER;
   public static final AtlasSourceType UNSTITCH;
   public static final AtlasSourceType PALETTED_PERMUTATIONS;
   public static Codec CODEC;
   public static Codec TYPE_CODEC;
   public static Codec LIST_CODEC;

   private static AtlasSourceType register(String id, MapCodec codec) {
      AtlasSourceType atlasSourceType = new AtlasSourceType(codec);
      Identifier identifier = Identifier.ofVanilla(id);
      AtlasSourceType atlasSourceType2 = (AtlasSourceType)SOURCE_TYPE_BY_ID.putIfAbsent(identifier, atlasSourceType);
      if (atlasSourceType2 != null) {
         throw new IllegalStateException("Duplicate registration " + String.valueOf(identifier));
      } else {
         return atlasSourceType;
      }
   }

   static {
      SINGLE = register("single", SingleAtlasSource.CODEC);
      DIRECTORY = register("directory", DirectoryAtlasSource.CODEC);
      FILTER = register("filter", FilterAtlasSource.CODEC);
      UNSTITCH = register("unstitch", UnstitchAtlasSource.CODEC);
      PALETTED_PERMUTATIONS = register("paletted_permutations", PalettedPermutationsAtlasSource.CODEC);
      CODEC = Identifier.CODEC.flatXmap((id) -> {
         AtlasSourceType atlasSourceType = (AtlasSourceType)SOURCE_TYPE_BY_ID.get(id);
         return atlasSourceType != null ? DataResult.success(atlasSourceType) : DataResult.error(() -> "Unknown type " + String.valueOf(id));
      }, (type) -> {
         Identifier identifier = (Identifier)SOURCE_TYPE_BY_ID.inverse().get(type);
         return type != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown type " + String.valueOf(identifier));
      });
      TYPE_CODEC = CODEC.dispatch(AtlasSource::getType, AtlasSourceType::codec);
      LIST_CODEC = TYPE_CODEC.listOf().fieldOf("sources").codec();
   }
}
