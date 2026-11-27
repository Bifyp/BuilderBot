package dev.bifyp.builderbot.bot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BotManager {
    private static final Set<String> RESERVED_NAMES = Set.of("list", "spawn", "remove");
    private final Map<String, BuilderBot> bots = new LinkedHashMap<>();

    public BuilderBot create(String name, ServerCommandSource source) {
        String key = name.toLowerCase(Locale.ROOT);
        if (RESERVED_NAMES.contains(key)) {
            throw new IllegalArgumentException("Bot name '" + name + "' is reserved for a /bot subcommand.");
        }
        if (bots.containsKey(key)) {
            throw new IllegalArgumentException("Bot '" + name + "' already exists. Choose another name or remove it first.");
        }
        UUID ownerUuid = source.getPlayer() != null ? source.getPlayer().getUuid() : new UUID(0L, 0L);
        BuilderBot bot = new BuilderBot(name, ownerUuid, source.getWorld(), BlockPos.ofFloored(source.getPosition()));
        bots.put(key, bot);
        return bot;
    }

    public BuilderBot get(String name) { return bots.get(name.toLowerCase(Locale.ROOT)); }
    public Collection<BuilderBot> all() { return bots.values(); }
    public boolean remove(String name) { return bots.remove(name.toLowerCase(Locale.ROOT)) != null; }
    public void tick(MinecraftServer server) { bots.values().forEach(BuilderBot::tick); }
}
