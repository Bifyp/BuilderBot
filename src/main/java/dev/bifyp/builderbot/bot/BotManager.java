package dev.bifyp.builderbot.bot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BotManager {
    private static final SimpleCommandExceptionType OPERATOR_REQUIRED = new SimpleCommandExceptionType(
        Text.literal("Operator permission level 2 is required to spawn or remove bots."));
    private static final SimpleCommandExceptionType ACCESS_DENIED = new SimpleCommandExceptionType(
        Text.literal("Only the bot owner or an operator can control this bot."));

    private final Map<String, BuilderBot> bots = new LinkedHashMap<>();

    public BuilderBot create(String name, ServerCommandSource source) throws CommandSyntaxException {
        requireOperator(source);
        UUID ownerUuid = source.getPlayer() != null ? source.getPlayer().getUuid() : new UUID(0L, 0L);
        BuilderBot bot = new BuilderBot(name, ownerUuid, source.getWorld(), BlockPos.ofFloored(source.getPosition()));
        bots.put(name.toLowerCase(), bot);
        return bot;
    }

    public BuilderBot get(String name, ServerCommandSource source) throws CommandSyntaxException {
        BuilderBot bot = bots.get(name.toLowerCase());
        if (bot != null && !isOperator(source)
                && (source.getPlayer() == null || !bot.ownerUuid().equals(source.getPlayer().getUuid()))) {
            throw ACCESS_DENIED.create();
        }
        return bot;
    }
    public Collection<BuilderBot> all() { return bots.values(); }
    public boolean remove(String name, ServerCommandSource source) throws CommandSyntaxException {
        requireOperator(source);
        return bots.remove(name.toLowerCase()) != null;
    }

    public static boolean isOperator(ServerCommandSource source) {
        return source.hasPermissionLevel(2);
    }

    private static void requireOperator(ServerCommandSource source) throws CommandSyntaxException {
        if (!isOperator(source)) throw OPERATOR_REQUIRED.create();
    }
    public void tick(MinecraftServer server) { bots.values().forEach(BuilderBot::tick); }
}
