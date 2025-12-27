package net.minecraft.client.network;

import com.mojang.authlib.GameProfile;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.ServerLinks;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ClientConnectionState(GameProfile localGameProfile, WorldSession worldSession, DynamicRegistryManager.Immutable receivedRegistries, FeatureSet enabledFeatures, @Nullable String serverBrand, @Nullable ServerInfo serverInfo, @Nullable Screen postDisconnectScreen, Map serverCookies, @Nullable ChatHud.ChatState chatState, boolean strictErrorHandling, Map customReportDetails, ServerLinks serverLinks) {
   public ClientConnectionState(GameProfile gameProfile, WorldSession worldSession, DynamicRegistryManager.Immutable immutable, FeatureSet featureSet, @Nullable String string, @Nullable ServerInfo serverInfo, @Nullable Screen screen, Map map, @Nullable ChatHud.ChatState chatState, @Deprecated(forRemoval = true) boolean bl, Map map2, ServerLinks serverLinks) {
      this.localGameProfile = gameProfile;
      this.worldSession = worldSession;
      this.receivedRegistries = immutable;
      this.enabledFeatures = featureSet;
      this.serverBrand = string;
      this.serverInfo = serverInfo;
      this.postDisconnectScreen = screen;
      this.serverCookies = map;
      this.chatState = chatState;
      this.strictErrorHandling = bl;
      this.customReportDetails = map2;
      this.serverLinks = serverLinks;
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public boolean strictErrorHandling() {
      return this.strictErrorHandling;
   }
}
