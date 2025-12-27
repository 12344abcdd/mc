package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.AnimalArmorItem.Type;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class HorseArmorFeatureRenderer extends FeatureRenderer {
   private final HorseEntityModel model;

   public HorseArmorFeatureRenderer(FeatureRendererContext context, EntityModelLoader loader) {
      super(context);
      this.model = new HorseEntityModel(loader.getModelPart(EntityModelLayers.HORSE_ARMOR));
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, HorseEntity horseEntity, float f, float g, float h, float j, float k, float l) {
      ItemStack itemStack = horseEntity.getBodyArmor();
      Item var13 = itemStack.getItem();
      if (var13 instanceof AnimalArmorItem animalArmorItem) {
         if (animalArmorItem.getType() == Type.EQUESTRIAN) {
            ((HorseEntityModel)this.getContextModel()).copyStateTo(this.model);
            this.model.animateModel((AbstractHorseEntity)horseEntity, f, g, h);
            this.model.setAngles((AbstractHorseEntity)horseEntity, f, g, j, k, l);
            int m;
            if (itemStack.isIn(ItemTags.DYEABLE)) {
               m = Argb.fullAlpha(DyedColorComponent.getColor(itemStack, -6265536));
            } else {
               m = -1;
            }

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(animalArmorItem.getEntityTexture()));
            this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, m);
            return;
         }
      }

   }
}
