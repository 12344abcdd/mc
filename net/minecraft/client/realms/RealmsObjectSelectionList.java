package net.minecraft.client.realms;

import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(EnvType.CLIENT)
public abstract class RealmsObjectSelectionList extends AlwaysSelectedEntryListWidget {
   protected RealmsObjectSelectionList(int width, int height, int top, int bottom) {
      super(MinecraftClient.getInstance(), width, height, top, bottom);
   }

   public void setSelectedItem(int index) {
      if (index == -1) {
         this.setSelected((EntryListWidget.Entry)null);
      } else if (super.getEntryCount() != 0) {
         this.setSelected((AlwaysSelectedEntryListWidget.Entry)this.getEntry(index));
      }

   }

   public void setSelected(int index) {
      this.setSelectedItem(index);
   }

   public int getMaxPosition() {
      return 0;
   }

   public int getRowWidth() {
      return (int)((double)this.width * 0.6);
   }

   public void replaceEntries(Collection newEntries) {
      super.replaceEntries(newEntries);
   }

   public int getEntryCount() {
      return super.getEntryCount();
   }

   public int getRowTop(int index) {
      return super.getRowTop(index);
   }

   public int getRowLeft() {
      return super.getRowLeft();
   }

   public int addEntry(AlwaysSelectedEntryListWidget.Entry entry) {
      return super.addEntry(entry);
   }

   public void clear() {
      this.clearEntries();
   }

   // $FF: synthetic method
   public int addEntry(final EntryListWidget.Entry entry) {
      return this.addEntry((AlwaysSelectedEntryListWidget.Entry)entry);
   }
}
