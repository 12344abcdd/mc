package net.minecraft.client.gui.screen.world;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ExperimentsScreen extends Screen {
   private static final Text TITLE = Text.translatable("selectWorld.experiments");
   private static final Text INFO_TEXT;
   private static final int INFO_WIDTH = 310;
   private final ThreePartsLayoutWidget experimentToggleList = new ThreePartsLayoutWidget(this);
   private final Screen parent;
   private final ResourcePackManager resourcePackManager;
   private final Consumer applier;
   private final Object2BooleanMap experiments = new Object2BooleanLinkedOpenHashMap();

   public ExperimentsScreen(Screen parent, ResourcePackManager resourcePackManager, Consumer applier) {
      super(TITLE);
      this.parent = parent;
      this.resourcePackManager = resourcePackManager;
      this.applier = applier;

      for(ResourcePackProfile resourcePackProfile : resourcePackManager.getProfiles()) {
         if (resourcePackProfile.getSource() == ResourcePackSource.FEATURE) {
            this.experiments.put(resourcePackProfile, resourcePackManager.getEnabledProfiles().contains(resourcePackProfile));
         }
      }

   }

   protected void init() {
      this.experimentToggleList.addHeader(TITLE, this.textRenderer);
      DirectionalLayoutWidget directionalLayoutWidget = (DirectionalLayoutWidget)this.experimentToggleList.addBody(DirectionalLayoutWidget.vertical());
      directionalLayoutWidget.add((new MultilineTextWidget(INFO_TEXT, this.textRenderer)).setMaxWidth(310), (Consumer)((positioner) -> positioner.marginBottom(15)));
      WorldScreenOptionGrid.Builder builder = WorldScreenOptionGrid.builder(310).withTooltipBox(2, true).setRowSpacing(4);
      this.experiments.forEach((pack, enabled) -> builder.add(getDataPackName(pack), () -> this.experiments.getBoolean(pack), (enabledx) -> this.experiments.put(pack, enabledx)).tooltip(pack.getDescription()));
      Objects.requireNonNull(directionalLayoutWidget);
      builder.build(directionalLayoutWidget::add);
      DirectionalLayoutWidget directionalLayoutWidget2 = (DirectionalLayoutWidget)this.experimentToggleList.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
      directionalLayoutWidget2.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.applyAndClose()).build());
      directionalLayoutWidget2.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close()).build());
      this.experimentToggleList.forEachChild((widget) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(widget);
      });
      this.initTabNavigation();
   }

   private static Text getDataPackName(ResourcePackProfile packProfile) {
      String string = "dataPack." + packProfile.getId() + ".name";
      return (Text)(I18n.hasTranslation(string) ? Text.translatable(string) : packProfile.getDisplayName());
   }

   protected void initTabNavigation() {
      this.experimentToggleList.refreshPositions();
   }

   public Text getNarratedTitle() {
      return ScreenTexts.joinSentences(new Text[]{super.getNarratedTitle(), INFO_TEXT});
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   private void applyAndClose() {
      List<ResourcePackProfile> list = new ArrayList(this.resourcePackManager.getEnabledProfiles());
      List<ResourcePackProfile> list2 = new ArrayList();
      this.experiments.forEach((pack, enabled) -> {
         list.remove(pack);
         if (enabled) {
            list2.add(pack);
         }

      });
      list.addAll(Lists.reverse(list2));
      this.resourcePackManager.setEnabledProfiles(list.stream().map(ResourcePackProfile::getId).toList());
      this.applier.accept(this.resourcePackManager);
   }

   static {
      INFO_TEXT = Text.translatable("selectWorld.experiments.info").formatted(Formatting.RED);
   }
}
