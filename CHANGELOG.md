# Changelog

All notable changes to Larp Client will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-03-30

### Added
- Initial project setup with Fabric mod loader
- Multi-version support for Minecraft 1.21.10 and 1.21.11 via Stonecutter
- Config GUI system powered by MoulConfig (open with `/larp` command or Right Shift key)
- Overlay rendering system with draggable positions (`/larpedit` or F12 key)
- Example overlay showing mod info and FPS
- Auto-update system via GitHub releases using libautoupdate
  - Checks for updates on startup
  - Filters releases by Minecraft version
  - Optional auto-download
  - Manual check via `/larpupdate` command
- ModMenu integration for config screen access
