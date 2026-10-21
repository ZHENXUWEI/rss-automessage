package cn.islys;

import cn.islys.command.AutoMessageCommand;
import cn.islys.config.AutoMessengerConfig;
import cn.islys.config.AutoMessengerConfig.MessageEntry;
import cn.islys.hud.CountdownHud;
import cn.islys.util.MessageImporter;
import cn.islys.config.ModConfigScreen;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

@Mod(RSsAutoMessage.MOD_ID)
public class RSsAutoMessageClient {

    private static RSsAutoMessageClient instance;
    private final MessageScheduler scheduler = new MessageScheduler();
    private final AutoReplyHandler autoReply = new AutoReplyHandler();
    private final CountdownHud countdownHud = new CountdownHud();

    private long lastSendTime = 0;
    private int remainingMs = 0;

    private List<MessageEntry> pendingMessages = null;
    private int sendIndex = 0;
    private int sendTickDelay = 0;

    private volatile boolean stopRequested = false;

    public RSsAutoMessageClient(FMLJavaModLoadingContext context) {
        instance = this;
        AutoMessengerConfig.load();

        context.registerExtensionPoint(ConfigScreenFactory.class,
                () -> new ConfigScreenFactory(ModConfigScreen::create));
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(event -> onServerJoined());
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> onServerLeft());
        ClientChatReceivedEvent.BUS.addListener(Priority.LOWEST, this::onChatReceived);
        ClientTickEvent.Post.BUS.addListener(event -> onClientTick(Minecraft.getInstance()));
        RegisterClientCommandsEvent.BUS.addListener(event -> AutoMessageCommand.register(event.getDispatcher()));
        AddGuiOverlayLayersEvent.BUS.addListener(countdownHud::register);

        try {
            MessageImporter.getTextLibrary().ensureDirectory();
        } catch (java.io.IOException e) {
            RSsAutoMessage.LOGGER.error("Could not create TXT directory", e);
        }
    }

    private void onChatReceived(ClientChatReceivedEvent event) {
        // Forge posts this event for player and disguised chat, excluding system messages.
        autoReply.onChatMessage(event.getMessage().getString());
    }

    private void onServerJoined() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        stopRequested = false;

        if (config.sendOnJoin && config.enableScheduledMessages) {
            sendAllScheduledMessages();
        }

        if (config.enableScheduledMessages) {
            remainingMs = config.sendIntervalMs;
        }

        lastSendTime = System.currentTimeMillis();
        countdownHud.updateCountdown(remainingMs);
        AutoMessengerConfig.save();
    }

    private void onServerLeft() {
        stopAllSending();
    }

    private void onClientTick(Minecraft client) {
        if (client.player == null || client.getConnection() == null) return;

        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        if (!config.enableScheduledMessages) {
            if (!stopRequested && pendingMessages != null) {
                stopAllSending();
            }
            return;
        } else {
            stopRequested = false;
        }

        if (pendingMessages != null && !pendingMessages.isEmpty()) {
            if (stopRequested) {
                pendingMessages = null;
                sendIndex = 0;
                sendTickDelay = 0;
                return;
            }

            sendTickDelay++;
            if (sendTickDelay >= 3) {
                sendTickDelay = 0;
                sendNextPendingMessage(client);
            }
            return;
        }

        if (!config.enableLoop) return;

        long now = System.currentTimeMillis();
        int elapsed = (int) (now - lastSendTime);
        remainingMs = Math.max(0, config.sendIntervalMs - elapsed);

        countdownHud.updateCountdown(remainingMs);

        if (remainingMs <= 0) {
            sendAllScheduledMessages();
            lastSendTime = now;
            remainingMs = config.sendIntervalMs;
            AutoMessengerConfig.save();
        }
    }

    private void sendNextPendingMessage(Minecraft client) {
        if (stopRequested) {
            pendingMessages = null;
            sendIndex = 0;
            return;
        }

        if (pendingMessages == null || sendIndex >= pendingMessages.size()) {
            pendingMessages = null;
            sendIndex = 0;
            return;
        }

        MessageEntry entry = pendingMessages.get(sendIndex);
        String msg = entry.getSendText();

        if (!msg.isEmpty()) {
            if (entry.isCommand) {
                client.player.connection.sendCommand(msg);
            } else {
                client.player.connection.sendChat(msg);
            }
        }

        sendIndex++;

        if (sendIndex >= pendingMessages.size()) {
            pendingMessages = null;
            sendIndex = 0;
        }
    }

    private void sendAllScheduledMessages() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        List<MessageEntry> messages = config.getScheduledMessages();

        if (messages == null || messages.isEmpty()) return;

        List<MessageEntry> sendList = new ArrayList<>(messages);

        if (config.randomSend && sendList.size() > 1) {
            java.util.Collections.shuffle(sendList);
        }

        this.pendingMessages = sendList;
        this.sendIndex = 0;
        this.sendTickDelay = 0;
    }

    public void stopAllSending() {
        stopRequested = true;
        pendingMessages = null;
        sendIndex = 0;
        sendTickDelay = 0;
        remainingMs = 0;
        countdownHud.updateCountdown(0);
        System.out.println("[AutoMessage] All sending stopped");
    }

    public void onMessageListChanged() {
        stopAllSending();
        resetStopFlag();
        resetCountdown();
    }
    public void resetStopFlag() {
        stopRequested = false;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void sendMessage(String message) {
        scheduler.sendMessage(message);
    }

    public void resetCountdown() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        lastSendTime = System.currentTimeMillis();
        remainingMs = config.sendIntervalMs;
        countdownHud.updateCountdown(remainingMs);
    }

    public int getRemainingMs() { return remainingMs; }
    public MessageScheduler getScheduler() { return scheduler; }
    public AutoReplyHandler getAutoReply() { return autoReply; }
    public CountdownHud getCountdownHud() { return countdownHud; }
    public static RSsAutoMessageClient getInstance() { return instance; }
}
