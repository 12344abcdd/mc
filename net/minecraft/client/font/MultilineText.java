package net.minecraft.client.font;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface MultilineText {
   MultilineText EMPTY = new MultilineText() {
      public void drawCenterWithShadow(DrawContext context, int x, int y) {
      }

      public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
      }

      public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
      }

      public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
         return y;
      }

      public int count() {
         return 0;
      }

      public int getMaxWidth() {
         return 0;
      }
   };

   static MultilineText create(TextRenderer renderer, Text... texts) {
      return create(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, texts);
   }

   static MultilineText create(TextRenderer renderer, int maxWidth, Text... texts) {
      return create(renderer, maxWidth, Integer.MAX_VALUE, texts);
   }

   static MultilineText create(TextRenderer renderer, Text text, int maxWidth) {
      return create(renderer, maxWidth, Integer.MAX_VALUE, text);
   }

   static MultilineText create(final TextRenderer renderer, final int maxWidth, final int maxLines, final Text... texts) {
      return texts.length == 0 ? EMPTY : new MultilineText(renderer, texts, maxWidth, maxLines) {
         @Nullable
         private List lines;
         @Nullable
         private Language language;

         public void drawCenterWithShadow(DrawContext context, int x, int y) {
            Objects.requireNonNull(renderer);
            this.drawCenterWithShadow(context, x, y, 9, -1);
         }

         public void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
            int i = y;

            for(Line line : this.getLines()) {
               context.drawCenteredTextWithShadow(renderer, line.text, x, i, color);
               i += lineHeight;
            }

         }

         public void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color) {
            int i = y;

            for(Line line : this.getLines()) {
               context.drawTextWithShadow(renderer, line.text, x, i, color);
               i += lineHeight;
            }

         }

         public int draw(DrawContext context, int x, int y, int lineHeight, int color) {
            int i = y;

            for(Line line : this.getLines()) {
               context.drawText(renderer, line.text, x, i, color, false);
               i += lineHeight;
            }

            return i;
         }

         private List getLines() {
            Language language = Language.getInstance();
            if (this.lines != null && language == this.language) {
               return this.lines;
            } else {
               this.language = language;
               List<OrderedText> list = new ArrayList();

               for(Text text : texts) {
                  list.addAll(renderer.wrapLines(text, maxWidth));
               }

               this.lines = new ArrayList();

               for(OrderedText orderedText : list.subList(0, Math.min(list.size(), maxLines))) {
                  this.lines.add(new Line(orderedText, renderer.getWidth(orderedText)));
               }

               return this.lines;
            }
         }

         public int count() {
            return this.getLines().size();
         }

         public int getMaxWidth() {
            return Math.min(maxWidth, this.getLines().stream().mapToInt(Line::width).max().orElse(0));
         }
      };
   }

   void drawCenterWithShadow(DrawContext context, int x, int y);

   void drawCenterWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

   void drawWithShadow(DrawContext context, int x, int y, int lineHeight, int color);

   int draw(DrawContext context, int x, int y, int lineHeight, int color);

   int count();

   int getMaxWidth();

   @Environment(EnvType.CLIENT)
   public static record Line(OrderedText text, int width) {
      final OrderedText text;
   }
}
