package net.minecraft.client.texture.atlas;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UnstitchAtlasSource implements AtlasSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Identifier.CODEC.fieldOf("resource").forGetter((source) -> source.resource), Codecs.nonEmptyList(UnstitchAtlasSource.Region.CODEC.listOf()).fieldOf("regions").forGetter((source) -> source.regions), Codec.DOUBLE.optionalFieldOf("divisor_x", (double)1.0F).forGetter((source) -> source.divisorX), Codec.DOUBLE.optionalFieldOf("divisor_y", (double)1.0F).forGetter((source) -> source.divisorY)).apply(instance, UnstitchAtlasSource::new));
   private final Identifier resource;
   private final List regions;
   private final double divisorX;
   private final double divisorY;

   public UnstitchAtlasSource(Identifier resource, List regions, double divisorX, double divisorY) {
      this.resource = resource;
      this.regions = regions;
      this.divisorX = divisorX;
      this.divisorY = divisorY;
   }

   public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
      Identifier identifier = RESOURCE_FINDER.toResourcePath(this.resource);
      Optional<Resource> optional = resourceManager.getResource(identifier);
      if (optional.isPresent()) {
         AtlasSprite atlasSprite = new AtlasSprite(identifier, (Resource)optional.get(), this.regions.size());

         for(Region region : this.regions) {
            regions.add(region.sprite, (AtlasSource.SpriteRegion)(new SpriteRegion(atlasSprite, region, this.divisorX, this.divisorY)));
         }
      } else {
         LOGGER.warn("Missing sprite: {}", identifier);
      }

   }

   public AtlasSourceType getType() {
      return AtlasSourceManager.UNSTITCH;
   }

   @Environment(EnvType.CLIENT)
   static record Region(Identifier sprite, double x, double y, double width, double height) {
      final Identifier sprite;
      final double x;
      final double y;
      final double width;
      final double height;
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> instance.group(Identifier.CODEC.fieldOf("sprite").forGetter(Region::sprite), Codec.DOUBLE.fieldOf("x").forGetter(Region::x), Codec.DOUBLE.fieldOf("y").forGetter(Region::y), Codec.DOUBLE.fieldOf("width").forGetter(Region::width), Codec.DOUBLE.fieldOf("height").forGetter(Region::height)).apply(instance, Region::new));
   }

   @Environment(EnvType.CLIENT)
   static class SpriteRegion implements AtlasSource.SpriteRegion {
      private final AtlasSprite sprite;
      private final Region region;
      private final double divisorX;
      private final double divisorY;

      SpriteRegion(AtlasSprite sprite, Region region, double divisorX, double divisorY) {
         this.sprite = sprite;
         this.region = region;
         this.divisorX = divisorX;
         this.divisorY = divisorY;
      }

      public SpriteContents apply(SpriteOpener spriteOpener) {
         try {
            NativeImage nativeImage = this.sprite.read();
            double d = (double)nativeImage.getWidth() / this.divisorX;
            double e = (double)nativeImage.getHeight() / this.divisorY;
            int i = MathHelper.floor(this.region.x * d);
            int j = MathHelper.floor(this.region.y * e);
            int k = MathHelper.floor(this.region.width * d);
            int l = MathHelper.floor(this.region.height * e);
            NativeImage nativeImage2 = new NativeImage(NativeImage.Format.RGBA, k, l, false);
            nativeImage.copyRect(nativeImage2, i, j, 0, 0, k, l, false, false);
            SpriteContents var12 = new SpriteContents(this.region.sprite, new SpriteDimensions(k, l), nativeImage2, ResourceMetadata.NONE);
            return var12;
         } catch (Exception exception) {
            UnstitchAtlasSource.LOGGER.error("Failed to unstitch region {}", this.region.sprite, exception);
         } finally {
            this.sprite.close();
         }

         return MissingSprite.createSpriteContents();
      }

      public void close() {
         this.sprite.close();
      }

      // $FF: synthetic method
      public Object apply(final Object opener) {
         return this.apply((SpriteOpener)opener);
      }
   }
}
