package dev.bifyp.builderbot.bot;

import dev.bifyp.builderbot.task.BotTask;
import dev.bifyp.builderbot.task.TaskResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

public class BuilderBot {
    private final String name;
    private final UUID ownerUuid;
    private final ServerWorld world;
    private final BlockPos home;
    private final Queue<BotTask> queue = new ArrayDeque<>();

    private BotState state = BotState.IDLE;
    private String lastError;

    // TODO Alpha 1.1: replace controller-only bot with real fake ServerPlayerEntity.
    private ServerPlayerEntity fakePlayer;

    public BuilderBot(String name, UUID ownerUuid, ServerWorld world, BlockPos home) {
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.world = world;
        this.home = home.toImmutable();
    }

    public void tick() {
        if (state == BotState.PAUSED || state == BotState.ERROR) return;
        BotTask task = queue.peek();
        if (task == null) {
            state = BotState.IDLE;
            return;
        }
        TaskResult result = task.tick(this);
        // A task may report a specific error via fail() before returning.
        if (state == BotState.ERROR) return;
        if (result == TaskResult.DONE) queue.poll();
        if (result == TaskResult.FAILED) fail("Task failed: " + task.name());
    }

    public void enqueue(BotTask task) { queue.add(task); }
    public void stop() { queue.clear(); state = BotState.IDLE; lastError = null; }
    public void pause() { if (state != BotState.ERROR) state = BotState.PAUSED; }
    public void resume() {
        if (state == BotState.PAUSED || state == BotState.ERROR) state = BotState.IDLE;
    }

    /** Discard the failed task once; resume() continues with the remaining queue. */
    public void fail(String message) {
        if (state != BotState.ERROR) queue.poll();
        lastError = message;
        state = BotState.ERROR;
    }

    public Text statusText() {
        BotTask task = queue.peek();
        return Text.literal("Bot " + name + " | state=" + state + " | task=" + (task == null ? "none" : task.name()) + " | queued=" + queue.size() + (lastError == null ? "" : " | error=" + lastError));
    }

    public String name() { return name; }
    public UUID ownerUuid() { return ownerUuid; }
    public ServerWorld world() { return world; }
    public BlockPos home() { return home; }
    public BotState state() { return state; }
    public void state(BotState state) { this.state = state; }
    public ServerPlayerEntity fakePlayer() { return fakePlayer; }
}
