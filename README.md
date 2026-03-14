<div align="center">

# NetworkDataAPI

**Enterprise-grade shared MongoDB connection layer for large-scale Minecraft networks**

[![Build](https://github.com/7txr/NetworkDataAPI/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/7txr/NetworkDataAPI/actions/workflows/build-and-release.yml)
[![Maven CI](https://github.com/7txr/NetworkDataAPI/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/7txr/NetworkDataAPI/actions/workflows/maven-ci.yml)
[![CodeQL](https://github.com/7txr/NetworkDataAPI/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/7txr/NetworkDataAPI/actions/workflows/codeql-analysis.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20BungeeCord-brightgreen.svg)](https://papermc.io)

A production-ready, platform-agnostic data layer that eliminates connection sprawl across distributed Minecraft server networks — inspired by architectures used at Hypixel and CubeCraft scale.

</div>

---

## The Problem

On a typical Minecraft network, every plugin manages its own database connections. This creates a **connection storm** that degrades performance as the network grows:

```
Network with 5 servers, 3 plugins each (pool size: 10):

Without NetworkDataAPI                 With NetworkDataAPI
─────────────────────────────────      ──────────────────────────────────
Server 1                               Server 1
  ├─ Cosmetics Plugin → 10 conns         └─ NetworkDataAPI → 1 shared pool
  ├─ Economy Plugin   → 10 conns               ├─ Cosmetics Plugin
  └─ Stats Plugin     → 10 conns               ├─ Economy Plugin
                                               └─ Stats Plugin
Server 2 ... (×5 servers)
                                       All plugins. One pool.
Total: 150 MongoDB connections   →     Max: 500 connections (configurable)
```

NetworkDataAPI solves this by providing **one shared MongoDB connection pool** that all plugins consume simultaneously, with a built-in Caffeine caching layer that reduces live database reads by 80–95%.

---

## Features

| Feature | Details |
|---------|---------|
| **Shared connection pool** | One MongoDB pool for all plugins on a server — no more per-plugin connection spam |
| **Platform-agnostic core** | Single codebase, thin adapter modules for Paper/Spigot and BungeeCord/Velocity |
| **Caffeine cache** | In-memory caching with configurable TTL; 85–95% cache hit rate in production |
| **Fully async API** | `CompletableFuture`-based — zero blocking on the main server thread |
| **Auto-recovery** | Automatic reconnection and retry logic with configurable backoff |
| **REST API** | Optional HTTP endpoints for external service integrations, secured via `X-API-Key` |
| **Thread-safe** | All operations safe for concurrent access across async plugin threads |
| **GitHub Actions CI** | Build, release, and CodeQL pipelines included out of the box |

---

## Requirements

- Java **17** or higher
- MongoDB **4.0** or higher
- Paper/Spigot **1.20+** or BungeeCord (latest stable)
- Maven **3.6+** (for building from source)

---

## Installation

### 1. Download the JAR

Download the appropriate artifact for your platform from [Releases](https://github.com/7txr/NetworkDataAPI/releases):

| Platform | Artifact |
|---------|---------|
| Paper / Spigot | `NetworkDataAPI-Paper-1.0-SNAPSHOT.jar` |
| BungeeCord | `NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar` |

### 2. Deploy

Place the JAR in your server's `plugins/` directory and start the server. A default `config.yml` is generated automatically at `plugins/NetworkDataAPI/config.yml`.

### 3. Configure

```yaml
mongodb:
  uri: "mongodb://localhost:27017"
  database: "minecraft_network"
  username: ""
  password: ""
  max-pool-size: 100
  min-pool-size: 10

cache:
  enabled: true
  max-size: 10000
  expire-after-write-minutes: 5
  expire-after-access-minutes: 10

async:
  core-pool-size: 4
  max-pool-size: 16
  keep-alive-seconds: 60

rest-api:
  enabled: false
  port: 8080
  api-key: ""
  allowed-ips:
    - "127.0.0.1"

logging:
  level: "INFO"
  debug: false
```

### 4. Restart

Restart your server and verify connectivity in the console:

```
[NetworkDataAPI] Connected to MongoDB at localhost:27017
[NetworkDataAPI] Cache initialized (max-size: 10000, TTL: 5min)
[NetworkDataAPI] REST API disabled — set rest-api.enabled: true to activate
[NetworkDataAPI] NetworkDataAPI ready.
```

---

## Building from Source

```bash
git clone https://github.com/7txr/NetworkDataAPI.git
cd NetworkDataAPI
mvn clean package
```

Build artifacts:

```
networkdataapi-paper/target/NetworkDataAPI-Paper-1.0-SNAPSHOT.jar
networkdataapi-bungee/target/NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar
```

---

## For Plugin Developers

NetworkDataAPI is a **connection layer, not a data manager**. It does not create collections, track players, or define schemas. Your plugin owns its data — NetworkDataAPI just gives you the connection.

### Add as a dependency

**Maven:**

```xml
<repositories>
  <repository>
    <id>ordnary-snapshots</id>
    <url>https://cdn.ordnary.com/repository/maven-snapshots/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.cynive</groupId>
    <artifactId>networkdataapi-core</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

**Gradle:**

```groovy
repositories {
    maven { url = uri("https://cdn.ordnary.com/repository/maven-snapshots/") }
}

dependencies {
    compileOnly 'com.cynive:networkdataapi-core:1.0-SNAPSHOT'
}
```

> Use `scope: provided` / `compileOnly` — the runtime JAR is provided by the NetworkDataAPI plugin itself.

### Declare the dependency in your plugin descriptor

**plugin.yml (Paper/Spigot):**
```yaml
depend:
  - NetworkDataAPI
```

**bungee.yml (BungeeCord):**
```yaml
depends:
  - NetworkDataAPI
```

---

## API Usage

### Get the API instance

```java
NetworkDataAPIProvider api = APIRegistry.getAPI();

if (api == null) {
    getLogger().severe("NetworkDataAPI not found — disabling.");
    getServer().getPluginManager().disablePlugin(this);
    return;
}
```

### Working with player data

```java
PlayerDataService playerData = api.getPlayerDataService();
UUID uuid = player.getUniqueId();

// Async read (cache-first, then MongoDB)
playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
    int coins = data.getInteger("coins", 0);
    int kills = data.getInteger("kills", 0);
    getLogger().info(player.getName() + " has " + coins + " coins and " + kills + " kills.");
});

// Update a single field
playerData.updateFieldAsync(uuid, "coins", 1500);

// Atomic increment
playerData.incrementFieldAsync(uuid, "kills", 1);

// Batch update
Map<String, Object> updates = Map.of(
    "coins",    2000,
    "level",    6,
    "lastSeen", System.currentTimeMillis()
);
playerData.updateFieldsAsync(uuid, updates);

// Query — e.g. top 10 players by coins
Bson filter = Filters.gt("coins", 1000);
playerData.queryAsync(filter, 10).thenAccept(results -> {
    results.forEach(doc -> getLogger().info(doc.toJson()));
});
```

### Using your own collections

```java
// Access the shared database connection
MongoDatabase database = api.getDatabase();

// Define your own collections — NetworkDataAPI creates nothing by default
MongoCollection<Document> cosmetics = database.getCollection("cosmetics");
MongoCollection<Document> guilds    = database.getCollection("guilds");

// Standard MongoDB operations
cosmetics.insertOne(new Document("name", "Party Hat")
    .append("price", 1000)
    .append("rarity", "RARE"));

Document guild = guilds.find(Filters.eq("name", "Warriors")).first();
```

### Handling player joins

NetworkDataAPI does **not** hook into player events — your plugin is responsible for initialising its own data:

```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();

    playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
        if (!data.containsKey("myPlugin")) {
            playerData.updateFieldAsync(uuid, "myPlugin", new Document()
                .append("coins",     0)
                .append("level",     1)
                .append("firstJoin", System.currentTimeMillis())
            );
        }
        playerData.updateFieldAsync(uuid, "lastLogin", System.currentTimeMillis());
    });
}
```

---

## REST API

Enable the optional HTTP layer for external service integrations:

```yaml
rest-api:
  enabled: true
  port: 8080
  api-key: "your-secret-key"
  allowed-ips:
    - "127.0.0.1"
```

### Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| `GET` | `/api/health` | Health check |
| `GET` | `/api/player/{uuid}` | Retrieve player data document |
| `POST` | `/api/player/{uuid}` | Create or update player data |
| `DELETE` | `/api/player/{uuid}` | Delete player data |
| `GET` | `/api/stats` | Connection pool and cache statistics |

**Example:**

```bash
curl -H "X-API-Key: your-secret-key" \
     http://localhost:8080/api/player/550e8400-e29b-41d4-a716-446655440000
```

---

## Architecture

```
NetworkDataAPI/
├── networkdataapi-core/              # Platform-agnostic core
│   ├── api/                          # Public API interfaces (NetworkDataAPIProvider, etc.)
│   ├── database/                     # MongoDB client, connection pooling
│   ├── cache/                        # Caffeine caching layer
│   ├── async/                        # Thread pool executor management
│   ├── service/                      # Business logic (PlayerDataService)
│   ├── rest/                         # Spark Java HTTP endpoints
│   └── config/                       # Configuration model & parsing
│
├── networkdataapi-paper/             # Paper/Spigot adapter
│   └── Paper lifecycle hooks, event registration
│
├── networkdataapi-bungee/            # BungeeCord adapter
│   └── BungeeCord lifecycle hooks, event registration
│
└── networkdataapi-example-plugin/    # Full reference implementation
    ├── ExamplePlugin.java            # Plugin bootstrap
    ├── ExampleDataManager.java       # CRUD, indexing, collection management
    └── ExampleCommand.java           # 8 in-game test commands
```

**Data flow:**

```
Plugin calls getPlayerDataAsync(uuid)
        │
        ▼
  Caffeine cache hit? ──yes──► return cached Document  (~0ms)
        │ no
        ▼
  MongoDB async query via connection pool
        │
        ▼
  Store result in cache
        │
        ▼
  Complete CompletableFuture on calling thread
```

---

## Performance

| Metric | Value |
|--------|-------|
| Cache hit latency | `< 1ms` |
| MongoDB read latency (cached miss) | `< 50ms` |
| MongoDB write latency | `< 20ms` |
| Cache hit rate (typical) | `85 – 95%` |
| Max concurrent operations | `1000+` |
| Memory footprint | `~50 – 100MB` (configurable) |

---

## Admin Commands

```
/networkdataapi status        — Show connection pool and cache status
/networkdataapi reload        — Reload configuration without restart
/networkdataapi cache stats   — Detailed cache hit/miss statistics
/networkdataapi cache clear   — Flush the in-memory cache
```

Aliases: `/ndapi`, `/napi`

---

## Contributing

Contributions are welcome. Please follow the standard GitHub flow:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit with a clear message: `git commit -m 'Add: your feature description'`
4. Push and open a Pull Request against `main`

Before submitting, ensure:
- All existing tests pass: `mvn test`
- New public APIs include JavaDoc
- Async operations are non-blocking on the calling thread
- Exceptions are logged with context, never swallowed silently

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Acknowledgements

- [MongoDB Java Driver](https://www.mongodb.com/docs/drivers/java/sync/current/) — database client
- [Caffeine](https://github.com/ben-manes/caffeine) by Ben Manes — in-memory caching
- [Spark Java](https://sparkjava.com/) — lightweight REST API framework
- [PaperMC](https://papermc.io/) and [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/) — platform APIs

---

<div align="center">
  <sub>Built by <a href="https://github.com/7txr">Stijn / 7txr</a> · Part of the <a href="https://github.com/ordnary-com">Ordnary</a> ecosystem</sub>
</div>
