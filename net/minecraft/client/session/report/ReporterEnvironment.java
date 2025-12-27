package net.minecraft.client.session.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.dto.RealmsServer;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ReporterEnvironment(String clientVersion, @Nullable Server server) {
   public static ReporterEnvironment ofIntegratedServer() {
      return ofServer((Server)null);
   }

   public static ReporterEnvironment ofThirdPartyServer(String ip) {
      return ofServer(new Server.ThirdParty(ip));
   }

   public static ReporterEnvironment ofRealm(RealmsServer server) {
      return ofServer(new Server.Realm(server));
   }

   public static ReporterEnvironment ofServer(@Nullable Server server) {
      return new ReporterEnvironment(getVersion(), server);
   }

   public AbuseReportRequest.ClientInfo toClientInfo() {
      return new AbuseReportRequest.ClientInfo(this.clientVersion, Locale.getDefault().toLanguageTag());
   }

   @Nullable
   public AbuseReportRequest.ThirdPartyServerInfo toThirdPartyServerInfo() {
      Server var2 = this.server;
      if (var2 instanceof Server.ThirdParty thirdParty) {
         return new AbuseReportRequest.ThirdPartyServerInfo(thirdParty.ip);
      } else {
         return null;
      }
   }

   @Nullable
   public AbuseReportRequest.RealmInfo toRealmInfo() {
      Server var2 = this.server;
      if (var2 instanceof Server.Realm realm) {
         return new AbuseReportRequest.RealmInfo(String.valueOf(realm.realmId()), realm.slotId());
      } else {
         return null;
      }
   }

   private static String getVersion() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("1.21.1");
      if (MinecraftClient.getModStatus().isModded()) {
         stringBuilder.append(" (modded)");
      }

      return stringBuilder.toString();
   }

   @Environment(EnvType.CLIENT)
   public interface Server {
      @Environment(EnvType.CLIENT)
      public static record Realm(long realmId, int slotId) implements Server {
         public Realm(RealmsServer server) {
            this(server.id, server.activeSlot);
         }
      }

      @Environment(EnvType.CLIENT)
      public static record ThirdParty(String ip) implements Server {
         final String ip;
      }
   }
}
