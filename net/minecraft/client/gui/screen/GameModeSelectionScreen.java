package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class GameModeSelectionScreen extends Screen {
   static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("gamemode_switcher/slot");
   static final Identifier SELECTION_TEXTURE = Identifier.ofVanilla("gamemode_switcher/selection");
   private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/gamemode_switcher.png");
   private static final int TEXTURE_WIDTH = 128;
   private static final int TEXTURE_HEIGHT = 128;
   private static final int BUTTON_SIZE = 26;
   private static final int ICON_OFFSET = 5;
   private static final int field_32314 = 31;
   private static final int field_32315 = 5;
   private static final int UI_WIDTH = GameModeSelectionScreen.GameModeSelection.values().length * 31 - 5;
   private static final Text SELECT_NEXT_TEXT;
   private final GameModeSelection currentGameMode = GameModeSelectionScreen.GameModeSelection.of(this.getPreviousGameMode());
   private GameModeSelection gameMode;
   private int lastMouseX;
   private int lastMouseY;
   private boolean mouseUsedForSelection;
   private final List gameModeButtons = Lists.newArrayList();

   public GameModeSelectionScreen() {
      super(NarratorManager.EMPTY);
      this.gameMode = this.currentGameMode;
   }

   private GameMode getPreviousGameMode() {
      ClientPlayerInteractionManager clientPlayerInteractionManager = MinecraftClient.getInstance().interactionManager;
      GameMode gameMode = clientPlayerInteractionManager.getPreviousGameMode();
      if (gameMode != null) {
         return gameMode;
      } else {
         return clientPlayerInteractionManager.getCurrentGameMode() == GameMode.CREATIVE ? GameMode.SURVIVAL : GameMode.CREATIVE;
      }
   }

   protected void init() {
      super.init();
      this.gameMode = this.currentGameMode;

      for(int i = 0; i < GameModeSelectionScreen.GameModeSelection.VALUES.length; ++i) {
         GameModeSelection gameModeSelection = GameModeSelectionScreen.GameModeSelection.VALUES[i];
         this.gameModeButtons.add(new ButtonWidget(gameModeSelection, this.width / 2 - UI_WIDTH / 2 + i * 31, this.height / 2 - 31));
      }

   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (!this.checkForClose()) {
         context.getMatrices().push();
         RenderSystem.enableBlend();
         int i = this.width / 2 - 62;
         int j = this.height / 2 - 31 - 27;
         context.drawTexture(TEXTURE, i, j, 0.0F, 0.0F, 125, 75, 128, 128);
         context.getMatrices().pop();
         super.render(context, mouseX, mouseY, delta);
         context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.gameMode.getText(), this.width / 2, this.height / 2 - 31 - 20, -1);
         context.drawCenteredTextWithShadow(this.textRenderer, SELECT_NEXT_TEXT, this.width / 2, this.height / 2 + 5, 16777215);
         if (!this.mouseUsedForSelection) {
            this.lastMouseX = mouseX;
            this.lastMouseY = mouseY;
            this.mouseUsedForSelection = true;
         }

         boolean bl = this.lastMouseX == mouseX && this.lastMouseY == mouseY;

         for(ButtonWidget buttonWidget : this.gameModeButtons) {
            buttonWidget.render(context, mouseX, mouseY, delta);
            buttonWidget.setSelected(this.gameMode == buttonWidget.gameMode);
            if (!bl && buttonWidget.isSelected()) {
               this.gameMode = buttonWidget.gameMode;
            }
         }

      }
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private void apply() {
      apply(this.client, this.gameMode);
   }

   private static void apply(MinecraftClient client, GameModeSelection gameModeSelection) {
      if (client.interactionManager != null && client.player != null) {
         GameModeSelection gameModeSelection2 = GameModeSelectionScreen.GameModeSelection.of(client.interactionManager.getCurrentGameMode());
         if (client.player.hasPermissionLevel(2) && gameModeSelection != gameModeSelection2) {
            client.player.networkHandler.sendCommand(gameModeSelection.getCommand());
         }

      }
   }

   private boolean checkForClose() {
      if (!InputUtil.isKeyPressed(this.client.getWindow().getHandle(), 292)) {
         this.apply();
         this.client.setScreen((Screen)null);
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 293) {
         this.mouseUsedForSelection = false;
         this.gameMode = this.gameMode.next();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   static {
      SELECT_NEXT_TEXT = Text.translatable("debug.gamemodes.select_next", new Object[]{Text.translatable("debug.gamemodes.press_f4").formatted(Formatting.AQUA)});
   }

   @Environment(EnvType.CLIENT)
   public class ButtonWidget extends ClickableWidget {
      final GameModeSelection gameMode;
      private boolean selected;

      public ButtonWidget(final GameModeSelection gameMode, final int x, final int y) {
         super(x, y, 26, 26, gameMode.getText());
         this.gameMode = gameMode;
      }

      public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
         this.drawBackground(context);
         this.gameMode.renderIcon(context, this.getX() + 5, this.getY() + 5);
         if (this.selected) {
            this.drawSelectionBox(context);
         }

      }

      public void appendClickableNarrations(NarrationMessageBuilder builder) {
         this.appendDefaultNarrations(builder);
      }

      public boolean isSelected() {
         return super.isSelected() || this.selected;
      }

      public void setSelected(boolean selected) {
         this.selected = selected;
      }

      private void drawBackground(DrawContext context) {
         context.drawGuiTexture(GameModeSelectionScreen.SLOT_TEXTURE, this.getX(), this.getY(), 26, 26);
      }

      private void drawSelectionBox(DrawContext context) {
         context.drawGuiTexture(GameModeSelectionScreen.SELECTION_TEXTURE, this.getX(), this.getY(), 26, 26);
      }
   }

   @Environment(EnvType.CLIENT)
   static enum GameModeSelection {
      CREATIVE(Text.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
      SURVIVAL(Text.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
      ADVENTURE(Text.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
      SPECTATOR(Text.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

      protected static final GameModeSelection[] VALUES = values();
      private static final int field_32317 = 16;
      protected static final int field_32316 = 5;
      final Text text;
      final String command;
      final ItemStack icon;

      private GameModeSelection(final Text text, final String command, final ItemStack icon) {
         this.text = text;
         this.command = command;
         this.icon = icon;
      }

      void renderIcon(DrawContext context, int x, int y) {
         context.drawItem(this.icon, x, y);
      }

      Text getText() {
         return this.text;
      }

      String getCommand() {
         return this.command;
      }

      GameModeSelection next() {
         GameModeSelection var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = SURVIVAL;
            case 1 -> var10000 = ADVENTURE;
            case 2 -> var10000 = SPECTATOR;
            case 3 -> var10000 = CREATIVE;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      static GameModeSelection of(GameMode gameMode) {
         GameModeSelection var10000;
         switch (gameMode) {
            case SPECTATOR -> var10000 = SPECTATOR;
            case SURVIVAL -> var10000 = SURVIVAL;
            case CREATIVE -> var10000 = CREATIVE;
            case ADVENTURE -> var10000 = ADVENTURE;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      // $FF: synthetic method
      private static GameModeSelection[] method_36886() {
         return new GameModeSelection[]{CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR};
      }
   }
}
