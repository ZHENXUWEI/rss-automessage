package cn.islys.config;

import cn.islys.RSsAutoMessageClient;
import cn.islys.util.MessageImporter;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        AutoMessengerConfig defaults = new AutoMessengerConfig();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.auto-messenger.title"))
                .category(buildScheduledCategory(defaults, config))
                .category(buildReplyCategory(defaults, config))
                .category(buildDisplayCategory(defaults, config))
                .save(() -> AutoMessengerConfig.save())
                .build()
                .generateScreen(parent);
    }

    // ========== 定时发送分类 ==========
    private static ConfigCategory buildScheduledCategory(AutoMessengerConfig defaults, AutoMessengerConfig config) {
        ConfigCategory.Builder scheduledCat = ConfigCategory.createBuilder()
                .name(Component.translatable("config.auto-messenger.category.scheduled"));

        // 基本选项
        scheduledCat.option(createBoolOption(
                Component.translatable("config.auto-messenger.enable_scheduled"),
                Component.translatable("config.auto-messenger.enable_scheduled.desc"),
                defaults.enableScheduledMessages,
                () -> config.enableScheduledMessages,
                value -> {
                    config.enableScheduledMessages = value;
                    if (!value) {
                        RSsAutoMessageClient client = RSsAutoMessageClient.getInstance();
                        if (client != null) {
                            client.stopAllSending();
                        }
                    }
                }
        ));

        scheduledCat.option(createBoolOption(
                Component.translatable("config.auto-messenger.send_on_join"),
                Component.translatable("config.auto-messenger.send_on_join.desc"),
                defaults.sendOnJoin,
                () -> config.sendOnJoin,
                value -> config.sendOnJoin = value
        ));

        scheduledCat.option(Option.<Integer>createBuilder()
                .name(Component.translatable("config.auto-messenger.send_interval"))
                .description(OptionDescription.of(Component.translatable("config.auto-messenger.send_interval.desc")))
                .binding(defaults.sendIntervalMs, () -> config.sendIntervalMs, value -> config.sendIntervalMs = Math.max(1000, value))
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(1000).max(3600000))
                .build());

        scheduledCat.option(createBoolOption(
                Component.translatable("config.auto-messenger.enable_loop"),
                Component.translatable("config.auto-messenger.enable_loop.desc"),
                defaults.enableLoop,
                () -> config.enableLoop,
                value -> config.enableLoop = value
        ));

        scheduledCat.option(createBoolOption(
                Component.translatable("config.auto-messenger.random_send"),
                Component.translatable("config.auto-messenger.random_send.desc"),
                defaults.randomSend,
                () -> config.randomSend,
                value -> config.randomSend = value
        ));

        // 消息统计组
        OptionGroup.Builder statsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("config.auto-messenger.stats_group.name", config.scheduledMessageList.size()));

        if (config.scheduledMessageList.size() > 100) {
            statsGroup.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("config.auto-messenger.show_all_messages"))
                    .description(OptionDescription.of(
                            Component.literal("§c⚠ 警告：大量数据会导致界面卡死！")
                                    .append(Component.literal("\n"))
                                    .append(Component.literal("§7每次重启游戏会自动关闭此选项"))
                    ))
                    .binding(defaults.showAllMessages, () -> config.showAllMessages, value -> config.showAllMessages = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
        }

        String statsKey = (config.scheduledMessageList.size() > 100 && !config.showAllMessages)
                ? "config.auto-messenger.stats.info.limited"
                : "config.auto-messenger.stats.info.all";
        statsGroup.option(LabelOption.create(Component.translatable(statsKey, config.scheduledMessageList.size())));

        statsGroup.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.clear_list"))
                .action((screen, option) -> {
                    config.scheduledMessageList.clear();
                    AutoMessengerConfig.save();
                    Minecraft.getInstance().setScreen(ModConfigScreen.create(screen));
                })
                .build());

        scheduledCat.group(statsGroup.build());

        // ListOption
        int maxEntries = (config.showAllMessages || config.scheduledMessageList.size() <= 100) ? 10000 : 100;

        scheduledCat.group(ListOption.<String>createBuilder()
                .name(Component.translatable("config.auto-messenger.scheduled_list"))
                .description(OptionDescription.of(Component.translatable("config.auto-messenger.scheduled_list.desc")))
                .binding(
                        defaults.scheduledMessageList,
                        () -> {
                            if (config.scheduledMessageList.size() > 100 && !config.showAllMessages) {
                                return config.scheduledMessageList.stream().limit(100).collect(Collectors.toList());
                            }
                            return new ArrayList<>(config.scheduledMessageList);
                        },
                        newVal -> config.scheduledMessageList = new ArrayList<>(newVal)
                )
                .initial(Component.translatable("config.auto-messenger.message_entry.default").getString())
                .controller(StringControllerBuilder::create)
                .minimumNumberOfEntries(0)
                .maximumNumberOfEntries(maxEntries)
                .insertEntriesAtEnd(true)
                .build());

        // 文件操作组
        OptionGroup.Builder fileGroup = OptionGroup.createBuilder()
                .name(Component.translatable("config.auto-messenger.file_operations"))
                .collapsed(false);

        addFileButtons(fileGroup, config);
        scheduledCat.group(fileGroup.build());

        return scheduledCat.build();
    }

    private static Option<Boolean> createBoolOption(Component name, Component desc,
                                                    boolean defaultVal, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(name)
                .description(OptionDescription.of(desc))
                .binding(defaultVal, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static void addFileButtons(OptionGroup.Builder group, AutoMessengerConfig config) {
        group.option(LabelOption.create(
                Component.translatable("config.auto-messenger.import_notice.line1")
                        .append(Component.literal("\n"))
                        .append(Component.translatable("config.auto-messenger.import_notice.line2"))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("config.auto-messenger.import_notice.line3"))
        ));

        // 打开文件夹按钮
        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.open_folder"))
                .action((screen, option) -> {
                    openFolderNative(MessageImporter.getConfigDir());
                })
                .build());

        // 快速导入按钮
        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.quick_import"))
                .description(OptionDescription.of(
                        Component.literal("§7※ 请先将 TXT 文件放入文件夹并命名为 messages.txt")
                ))
                .action((screen, option) -> {
                    Minecraft client = Minecraft.getInstance();
                    // 26.1 改用 sendSystemMessage
                    client.player.sendSystemMessage(
                            Component.translatable("chat.auto-messenger.importing", "messages.txt")
                                    .withStyle(ChatFormatting.YELLOW)
                    );

                    new Thread(() -> {
                        MessageImporter.ImportResult result = MessageImporter.importFromDefault();
                        client.execute(() -> {
                            if (result.success()) {
                                client.player.sendSystemMessage(
                                        Component.translatable("chat.auto-messenger.import.success", result.message())
                                                .withStyle(ChatFormatting.GREEN)
                                );
                                client.setScreen(ModConfigScreen.create(screen));
                            } else {
                                client.player.sendSystemMessage(
                                        Component.translatable("chat.auto-messenger.import.failed", result.message())
                                                .withStyle(ChatFormatting.RED)
                                );
                            }
                        });
                    }, "AutoMessage-Import").start();
                })
                .build());
    }

    private static void openFolderNative(Path dir) {
        try {
            if (!java.nio.file.Files.exists(dir)) {
                java.nio.file.Files.createDirectories(dir);
            }

            String os = System.getProperty("os.name").toLowerCase();
            String path = dir.toAbsolutePath().toString();

            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", path).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", path).start();
            } else {
                new ProcessBuilder("xdg-open", path).start();
            }

            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.translatable("chat.auto-messenger.folder.opened")
                                .withStyle(ChatFormatting.GREEN)
                );
            });

        } catch (Exception e) {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.translatable("chat.auto-messenger.folder.failed")
                                .withStyle(ChatFormatting.RED)
                );
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.literal("§7路径: " + dir.toAbsolutePath())
                );
            });
        }
    }

    private static ConfigCategory buildReplyCategory(AutoMessengerConfig defaults, AutoMessengerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("config.auto-messenger.category.reply"))
                .option(createBoolOption(
                        Component.translatable("config.auto-messenger.enable_autoreply"),
                        Component.translatable("config.auto-messenger.enable_autoreply.desc"),
                        defaults.enableAutoReply,
                        () -> config.enableAutoReply,
                        value -> config.enableAutoReply = value))
                .group(ListOption.<String>createBuilder()
                        .name(Component.translatable("config.auto-messenger.trigger_keywords"))
                        .description(OptionDescription.of(Component.translatable("config.auto-messenger.trigger_keywords.desc")))
                        .binding(defaults.autoReplyTriggers, () -> new ArrayList<>(config.autoReplyTriggers), newVal -> {
                            config.autoReplyTriggers.clear();
                            config.autoReplyTriggers.addAll(newVal);
                        })
                        .initial(Component.translatable("config.auto-messenger.keyword.default").getString())
                        .controller(StringControllerBuilder::create)
                        .build())
                .option(Option.<String>createBuilder()
                        .name(Component.translatable("config.auto-messenger.reply_message"))
                        .description(OptionDescription.of(Component.translatable("config.auto-messenger.reply_message.desc")))
                        .binding(defaults.autoReplyMessage, () -> config.autoReplyMessage, value -> config.autoReplyMessage = value)
                        .controller(StringControllerBuilder::create)
                        .build())
                .option(Option.<Integer>createBuilder()
                        .name(Component.translatable("config.auto-messenger.reply_cooldown"))
                        .description(OptionDescription.of(Component.translatable("config.auto-messenger.reply_cooldown.desc")))
                        .binding(defaults.autoReplyCooldownMs, () -> config.autoReplyCooldownMs, value -> config.autoReplyCooldownMs = Math.max(500, value))
                        .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(500).max(60000))
                        .build())
                .build();
    }

    private static ConfigCategory buildDisplayCategory(AutoMessengerConfig defaults, AutoMessengerConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("config.auto-messenger.category.display"))
                .option(createBoolOption(
                        Component.translatable("config.auto-messenger.show_countdown"),
                        Component.translatable("config.auto-messenger.show_countdown.desc"),
                        defaults.showCountdown,
                        () -> config.showCountdown,
                        value -> config.showCountdown = value))
                .build();
    }
}