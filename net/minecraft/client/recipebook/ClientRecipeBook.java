package net.minecraft.client.recipebook;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map resultsByGroup = ImmutableMap.of();
   private List orderedResults = ImmutableList.of();

   public void reload(Iterable recipes, DynamicRegistryManager registryManager) {
      Map<RecipeBookGroup, List<List<RecipeEntry<?>>>> map = toGroupedMap(recipes);
      Map<RecipeBookGroup, List<RecipeResultCollection>> map2 = Maps.newHashMap();
      ImmutableList.Builder<RecipeResultCollection> builder = ImmutableList.builder();
      map.forEach((recipeBookGroup, list) -> {
         Stream var10002 = list.stream().map((recipes) -> new RecipeResultCollection(registryManager, recipes));
         Objects.requireNonNull(builder);
         map2.put(recipeBookGroup, (List)var10002.peek(builder::add).collect(ImmutableList.toImmutableList()));
      });
      RecipeBookGroup.SEARCH_MAP.forEach((group, searchGroups) -> map2.put(group, (List)searchGroups.stream().flatMap((searchGroup) -> ((List)map2.getOrDefault(searchGroup, ImmutableList.of())).stream()).collect(ImmutableList.toImmutableList())));
      this.resultsByGroup = ImmutableMap.copyOf(map2);
      this.orderedResults = builder.build();
   }

   private static Map toGroupedMap(Iterable recipes) {
      Map<RecipeBookGroup, List<List<RecipeEntry<?>>>> map = Maps.newHashMap();
      Table<RecipeBookGroup, String, List<RecipeEntry<?>>> table = HashBasedTable.create();

      for(RecipeEntry recipeEntry : recipes) {
         Recipe<?> recipe = recipeEntry.value();
         if (!recipe.isIgnoredInRecipeBook() && !recipe.isEmpty()) {
            RecipeBookGroup recipeBookGroup = getGroupForRecipe(recipeEntry);
            String string = recipe.getGroup();
            if (string.isEmpty()) {
               ((List)map.computeIfAbsent(recipeBookGroup, (group) -> Lists.newArrayList())).add(ImmutableList.of(recipeEntry));
            } else {
               List<RecipeEntry<?>> list = (List)table.get(recipeBookGroup, string);
               if (list == null) {
                  list = Lists.newArrayList();
                  table.put(recipeBookGroup, string, list);
                  ((List)map.computeIfAbsent(recipeBookGroup, (group) -> Lists.newArrayList())).add(list);
               }

               list.add(recipeEntry);
            }
         }
      }

      return map;
   }

   private static RecipeBookGroup getGroupForRecipe(RecipeEntry recipe) {
      Recipe<?> recipe2 = recipe.value();
      if (recipe2 instanceof CraftingRecipe) {
         CraftingRecipe craftingRecipe = (CraftingRecipe)recipe2;
         RecipeBookGroup var7;
         switch (craftingRecipe.getCategory()) {
            case BUILDING -> var7 = RecipeBookGroup.CRAFTING_BUILDING_BLOCKS;
            case EQUIPMENT -> var7 = RecipeBookGroup.CRAFTING_EQUIPMENT;
            case REDSTONE -> var7 = RecipeBookGroup.CRAFTING_REDSTONE;
            case MISC -> var7 = RecipeBookGroup.CRAFTING_MISC;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var7;
      } else {
         RecipeType<?> recipeType = recipe2.getType();
         if (recipe2 instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe abstractCookingRecipe = (AbstractCookingRecipe)recipe2;
            CookingRecipeCategory cookingRecipeCategory = abstractCookingRecipe.getCategory();
            if (recipeType == RecipeType.SMELTING) {
               RecipeBookGroup var6;
               switch (cookingRecipeCategory) {
                  case BLOCKS -> var6 = RecipeBookGroup.FURNACE_BLOCKS;
                  case FOOD -> var6 = RecipeBookGroup.FURNACE_FOOD;
                  case MISC -> var6 = RecipeBookGroup.FURNACE_MISC;
                  default -> throw new MatchException((String)null, (Throwable)null);
               }

               return var6;
            }

            if (recipeType == RecipeType.BLASTING) {
               return cookingRecipeCategory == CookingRecipeCategory.BLOCKS ? RecipeBookGroup.BLAST_FURNACE_BLOCKS : RecipeBookGroup.BLAST_FURNACE_MISC;
            }

            if (recipeType == RecipeType.SMOKING) {
               return RecipeBookGroup.SMOKER_FOOD;
            }

            if (recipeType == RecipeType.CAMPFIRE_COOKING) {
               return RecipeBookGroup.CAMPFIRE;
            }
         }

         if (recipeType == RecipeType.STONECUTTING) {
            return RecipeBookGroup.STONECUTTER;
         } else if (recipeType == RecipeType.SMITHING) {
            return RecipeBookGroup.SMITHING;
         } else {
            Logger var10000 = LOGGER;
            Object var10002 = LogUtils.defer(() -> Registries.RECIPE_TYPE.getId(recipe2.getType()));
            Objects.requireNonNull(recipe);
            var10000.warn("Unknown recipe category: {}/{}", var10002, LogUtils.defer(recipe::id));
            return RecipeBookGroup.UNKNOWN;
         }
      }
   }

   public List getOrderedResults() {
      return this.orderedResults;
   }

   public List getResultsForGroup(RecipeBookGroup category) {
      return (List)this.resultsByGroup.getOrDefault(category, Collections.emptyList());
   }
}
