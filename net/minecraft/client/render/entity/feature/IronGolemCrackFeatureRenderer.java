package net.minecraft.client.render.entity.feature;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.Cracks.CrackLevel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class IronGolemCrackFeatureRenderer extends FeatureRenderer {
   private static final Map CRACK_TEXTURES;

   public IronGolemCrackFeatureRenderer(FeatureRendererContext featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, IronGolemEntity ironGolemEntity, float f, float g, float h, float j, float k, float l) {
      if (!ironGolemEntity.isInvisible()) {
         Cracks.CrackLevel crackLevel = ironGolemEntity.getCrackLevel();
         if (crackLevel != CrackLevel.NONE) {
            Identifier identifier = (Identifier)CRACK_TEXTURES.get(crackLevel);
            renderModel(this.getContextModel(), identifier, matrixStack, vertexConsumerProvider, i, ironGolemEntity, -1);
         }
      }
   }

   static {
      CRACK_TEXTURES = ImmutableMap.of(CrackLevel.LOW, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_low.png"), CrackLevel.MEDIUM, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_medium.png"), CrackLevel.HIGH, Identifier.ofVanilla("textures/entity/iron_golem/iron_golem_crackiness_high.png"));
   }
}
