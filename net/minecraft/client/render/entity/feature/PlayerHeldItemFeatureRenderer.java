package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PlayerHeldItemFeatureRenderer extends HeldItemFeatureRenderer {
   private final HeldItemRenderer playerHeldItemRenderer;
   private static final float HEAD_YAW = (-(float)Math.PI / 6F);
   private static final float HEAD_ROLL = ((float)Math.PI / 2F);

   public PlayerHeldItemFeatureRenderer(FeatureRendererContext featureRendererContext, HeldItemRenderer heldItemRenderer) {
      super(featureRendererContext, heldItemRenderer);
      this.playerHeldItemRenderer = heldItemRenderer;
   }

   protected void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode transformationMode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (stack.isOf(Items.SPYGLASS) && entity.getActiveItem() == stack && entity.handSwingTicks == 0) {
         this.renderSpyglass(entity, stack, arm, matrices, vertexConsumers, light);
      } else {
         super.renderItem(entity, stack, transformationMode, arm, matrices, vertexConsumers, light);
      }

   }

   private void renderSpyglass(LivingEntity entity, ItemStack stack, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      matrices.push();
      ModelPart modelPart = ((ModelWithHead)this.getContextModel()).getHead();
      float f = modelPart.pitch;
      modelPart.pitch = MathHelper.clamp(modelPart.pitch, (-(float)Math.PI / 6F), ((float)Math.PI / 2F));
      modelPart.rotate(matrices);
      modelPart.pitch = f;
      HeadFeatureRenderer.translate(matrices, false);
      boolean bl = arm == Arm.LEFT;
      matrices.translate((bl ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
      this.playerHeldItemRenderer.renderItem(entity, stack, ModelTransformationMode.HEAD, false, matrices, vertexConsumers, light);
      matrices.pop();
   }
}
