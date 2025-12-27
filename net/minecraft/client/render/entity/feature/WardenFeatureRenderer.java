package net.minecraft.client.render.entity.feature;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.WardenEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class WardenFeatureRenderer extends FeatureRenderer {
   private final Identifier texture;
   private final AnimationAngleAdjuster animationAngleAdjuster;
   private final ModelPartVisibility modelPartVisibility;

   public WardenFeatureRenderer(FeatureRendererContext context, Identifier texture, AnimationAngleAdjuster animationAngleAdjuster, ModelPartVisibility modelPartVisibility) {
      super(context);
      this.texture = texture;
      this.animationAngleAdjuster = animationAngleAdjuster;
      this.modelPartVisibility = modelPartVisibility;
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, WardenEntity wardenEntity, float f, float g, float h, float j, float k, float l) {
      if (!wardenEntity.isInvisible()) {
         this.updateModelPartVisibility();
         VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucentEmissive(this.texture));
         float m = this.animationAngleAdjuster.apply(wardenEntity, h, j);
         int n = Argb.getArgb(MathHelper.floor(m * 255.0F), 255, 255, 255);
         ((WardenEntityModel)this.getContextModel()).render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(wardenEntity, 0.0F), n);
         this.unhideAllModelParts();
      }
   }

   private void updateModelPartVisibility() {
      List<ModelPart> list = this.modelPartVisibility.getPartsToDraw((WardenEntityModel)this.getContextModel());
      ((WardenEntityModel)this.getContextModel()).getPart().traverse().forEach((part) -> part.hidden = true);
      list.forEach((part) -> part.hidden = false);
   }

   private void unhideAllModelParts() {
      ((WardenEntityModel)this.getContextModel()).getPart().traverse().forEach((part) -> part.hidden = false);
   }

   @Environment(EnvType.CLIENT)
   public interface AnimationAngleAdjuster {
      float apply(WardenEntity warden, float tickDelta, float animationProgress);
   }

   @Environment(EnvType.CLIENT)
   public interface ModelPartVisibility {
      List getPartsToDraw(EntityModel model);
   }
}
