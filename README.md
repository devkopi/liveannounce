# LiveAnnounce

A lightweight Minecraft Bukkit/Spigot plugin that allows announcing live streams to the entire server with a single command. Automatically detects streaming platforms from URLs and sends formatted announcements.

## Features
- Single command: `/directo <stream-url>`
- Automatic platform detection (Twitch, YouTube, Kick, TikTok)
- Permission-based access control
- Easy configuration

## Commands & Permissions
| Command | Description | Permission |
|---------|-------------|------------|
| `/directo <url>` | Announce a stream to all players | `liveannounce.directo` |
| `/la help` | Show plugin help | None |
| `/la version` | Show plugin version | None |
| `/la reload` | Reload configuration | `liveannounce.admin` |
| `/la status` | Show plugin status | `liveannounce.admin` |

## Examples
```bash
/directo https://twitch.tv/ccoding
/directo https://youtube.com/live/ccoding
/directo https://kick.com/ccoding
/directo https://tiktok.com/@ccoing/live
```

Output:
```bas
--------------------------------------------------
⚡ ¡LIVE ON TWITCH! ⚡
PlayerName is streaming live
Join now! https://twitch.tv/ccoding
--------------------------------------------------
```

# Installation
1. Download the plugin from [Spigot](https://www.spigotmc.org/resources/liveannouncer-stream-alert-system-%E3%80%8C1-8-1-21%E3%80%8D.130601/) | [Modrinth](https://modrinth.com/plugin/liveannouncer)
2. Place the plugin in the `plugins` folder of your Spigot server
3. Restart/Start your server