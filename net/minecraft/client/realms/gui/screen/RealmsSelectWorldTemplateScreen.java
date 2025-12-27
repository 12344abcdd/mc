package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.RealmsObjectSelectionList;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.WorldTemplate;
import net.minecraft.client.realms.dto.WorldTemplatePaginatedList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.util.RealmsTextureManager;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Urls;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final Identifier SLOT_FRAME_TEXTURE = Identifier.ofVanilla("widget/slot_frame");
   private static final Text SELECT_TEXT = Text.translatable("mco.template.button.select");
   private static final Text TRAILER_TEXT = Text.translatable("mco.template.button.trailer");
   private static final Text PUBLISHER_TEXT = Text.translatable("mco.template.button.publisher");
   private static final int field_45974 = 100;
   private static final int field_45975 = 10;
   private final ThreePartsLayoutWidget layout;
   final Consumer callback;
   WorldTemplateObjectSelectionList templateList;
   private final RealmsServer.WorldType worldType;
   private ButtonWidget selectButton;
   private ButtonWidget trailerButton;
   private ButtonWidget publisherButton;
   @Nullable
   WorldTemplate selectedTemplate;
   @Nullable
   String currentLink;
   @Nullable
   private Text[] warning;
   @Nullable
   List noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(Text title, Consumer callback, RealmsServer.WorldType worldType) {
      this(title, callback, worldType, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(Text title, Consumer callback, RealmsServer.WorldType worldType, @Nullable WorldTemplatePaginatedList templateList) {
      super(title);
      this.layout = new ThreePartsLayoutWidget(this);
      this.selectedTemplate = null;
      this.callback = callback;
      this.worldType = worldType;
      if (templateList == null) {
         this.templateList = new WorldTemplateObjectSelectionList();
         this.setPagination(new WorldTemplatePaginatedList(10));
      } else {
         this.templateList = new WorldTemplateObjectSelectionList(Lists.newArrayList(templateList.templates));
         this.setPagination(templateList);
      }

   }

   public void setWarning(Text... warning) {
      this.warning = warning;
   }

   public void init() {
      this.layout.addHeader(this.title, this.textRenderer);
      this.templateList = (WorldTemplateObjectSelectionList)this.layout.addBody(new WorldTemplateObjectSelectionList(this.templateList.getValues()));
      DirectionalLayoutWidget directionalLayoutWidget = (DirectionalLayoutWidget)this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(10));
      directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
      this.trailerButton = (ButtonWidget)directionalLayoutWidget.add(ButtonWidget.builder(TRAILER_TEXT, (button) -> this.onTrailer()).width(100).build());
      this.selectButton = (ButtonWidget)directionalLayoutWidget.add(ButtonWidget.builder(SELECT_TEXT, (button) -> this.selectTemplate()).width(100).build());
      directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close()).width(100).build());
      this.publisherButton = (ButtonWidget)directionalLayoutWidget.add(ButtonWidget.builder(PUBLISHER_TEXT, (button) -> this.onPublish()).width(100).build());
      this.updateButtonStates();
      this.layout.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      this.initTabNavigation();
   }

   protected void initTabNavigation() {
      this.templateList.setDimensions(this.width, this.height - this.layout.getFooterHeight() - this.getTemplateListTop());
      this.layout.refreshPositions();
   }

   public Text getNarratedTitle() {
      List<Text> list = Lists.newArrayListWithCapacity(2);
      list.add(this.title);
      if (this.warning != null) {
         list.addAll(Arrays.asList(this.warning));
      }

      return ScreenTexts.joinLines(list);
   }

   void updateButtonStates() {
      this.publisherButton.visible = this.selectedTemplate != null && !this.selectedTemplate.link.isEmpty();
      this.trailerButton.visible = this.selectedTemplate != null && !this.selectedTemplate.trailer.isEmpty();
      this.selectButton.active = this.selectedTemplate != null;
   }

   public void close() {
      this.callback.accept((Object)null);
   }

   private void selectTemplate() {
      if (this.selectedTemplate != null) {
         this.callback.accept(this.selectedTemplate);
      }

   }

   private void onTrailer() {
      if (this.selectedTemplate != null && !this.selectedTemplate.trailer.isBlank()) {
         ConfirmLinkScreen.open(this, (String)this.selectedTemplate.trailer);
      }

   }

   private void onPublish() {
      if (this.selectedTemplate != null && !this.selectedTemplate.link.isBlank()) {
         ConfirmLinkScreen.open(this, (String)this.selectedTemplate.link);
      }

   }

   private void setPagination(final WorldTemplatePaginatedList templateList) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList worldTemplatePaginatedList = templateList;

            Either<WorldTemplatePaginatedList, Exception> either;
            for(RealmsClient realmsClient = RealmsClient.create(); worldTemplatePaginatedList != null; worldTemplatePaginatedList = (WorldTemplatePaginatedList)RealmsSelectWorldTemplateScreen.this.client.submit(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates", (Throwable)either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.translate("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  WorldTemplatePaginatedList worldTemplatePaginatedList = (WorldTemplatePaginatedList)either.left().get();

                  for(WorldTemplate worldTemplate : worldTemplatePaginatedList.templates) {
                     RealmsSelectWorldTemplateScreen.this.templateList.addEntry(worldTemplate);
                  }

                  if (worldTemplatePaginatedList.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.templateList.isEmpty()) {
                        String string = I18n.translate("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment lineSegment = TextRenderingUtils.LineSegment.link(I18n.translate("mco.template.select.none.linkTitle"), Urls.REALMS_CONTENT_CREATOR.toString());
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(string, lineSegment);
                     }

                     return null;
                  } else {
                     return worldTemplatePaginatedList;
                  }
               }
            }).join()) {
               either = RealmsSelectWorldTemplateScreen.this.fetchWorldTemplates(worldTemplatePaginatedList, realmsClient);
            }

         }
      }).start();
   }

   Either fetchWorldTemplates(WorldTemplatePaginatedList templateList, RealmsClient realms) {
      try {
         return Either.left(realms.fetchWorldTemplates(templateList.page + 1, templateList.size, this.worldType));
      } catch (RealmsServiceException realmsServiceException) {
         return Either.right(realmsServiceException);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.currentLink = null;
      if (this.noTemplatesMessage != null) {
         this.renderMessages(context, mouseX, mouseY, this.noTemplatesMessage);
      }

      if (this.warning != null) {
         for(int i = 0; i < this.warning.length; ++i) {
            Text text = this.warning[i];
            context.drawCenteredTextWithShadow(this.textRenderer, text, this.width / 2, row(-1 + i), -6250336);
         }
      }

   }

   private void renderMessages(DrawContext context, int x, int y, List messages) {
      for(int i = 0; i < messages.size(); ++i) {
         TextRenderingUtils.Line line = (TextRenderingUtils.Line)messages.get(i);
         int j = row(4 + i);
         int k = line.segments.stream().mapToInt((segment) -> this.textRenderer.getWidth(segment.renderedText())).sum();
         int l = this.width / 2 - k / 2;

         for(TextRenderingUtils.LineSegment lineSegment : line.segments) {
            int m = lineSegment.isLink() ? 3368635 : -1;
            int n = context.drawTextWithShadow(this.textRenderer, lineSegment.renderedText(), l, j, m);
            if (lineSegment.isLink() && x > l && x < n && y > j - 3 && y < j + 8) {
               this.setTooltip(Text.literal(lineSegment.getLinkUrl()));
               this.currentLink = lineSegment.getLinkUrl();
            }

            l = n;
         }
      }

   }

   int getTemplateListTop() {
      return this.warning != null ? row(1) : 33;
   }

   @Environment(EnvType.CLIENT)
   class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(final Iterable templates) {
         super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height - 33 - RealmsSelectWorldTemplateScreen.this.getTemplateListTop(), RealmsSelectWorldTemplateScreen.this.getTemplateListTop(), 46);
         templates.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate template) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new WorldTemplateObjectSelectionListEntry(template));
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
            ConfirmLinkScreen.open(RealmsSelectWorldTemplateScreen.this, (String)RealmsSelectWorldTemplateScreen.this.currentLink);
            return true;
         } else {
            return super.mouseClicked(mouseX, mouseY, button);
         }
      }

      public void setSelected(@Nullable WorldTemplateObjectSelectionListEntry worldTemplateObjectSelectionListEntry) {
         super.setSelected(worldTemplateObjectSelectionListEntry);
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = worldTemplateObjectSelectionListEntry == null ? null : worldTemplateObjectSelectionListEntry.mTemplate;
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public int getMaxPosition() {
         return this.getEntryCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public boolean isEmpty() {
         return this.getEntryCount() == 0;
      }

      public List getValues() {
         return (List)this.children().stream().map((child) -> child.mTemplate).collect(Collectors.toList());
      }
   }

   @Environment(EnvType.CLIENT)
   class WorldTemplateObjectSelectionListEntry extends AlwaysSelectedEntryListWidget.Entry {
      private static final ButtonTextures LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/link"), Identifier.ofVanilla("icon/link_highlighted"));
      private static final ButtonTextures VIDEO_LINK_TEXTURES = new ButtonTextures(Identifier.ofVanilla("icon/video_link"), Identifier.ofVanilla("icon/video_link_highlighted"));
      private static final Text INFO_TOOLTIP_TEXT = Text.translatable("mco.template.info.tooltip");
      private static final Text TRAILER_TOOLTIP_TEXT = Text.translatable("mco.template.trailer.tooltip");
      public final WorldTemplate mTemplate;
      private long prevClickTime;
      @Nullable
      private TexturedButtonWidget infoButton;
      @Nullable
      private TexturedButtonWidget trailerButton;

      public WorldTemplateObjectSelectionListEntry(final WorldTemplate template) {
         this.mTemplate = template;
         if (!template.link.isBlank()) {
            this.infoButton = new TexturedButtonWidget(15, 15, LINK_TEXTURES, ConfirmLinkScreen.opening(RealmsSelectWorldTemplateScreen.this, (String)template.link), INFO_TOOLTIP_TEXT);
            this.infoButton.setTooltip(Tooltip.of(INFO_TOOLTIP_TEXT));
         }

         if (!template.trailer.isBlank()) {
            this.trailerButton = new TexturedButtonWidget(15, 15, VIDEO_LINK_TEXTURES, ConfirmLinkScreen.opening(RealmsSelectWorldTemplateScreen.this, (String)template.trailer), TRAILER_TOOLTIP_TEXT);
            this.trailerButton.setTooltip(Tooltip.of(TRAILER_TOOLTIP_TEXT));
         }

      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.mTemplate;
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
         if (Util.getMeasuringTimeMs() - this.prevClickTime < 250L && this.isFocused()) {
            RealmsSelectWorldTemplateScreen.this.callback.accept(this.mTemplate);
         }

         this.prevClickTime = Util.getMeasuringTimeMs();
         if (this.infoButton != null) {
            this.infoButton.mouseClicked(mouseX, mouseY, button);
         }

         if (this.trailerButton != null) {
            this.trailerButton.mouseClicked(mouseX, mouseY, button);
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }

      public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
         context.drawTexture(RealmsTextureManager.getTextureId(this.mTemplate.id, this.mTemplate.image), x + 1, y + 1 + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         context.drawGuiTexture(RealmsSelectWorldTemplateScreen.SLOT_FRAME_TEXTURE, x, y + 1, 40, 40);
         int i = 5;
         int j = RealmsSelectWorldTemplateScreen.this.textRenderer.getWidth(this.mTemplate.version);
         if (this.infoButton != null) {
            this.infoButton.setPosition(x + entryWidth - j - this.infoButton.getWidth() - 10, y);
            this.infoButton.render(context, mouseX, mouseY, tickDelta);
         }

         if (this.trailerButton != null) {
            this.trailerButton.setPosition(x + entryWidth - j - this.trailerButton.getWidth() * 2 - 15, y);
            this.trailerButton.render(context, mouseX, mouseY, tickDelta);
         }

         int k = x + 45 + 20;
         int l = y + 5;
         context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, (String)this.mTemplate.name, k, l, -1, false);
         context.drawText(RealmsSelectWorldTemplateScreen.this.textRenderer, this.mTemplate.version, x + entryWidth - j - 5, l, 7105644, false);
         TextRenderer var10001 = RealmsSelectWorldTemplateScreen.this.textRenderer;
         String var10002 = this.mTemplate.author;
         Objects.requireNonNull(RealmsSelectWorldTemplateScreen.this.textRenderer);
         context.drawText(var10001, var10002, k, l + 9 + 5, -6250336, false);
         if (!this.mTemplate.recommendedPlayers.isBlank()) {
            var10001 = RealmsSelectWorldTemplateScreen.this.textRenderer;
            var10002 = this.mTemplate.recommendedPlayers;
            int var10004 = y + entryHeight;
            Objects.requireNonNull(RealmsSelectWorldTemplateScreen.this.textRenderer);
            context.drawText(var10001, var10002, k, var10004 - 9 / 2 - 5, 5000268, false);
         }

      }

      public Text getNarration() {
         Text text = ScreenTexts.joinLines(new Text[]{Text.literal(this.mTemplate.name), Text.translatable("mco.template.select.narrate.authors", new Object[]{this.mTemplate.author}), Text.literal(this.mTemplate.recommendedPlayers), Text.translatable("mco.template.select.narrate.version", new Object[]{this.mTemplate.version})});
         return Text.translatable("narrator.select", new Object[]{text});
      }
   }
}
