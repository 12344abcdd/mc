package net.minecraft.client.realms.dto;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Util;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsServer extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_PARENT = -1;
   public long id;
   public String remoteSubscriptionId;
   public String name;
   public String description;
   public State state;
   public String owner;
   public UUID ownerUUID;
   public List players;
   public Map slots;
   public boolean expired;
   public boolean expiredTrial;
   public int daysLeft;
   public WorldType worldType;
   public int activeSlot;
   @Nullable
   public String minigameName;
   public int minigameId;
   public String minigameImage;
   public long parentWorldId;
   @Nullable
   public String parentWorldName;
   public String activeVersion;
   public Compatibility compatibility;

   public RealmsServer() {
      this.ownerUUID = Util.NIL_UUID;
      this.parentWorldId = -1L;
      this.activeVersion = "";
      this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
   }

   public String getDescription() {
      return this.description;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getMinigameName() {
      return this.minigameName;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public static RealmsServer parse(JsonObject node) {
      RealmsServer realmsServer = new RealmsServer();

      try {
         realmsServer.id = JsonUtils.getLongOr("id", node, -1L);
         realmsServer.remoteSubscriptionId = JsonUtils.getNullableStringOr("remoteSubscriptionId", node, (String)null);
         realmsServer.name = JsonUtils.getNullableStringOr("name", node, (String)null);
         realmsServer.description = JsonUtils.getNullableStringOr("motd", node, (String)null);
         realmsServer.state = getState(JsonUtils.getNullableStringOr("state", node, RealmsServer.State.CLOSED.name()));
         realmsServer.owner = JsonUtils.getNullableStringOr("owner", node, (String)null);
         if (node.get("players") != null && node.get("players").isJsonArray()) {
            realmsServer.players = parseInvited(node.get("players").getAsJsonArray());
            sortInvited(realmsServer);
         } else {
            realmsServer.players = Lists.newArrayList();
         }

         realmsServer.daysLeft = JsonUtils.getIntOr("daysLeft", node, 0);
         realmsServer.expired = JsonUtils.getBooleanOr("expired", node, false);
         realmsServer.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", node, false);
         realmsServer.worldType = getWorldType(JsonUtils.getNullableStringOr("worldType", node, RealmsServer.WorldType.NORMAL.name()));
         realmsServer.ownerUUID = JsonUtils.getUuidOr("ownerUUID", node, Util.NIL_UUID);
         if (node.get("slots") != null && node.get("slots").isJsonArray()) {
            realmsServer.slots = parseSlots(node.get("slots").getAsJsonArray());
         } else {
            realmsServer.slots = getEmptySlots();
         }

         realmsServer.minigameName = JsonUtils.getNullableStringOr("minigameName", node, (String)null);
         realmsServer.activeSlot = JsonUtils.getIntOr("activeSlot", node, -1);
         realmsServer.minigameId = JsonUtils.getIntOr("minigameId", node, -1);
         realmsServer.minigameImage = JsonUtils.getNullableStringOr("minigameImage", node, (String)null);
         realmsServer.parentWorldId = JsonUtils.getLongOr("parentWorldId", node, -1L);
         realmsServer.parentWorldName = JsonUtils.getNullableStringOr("parentWorldName", node, (String)null);
         realmsServer.activeVersion = JsonUtils.getNullableStringOr("activeVersion", node, "");
         realmsServer.compatibility = getCompatibility(JsonUtils.getNullableStringOr("compatibility", node, RealmsServer.Compatibility.UNVERIFIABLE.name()));
      } catch (Exception exception) {
         LOGGER.error("Could not parse McoServer: {}", exception.getMessage());
      }

      return realmsServer;
   }

   private static void sortInvited(RealmsServer server) {
      server.players.sort((a, b) -> ComparisonChain.start().compareFalseFirst(b.isAccepted(), a.isAccepted()).compare(a.getName().toLowerCase(Locale.ROOT), b.getName().toLowerCase(Locale.ROOT)).result());
   }

   private static List parseInvited(JsonArray jsonArray) {
      List<PlayerInfo> list = Lists.newArrayList();

      for(JsonElement jsonElement : jsonArray) {
         try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setName(JsonUtils.getNullableStringOr("name", jsonObject, (String)null));
            playerInfo.setUuid(JsonUtils.getUuidOr("uuid", jsonObject, Util.NIL_UUID));
            playerInfo.setOperator(JsonUtils.getBooleanOr("operator", jsonObject, false));
            playerInfo.setAccepted(JsonUtils.getBooleanOr("accepted", jsonObject, false));
            playerInfo.setOnline(JsonUtils.getBooleanOr("online", jsonObject, false));
            list.add(playerInfo);
         } catch (Exception var6) {
         }
      }

      return list;
   }

   private static Map parseSlots(JsonArray json) {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();

      for(JsonElement jsonElement : json) {
         try {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement2 = jsonParser.parse(jsonObject.get("options").getAsString());
            RealmsWorldOptions realmsWorldOptions;
            if (jsonElement2 == null) {
               realmsWorldOptions = RealmsWorldOptions.getDefaults();
            } else {
               realmsWorldOptions = RealmsWorldOptions.parse(jsonElement2.getAsJsonObject());
            }

            int i = JsonUtils.getIntOr("slotId", jsonObject, -1);
            map.put(i, realmsWorldOptions);
         } catch (Exception var9) {
         }
      }

      for(int j = 1; j <= 3; ++j) {
         if (!map.containsKey(j)) {
            map.put(j, RealmsWorldOptions.getEmptyDefaults());
         }
      }

      return map;
   }

   private static Map getEmptySlots() {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();
      map.put(1, RealmsWorldOptions.getEmptyDefaults());
      map.put(2, RealmsWorldOptions.getEmptyDefaults());
      map.put(3, RealmsWorldOptions.getEmptyDefaults());
      return map;
   }

   public static RealmsServer parse(String json) {
      try {
         return parse((new JsonParser()).parse(json).getAsJsonObject());
      } catch (Exception exception) {
         LOGGER.error("Could not parse McoServer: {}", exception.getMessage());
         return new RealmsServer();
      }
   }

   private static State getState(String state) {
      try {
         return RealmsServer.State.valueOf(state);
      } catch (Exception var2) {
         return RealmsServer.State.CLOSED;
      }
   }

   private static WorldType getWorldType(String worldType) {
      try {
         return RealmsServer.WorldType.valueOf(worldType);
      } catch (Exception var2) {
         return RealmsServer.WorldType.NORMAL;
      }
   }

   public static Compatibility getCompatibility(@Nullable String compatibility) {
      try {
         return RealmsServer.Compatibility.valueOf(compatibility);
      } catch (Exception var2) {
         return RealmsServer.Compatibility.UNVERIFIABLE;
      }
   }

   public boolean isCompatible() {
      return this.compatibility.isCompatible();
   }

   public boolean needsUpgrade() {
      return this.compatibility.needsUpgrade();
   }

   public boolean needsDowngrade() {
      return this.compatibility.needsDowngrade();
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.name, this.description, this.state, this.owner, this.expired});
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         RealmsServer realmsServer = (RealmsServer)o;
         return (new EqualsBuilder()).append(this.id, realmsServer.id).append(this.name, realmsServer.name).append(this.description, realmsServer.description).append(this.state, realmsServer.state).append(this.owner, realmsServer.owner).append(this.expired, realmsServer.expired).append(this.worldType, this.worldType).isEquals();
      }
   }

   public RealmsServer clone() {
      RealmsServer realmsServer = new RealmsServer();
      realmsServer.id = this.id;
      realmsServer.remoteSubscriptionId = this.remoteSubscriptionId;
      realmsServer.name = this.name;
      realmsServer.description = this.description;
      realmsServer.state = this.state;
      realmsServer.owner = this.owner;
      realmsServer.players = this.players;
      realmsServer.slots = this.cloneSlots(this.slots);
      realmsServer.expired = this.expired;
      realmsServer.expiredTrial = this.expiredTrial;
      realmsServer.daysLeft = this.daysLeft;
      realmsServer.worldType = this.worldType;
      realmsServer.ownerUUID = this.ownerUUID;
      realmsServer.minigameName = this.minigameName;
      realmsServer.activeSlot = this.activeSlot;
      realmsServer.minigameId = this.minigameId;
      realmsServer.minigameImage = this.minigameImage;
      realmsServer.parentWorldName = this.parentWorldName;
      realmsServer.parentWorldId = this.parentWorldId;
      realmsServer.activeVersion = this.activeVersion;
      realmsServer.compatibility = this.compatibility;
      return realmsServer;
   }

   public Map cloneSlots(Map slots) {
      Map<Integer, RealmsWorldOptions> map = Maps.newHashMap();

      for(Map.Entry entry : slots.entrySet()) {
         map.put((Integer)entry.getKey(), ((RealmsWorldOptions)entry.getValue()).clone());
      }

      return map;
   }

   public boolean hasParentWorld() {
      return this.parentWorldId != -1L;
   }

   public boolean isMinigame() {
      return this.worldType == RealmsServer.WorldType.MINIGAME;
   }

   public String getWorldName(int slotId) {
      return this.name + " (" + ((RealmsWorldOptions)this.slots.get(slotId)).getSlotName(slotId) + ")";
   }

   public ServerInfo createServerInfo(String address) {
      return new ServerInfo(this.name, address, ServerInfo.ServerType.REALM);
   }

   // $FF: synthetic method
   public Object clone() throws CloneNotSupportedException {
      return this.clone();
   }

   @Environment(EnvType.CLIENT)
   public static enum Compatibility {
      UNVERIFIABLE,
      INCOMPATIBLE,
      RELEASE_TYPE_INCOMPATIBLE,
      NEEDS_DOWNGRADE,
      NEEDS_UPGRADE,
      COMPATIBLE;

      public boolean isCompatible() {
         return this == COMPATIBLE;
      }

      public boolean needsUpgrade() {
         return this == NEEDS_UPGRADE;
      }

      public boolean needsDowngrade() {
         return this == NEEDS_DOWNGRADE;
      }

      // $FF: synthetic method
      private static Compatibility[] method_54368() {
         return new Compatibility[]{UNVERIFIABLE, INCOMPATIBLE, RELEASE_TYPE_INCOMPATIBLE, NEEDS_DOWNGRADE, NEEDS_UPGRADE, COMPATIBLE};
      }
   }

   @Environment(EnvType.CLIENT)
   public static class McoServerComparator implements Comparator {
      private final String refOwner;

      public McoServerComparator(String owner) {
         this.refOwner = owner;
      }

      public int compare(RealmsServer realmsServer, RealmsServer realmsServer2) {
         return ComparisonChain.start().compareTrueFirst(realmsServer.hasParentWorld(), realmsServer2.hasParentWorld()).compareTrueFirst(realmsServer.state == RealmsServer.State.UNINITIALIZED, realmsServer2.state == RealmsServer.State.UNINITIALIZED).compareTrueFirst(realmsServer.expiredTrial, realmsServer2.expiredTrial).compareTrueFirst(realmsServer.owner.equals(this.refOwner), realmsServer2.owner.equals(this.refOwner)).compareFalseFirst(realmsServer.expired, realmsServer2.expired).compareTrueFirst(realmsServer.state == RealmsServer.State.OPEN, realmsServer2.state == RealmsServer.State.OPEN).compare(realmsServer.id, realmsServer2.id).result();
      }

      // $FF: synthetic method
      public int compare(final Object one, final Object two) {
         return this.compare((RealmsServer)one, (RealmsServer)two);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum State {
      CLOSED,
      OPEN,
      UNINITIALIZED;

      // $FF: synthetic method
      private static State[] method_36848() {
         return new State[]{CLOSED, OPEN, UNINITIALIZED};
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum WorldType {
      NORMAL,
      MINIGAME,
      ADVENTUREMAP,
      EXPERIENCE,
      INSPIRATION;

      // $FF: synthetic method
      private static WorldType[] method_36849() {
         return new WorldType[]{NORMAL, MINIGAME, ADVENTUREMAP, EXPERIENCE, INSPIRATION};
      }
   }
}
