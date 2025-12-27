package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EmptyEntityRenderer extends EntityRenderer {
   public EmptyEntityRenderer(EntityRendererFactory.Context context) {
      super(context);
   }

   public Identifier getTexture(Entity entity) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
