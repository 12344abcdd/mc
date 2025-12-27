package net.minecraft.client.gui.hud.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(EnvType.CLIENT)
public class PingChart extends DebugChart {
   private static final int field_45931 = -65536;
   private static final int field_45932 = -256;
   private static final int field_45933 = -16711936;
   private static final int field_45934 = 500;

   public PingChart(TextRenderer textRenderer, MultiValueDebugSampleLog multiValueDebugSampleLog) {
      super(textRenderer, multiValueDebugSampleLog);
   }

   protected void renderThresholds(DrawContext context, int x, int width, int height) {
      this.drawBorderedText(context, "500 ms", x + 1, height - 60 + 1);
   }

   protected String format(double value) {
      return String.format(Locale.ROOT, "%d ms", (int)Math.round(value));
   }

   protected int getHeight(double value) {
      return (int)Math.round(value * (double)60.0F / (double)500.0F);
   }

   protected int getColor(long value) {
      return this.getColor((double)value, (double)0.0F, -16711936, (double)250.0F, -256, (double)500.0F, -65536);
   }
}
