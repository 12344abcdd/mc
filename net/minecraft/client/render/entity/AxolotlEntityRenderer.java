package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.AxolotlEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.AxolotlEntity.Variant;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class AxolotlEntityRenderer extends MobEntityRenderer {
   private static final Map TEXTURES = (Map)Util.make(Maps.newHashMap(), (variants) -> {
      for(AxolotlEntity.Variant variant : Variant.values()) {
         variants.put(variant, Identifier.ofVanilla(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", variant.getName())));
      }

   });

   public AxolotlEntityRenderer(EntityRendererFactory.Context context) {
      super(context, new AxolotlEntityModel(context.getPart(EntityModelLayers.AXOLOTL)), 0.5F);
   }

   public Identifier getTexture(AxolotlEntity axolotlEntity) {
      return (Identifier)TEXTURES.get(axolotlEntity.getVariant());
   }
}
