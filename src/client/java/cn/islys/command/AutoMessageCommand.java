package cn.islys.command;

import cn.islys.config.AutoMessengerConfig;
import cn.islys.util.MessageImporter;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AutoMessageCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("automessage")
                .then(ClientCommandManager.literal("import")
                        .executes(context -> {
                            context.getSource().sendFeedback(
                                    Component.literal("正在导入消息列表...").withStyle(ChatFormatting.YELLOW)
                            );

                            // 异步执行避免卡顿
                            new Thread(() -> {
                                MessageImporter.ImportResult result = MessageImporter.importFromDefault();

                                context.getSource().getClient().execute(() -> {
                                    if (result.success()) {
                                        context.getSource().sendFeedback(
                                                Component.literal("✓ " + result.message())
                                                        .withStyle(ChatFormatting.GREEN)
                                        );

                                        // 显示部分错误
                                        if (result.errors() != null && !result.errors().isEmpty()) {
                                            int showErrors = Math.min(3, result.errors().size());
                                            for (int i = 0; i < showErrors; i++) {
                                                context.getSource().sendFeedback(
                                                        Component.literal("! " + result.errors().get(i))
                                                                .withStyle(ChatFormatting.RED)
                                                );
                                            }
                                            if (result.errors().size() > 3) {
                                                context.getSource().sendFeedback(
                                                        Component.literal("... 还有 " + (result.errors().size() - 3) + " 个错误")
                                                                .withStyle(ChatFormatting.GRAY)
                                                );
                                            }
                                        }
                                    } else {
                                        context.getSource().sendFeedback(
                                                Component.literal("✗ " + result.message())
                                                        .withStyle(ChatFormatting.RED)
                                        );
                                    }
                                });
                            }, "AutoMessage-Import").start();

                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("export")
                        .executes(context -> {
                            boolean success = MessageImporter.exportToDefault();
                            if (success) {
                                context.getSource().sendFeedback(
                                        Component.literal("✓ 已导出到 config/rss-automessage/messages.txt")
                                                .withStyle(ChatFormatting.GREEN)
                                );
                            } else {
                                context.getSource().sendFeedback(
                                        Component.literal("✗ 导出失败")
                                                .withStyle(ChatFormatting.RED)
                                );
                            }
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("example")
                        .executes(context -> {
                            MessageImporter.createExampleFile();
                            context.getSource().sendFeedback(
                                    Component.literal("✓ 已创建示例文件 config/rss-automessage/messages-example.txt")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("clear")
                        .executes(context -> {
                            AutoMessengerConfig config = AutoMessengerConfig.getInstance();
                            int count = config.scheduledMessageList.size();
                            config.scheduledMessageList.clear();
                            AutoMessengerConfig.save();
                            context.getSource().sendFeedback(
                                    Component.literal("✓ 已清空 " + count + " 条消息")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
        );
    }
}