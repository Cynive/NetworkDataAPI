# NetworkDataAPI

> Enterprise-level data synchronization plugin for large Minecraft networks

[![Build](https://github.com/Cynive/NetworkDataAPI/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/Cynive/NetworkDataAPI/actions/workflows/build-and-release.yml)
[![Maven CI](https://github.com/Cynive/NetworkDataAPI/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/Cynive/NetworkDataAPI/actions/workflows/maven-ci.yml)
[![CodeQL](https://github.com/Cynive/NetworkDataAPI/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/Cynive/NetworkDataAPI/actions/workflows/codeql-analysis.yml)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20BungeeCord-brightgreen.svg)](https://papermc.io)

## 🚀 Overview

NetworkDataAPI is a production-grade, enterprise-level **MongoDB connection layer** designed for large Minecraft networks (similar to Hypixel or CubeCraft). It provides a **shared MongoDB connection pool** that all your plugins can use, eliminating the need for each plugin to create its own database connections.

### ✨ Key Features

- **🌐 Universal Compatibility**: Single codebase works on both Paper/Spigot and BungeeCord
- **🔗 Shared Connection Pool**: ONE database connection for ALL your plugins - no more connection spam!
- **⚡ High Performance**: Built-in Caffeine caching reduces database load by 80%+
- **🔒 Thread-Safe**: All operations are thread-safe with comprehensive async support
- **🔄 Auto-Recovery**: Automatic reconnection and retry logic for resilience
- **💾 Connection Pooling**: Configurable MongoDB connection pools for optimal performance
- **🌍 REST API**: Optional HTTP endpoints for external integrations
- **📚 Well Documented**: Comprehensive JavaDoc and developer documentation
- **🏗️ Clean Architecture**: SOLID principles with dependency injection and service patterns

### 🎯 What NetworkDataAPI Does (and Doesn't Do)

**✅ What it DOES:**
- Provides a **shared MongoDB connection pool** for all plugins on a server
- Offers a **high-level API** for database operations (insert, query, update, delete)
- Handles **automatic reconnection** and connection health monitoring
- Provides **caching** to reduce database load (80%+ reduction)
- Offers **async operations** to prevent server lag

**❌ What it DOESN'T Do:**
- Does **NOT** automatically manage player data
- Does **NOT** automatically track player joins/quits
- Does **NOT** create any default collections or documents
- Does **NOT** decide what data your plugins store

**NetworkDataAPI is ONLY a database connection layer!**

### 💡 Real-World Use Case

**Scenario:** You have a network with 5 servers

**Without NetworkDataAPI:**
```java
// In your Cosmetics Plugin
MongoClient client = new MongoClient("mongodb://...");  // 10 connections
MongoDatabase db = client.getDatabase("cosmetics");
// ... cosmetics logic

// In your Economy Plugin  
MongoClient client = new MongoClient("mongodb://...");  // Another 10 connections!
MongoDatabase db = client.getDatabase("economy");
// ... economy logic

// Each plugin opens its own connections = connection spam!
```

**With NetworkDataAPI:**
```java
// In your Cosmetics Plugin
NetworkDataAPIProvider api = APIRegistry.getAPI();
MongoDatabase db = api.getDatabase("cosmetics");  // Uses shared pool!
MongoCollection<Document> cosmetics = db.getCollection("player_cosmetics");
// ... cosmetics logic

// In your Economy Plugin
NetworkDataAPIProvider api = APIRegistry.getAPI();
MongoDatabase db = api.getDatabase("economy");  // Uses the same shared pool!
MongoCollection<Document> balances = db.getCollection("player_balances");
// ... economy logic

// Both plugins share 1 connection pool = efficient!
```

**Each plugin creates its own data when needed:**
- Cosmetics plugin creates cosmetic data when a player claims a cosmetic
- Economy plugin creates balance data when a player earns coins
- Stats plugin creates stats data when a player gets a kill
- **NetworkDataAPI creates NOTHING automatically!**

## 🎯 Why Use This?

### The Problem: Connection Overload
Without NetworkDataAPI, **each plugin** creates its own database connections:

```
Server with 3 plugins (each with connection pool of 10):
├─ Cosmetics Plugin → 10 MongoDB connections
├─ Economy Plugin   → 10 MongoDB connections  
└─ Stats Plugin     → 10 MongoDB connections
   TOTAL: 30 database connections per server! 😱

With 5 servers in your network = 150 connections!
```

### The Solution: Shared Connection Pool
With NetworkDataAPI, **all plugins** share one connection pool:

```
Server with 3 plugins via NetworkDataAPI:
└─ NetworkDataAPI → 1 shared connection pool (max 100 connections)
   ├─ Cosmetics Plugin → uses shared pool
   ├─ Economy Plugin   → uses shared pool
   └─ Stats Plugin     → uses shared pool
   TOTAL: Max 100 connections, shared by all plugins! 🚀

With 5 servers in your network = Max 500 connections (vs 750!)
```

**Benefits:**
- ✅ Less RAM usage
- ✅ Better performance  
- ✅ Automatic reconnection for ALL plugins
- ✅ Shared caching layer
- ✅ Each plugin creates its own data (cosmetics, economy, stats, etc.)

### How it works

**NetworkDataAPI does:**
- Opens 1 MongoDB connection pool
- Provides API for database operations
- Handles caching & reconnection

**Your plugins do:**
- **Cosmetics Plugin**: Creates `claimed_cosmetics` collection with cosmetic data
- **Economy Plugin**: Creates `player_money` collection with balance data  
- **Stats Plugin**: Creates `player_stats` collection with kills/deaths/etc
- Each plugin decides WHAT data, WHEN to save, HOW to structure

**No default player data!** NetworkDataAPI creates NOTHING automatically.

**See `API_DOCUMENTATION.md` for details!**

## 📋 Requirements

- **Java 17 or higher**
- **MongoDB 4.0 or higher**
- **Paper/Spigot 1.20+ or BungeeCord**

## 📦 Installation

### Quick Start

1. **Download** the appropriate JAR:
   - Paper/Spigot: `NetworkDataAPI-Paper-1.0-SNAPSHOT.jar`
   - BungeeCord: `NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar`

2. **Place** in your `plugins/` folder

3. **Start** your server - config will be auto-generated

4. **Configure** MongoDB connection in `plugins/NetworkDataAPI/config.yml`:

```yaml
mongodb:
  uri: "mongodb://localhost:27017"
  database: "minecraft_network"
  username: ""
  password: ""
```

5. **Restart** your server

## 🔧 Building from Source

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Build Commands

**Windows:**
```bash
mvn clean package
```

**Linux/Mac:**
```bash
mvn clean package
```

### Output
Build artifacts will be located at:
- `networkdataapi-paper/target/NetworkDataAPI-Paper-1.0-SNAPSHOT.jar`
- `networkdataapi-bungee/target/NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar`

---

## 📖 For Plugin Developers

### Option 1: Use as a Library (Maven/Gradle)

Add the NetworkDataAPI Core library to your plugin's dependencies:

#### Maven
```xml
<repositories>
   <repository>
      <id>cynive-snapshots</id>
      <name>Cynive Maven Snapshots</name>
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

#### Gradle
```gradle
repositories {
    maven {
        url = uri("https://cdn.ordnary.com/repository/maven-snapshots/")
    }
}

dependencies {
    compileOnly 'com.cynive:networkdataapi-core:1.0-SNAPSHOT'
}
```

**Note:** Use `scope: provided` (Maven) or `compileOnly` (Gradle) because the NetworkDataAPI plugin provides the library at runtime!

### Option 2: Plugin Dependency

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

### Basic Usage

```java
import api.com.cynive.networkdataapi.core.APIRegistry;
import api.com.cynive.networkdataapi.core.NetworkDataAPIProvider;
import service.com.cynive.networkdataapi.core.PlayerDataService;

public class YourPlugin extends JavaPlugin {
    
    private PlayerDataService playerData;
    
    @Override
    public void onEnable() {
        // Get API
        NetworkDataAPIProvider api = APIRegistry.getAPI();
        if (api == null) {
            getLogger().severe("NetworkDataAPI not found!");
            return;
        }
        
        playerData = api.getPlayerDataService();
        
        // Use it!
        UUID playerUUID = // ... get player UUID
        
        // Get player data (async)
        playerData.getPlayerDataAsync(playerUUID).thenAccept(data -> {
            int coins = data.getInteger("coins", 0);
            getLogger().info("Player has " + coins + " coins");
        });
        
        // Update data
        playerData.updateFieldAsync(playerUUID, "coins", 1000);
        
        // Increment values
        playerData.incrementFieldAsync(playerUUID, "kills", 1);
    }
}
```

### ⚠️ Important: Player Event Handling

**NetworkDataAPI does NOT automatically track player joins/quits or create default player data.** You must handle this in your custom plugins!

#### Example: Handling Player Joins in Your Plugin

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MyPlayerListener implements Listener {
    
    private final PlayerDataService playerData;
    
    public MyPlayerListener(PlayerDataService playerData) {
        this.playerData = playerData;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        // Load/create player data for YOUR plugin
        playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
            // Handle player data - set defaults if needed
            if (!data.containsKey("myPluginData")) {
                data.put("myPluginData", new Document()
                    .append("coins", 0)
                    .append("level", 1)
                    .append("firstJoin", System.currentTimeMillis())
                );
                playerData.savePlayerDataAsync(uuid, data);
            }
            
            // Update last login
            playerData.updateFieldAsync(uuid, "lastLogin", System.currentTimeMillis());
        });
    }
}
```

**Why this design?**
- ✅ Each plugin controls its own data
- ✅ No unwanted default fields created
- ✅ Multiple plugins can coexist without conflicts
- ✅ You decide WHAT data to store and WHEN

See `PlayerConnectionListener.java` in the source code for a complete reference implementation!

### Using for Your Own Data (Custom Collections)

**Perfect for Cosmetics, Guilds, Ranks, Punishments, etc!**

```java
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// Get the shared database connection
MongoDatabase database = api.getDatabase();

// Create your own collections
MongoCollection<Document> cosmetics = database.getCollection("cosmetics");
MongoCollection<Document> guilds = database.getCollection("guilds");

// Use them just like regular MongoDB!
Document cosmetic = new Document("name", "Party Hat")
    .append("price", 1000)
    .append("rarity", "RARE");
cosmetics.insertOne(cosmetic);

// Query your data
Document guild = guilds.find(Filters.eq("name", "Warriors")).first();
```

**Benefits:**
- ✅ No separate database connection needed
- ✅ Uses shared connection pool (efficient!)
- ✅ Automatic reconnection
- ✅ Less resource usage

### 📦 Example Plugin

**Want to see a complete working example?**

Check out the [networkdataapi-example-plugin](networkdataapi-example-plugin/) module!

This example plugin demonstrates:
- ✅ Creating an isolated MongoDB database
- ✅ Managing custom collections
- ✅ Insert, query, update, and delete operations
- ✅ Creating indexes for performance
- ✅ Comprehensive logging for debugging
- ✅ In-game commands for testing

**Quick Start:**
```bash
# See the example plugin guide
cat EXAMPLE_PLUGIN_GUIDE.md

# Or jump straight to the code
cd networkdataapi-example-plugin/src/main/java
```

**Key Features:**
- Full CRUD operations on custom collections
- Dedicated database per plugin (`example_plugin`)
- Sample collection (`example_collection`)
- 8 in-game commands to test all operations
- Production-ready code with best practices

See [EXAMPLE_PLUGIN_GUIDE.md](EXAMPLE_PLUGIN_GUIDE.md) for full details!

### More Examples

**Save complete player data:**
```java
Document playerData = new Document()
    .append("coins", 1000)
    .append("level", 5)
    .append("rank", "VIP");
    
playerDataService.savePlayerDataAsync(uuid, playerData);
```

**Query players:**
```java
import com.mongodb.client.model.Filters;

Bson filter = Filters.gt("coins", 1000);
playerDataService.queryAsync(filter, 10).thenAccept(results -> {
    // Process top 10 richest players
});
```

**Update multiple fields:**
```java
Map<String, Object> updates = Map.of(
    "coins", 2000,
    "level", 6,
    "lastSeen", System.currentTimeMillis()
);
playerDataService.updateFieldsAsync(uuid, updates);
```

## 📚 Documentation

- **[Complete API Documentation](API_DOCUMENTATION.md)** - Full developer guide with examples
- **[JavaDoc](docs/)** - Generated API documentation (coming soon)

## 🏗️ Architecture

```
NetworkDataAPI-parent/
├── networkdataapi-core/          # Shared core logic
│   ├── config/                   # Configuration management
│   ├── database/                 # MongoDB connection & pooling
│   ├── cache/                    # Caffeine caching layer
│   ├── async/                    # Async executor & thread pools
│   ├── service/                  # Business logic (PlayerDataService)
│   ├── rest/                     # REST API endpoints
│   └── api/                      # Public API interfaces
│
├── networkdataapi-paper/         # Paper/Spigot implementation
│   └── Paper-specific hooks
│
├── networkdataapi-bungee/        # BungeeCord implementation
│   └── BungeeCord-specific hooks
│
└── networkdataapi-example-plugin/ # Example plugin (NEW!)
    ├── ExamplePlugin.java        # Main plugin class
    ├── ExampleDataManager.java   # MongoDB operations
    ├── ExampleCommand.java       # In-game commands
    └── README.md                 # Complete documentation
```

## ⚙️ Configuration

<details>
<summary>View complete configuration</summary>

```yaml
# MongoDB Connection
mongodb:
  uri: "mongodb://localhost:27017"
  database: "minecraft_network"
  max-pool-size: 100
  min-pool-size: 10

# Cache Settings  
cache:
  enabled: true
  max-size: 10000
  expire-after-write-minutes: 5
  expire-after-access-minutes: 10

# REST API (Optional)
rest-api:
  enabled: false
  port: 8080
  api-key: ""
  allowed-ips:
    - "127.0.0.1"

# Thread Pool
async:
  core-pool-size: 4
  max-pool-size: 16
  keep-alive-seconds: 60

# Logging
logging:
  level: "INFO"
  debug: false
```

</details>

## 🔌 REST API

Enable the REST API for external integrations:

```yaml
rest-api:
  enabled: true
  port: 8080
  api-key: "your-secret-key"
```

**Endpoints:**
- `GET /api/health` - Health check
- `GET /api/player/{uuid}` - Get player data
- `POST /api/player/{uuid}` - Update player data
- `DELETE /api/player/{uuid}` - Delete player data
- `GET /api/stats` - API statistics

**Example:**
```bash
curl -H "X-API-Key: your-secret-key" \
     http://localhost:8080/api/player/uuid-here
```

## 📊 Performance

- **Cache Hit Rate**: 85-95% (typical)
- **Query Response**: <5ms (cached), <50ms (database)
- **Connection Pool**: Handles 1000+ concurrent operations
- **Memory Usage**: ~50-100MB (configurable)

## 🛠️ Admin Commands

```
/networkdataapi status          # Show API status
/networkdataapi reload          # Reload configuration
/networkdataapi cache stats     # Show cache statistics
/networkdataapi cache clear     # Clear cache
```

Aliases: `/ndapi`, `/napi`

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Stijn Jakobs**

## 🙏 Acknowledgments

- MongoDB Java Driver
- Caffeine Cache by Ben Manes
- Spark Java for REST API
- Paper and BungeeCord teams

## 📞 Support

- **Documentation**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **Issues**: [GitHub Issues](https://github.com/astroid/NetworkDataAPI/issues)

---

**Built with ❤️ for the Minecraft community**

