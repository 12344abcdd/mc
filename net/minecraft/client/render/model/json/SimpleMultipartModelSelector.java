package net.minecraft.client.render.model.json;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

@Environment(EnvType.CLIENT)
public class SimpleMultipartModelSelector implements MultipartModelSelector {
   private static final Splitter VALUE_SPLITTER = Splitter.on('|').omitEmptyStrings();
   private final String key;
   private final String valueString;

   public SimpleMultipartModelSelector(String key, String valueString) {
      this.key = key;
      this.valueString = valueString;
   }

   public Predicate getPredicate(StateManager stateManager) {
      Property<?> property = stateManager.getProperty(this.key);
      if (property == null) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", this.key, stateManager.getOwner()));
      } else {
         String string = this.valueString;
         boolean bl = !string.isEmpty() && string.charAt(0) == '!';
         if (bl) {
            string = string.substring(1);
         }

         List<String> list = VALUE_SPLITTER.splitToList(string);
         if (list.isEmpty()) {
            throw new RuntimeException(String.format(Locale.ROOT, "Empty value '%s' for property '%s' on '%s'", this.valueString, this.key, stateManager.getOwner()));
         } else {
            Predicate<BlockState> predicate;
            if (list.size() == 1) {
               predicate = this.createPredicate(stateManager, property, string);
            } else {
               List<Predicate<BlockState>> list2 = (List)list.stream().map((value) -> this.createPredicate(stateManager, property, value)).collect(Collectors.toList());
               predicate = (state) -> list2.stream().anyMatch((predicate) -> predicate.test(state));
            }

            return bl ? predicate.negate() : predicate;
         }
      }
   }

   private Predicate createPredicate(StateManager stateFactory, Property property, String valueString) {
      Optional<?> optional = property.parse(valueString);
      if (optional.isEmpty()) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", valueString, this.key, stateFactory.getOwner(), this.valueString));
      } else {
         return (state) -> state.get(property).equals(optional.get());
      }
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.valueString).toString();
   }
}
