package cn.islys.config;

import cn.islys.RSsAutoMessageClient;
import cn.islys.gui.FileDialogUtil;
import cn.islys.util.FileChooserUtil;
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
import java.util.Optional;
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
                    // 如果禁用，立即停止
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

        // 随机发送顺序 - 使用翻译键
        scheduledCat.option(Option.<Boolean>createBuilder()
                .name(Component.translatable("config.auto-messenger.random_send"))
                .description(OptionDescription.of(Component.translatable("config.auto-messenger.random_send.desc")))
                .binding(defaults.randomSend, () -> config.randomSend, value -> config.randomSend = value)
                .controller(TickBoxControllerBuilder::create)
                .build());

        // 消息统计组 - 使用翻译键
        OptionGroup.Builder statsGroup = OptionGroup.createBuilder()
                .name(Component.translatable("config.auto-messenger.stats_group.name", config.scheduledMessageList.size()));

        if (config.scheduledMessageList.size() > 100) {
            statsGroup.option(Option.<Boolean>createBuilder()
                    .name(Component.translatable("config.auto-messenger.show_all_messages"))
                    .description(OptionDescription.of(Component.translatable("config.auto-messenger.show_all_messages.desc", config.scheduledMessageList.size())))
                    .binding(defaults.showAllMessages, () -> config.showAllMessages, value -> config.showAllMessages = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build());
        }

        // 统计信息文本 - 使用翻译键
        String statsKey = (config.scheduledMessageList.size() > 100 && !config.showAllMessages)
                ? "config.auto-messenger.stats.info.limited"
                : "config.auto-messenger.stats.info.all";
        statsGroup.option(LabelOption.create(Component.translatable(statsKey, config.scheduledMessageList.size())));

        // 清空列表按钮 - 使用翻译键
        statsGroup.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.clear_list"))
                .action((screen, option) -> {
                    config.scheduledMessageList.clear();
                    AutoMessengerConfig.save();
                    Minecraft.getInstance().setScreen(ModConfigScreen.create(null));
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

        // 文件操作组 - 使用翻译键
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
        // 浏览选择文件按钮 - 使用翻译键
        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.browse_file"))
                .action((screen, option) -> {
                    Minecraft client = Minecraft.getInstance();
                    client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.opening_dialog").withStyle(ChatFormatting.YELLOW));

                    new Thread(() -> {
                        Optional<Path> selected = FileChooserUtil.openTxtFileChooser();
                        client.execute(() -> {
                            selected.ifPresentOrElse(path -> {
                                client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.importing", path.getFileName()).withStyle(ChatFormatting.YELLOW));
                                new Thread(() -> {
                                    MessageImporter.ImportResult result = MessageImporter.importFromFile(path);
                                    client.execute(() -> {
                                        if (result.success()) {
                                            client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.import.success", result.message()).withStyle(ChatFormatting.GREEN));
                                            client.setScreen(ModConfigScreen.create(null));
                                        } else {
                                            client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.import.failed", result.message()).withStyle(ChatFormatting.RED));
                                        }
                                    });
                                }, "AutoMessage-Import").start();
                            }, () -> client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.no_file_selected").withStyle(ChatFormatting.GRAY)));
                        });
                    }, "FileDialog").start();
                })
                .build());

        // 快速导入按钮 - 使用翻译键
        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.quick_import"))
                .action((screen, option) -> {
                    Minecraft client = Minecraft.getInstance();
                    new Thread(() -> {
                        MessageImporter.ImportResult result = MessageImporter.importFromDefault();
                        client.execute(() -> {
                            if (result.success()) {
                                client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.import.success", result.message()).withStyle(ChatFormatting.GREEN));
                                client.setScreen(ModConfigScreen.create(null));
                            } else {
                                client.gui.getChat().addMessage(Component.translatable("chat.auto-messenger.import.failed", result.message()).withStyle(ChatFormatting.RED));
                            }
                        });
                    }, "AutoMessage-Import").start();
                })
                .build());

        // 打开配置文件夹按钮 - 使用翻译键
        group.option(ButtonOption.createBuilder()
                .name(Component.translatable("config.auto-messenger.open_folder"))
                .action((screen, option) -> {
                    try {
                        java.nio.file.Path dir = MessageImporter.getConfigDir();
                        if (!java.nio.file.Files.exists(dir)) java.nio.file.Files.createDirectories(dir);

                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) Runtime.getRuntime().exec("explorer.exe \"" + dir + "\"");
                        else if (os.contains("mac")) Runtime.getRuntime().exec("open \"" + dir + "\"");
                        else Runtime.getRuntime().exec("xdg-open \"" + dir + "\"");

                        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("chat.auto-messenger.folder.opened").withStyle(ChatFormatting.GREEN));
                    } catch (Exception e) {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.translatable("chat.auto-messenger.folder.failed").withStyle(ChatFormatting.RED));
                    }
                })
                .build());
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
                            config.autoReplyTriggers.clear(); config.autoReplyTriggers.addAll(newVal);
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
                .option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("config.auto-messenger.show_countdown"))
                        .description(OptionDescription.of(Component.translatable("config.auto-messenger.show_countdown.desc")))
                        .binding(defaults.showCountdown, () -> config.showCountdown, value -> config.showCountdown = value)
                        .controller(TickBoxControllerBuilder::create)
                        .flag(OptionFlag.GAME_RESTART)
                        .build())
                .build();
    }
}