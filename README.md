⭐ ElemPoints
Multi-Currency Economy Plugin for Minecraft
Release
JitPack
Minecraft
Java

Unlimited donate currencies • Vault & PlaceholderAPI • SQLite & MySQL • Developer API

📥 Download · 📖 Wiki · 🐛 Issues

📋 Features
💰 Unlimited currencies — each in its own .yml file
🔌 Enable/disable currencies in config.yml without deleting files
⚡ Custom commands and aliases per currency
🌍 Global messages + per-currency overrides
🗄️ SQLite & MySQL with optional per-currency databases
🏦 Vault integration (primary currency)
📊 PlaceholderAPI support
🔄 Data export between currencies with conversion rates
💸 Player transfers with configurable fees
🔧 Developer API with async, events, transaction results
🎨 Hex color support (&#RRGGBB) on 1.16+
♻️ Hot reload, auto-save, in-memory caching
📥 Installation
Download ElemPoints-2.0.0.jar from Releases
Drop into plugins/ folder
Restart server
Optional: install Vault and PlaceholderAPI
Generated files:

text

plugins/ElemPoints/
├── config.yml
├── messages.yml
└── currencies/
    ├── points.yml
    └── crystals.yml
⚙️ Configuration
config.yml
YAML

general:
  locale: "en_US"
  debug: false
  auto-save-interval: 5

database:
  type: SQLITE
  sqlite:
    file: "storage/global.db"

currencies:
  enabled:
    - "points"
    - "crystals"
    # - "tokens"    # commented = disabled
Currency file example (currencies/points.yml)
YAML

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
Add new currency
Create currencies/tokens.yml with unique id: "tokens"
Add "tokens" to config.yml → currencies.enabled
/elempoints reload
🎮 Commands
Global
Command	Description	Permission
/elempoints	Plugin info	—
/elempoints reload	Reload config	elempoints.reload
Aliases: /ep, /epoints

Player (per currency)
Command	Description	Permission
/<cmd>	Your balance	elempoints.currency.<id>.use
/<cmd> <player>	Other's balance	elempoints.currency.<id>.use
/<cmd> pay <player> <amount>	Transfer	elempoints.currency.<id>.use
/<cmd> help	Help	elempoints.currency.<id>.use
Admin (per currency)
Command	Description	Permission
/<admin> give <player> <amount>	Add balance	elempoints.currency.<id>.admin
/<admin> take <player> <amount>	Remove balance	elempoints.currency.<id>.admin
/<admin> set <player> <amount>	Set balance	elempoints.currency.<id>.admin
/<admin> reset <player>	Reset to default	elempoints.currency.<id>.admin
/<admin> check <player>	View balance	elempoints.currency.<id>.admin
/<admin> export <currency> <rate>	Export data	elempoints.currency.<id>.admin
🔑 Permissions
Permission	Default
elempoints.admin	OP
elempoints.reload	OP
elempoints.currency.<id>.use	true
elempoints.currency.<id>.admin	OP
🔄 Export Examples
Command	Result
/pointsadmin export crystals 10.0	1 point → 10 crystals
/pointsadmin export crystals 0.5	2 points → 1 crystal
📊 PlaceholderAPI
Primary currency
Placeholder	Example
%elempoints_points%	1337
%elempoints_points_formatted%	1,337
%elempoints_points_shorthand%	1.3k
Any currency
Placeholder	Example
%elempoints_<id>_points%	500
%elempoints_<id>_points_formatted%	500
%elempoints_<id>_points_shorthand%	500
%elempoints_<id>_symbol%	💎
%elempoints_<id>_name%	&bCrystals
Shorthand: 1k, 1.5M, 3B, 1.2T

🗄️ Database
SQLite — default, zero config.
MySQL — set type: MYSQL in config.yml with host/port/credentials.
Per-currency — add database: section to any currency .yml.
BungeeCord/Velocity — use MySQL with same credentials on all servers.
🔧 Developer API
Dependency
Maven:

XML

<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.Greston897.ElemPoints</groupId>
    <artifactId>elempoints-api</artifactId>
    <version>v2.0.0</version>
    <scope>provided</scope>
</dependency>
Gradle:

groovy

maven { url 'https://jitpack.io' }
compileOnly 'com.github.Greston897.ElemPoints:elempoints-api:v2.0.0'
plugin.yml: depend: [ElemPoints] or softdepend: [ElemPoints]

Usage
Java

import jar.elem.elempoints.api.ElemPointsAPI;
import jar.elem.elempoints.api.ElemPointsProvider;
import jar.elem.elempoints.api.result.TransactionResult;

// Get API
ElemPointsAPI api = ElemPointsProvider.get();

// Check currency exists
if (!api.hasCurrency("crystals")) {
    api.logCurrencyNotFound("MyPlugin", "crystals");
    return;
}

UUID uuid = player.getUniqueId();

// Primary currency
double bal = api.getBalance(uuid);
api.deposit(uuid, 100.0);
api.withdraw(uuid, 50.0);
api.has(uuid, 200.0);

// Specific currency
double crystals = api.getBalance(uuid, "crystals");
api.deposit(uuid, "crystals", 100.0);
api.withdraw(uuid, "crystals", 50.0);
api.setBalance(uuid, "crystals", 500.0);

// Transfer
api.transfer(sender, receiver, "crystals", 100.0);

// Transaction result
TransactionResult r = api.withdraw(uuid, "crystals", 100);
if (r.isSuccess()) {
    player.sendMessage("Balance: " + r.getNewBalance());
} else {
    player.sendMessage("Failed: " + r.getStatus());
}

// Async
api.getBalanceAsync(uuid, "crystals").thenAccept(balance -> {
    Bukkit.getScheduler().runTask(plugin, () -> {
        player.sendMessage("Balance: " + balance);
    });
});
Events
Java

// BalanceChangeEvent — before any change, cancellable
@EventHandler
public void onBalance(BalanceChangeEvent e) {
    if (e.getDelta() < -10000) e.setCancelled(true);
}

// TransferEvent — before transfer, cancellable
@EventHandler
public void onTransfer(TransferEvent e) {
    if (isVIP(e.getSender())) e.setFee(0);
}

// CurrencyRegisterEvent — when currency loads
@EventHandler
public void onRegister(CurrencyRegisterEvent e) {
    getLogger().info("Loaded: " + e.getCurrency().getId());
}
Status codes
SUCCESS, INSUFFICIENT_FUNDS, CURRENCY_NOT_FOUND, MAX_BALANCE_EXCEEDED, TRANSFER_DISABLED, BELOW_MINIMUM, CANCELLED_BY_EVENT, ERROR

🖥️ Supported Platforms
Platform	Versions	Status
Spigot/Paper/Purpur	1.16 – 1.21+	✅
Folia	—	❌
📜 License
MIT License — see LICENSE.

<div align="center">
Made with ❤️ by Greston

</div>
