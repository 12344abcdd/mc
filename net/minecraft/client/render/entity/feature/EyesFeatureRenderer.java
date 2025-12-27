package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EyesFeatureRenderer extends FeatureRenderer {
   public EyesFeatureRenderer(FeatureRendererContext featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.getEyesTexture());
      this.getContextModel().render(matrices, vertexConsumer, 15728640, OverlayTexture.DEFAULT_UV);
   }

   public abstract RenderLayer getEyesTexture();
}
