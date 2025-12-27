package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.WitchEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class WitchHeldItemFeatureRenderer extends VillagerHeldItemFeatureRenderer {
   public WitchHeldItemFeatureRenderer(FeatureRendererContext featureRendererContext, HeldItemRenderer heldItemRenderer) {
      super(featureRendererContext, heldItemRenderer);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l) {
      ItemStack itemStack = livingEntity.getMainHandStack();
      matrixStack.push();
      if (itemStack.isOf(Items.POTION)) {
         ((WitchEntityModel)this.getContextModel()).getHead().rotate(matrixStack);
         ((WitchEntityModel)this.getContextModel()).getNose().rotate(matrixStack);
         matrixStack.translate(0.0625F, 0.25F, 0.0F);
         matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(140.0F));
         matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(10.0F));
         matrixStack.translate(0.0F, -0.4F, 0.4F);
      }

      super.render(matrixStack, vertexConsumerProvider, i, livingEntity, f, g, h, j, k, l);
      matrixStack.pop();
   }
}
