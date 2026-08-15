package dev.bifyp.builderbot.task;

import dev.bifyp.builderbot.bot.BotState;
import dev.bifyp.builderbot.bot.BuilderBot;

public class WaitTask implements BotTask {
    private int ticksLeft;

    public WaitTask(int ticks) { this.ticksLeft = Math.max(1, ticks); }

    @Override public String name() { return "Wait " + ticksLeft + " ticks"; }

    @Override public TaskResult tick(BuilderBot bot) {
        bot.state(BotState.WAITING);
        ticksLeft--;
        return ticksLeft <= 0 ? TaskResult.DONE : TaskResult.RUNNING;
    }
}
