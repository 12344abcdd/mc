package net.minecraft.client.realms;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsClientIncompatibleScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsParentalConsentScreen;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsAvailability {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private static CompletableFuture currentFuture;

   public static CompletableFuture check() {
      if (currentFuture == null || wasUnsuccessful(currentFuture)) {
         currentFuture = checkInternal();
      }

      return currentFuture;
   }

   private static boolean wasUnsuccessful(CompletableFuture future) {
      Info info = (Info)future.getNow((Object)null);
      return info != null && info.exception() != null;
   }

   private static CompletableFuture checkInternal() {
      Session session = MinecraftClient.getInstance().getSession();
      return session.getAccountType() != Session.AccountType.MSA ? CompletableFuture.completedFuture(new Info(RealmsAvailability.Type.AUTHENTICATION_ERROR)) : CompletableFuture.supplyAsync(() -> {
         RealmsClient realmsClient = RealmsClient.create();

         try {
            if (realmsClient.clientCompatible() != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
               return new Info(RealmsAvailability.Type.INCOMPATIBLE_CLIENT);
            } else {
               return !realmsClient.mcoEnabled() ? new Info(RealmsAvailability.Type.NEEDS_PARENTAL_CONSENT) : new Info(RealmsAvailability.Type.SUCCESS);
            }
         } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't connect to realms", realmsServiceException);
            return realmsServiceException.error.getErrorCode() == 401 ? new Info(RealmsAvailability.Type.AUTHENTICATION_ERROR) : new Info(realmsServiceException);
         }
      }, Util.getIoWorkerExecutor());
   }

   @Environment(EnvType.CLIENT)
   public static record Info(Type type, @Nullable RealmsServiceException exception) {
      public Info(Type type) {
         this(type, (RealmsServiceException)null);
      }

      public Info(RealmsServiceException exception) {
         this(RealmsAvailability.Type.UNEXPECTED_ERROR, exception);
      }

      @Nullable
      public Screen createScreen(Screen parent) {
         Object var10000;
         switch (this.type.ordinal()) {
            case 0 -> var10000 = null;
            case 1 -> var10000 = new RealmsClientIncompatibleScreen(parent);
            case 2 -> var10000 = new RealmsParentalConsentScreen(parent);
            case 3 -> var10000 = new RealmsGenericErrorScreen(Text.translatable("mco.error.invalid.session.title"), Text.translatable("mco.error.invalid.session.message"), parent);
            case 4 -> var10000 = new RealmsGenericErrorScreen((RealmsServiceException)Objects.requireNonNull(this.exception), parent);
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return (Screen)var10000;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum Type {
      SUCCESS,
      INCOMPATIBLE_CLIENT,
      NEEDS_PARENTAL_CONSENT,
      AUTHENTICATION_ERROR,
      UNEXPECTED_ERROR;

      // $FF: synthetic method
      private static Type[] method_52629() {
         return new Type[]{SUCCESS, INCOMPATIBLE_CLIENT, NEEDS_PARENTAL_CONSENT, AUTHENTICATION_ERROR, UNEXPECTED_ERROR};
      }
   }
}
