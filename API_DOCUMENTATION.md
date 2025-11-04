# NetworkDataAPI - Complete Developer Documentation

## Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [API Usage](#api-usage)
5. [Common Operations](#common-operations)
6. [Advanced Features](#advanced-features)
7. [REST API](#rest-api)
8. [Best Practices](#best-practices)
9. [Examples](#examples)
10. [Troubleshooting](#troubleshooting)

---

## Overview

NetworkDataAPI is an enterprise-grade data synchronization solution for large Minecraft networks (similar to Hypixel or CubeCraft). It provides a unified MongoDB-backed data layer that works seamlessly across both Paper/Spigot servers and BungeeCord/Velocity proxies.

### Key Features

- **Universal Compatibility**: Works on both Paper/Spigot and BungeeCord with the same API
- **High Performance**: Built-in caching with Caffeine reduces database load by 80%+
- **Thread-Safe**: All operations are thread-safe with async support
- **Connection Pooling**: Configurable MongoDB connection pools for optimal performance
- **Auto-Recovery**: Automatic reconnection and retry logic
- **REST API**: Optional HTTP endpoints for external integrations
- **Production-Ready**: Follows SOLID principles with comprehensive error handling

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Your Plugins                         │
├─────────────────────────────────────────────────────────┤
│              NetworkDataAPI Public API                  │
├──────────────────┬──────────────────┬──────────────────┤
│  Paper Module    │   Core Module    │  Bungee Module   │
├──────────────────┴──────────────────┴──────────────────┤
│                    MongoDB Database                     │
└─────────────────────────────────────────────────────────┘
```

---

## Installation

### Prerequisites

- **Java 17 or higher**
- **MongoDB 4.0 or higher**
- **Paper/Spigot 1.20+ or BungeeCord**

### Step 1: Download

Download the appropriate JAR file:
- For Paper/Spigot: `NetworkDataAPI-Paper-1.0-SNAPSHOT.jar`
- For BungeeCord: `NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar`

### Step 2: Install

Place the JAR file in your `plugins/` folder:

**Paper/Spigot:**
```
server/
└── plugins/
    └── NetworkDataAPI-Paper-1.0-SNAPSHOT.jar
```

**BungeeCord:**
```
proxy/
└── plugins/
    └── NetworkDataAPI-Bungee-1.0-SNAPSHOT.jar
```

### Step 3: First Run

Start your server/proxy. The plugin will create a default configuration file:

```
plugins/NetworkDataAPI/config.yml
```

### Step 4: Configure MongoDB

Edit `config.yml` and set your MongoDB connection details:

```yaml
mongodb:
  uri: "mongodb://localhost:27017"
  database: "minecraft_network"
  username: ""
  password: ""
```

### Step 5: Restart

Restart your server/proxy to apply the configuration.

---

## Configuration

### Complete config.yml Reference

```yaml
# MongoDB Connection Settings
mongodb:
  uri: "mongodb://localhost:27017"
  database: "minecraft_network"
  username: ""
  password: ""
  
  # Connection pool settings
  max-pool-size: 100          # Maximum connections
  min-pool-size: 10           # Minimum connections
  connection-timeout-ms: 10000
  socket-timeout-ms: 5000
  server-selection-timeout-ms: 5000
  max-connection-idle-time-ms: 60000
  max-connection-life-time-ms: 600000

# Cache Settings
cache:
  enabled: true
  max-size: 10000                      # Maximum cached entries
  expire-after-write-minutes: 5        # Expire after write
  expire-after-access-minutes: 10      # Expire after last access

# REST API Settings (optional)
rest-api:
  enabled: false
  port: 8080
  api-key: ""                          # Leave empty to disable auth
  allowed-ips:
    - "127.0.0.1"

# Async Executor Settings
async:
  core-pool-size: 4
  max-pool-size: 16
  keep-alive-seconds: 60

# Logging Settings
logging:
  level: "INFO"                        # TRACE, DEBUG, INFO, WARN, ERROR
  debug: false

# Environment Detection
environment:
  type: "AUTO"                         # AUTO, PAPER, BUNGEECORD
```

---

## API Usage

### Adding NetworkDataAPI as a Dependency

#### In your plugin.yml (Paper/Spigot):

```yaml
name: YourPlugin
version: 1.0
main: com.example.yourplugin.YourPlugin
depend:
  - NetworkDataAPI  # Hard dependency
```

Or use soft dependency:

```yaml
softdepend:
  - NetworkDataAPI  # Optional dependency
```

#### In your bungee.yml (BungeeCord):

```yaml
name: YourPlugin
version: 1.0
main: com.example.yourplugin.YourPlugin
depends:
  - NetworkDataAPI
```

### Getting the API Instance

```java
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;

public class YourPlugin extends JavaPlugin {
    
    private NetworkDataAPIProvider api;
    
    @Override
    public void onEnable() {
        // Check if NetworkDataAPI is available
        if (!APIRegistry.isAvailable()) {
            getLogger().severe("NetworkDataAPI not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Get API instance
        api = APIRegistry.getAPI();
        
        // Verify connection health
        if (!api.isHealthy()) {
            getLogger().warning("NetworkDataAPI database is not healthy!");
        }
        
        getLogger().info("Successfully hooked into NetworkDataAPI v" + api.getVersion());
    }
}
```

---

## Common Operations

### 1. Get Player Data (Async - Recommended)

```java
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import org.bson.Document;

PlayerDataService playerData = api.getPlayerDataService();

// Get player data asynchronously
playerData.getPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
    // This runs asynchronously - safe for database operations
    int coins = data.getInteger("coins", 0);
    int level = data.getInteger("level", 1);
    
    // To update UI, switch back to main thread:
    Bukkit.getScheduler().runTask(plugin, () -> {
        player.sendMessage("You have " + coins + " coins!");
        player.sendMessage("Your level is: " + level);
    });
}).exceptionally(throwable -> {
    getLogger().error("Failed to load player data", throwable);
    return null;
});
```

### 2. Get Player Data (Sync - Use Sparingly)

```java
// Only use this if absolutely necessary (blocks the thread!)
Document data = playerData.getPlayerData(player.getUniqueId());
int coins = data.getInteger("coins", 0);
```

### 3. Save Player Data

```java
// Build the data document
Document playerDocument = new Document()
    .append("coins", 1000)
    .append("level", 5)
    .append("experience", 2500)
    .append("rank", "VIP")
    .append("friends", Arrays.asList("uuid1", "uuid2"))
    .append("settings", new Document()
        .append("chat", true)
        .append("particles", false)
    );

// Save asynchronously (recommended)
playerData.savePlayerDataAsync(player.getUniqueId(), playerDocument)
    .thenRun(() -> {
        player.sendMessage("Data saved successfully!");
    })
    .exceptionally(throwable -> {
        getLogger().error("Failed to save player data", throwable);
        return null;
    });
```

### 4. Update Single Field (Most Efficient)

```java
// Update a single field without loading the entire document
playerData.updateFieldAsync(player.getUniqueId(), "coins", 1500)
    .thenRun(() -> {
        player.sendMessage("Coins updated!");
    });

// Update last seen timestamp
playerData.updateFieldAsync(player.getUniqueId(), "lastSeen", System.currentTimeMillis());
```

### 5. Update Multiple Fields

```java
Map<String, Object> updates = new HashMap<>();
updates.put("coins", 2000);
updates.put("level", 6);
updates.put("experience", 3000);

playerData.updateFieldsAsync(player.getUniqueId(), updates)
    .thenRun(() -> {
        player.sendMessage("Profile updated!");
    });
```

### 6. Increment/Decrement Values

```java
// Add 100 coins
playerData.incrementFieldAsync(player.getUniqueId(), "coins", 100);

// Remove 50 coins (negative increment)
playerData.incrementFieldAsync(player.getUniqueId(), "coins", -50);

// Increment kills
playerData.incrementFieldAsync(player.getUniqueId(), "kills", 1);
```

### 7. Check if Player Data Exists

```java
playerData.existsAsync(playerUUID).thenAccept(exists -> {
    if (exists) {
        // Player has data
    } else {
        // New player
    }
});
```

### 8. Delete Player Data

```java
playerData.deletePlayerDataAsync(player.getUniqueId())
    .thenAccept(deleted -> {
        if (deleted) {
            player.sendMessage("Your data has been deleted.");
        } else {
            player.sendMessage("No data found to delete.");
        }
    });
```

---

## Advanced Features

### Query Player Data with Filters

```java
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;

// Find all players with more than 1000 coins
Bson filter = Filters.gt("coins", 1000);
playerData.queryAsync(filter, 10).thenAccept(results -> {
    results.forEach(doc -> {
        String name = doc.getString("lastKnownName");
        int coins = doc.getInteger("coins", 0);
        System.out.println(name + " has " + coins + " coins");
    });
});

// Complex query: VIP players who logged in recently
Bson complexFilter = Filters.and(
    Filters.eq("rank", "VIP"),
    Filters.gte("lastLogin", System.currentTimeMillis() - 86400000) // Last 24h
);
playerData.queryAsync(complexFilter, 50).thenAccept(results -> {
    // Process results
});
```

### Custom Collections

While `PlayerDataService` handles player data, you can easily create your own collections for ANY data type (cosmetics, guilds, punishments, etc.) using the **shared database connection**:

```java
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// Get direct access to the shared database connection
MongoDatabase database = api.getDatabase();

// Create your own collection (for example for cosmetics)
MongoCollection<Document> cosmetics = database.getCollection("cosmetics");

// Perform operations on your custom collection
Document cosmetic = new Document("_id", "party_hat")
    .append("name", "Party Hat")
    .append("type", "HAT")
    .append("rarity", "RARE")
    .append("price", 1000);

cosmetics.insertOne(cosmetic);

// Create another collection for player cosmetics
MongoCollection<Document> playerCosmetics = database.getCollection("player_cosmetics");

Document playerCosmeticData = new Document("_id", playerUUID.toString())
    .append("owned", Arrays.asList("party_hat", "crown", "santa_hat"))
    .append("equipped", new Document()
        .append("hat", "party_hat")
        .append("trail", "hearts")
    );

playerCosmetics.insertOne(playerCosmeticData);
```

**Benefits of this approach:**
- ✅ **No separate database connection** needed in your plugin
- ✅ **Uses the shared connection pool** (max 100 connections)
- ✅ **Automatic reconnection** and error handling
- ✅ **Less resource usage** - all plugins share the same pool
- ✅ **Simple setup** - one line of code: `api.getDatabase()`
- ✅ **Full MongoDB API** - all operations available

### Custom Databases per Plugin

**New in v1.0!** Each plugin can now have its **own MongoDB database** for **complete isolation**:

```java
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// Each plugin gets its own database
MongoDatabase cosmeticsDB = api.getDatabase("cosmetics_plugin");
MongoDatabase guildsDB = api.getDatabase("guilds_plugin");
MongoDatabase punishmentsDB = api.getDatabase("punishments_plugin");

// Work with your own database - 100% isolated!
MongoCollection<Document> items = cosmeticsDB.getCollection("items");
MongoCollection<Document> purchases = cosmeticsDB.getCollection("purchases");

// No conflicts with other plugins possible!
items.insertOne(new Document("name", "Crown").append("price", 5000));
```

**When to use a separate database?**
- ✅ **Large plugins** with lots of data (1M+ documents)
- ✅ **Complete isolation** from other plugins
- ✅ **Own backup schema** per plugin
- ✅ **Different replication settings**
- ✅ **Easier data management** and migrations
- ✅ **Separate monitoring** per plugin

**When to use the same database with separate collections?**
- ✅ **Small to medium-sized plugins**
- ✅ **Cross-plugin queries** needed
- ✅ **Simpler setup**

**Complete example: Cosmetics Plugin with its own database**

```java
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CosmeticsPlugin extends JavaPlugin {
    
    private NetworkDataAPIProvider api;
    private MongoDatabase database;
    private MongoCollection<Document> itemsCollection;
    private MongoCollection<Document> playerCosmeticsCollection;
    
    @Override
    public void onEnable() {
        // Hook into NetworkDataAPI
        api = APIRegistry.getAPI();
        
        // Get your own dedicated database
        database = api.getDatabase("cosmetics_plugin");
        
        // Initialize collections
        itemsCollection = database.getCollection("items");
        playerCosmeticsCollection = database.getCollection("player_cosmetics");
        
        // Create indexes for better performance
        createIndexes();
        
        // Load cosmetic items from database
        loadCosmeticItems();
        
        getLogger().info("Cosmetics Plugin using dedicated database: cosmetics_plugin");
    }
    
    private void createIndexes() {
        // Index on item type for faster queries
        itemsCollection.createIndex(new Document("type", 1));
        
        // Index on rarity for faster filtering
        itemsCollection.createIndex(new Document("rarity", 1));
        
        // Compound index for player queries
        playerCosmeticsCollection.createIndex(
            new Document("uuid", 1).append("equipped", 1)
        );
    }
    
    private void loadCosmeticItems() {
        // Count items
        long itemCount = itemsCollection.countDocuments();
        
        if (itemCount == 0) {
            getLogger().info("No cosmetic items found. Creating defaults...");
            createDefaultItems();
        } else {
            getLogger().info("Loaded " + itemCount + " cosmetic items");
        }
    }
    
    private void createDefaultItems() {
        // Create default cosmetic items
        List<Document> defaultItems = Arrays.asList(
            new Document("_id", "party_hat")
                .append("name", "Party Hat")
                .append("type", "HAT")
                .append("rarity", "RARE")
                .append("price", 1000),
            
            new Document("_id", "crown")
                .append("name", "Royal Crown")
                .append("type", "HAT")
                .append("rarity", "LEGENDARY")
                .append("price", 5000),
            
            new Document("_id", "hearts_trail")
                .append("name", "Hearts Trail")
                .append("type", "TRAIL")
                .append("rarity", "UNCOMMON")
                .append("price", 500)
        );
        
        itemsCollection.insertMany(defaultItems);
        getLogger().info("Created " + defaultItems.size() + " default items");
    }
    
    // API Methods for your plugin
    
    public CompletableFuture<List<Document>> getPlayerCosmetics(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            Document playerData = playerCosmeticsCollection.find(
                Filters.eq("_id", playerUUID.toString())
            ).first();
            
            if (playerData == null) {
                return Collections.emptyList();
            }
            
            return playerData.getList("owned", String.class).stream()
                .map(itemId -> itemsCollection.find(Filters.eq("_id", itemId)).first())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        });
    }
    
    public CompletableFuture<Boolean> purchaseCosmetic(UUID playerUUID, String itemId) {
        return CompletableFuture.supplyAsync(() -> {
            // Get item
            Document item = itemsCollection.find(Filters.eq("_id", itemId)).first();
            if (item == null) return false;
            
            int price = item.getInteger("price", 0);
            
            // Check if player can afford it (using NetworkDataAPI player data)
            Document playerData = api.getPlayerDataService().getPlayerData(playerUUID);
            int coins = playerData.getInteger("coins", 0);
            
            if (coins < price) return false;
            
            // Deduct coins
            api.getPlayerDataService().incrementField(playerUUID, "coins", -price);
            
            // Add cosmetic to player
            playerCosmeticsCollection.updateOne(
                Filters.eq("_id", playerUUID.toString()),
                Updates.addToSet("owned", itemId),
                new UpdateOptions().upsert(true)
            );
            
            return true;
        });
    }
    
    public CompletableFuture<Void> equipCosmetic(UUID playerUUID, String itemId) {
        return CompletableFuture.runAsync(() -> {
            // Get item type
            Document item = itemsCollection.find(Filters.eq("_id", itemId)).first();
            if (item == null) return;
            
            String type = item.getString("type");
            
            // Equip the cosmetic
            playerCosmeticsCollection.updateOne(
                Filters.eq("_id", playerUUID.toString()),
                Updates.set("equipped." + type.toLowerCase(), itemId),
                new UpdateOptions().upsert(true)
            );
        });
    }
}
```

**Benefits of own database per plugin:**
- ✅ **Complete isolation** - no conflicts possible
- ✅ **Own backup strategy** per plugin
- ✅ **Better organization** for large datasets
- ✅ **Separate monitoring** and performance tuning
- ✅ **Uses the same connection pool** - efficient!
- ✅ **No extra configuration** - works out-of-the-box

**Database Management Best Practices:**

```java
// ❌ WRONG: Creating your own MongoClient
MongoClient myOwnClient = MongoClients.create("mongodb://localhost:27017");
// This wastes resources and connections!

// ✅ CORRECT: Use NetworkDataAPI's connection
MongoDatabase myDB = api.getDatabase("my_plugin");
// Uses the shared, configured connection pool!
```

**Example: Guild Plugin with its own database**

```java
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GuildsPlugin extends JavaPlugin {
    
    private MongoDatabase guildsDatabase;
    private MongoCollection<Document> guildsCollection;
    private MongoCollection<Document> guildMembersCollection;
    
    @Override
    public void onEnable() {
        NetworkDataAPIProvider api = APIRegistry.getAPI();
        
        // Dedicated database for guilds
        guildsDatabase = api.getDatabase("guilds_plugin");
        
        guildsCollection = guildsDatabase.getCollection("guilds");
        guildMembersCollection = guildsDatabase.getCollection("guild_members");
        
        // Create indexes
        guildsCollection.createIndex(new Document("name", 1));
        guildsCollection.createIndex(new Document("level", -1));
        guildMembersCollection.createIndex(new Document("guildId", 1));
    }
    
    public void createGuild(String guildName, UUID ownerUUID) {
        Document guild = new Document()
            .append("name", guildName)
            .append("owner", ownerUUID.toString())
            .append("level", 1)
            .append("members", 1)
            .append("createdAt", System.currentTimeMillis())
            .append("bankBalance", 0);
        
        guildsCollection.insertOne(guild);
        
        // Add owner as member
        Document member = new Document()
            .append("uuid", ownerUUID.toString())
            .append("guildId", guild.getObjectId("_id"))
            .append("rank", "OWNER")
            .append("joinedAt", System.currentTimeMillis());
        
        guildMembersCollection.insertOne(member);
    }
}
```

**Complete example for a Cosmetics Plugin:**
See `COSMETICS_PLUGIN_EXAMPLE.java` in the repository for a fully working example!

### Working with Nested Documents

```java
// Save nested structure
Document settings = new Document()
    .append("notifications", new Document()
        .append("chat", true)
        .append("friend_requests", false)
    )
    .append("privacy", new Document()
        .append("show_online", true)
        .append("allow_messages", true)
    );

playerData.updateFieldAsync(playerUUID, "settings", settings);

// Read nested value
playerData.getPlayerDataAsync(playerUUID).thenAccept(data -> {
    Document settingsDoc = (Document) data.get("settings");
    if (settingsDoc != null) {
        Document notifs = (Document) settingsDoc.get("notifications");
        boolean chatNotifs = notifs.getBoolean("chat", true);
    }
});

// Update nested field using dot notation
playerData.updateFieldAsync(playerUUID, "settings.notifications.chat", false);
```

---

## REST API

### Enabling the REST API

In `config.yml`:

```yaml
rest-api:
  enabled: true
  port: 8080
  api-key: "your-secret-key-here"
  allowed-ips:
    - "127.0.0.1"
    - "10.0.0.0/24"
```

### Available Endpoints

#### 1. Health Check
```bash
GET /api/health

Response:
{
  "status": "healthy",
  "timestamp": 1699123456789
}
```

#### 2. Get Player Data
```bash
GET /api/player/{uuid}
Headers:
  X-API-Key: your-secret-key-here

Response:
{
  "_id": "uuid-here",
  "coins": 1000,
  "level": 5,
  "lastLogin": 1699123456789
}
```

#### 3. Update Player Data
```bash
POST /api/player/{uuid}
Headers:
  X-API-Key: your-secret-key-here
  Content-Type: application/json
  
Body:
{
  "coins": 1500,
  "level": 6
}

Response:
{
  "message": "Player data updated successfully"
}
```

#### 4. Delete Player Data
```bash
DELETE /api/player/{uuid}
Headers:
  X-API-Key: your-secret-key-here

Response:
{
  "message": "Player data deleted successfully"
}
```

#### 5. API Statistics
```bash
GET /api/stats
Headers:
  X-API-Key: your-secret-key-here

Response:
{
  "running": true,
  "timestamp": 1699123456789
}
```

### Example: External Integration with Python

```python
import requests

API_URL = "http://localhost:8080/api"
API_KEY = "your-secret-key-here"

def get_player_data(uuid):
    headers = {"X-API-Key": API_KEY}
    response = requests.get(f"{API_URL}/player/{uuid}", headers=headers)
    return response.json()

def update_player_coins(uuid, coins):
    headers = {
        "X-API-Key": API_KEY,
        "Content-Type": "application/json"
    }
    data = {"coins": coins}
    response = requests.post(f"{API_URL}/player/{uuid}", headers=headers, json=data)
    return response.json()

# Usage
player_uuid = "123e4567-e89b-12d3-a456-426614174000"
data = get_player_data(player_uuid)
print(f"Player has {data['coins']} coins")

update_player_coins(player_uuid, 2000)
```

---

## Best Practices

### 1. Always Use Async Methods

```java
// ❌ BAD: Blocks main thread
Document data = playerData.getPlayerData(uuid);

// ✅ GOOD: Non-blocking
playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
    // Process data
});
```

### 2. Handle Errors Properly

```java
playerData.getPlayerDataAsync(uuid)
    .thenAccept(data -> {
        // Success handler
    })
    .exceptionally(throwable -> {
        // Error handler
        getLogger().error("Failed to load data", throwable);
        player.sendMessage("Failed to load your data. Please try again.");
        return null;
    });
```

### 3. Use Field Updates for Small Changes

```java
// ❌ BAD: Loads entire document just to update one field
Document data = playerData.getPlayerData(uuid);
data.put("coins", 1000);
playerData.savePlayerData(uuid, data);

// ✅ GOOD: Updates only the field
playerData.updateFieldAsync(uuid, "coins", 1000);
```

### 4. Cache Frequently Accessed Data

The plugin automatically caches player data, but you can also implement your own caching:

```java
private final Map<UUID, PlayerProfile> profileCache = new ConcurrentHashMap<>();

public CompletableFuture<PlayerProfile> getProfile(UUID uuid) {
    // Check local cache first
    if (profileCache.containsKey(uuid)) {
        return CompletableFuture.completedFuture(profileCache.get(uuid));
    }
    
    // Load from API (which uses its own cache)
    return playerData.getPlayerDataAsync(uuid).thenApply(data -> {
        PlayerProfile profile = new PlayerProfile(data);
        profileCache.put(uuid, profile);
        return profile;
    });
}
```

### 5. Batch Operations

```java
// Update multiple players efficiently
List<UUID> players = Arrays.asList(uuid1, uuid2, uuid3);

List<CompletableFuture<Void>> futures = players.stream()
    .map(uuid -> playerData.updateFieldAsync(uuid, "event_participated", true))
    .collect(Collectors.toList());

// Wait for all updates to complete
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .thenRun(() -> {
        getLogger().info("All players updated!");
    });
```

### 6. Graceful Shutdown

```java
@Override
public void onDisable() {
    // Save any pending data
    onlinePlayers.forEach(player -> {
        Document data = buildPlayerData(player);
        // Use synchronous save on shutdown
        playerData.savePlayerData(player.getUniqueId(), data);
    });
}
```

---

## Examples

### Example 1: Economy Plugin Integration

```java
public class EconomyManager {
    private final PlayerDataService playerData;
    
    public EconomyManager(NetworkDataAPIProvider api) {
        this.playerData = api.getPlayerDataService();
    }
    
    public CompletableFuture<Integer> getBalance(UUID uuid) {
        return playerData.getPlayerDataAsync(uuid)
            .thenApply(data -> data.getInteger("coins", 0));
    }
    
    public CompletableFuture<Boolean> addCoins(UUID uuid, int amount) {
        return playerData.incrementFieldAsync(uuid, "coins", amount)
            .thenApply(v -> true)
            .exceptionally(throwable -> false);
    }
    
    public CompletableFuture<Boolean> removeCoins(UUID uuid, int amount) {
        return getBalance(uuid).thenCompose(balance -> {
            if (balance < amount) {
                return CompletableFuture.completedFuture(false);
            }
            return playerData.incrementFieldAsync(uuid, "coins", -amount)
                .thenApply(v -> true);
        });
    }
    
    public CompletableFuture<Boolean> setBalance(UUID uuid, int amount) {
        return playerData.updateFieldAsync(uuid, "coins", amount)
            .thenApply(v -> true)
            .exceptionally(throwable -> false);
    }
}
```

### Example 2: Stats Tracking

```java
public class StatsManager {
    private final PlayerDataService playerData;
    
    public void recordKill(UUID killer, UUID victim) {
        // Increment killer's kills
        playerData.incrementFieldAsync(killer, "stats.kills", 1);
        
        // Increment victim's deaths
        playerData.incrementFieldAsync(victim, "stats.deaths", 1);
        
        // Update kill/death ratio
        playerData.getPlayerDataAsync(killer).thenAccept(data -> {
            Document stats = (Document) data.get("stats");
            int kills = stats.getInteger("kills", 0);
            int deaths = stats.getInteger("deaths", 0);
            double kd = deaths > 0 ? (double) kills / deaths : kills;
            
            playerData.updateFieldAsync(killer, "stats.kd_ratio", kd);
        });
    }
    
    public CompletableFuture<Document> getStats(UUID uuid) {
        return playerData.getPlayerDataAsync(uuid)
            .thenApply(data -> (Document) data.get("stats"));
    }
}
```

### Example 3: Cross-Server Messaging

```java
public class CrossServerMessaging {
    
    // On Server A: Player sends message
    public void sendMessage(UUID from, UUID to, String message) {
        Document messageDoc = new Document()
            .append("from", from.toString())
            .append("message", message)
            .append("timestamp", System.currentTimeMillis())
            .append("read", false);
        
        playerData.getPlayerDataAsync(to).thenAccept(data -> {
            List<Document> messages = (List<Document>) data.get("messages");
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.add(messageDoc);
            
            playerData.updateFieldAsync(to, "messages", messages);
        });
    }
    
    // On Server B: Player receives messages
    public void checkMessages(UUID player) {
        playerData.getPlayerDataAsync(player).thenAccept(data -> {
            List<Document> messages = (List<Document>) data.get("messages");
            if (messages != null) {
                messages.stream()
                    .filter(msg -> !msg.getBoolean("read", false))
                    .forEach(msg -> {
                        // Display message to player
                        String from = msg.getString("from");
                        String text = msg.getString("message");
                        // Send to player...
                    });
            }
        });
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. "NetworkDataAPI not found!"

**Problem**: Your plugin can't find NetworkDataAPI.

**Solution**:
- Ensure NetworkDataAPI is installed in the plugins folder
- Check that you've added `depend` or `softdepend` in plugin.yml
- Verify NetworkDataAPI loads before your plugin (check startup logs)

#### 2. "Database connection failed"

**Problem**: Can't connect to MongoDB.

**Solutions**:
- Check MongoDB is running: `mongo --eval "db.adminCommand('ping')"`
- Verify connection string in config.yml
- Check firewall settings
- Test connectivity: `telnet mongodb-host 27017`

#### 3. "Async operation timeout"

**Problem**: Operations taking too long.

**Solutions**:
- Increase timeout values in config.yml
- Check MongoDB performance
- Review query indexes
- Monitor network latency

#### 4. High memory usage

**Problem**: Plugin using too much RAM.

**Solutions**:
- Reduce `cache.max-size` in config.yml
- Decrease `cache.expire-after-write-minutes`
- Check for memory leaks in your code

#### 5. Cache hit rate is low

**Problem**: Cache not effective.

**Solutions**:
- Increase `cache.max-size`
- Increase `cache.expire-after-access-minutes`
- Review data access patterns

### Debug Mode

Enable debug logging in `config.yml`:

```yaml
logging:
  level: "DEBUG"
  debug: true
```

Then check logs for detailed information:
- Paper/Spigot: `logs/latest.log`
- BungeeCord: `proxy.log.0`

### Performance Monitoring

Use the admin command to monitor performance:

```
/networkdataapi status
/networkdataapi cache stats
```

### Getting Help

1. Check logs for error messages
2. Enable debug mode
3. Review this documentation
4. Open an issue on GitHub with:
   - Full error logs
   - Configuration file
   - Steps to reproduce

---

## Version History

### 1.0-SNAPSHOT (Current)
- Initial release
- Multi-platform support (Paper + BungeeCord)
- MongoDB integration with connection pooling
- Caffeine caching
- REST API
- Full async support

---

## License

This project is licensed under the MIT License.

## Credits

**Author**: Stijn Jakobs
**Architecture**: Enterprise-grade, production-ready
**Supported Platforms**: Paper, Spigot, BungeeCord

---

*For questions or support, please open an issue on GitHub.*

