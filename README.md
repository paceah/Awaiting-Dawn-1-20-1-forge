# Awaiting Dawn

[English](#english) | [Русский](#русский)

## English

Awaiting Dawn is a Minecraft Forge mod that ties resurrection to a shared world calendar. When a player dies on a mortal day, they wait in Spectator mode until the next resurrection day. An optional lives system gives players more chances before they have to wait.

### Requirements and installation

- Minecraft **1.20.1** and Forge **47.x**
- Java **17**
- Install the same mod JAR in the `mods` folder on each client and on the dedicated server. For single-player, install it on the client.

### How it works

- The calendar follows the Overworld's time. Day zero is mortal; by default, days 10, 20, 30 and so on are resurrection days.
- On a resurrection day, waiting players return and normal respawns do not consume lives. Mortality returns the following day.
- With lives disabled, the first death in Survival or Adventure on a mortal day starts the wait. With lives enabled, the default limit is three deaths.
- Players who are offline when a resurrection day arrives return on their next login. Death state and progress are saved with the world.
- Resurrection can use the player's valid bed or charged respawn anchor, with a safe world spawn as fallback, or use the world spawn directly. Drops, experience and inventory follow vanilla rules.
- Vanilla Spectator travel remains available while waiting.

The interval, lives, resurrection location, messages, colors and sounds can be configured. Messages support English, Russian, Chinese, Spanish, Portuguese (Brazil), German, Japanese, French, Polish and Korean.

### Configuration

Forge creates `serverconfig/awaitingdawn-server.toml` inside the world folder. In single-player, the path is `saves/<world>/serverconfig/awaitingdawn-server.toml`. The file contains comments explaining each option.

The default interval is 10 days, limited lives are disabled, and the resurrection location uses the player's most recent valid spawn. If no safe destination is available, the mod keeps the player waiting and retries.

### Commands

All commands require permission level 2. Debug commands also require `development.enableDebugCommands = true` in the server config.

```text
/awaitingdawn status [player]
/awaitingdawn debug setday <day>
/awaitingdawn debug setlives <player> <count>
```

### Build from source

Install JDK 17 and use the included Gradle wrapper:

```bash
./gradlew build
```

On Windows, run `gradlew.bat build`. The mod JAR is written to `build/libs/`. For development, use `gradlew.bat runClient` or `gradlew.bat runServer` on Windows; the equivalent Unix commands use `./gradlew`.

## Русский

Awaiting Dawn привязывает воскрешение игроков к общему календарю мира Minecraft. После смерти в смертный день игрок ждёт в режиме наблюдателя до следующего дня воскрешения. При желании можно включить систему жизней, чтобы дать игрокам несколько попыток до ожидания.

### Требования и установка

- Minecraft **1.20.1** и Forge **47.x**
- Java **17**
- Поместите один и тот же JAR мода в папку `mods` каждого клиента и выделенного сервера. Для одиночной игры установите мод на клиент.

### Как это работает

- Календарь следует времени Верхнего мира. Нулевой день смертный; по умолчанию дни 10, 20, 30 и далее являются днями воскрешения.
- В день воскрешения ожидающие игроки возвращаются, а обычное возрождение не расходует жизни. На следующий день смертность возвращается.
- Если жизни отключены, первая смерть в режиме выживания или приключения в смертный день отправляет игрока в ожидание. При включённых жизнях по умолчанию доступны три смерти.
- Если игрок не в сети в день воскрешения, он вернётся при следующем входе. Состояние смерти и прогресс сохраняются вместе с миром.
- Место воскрешения можно выбрать: последняя действующая кровать или заряженный якорь возрождения с запасным безопасным спавном мира либо сразу спавн мира. Выпадение вещей, опыт и инвентарь работают по правилам Minecraft.
- Во время ожидания доступны обычные перемещения наблюдателя.

Интервал, жизни, место воскрешения, сообщения, цвета и звуки настраиваются. Сообщения доступны на английском, русском, китайском, испанском, бразильском португальском, немецком, японском, французском, польском и корейском языках.

### Настройка

Forge создаёт `serverconfig/awaitingdawn-server.toml` в папке мира. В одиночной игре путь выглядит так: `saves/<world>/serverconfig/awaitingdawn-server.toml`. В файле есть комментарии к каждой настройке.

По умолчанию интервал составляет 10 дней, ограничение жизней выключено, а местом воскрешения служит последняя действующая точка возрождения игрока. Если безопасное место недоступно, игрок останется в ожидании, а мод повторит попытку.

### Команды

Для всех команд нужен уровень прав 2. Для отладочных команд также требуется включить `development.enableDebugCommands = true` в конфигурации сервера.

```text
/awaitingdawn status [player]
/awaitingdawn debug setday <day>
/awaitingdawn debug setlives <player> <count>
```

### Сборка из исходников

Установите JDK 17 и используйте Gradle Wrapper из проекта:

```bash
./gradlew build
```

В Windows выполните `gradlew.bat build`. Готовый JAR появится в `build/libs/`. Для разработки в Windows используйте `gradlew.bat runClient` или `gradlew.bat runServer`; в Unix используйте те же команды с `./gradlew`.

## License / Лицензия

[MIT](LICENSE)
