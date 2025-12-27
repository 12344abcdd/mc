package net.minecraft.client.font;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BitmapFont implements Font {
   static final Logger LOGGER = LogUtils.getLogger();
   private final NativeImage image;
   private final GlyphContainer glyphs;

   BitmapFont(NativeImage image, GlyphContainer glyphs) {
      this.image = image;
      this.glyphs = glyphs;
   }

   public void close() {
      this.image.close();
   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      return (Glyph)this.glyphs.get(codePoint);
   }

   public IntSet getProvidedGlyphs() {
      return IntSets.unmodifiable(this.glyphs.getProvidedGlyphs());
   }

   @Environment(EnvType.CLIENT)
   static record BitmapFontGlyph(float scaleFactor, NativeImage image, int x, int y, int width, int height, int advance, int ascent) implements Glyph {
      final float scaleFactor;
      final NativeImage image;
      final int x;
      final int y;
      final int width;
      final int height;
      final int ascent;

      public float getAdvance() {
         return (float)this.advance;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public float getOversample() {
               return 1.0F / BitmapFontGlyph.this.scaleFactor;
            }

            public int getWidth() {
               return BitmapFontGlyph.this.width;
            }

            public int getHeight() {
               return BitmapFontGlyph.this.height;
            }

            public float getAscent() {
               return (float)BitmapFontGlyph.this.ascent;
            }

            public void upload(int x, int y) {
               BitmapFontGlyph.this.image.upload(0, x, y, BitmapFontGlyph.this.x, BitmapFontGlyph.this.y, BitmapFontGlyph.this.width, BitmapFontGlyph.this.height, false, false);
            }

            public boolean hasColor() {
               return BitmapFontGlyph.this.image.getFormat().getChannelCount() > 1;
            }
         });
      }
   }

   @Environment(EnvType.CLIENT)
   public static record Loader(Identifier file, int height, int ascent, int[][] codepointGrid) implements FontLoader {
      private static final Codec CODE_POINT_GRID_CODEC;
      public static final MapCodec CODEC;

      private static DataResult validateCodePointGrid(int[][] codePointGrid) {
         int i = codePointGrid.length;
         if (i == 0) {
            return DataResult.error(() -> "Expected to find data in codepoint grid");
         } else {
            int[] is = codePointGrid[0];
            int j = is.length;
            if (j == 0) {
               return DataResult.error(() -> "Expected to find data in codepoint grid");
            } else {
               for(int k = 1; k < i; ++k) {
                  int[] js = codePointGrid[k];
                  if (js.length != j) {
                     return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + js.length + " codepoints, expected: " + j + "), pad with \\u0000");
                  }
               }

               return DataResult.success(codePointGrid);
            }
         }
      }

      private static DataResult validate(Loader fontLoader) {
         return fontLoader.ascent > fontLoader.height ? DataResult.error(() -> "Ascent " + fontLoader.ascent + " higher than height " + fontLoader.height) : DataResult.success(fontLoader);
      }

      public FontType getType() {
         return FontType.BITMAP;
      }

      public Either build() {
         return Either.left(this::load);
      }

      private Font load(ResourceManager resourceManager) throws IOException {
         Identifier identifier = this.file.withPrefixedPath("textures/");
         InputStream inputStream = resourceManager.open(identifier);

         BitmapFont var22;
         try {
            NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
            int i = nativeImage.getWidth();
            int j = nativeImage.getHeight();
            int k = i / this.codepointGrid[0].length;
            int l = j / this.codepointGrid.length;
            float f = (float)this.height / (float)l;
            GlyphContainer<BitmapFontGlyph> glyphContainer = new GlyphContainer((ix) -> new BitmapFontGlyph[ix], (ix) -> new BitmapFontGlyph[ix][]);

            for(int m = 0; m < this.codepointGrid.length; ++m) {
               int n = 0;

               for(int o : this.codepointGrid[m]) {
                  int p = n++;
                  if (o != 0) {
                     int q = this.findCharacterStartX(nativeImage, k, l, p, m);
                     BitmapFontGlyph bitmapFontGlyph = (BitmapFontGlyph)glyphContainer.put(o, new BitmapFontGlyph(f, nativeImage, p * k, m * l, k, l, (int)((double)0.5F + (double)((float)q * f)) + 1, this.ascent));
                     if (bitmapFontGlyph != null) {
                        BitmapFont.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(o), identifier);
                     }
                  }
               }
            }

            var22 = new BitmapFont(nativeImage, glyphContainer);
         } catch (Throwable var21) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var20) {
                  var21.addSuppressed(var20);
               }
            }

            throw var21;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var22;
      }

      private int findCharacterStartX(NativeImage image, int characterWidth, int characterHeight, int charPosX, int charPosY) {
         int i;
         for(i = characterWidth - 1; i >= 0; --i) {
            int j = charPosX * characterWidth + i;

            for(int k = 0; k < characterHeight; ++k) {
               int l = charPosY * characterHeight + k;
               if (image.getOpacity(j, l) != 0) {
                  return i + 1;
               }
            }
         }

         return i + 1;
      }

      static {
         CODE_POINT_GRID_CODEC = Codec.STRING.listOf().xmap((strings) -> {
            int i = strings.size();
            int[][] is = new int[i][];

            for(int j = 0; j < i; ++j) {
               is[j] = ((String)strings.get(j)).codePoints().toArray();
            }

            return is;
         }, (codePointGrid) -> {
            List<String> list = new ArrayList(codePointGrid.length);

            for(int[] is : codePointGrid) {
               list.add(new String(is, 0, is.length));
            }

            return list;
         }).validate(Loader::validateCodePointGrid);
         CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Identifier.CODEC.fieldOf("file").forGetter(Loader::file), Codec.INT.optionalFieldOf("height", 8).forGetter(Loader::height), Codec.INT.fieldOf("ascent").forGetter(Loader::ascent), CODE_POINT_GRID_CODEC.fieldOf("chars").forGetter(Loader::codepointGrid)).apply(instance, Loader::new)).validate(Loader::validate);
      }
   }
}
