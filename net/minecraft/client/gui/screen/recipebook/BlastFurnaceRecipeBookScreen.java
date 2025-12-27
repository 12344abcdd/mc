package net.minecraft.client.gui.screen.recipebook;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class BlastFurnaceRecipeBookScreen extends AbstractFurnaceRecipeBookScreen {
   private static final Text TOGGLE_BLASTABLE_RECIPES_TEXT = Text.translatable("gui.recipebook.toggleRecipes.blastable");

   protected Text getToggleCraftableButtonText() {
      return TOGGLE_BLASTABLE_RECIPES_TEXT;
   }

   protected Set getAllowedFuels() {
      return AbstractFurnaceBlockEntity.createFuelTimeMap().keySet();
   }
}
