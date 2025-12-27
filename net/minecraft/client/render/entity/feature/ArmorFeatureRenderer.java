package net.minecraft.client.render.entity.feature;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;

@Environment(EnvType.CLIENT)
public class ArmorFeatureRenderer extends FeatureRenderer {
   private static final Map ARMOR_TEXTURE_CACHE = Maps.newHashMap();
   private final BipedEntityModel innerModel;
   private final BipedEntityModel outerModel;
   private final SpriteAtlasTexture armorTrimsAtlas;

   public ArmorFeatureRenderer(FeatureRendererContext context, BipedEntityModel innerModel, BipedEntityModel outerModel, BakedModelManager bakery) {
      super(context);
      this.innerModel = innerModel;
      this.outerModel = outerModel;
      this.armorTrimsAtlas = bakery.getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l) {
      this.renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.CHEST, i, this.getModel(EquipmentSlot.CHEST));
      this.renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.LEGS, i, this.getModel(EquipmentSlot.LEGS));
      this.renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.FEET, i, this.getModel(EquipmentSlot.FEET));
      this.renderArmor(matrixStack, vertexConsumerProvider, livingEntity, EquipmentSlot.HEAD, i, this.getModel(EquipmentSlot.HEAD));
   }

   private void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity entity, EquipmentSlot armorSlot, int light, BipedEntityModel model) {
      ItemStack itemStack = entity.getEquippedStack(armorSlot);
      Item var9 = itemStack.getItem();
      if (var9 instanceof ArmorItem armorItem) {
         if (armorItem.getSlotType() == armorSlot) {
            ((BipedEntityModel)this.getContextModel()).copyBipedStateTo(model);
            this.setVisible(model, armorSlot);
            boolean bl = this.usesInnerModel(armorSlot);
            ArmorMaterial armorMaterial = (ArmorMaterial)armorItem.getMaterial().value();
            int i = itemStack.isIn(ItemTags.DYEABLE) ? Argb.fullAlpha(DyedColorComponent.getColor(itemStack, -6265536)) : -1;

            for(ArmorMaterial.Layer layer : armorMaterial.layers()) {
               int j = layer.isDyeable() ? i : -1;
               this.renderArmorParts(matrices, vertexConsumers, light, model, j, layer.getTexture(bl));
            }

            ArmorTrim armorTrim = (ArmorTrim)itemStack.get(DataComponentTypes.TRIM);
            if (armorTrim != null) {
               this.renderTrim(armorItem.getMaterial(), matrices, vertexConsumers, light, armorTrim, model, bl);
            }

            if (itemStack.hasGlint()) {
               this.renderGlint(matrices, vertexConsumers, light, model);
            }

         }
      }
   }

   protected void setVisible(BipedEntityModel bipedModel, EquipmentSlot slot) {
      bipedModel.setVisible(false);
      switch (slot) {
         case HEAD:
            bipedModel.head.visible = true;
            bipedModel.hat.visible = true;
            break;
         case CHEST:
            bipedModel.body.visible = true;
            bipedModel.rightArm.visible = true;
            bipedModel.leftArm.visible = true;
            break;
         case LEGS:
            bipedModel.body.visible = true;
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
            break;
         case FEET:
            bipedModel.rightLeg.visible = true;
            bipedModel.leftLeg.visible = true;
      }

   }

   private void renderArmorParts(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel model, int i, Identifier identifier) {
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(identifier));
      model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, i);
   }

   private void renderTrim(RegistryEntry armorMaterial, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmorTrim trim, BipedEntityModel model, boolean leggings) {
      Sprite sprite = this.armorTrimsAtlas.getSprite(leggings ? trim.getLeggingsModelId(armorMaterial) : trim.getGenericModelId(armorMaterial));
      VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(vertexConsumers.getBuffer(TexturedRenderLayers.getArmorTrims(((ArmorTrimPattern)trim.getPattern().value()).decal())));
      model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
   }

   private void renderGlint(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityModel model) {
      model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getArmorEntityGlint()), light, OverlayTexture.DEFAULT_UV);
   }

   private BipedEntityModel getModel(EquipmentSlot slot) {
      return this.usesInnerModel(slot) ? this.innerModel : this.outerModel;
   }

   private boolean usesInnerModel(EquipmentSlot slot) {
      return slot == EquipmentSlot.LEGS;
   }
}
