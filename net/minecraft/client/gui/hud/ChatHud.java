package net.minecraft.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.network.message.ChatVisibility;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Nullables;
import net.minecraft.util.collection.ArrayListDeque;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ChatHud {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_MESSAGES = 100;
   private static final int MISSING_MESSAGE_INDEX = -1;
   private static final int field_39772 = 4;
   private static final int field_39773 = 4;
   private static final int OFFSET_FROM_BOTTOM = 40;
   private static final int REMOVAL_QUEUE_TICKS = 60;
   private static final Text DELETED_MARKER_TEXT;
   private final MinecraftClient client;
   private final ArrayListDeque messageHistory = new ArrayListDeque(100);
   private final List messages = Lists.newArrayList();
   private final List visibleMessages = Lists.newArrayList();
   private int scrolledLines;
   private boolean hasUnreadNewMessages;
   private final List removalQueue = new ArrayList();

   public ChatHud(MinecraftClient client) {
      this.client = client;
      this.messageHistory.addAll(client.getCommandHistoryManager().getHistory());
   }

   public void tickRemovalQueueIfExists() {
      if (!this.removalQueue.isEmpty()) {
         this.tickRemovalQueue();
      }

   }

   public void render(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused) {
      if (!this.isChatHidden()) {
         int i = this.getVisibleLineCount();
         int j = this.visibleMessages.size();
         if (j > 0) {
            this.client.getProfiler().push("chat");
            float f = (float)this.getChatScale();
            int k = MathHelper.ceil((float)this.getWidth() / f);
            int l = context.getScaledWindowHeight();
            context.getMatrices().push();
            context.getMatrices().scale(f, f, 1.0F);
            context.getMatrices().translate(4.0F, 0.0F, 0.0F);
            int m = MathHelper.floor((float)(l - 40) / f);
            int n = this.getMessageIndex(this.toChatLineX((double)mouseX), this.toChatLineY((double)mouseY));
            double d = (Double)this.client.options.getChatOpacity().getValue() * (double)0.9F + (double)0.1F;
            double e = (Double)this.client.options.getTextBackgroundOpacity().getValue();
            double g = (Double)this.client.options.getChatLineSpacing().getValue();
            int o = this.getLineHeight();
            int p = (int)Math.round((double)-8.0F * (g + (double)1.0F) + (double)4.0F * g);
            int q = 0;

            for(int r = 0; r + this.scrolledLines < this.visibleMessages.size() && r < i; ++r) {
               int s = r + this.scrolledLines;
               ChatHudLine.Visible visible = (ChatHudLine.Visible)this.visibleMessages.get(s);
               if (visible != null) {
                  int t = currentTick - visible.addedTime();
                  if (t < 200 || focused) {
                     double h = focused ? (double)1.0F : getMessageOpacityMultiplier(t);
                     int u = (int)((double)255.0F * h * d);
                     int v = (int)((double)255.0F * h * e);
                     ++q;
                     if (u > 3) {
                        int w = 0;
                        int x = m - r * o;
                        int y = x + p;
                        context.fill(-4, x - o, 0 + k + 4 + 4, x, v << 24);
                        MessageIndicator messageIndicator = visible.indicator();
                        if (messageIndicator != null) {
                           int z = messageIndicator.indicatorColor() | u << 24;
                           context.fill(-4, x - o, -2, x, z);
                           if (s == n && messageIndicator.icon() != null) {
                              int aa = this.getIndicatorX(visible);
                              Objects.requireNonNull(this.client.textRenderer);
                              int ab = y + 9;
                              this.drawIndicatorIcon(context, aa, ab, messageIndicator.icon());
                           }
                        }

                        context.getMatrices().push();
                        context.getMatrices().translate(0.0F, 0.0F, 50.0F);
                        context.drawTextWithShadow(this.client.textRenderer, (OrderedText)visible.content(), 0, y, 16777215 + (u << 24));
                        context.getMatrices().pop();
                     }
                  }
               }
            }

            long ac = this.client.getMessageHandler().getUnprocessedMessageCount();
            if (ac > 0L) {
               int ad = (int)((double)128.0F * d);
               int t = (int)((double)255.0F * e);
               context.getMatrices().push();
               context.getMatrices().translate(0.0F, (float)m, 0.0F);
               context.fill(-2, 0, k + 4, 9, t << 24);
               context.getMatrices().translate(0.0F, 0.0F, 50.0F);
               context.drawTextWithShadow(this.client.textRenderer, (Text)Text.translatable("chat.queue", new Object[]{ac}), 0, 1, 16777215 + (ad << 24));
               context.getMatrices().pop();
            }

            if (focused) {
               int ad = this.getLineHeight();
               int t = j * ad;
               int ae = q * ad;
               int af = this.scrolledLines * ae / j - m;
               int u = ae * ae / t;
               if (t != ae) {
                  int v = af > 0 ? 170 : 96;
                  int w = this.hasUnreadNewMessages ? 13382451 : 3355562;
                  int x = k + 4;
                  context.fill(x, -af, x + 2, -af - u, 100, w + (v << 24));
                  context.fill(x + 2, -af, x + 1, -af - u, 100, 13421772 + (v << 24));
               }
            }

            context.getMatrices().pop();
            this.client.getProfiler().pop();
         }
      }
   }

   private void drawIndicatorIcon(DrawContext context, int x, int y, MessageIndicator.Icon icon) {
      int i = y - icon.height - 1;
      icon.draw(context, x, i);
   }

   private int getIndicatorX(ChatHudLine.Visible line) {
      return this.client.textRenderer.getWidth(line.content()) + 4;
   }

   private boolean isChatHidden() {
      return this.client.options.getChatVisibility().getValue() == ChatVisibility.HIDDEN;
   }

   private static double getMessageOpacityMultiplier(int age) {
      double d = (double)age / (double)200.0F;
      d = (double)1.0F - d;
      d *= (double)10.0F;
      d = MathHelper.clamp(d, (double)0.0F, (double)1.0F);
      d *= d;
      return d;
   }

   public void clear(boolean clearHistory) {
      this.client.getMessageHandler().processAll();
      this.removalQueue.clear();
      this.visibleMessages.clear();
      this.messages.clear();
      if (clearHistory) {
         this.messageHistory.clear();
         this.messageHistory.addAll(this.client.getCommandHistoryManager().getHistory());
      }

   }

   public void addMessage(Text message) {
      this.addMessage(message, (MessageSignatureData)null, this.client.isConnectedToLocalServer() ? MessageIndicator.singlePlayer() : MessageIndicator.system());
   }

   public void addMessage(Text message, @Nullable MessageSignatureData signatureData, @Nullable MessageIndicator indicator) {
      ChatHudLine chatHudLine = new ChatHudLine(this.client.inGameHud.getTicks(), message, signatureData, indicator);
      this.logChatMessage(chatHudLine);
      this.addVisibleMessage(chatHudLine);
      this.addMessage(chatHudLine);
   }

   private void logChatMessage(ChatHudLine message) {
      String string = message.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
      String string2 = (String)Nullables.map(message.indicator(), MessageIndicator::loggedName);
      if (string2 != null) {
         LOGGER.info("[{}] [CHAT] {}", string2, string);
      } else {
         LOGGER.info("[CHAT] {}", string);
      }

   }

   private void addVisibleMessage(ChatHudLine message) {
      int i = MathHelper.floor((double)this.getWidth() / this.getChatScale());
      MessageIndicator.Icon icon = message.getIcon();
      if (icon != null) {
         i -= icon.width + 4 + 2;
      }

      List<OrderedText> list = ChatMessages.breakRenderedChatMessageLines(message.content(), i, this.client.textRenderer);
      boolean bl = this.isChatFocused();

      for(int j = 0; j < list.size(); ++j) {
         OrderedText orderedText = (OrderedText)list.get(j);
         if (bl && this.scrolledLines > 0) {
            this.hasUnreadNewMessages = true;
            this.scroll(1);
         }

         boolean bl2 = j == list.size() - 1;
         this.visibleMessages.add(0, new ChatHudLine.Visible(message.creationTick(), orderedText, message.indicator(), bl2));
      }

      while(this.visibleMessages.size() > 100) {
         this.visibleMessages.remove(this.visibleMessages.size() - 1);
      }

   }

   private void addMessage(ChatHudLine message) {
      this.messages.add(0, message);

      while(this.messages.size() > 100) {
         this.messages.remove(this.messages.size() - 1);
      }

   }

   private void tickRemovalQueue() {
      int i = this.client.inGameHud.getTicks();
      this.removalQueue.removeIf((message) -> {
         if (i >= message.deletableAfter()) {
            return this.queueForRemoval(message.signature()) == null;
         } else {
            return false;
         }
      });
   }

   public void removeMessage(MessageSignatureData signature) {
      RemovalQueuedMessage removalQueuedMessage = this.queueForRemoval(signature);
      if (removalQueuedMessage != null) {
         this.removalQueue.add(removalQueuedMessage);
      }

   }

   @Nullable
   private RemovalQueuedMessage queueForRemoval(MessageSignatureData signature) {
      int i = this.client.inGameHud.getTicks();
      ListIterator<ChatHudLine> listIterator = this.messages.listIterator();

      while(listIterator.hasNext()) {
         ChatHudLine chatHudLine = (ChatHudLine)listIterator.next();
         if (signature.equals(chatHudLine.signature())) {
            int j = chatHudLine.creationTick() + 60;
            if (i >= j) {
               listIterator.set(this.createRemovalMarker(chatHudLine));
               this.refresh();
               return null;
            }

            return new RemovalQueuedMessage(signature, j);
         }
      }

      return null;
   }

   private ChatHudLine createRemovalMarker(ChatHudLine original) {
      return new ChatHudLine(original.creationTick(), DELETED_MARKER_TEXT, (MessageSignatureData)null, MessageIndicator.system());
   }

   public void reset() {
      this.resetScroll();
      this.refresh();
   }

   private void refresh() {
      this.visibleMessages.clear();

      for(ChatHudLine chatHudLine : Lists.reverse(this.messages)) {
         this.addVisibleMessage(chatHudLine);
      }

   }

   public ArrayListDeque getMessageHistory() {
      return this.messageHistory;
   }

   public void addToMessageHistory(String message) {
      if (!message.equals(this.messageHistory.peekLast())) {
         if (this.messageHistory.size() >= 100) {
            this.messageHistory.removeFirst();
         }

         this.messageHistory.addLast(message);
      }

      if (message.startsWith("/")) {
         this.client.getCommandHistoryManager().add(message);
      }

   }

   public void resetScroll() {
      this.scrolledLines = 0;
      this.hasUnreadNewMessages = false;
   }

   public void scroll(int scroll) {
      this.scrolledLines += scroll;
      int i = this.visibleMessages.size();
      if (this.scrolledLines > i - this.getVisibleLineCount()) {
         this.scrolledLines = i - this.getVisibleLineCount();
      }

      if (this.scrolledLines <= 0) {
         this.scrolledLines = 0;
         this.hasUnreadNewMessages = false;
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY) {
      if (this.isChatFocused() && !this.client.options.hudHidden && !this.isChatHidden()) {
         MessageHandler messageHandler = this.client.getMessageHandler();
         if (messageHandler.getUnprocessedMessageCount() == 0L) {
            return false;
         } else {
            double d = mouseX - (double)2.0F;
            double e = (double)this.client.getWindow().getScaledHeight() - mouseY - (double)40.0F;
            if (d <= (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()) && e < (double)0.0F && e > (double)MathHelper.floor((double)-9.0F * this.getChatScale())) {
               messageHandler.process();
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Nullable
   public Style getTextStyleAt(double x, double y) {
      double d = this.toChatLineX(x);
      double e = this.toChatLineY(y);
      int i = this.getMessageLineIndex(d, e);
      if (i >= 0 && i < this.visibleMessages.size()) {
         ChatHudLine.Visible visible = (ChatHudLine.Visible)this.visibleMessages.get(i);
         return this.client.textRenderer.getTextHandler().getStyleAt(visible.content(), MathHelper.floor(d));
      } else {
         return null;
      }
   }

   @Nullable
   public MessageIndicator getIndicatorAt(double mouseX, double mouseY) {
      double d = this.toChatLineX(mouseX);
      double e = this.toChatLineY(mouseY);
      int i = this.getMessageIndex(d, e);
      if (i >= 0 && i < this.visibleMessages.size()) {
         ChatHudLine.Visible visible = (ChatHudLine.Visible)this.visibleMessages.get(i);
         MessageIndicator messageIndicator = visible.indicator();
         if (messageIndicator != null && this.isXInsideIndicatorIcon(d, visible, messageIndicator)) {
            return messageIndicator;
         }
      }

      return null;
   }

   private boolean isXInsideIndicatorIcon(double x, ChatHudLine.Visible line, MessageIndicator indicator) {
      if (x < (double)0.0F) {
         return true;
      } else {
         MessageIndicator.Icon icon = indicator.icon();
         if (icon == null) {
            return false;
         } else {
            int i = this.getIndicatorX(line);
            int j = i + icon.width;
            return x >= (double)i && x <= (double)j;
         }
      }
   }

   private double toChatLineX(double x) {
      return x / this.getChatScale() - (double)4.0F;
   }

   private double toChatLineY(double y) {
      double d = (double)this.client.getWindow().getScaledHeight() - y - (double)40.0F;
      return d / (this.getChatScale() * (double)this.getLineHeight());
   }

   private int getMessageIndex(double chatLineX, double chatLineY) {
      int i = this.getMessageLineIndex(chatLineX, chatLineY);
      if (i == -1) {
         return -1;
      } else {
         while(i >= 0) {
            if (((ChatHudLine.Visible)this.visibleMessages.get(i)).endOfEntry()) {
               return i;
            }

            --i;
         }

         return i;
      }
   }

   private int getMessageLineIndex(double chatLineX, double chatLineY) {
      if (this.isChatFocused() && !this.isChatHidden()) {
         if (!(chatLineX < (double)-4.0F) && !(chatLineX > (double)MathHelper.floor((double)this.getWidth() / this.getChatScale()))) {
            int i = Math.min(this.getVisibleLineCount(), this.visibleMessages.size());
            if (chatLineY >= (double)0.0F && chatLineY < (double)i) {
               int j = MathHelper.floor(chatLineY + (double)this.scrolledLines);
               if (j >= 0 && j < this.visibleMessages.size()) {
                  return j;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public boolean isChatFocused() {
      return this.client.currentScreen instanceof ChatScreen;
   }

   public int getWidth() {
      return getWidth((Double)this.client.options.getChatWidth().getValue());
   }

   public int getHeight() {
      return getHeight(this.isChatFocused() ? (Double)this.client.options.getChatHeightFocused().getValue() : (Double)this.client.options.getChatHeightUnfocused().getValue());
   }

   public double getChatScale() {
      return (Double)this.client.options.getChatScale().getValue();
   }

   public static int getWidth(double widthOption) {
      int i = 320;
      int j = 40;
      return MathHelper.floor(widthOption * (double)280.0F + (double)40.0F);
   }

   public static int getHeight(double heightOption) {
      int i = 180;
      int j = 20;
      return MathHelper.floor(heightOption * (double)160.0F + (double)20.0F);
   }

   public static double getDefaultUnfocusedHeight() {
      int i = 180;
      int j = 20;
      return (double)70.0F / (double)(getHeight((double)1.0F) - 20);
   }

   public int getVisibleLineCount() {
      return this.getHeight() / this.getLineHeight();
   }

   private int getLineHeight() {
      Objects.requireNonNull(this.client.textRenderer);
      return (int)((double)9.0F * ((Double)this.client.options.getChatLineSpacing().getValue() + (double)1.0F));
   }

   public ChatState toChatState() {
      return new ChatState(List.copyOf(this.messages), List.copyOf(this.messageHistory), List.copyOf(this.removalQueue));
   }

   public void restoreChatState(ChatState state) {
      this.messageHistory.clear();
      this.messageHistory.addAll(state.messageHistory);
      this.removalQueue.clear();
      this.removalQueue.addAll(state.removalQueue);
      this.messages.clear();
      this.messages.addAll(state.messages);
      this.refresh();
   }

   static {
      DELETED_MARKER_TEXT = Text.translatable("chat.deleted_marker").formatted(new Formatting[]{Formatting.GRAY, Formatting.ITALIC});
   }

   @Environment(EnvType.CLIENT)
   public static class ChatState {
      final List messages;
      final List messageHistory;
      final List removalQueue;

      public ChatState(List messages, List messageHistory, List removalQueue) {
         this.messages = messages;
         this.messageHistory = messageHistory;
         this.removalQueue = removalQueue;
      }
   }

   @Environment(EnvType.CLIENT)
   static record RemovalQueuedMessage(MessageSignatureData signature, int deletableAfter) {
   }
}
