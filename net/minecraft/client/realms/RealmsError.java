package net.minecraft.client.realms;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.exception.RealmsHttpException;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public interface RealmsError {
   Text NO_DETAILS_TEXT = Text.translatable("mco.errorMessage.noDetails");
   Logger LOGGER = LogUtils.getLogger();

   int getErrorCode();

   Text getText();

   String getErrorMessage();

   static RealmsError ofHttp(int statusCode, String response) {
      if (statusCode == 429) {
         return RealmsError.SimpleHttpError.SERVICE_BUSY;
      } else if (Strings.isNullOrEmpty(response)) {
         return RealmsError.SimpleHttpError.statusCodeOnly(statusCode);
      } else {
         try {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            String string = JsonHelper.getString(jsonObject, "reason", (String)null);
            String string2 = JsonHelper.getString(jsonObject, "errorMsg", (String)null);
            int i = JsonHelper.getInt(jsonObject, "errorCode", -1);
            if (string2 != null || string != null || i != -1) {
               return new DetailedHttpError(statusCode, i != -1 ? i : statusCode, string, string2);
            }
         } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError", exception);
         }

         return new RawHttpPayloadError(statusCode, response);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record AuthenticationError(String message) implements RealmsError {
      public static final int ERROR_CODE = 401;

      public int getErrorCode() {
         return 401;
      }

      public Text getText() {
         return Text.literal(this.message);
      }

      public String getErrorMessage() {
         return String.format(Locale.ROOT, "Realms authentication error with message '%s'", this.message);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record DetailedHttpError(int httpCode, int code, @Nullable String reason, @Nullable String message) implements RealmsError {
      public int getErrorCode() {
         return this.code;
      }

      public Text getText() {
         String string = "mco.errorMessage." + this.code;
         if (I18n.hasTranslation(string)) {
            return Text.translatable(string);
         } else {
            if (this.reason != null) {
               String string2 = "mco.errorReason." + this.reason;
               if (I18n.hasTranslation(string2)) {
                  return Text.translatable(string2);
               }
            }

            return (Text)(this.message != null ? Text.literal(this.message) : NO_DETAILS_TEXT);
         }
      }

      public String getErrorMessage() {
         return String.format(Locale.ROOT, "Realms service error (%d/%d/%s) with message '%s'", this.httpCode, this.code, this.reason, this.message);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record RawHttpPayloadError(int httpCode, String payload) implements RealmsError {
      public int getErrorCode() {
         return this.httpCode;
      }

      public Text getText() {
         return Text.literal(this.payload);
      }

      public String getErrorMessage() {
         return String.format(Locale.ROOT, "Realms service error (%d) with raw payload '%s'", this.httpCode, this.payload);
      }
   }

   @Environment(EnvType.CLIENT)
   public static record SimpleHttpError(int httpCode, @Nullable Text payload) implements RealmsError {
      public static final SimpleHttpError SERVICE_BUSY = new SimpleHttpError(429, Text.translatable("mco.errorMessage.serviceBusy"));
      public static final Text RETRY_TEXT = Text.translatable("mco.errorMessage.retry");

      public static SimpleHttpError unknownCompatibility(String response) {
         return new SimpleHttpError(500, Text.translatable("mco.errorMessage.realmsService.unknownCompatibility", new Object[]{response}));
      }

      public static SimpleHttpError connectivity(RealmsHttpException exception) {
         return new SimpleHttpError(500, Text.translatable("mco.errorMessage.realmsService.connectivity", new Object[]{exception.getMessage()}));
      }

      public static SimpleHttpError retryable(int statusCode) {
         return new SimpleHttpError(statusCode, RETRY_TEXT);
      }

      public static SimpleHttpError statusCodeOnly(int statusCode) {
         return new SimpleHttpError(statusCode, (Text)null);
      }

      public int getErrorCode() {
         return this.httpCode;
      }

      public Text getText() {
         return this.payload != null ? this.payload : NO_DETAILS_TEXT;
      }

      public String getErrorMessage() {
         return this.payload != null ? String.format(Locale.ROOT, "Realms service error (%d) with message '%s'", this.httpCode, this.payload.getString()) : String.format(Locale.ROOT, "Realms service error (%d) with no payload", this.httpCode);
      }
   }
}
