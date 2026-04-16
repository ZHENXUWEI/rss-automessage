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

    // 定时器
    private long lastSendTime = 0;
    private int remainingMs = 0;

    // 批量发送状态
    private List<MessageEntry> pendingMessages = null;
    private int sendIndex = 0;
    private int sendTickDelay = 0;

    // 停止标志 - 关键：用于立即停止发送
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

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                autoReply.onChatMessage(message.getString());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onClientTick(client);
        });

        countdownHud.register();

        // 注册命令
        AutoMessageCommand.register();

        // 启动时检查并创建示例文件
        MessageImporter.createExampleFile();
    }

    private void onServerJoined() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        // 重置停止标志
        stopRequested = false;

        if (config.sendOnJoin && config.enableScheduledMessages) {
//            不随机
//            startBatchSend(config.getScheduledMessages());
//            随机
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

        // 检查是否需要停止发送（功能被禁用）
        if (!config.enableScheduledMessages) {
            if (!stopRequested && pendingMessages != null) {
                stopAllSending();
            }
            return;
        } else {
            // 功能重新启用时，重置停止标志
            stopRequested = false;
        }

        // 处理批量发送（每3tick发送一条，避免卡顿）
        if (pendingMessages != null && !pendingMessages.isEmpty()) {
            // 检查停止请求
            if (stopRequested) {
                pendingMessages = null;
                sendIndex = 0;
                sendTickDelay = 0;
                return;
            }

            sendTickDelay++;
            if (sendTickDelay >= 3) { // 每3tick（约150ms）发送一条
                sendTickDelay = 0;
                sendNextPendingMessage(client);
            }
            return; // 发送期间不检查间隔
        }

        if (!config.enableLoop) return;

        // 计算剩余时间
        long now = System.currentTimeMillis();
        int elapsed = (int) (now - lastSendTime);
        remainingMs = Math.max(0, config.sendIntervalMs - elapsed);

        // 更新倒计时显示
        countdownHud.updateCountdown(remainingMs);

        // 时间到了，开始批量发送
        if (remainingMs <= 0) {
            sendAllScheduledMessages();
            lastSendTime = now;
            remainingMs = config.sendIntervalMs;
            AutoMessengerConfig.save();
        }
    }

    // 开始批量发送
    private void startBatchSend(List<MessageEntry> messages) {
        if (messages == null || messages.isEmpty()) return;
        if (stopRequested) return; // 如果已请求停止，则不开始

        this.pendingMessages = messages;
        this.sendIndex = 0;
        this.sendTickDelay = 0;
    }

    // 发送下一条待处理的消息
    private void sendNextPendingMessage(Minecraft client) {
        // 检查停止请求
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

        // 全部发送完毕
        if (sendIndex >= pendingMessages.size()) {
            pendingMessages = null;
            sendIndex = 0;
        }
    }

    // 实现随机发送
    private void sendAllScheduledMessages() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        List<MessageEntry> messages = config.getScheduledMessages();

        // 创建副本，不破坏原列表
        List<MessageEntry> sendList = new ArrayList<>(messages);

        // 只打乱本次发送的副本，原配置保持不变
        if (config.randomSend && sendList.size() > 1) {
            java.util.Collections.shuffle(sendList);
        }

        // 发送打乱后的副本
        this.pendingMessages = sendList;
        this.sendIndex = 0;
        this.sendTickDelay = 0;
    }

    /**
     * 公共方法：停止所有发送
     * 当关闭功能时调用，立即停止所有待发送消息
     */
    public void stopAllSending() {
        stopRequested = true;
        pendingMessages = null;
        sendIndex = 0;
        sendTickDelay = 0;
        remainingMs = 0;
        countdownHud.updateCountdown(0);
        System.out.println("[AutoMessage] All sending stopped");
    }

    /**
     * 重置停止标志（当功能重新启用时调用）
     */
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