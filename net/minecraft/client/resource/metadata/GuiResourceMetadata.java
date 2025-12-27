package net.minecraft.client.resource.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Scaling;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

@Environment(EnvType.CLIENT)
public record GuiResourceMetadata(Scaling scaling) {
   public static final GuiResourceMetadata DEFAULT;
   public static final Codec CODEC;
   public static final ResourceMetadataSerializer SERIALIZER;

   static {
      DEFAULT = new GuiResourceMetadata(Scaling.STRETCH);
      CODEC = RecordCodecBuilder.create((instance) -> instance.group(Scaling.CODEC.optionalFieldOf("scaling", Scaling.STRETCH).forGetter(GuiResourceMetadata::scaling)).apply(instance, GuiResourceMetadata::new));
      SERIALIZER = ResourceMetadataSerializer.fromCodec("gui", CODEC);
   }
}
