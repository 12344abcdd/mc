package net.minecraft.client.toast;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RecipeToast implements Toast {
   private static final Identifier TEXTURE = Identifier.ofVanilla("toast/recipe");
   private static final long DEFAULT_DURATION_MS = 5000L;
   private static final Text TITLE = Text.translatable("recipe.toast.title");
   private static final Text DESCRIPTION = Text.translatable("recipe.toast.description");
   private final List recipes = Lists.newArrayList();
   private long startTime;
   private boolean justUpdated;

   public RecipeToast(RecipeEntry recipe) {
      this.recipes.add(recipe);
   }

   public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
      if (this.justUpdated) {
         this.startTime = startTime;
         this.justUpdated = false;
      }

      if (this.recipes.isEmpty()) {
         return Toast.Visibility.HIDE;
      } else {
         context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
         context.drawText(manager.getClient().textRenderer, (Text)TITLE, 30, 7, -11534256, false);
         context.drawText(manager.getClient().textRenderer, (Text)DESCRIPTION, 30, 18, -16777216, false);
         RecipeEntry<?> recipeEntry = (RecipeEntry)this.recipes.get((int)((double)startTime / Math.max((double)1.0F, (double)5000.0F * manager.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size()) % (double)this.recipes.size()));
         ItemStack itemStack = recipeEntry.value().createIcon();
         context.getMatrices().push();
         context.getMatrices().scale(0.6F, 0.6F, 1.0F);
         context.drawItemWithoutEntity(itemStack, 3, 3);
         context.getMatrices().pop();
         context.drawItemWithoutEntity(recipeEntry.value().getResult(manager.getClient().world.getRegistryManager()), 8, 8);
         return (double)(startTime - this.startTime) >= (double)5000.0F * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      }
   }

   private void addRecipes(RecipeEntry recipe) {
      this.recipes.add(recipe);
      this.justUpdated = true;
   }

   public static void show(ToastManager manager, RecipeEntry recipe) {
      RecipeToast recipeToast = (RecipeToast)manager.getToast(RecipeToast.class, TYPE);
      if (recipeToast == null) {
         manager.add(new RecipeToast(recipe));
      } else {
         recipeToast.addRecipes(recipe);
      }

   }
}
