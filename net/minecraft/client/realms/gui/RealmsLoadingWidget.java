package net.minecraft.client.realms.gui;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RealmsLoadingWidget extends ClickableWidget {
   private final TextRenderer textRenderer;

   public RealmsLoadingWidget(TextRenderer textRenderer, Text message) {
      int var10003 = textRenderer.getWidth((StringVisitable)message);
      Objects.requireNonNull(textRenderer);
      super(0, 0, var10003, 9 * 3, message);
      this.textRenderer = textRenderer;
   }

   protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      int i = this.getX() + this.getWidth() / 2;
      int j = this.getY() + this.getHeight() / 2;
      Text text = this.getMessage();
      TextRenderer var10001 = this.textRenderer;
      int var10003 = i - this.textRenderer.getWidth((StringVisitable)text) / 2;
      Objects.requireNonNull(this.textRenderer);
      context.drawText(var10001, (Text)text, var10003, j - 9, -1, false);
      String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
      var10001 = this.textRenderer;
      var10003 = i - this.textRenderer.getWidth(string) / 2;
      Objects.requireNonNull(this.textRenderer);
      context.drawText(var10001, string, var10003, j + 9, -8355712, false);
   }

   protected void appendClickableNarrations(NarrationMessageBuilder builder) {
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public boolean isNarratable() {
      return false;
   }

   @Nullable
   public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      return null;
   }
}
