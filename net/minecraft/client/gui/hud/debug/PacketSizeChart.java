package net.minecraft.client.gui.hud.debug;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(EnvType.CLIENT)
public class PacketSizeChart extends DebugChart {
   private static final int field_45920 = -16711681;
   private static final int field_45921 = -6250241;
   private static final int field_45922 = -65536;
   private static final int field_45923 = 1024;
   private static final int field_45924 = 1048576;
   private static final int field_45925 = 1048576;

   public PacketSizeChart(TextRenderer textRenderer, MultiValueDebugSampleLog multiValueDebugSampleLog) {
      super(textRenderer, multiValueDebugSampleLog);
   }

   protected void renderThresholds(DrawContext context, int x, int width, int height) {
      this.drawSizeBar(context, x, width, height, 64);
      this.drawSizeBar(context, x, width, height, 1024);
      this.drawSizeBar(context, x, width, height, 16384);
      this.drawBorderedText(context, formatBytesPerSecond((double)1048576.0F), x + 1, height - calculateHeight((double)1048576.0F) + 1);
   }

   private void drawSizeBar(DrawContext context, int x, int width, int height, int bytes) {
      this.drawSizeBar(context, x, width, height - calculateHeight((double)bytes), formatBytesPerSecond((double)bytes));
   }

   private void drawSizeBar(DrawContext context, int x, int width, int y, String label) {
      this.drawBorderedText(context, label, x + 1, y + 1);
      context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, y, -1);
   }

   protected String format(double value) {
      return formatBytesPerSecond(toBytesPerSecond(value));
   }

   private static String formatBytesPerSecond(double value) {
      if (value >= (double)1048576.0F) {
         return String.format(Locale.ROOT, "%.1f MiB/s", value / (double)1048576.0F);
      } else {
         return value >= (double)1024.0F ? String.format(Locale.ROOT, "%.1f KiB/s", value / (double)1024.0F) : String.format(Locale.ROOT, "%d B/s", MathHelper.floor(value));
      }
   }

   protected int getHeight(double value) {
      return calculateHeight(toBytesPerSecond(value));
   }

   private static int calculateHeight(double value) {
      return (int)Math.round(Math.log(value + (double)1.0F) * (double)60.0F / Math.log((double)1048576.0F));
   }

   protected int getColor(long value) {
      return this.getColor(toBytesPerSecond((double)value), (double)0.0F, -16711681, (double)8192.0F, -6250241, (double)1.048576E7F, -65536);
   }

   private static double toBytesPerSecond(double bytesPerTick) {
      return bytesPerTick * (double)20.0F;
   }
}
