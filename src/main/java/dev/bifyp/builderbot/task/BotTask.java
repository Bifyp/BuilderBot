package dev.bifyp.builderbot.task;

import dev.bifyp.builderbot.bot.BuilderBot;

public interface BotTask {
    String name();
    TaskResult tick(BuilderBot bot);
}
