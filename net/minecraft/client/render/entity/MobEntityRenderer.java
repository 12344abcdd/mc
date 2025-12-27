package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

@Environment(EnvType.CLIENT)
public abstract class MobEntityRenderer extends LivingEntityRenderer {
   public MobEntityRenderer(EntityRendererFactory.Context context, EntityModel entityModel, float f) {
      super(context, entityModel, f);
   }

   protected boolean hasLabel(MobEntity mobEntity) {
      return super.hasLabel((LivingEntity)mobEntity) && (mobEntity.shouldRenderName() || mobEntity.hasCustomName() && mobEntity == this.dispatcher.targetedEntity);
   }

   protected float getShadowRadius(MobEntity mobEntity) {
      return super.getShadowRadius((LivingEntity)mobEntity) * mobEntity.getScaleFactor();
   }

   // $FF: synthetic method
   protected float getShadowRadius(final LivingEntity livingEntity) {
      return this.getShadowRadius((MobEntity)livingEntity);
   }

   // $FF: synthetic method
   protected boolean hasLabel(final LivingEntity livingEntity) {
      return this.hasLabel((MobEntity)livingEntity);
   }

   // $FF: synthetic method
   protected float getShadowRadius(final Entity entity) {
      return this.getShadowRadius((MobEntity)entity);
   }

   // $FF: synthetic method
   protected boolean hasLabel(final Entity entity) {
      return this.hasLabel((MobEntity)entity);
   }
}
