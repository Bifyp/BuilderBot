package dev.bifyp.builderbot.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.bifyp.builderbot.BuilderBotMod;
import dev.bifyp.builderbot.bot.BuilderBot;
import net.minecraft.server.command.ServerCommandSource;
import dev.bifyp.builderbot.task.WaitTask;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BotCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bot")
            .then(literal("spawn").then(argument("name", word()).executes(ctx -> {
                String name = getString(ctx, "name");
                BuilderBot bot = BuilderBotMod.BOT_MANAGER.create(name, ctx.getSource());
                ctx.getSource().sendFeedback(() -> Text.literal("BuilderBot created: " + bot.name()), false);
                return 1;
            })))
            .then(literal("list").executes(ctx -> {
                String names = BuilderBotMod.BOT_MANAGER.all().stream().map(BuilderBot::name).reduce((a, b) -> a + ", " + b).orElse("none");
                ctx.getSource().sendFeedback(() -> Text.literal("BuilderBots: " + names), false);
                return 1;
            }))
            .then(literal("remove").then(argument("name", word()).executes(ctx -> {
                String name = getString(ctx, "name");
                boolean removed = BuilderBotMod.BOT_MANAGER.remove(name);
                ctx.getSource().sendFeedback(() -> Text.literal(removed ? "Removed bot: " + name : "Bot not found: " + name), false);
                return removed ? 1 : 0;
            })))
            .then(argument("name", word())
                .then(literal("status").executes(ctx -> withBot(ctx.getSource(), getString(ctx, "name"), bot -> ctx.getSource().sendFeedback(bot::statusText, false))))
                .then(literal("stop").executes(ctx -> withBot(ctx.getSource(), getString(ctx, "name"), bot -> { bot.stop(); ctx.getSource().sendFeedback(() -> Text.literal("Stopped bot: " + bot.name()), false); })))
                .then(literal("pause").executes(ctx -> withBot(ctx.getSource(), getString(ctx, "name"), bot -> { bot.pause(); ctx.getSource().sendFeedback(() -> Text.literal("Paused bot: " + bot.name()), false); })))
                .then(literal("resume").executes(ctx -> withBot(ctx.getSource(), getString(ctx, "name"), bot -> { bot.resume(); ctx.getSource().sendFeedback(() -> Text.literal("Resumed bot: " + bot.name()), false); })))
                .then(literal("wait").then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<ServerCommandSource, Integer>argument("seconds", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 3600)).executes(ctx -> withBot(ctx.getSource(), getString(ctx, "name"), bot -> { int seconds = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "seconds"); bot.enqueue(new WaitTask(seconds * 20)); ctx.getSource().sendFeedback(() -> Text.literal("Queued wait for " + bot.name() + ": " + seconds + "s"), false); }))))
            )
        );
    }

    private static int withBot(ServerCommandSource source, String name, BotAction action) {
        BuilderBot bot = BuilderBotMod.BOT_MANAGER.get(name);
        if (bot == null) {
            source.sendError(Text.literal("Bot not found: " + name));
            return 0;
        }
        action.run(bot);
        return 1;
    }

    private interface BotAction { void run(BuilderBot bot); }
}
