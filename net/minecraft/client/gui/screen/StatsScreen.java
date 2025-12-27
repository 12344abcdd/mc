package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.gui.RealmsLoadingWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen {
   private static final Text TITLE_TEXT = Text.translatable("gui.stats");
   static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
   static final Identifier HEADER_TEXTURE = Identifier.ofVanilla("statistics/header");
   static final Identifier SORT_UP_TEXTURE = Identifier.ofVanilla("statistics/sort_up");
   static final Identifier SORT_DOWN_TEXTURE = Identifier.ofVanilla("statistics/sort_down");
   private static final Text DOWNLOADING_STATS_TEXT = Text.translatable("multiplayer.downloadingStats");
   static final Text NONE_TEXT = Text.translatable("stats.none");
   private static final Text GENERAL_BUTTON_TEXT = Text.translatable("stat.generalButton");
   private static final Text ITEM_BUTTON_TEXT = Text.translatable("stat.itemsButton");
   private static final Text MOBS_BUTTON_TEXT = Text.translatable("stat.mobsButton");
   protected final Screen parent;
   private static final int field_49520 = 280;
   private static final int field_49521 = 5;
   private static final int field_49522 = 58;
   private ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 33, 58);
   @Nullable
   private GeneralStatsListWidget generalStats;
   @Nullable
   ItemStatsListWidget itemStats;
   @Nullable
   private EntityStatsListWidget mobStats;
   final StatHandler statHandler;
   @Nullable
   private AlwaysSelectedEntryListWidget selectedList;
   private boolean downloadingStats = true;

   public StatsScreen(Screen parent, StatHandler statHandler) {
      super(TITLE_TEXT);
      this.parent = parent;
      this.statHandler = statHandler;
   }

   protected void init() {
      this.layout.addBody(new RealmsLoadingWidget(this.textRenderer, DOWNLOADING_STATS_TEXT));
      this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(Mode.REQUEST_STATS));
   }

   public void createLists() {
      this.generalStats = new GeneralStatsListWidget(this.client);
      this.itemStats = new ItemStatsListWidget(this.client);
      this.mobStats = new EntityStatsListWidget(this.client);
   }

   public void createButtons() {
      ThreePartsLayoutWidget threePartsLayoutWidget = new ThreePartsLayoutWidget(this, 33, 58);
      threePartsLayoutWidget.addHeader(TITLE_TEXT, this.textRenderer);
      DirectionalLayoutWidget directionalLayoutWidget = ((DirectionalLayoutWidget)threePartsLayoutWidget.addFooter(DirectionalLayoutWidget.vertical())).spacing(5);
      directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
      DirectionalLayoutWidget directionalLayoutWidget2 = ((DirectionalLayoutWidget)directionalLayoutWidget.add(DirectionalLayoutWidget.horizontal())).spacing(5);
      directionalLayoutWidget2.add(ButtonWidget.builder(GENERAL_BUTTON_TEXT, (button) -> this.selectStatList(this.generalStats)).width(120).build());
      ButtonWidget buttonWidget = (ButtonWidget)directionalLayoutWidget2.add(ButtonWidget.builder(ITEM_BUTTON_TEXT, (button) -> this.selectStatList(this.itemStats)).width(120).build());
      ButtonWidget buttonWidget2 = (ButtonWidget)directionalLayoutWidget2.add(ButtonWidget.builder(MOBS_BUTTON_TEXT, (button) -> this.selectStatList(this.mobStats)).width(120).build());
      directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.close()).width(200).build());
      if (this.itemStats != null && this.itemStats.children().isEmpty()) {
         buttonWidget.active = false;
      }

      if (this.mobStats != null && this.mobStats.children().isEmpty()) {
         buttonWidget2.active = false;
      }

      this.layout = threePartsLayoutWidget;
      this.layout.forEachChild((child) -> {
         ClickableWidget var10000 = (ClickableWidget)this.addDrawableChild(child);
      });
      this.initTabNavigation();
   }

   protected void initTabNavigation() {
      this.layout.refreshPositions();
      if (this.selectedList != null) {
         this.selectedList.position(this.width, this.layout);
      }

   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public void onStatsReady() {
      if (this.downloadingStats) {
         this.createLists();
         this.selectStatList(this.generalStats);
         this.createButtons();
         this.setInitialFocus();
         this.downloadingStats = false;
      }

   }

   public boolean shouldPause() {
      return !this.downloadingStats;
   }

   public void selectStatList(@Nullable AlwaysSelectedEntryListWidget list) {
      if (this.selectedList != null) {
         this.remove(this.selectedList);
      }

      if (list != null) {
         this.addDrawableChild(list);
         this.selectedList = list;
         this.initTabNavigation();
      }

   }

   static String getStatTranslationKey(Stat stat) {
      String var10000 = ((Identifier)stat.getValue()).toString();
      return "stat." + var10000.replace(':', '.');
   }

   @Environment(EnvType.CLIENT)
   class EntityStatsListWidget extends AlwaysSelectedEntryListWidget {
      public EntityStatsListWidget(final MinecraftClient client) {
         int var10002 = StatsScreen.this.width;
         int var10003 = StatsScreen.this.height - 33 - 58;
         Objects.requireNonNull(StatsScreen.this.textRenderer);
         super(client, var10002, var10003, 33, 9 * 4);

         for(EntityType entityType : Registries.ENTITY_TYPE) {
            if (StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType)) > 0 || StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType)) > 0) {
               this.addEntry(new Entry(entityType));
            }
         }

      }

      public int getRowWidth() {
         return 280;
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Text entityTypeName;
         private final Text killedText;
         private final Text killedByText;
         private final boolean killedAny;
         private final boolean killedByAny;

         public Entry(final EntityType entityType) {
            this.entityTypeName = entityType.getName();
            int i = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType));
            if (i == 0) {
               this.killedText = Text.translatable("stat_type.minecraft.killed.none", new Object[]{this.entityTypeName});
               this.killedAny = false;
            } else {
               this.killedText = Text.translatable("stat_type.minecraft.killed", new Object[]{i, this.entityTypeName});
               this.killedAny = true;
            }

            int j = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType));
            if (j == 0) {
               this.killedByText = Text.translatable("stat_type.minecraft.killed_by.none", new Object[]{this.entityTypeName});
               this.killedByAny = false;
            } else {
               this.killedByText = Text.translatable("stat_type.minecraft.killed_by", new Object[]{this.entityTypeName, j});
               this.killedByAny = true;
            }

         }

         public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(StatsScreen.this.textRenderer, (Text)this.entityTypeName, x + 2, y + 1, -1);
            TextRenderer var10001 = StatsScreen.this.textRenderer;
            Text var10002 = this.killedText;
            int var10003 = x + 2 + 10;
            int var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            context.drawTextWithShadow(var10001, var10002, var10003, var10004 + 9, this.killedAny ? -4539718 : -8355712);
            var10001 = StatsScreen.this.textRenderer;
            var10002 = this.killedByText;
            var10003 = x + 2 + 10;
            var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            context.drawTextWithShadow(var10001, var10002, var10003, var10004 + 9 * 2, this.killedByAny ? -4539718 : -8355712);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", new Object[]{ScreenTexts.joinSentences(new Text[]{this.killedText, this.killedByText})});
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class GeneralStatsListWidget extends AlwaysSelectedEntryListWidget {
      public GeneralStatsListWidget(final MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 14);
         ObjectArrayList<Stat<Identifier>> objectArrayList = new ObjectArrayList(Stats.CUSTOM.iterator());
         objectArrayList.sort(Comparator.comparing((statx) -> I18n.translate(StatsScreen.getStatTranslationKey(statx))));
         ObjectListIterator var4 = objectArrayList.iterator();

         while(var4.hasNext()) {
            Stat<Identifier> stat = (Stat)var4.next();
            this.addEntry(new Entry(stat));
         }

      }

      public int getRowWidth() {
         return 280;
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Stat stat;
         private final Text displayName;

         Entry(final Stat stat) {
            this.stat = stat;
            this.displayName = Text.translatable(StatsScreen.getStatTranslationKey(stat));
         }

         private String getFormatted() {
            return this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
         }

         public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int var10000 = y + entryHeight / 2;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            int i = var10000 - 9 / 2;
            int j = index % 2 == 0 ? -1 : -4539718;
            context.drawTextWithShadow(StatsScreen.this.textRenderer, this.displayName, x + 2, i, j);
            String string = this.getFormatted();
            context.drawTextWithShadow(StatsScreen.this.textRenderer, string, x + entryWidth - StatsScreen.this.textRenderer.getWidth(string) - 4, i, j);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", new Object[]{Text.empty().append(this.displayName).append(ScreenTexts.SPACE).append(this.getFormatted())});
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class ItemStatsListWidget extends AlwaysSelectedEntryListWidget {
      private static final int field_49524 = 18;
      private static final int field_49525 = 22;
      private static final int field_49526 = 1;
      private static final int field_49527 = 0;
      private static final int field_49528 = -1;
      private static final int field_49529 = 1;
      private final Identifier[] headerIconTextures = new Identifier[]{Identifier.ofVanilla("statistics/block_mined"), Identifier.ofVanilla("statistics/item_broken"), Identifier.ofVanilla("statistics/item_crafted"), Identifier.ofVanilla("statistics/item_used"), Identifier.ofVanilla("statistics/item_picked_up"), Identifier.ofVanilla("statistics/item_dropped")};
      protected final List blockStatTypes = Lists.newArrayList();
      protected final List itemStatTypes;
      protected final Comparator comparator = new ItemComparator();
      @Nullable
      protected StatType selectedStatType;
      protected int selectedHeaderColumn = -1;
      protected int listOrder;

      public ItemStatsListWidget(final MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 22);
         this.blockStatTypes.add(Stats.MINED);
         this.itemStatTypes = Lists.newArrayList(new StatType[]{Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED});
         this.setRenderHeader(true, 22);
         Set<Item> set = Sets.newIdentityHashSet();

         for(Item item : Registries.ITEM) {
            boolean bl = false;

            for(StatType statType : this.itemStatTypes) {
               if (statType.hasStat(item) && StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(item)) > 0) {
                  bl = true;
               }
            }

            if (bl) {
               set.add(item);
            }
         }

         for(Block block : Registries.BLOCK) {
            boolean bl = false;

            for(StatType statType : this.blockStatTypes) {
               if (statType.hasStat(block) && StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(block)) > 0) {
                  bl = true;
               }
            }

            if (bl) {
               set.add(block.asItem());
            }
         }

         set.remove(Items.AIR);

         for(Item item : set) {
            this.addEntry(new Entry(item));
         }

      }

      int method_57742(int i) {
         return 75 + 40 * i;
      }

      protected void renderHeader(DrawContext context, int x, int y) {
         if (!this.client.mouse.wasLeftButtonClicked()) {
            this.selectedHeaderColumn = -1;
         }

         for(int i = 0; i < this.headerIconTextures.length; ++i) {
            Identifier identifier = this.selectedHeaderColumn == i ? StatsScreen.SLOT_TEXTURE : StatsScreen.HEADER_TEXTURE;
            context.drawGuiTexture(identifier, x + this.method_57742(i) - 18, y + 1, 0, 18, 18);
         }

         if (this.selectedStatType != null) {
            int i = this.method_57742(this.getHeaderIndex(this.selectedStatType)) - 36;
            Identifier identifier = this.listOrder == 1 ? StatsScreen.SORT_UP_TEXTURE : StatsScreen.SORT_DOWN_TEXTURE;
            context.drawGuiTexture(identifier, x + i, y + 1, 0, 18, 18);
         }

         for(int i = 0; i < this.headerIconTextures.length; ++i) {
            int j = this.selectedHeaderColumn == i ? 1 : 0;
            context.drawGuiTexture(this.headerIconTextures[i], x + this.method_57742(i) - 18 + j, y + 1 + j, 0, 18, 18);
         }

      }

      public int getRowWidth() {
         return 280;
      }

      protected boolean clickedHeader(int x, int y) {
         this.selectedHeaderColumn = -1;

         for(int i = 0; i < this.headerIconTextures.length; ++i) {
            int j = x - this.method_57742(i);
            if (j >= -36 && j <= 0) {
               this.selectedHeaderColumn = i;
               break;
            }
         }

         if (this.selectedHeaderColumn >= 0) {
            this.selectStatType(this.getStatType(this.selectedHeaderColumn));
            this.client.getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
         } else {
            return super.clickedHeader(x, y);
         }
      }

      private StatType getStatType(int headerColumn) {
         return headerColumn < this.blockStatTypes.size() ? (StatType)this.blockStatTypes.get(headerColumn) : (StatType)this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
      }

      private int getHeaderIndex(StatType statType) {
         int i = this.blockStatTypes.indexOf(statType);
         if (i >= 0) {
            return i;
         } else {
            int j = this.itemStatTypes.indexOf(statType);
            return j >= 0 ? j + this.blockStatTypes.size() : -1;
         }
      }

      protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
         if (mouseY >= this.getY() && mouseY <= this.getBottom()) {
            Entry entry = (Entry)this.getHoveredEntry();
            int i = this.getRowLeft();
            if (entry != null) {
               if (mouseX < i || mouseX > i + 18) {
                  return;
               }

               Item item = entry.getItem();
               context.drawTooltip(StatsScreen.this.textRenderer, item.getName(), mouseX, mouseY);
            } else {
               Text text = null;
               int j = mouseX - i;

               for(int k = 0; k < this.headerIconTextures.length; ++k) {
                  int l = this.method_57742(k);
                  if (j >= l - 18 && j <= l) {
                     text = this.getStatType(k).getName();
                     break;
                  }
               }

               if (text != null) {
                  context.drawTooltip(StatsScreen.this.textRenderer, text, mouseX, mouseY);
               }
            }

         }
      }

      protected void selectStatType(StatType statType) {
         if (statType != this.selectedStatType) {
            this.selectedStatType = statType;
            this.listOrder = -1;
         } else if (this.listOrder == -1) {
            this.listOrder = 1;
         } else {
            this.selectedStatType = null;
            this.listOrder = 0;
         }

         this.children().sort(this.comparator);
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Item item;

         Entry(final Item item) {
            this.item = item;
         }

         public Item getItem() {
            return this.item;
         }

         public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawGuiTexture(StatsScreen.SLOT_TEXTURE, x, y, 0, 18, 18);
            context.drawItemWithoutEntity(this.item.getDefaultStack(), x + 1, y + 1);
            if (StatsScreen.this.itemStats != null) {
               for(int i = 0; i < StatsScreen.this.itemStats.blockStatTypes.size(); ++i) {
                  Item var14 = this.item;
                  Stat<Block> stat;
                  if (var14 instanceof BlockItem) {
                     BlockItem blockItem = (BlockItem)var14;
                     stat = ((StatType)StatsScreen.this.itemStats.blockStatTypes.get(i)).getOrCreateStat(blockItem.getBlock());
                  } else {
                     stat = null;
                  }

                  int var10003 = x + ItemStatsListWidget.this.method_57742(i);
                  int var10004 = y + entryHeight / 2;
                  Objects.requireNonNull(StatsScreen.this.textRenderer);
                  this.render(context, stat, var10003, var10004 - 9 / 2, index % 2 == 0);
               }

               for(int i = 0; i < StatsScreen.this.itemStats.itemStatTypes.size(); ++i) {
                  Stat var10002 = ((StatType)StatsScreen.this.itemStats.itemStatTypes.get(i)).getOrCreateStat(this.item);
                  int var16 = x + ItemStatsListWidget.this.method_57742(i + StatsScreen.this.itemStats.blockStatTypes.size());
                  int var17 = y + entryHeight / 2;
                  Objects.requireNonNull(StatsScreen.this.textRenderer);
                  this.render(context, var10002, var16, var17 - 9 / 2, index % 2 == 0);
               }
            }

         }

         protected void render(DrawContext context, @Nullable Stat stat, int x, int y, boolean white) {
            Text text = (Text)(stat == null ? StatsScreen.NONE_TEXT : Text.literal(stat.format(StatsScreen.this.statHandler.getStat(stat))));
            context.drawTextWithShadow(StatsScreen.this.textRenderer, text, x - StatsScreen.this.textRenderer.getWidth((StringVisitable)text), y, white ? -1 : -4539718);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", new Object[]{this.item.getName()});
         }
      }

      @Environment(EnvType.CLIENT)
      class ItemComparator implements Comparator {
         public int compare(Entry entry, Entry entry2) {
            Item item = entry.getItem();
            Item item2 = entry2.getItem();
            int i;
            int j;
            if (ItemStatsListWidget.this.selectedStatType == null) {
               i = 0;
               j = 0;
            } else if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
               StatType<Block> statType = ItemStatsListWidget.this.selectedStatType;
               i = item instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item).getBlock()) : -1;
               j = item2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item2).getBlock()) : -1;
            } else {
               StatType<Item> statType = ItemStatsListWidget.this.selectedStatType;
               i = StatsScreen.this.statHandler.getStat(statType, item);
               j = StatsScreen.this.statHandler.getStat(statType, item2);
            }

            return i == j ? ItemStatsListWidget.this.listOrder * Integer.compare(Item.getRawId(item), Item.getRawId(item2)) : ItemStatsListWidget.this.listOrder * Integer.compare(i, j);
         }

         // $FF: synthetic method
         public int compare(final Object a, final Object b) {
            return this.compare((Entry)a, (Entry)b);
         }
      }
   }
}
