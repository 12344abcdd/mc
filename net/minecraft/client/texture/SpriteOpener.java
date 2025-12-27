package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface SpriteOpener {
   Logger LOGGER = LogUtils.getLogger();

   static SpriteOpener create(Collection metadatas) {
      return (id, resource) -> {
         ResourceMetadata resourceMetadata;
         try {
            resourceMetadata = resource.getMetadata().copy(metadatas);
         } catch (Exception exception) {
            LOGGER.error("Unable to parse metadata from {}", id, exception);
            return null;
         }

         NativeImage nativeImage;
         try {
            InputStream inputStream = resource.getInputStream();

            try {
               nativeImage = NativeImage.read(inputStream);
            } catch (Throwable var10) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var8) {
                     var10.addSuppressed(var8);
                  }
               }

               throw var10;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         } catch (IOException iOException) {
            LOGGER.error("Using missing texture, unable to load {}", id, iOException);
            return null;
         }

         AnimationResourceMetadata animationResourceMetadata = (AnimationResourceMetadata)resourceMetadata.decode(AnimationResourceMetadata.READER).orElse(AnimationResourceMetadata.EMPTY);
         SpriteDimensions spriteDimensions = animationResourceMetadata.getSize(nativeImage.getWidth(), nativeImage.getHeight());
         if (MathHelper.isMultipleOf(nativeImage.getWidth(), spriteDimensions.width()) && MathHelper.isMultipleOf(nativeImage.getHeight(), spriteDimensions.height())) {
            return new SpriteContents(id, spriteDimensions, nativeImage, resourceMetadata);
         } else {
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", new Object[]{id, nativeImage.getWidth(), nativeImage.getHeight(), spriteDimensions.width(), spriteDimensions.height()});
            nativeImage.close();
            return null;
         }
      };
   }

   @Nullable
   SpriteContents loadSprite(Identifier id, Resource resource);
}
