package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SaddleFeatureRenderer extends FeatureRenderer {
   private final Identifier texture;
   private final EntityModel model;

   public SaddleFeatureRenderer(FeatureRendererContext context, EntityModel model, Identifier texture) {
      super(context);
      this.model = model;
      this.texture = texture;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
      if (((Saddleable)entity).isSaddled()) {
         this.getContextModel().copyStateTo(this.model);
         this.model.animateModel(entity, limbAngle, limbDistance, tickDelta);
         this.model.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(this.texture));
         this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
      }
   }
}
