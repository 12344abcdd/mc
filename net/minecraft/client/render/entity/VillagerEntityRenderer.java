package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class VillagerEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/villager/villager.png");

   public VillagerEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new VillagerResemblingModel(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);
      this.addFeature(new HeadFeatureRenderer(this, context.getModelLoader(), context.getHeldItemRenderer()));
      this.addFeature(new VillagerClothingFeatureRenderer(this, context.getResourceManager(), "villager"));
      this.addFeature(new VillagerHeldItemFeatureRenderer(this, context.getHeldItemRenderer()));
   }

   public Identifier getTexture(VillagerEntity villagerEntity) {
      return TEXTURE;
   }

   protected void scale(VillagerEntity villagerEntity, MatrixStack matrixStack, float f) {
      float g = 0.9375F * villagerEntity.getScaleFactor();
      matrixStack.scale(g, g, g);
   }

   protected float getShadowRadius(VillagerEntity villagerEntity) {
      float f = super.getShadowRadius((MobEntity)villagerEntity);
      return villagerEntity.isBaby() ? f * 0.5F : f;
   }

   // $FF: synthetic method
   protected float getShadowRadius(final LivingEntity livingEntity) {
      return this.getShadowRadius((VillagerEntity)livingEntity);
   }

   // $FF: synthetic method
   protected float getShadowRadius(final Entity entity) {
      return this.getShadowRadius((VillagerEntity)entity);
   }
}
