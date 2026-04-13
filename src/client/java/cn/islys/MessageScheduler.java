package cn.islys;

import net.minecraft.client.Minecraft;

public class MessageScheduler {

    public void sendMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // 判断是否为命令
        if (message.startsWith("/")) {
            client.player.connection.sendCommand(message.substring(1));
        } else {
            client.player.connection.sendChat(message);
        }
    }

    public void reset() {
        // 重置状态
    }
}
