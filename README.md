# ⭐ ElemPoints

### Production-Grade Multi-Currency Economy Plugin for Minecraft

[![Release](https://img.shields.io/github/v/release/Greston897/ElemPoints?style=for-the-badge\&color=blue)](https://github.com/Greston897/ElemPoints/releases)
[![JitPack](https://img.shields.io/jitpack/version/com.github.Greston897/ElemPoints?style=for-the-badge\&color=green)](https://jitpack.io/#Greston897/ElemPoints)
[![License](https://img.shields.io/github/license/Greston897/ElemPoints?style=for-the-badge)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16--1.21+-brightgreen?style=for-the-badge\&logo=minecraft)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge\&logo=openjdk)](https://adoptium.net/)

**Create unlimited donate currencies • Full Vault & PlaceholderAPI support • SQLite & MySQL • Developer API**

[📥 Download](https://github.com/Greston897/ElemPoints/releases) • [📖 Wiki](https://github.com/Greston897/ElemPoints/wiki) • [🔌 API Guide](#-developer-api) • [🐛 Issues](https://github.com/Greston897/ElemPoints/issues)

---

## 📋 Features

| Feature                      | Description                                                               |
| ---------------------------- | ------------------------------------------------------------------------- |
| 💰 **Unlimited Currencies**  | Create as many donate currencies as you need, each in its own `.yml` file |
| 🔌 **Enable/Disable**        | Toggle currencies on and off in `config.yml` without deleting files       |
| ⚡ **Custom Commands**        | Each currency gets its own commands with custom aliases                   |
| 🌍 **Per-Currency Messages** | Global messages + per-currency overrides for full localization            |
| 🗄️ **SQLite & MySQL**       | Global database or separate database per currency                         |
| 🏦 **Vault Integration**     | Primary currency works as Vault economy provider                          |
| 📊 **PlaceholderAPI**        | Full placeholder support for scoreboards, holograms, etc.                 |
| 🔄 **Data Export**           | Convert balances between currencies with custom rates                     |
| 🛡️ **Permissions**          | Separate permissions for each currency's commands                         |
| 💸 **Transfers**             | Player-to-player transfers with configurable fees                         |
| 🔧 **Developer API**         | Clean API with async support, events, and transaction results             |
| 🎨 **Hex Colors**            | Full hex color support (`&#RRGGBB`) on 1.16+                              |
| ♻️ **Hot Reload**            | Reload everything without server restart                                  |
| 💾 **Auto-Save**             | Configurable auto-save interval                                           |
| 📦 **Caching**               | In-memory cache for fast balance lookups                                  |

---

## 📥 Installation

1. Download `ElemPoints-2.0.0.jar` from [Releases](https://github.com/Greston897/ElemPoints/releases)
2. Place it in your server's `plugins/` folder
3. Start/restart the server
4. Configure in `plugins/ElemPoints/`

### Optional Dependencies

| Plugin                                                                    | Purpose                                       |
| ------------------------------------------------------------------------- | --------------------------------------------- |
| [Vault](https://www.spigotmc.org/resources/vault.34315/)                  | Economy integration with other plugins        |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Placeholders for scoreboards, holograms, etc. |

### Verify Installation

```
[ElemPoints] ========================================
[ElemPoints] ElemPoints v2.0.0 enabled!
[ElemPoints] Loaded 2 currencies in 45ms
[ElemPoints] ========================================
```

---

## ⚙️ Configuration

### File Structure

```
plugins/ElemPoints/
├── config.yml
├── messages.yml
├── currencies/
│   ├── points.yml
│   ├── crystals.yml
│   └── tokens.yml
└── storage/
    ├── global.db
    └── crystals.db
```

### config.yml

<details>
<summary>Click to expand</summary>

```yaml
general:
  locale: "en_US"
  debug: false
  auto-save-interval: 5

database:
  type: SQLITE
  sqlite:
    file: "storage/global.db"
  mysql:
    host: "localhost"
    port: 3306
    database: "elempoints"
    username: "root"
    password: ""
    table-prefix: "ep_"
    pool:
      max-size: 10
      min-idle: 2
      max-lifetime: 1800000
      timeout: 5000
    properties:
      useSSL: "false"
      characterEncoding: "utf8mb4"
      useUnicode: "true"
      autoReconnect: "true"

currencies:
  enabled:
    - "points"
    - "crystals"
    # - "tokens"
```

</details>

---

### Currency Example — points.yml

<details>
<summary>Click to expand</summary>

```yaml
id: "points"

display:
  name: "&6Donate Points"
  singular: "point"
  plural: "points"
  symbol: "⭐"

economy:
  default-balance: 0.0
  primary: true
  max-balance: 0
  min-balance: 0

transfer:
  enabled: true
  fee-percent: 0.0
  minimum: 1.0

commands:
  player:
    name: "points"
    aliases: ["pts", "dp"]
    permission: "elempoints.currency.points.use"

  admin:
    name: "pointsadmin"
    aliases: ["ptsa"]
    permission: "elempoints.currency.points.admin"
```

</details>

---

### Adding a New Currency

1. Create `plugins/ElemPoints/currencies/tokens.yml`:

```yaml
id: "tokens"
display:
  name: "&eTokens"
  singular: "token"
  plural: "tokens"
  symbol: "🪙"
economy:
  default-balance: 100
  primary: false
  max-balance: 0
  min-balance: 0
transfer:
  enabled: false
  fee-percent: 0
  minimum: 1
commands:
  player:
    name: "tokens"
    aliases: ["tk"]
    permission: "elempoints.currency.tokens.use"
  admin:
    name: "tokensadmin"
    aliases: ["tka"]
    permission: "elempoints.currency.tokens.admin"
```

2. Add to `config.yml`:

```yaml
currencies:
  enabled:
    - "points"
    - "crystals"
    - "tokens"
```

3. Reload: `/elempoints reload`

---

# 📝 Commands & Permissions

(Полный список команд, разрешений и PlaceholderAPI можно вставить сюда из твоего текста, как в оригинале)

---

# 📄 License

MIT License

<div align="center">
Made with ❤️ by Greston
</div>
