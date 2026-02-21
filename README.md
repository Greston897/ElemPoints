<div align="center">

# ⭐ ElemPoints

### Production-Grade Multi-Currency Economy Plugin for Minecraft

[![Release](https://img.shields.io/github/v/release/Greston897/ElemPoints?style=for-the-badge&color=blue)](https://github.com/Greston897/ElemPoints/releases)
[![JitPack](https://img.shields.io/jitpack/version/com.github.Greston897/ElemPoints?style=for-the-badge&color=green)](https://jitpack.io/#Greston897/ElemPoints)
[![License](https://img.shields.io/github/license/Greston897/ElemPoints?style=for-the-badge)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16--1.21+-brightgreen?style=for-the-badge&logo=minecraft)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=openjdk)](https://adoptium.net/)

**Create unlimited donate currencies • Full Vault & PlaceholderAPI support • SQLite & MySQL • Developer API**

[📥 Download](https://github.com/Greston897/ElemPoints/releases) •
[📖 Documentation](docs/) •
[🔌 API Guide](#-developer-api) •
[🐛 Issues](https://github.com/Greston897/ElemPoints/issues)

</div>

---

## 📋 Features

| Feature | Description |
|---------|-------------|
| 💰 **Unlimited Currencies** | Create as many donate currencies as you need, each in its own `.yml` file |
| 🔌 **Enable/Disable** | Toggle currencies on and off in `config.yml` without deleting files |
| ⚡ **Custom Commands** | Each currency gets its own commands with custom aliases |
| 🌍 **Per-Currency Messages** | Global messages + per-currency overrides for full localization |
| 🗄️ **SQLite & MySQL** | Global database or separate database per currency |
| 🏦 **Vault Integration** | Primary currency works as Vault economy provider |
| 📊 **PlaceholderAPI** | Full placeholder support for scoreboards, holograms, etc. |
| 🔄 **Data Export** | Convert balances between currencies with custom rates |
| 🛡️ **Permissions** | Separate permissions for each currency's commands |
| 💸 **Transfers** | Player-to-player transfers with configurable fees |
| 🔧 **Developer API** | Clean API with async support, events, and transaction results |
| 🎨 **Hex Colors** | Full hex color support (`&#RRGGBB`) on 1.16+ |
| ♻️ **Hot Reload** | Reload everything without server restart |
| 💾 **Auto-Save** | Configurable auto-save interval |
| 📦 **Caching** | In-memory cache for fast balance lookups |

---

## 📥 Installation

1. Download `ElemPoints-1.0.jar` from [Releases](https://github.com/Greston897/ElemPoints/releases)
2. Place it in your server's `plugins/` folder
3. Start/restart the server
4. Configure in `plugins/ElemPoints/`

### Optional Dependencies

| Plugin | Purpose |
|--------|---------|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Economy integration with other plugins |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Placeholders for scoreboards, holograms, etc. |

---

## ⚙️ Configuration

### File Structure
