package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SilverfishEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SilverfishEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/silverfish.png");

   public SilverfishEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new SilverfishEntityModel(context.getPart(EntityModelLayers.SILVERFISH)), 0.3F);
   }

   protected float getLyingAngle(SilverfishEntity silverfishEntity) {
      return 180.0F;
   }

   public Identifier getTexture(SilverfishEntity silverfishEntity) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getLyingAngle(final LivingEntity entity) {
      return this.getLyingAngle((SilverfishEntity)entity);
   }

   // $FF: synthetic method
   public Identifier getTexture(final Entity entity) {
      return this.getTexture((SilverfishEntity)entity);
   }
}
