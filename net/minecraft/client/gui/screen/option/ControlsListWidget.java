package net.minecraft.client.gui.screen.option;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ControlsListWidget extends ElementListWidget {
   private static final int field_49533 = 20;
   final KeybindsScreen parent;
   private int maxKeyNameLength;

   public ControlsListWidget(KeybindsScreen parent, MinecraftClient client) {
      super(client, parent.width, parent.layout.getContentHeight(), parent.layout.getHeaderHeight(), 20);
      this.parent = parent;
      KeyBinding[] keyBindings = (KeyBinding[])ArrayUtils.clone(client.options.allKeys);
      Arrays.sort(keyBindings);
      String string = null;

      for(KeyBinding keyBinding : keyBindings) {
         String string2 = keyBinding.getCategory();
         if (!string2.equals(string)) {
            string = string2;
            this.addEntry(new CategoryEntry(Text.translatable(string2)));
         }

         Text text = Text.translatable(keyBinding.getTranslationKey());
         int i = client.textRenderer.getWidth((StringVisitable)text);
         if (i > this.maxKeyNameLength) {
            this.maxKeyNameLength = i;
         }

         this.addEntry(new KeyBindingEntry(keyBinding, text));
      }

   }

   public void update() {
      KeyBinding.updateKeysByCode();
      this.updateChildren();
   }

   public void updateChildren() {
      this.children().forEach(Entry::update);
   }

   public int getRowWidth() {
      return 340;
   }

   @Environment(EnvType.CLIENT)
   public class CategoryEntry extends Entry {
      final Text text;
      private final int textWidth;

      public CategoryEntry(final Text text) {
         this.text = text;
         this.textWidth = ControlsListWidget.this.client.textRenderer.getWidth((StringVisitable)this.text);
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         TextRenderer var10001 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.text;
         int var10003 = ControlsListWidget.this.width / 2 - this.textWidth / 2;
         int var10004 = y + entryHeight;
         Objects.requireNonNull(ControlsListWidget.this.client.textRenderer);
         context.drawText(var10001, (Text)var10002, var10003, var10004 - 9 - 1, -1, false);
      }

      @Nullable
      public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
         return null;
      }

      public List children() {
         return Collections.emptyList();
      }

      public List selectableChildren() {
         return ImmutableList.of(new Selectable() {
            public Selectable.SelectionType getType() {
               return Selectable.SelectionType.HOVERED;
            }

            public void appendNarrations(NarrationMessageBuilder builder) {
               builder.put(NarrationPart.TITLE, CategoryEntry.this.text);
            }
         });
      }

      protected void update() {
      }
   }

   @Environment(EnvType.CLIENT)
   public class KeyBindingEntry extends Entry {
      private static final Text RESET_TEXT = Text.translatable("controls.reset");
      private static final int field_49535 = 10;
      private final KeyBinding binding;
      private final Text bindingName;
      private final ButtonWidget editButton;
      private final ButtonWidget resetButton;
      private boolean duplicate = false;

      KeyBindingEntry(final KeyBinding binding, final Text bindingName) {
         this.binding = binding;
         this.bindingName = bindingName;
         this.editButton = ButtonWidget.builder(bindingName, (button) -> {
            ControlsListWidget.this.parent.selectedKeyBinding = binding;
            ControlsListWidget.this.update();
         }).dimensions(0, 0, 75, 20).narrationSupplier((textSupplier) -> binding.isUnbound() ? Text.translatable("narrator.controls.unbound", new Object[]{bindingName}) : Text.translatable("narrator.controls.bound", new Object[]{bindingName, textSupplier.get()})).build();
         this.resetButton = ButtonWidget.builder(RESET_TEXT, (button) -> {
            ControlsListWidget.this.client.options.setKeyCode(binding, binding.getDefaultKey());
            ControlsListWidget.this.update();
         }).dimensions(0, 0, 50, 20).narrationSupplier((textSupplier) -> Text.translatable("narrator.controls.reset", new Object[]{bindingName})).build();
         this.update();
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         int i = ControlsListWidget.this.getScrollbarX() - this.resetButton.getWidth() - 10;
         int j = y - 2;
         this.resetButton.setPosition(i, j);
         this.resetButton.render(context, mouseX, mouseY, tickDelta);
         int k = i - 5 - this.editButton.getWidth();
         this.editButton.setPosition(k, j);
         this.editButton.render(context, mouseX, mouseY, tickDelta);
         TextRenderer var10001 = ControlsListWidget.this.client.textRenderer;
         Text var10002 = this.bindingName;
         int var10004 = y + entryHeight / 2;
         Objects.requireNonNull(ControlsListWidget.this.client.textRenderer);
         context.drawTextWithShadow(var10001, (Text)var10002, x, var10004 - 9 / 2, -1);
         if (this.duplicate) {
            int l = 3;
            int m = this.editButton.getX() - 6;
            context.fill(m, y - 1, m + 3, y + entryHeight, -65536);
         }

      }

      public List children() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      public List selectableChildren() {
         return ImmutableList.of(this.editButton, this.resetButton);
      }

      protected void update() {
         this.editButton.setMessage(this.binding.getBoundKeyLocalizedText());
         this.resetButton.active = !this.binding.isDefault();
         this.duplicate = false;
         MutableText mutableText = Text.empty();
         if (!this.binding.isUnbound()) {
            for(KeyBinding keyBinding : ControlsListWidget.this.client.options.allKeys) {
               if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
                  if (this.duplicate) {
                     mutableText.append(", ");
                  }

                  this.duplicate = true;
                  mutableText.append(Text.translatable(keyBinding.getTranslationKey()));
               }
            }
         }

         if (this.duplicate) {
            this.editButton.setMessage(Text.literal("[ ").append(this.editButton.getMessage().copy().formatted(Formatting.WHITE)).append(" ]").formatted(Formatting.RED));
            this.editButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", new Object[]{mutableText})));
         } else {
            this.editButton.setTooltip((Tooltip)null);
         }

         if (ControlsListWidget.this.parent.selectedKeyBinding == this.binding) {
            this.editButton.setMessage(Text.literal("> ").append(this.editButton.getMessage().copy().formatted(new Formatting[]{Formatting.WHITE, Formatting.UNDERLINE})).append(" <").formatted(Formatting.YELLOW));
         }

      }
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry extends ElementListWidget.Entry {
      abstract void update();
   }
}
