package net.minecraft.client.render.entity.feature;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.passive.Cracks;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.Cracks.CrackLevel;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.AnimalArmorItem.Type;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class WolfArmorFeatureRenderer extends FeatureRenderer {
   private final WolfEntityModel model;
   private static final Map CRACK_TEXTURES;

   public WolfArmorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new WolfEntityModel(loader.getModelPart(EntityModelLayers.WOLF_ARMOR));
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, WolfEntity wolfEntity, float f, float g, float h, float j, float k, float l) {
      if (wolfEntity.hasArmor()) {
         ItemStack itemStack = wolfEntity.getBodyArmor();
         Item var13 = itemStack.getItem();
         if (var13 instanceof AnimalArmorItem) {
            AnimalArmorItem animalArmorItem = (AnimalArmorItem)var13;
            if (animalArmorItem.getType() == Type.CANINE) {
               ((WolfEntityModel)this.getContextModel()).copyStateTo(this.model);
               this.model.animateModel(wolfEntity, f, g, h);
               this.model.setAngles(wolfEntity, f, g, j, k, l);
               VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(animalArmorItem.getEntityTexture()));
               this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
               this.renderDyed(matrixStack, vertexConsumerProvider, i, itemStack, animalArmorItem);
               this.renderCracks(matrixStack, vertexConsumerProvider, i, itemStack);
               return;
            }
         }

      }
   }

   private void renderDyed(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, AnimalArmorItem item) {
      if (stack.isIn(ItemTags.DYEABLE)) {
         int i = DyedColorComponent.getColor(stack, 0);
         if (Argb.getAlpha(i) == 0) {
            return;
         }

         Identifier identifier = item.getOverlayTexture();
         if (identifier == null) {
            return;
         }

         this.model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier)), light, OverlayTexture.DEFAULT_UV, Argb.fullAlpha(i));
      }

   }

   private void renderCracks(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack) {
      Cracks.CrackLevel crackLevel = Cracks.WOLF_ARMOR.getCrackLevel(stack);
      if (crackLevel != CrackLevel.NONE) {
         Identifier identifier = (Identifier)CRACK_TEXTURES.get(crackLevel);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(identifier));
         this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
      }
   }

   static {
      CRACK_TEXTURES = Map.of(CrackLevel.LOW, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_low.png"), CrackLevel.MEDIUM, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_medium.png"), CrackLevel.HIGH, Identifier.ofVanilla("textures/entity/wolf/wolf_armor_crackiness_high.png"));
   }
}
