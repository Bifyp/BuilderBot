package dev.bifyp.builderbot.bot;

import com.mojang.brigadier.CommandDispatcher;
import dev.bifyp.builderbot.BuilderBotMod;
import dev.bifyp.builderbot.command.BotCommands;
import dev.bifyp.builderbot.task.WaitTask;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

class BotNameTest {
    private final Locale originalLocale = Locale.getDefault();
    private ServerCommandSource source;

    @BeforeEach
    void setUp() {
        source = mock(ServerCommandSource.class);
        when(source.getPosition()).thenReturn(Vec3d.ZERO);
        BuilderBotMod.BOT_MANAGER.all().clear();
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(originalLocale);
        BuilderBotMod.BOT_MANAGER.all().clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Builder", "builder", "BUILDER"})
    void duplicateDoesNotReplaceExistingBotOrDropItsQueue(String duplicateName) {
        BotManager manager = new BotManager();
        BuilderBot original = manager.create("Builder", source);
        original.enqueue(new WaitTask(20));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> manager.create(duplicateName, source));
        assertTrue(error.getMessage().contains("already exists"));
        assertSame(original, manager.get("builder"));
        assertEquals(1, manager.all().size());
        manager.tick(null);
        assertEquals(BotState.WAITING, original.state(), "Queued wait must still run");
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "spawn", "remove", "LIST", "Spawn", "ReMoVe"})
    void reservedNamesAreRejectedBeforeCreatingABot(String name) {
        BotManager manager = new BotManager();
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> manager.create(name, source));
        assertTrue(error.getMessage().contains("reserved"));
        assertTrue(manager.all().isEmpty());
        verify(source, never()).getPosition();
    }

    @Test
    void namesRemainCaseInsensitiveAcrossDefaultLocaleChanges() {
        BotManager manager = new BotManager();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        BuilderBot original = manager.create("MINER", source);
        assertSame(original, manager.get("miner"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("miner", source));
        assertThrows(IllegalArgumentException.class, () -> manager.create("LIST", source));
        Locale.setDefault(Locale.US);
        assertSame(original, manager.get("MINER"));
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        assertTrue(manager.remove("MINER"));
        assertTrue(manager.all().isEmpty());
    }

    @Test
    void ordinaryNamesStillCreateAndCanBeReusedAfterRemoval() {
        BotManager manager = new BotManager();
        BuilderBot first = manager.create("spawn-helper", source);
        assertEquals("spawn-helper", first.name());
        assertSame(first, manager.get("SPAWN-HELPER"));
        assertTrue(manager.remove("SPAWN-HELPER"));
        assertNotSame(first, manager.create("spawn-helper", source));
    }

    @Test
    void spawnDuplicateReturnsFailureWithClearErrorAndNoSuccessFeedback() throws Exception {
        BotManager manager = BuilderBotMod.BOT_MANAGER;
        BuilderBot original = manager.create("Builder", source);
        original.enqueue(new WaitTask(20));
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        BotCommands.register(dispatcher);

        assertEquals(0, dispatcher.execute("bot spawn builder", source));
        verify(source).sendError(argThat(text -> text.getString().contains("already exists")));
        verify(source, never()).sendFeedback(any(), anyBoolean());
        assertSame(original, manager.get("Builder"));
        manager.tick(null);
        assertEquals(BotState.WAITING, original.state());
    }

    @Test
    void spawnOrdinaryNameReturnsSuccess() throws Exception {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        BotCommands.register(dispatcher);
        assertEquals(1, dispatcher.execute("bot spawn Miner", source));
        assertEquals("Miner", BuilderBotMod.BOT_MANAGER.get("miner").name());
        verify(source).sendFeedback(any(), anyBoolean());
        verify(source, never()).sendError(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "spawn", "remove", "LIST", "Spawn", "ReMoVe"})
    void spawnReservedNameReturnsFailureWithClearError(String name) throws Exception {
        CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
        BotCommands.register(dispatcher);
        assertEquals(0, dispatcher.execute("bot spawn " + name, source));
        verify(source).sendError(argThat(text -> text.getString().contains("reserved")));
        verify(source, never()).sendFeedback(any(), anyBoolean());
        assertTrue(BuilderBotMod.BOT_MANAGER.all().isEmpty());
    }
}
