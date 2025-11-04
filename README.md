# NetworkDataAPI

> Enterprise-level data synchronization plugin for large Minecraft networks

[![Build](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/build-and-release.yml)
[![Maven CI](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/maven-ci.yml)
[![CodeQL](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/astroidmc/NetworkDataAPI/actions/workflows/codeql-analysis.yml)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20BungeeCord-brightgreen.svg)](https://papermc.io)

## 🚀 Overview

NetworkDataAPI is a production-grade, enterprise-level data synchronization solution designed for large Minecraft networks (similar to Hypixel or CubeCraft). It provides a unified MongoDB-backed data layer that works seamlessly across both Paper/Spigot servers and BungeeCord/Velocity proxies.

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

## 🎯 Why Use This?

### The Problem
Without NetworkDataAPI, each plugin creates its own database connection:
- Cosmetics Plugin: 10 connections
- Economy Plugin: 10 connections  
- Stats Plugin: 10 connections
- **Total: 30+ database connections!** 😱

### The Solution  
With NetworkDataAPI, all plugins share ONE connection pool:
- **All Plugins → NetworkDataAPI → Max 100 shared connections** 🚀
- Less RAM usage
- Better performance
- Automatic reconnection for ALL plugins
- Shared caching layer

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
# Use the provided build script
build.bat

# Or use Maven directly
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
      <id>astroidmc-releases</id>
      <name>AstroidMC Maven Releases</name>
      <url>https://maven.astroidmc.com/repository/maven-public/</url>
   </repository>
</repositories>

<dependencies>
   <dependency>
      <groupId>com.astroid.stijnjakobs</groupId>
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
        url = uri("https://maven.astroidmc.com/repository/maven-public/")
    }
}

dependencies {
    compileOnly 'com.astroid.stijnjakobs:networkdataapi-core:1.0-SNAPSHOT'
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
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;

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

