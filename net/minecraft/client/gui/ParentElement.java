package net.minecraft.client.gui;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public interface ParentElement extends Element {
   List children();

   default Optional hoveredElement(double mouseX, double mouseY) {
      for(Element element : this.children()) {
         if (element.isMouseOver(mouseX, mouseY)) {
            return Optional.of(element);
         }
      }

      return Optional.empty();
   }

   default boolean mouseClicked(double mouseX, double mouseY, int button) {
      for(Element element : this.children()) {
         if (element.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(element);
            if (button == 0) {
               this.setDragging(true);
            }

            return true;
         }
      }

      return false;
   }

   default boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isDragging()) {
         this.setDragging(false);
         if (this.getFocused() != null) {
            return this.getFocused().mouseReleased(mouseX, mouseY, button);
         }
      }

      return this.hoveredElement(mouseX, mouseY).filter((element) -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
   }

   default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
   }

   boolean isDragging();

   void setDragging(boolean dragging);

   default boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.hoveredElement(mouseX, mouseY).filter((element) -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
   }

   default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
   }

   default boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
   }

   default boolean charTyped(char chr, int modifiers) {
      return this.getFocused() != null && this.getFocused().charTyped(chr, modifiers);
   }

   @Nullable
   Element getFocused();

   void setFocused(@Nullable Element focused);

   default void setFocused(boolean focused) {
   }

   default boolean isFocused() {
      return this.getFocused() != null;
   }

   @Nullable
   default GuiNavigationPath getFocusedPath() {
      Element element = this.getFocused();
      return element != null ? GuiNavigationPath.of(this, element.getFocusedPath()) : null;
   }

   @Nullable
   default GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
      Element element = this.getFocused();
      if (element != null) {
         GuiNavigationPath guiNavigationPath = element.getNavigationPath(navigation);
         if (guiNavigationPath != null) {
            return GuiNavigationPath.of(this, guiNavigationPath);
         }
      }

      if (navigation instanceof GuiNavigation.Tab tab) {
         return this.computeNavigationPath(tab);
      } else if (navigation instanceof GuiNavigation.Arrow arrow) {
         return this.computeNavigationPath(arrow);
      } else {
         return null;
      }
   }

   @Nullable
   private GuiNavigationPath computeNavigationPath(GuiNavigation.Tab navigation) {
      boolean bl = navigation.forward();
      Element element = this.getFocused();
      List<? extends Element> list = new ArrayList(this.children());
      Collections.sort(list, Comparator.comparingInt((elementx) -> elementx.getNavigationOrder()));
      int i = list.indexOf(element);
      int j;
      if (element != null && i >= 0) {
         j = i + (bl ? 1 : 0);
      } else if (bl) {
         j = 0;
      } else {
         j = list.size();
      }

      ListIterator<? extends Element> listIterator = list.listIterator(j);
      BooleanSupplier var10000;
      if (bl) {
         Objects.requireNonNull(listIterator);
         var10000 = listIterator::hasNext;
      } else {
         Objects.requireNonNull(listIterator);
         var10000 = listIterator::hasPrevious;
      }

      BooleanSupplier booleanSupplier = var10000;
      Supplier var12;
      if (bl) {
         Objects.requireNonNull(listIterator);
         var12 = listIterator::next;
      } else {
         Objects.requireNonNull(listIterator);
         var12 = listIterator::previous;
      }

      Supplier<? extends Element> supplier = var12;

      while(booleanSupplier.getAsBoolean()) {
         Element element2 = (Element)supplier.get();
         GuiNavigationPath guiNavigationPath = element2.getNavigationPath(navigation);
         if (guiNavigationPath != null) {
            return GuiNavigationPath.of(this, guiNavigationPath);
         }
      }

      return null;
   }

   @Nullable
   private GuiNavigationPath computeNavigationPath(GuiNavigation.Arrow navigation) {
      Element element = this.getFocused();
      if (element == null) {
         NavigationDirection navigationDirection = navigation.direction();
         ScreenRect screenRect = this.getNavigationFocus().getBorder(navigationDirection.getOpposite());
         return GuiNavigationPath.of(this, this.computeChildPath(screenRect, navigationDirection, (Element)null, navigation));
      } else {
         ScreenRect screenRect2 = element.getNavigationFocus();
         return GuiNavigationPath.of(this, this.computeChildPath(screenRect2, navigation.direction(), element, navigation));
      }
   }

   @Nullable
   private GuiNavigationPath computeChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
      NavigationAxis navigationAxis = direction.getAxis();
      NavigationAxis navigationAxis2 = navigationAxis.getOther();
      NavigationDirection navigationDirection = navigationAxis2.getPositiveDirection();
      int i = focus.getBoundingCoordinate(direction.getOpposite());
      List<Element> list = new ArrayList();

      for(Element element : this.children()) {
         if (element != focused) {
            ScreenRect screenRect = element.getNavigationFocus();
            if (screenRect.overlaps(focus, navigationAxis2)) {
               int j = screenRect.getBoundingCoordinate(direction.getOpposite());
               if (direction.isAfter(j, i)) {
                  list.add(element);
               } else if (j == i && direction.isAfter(screenRect.getBoundingCoordinate(direction), focus.getBoundingCoordinate(direction))) {
                  list.add(element);
               }
            }
         }
      }

      Comparator<Element> comparator = Comparator.comparing((elementx) -> elementx.getNavigationFocus().getBoundingCoordinate(direction.getOpposite()), direction.getComparator());
      Comparator<Element> comparator2 = Comparator.comparing((elementx) -> elementx.getNavigationFocus().getBoundingCoordinate(navigationDirection.getOpposite()), navigationDirection.getComparator());
      list.sort(comparator.thenComparing(comparator2));

      for(Element element2 : list) {
         GuiNavigationPath guiNavigationPath = element2.getNavigationPath(navigation);
         if (guiNavigationPath != null) {
            return guiNavigationPath;
         }
      }

      return this.computeInitialChildPath(focus, direction, focused, navigation);
   }

   @Nullable
   private GuiNavigationPath computeInitialChildPath(ScreenRect focus, NavigationDirection direction, @Nullable Element focused, GuiNavigation navigation) {
      NavigationAxis navigationAxis = direction.getAxis();
      NavigationAxis navigationAxis2 = navigationAxis.getOther();
      List<Pair<Element, Long>> list = new ArrayList();
      ScreenPos screenPos = ScreenPos.of(navigationAxis, focus.getBoundingCoordinate(direction), focus.getCenter(navigationAxis2));

      for(Element element : this.children()) {
         if (element != focused) {
            ScreenRect screenRect = element.getNavigationFocus();
            ScreenPos screenPos2 = ScreenPos.of(navigationAxis, screenRect.getBoundingCoordinate(direction.getOpposite()), screenRect.getCenter(navigationAxis2));
            if (direction.isAfter(screenPos2.getComponent(navigationAxis), screenPos.getComponent(navigationAxis))) {
               long l = Vector2i.distanceSquared(screenPos.x(), screenPos.y(), screenPos2.x(), screenPos2.y());
               list.add(Pair.of(element, l));
            }
         }
      }

      list.sort(Comparator.comparingDouble(Pair::getSecond));

      for(Pair pair : list) {
         GuiNavigationPath guiNavigationPath = ((Element)pair.getFirst()).getNavigationPath(navigation);
         if (guiNavigationPath != null) {
            return guiNavigationPath;
         }
      }

      return null;
   }
}
