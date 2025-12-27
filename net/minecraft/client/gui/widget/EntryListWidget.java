package net.minecraft.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class EntryListWidget extends ContainerWidget {
   protected static final int field_45909 = 6;
   private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("widget/scroller");
   private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.ofVanilla("widget/scroller_background");
   private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
   private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");
   protected final MinecraftClient client;
   protected final int itemHeight;
   private final List children = new Entries();
   protected boolean centerListVertically = true;
   private double scrollAmount;
   private boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;
   @Nullable
   private Entry selected;
   @Nullable
   private Entry hoveredEntry;

   public EntryListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
      super(0, y, width, height, ScreenTexts.EMPTY);
      this.client = client;
      this.itemHeight = itemHeight;
   }

   protected void setRenderHeader(boolean renderHeader, int headerHeight) {
      this.renderHeader = renderHeader;
      this.headerHeight = headerHeight;
      if (!renderHeader) {
         this.headerHeight = 0;
      }

   }

   public int getRowWidth() {
      return 220;
   }

   @Nullable
   public Entry getSelectedOrNull() {
      return this.selected;
   }

   public void setSelected(@Nullable Entry entry) {
      this.selected = entry;
   }

   public Entry getFirst() {
      return (Entry)this.children.get(0);
   }

   @Nullable
   public Entry getFocused() {
      return (Entry)super.getFocused();
   }

   public final List children() {
      return this.children;
   }

   protected void clearEntries() {
      this.children.clear();
      this.selected = null;
   }

   protected void replaceEntries(Collection newEntries) {
      this.clearEntries();
      this.children.addAll(newEntries);
   }

   protected Entry getEntry(int index) {
      return (Entry)this.children().get(index);
   }

   protected int addEntry(Entry entry) {
      this.children.add(entry);
      return this.children.size() - 1;
   }

   protected void addEntryToTop(Entry entry) {
      double d = (double)this.getMaxScroll() - this.getScrollAmount();
      this.children.add(0, entry);
      this.setScrollAmount((double)this.getMaxScroll() - d);
   }

   protected boolean removeEntryWithoutScrolling(Entry entry) {
      double d = (double)this.getMaxScroll() - this.getScrollAmount();
      boolean bl = this.removeEntry(entry);
      this.setScrollAmount((double)this.getMaxScroll() - d);
      return bl;
   }

   protected int getEntryCount() {
      return this.children().size();
   }

   protected boolean isSelectedEntry(int index) {
      return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
   }

   @Nullable
   protected final Entry getEntryAtPosition(double x, double y) {
      int i = this.getRowWidth() / 2;
      int j = this.getX() + this.width / 2;
      int k = j - i;
      int l = j + i;
      int m = MathHelper.floor(y - (double)this.getY()) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int n = m / this.itemHeight;
      return x >= (double)k && x <= (double)l && n >= 0 && m >= 0 && n < this.getEntryCount() ? (Entry)this.children().get(n) : null;
   }

   public void position(int width, ThreePartsLayoutWidget layout) {
      this.position(width, layout.getContentHeight(), layout.getHeaderHeight());
   }

   public void position(int width, int height, int y) {
      this.setDimensions(width, height);
      this.setPosition(0, y);
      this.refreshScroll();
   }

   protected int getMaxPosition() {
      return this.getEntryCount() * this.itemHeight + this.headerHeight;
   }

   protected boolean clickedHeader(int x, int y) {
      return false;
   }

   protected void renderHeader(DrawContext context, int x, int y) {
   }

   protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
   }

   public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      this.hoveredEntry = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPosition((double)mouseX, (double)mouseY) : null;
      this.drawMenuListBackground(context);
      this.enableScissor(context);
      if (this.renderHeader) {
         int i = this.getRowLeft();
         int j = this.getY() + 4 - (int)this.getScrollAmount();
         this.renderHeader(context, i, j);
      }

      this.renderList(context, mouseX, mouseY, delta);
      context.disableScissor();
      this.drawHeaderAndFooterSeparators(context);
      if (this.isScrollbarVisible()) {
         int i = this.getScrollbarX();
         int j = (int)((float)(this.height * this.height) / (float)this.getMaxPosition());
         j = MathHelper.clamp(j, 32, this.height - 8);
         int k = (int)this.getScrollAmount() * (this.height - j) / this.getMaxScroll() + this.getY();
         if (k < this.getY()) {
            k = this.getY();
         }

         RenderSystem.enableBlend();
         context.drawGuiTexture(SCROLLER_BACKGROUND_TEXTURE, i, this.getY(), 6, this.getHeight());
         context.drawGuiTexture(SCROLLER_TEXTURE, i, k, 6, j);
         RenderSystem.disableBlend();
      }

      this.renderDecorations(context, mouseX, mouseY);
      RenderSystem.disableBlend();
   }

   protected boolean isScrollbarVisible() {
      return this.getMaxScroll() > 0;
   }

   protected void drawHeaderAndFooterSeparators(DrawContext context) {
      RenderSystem.enableBlend();
      Identifier identifier = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
      Identifier identifier2 = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
      context.drawTexture(identifier, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
      context.drawTexture(identifier2, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2);
      RenderSystem.disableBlend();
   }

   protected void drawMenuListBackground(DrawContext context) {
      RenderSystem.enableBlend();
      Identifier identifier = this.client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
      context.drawTexture(identifier, this.getX(), this.getY(), (float)this.getRight(), (float)(this.getBottom() + (int)this.getScrollAmount()), this.getWidth(), this.getHeight(), 32, 32);
      RenderSystem.disableBlend();
   }

   protected void enableScissor(DrawContext context) {
      context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
   }

   protected void centerScrollOn(Entry entry) {
      this.setScrollAmount((double)(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - this.height / 2));
   }

   protected void ensureVisible(Entry entry) {
      int i = this.getRowTop(this.children().indexOf(entry));
      int j = i - this.getY() - 4 - this.itemHeight;
      if (j < 0) {
         this.scroll(j);
      }

      int k = this.getBottom() - i - this.itemHeight - this.itemHeight;
      if (k < 0) {
         this.scroll(-k);
      }

   }

   private void scroll(int amount) {
      this.setScrollAmount(this.getScrollAmount() + (double)amount);
   }

   public double getScrollAmount() {
      return this.scrollAmount;
   }

   public void setScrollAmountOnly(double amount) {
      this.scrollAmount = MathHelper.clamp(amount, (double)0.0F, (double)this.getMaxScroll());
   }

   public void setScrollAmount(double amount) {
      this.setScrollAmountOnly(amount);
   }

   public void refreshScroll() {
      this.setScrollAmountOnly(this.getScrollAmount());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - (this.height - 4));
   }

   protected void updateScrollingState(double mouseX, double mouseY, int button) {
      this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarX() && mouseX < (double)(this.getScrollbarX() + 6);
   }

   protected int getScrollbarX() {
      return this.getDefaultScrollbarX();
   }

   protected int getDefaultScrollbarX() {
      return this.getBorderBoxRight() + this.getScrollbarMarginX();
   }

   private int getScrollbarMarginX() {
      return 10;
   }

   protected boolean isSelectButton(int button) {
      return button == 0;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isSelectButton(button)) {
         return false;
      } else {
         this.updateScrollingState(mouseX, mouseY, button);
         if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
         } else {
            E entry = (E)this.getEntryAtPosition(mouseX, mouseY);
            if (entry != null) {
               if (entry.mouseClicked(mouseX, mouseY, button)) {
                  E entry2 = (E)this.getFocused();
                  if (entry2 != entry && entry2 instanceof ParentElement) {
                     ParentElement parentElement = (ParentElement)entry2;
                     parentElement.setFocused((Element)null);
                  }

                  this.setFocused(entry);
                  this.setDragging(true);
                  return true;
               }
            } else if (this.clickedHeader((int)(mouseX - (double)(this.getX() + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.getY()) + (int)this.getScrollAmount() - 4)) {
               return true;
            }

            return this.scrolling;
         }
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.getFocused() != null ? this.getFocused().mouseReleased(mouseX, mouseY, button) : false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
         return true;
      } else if (button == 0 && this.scrolling) {
         if (mouseY < (double)this.getY()) {
            this.setScrollAmount((double)0.0F);
         } else if (mouseY > (double)this.getBottom()) {
            this.setScrollAmount((double)this.getMaxScroll());
         } else {
            double d = (double)Math.max(1, this.getMaxScroll());
            int i = this.height;
            int j = MathHelper.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
            double e = Math.max((double)1.0F, d / (double)(i - j));
            this.setScrollAmount(this.getScrollAmount() + deltaY * e);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.setScrollAmount(this.getScrollAmount() - verticalAmount * (double)this.itemHeight / (double)2.0F);
      return true;
   }

   public void setFocused(@Nullable Element focused) {
      super.setFocused(focused);
      int i = this.children.indexOf(focused);
      if (i >= 0) {
         E entry = (E)((Entry)this.children.get(i));
         this.setSelected(entry);
         if (this.client.getNavigationType().isKeyboard()) {
            this.ensureVisible(entry);
         }
      }

   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction) {
      return this.getNeighboringEntry(direction, (entry) -> true);
   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction, Predicate predicate) {
      return this.getNeighboringEntry(direction, predicate, this.getSelectedOrNull());
   }

   @Nullable
   protected Entry getNeighboringEntry(NavigationDirection direction, Predicate predicate, @Nullable Entry selected) {
      byte var10000;
      switch (direction) {
         case RIGHT:
         case LEFT:
            var10000 = 0;
            break;
         case UP:
            var10000 = -1;
            break;
         case DOWN:
            var10000 = 1;
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      int i = var10000;
      if (!this.children().isEmpty() && i != 0) {
         int j;
         if (selected == null) {
            j = i > 0 ? 0 : this.children().size() - 1;
         } else {
            j = this.children().indexOf(selected) + i;
         }

         for(int k = j; k >= 0 && k < this.children.size(); k += i) {
            E entry = (E)((Entry)this.children().get(k));
            if (predicate.test(entry)) {
               return entry;
            }
         }
      }

      return null;
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return mouseY >= (double)this.getY() && mouseY <= (double)this.getBottom() && mouseX >= (double)this.getX() && mouseX <= (double)this.getRight();
   }

   protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
      int i = this.getRowLeft();
      int j = this.getRowWidth();
      int k = this.itemHeight - 4;
      int l = this.getEntryCount();

      for(int m = 0; m < l; ++m) {
         int n = this.getRowTop(m);
         int o = this.getRowBottom(m);
         if (o >= this.getY() && n <= this.getBottom()) {
            this.renderEntry(context, mouseX, mouseY, delta, m, i, n, j, k);
         }
      }

   }

   protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
      E entry = (E)this.getEntry(index);
      entry.drawBorder(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);
      if (this.isSelectedEntry(index)) {
         int i = this.isFocused() ? -1 : -8355712;
         this.drawSelectionHighlight(context, y, entryWidth, entryHeight, i, -16777216);
      }

      entry.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);
   }

   protected void drawSelectionHighlight(DrawContext context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
      int i = this.getX() + (this.width - entryWidth) / 2;
      int j = this.getX() + (this.width + entryWidth) / 2;
      context.fill(i, y - 2, j, y + entryHeight + 2, borderColor);
      context.fill(i + 1, y - 1, j - 1, y + entryHeight + 1, fillColor);
   }

   public int getRowLeft() {
      return this.getX() + this.width / 2 - this.getRowWidth() / 2 + 2;
   }

   private int getBorderBoxLeft() {
      return this.getX() + this.width / 2 - this.getRowWidth() / 2;
   }

   public int getRowRight() {
      return this.getRowLeft() + this.getRowWidth();
   }

   private int getBorderBoxRight() {
      return this.getBorderBoxLeft() + this.getRowWidth();
   }

   protected int getRowTop(int index) {
      return this.getY() + 4 - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
   }

   protected int getRowBottom(int index) {
      return this.getRowTop(index) + this.itemHeight;
   }

   public Selectable.SelectionType getType() {
      if (this.isFocused()) {
         return Selectable.SelectionType.FOCUSED;
      } else {
         return this.hoveredEntry != null ? Selectable.SelectionType.HOVERED : Selectable.SelectionType.NONE;
      }
   }

   @Nullable
   protected Entry remove(int index) {
      E entry = (E)((Entry)this.children.get(index));
      return this.removeEntry((Entry)this.children.get(index)) ? entry : null;
   }

   protected boolean removeEntry(Entry entry) {
      boolean bl = this.children.remove(entry);
      if (bl && entry == this.getSelectedOrNull()) {
         this.setSelected((Entry)null);
      }

      return bl;
   }

   @Nullable
   protected Entry getHoveredEntry() {
      return this.hoveredEntry;
   }

   void setEntryParentList(Entry entry) {
      entry.parentList = this;
   }

   protected void appendNarrations(NarrationMessageBuilder builder, Entry entry) {
      List<E> list = this.children();
      if (list.size() > 1) {
         int i = list.indexOf(entry);
         if (i != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.list", new Object[]{i + 1, list.size()}));
         }
      }

   }

   // $FF: synthetic method
   @Nullable
   public Element getFocused() {
      return this.getFocused();
   }

   @Environment(EnvType.CLIENT)
   class Entries extends AbstractList {
      private final List entries = Lists.newArrayList();

      public Entry get(int i) {
         return (Entry)this.entries.get(i);
      }

      public int size() {
         return this.entries.size();
      }

      public Entry set(int i, Entry entry) {
         E entry2 = (E)((Entry)this.entries.set(i, entry));
         EntryListWidget.this.setEntryParentList(entry);
         return entry2;
      }

      public void add(int i, Entry entry) {
         this.entries.add(i, entry);
         EntryListWidget.this.setEntryParentList(entry);
      }

      public Entry remove(int i) {
         return (Entry)this.entries.remove(i);
      }

      // $FF: synthetic method
      public Object remove(final int index) {
         return this.remove(index);
      }

      // $FF: synthetic method
      public void add(final int index, final Object entry) {
         this.add(index, (Entry)entry);
      }

      // $FF: synthetic method
      public Object set(final int index, final Object entry) {
         return this.set(index, (Entry)entry);
      }

      // $FF: synthetic method
      public Object get(final int index) {
         return this.get(index);
      }
   }

   @Environment(EnvType.CLIENT)
   protected abstract static class Entry implements Element {
      /** @deprecated */
      @Deprecated
      EntryListWidget parentList;

      public void setFocused(boolean focused) {
      }

      public boolean isFocused() {
         return this.parentList.getFocused() == this;
      }

      public abstract void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);

      public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
      }

      public boolean isMouseOver(double mouseX, double mouseY) {
         return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
      }
   }
}
