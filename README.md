# BuilderBot

Server-side Fabric mod prototype for a Minecraft builder bot.

## Vision

BuilderBot is planned as a fake-player-style autonomous worker controlled by commands and, later, an AI planner.

Target features:

- fake player bot;
- task queue;
- movement/pathfinding;
- building;
- clearing/terraforming;
- resource gathering;
- storage/inventory management;
- safe mode;
- natural-language AI planner.

## Alpha 0.1.0

This alpha tests the command system, bot registry, state machine, server tick loop, and task queue.

Available commands:

```mcfunction
/bot list
/bot spawn <name>
/bot remove <name>
/bot <name> status
/bot <name> stop
/bot <name> pause
/bot <name> resume
/bot <name> wait <seconds>
```

See `ALPHA_TESTING.md` for test steps.

## Roadmap

1. Bot Core
2. Real fake player implementation
3. Movement/pathfinding
4. Break/place/clear tasks
5. Inventory/storage
6. JSON building schemes
7. Resource gathering
8. AI planner
