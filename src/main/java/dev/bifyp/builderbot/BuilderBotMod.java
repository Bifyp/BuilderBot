package dev.bifyp.builderbot;

import dev.bifyp.builderbot.bot.BotManager;
import dev.bifyp.builderbot.command.BotCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class BuilderBotMod implements ModInitializer {
    public static final String MOD_ID = "builderbot";
    public static final BotManager BOT_MANAGER = new BotManager();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> BotCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> BOT_MANAGER.tick(server));
    }
}
