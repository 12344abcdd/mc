package net.minecraft.client.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record TextRenderLayerSet(RenderLayer normal, RenderLayer seeThrough, RenderLayer polygonOffset) {
   public static TextRenderLayerSet ofIntensity(Identifier textureId) {
      return new TextRenderLayerSet(RenderLayer.getTextIntensity(textureId), RenderLayer.getTextIntensitySeeThrough(textureId), RenderLayer.getTextIntensityPolygonOffset(textureId));
   }

   public static TextRenderLayerSet of(Identifier textureId) {
      return new TextRenderLayerSet(RenderLayer.getText(textureId), RenderLayer.getTextSeeThrough(textureId), RenderLayer.getTextPolygonOffset(textureId));
   }

   public RenderLayer getRenderLayer(TextRenderer.TextLayerType layerType) {
      RenderLayer var10000;
      switch (layerType) {
         case NORMAL -> var10000 = this.normal;
         case SEE_THROUGH -> var10000 = this.seeThrough;
         case POLYGON_OFFSET -> var10000 = this.polygonOffset;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
