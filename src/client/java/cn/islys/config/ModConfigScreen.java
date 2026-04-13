package cn.islys.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.create(AutoMessengerConfig.HANDLER,
                        (defaults, config, builder) -> builder
                                .title(Component.translatable("config.auto-messenger.title"))

                                // === 定时发送设置 ===
                                .category(ConfigCategory.createBuilder()
                                        .name(Component.translatable("config.auto-messenger.category.scheduled"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.enable_scheduled"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.enable_scheduled.desc")))
                                                .binding(
                                                        defaults.enableScheduledMessages,
                                                        () -> config.enableScheduledMessages,
                                                        value -> config.enableScheduledMessages = value
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.send_on_join"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.send_on_join.desc")))
                                                .binding(
                                                        defaults.sendOnJoin,
                                                        () -> config.sendOnJoin,
                                                        value -> config.sendOnJoin = value
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        // 发送间隔（毫秒）
                                        .option(Option.<Integer>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.send_interval"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.send_interval.desc")))
                                                .binding(
                                                        defaults.sendIntervalMs,
                                                        () -> config.sendIntervalMs,
                                                        value -> config.sendIntervalMs = Math.max(1000, value) // 最小1000ms
                                                )
                                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                                        .min(1000)
                                                        .max(3600000)) // 最大1小时
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.enable_loop"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.enable_loop.desc")))
                                                .binding(
                                                        defaults.enableLoop,
                                                        () -> config.enableLoop,
                                                        value -> config.enableLoop = value
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        // 定时发送列表
                                        .group(ListOption.<String>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.scheduled_list"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.scheduled_list.desc")))
                                                .binding(
                                                        defaults.scheduledMessageList,
                                                        () -> new ArrayList<>(config.scheduledMessageList),
                                                        newVal -> {
                                                            config.scheduledMessageList.clear();
                                                            config.scheduledMessageList.addAll(newVal);
                                                        }
                                                )
                                                .initial("新消息|msg")
                                                .controller(StringControllerBuilder::create)
                                                .minimumNumberOfEntries(1)
                                                .insertEntriesAtEnd(true)
                                                .build())
                                        .build())

                                // === 自动回复设置 ===
                                .category(ConfigCategory.createBuilder()
                                        .name(Component.translatable("config.auto-messenger.category.reply"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.enable_autoreply"))
                                                .binding(
                                                        defaults.enableAutoReply,
                                                        () -> config.enableAutoReply,
                                                        value -> config.enableAutoReply = value
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .build())
                                        // 触发关键词列表
                                        .group(ListOption.<String>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.trigger_keywords"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.trigger_keywords.desc")))
                                                .binding(
                                                        defaults.autoReplyTriggers,
                                                        () -> new ArrayList<>(config.autoReplyTriggers),
                                                        newVal -> {
                                                            config.autoReplyTriggers.clear();
                                                            config.autoReplyTriggers.addAll(newVal);
                                                        }
                                                )
                                                .initial("关键词")
                                                .controller(StringControllerBuilder::create)
                                                .minimumNumberOfEntries(1)
                                                .insertEntriesAtEnd(true)
                                                .build())
                                        .option(Option.<String>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.reply_message"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.reply_message.desc")))
                                                .binding(
                                                        defaults.autoReplyMessage,
                                                        () -> config.autoReplyMessage,
                                                        value -> config.autoReplyMessage = value
                                                )
                                                .controller(StringControllerBuilder::create)
                                                .build())
                                        // 冷却时间（毫秒）
                                        .option(Option.<Integer>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.reply_cooldown"))
                                                .description(OptionDescription.of(Component.translatable("config.auto-messenger.reply_cooldown.desc")))
                                                .binding(
                                                        defaults.autoReplyCooldownMs,
                                                        () -> config.autoReplyCooldownMs,
                                                        value -> config.autoReplyCooldownMs = Math.max(500, value)
                                                )
                                                .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                                        .min(500)
                                                        .max(60000))
                                                .build())
                                        .build())

                                // === 显示设置 ===
                                .category(ConfigCategory.createBuilder()
                                        .name(Component.translatable("config.auto-messenger.category.display"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Component.translatable("config.auto-messenger.show_countdown"))
                                                .binding(
                                                        defaults.showCountdown,
                                                        () -> config.showCountdown,
                                                        value -> config.showCountdown = value
                                                )
                                                .controller(TickBoxControllerBuilder::create)
                                                .flag(OptionFlag.GAME_RESTART)
                                                .build())
                                        .build())
                )
                .generateScreen(parent);
    }
}