package cn.islys.command;

import cn.islys.config.AutoMessengerConfig;
import cn.islys.util.MessageImporter;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AutoMessageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("automessage") // 2. 使用 Commands.literal
                .then(Commands.literal("import")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            sendFeedback(source,
                                    Component.literal("正在导入消息列表...").withStyle(ChatFormatting.YELLOW)
                            );

                            // 异步执行避免卡顿
                            new Thread(() -> {
                                MessageImporter.ImportResult result = MessageImporter.importFromDefault();

                                Minecraft.getInstance().execute(() -> {
                                    if (result.success()) {
                                        sendFeedback(source,
                                                Component.literal("✓ " + result.message())
                                                        .withStyle(ChatFormatting.GREEN)
                                        );

                                        // 显示部分错误
                                        if (result.errors() != null && !result.errors().isEmpty()) {
                                            int showErrors = Math.min(3, result.errors().size());
                                            for (int i = 0; i < showErrors; i++) {
                                                sendFeedback(source,
                                                        Component.literal("! " + result.errors().get(i))
                                                                .withStyle(ChatFormatting.RED)
                                                );
                                            }
                                            if (result.errors().size() > 3) {
                                                sendFeedback(source,
                                                        Component.literal("... 还有 " + (result.errors().size() - 3) + " 个错误")
                                                                .withStyle(ChatFormatting.GRAY)
                                                );
                                            }
                                        }
                                    } else {
                                        sendFeedback(source,
                                                Component.literal("✗ " + result.message())
                                                        .withStyle(ChatFormatting.RED)
                                        );
                                    }
                                });
                            }, "AutoMessage-Import").start();

                            return 1;
                        })
                )
                .then(Commands.literal("export")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            boolean success = MessageImporter.exportToDefault();
                            if (success) {
                                sendFeedback(source,
                                        Component.literal("✓ 已导出到 config/rss-automessage/messages.txt")
                                                .withStyle(ChatFormatting.GREEN)
                                );
                            } else {
                                sendFeedback(source,
                                        Component.literal("✗ 导出失败")
                                                .withStyle(ChatFormatting.RED)
                                );
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("example")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            MessageImporter.createExampleFile();
                            sendFeedback(source,
                                    Component.literal("✓ 已创建示例文件 config/rss-automessage/messages-example.txt")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
                .then(Commands.literal("clear")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            AutoMessengerConfig config = AutoMessengerConfig.getInstance();
                            int count = config.scheduledMessageList.size();
                            config.scheduledMessageList.clear();
                            AutoMessengerConfig.save();
                            sendFeedback(source,
                                    Component.literal("✓ 已清空 " + count + " 条消息")
                                            .withStyle(ChatFormatting.GREEN)
                            );
                            return 1;
                        })
                )
        );
    }
    private static void sendFeedback(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

}
