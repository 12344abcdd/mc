package net.minecraft.client.realms.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Date;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.util.JsonUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Backup extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String backupId;
   public Date lastModifiedDate;
   public long size;
   private boolean uploadedVersion;
   public Map metadata = Maps.newHashMap();
   public Map changeList = Maps.newHashMap();

   public static Backup parse(JsonElement node) {
      JsonObject jsonObject = node.getAsJsonObject();
      Backup backup = new Backup();

      try {
         backup.backupId = JsonUtils.getNullableStringOr("backupId", jsonObject, "");
         backup.lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", jsonObject);
         backup.size = JsonUtils.getLongOr("size", jsonObject, 0L);
         if (jsonObject.has("metadata")) {
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("metadata");

            for(Map.Entry entry : jsonObject2.entrySet()) {
               if (!((JsonElement)entry.getValue()).isJsonNull()) {
                  backup.metadata.put((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
               }
            }
         }
      } catch (Exception exception) {
         LOGGER.error("Could not parse Backup: {}", exception.getMessage());
      }

      return backup;
   }

   public boolean isUploadedVersion() {
      return this.uploadedVersion;
   }

   public void setUploadedVersion(boolean uploadedVersion) {
      this.uploadedVersion = uploadedVersion;
   }
}
