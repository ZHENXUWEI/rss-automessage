package cn.islys;

import cn.islys.command.AutoMessageCommand;
import cn.islys.config.AutoMessengerConfig;
import cn.islys.config.AutoMessengerConfig.MessageEntry;
import cn.islys.hud.CountdownHud;
import cn.islys.util.MessageImporter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class RSsAutoMessageClient implements ClientModInitializer {

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

    @Override
    public void onInitializeClient() {
        instance = this;

        AutoMessengerConfig.load();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onServerJoined();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onServerLeft();
        });

        // 26.1 中 ClientReceiveMessageEvents.GAME 改为 CHAT
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            autoReply.onChatMessage(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onClientTick(client);
        });

        countdownHud.register();

        AutoMessageCommand.register();

        MessageImporter.createExampleFile();
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