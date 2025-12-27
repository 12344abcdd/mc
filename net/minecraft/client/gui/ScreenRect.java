package net.minecraft.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.NavigationAxis;
import net.minecraft.client.gui.navigation.NavigationDirection;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ScreenRect(ScreenPos position, int width, int height) {
   private static final ScreenRect EMPTY = new ScreenRect(0, 0, 0, 0);

   public ScreenRect(int sameAxis, int otherAxis, int width, int height) {
      this(new ScreenPos(sameAxis, otherAxis), width, height);
   }

   public static ScreenRect empty() {
      return EMPTY;
   }

   public static ScreenRect of(NavigationAxis axis, int sameAxisCoord, int otherAxisCoord, int sameAxisLength, int otherAxisLength) {
      ScreenRect var10000;
      switch (axis) {
         case HORIZONTAL -> var10000 = new ScreenRect(sameAxisCoord, otherAxisCoord, sameAxisLength, otherAxisLength);
         case VERTICAL -> var10000 = new ScreenRect(otherAxisCoord, sameAxisCoord, otherAxisLength, sameAxisLength);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public ScreenRect add(NavigationDirection direction) {
      return new ScreenRect(this.position.add(direction), this.width, this.height);
   }

   public int getLength(NavigationAxis axis) {
      int var10000;
      switch (axis) {
         case HORIZONTAL -> var10000 = this.width;
         case VERTICAL -> var10000 = this.height;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public int getBoundingCoordinate(NavigationDirection direction) {
      NavigationAxis navigationAxis = direction.getAxis();
      return direction.isPositive() ? this.position.getComponent(navigationAxis) + this.getLength(navigationAxis) - 1 : this.position.getComponent(navigationAxis);
   }

   public ScreenRect getBorder(NavigationDirection direction) {
      int i = this.getBoundingCoordinate(direction);
      NavigationAxis navigationAxis = direction.getAxis().getOther();
      int j = this.getBoundingCoordinate(navigationAxis.getNegativeDirection());
      int k = this.getLength(navigationAxis);
      return of(direction.getAxis(), i, j, 1, k).add(direction);
   }

   public boolean overlaps(ScreenRect other) {
      return this.overlaps(other, NavigationAxis.HORIZONTAL) && this.overlaps(other, NavigationAxis.VERTICAL);
   }

   public boolean overlaps(ScreenRect other, NavigationAxis axis) {
      int i = this.getBoundingCoordinate(axis.getNegativeDirection());
      int j = other.getBoundingCoordinate(axis.getNegativeDirection());
      int k = this.getBoundingCoordinate(axis.getPositiveDirection());
      int l = other.getBoundingCoordinate(axis.getPositiveDirection());
      return Math.max(i, j) <= Math.min(k, l);
   }

   public int getCenter(NavigationAxis axis) {
      return (this.getBoundingCoordinate(axis.getPositiveDirection()) + this.getBoundingCoordinate(axis.getNegativeDirection())) / 2;
   }

   @Nullable
   public ScreenRect intersection(ScreenRect other) {
      int i = Math.max(this.getLeft(), other.getLeft());
      int j = Math.max(this.getTop(), other.getTop());
      int k = Math.min(this.getRight(), other.getRight());
      int l = Math.min(this.getBottom(), other.getBottom());
      return i < k && j < l ? new ScreenRect(i, j, k - i, l - j) : null;
   }

   public int getTop() {
      return this.position.y();
   }

   public int getBottom() {
      return this.position.y() + this.height;
   }

   public int getLeft() {
      return this.position.x();
   }

   public int getRight() {
      return this.position.x() + this.width;
   }

   public boolean contains(int x, int y) {
      return x >= this.getLeft() && x < this.getRight() && y >= this.getTop() && y < this.getBottom();
   }
}
