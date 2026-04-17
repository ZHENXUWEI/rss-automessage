package cn.islys.command;

import cn.islys.config.AutoMessengerConfig;
import cn.islys.util.MessageImporter;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands; // 1. 导入新的类
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutoMessageCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("automessage") // 2. 使用 ClientCommands.literal
                .then(ClientCommands.literal("import")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            source.sendFeedback(
                                    Component.literal("正在导入消息列表...").withStyle(ChatFormatting.YELLOW)
                            );

                            // 异步执行避免卡顿
                            new Thread(() -> {
                                MessageImporter.ImportResult result = MessageImporter.importFromDefault();

                                Minecraft.getInstance().execute(() -> {
                                    if (result.success()) {
                                        source.sendFeedback(
                                                Component.literal("✓ " + result.message())
                                                        .withStyle(ChatFormatting.GREEN)
                                        );

                                        // 显示部分错误
                                        if (result.errors() != null && !result.errors().isEmpty()) {
                                            int showErrors = Math.min(3, result.errors().size());
                                            for (int i = 0; i < showErrors; i++) {
                                                source.sendFeedback(
                                                        Component.literal("! " + result.errors().get(i))
                                                                .withStyle(ChatFormatting.RED)
                                                );
                                            }
                                            if (result.errors().size() > 3) {
                                                source.sendFeedback(
                                                        Component.literal("... 还有 " + (result.errors().size() - 3) + " 个错误")
                                                                .withStyle(ChatFormatting.GRAY)
                                                );
                                            }
                                        }
                                    } else {
                                        source.sendFeedback(
                                                Component.literal("✗ " + result.message())
                                                        .withStyle(ChatFormatting.RED)
                                        );
                                    }
                                });
                            }, "AutoMessage-Import").start();

                            return 1;
                        })
                )
                .then(ClientCommands.literal("export")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            boolean success = MessageImporter.exportToDefault();
                            if (success) {
                                source.sendFeedback(
                                        Component.literal("✓ 已导出到 config/rss-automessage/messages.txt")
                                                .withStyle(ChatFormatting.GREEN)
                                );
                            } else {
                                source.sendFeedback(
                                        Component.literal("✗ 导出失败")
                                                .withStyle(ChatFormatting.RED)
                                );
                            }
                            return 1;
                        })
                )
                .then(ClientCommands.literal("example")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            MessageImporter.createExampleFile();
                            source.sendFeedback(
                                    Component.literal("✓ 已创建示例文件 config/rss-automessage/messages-example.txt")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
                .then(ClientCommands.literal("clear")
                        .executes(context -> {
                            FabricClientCommandSource source = context.getSource();
                            AutoMessengerConfig config = AutoMessengerConfig.getInstance();
                            int count = config.scheduledMessageList.size();
                            config.scheduledMessageList.clear();
                            AutoMessengerConfig.save();
                            source.sendFeedback(
                                    Component.literal("✓ 已清空 " + count + " 条消息")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
        );
    }
}