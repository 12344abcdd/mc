package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.WitherEntityModel;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class WitherArmorFeatureRenderer extends EnergySwirlOverlayFeatureRenderer {
   private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/wither/wither_armor.png");
   private final WitherEntityModel model;

   public WitherArmorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new WitherEntityModel(loader.getModelPart(EntityModelLayers.WITHER_ARMOR));
   }

   protected float getEnergySwirlX(float partialAge) {
      return MathHelper.cos(partialAge * 0.02F) * 3.0F;
   }

   protected Identifier getEnergySwirlTexture() {
      return SKIN;
   }

   protected EntityModel getEnergySwirlModel() {
      return this.model;
   }
}
