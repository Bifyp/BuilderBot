# BuilderBot Alpha Test

## Что есть в этой альфе

Это тест ядра мода, не физического fake-player.

Команды:

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

`wait` добавляет тестовую задачу в очередь, чтобы проверить tick-loop, состояния и очередь задач.

## Сборка

Нужна Java 21.

```bash
gradle build
```

Если есть Gradle wrapper:

```bash
./gradlew build
```

Jar будет в:

```text
build/libs/builderbot-0.1.0.jar
```

Не используй `-sources.jar`.

## Установка

1. Установи Fabric Loader для Minecraft 1.21.8.
2. Скачай Fabric API под 1.21.8.
3. Положи Fabric API и `builderbot-0.1.0.jar` в папку `mods`.
4. Запусти мир с включёнными командами или сервер.

## Быстрый тест

```mcfunction
/bot list
/bot spawn Builder
/bot list
/bot Builder status
/bot Builder wait 10
/bot Builder status
/bot Builder pause
/bot Builder status
/bot Builder resume
/bot Builder stop
/bot remove Builder
```

Ожидаемо:

- `/bot spawn Builder` создаёт логического бота;
- `/bot Builder wait 10` добавляет задачу ожидания;
- `/bot Builder status` показывает состояние и очередь;
- `/bot Builder stop` очищает очередь;
- `/bot remove Builder` удаляет бота.

## Ограничения Alpha

- бот пока не появляется телом в мире;
- нет движения;
- нет ломания/постановки блоков;
- нет pathfinding;
- нет ИИ.

Следующий этап: настоящий fake player + `/bot <name> goto`.
