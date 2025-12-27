package net.minecraft.client.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class FontStorage implements AutoCloseable {
   private static final Random RANDOM = Random.create();
   private static final float MAX_ADVANCE = 32.0F;
   private final TextureManager textureManager;
   private final Identifier id;
   private GlyphRenderer blankGlyphRenderer;
   private GlyphRenderer whiteRectangleGlyphRenderer;
   private List allFonts = List.of();
   private List availableFonts = List.of();
   private final GlyphContainer glyphRendererCache = new GlyphContainer((i) -> new GlyphRenderer[i], (rowCount) -> new GlyphRenderer[rowCount][]);
   private final GlyphContainer glyphCache = new GlyphContainer((i) -> new GlyphPair[i], (rowCount) -> new GlyphPair[rowCount][]);
   private final Int2ObjectMap charactersByWidth = new Int2ObjectOpenHashMap();
   private final List glyphAtlases = Lists.newArrayList();

   public FontStorage(TextureManager textureManager, Identifier id) {
      this.textureManager = textureManager;
      this.id = id;
   }

   public void setFonts(List allFonts, Set activeFilters) {
      this.allFonts = allFonts;
      this.setActiveFilters(activeFilters);
   }

   public void setActiveFilters(Set activeFilters) {
      this.availableFonts = List.of();
      this.clear();
      this.availableFonts = this.applyFilters(this.allFonts, activeFilters);
   }

   private void clear() {
      this.closeGlyphAtlases();
      this.glyphRendererCache.clear();
      this.glyphCache.clear();
      this.charactersByWidth.clear();
      this.blankGlyphRenderer = BuiltinEmptyGlyph.MISSING.bake(this::getGlyphRenderer);
      this.whiteRectangleGlyphRenderer = BuiltinEmptyGlyph.WHITE.bake(this::getGlyphRenderer);
   }

   private List applyFilters(List allFonts, Set activeFilters) {
      IntSet intSet = new IntOpenHashSet();
      List<Font> list = new ArrayList();

      for(Font.FontFilterPair fontFilterPair : allFonts) {
         if (fontFilterPair.filter().isAllowed(activeFilters)) {
            list.add(fontFilterPair.provider());
            intSet.addAll(fontFilterPair.provider().getProvidedGlyphs());
         }
      }

      Set<Font> set = Sets.newHashSet();
      intSet.forEach((codePoint) -> {
         for(Font font : list) {
            Glyph glyph = font.getGlyph(codePoint);
            if (glyph != null) {
               set.add(font);
               if (glyph != BuiltinEmptyGlyph.MISSING) {
                  ((IntList)this.charactersByWidth.computeIfAbsent(MathHelper.ceil(glyph.getAdvance(false)), (i) -> new IntArrayList())).add(codePoint);
               }
               break;
            }
         }

      });
      Stream var10000 = list.stream();
      Objects.requireNonNull(set);
      return var10000.filter(set::contains).toList();
   }

   public void close() {
      this.closeGlyphAtlases();
   }

   private void closeGlyphAtlases() {
      for(GlyphAtlasTexture glyphAtlasTexture : this.glyphAtlases) {
         glyphAtlasTexture.close();
      }

      this.glyphAtlases.clear();
   }

   private static boolean isAdvanceInvalid(Glyph glyph) {
      float f = glyph.getAdvance(false);
      if (!(f < 0.0F) && !(f > 32.0F)) {
         float g = glyph.getAdvance(true);
         return g < 0.0F || g > 32.0F;
      } else {
         return true;
      }
   }

   private GlyphPair findGlyph(int codePoint) {
      Glyph glyph = null;

      for(Font font : this.availableFonts) {
         Glyph glyph2 = font.getGlyph(codePoint);
         if (glyph2 != null) {
            if (glyph == null) {
               glyph = glyph2;
            }

            if (!isAdvanceInvalid(glyph2)) {
               return new GlyphPair(glyph, glyph2);
            }
         }
      }

      if (glyph != null) {
         return new GlyphPair(glyph, BuiltinEmptyGlyph.MISSING);
      } else {
         return FontStorage.GlyphPair.MISSING;
      }
   }

   public Glyph getGlyph(int codePoint, boolean validateAdvance) {
      return ((GlyphPair)this.glyphCache.computeIfAbsent(codePoint, this::findGlyph)).getGlyph(validateAdvance);
   }

   private GlyphRenderer findGlyphRenderer(int codePoint) {
      for(Font font : this.availableFonts) {
         Glyph glyph = font.getGlyph(codePoint);
         if (glyph != null) {
            return glyph.bake(this::getGlyphRenderer);
         }
      }

      return this.blankGlyphRenderer;
   }

   public GlyphRenderer getGlyphRenderer(int codePoint) {
      return (GlyphRenderer)this.glyphRendererCache.computeIfAbsent(codePoint, this::findGlyphRenderer);
   }

   private GlyphRenderer getGlyphRenderer(RenderableGlyph c) {
      for(GlyphAtlasTexture glyphAtlasTexture : this.glyphAtlases) {
         GlyphRenderer glyphRenderer = glyphAtlasTexture.getGlyphRenderer(c);
         if (glyphRenderer != null) {
            return glyphRenderer;
         }
      }

      Identifier identifier = this.id.withSuffixedPath("/" + this.glyphAtlases.size());
      boolean bl = c.hasColor();
      TextRenderLayerSet textRenderLayerSet = bl ? TextRenderLayerSet.of(identifier) : TextRenderLayerSet.ofIntensity(identifier);
      GlyphAtlasTexture glyphAtlasTexture2 = new GlyphAtlasTexture(textRenderLayerSet, bl);
      this.glyphAtlases.add(glyphAtlasTexture2);
      this.textureManager.registerTexture(identifier, glyphAtlasTexture2);
      GlyphRenderer glyphRenderer2 = glyphAtlasTexture2.getGlyphRenderer(c);
      return glyphRenderer2 == null ? this.blankGlyphRenderer : glyphRenderer2;
   }

   public GlyphRenderer getObfuscatedGlyphRenderer(Glyph glyph) {
      IntList intList = (IntList)this.charactersByWidth.get(MathHelper.ceil(glyph.getAdvance(false)));
      return intList != null && !intList.isEmpty() ? this.getGlyphRenderer(intList.getInt(RANDOM.nextInt(intList.size()))) : this.blankGlyphRenderer;
   }

   public Identifier getId() {
      return this.id;
   }

   public GlyphRenderer getRectangleRenderer() {
      return this.whiteRectangleGlyphRenderer;
   }

   @Environment(EnvType.CLIENT)
   static record GlyphPair(Glyph glyph, Glyph advanceValidatedGlyph) {
      static final GlyphPair MISSING;

      Glyph getGlyph(boolean validateAdvance) {
         return validateAdvance ? this.advanceValidatedGlyph : this.glyph;
      }

      static {
         MISSING = new GlyphPair(BuiltinEmptyGlyph.MISSING, BuiltinEmptyGlyph.MISSING);
      }
   }
}
