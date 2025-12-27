package net.minecraft.client.session.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.logging.LogWriter;
import net.minecraft.util.thread.TaskExecutor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ThreadedLogWriter implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final LogWriter writer;
   private final TaskExecutor executor;

   public ThreadedLogWriter(FileChannel channel, Executor executor) {
      this.writer = new LogWriter(SentTelemetryEvent.CODEC, channel);
      this.executor = TaskExecutor.create(executor, "telemetry-event-log");
   }

   public TelemetryLogger getLogger() {
      return (event) -> this.executor.send((Runnable)() -> {
            try {
               this.writer.write(event);
            } catch (IOException iOException) {
               LOGGER.error("Failed to write telemetry event to log", iOException);
            }

         });
   }

   public void close() {
      this.executor.send((Runnable)() -> IOUtils.closeQuietly(this.writer));
      this.executor.close();
   }
}
