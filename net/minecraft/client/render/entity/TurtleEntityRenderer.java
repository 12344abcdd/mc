package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TurtleEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TurtleEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/turtle/big_sea_turtle.png");

   public TurtleEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new TurtleEntityModel(context.getPart(EntityModelLayers.TURTLE)), 0.7F);
   }

   protected float getShadowRadius(TurtleEntity turtleEntity) {
      float f = super.getShadowRadius((MobEntity)turtleEntity);
      return turtleEntity.isBaby() ? f * 0.83F : f;
   }

   public Identifier getTexture(TurtleEntity turtleEntity) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getShadowRadius(final MobEntity mobEntity) {
      return this.getShadowRadius((TurtleEntity)mobEntity);
   }

   // $FF: synthetic method
   protected float getShadowRadius(final LivingEntity livingEntity) {
      return this.getShadowRadius((TurtleEntity)livingEntity);
   }

   // $FF: synthetic method
   public Identifier getTexture(final Entity entity) {
      return this.getTexture((TurtleEntity)entity);
   }

   // $FF: synthetic method
   protected float getShadowRadius(final Entity entity) {
      return this.getShadowRadius((TurtleEntity)entity);
   }
}
