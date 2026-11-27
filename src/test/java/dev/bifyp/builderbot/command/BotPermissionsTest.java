package dev.bifyp.builderbot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.bifyp.builderbot.BuilderBotMod;
import dev.bifyp.builderbot.bot.BotManager;
import dev.bifyp.builderbot.bot.BotState;
import dev.bifyp.builderbot.bot.BuilderBot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BotPermissionsTest {
    @org.junit.jupiter.api.BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.SharedConstants.createGameVersion();
        net.minecraft.Bootstrap.initialize();
    }

    private static ServerCommandSource player(UUID uuid, boolean operator) {
        ServerCommandSource source = mock(ServerCommandSource.class);
        ServerPlayerEntity player = mock(ServerPlayerEntity.class);
        when(player.getUuid()).thenReturn(uuid);
        when(source.getPlayer()).thenReturn(player);
        when(source.hasPermissionLevel(2)).thenReturn(operator);
        when(source.getPosition()).thenReturn(Vec3d.ZERO);
        return source;
    }

    private static ServerCommandSource nonPlayer(boolean operator) {
        ServerCommandSource source = mock(ServerCommandSource.class);
        when(source.hasPermissionLevel(2)).thenReturn(operator);
        when(source.getPosition()).thenReturn(Vec3d.ZERO);
        return source;
    }

    @Test
    void onlyOperatorsCanCreateOrRemoveEvenThroughManager() throws Exception {
        BotManager manager = new BotManager();
        ServerCommandSource op = player(UUID.randomUUID(), true);
        ServerCommandSource ordinary = player(UUID.randomUUID(), false);
        assertThrows(CommandSyntaxException.class, () -> manager.create("denied", ordinary));
        assertTrue(manager.all().isEmpty());
        BuilderBot bot = manager.create("owned", op);
        when(op.hasPermissionLevel(2)).thenReturn(false);
        assertThrows(CommandSyntaxException.class, () -> manager.remove("owned", op));
        assertThrows(CommandSyntaxException.class, () -> manager.remove("missing", ordinary));
        assertSame(bot, manager.get("owned", op));
        when(op.hasPermissionLevel(2)).thenReturn(true);
        assertTrue(manager.remove("owned", op));
        assertFalse(manager.remove("missing", op));
    }

    @Test
    void namedBotsRequireOwnerOrOperatorAndNeverGrantOwnershipToNonPlayers() throws Exception {
        BotManager manager = new BotManager();
        UUID ownerId = UUID.randomUUID();
        ServerCommandSource owner = player(ownerId, true);
        BuilderBot bot = manager.create("owned", owner);
        when(owner.hasPermissionLevel(2)).thenReturn(false);
        assertSame(bot, manager.get("OWNED", owner));
        assertSame(bot, manager.get("owned", player(ownerId, false)));
        assertSame(bot, manager.get("owned", player(UUID.randomUUID(), true)));
        assertThrows(CommandSyntaxException.class,
                () -> manager.get("owned", player(UUID.randomUUID(), false)));
        assertThrows(CommandSyntaxException.class, () -> manager.get("owned", nonPlayer(false)));
        assertNull(manager.get("missing", owner));
        ServerCommandSource console = nonPlayer(true);
        BuilderBot consoleBot = manager.create("console", console);
        assertSame(consoleBot, manager.get("console", console));
        assertSame(bot, manager.get("owned", console));
        assertThrows(CommandSyntaxException.class, () -> manager.get("console", nonPlayer(false)));
        assertThrows(CommandSyntaxException.class, () -> manager.create("denied", nonPlayer(false)));
        assertThrows(CommandSyntaxException.class, () -> manager.remove("console", nonPlayer(false)));
        assertTrue(manager.remove("owned", console));
        assertTrue(manager.remove("console", console));
    }

    @Test
    void dispatcherProtectsEveryNamedCommandAndKeepsListPublic() throws Exception {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        BotCommands.register(dispatcher);
        ServerCommandSource owner = player(UUID.randomUUID(), true);
        ServerCommandSource stranger = player(UUID.randomUUID(), false);
        ServerCommandSource op = nonPlayer(true);
        String name = "permissionRegression";
        assertEquals(1, dispatcher.execute("bot spawn " + name, owner));
        BuilderBot bot = BuilderBotMod.BOT_MANAGER.get(name, owner);
        try {
            when(owner.hasPermissionLevel(2)).thenReturn(false);
            assertFalse(dispatcher.getRoot().getChild("bot").getChild("spawn").canUse(owner));
            assertFalse(dispatcher.getRoot().getChild("bot").getChild("remove").canUse(owner));
            assertTrue(dispatcher.getRoot().getChild("bot").getChild("spawn").canUse(op));
            assertEquals(1, dispatcher.execute("bot list", stranger));
            assertThrows(CommandSyntaxException.class,
                    () -> dispatcher.execute("bot spawn forbidden", stranger));
            assertThrows(CommandSyntaxException.class,
                    () -> dispatcher.execute("bot remove " + name, owner));
            assertEquals(1, dispatcher.execute("bot " + name + " wait 10", owner));
            bot.pause();
            String originalStatus = bot.statusText().getString();
            for (String command : new String[]{"status", "stop", "pause", "resume", "wait 1"}) {
                assertThrows(CommandSyntaxException.class,
                        () -> dispatcher.execute("bot " + name + " " + command, stranger), command);
                assertEquals(BotState.PAUSED, bot.state(), command + " must not change state");
                assertEquals(originalStatus, bot.statusText().getString(), command + " must not change queue");
            }
            for (ServerCommandSource authorized : new ServerCommandSource[]{owner, op}) {
                for (String command : new String[]{"status", "resume", "pause", "wait 1", "stop"}) {
                    assertEquals(1, dispatcher.execute("bot " + name + " " + command, authorized));
                }
            }
            assertEquals(0, dispatcher.execute("bot missing status", owner));
            assertEquals(1, dispatcher.execute("bot remove " + name, op));
        } finally {
            BuilderBotMod.BOT_MANAGER.remove(name, op);
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"status", "stop", "pause", "resume", "wait 1"})
    void unrelatedPlayerCannotExecuteNamedCommand(String command) throws Exception {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        BotCommands.register(dispatcher);
        ServerCommandSource owner = player(UUID.randomUUID(), true);
        ServerCommandSource stranger = player(UUID.randomUUID(), false);
        String name = "isolatedPermissionCheck";
        dispatcher.execute("bot spawn " + name, owner);
        BuilderBot bot = BuilderBotMod.BOT_MANAGER.get(name, owner);
        bot.pause();
        String originalStatus = bot.statusText().getString();
        try {
            assertThrows(CommandSyntaxException.class,
                    () -> dispatcher.execute("bot " + name + " " + command, stranger));
            assertEquals(originalStatus, bot.statusText().getString());
        } finally {
            BuilderBotMod.BOT_MANAGER.remove(name, owner);
        }
    }
}
