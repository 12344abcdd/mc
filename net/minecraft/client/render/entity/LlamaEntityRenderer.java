package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.LlamaDecorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class LlamaEntityRenderer extends MobEntityRenderer {
   private static final Identifier CREAMY_TEXTURE = Identifier.ofVanilla("textures/entity/llama/creamy.png");
   private static final Identifier WHITE_TEXTURE = Identifier.ofVanilla("textures/entity/llama/white.png");
   private static final Identifier BROWN_TEXTURE = Identifier.ofVanilla("textures/entity/llama/brown.png");
   private static final Identifier GRAY_TEXTURE = Identifier.ofVanilla("textures/entity/llama/gray.png");

   public LlamaEntityRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer) {
      super(ctx, new LlamaEntityModel(ctx.getPart(layer)), 0.7F);
      this.addFeature(new LlamaDecorFeatureRenderer(this, ctx.getModelLoader()));
   }

   public Identifier getTexture(LlamaEntity llamaEntity) {
      Identifier var10000;
      switch (llamaEntity.getVariant()) {
         case CREAMY -> var10000 = CREAMY_TEXTURE;
         case WHITE -> var10000 = WHITE_TEXTURE;
         case BROWN -> var10000 = BROWN_TEXTURE;
         case GRAY -> var10000 = GRAY_TEXTURE;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
