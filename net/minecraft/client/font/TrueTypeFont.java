package net.minecraft.client.font;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@Environment(EnvType.CLIENT)
public class TrueTypeFont implements Font {
   @Nullable
   private ByteBuffer buffer;
   @Nullable
   private FT_Face face;
   final float oversample;
   private final IntSet excludedCharacters = new IntArraySet();

   public TrueTypeFont(ByteBuffer buffer, FT_Face face, float size, float oversample, float shiftX, float shiftY, String excludedCharacters) {
      this.buffer = buffer;
      this.face = face;
      this.oversample = oversample;
      IntStream var10000 = excludedCharacters.codePoints();
      IntSet var10001 = this.excludedCharacters;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::add);
      int i = Math.round(size * oversample);
      FreeType.FT_Set_Pixel_Sizes(face, i, i);
      float f = shiftX * oversample;
      float g = -shiftY * oversample;
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         FT_Vector fT_Vector = FreeTypeUtil.set(FT_Vector.malloc(memoryStack), f, g);
         FreeType.FT_Set_Transform(face, (FT_Matrix)null, fT_Vector);
      } catch (Throwable var15) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var14) {
               var15.addSuppressed(var14);
            }
         }

         throw var15;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

   }

   @Nullable
   public Glyph getGlyph(int codePoint) {
      FT_Face fT_Face = this.getInfo();
      if (this.excludedCharacters.contains(codePoint)) {
         return null;
      } else {
         int i = FreeType.FT_Get_Char_Index(fT_Face, (long)codePoint);
         if (i == 0) {
            return null;
         } else {
            FreeTypeUtil.checkFatalError(FreeType.FT_Load_Glyph(fT_Face, i, 4194312), "Loading glyph");
            FT_GlyphSlot fT_GlyphSlot = (FT_GlyphSlot)Objects.requireNonNull(fT_Face.glyph(), "Glyph not initialized");
            float f = FreeTypeUtil.getX(fT_GlyphSlot.advance());
            FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
            int j = fT_GlyphSlot.bitmap_left();
            int k = fT_GlyphSlot.bitmap_top();
            int l = fT_Bitmap.width();
            int m = fT_Bitmap.rows();
            return (Glyph)(l > 0 && m > 0 ? new TtfGlyph((float)j, (float)k, l, m, f, i) : () -> f / this.oversample);
         }
      }
   }

   FT_Face getInfo() {
      if (this.buffer != null && this.face != null) {
         return this.face;
      } else {
         throw new IllegalStateException("Provider already closed");
      }
   }

   public void close() {
      if (this.face != null) {
         synchronized(FreeTypeUtil.LOCK) {
            FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
         }

         this.face = null;
      }

      MemoryUtil.memFree(this.buffer);
      this.buffer = null;
   }

   public IntSet getProvidedGlyphs() {
      FT_Face fT_Face = this.getInfo();
      IntSet intSet = new IntOpenHashSet();
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         IntBuffer intBuffer = memoryStack.mallocInt(1);

         for(long l = FreeType.FT_Get_First_Char(fT_Face, intBuffer); intBuffer.get(0) != 0; l = FreeType.FT_Get_Next_Char(fT_Face, l, intBuffer)) {
            intSet.add((int)l);
         }
      } catch (Throwable var8) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }

      intSet.removeAll(this.excludedCharacters);
      return intSet;
   }

   @Environment(EnvType.CLIENT)
   class TtfGlyph implements Glyph {
      final int width;
      final int height;
      final float bearingX;
      final float ascent;
      private final float advance;
      final int glyphIndex;

      TtfGlyph(final float bearingX, final float ascent, final int width, final int height, final float advance, final int glyphIndex) {
         this.width = width;
         this.height = height;
         this.advance = advance / TrueTypeFont.this.oversample;
         this.bearingX = bearingX / TrueTypeFont.this.oversample;
         this.ascent = ascent / TrueTypeFont.this.oversample;
         this.glyphIndex = glyphIndex;
      }

      public float getAdvance() {
         return this.advance;
      }

      public GlyphRenderer bake(Function function) {
         return (GlyphRenderer)function.apply(new RenderableGlyph() {
            public int getWidth() {
               return TtfGlyph.this.width;
            }

            public int getHeight() {
               return TtfGlyph.this.height;
            }

            public float getOversample() {
               return TrueTypeFont.this.oversample;
            }

            public float getBearingX() {
               return TtfGlyph.this.bearingX;
            }

            public float getAscent() {
               return TtfGlyph.this.ascent;
            }

            public void upload(int x, int y) {
               FT_Face fT_Face = TrueTypeFont.this.getInfo();
               NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, TtfGlyph.this.width, TtfGlyph.this.height, false);
               if (nativeImage.makeGlyphBitmapSubpixel(fT_Face, TtfGlyph.this.glyphIndex)) {
                  nativeImage.upload(0, x, y, 0, 0, TtfGlyph.this.width, TtfGlyph.this.height, false, true);
               } else {
                  nativeImage.close();
               }

            }

            public boolean hasColor() {
               return false;
            }
         });
      }
   }
}
