package net.minecraft.client.texture.atlas;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Abgr;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class PalettedPermutationsAtlasSource implements AtlasSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.list(Identifier.CODEC).fieldOf("textures").forGetter((source) -> source.textures), Identifier.CODEC.fieldOf("palette_key").forGetter((source) -> source.paletteKey), Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("permutations").forGetter((source) -> source.permutations)).apply(instance, PalettedPermutationsAtlasSource::new));
   private final List textures;
   private final Map permutations;
   private final Identifier paletteKey;

   private PalettedPermutationsAtlasSource(List textures, Identifier paletteKey, Map permutations) {
      this.textures = textures;
      this.permutations = permutations;
      this.paletteKey = paletteKey;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      Supplier<int[]> supplier = Suppliers.memoize(() -> open(resourceManager, this.paletteKey));
      Map<String, Supplier<IntUnaryOperator>> map = new HashMap();
      this.permutations.forEach((key, texture) -> map.put(key, Suppliers.memoize(() -> toMapper((int[])supplier.get(), open(resourceManager, texture)))));

      for(Identifier identifier : this.textures) {
         Identifier identifier2 = RESOURCE_FINDER.toResourcePath(identifier);
         Optional<Resource> optional = resourceManager.getResource(identifier2);
         if (optional.isEmpty()) {
            LOGGER.warn("Unable to find texture {}", identifier2);
         } else {
            AtlasSprite atlasSprite = new AtlasSprite(identifier2, (Resource)optional.get(), map.size());

            for(Map.Entry entry : map.entrySet()) {
               Identifier identifier3 = identifier.withSuffixedPath("_" + (String)entry.getKey());
               regions.add(identifier3, (AtlasSource.SpriteRegion)(new PalettedSpriteRegion(atlasSprite, (Supplier)entry.getValue(), identifier3)));
            }
         }
      }

   }

   private static IntUnaryOperator toMapper(int[] from, int[] to) {
      if (to.length != from.length) {
         LOGGER.warn("Palette mapping has different sizes: {} and {}", from.length, to.length);
         throw new IllegalArgumentException();
      } else {
         Int2IntMap int2IntMap = new Int2IntOpenHashMap(to.length);

         for(int i = 0; i < from.length; ++i) {
            int j = from[i];
            if (Abgr.getAlpha(j) != 0) {
               int2IntMap.put(Abgr.getBgr(j), to[i]);
            }
         }

         return (color) -> {
            int i = Abgr.getAlpha(color);
            if (i == 0) {
               return color;
            } else {
               int j = Abgr.getBgr(color);
               int k = int2IntMap.getOrDefault(j, Abgr.toOpaque(j));
               int l = Abgr.getAlpha(k);
               return Abgr.withAlpha(i * l / 255, k);
            }
         };
      }
   }

   public static int[] open(ResourceManager resourceManager, Identifier texture) {
      Optional<Resource> optional = resourceManager.getResource(RESOURCE_FINDER.toResourcePath(texture));
      if (optional.isEmpty()) {
         LOGGER.error("Failed to load palette image {}", texture);
         throw new IllegalArgumentException();
      } else {
         try {
            InputStream inputStream = ((Resource)optional.get()).getInputStream();

            int[] var5;
            try (NativeImage nativeImage = NativeImage.read(inputStream)) {
               var5 = nativeImage.copyPixelsRgba();
            } catch (Throwable var10) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return var5;
         } catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", texture, exception);
            throw new IllegalArgumentException();
         }
      }
   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.PALETTED_PERMUTATIONS;
   }

   @Environment(EnvType.CLIENT)
   static record PalettedSpriteRegion(AtlasSprite baseImage, Supplier palette, Identifier permutationLocation) implements AtlasSource.SpriteRegion {
      @Nullable
      public SpriteContents apply(SpriteOpener spriteOpener) {
         SpriteContents var3;
         try {
            NativeImage nativeImage = this.baseImage.read().applyToCopy((IntUnaryOperator)this.palette.get());
            var3 = new SpriteContents(this.permutationLocation, new SpriteDimensions(nativeImage.getWidth(), nativeImage.getHeight()), nativeImage, ResourceMetadata.NONE);
            return var3;
         } catch (IllegalArgumentException | IOException exception) {
            PalettedPermutationsAtlasSource.LOGGER.error("unable to apply palette to {}", this.permutationLocation, exception);
            var3 = null;
         } finally {
            this.baseImage.close();
         }

         return var3;
      }

      public void close() {
         this.baseImage.close();
      }

      // $FF: synthetic method
      @Nullable
      public Object apply(final Object opener) {
         return this.apply((SpriteOpener)opener);
      }
   }
}
