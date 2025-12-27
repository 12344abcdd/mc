package net.minecraft.client.gui.hud.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(EnvType.CLIENT)
public class RenderingChart extends DebugChart {
   private static final int field_45926 = -65536;
   private static final int field_45927 = -256;
   private static final int field_45928 = -16711936;
   private static final int field_45929 = 30;
   private static final double field_45930 = 33.333333333333336;

   public RenderingChart(TextRenderer textRenderer, MultiValueDebugSampleLog multiValueDebugSampleLog) {
      super(textRenderer, multiValueDebugSampleLog);
   }

   protected void renderThresholds(DrawContext context, int x, int width, int height) {
      this.drawBorderedText(context, "30 FPS", x + 1, height - 60 + 1);
      this.drawBorderedText(context, "60 FPS", x + 1, height - 30 + 1);
      context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, height - 30, -1);
      int i = (Integer)MinecraftClient.getInstance().options.getMaxFps().getValue();
      if (i > 0 && i <= 250) {
         context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, height - this.getHeight((double)1.0E9F / (double)i) - 1, -16711681);
      }

   }

   protected String format(double value) {
      return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMillisecondsPerFrame(value)));
   }

   protected int getHeight(double value) {
      return (int)Math.round(toMillisecondsPerFrame(value) * (double)60.0F / 33.333333333333336);
   }

   protected int getColor(long value) {
      return this.getColor(toMillisecondsPerFrame((double)value), (double)0.0F, -16711936, (double)28.0F, -256, (double)56.0F, -65536);
   }

   private static double toMillisecondsPerFrame(double nanosecondsPerFrame) {
      return nanosecondsPerFrame / (double)1000000.0F;
   }
}
