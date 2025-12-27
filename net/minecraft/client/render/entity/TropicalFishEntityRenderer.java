package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.TropicalFishColorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class TropicalFishEntityRenderer extends MobEntityRenderer {
   private final TintableCompositeModel smallModel = (TintableCompositeModel)this.getModel();
   private final TintableCompositeModel largeModel;
   private static final Identifier A_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a.png");
   private static final Identifier B_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b.png");

   public TropicalFishEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new SmallTropicalFishEntityModel(context.getPart(EntityModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
      this.largeModel = new LargeTropicalFishEntityModel(context.getPart(EntityModelLayers.TROPICAL_FISH_LARGE));
      this.addFeature(new TropicalFishColorFeatureRenderer(this, context.getModelLoader()));
   }

   public Identifier getTexture(TropicalFishEntity tropicalFishEntity) {
      Identifier var10000;
      switch (tropicalFishEntity.getVariant().getSize()) {
         case SMALL -> var10000 = A_TEXTURE;
         case LARGE -> var10000 = B_TEXTURE;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public void render(TropicalFishEntity tropicalFishEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      TintableCompositeModel var10000;
      switch (tropicalFishEntity.getVariant().getSize()) {
         case SMALL -> var10000 = this.smallModel;
         case LARGE -> var10000 = this.largeModel;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      TintableCompositeModel<TropicalFishEntity> tintableCompositeModel = var10000;
      this.model = tintableCompositeModel;
      tintableCompositeModel.setColorMultiplier(tropicalFishEntity.getBaseColorComponents().getEntityColor());
      super.render(tropicalFishEntity, f, g, matrixStack, vertexConsumerProvider, i);
      tintableCompositeModel.setColorMultiplier(-1);
   }

   protected void setupTransforms(TropicalFishEntity tropicalFishEntity, MatrixStack matrixStack, float f, float g, float h, float i) {
      super.setupTransforms(tropicalFishEntity, matrixStack, f, g, h, i);
      float j = 4.3F * MathHelper.sin(0.6F * f);
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
      if (!tropicalFishEntity.isTouchingWater()) {
         matrixStack.translate(0.2F, 0.1F, 0.0F);
         matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
      }

   }
}
