package net.minecraft.client.gui.screen;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.ServerLinks;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ServerLinksScreen extends Screen {
   private static final int LIST_WIDTH = 310;
   private static final int ENTRY_HEIGHT = 25;
   private static final Text TITLE = Text.translatable("menu.server_links.title");
   private final Screen parent;
   @Nullable
   private LinksListWidget list;
   final ThreePartsLayoutWidget layoutWidget = new ThreePartsLayoutWidget(this);
   final ServerLinks serverLinks;

   public ServerLinksScreen(Screen parent, ServerLinks serverLinks) {
      super(TITLE);
      this.parent = parent;
      this.serverLinks = serverLinks;
   }

   protected void init() {
      this.layoutWidget.addHeader(this.title, this.textRenderer);
      this.list = (LinksListWidget)this.layoutWidget.addBody(new LinksListWidget(this.client, this.width, this));
      this.layoutWidget.addFooter(ButtonWidget.builder(ScreenTexts.BACK, (button) -> this.close()).width(200).build());
      this.layoutWidget.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      this.initTabNavigation();
   }

   protected void initTabNavigation() {
      this.layoutWidget.refreshPositions();
      if (this.list != null) {
         this.list.position(this.width, this.layoutWidget);
      }

   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   @Environment(EnvType.CLIENT)
   static class LinksListEntry extends ElementListWidget.Entry {
      final ClickableWidget button;

      LinksListEntry(Screen screen, ServerLinks.Entry link) {
         this.button = ButtonWidget.builder(link.getText(), ConfirmLinkScreen.opening(screen, link.link(), false)).width(310).build();
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         this.button.setY(y);
         this.button.render(context, mouseX, mouseY, tickDelta);
      }

      public List children() {
         return List.of(this.button);
      }

      public List selectableChildren() {
         return List.of(this.button);
      }
   }

   @Environment(EnvType.CLIENT)
   static class LinksListWidget extends ElementListWidget {
      public LinksListWidget(MinecraftClient client, int width, ServerLinksScreen screen) {
         super(client, width, screen.layoutWidget.getContentHeight(), screen.layoutWidget.getHeaderHeight(), 25);
         screen.serverLinks.entries().forEach((entry) -> this.addEntry(new LinksListEntry(screen, entry)));
      }

      public int getRowWidth() {
         return 310;
      }

      public void position(int width, ThreePartsLayoutWidget layout) {
         super.position(width, layout);
         int i = width / 2 - 155;
         this.children().forEach((child) -> child.button.setX(i));
      }
   }
}
