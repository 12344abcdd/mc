package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;

@Environment(EnvType.CLIENT)
public class RecipeResultCollection {
   private final DynamicRegistryManager registryManager;
   private final List recipes;
   private final boolean singleOutput;
   private final Set craftableRecipes = Sets.newHashSet();
   private final Set fittingRecipes = Sets.newHashSet();
   private final Set unlockedRecipes = Sets.newHashSet();

   public RecipeResultCollection(DynamicRegistryManager registryManager, List recipes) {
      this.registryManager = registryManager;
      this.recipes = ImmutableList.copyOf(recipes);
      if (recipes.size() <= 1) {
         this.singleOutput = true;
      } else {
         this.singleOutput = shouldHaveSingleOutput(registryManager, recipes);
      }

   }

   private static boolean shouldHaveSingleOutput(DynamicRegistryManager registryManager, List recipes) {
      int i = recipes.size();
      ItemStack itemStack = ((RecipeEntry)recipes.get(0)).value().getResult(registryManager);

      for(int j = 1; j < i; ++j) {
         ItemStack itemStack2 = ((RecipeEntry)recipes.get(j)).value().getResult(registryManager);
         if (!ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) {
            return false;
         }
      }

      return true;
   }

   public DynamicRegistryManager getRegistryManager() {
      return this.registryManager;
   }

   public boolean isInitialized() {
      return !this.unlockedRecipes.isEmpty();
   }

   public void initialize(RecipeBook recipeBook) {
      for(RecipeEntry recipeEntry : this.recipes) {
         if (recipeBook.contains(recipeEntry)) {
            this.unlockedRecipes.add(recipeEntry);
         }
      }

   }

   public void computeCraftables(RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook) {
      for(RecipeEntry recipeEntry : this.recipes) {
         boolean bl = recipeEntry.value().fits(gridWidth, gridHeight) && recipeBook.contains(recipeEntry);
         if (bl) {
            this.fittingRecipes.add(recipeEntry);
         } else {
            this.fittingRecipes.remove(recipeEntry);
         }

         if (bl && recipeFinder.match(recipeEntry.value(), (IntList)null)) {
            this.craftableRecipes.add(recipeEntry);
         } else {
            this.craftableRecipes.remove(recipeEntry);
         }
      }

   }

   public boolean isCraftable(RecipeEntry recipe) {
      return this.craftableRecipes.contains(recipe);
   }

   public boolean hasCraftableRecipes() {
      return !this.craftableRecipes.isEmpty();
   }

   public boolean hasFittingRecipes() {
      return !this.fittingRecipes.isEmpty();
   }

   public List getAllRecipes() {
      return this.recipes;
   }

   public List getResults(boolean craftableOnly) {
      List<RecipeEntry<?>> list = Lists.newArrayList();
      Set<RecipeEntry<?>> set = craftableOnly ? this.craftableRecipes : this.fittingRecipes;

      for(RecipeEntry recipeEntry : this.recipes) {
         if (set.contains(recipeEntry)) {
            list.add(recipeEntry);
         }
      }

      return list;
   }

   public List getRecipes(boolean craftable) {
      List<RecipeEntry<?>> list = Lists.newArrayList();

      for(RecipeEntry recipeEntry : this.recipes) {
         if (this.fittingRecipes.contains(recipeEntry) && this.craftableRecipes.contains(recipeEntry) == craftable) {
            list.add(recipeEntry);
         }
      }

      return list;
   }

   public boolean hasSingleOutput() {
      return this.singleOutput;
   }
}
