package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.EndermiteEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EndermiteEntityRenderer extends MobEntityRenderer {
   private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/endermite.png");

   public EndermiteEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new EndermiteEntityModel(context.getPart(EntityModelLayers.ENDERMITE)), 0.3F);
   }

   protected float getLyingAngle(EndermiteEntity endermiteEntity) {
      return 180.0F;
   }

   public Identifier getTexture(EndermiteEntity endermiteEntity) {
      return TEXTURE;
   }

   // $FF: synthetic method
   protected float getLyingAngle(final LivingEntity entity) {
      return this.getLyingAngle((EndermiteEntity)entity);
   }

   // $FF: synthetic method
   public Identifier getTexture(final Entity entity) {
      return this.getTexture((EndermiteEntity)entity);
   }
}
