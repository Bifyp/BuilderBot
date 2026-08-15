package dev.bifyp.builderbot.bot;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BotManager {
    private final Map<String, BuilderBot> bots = new LinkedHashMap<>();

    public BuilderBot create(String name, ServerCommandSource source) {
        UUID ownerUuid = source.getPlayer() != null ? source.getPlayer().getUuid() : new UUID(0L, 0L);
        BuilderBot bot = new BuilderBot(name, ownerUuid, source.getWorld(), BlockPos.ofFloored(source.getPosition()));
        bots.put(name.toLowerCase(), bot);
        return bot;
    }

    public BuilderBot get(String name) { return bots.get(name.toLowerCase()); }
    public Collection<BuilderBot> all() { return bots.values(); }
    public boolean remove(String name) { return bots.remove(name.toLowerCase()) != null; }
    public void tick(MinecraftServer server) { bots.values().forEach(BuilderBot::tick); }
}
