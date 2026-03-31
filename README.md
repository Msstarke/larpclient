# Larp Client

A feature-rich Minecraft Fabric overlay mod for Hypixel SkyBlock and general use. Larp Client lets you add custom image/GIF/video overlays to your screen, replace vanilla HUD elements, and interact with party chat through fun commands.

## Features

### Overlay System
- **Image/GIF/Video overlays** — drop PNG, JPG, GIF, or MP4 files into a folder, or paste a URL (Tenor, direct links) to download them directly in-game
- **Layer system** — Photoshop-style layer panel with drag-to-reorder, per-layer opacity, lock, and disable controls
- **Pixel-art icon buttons** for lock, disable, opacity, and play/pause
- **GIF animation** with proper frame compositing, disposal methods, and play/pause control
- **Video support** via FFmpeg — drop MP4/MKV/WebM files, frames are extracted and played as animated overlays
- **Auto-scaling** — all overlays scale proportionally when you resize the window or change GUI scale
- Positions, scale, opacity, layer order, and lock state all persist across restarts

### Cortisol Meter
- Speedometer-style stress gauge that replaces vanilla hearts
- 0 = calm (full HP), 20 = dying, negative = absorption overflow
- Smooth animated needle, color gradient (green to red), pulsing glow at high stress
- Scales with window size, manually resizable

### HUD Toggles
- Hide vanilla hearts, armor, hunger, and air bubbles independently
- Hide Hypixel action bar (health/mana display)

### Overlay Editor (`/larpedit` or F12)
- Drag overlays to reposition on screen
- Scroll to resize
- Collapsible side panel with full layer management
- Keyboard shortcuts: L=lock, D=disable, O/P=opacity, W/S=layer, Space=play/pause, TAB=toggle panel

### Party Chat Commands
All commands send results to Hypixel party chat (`/pc`).

| Command | Description |
|---------|-------------|
| `!wind` | Highest current wind speed in the world (40 locations, Open-Meteo API) |
| `!stats <player>` | Hypixel player stats — network level, karma, online/offline |
| `!8ball [question]` | Magic 8 ball |
| `!coinflip` | Heads or tails |
| `!calc <expression>` | Calculator with +, -, *, /, parentheses |
| `!roll [max]` | Random number 1 to max (default 100) |
| `!rps` | Rock paper scissors |

### Keybinds
| Key | Action |
|-----|--------|
| Right Shift | Open config GUI |
| F12 | Open overlay editor |
| F10 | Toggle all overlays on/off |

### Commands
| Command | Description |
|---------|-------------|
| `/larp` | Open config GUI |
| `/larpedit` | Open overlay editor |
| `/larpversion` | Show mod version |
| `/larpupdate` | Check for updates |
| `/larpreload` | Reload image overlays from disk |

### Auto-Updater
- Checks GitHub releases on startup
- Auto-downloads new versions
- Swaps JAR on next game restart

## Download

Grab the latest JAR from [Releases](https://github.com/Msstarke/larpclient/releases).

## Supported Versions
- Minecraft 1.21.10
- Minecraft 1.21.11

## Requirements
- [Fabric Loader](https://fabricmc.net/) 0.16+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- FFmpeg (optional, for video overlay support)

## Quick Start
1. Install Fabric + dependencies
2. Drop the JAR in your mods folder
3. Launch the game
4. `/larp` to open settings, F12 to edit overlay positions
5. To add images: `/larp` > Image Overlays > Open Folder, drop files in, click Reload
6. Or paste a URL and click Download
