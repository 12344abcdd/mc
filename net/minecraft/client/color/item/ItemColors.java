package net.minecraft.client.color.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.FoliageColors;
import net.minecraft.world.biome.GrassColors;

@Environment(EnvType.CLIENT)
public class ItemColors {
   private static final int NO_COLOR = -1;
   private final IdList providers = new IdList(32);

   public static ItemColors create(BlockColors blockColors) {
      ItemColors itemColors = new ItemColors();
      itemColors.register((stack, tintIndex) -> tintIndex > 0 ? -1 : DyedColorComponent.getColor(stack, -6265536), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
      itemColors.register((stack, tintIndex) -> tintIndex != 1 ? -1 : DyedColorComponent.getColor(stack, 0), Items.WOLF_ARMOR);
      itemColors.register((stack, tintIndex) -> GrassColors.getColor((double)0.5F, (double)1.0F), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
      itemColors.register((stack, tintIndex) -> {
         if (tintIndex != 1) {
            return -1;
         } else {
            FireworkExplosionComponent fireworkExplosionComponent = (FireworkExplosionComponent)stack.get(DataComponentTypes.FIREWORK_EXPLOSION);
            IntList intList = fireworkExplosionComponent != null ? fireworkExplosionComponent.colors() : IntList.of();
            int i = intList.size();
            if (i == 0) {
               return -7697782;
            } else if (i == 1) {
               return Argb.fullAlpha(intList.getInt(0));
            } else {
               int j = 0;
               int k = 0;
               int l = 0;

               for(int m = 0; m < i; ++m) {
                  int n = intList.getInt(m);
                  j += Argb.getRed(n);
                  k += Argb.getGreen(n);
                  l += Argb.getBlue(n);
               }

               return Argb.getArgb(j / i, k / i, l / i);
            }
         }
      }, Items.FIREWORK_STAR);
      itemColors.register((stack, tintIndex) -> tintIndex > 0 ? -1 : Argb.fullAlpha(((PotionContentsComponent)stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)).getColor()), Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW);

      for(SpawnEggItem spawnEggItem : SpawnEggItem.getAll()) {
         itemColors.register((stack, tintIndex) -> Argb.fullAlpha(spawnEggItem.getColor(tintIndex)), spawnEggItem);
      }

      itemColors.register((stack, tintIndex) -> {
         BlockState blockState = ((BlockItem)stack.getItem()).getBlock().getDefaultState();
         return blockColors.getColor(blockState, (BlockRenderView)null, (BlockPos)null, tintIndex);
      }, Blocks.GRASS_BLOCK, Blocks.SHORT_GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
      itemColors.register((stack, tintIndex) -> FoliageColors.getMangroveColor(), Blocks.MANGROVE_LEAVES);
      itemColors.register((stack, tintIndex) -> tintIndex == 0 ? -1 : Argb.fullAlpha(((MapColorComponent)stack.getOrDefault(DataComponentTypes.MAP_COLOR, MapColorComponent.DEFAULT)).rgb()), Items.FILLED_MAP);
      return itemColors;
   }

   public int getColor(ItemStack item, int tintIndex) {
      ItemColorProvider itemColorProvider = (ItemColorProvider)this.providers.get(Registries.ITEM.getRawId(item.getItem()));
      return itemColorProvider == null ? -1 : itemColorProvider.getColor(item, tintIndex);
   }

   public void register(ItemColorProvider provider, ItemConvertible... items) {
      for(ItemConvertible itemConvertible : items) {
         this.providers.set(provider, Item.getRawId(itemConvertible.asItem()));
      }

   }
}
