![LiveAnnounce](https://i.imgur.com/vnkVt9T.jpeg)

# LiveAnnounce

LiveAnnounce is a Bukkit and Spigot plugin that posts a player's live stream to the server with one command. It reads the URL, detects the platform, and sends the announcement to chat.

Made by ccoding.

![Documentation](https://i.imgur.com/SUwTaxv.jpeg)

## Requirements

- Paper or Spigot 1.16 or newer
- Java 8 or newer
- PlaceholderAPI is optional

## Install

1. Download the jar from [Spigot](https://www.spigotmc.org/resources/liveannouncer-stream-alert-system-%E3%80%8C1-8-1-21%E3%80%8D.130601/) or [Modrinth](https://modrinth.com/plugin/liveannouncer).
2. Put it in the server `plugins` folder.
3. Start the server. The first start writes `plugins/LiveAnnounce/config.yml`.
4. Edit that file, then run `/la reload`.

An existing `config.yml` is not overwritten. New keys use the built-in defaults until you add them.

## Announce a stream

Players with `liveannounce.directo` can run:

```
/directo https://twitch.tv/ccoding
/live https://youtube.com/live/ccoding
/stream https://kick.com/ccoding
/livestream https://tiktok.com/@ccoding/live
```

`/live`, `/stream`, and `/livestream` are aliases of `/directo`. The URL must start with `http://` or `https://`. Supported platforms are Twitch, YouTube, Kick, and TikTok.

With no arguments, `/directo` prints command help.

Each successful announcement can send:

- a clickable chat message
- a title and subtitle
- a boss bar
- a sound for players who have not hidden announcements
- a Discord webhook, when a real webhook URL is set

`/la live` lists streams that are still inside the configured time window. The list is clickable. It is not hidden by `/la toggle`.

## Commands

| Command | What it does | Permission |
| --- | --- | --- |
| `/directo <url>` | Announce a stream | `liveannounce.directo` |
| `/la live` | Show who is currently listed as live | `liveannounce.live` |
| `/la toggle` | Hide or show announcements for yourself | `liveannounce.toggle` |
| `/la help` | Show command help | none |
| `/la version` | Show the plugin version | none |
| `/la reload` | Reload `config.yml` | `liveannounce.admin` |
| `/la status` | Show plugin, display, holiday, and mute state | `liveannounce.admin` |
| `/la enable` | Set `enabled` to true and save | `liveannounce.admin` |
| `/la disable` | Set `enabled` to false and save | `liveannounce.admin` |

`/la` suggests the subcommands you are allowed to use. `/directo` does not suggest player names.

`liveannounce.live` and `liveannounce.toggle` default to everyone. `liveannounce.directo` and `liveannounce.admin` default to operators.

`liveannounce.directo.bypass.cooldown` skips the wait between announcements.

## Configuration

All of this lives in `plugins/LiveAnnounce/config.yml`.

Do not mix MiniMessage (`<red>`, `<gradient>`) and legacy codes (`&c`) on the same line. `{color}` is a legacy code. Use it only on a legacy line.

`player-format` is the name printed for `{player}`. The default is `%player_name%`. PlaceholderAPI placeholders work here when that plugin is installed.

### Chat format

`announcement-formats.default` sets the border, title, description, link, and hover text. Placeholders: `{player}`, `{platform}`, `{platform_upper}`, `{channel}`, `{link}`, `{color}`.

The link line opens the stream on click.

### Title and boss bar

`display.title` and `display.bossbar` can be turned off separately.

Title times are ticks. 20 ticks is one second. Fade is capped at 40 ticks and stay at 200. Boss bar duration is capped at 30 seconds. The bar is sent once and removed by a single delayed task.

Boss bar colors: `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE`.

Styles: `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, `SEGMENTED_20`.

### Active streams

`live-sessions` controls `/la live`.

- `duration-seconds`: how long a stream stays listed. `0` means no timer.
- `remove-on-quit`: remove the row when that player leaves this server.
- If both would keep rows forever, the plugin uses a 6 hour cap.
- `max-display` limits how many rows are printed. The maximum is 50.

### Hide announcements

`/la toggle` hides the chat lines, the title, the boss bar, and the announcement sound for that player. The choice is stored in `muted-players.txt`.

### Holiday line

`christmas` adds one line to the announcement and can recolor the boss bar.

- `mode: auto` is active from `from` through `to`. The default range is December 1 through December 26.
- `mode: on` forces it.
- `mode: off` hides it.
- `bossbar-color: none` keeps the normal boss bar color.

Unquoted `on` and `off` are valid. `/la status` reports `On`, `Scheduled`, or `Off`.

### Sounds

`sounds.enabled` turns announcement sounds on or off. `sounds.announce` plays for players who can see the announcement. `sounds.notify-sender` plays for the player who ran the command.

Sound names must match the server's `Sound` enum, for example `ENTITY_PLAYER_LEVELUP`.

### Discord

Set `discord.enabled` to true and `discord.webhook-url` to a real webhook. The default placeholder URL does not send anything. The embed supports `{player}`, `{platform}`, `{link}`, `{channel}`, and `{timestamp}`.

### Proxy

`proxy.enabled: false` keeps announcements on the local server. Velocity needs Paper, `proxy.velocity-enabled: true`, and `proxy.has-network: true` when more than one server should receive the announcement.

## Placeholders

These work in announcement lines, the holiday line, the title, and the boss bar:

| Placeholder | Value |
| --- | --- |
| `{player}` | Name from `player-format` |
| `{platform}` | Twitch, YouTube, Kick, or TikTok |
| `{platform_upper}` | Platform name in uppercase |
| `{channel}` | Channel parsed from the URL |
| `{link}` | Stream URL |
| `{color}` | Legacy color for that platform |

![Contribute](https://i.imgur.com/EZgFeWf.jpeg)

## Contributing

You can clone this repository, open an issue, suggest an idea, or send a pull request.

Do not republish LiveAnnounce as your own plugin, remove the author, or present a copy as original work.

Keep pull requests focused. Match the code that is already here. Do not put repeated work on the server main thread: announcement delivery is a single pass, and file cleanup stays off that thread.
