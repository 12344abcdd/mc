package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.LargeTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.SmallTropicalFishEntityModel;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TropicalFishColorFeatureRenderer extends FeatureRenderer {
   private static final Identifier KOB_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_1.png");
   private static final Identifier SUNSTREAK_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_2.png");
   private static final Identifier SNOOPER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_3.png");
   private static final Identifier DASHER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_4.png");
   private static final Identifier BRINELY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_5.png");
   private static final Identifier SPOTTY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_a_pattern_6.png");
   private static final Identifier FLOPPER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_1.png");
   private static final Identifier STRIPEY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_2.png");
   private static final Identifier GLITTER_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_3.png");
   private static final Identifier BLOCKFISH_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_4.png");
   private static final Identifier BETTY_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_5.png");
   private static final Identifier CLAYFISH_TEXTURE = Identifier.ofVanilla("textures/entity/fish/tropical_b_pattern_6.png");
   private final SmallTropicalFishEntityModel smallModel;
   private final LargeTropicalFishEntityModel largeModel;

   public TropicalFishColorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.smallModel = new SmallTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_SMALL_PATTERN));
      this.largeModel = new LargeTropicalFishEntityModel(loader.getModelPart(EntityModelLayers.TROPICAL_FISH_LARGE_PATTERN));
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, TropicalFishEntity tropicalFishEntity, float f, float g, float h, float j, float k, float l) {
      TropicalFishEntity.Variety variety = tropicalFishEntity.getVariant();
      Object var10000;
      switch (variety.getSize()) {
         case SMALL -> var10000 = this.smallModel;
         case LARGE -> var10000 = this.largeModel;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      EntityModel<TropicalFishEntity> entityModel = (EntityModel<TropicalFishEntity>)var10000;
      Identifier var15;
      switch (variety) {
         case KOB -> var15 = KOB_TEXTURE;
         case SUNSTREAK -> var15 = SUNSTREAK_TEXTURE;
         case SNOOPER -> var15 = SNOOPER_TEXTURE;
         case DASHER -> var15 = DASHER_TEXTURE;
         case BRINELY -> var15 = BRINELY_TEXTURE;
         case SPOTTY -> var15 = SPOTTY_TEXTURE;
         case FLOPPER -> var15 = FLOPPER_TEXTURE;
         case STRIPEY -> var15 = STRIPEY_TEXTURE;
         case GLITTER -> var15 = GLITTER_TEXTURE;
         case BLOCKFISH -> var15 = BLOCKFISH_TEXTURE;
         case BETTY -> var15 = BETTY_TEXTURE;
         case CLAYFISH -> var15 = CLAYFISH_TEXTURE;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      Identifier identifier = var15;
      int m = tropicalFishEntity.getPatternColorComponents().getEntityColor();
      render(this.getContextModel(), entityModel, identifier, matrixStack, vertexConsumerProvider, i, tropicalFishEntity, f, g, j, k, l, h, m);
   }
}
