# Using NetworkDataAPI as a Dependency

This guide explains how to use NetworkDataAPI as a Maven dependency in your own plugins.

## 📦 Adding the Dependency

### Step 1: Add the Repository

Add the AstroidMC Maven repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>astroidmc-releases</id>
        <name>AstroidMC Maven Releases</name>
        <url>https://maven.astroidmc.com/repository/maven-releases/</url>
    </repository>
    <!-- For development/snapshot versions -->
    <repository>
        <id>astroidmc-snapshots</id>
        <name>AstroidMC Maven Snapshots</name>
        <url>https://maven.astroidmc.com/repository/maven-snapshots/</url>
    </repository>
</repositories>
```

### Step 2: Add the Dependency

Add NetworkDataAPI as a dependency:

```xml
<dependencies>
    <!-- NetworkDataAPI Core -->
    <dependency>
        <groupId>com.astroid.stijnjakobs</groupId>
        <artifactId>networkdataapi-core</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Important Notes:**
- Use `scope>provided</scope>` - NetworkDataAPI will be on the server
- The version `1.0-SNAPSHOT` is for development, use `1.0.0` for stable releases
- Only depend on `networkdataapi-core`, NOT the Paper or Bungee modules

### Step 3: Add Plugin Dependency

In your `plugin.yml` (Paper/Spigot):

```yaml
name: YourPlugin
version: 1.0.0
main: com.example.yourplugin.YourPlugin
depend:
  - NetworkDataAPI
```

Or in `bungee.yml` (BungeeCord):

```yaml
name: YourPlugin
version: 1.0.0
main: com.example.yourplugin.YourPlugin
depends:
  - NetworkDataAPI
```

---

## 💻 Using the API

### Basic Setup

```java
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class YourPlugin extends JavaPlugin {
    
    private NetworkDataAPIProvider api;
    private MongoDatabase database;
    
    @Override
    public void onEnable() {
        // Check if NetworkDataAPI is available
        if (!APIRegistry.isAvailable()) {
            getLogger().severe("NetworkDataAPI is not available!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Get the API
        api = APIRegistry.getAPI();
        
        // Get shared database connection
        database = api.getDatabase();
        
        getLogger().info("Successfully connected to NetworkDataAPI!");
    }
}
```

### Using Player Data Service

```java
PlayerDataService playerData = api.getPlayerDataService();

// Get player data
playerData.getPlayerDataAsync(playerUUID).thenAccept(data -> {
    int coins = data.getInteger("coins", 0);
    int level = data.getInteger("level", 1);
    
    // Use the data...
});

// Update player data
playerData.updateFieldAsync(playerUUID, "coins", 1000);

// Increment values
playerData.incrementFieldAsync(playerUUID, "kills", 1);
```

### Using Custom Collections

```java
// Get your own collection
MongoCollection<Document> cosmetics = database.getCollection("cosmetics");

// Insert data
Document cosmetic = new Document("name", "Party Hat")
    .append("price", 1000)
    .append("rarity", "RARE");
cosmetics.insertOne(cosmetic);

// Query data
Document result = cosmetics.find(Filters.eq("name", "Party Hat")).first();

// Update data
cosmetics.updateOne(
    Filters.eq("name", "Party Hat"),
    Updates.set("price", 1500)
);
```

---

## 📚 Available API Classes

### Core API
- `APIRegistry` - Main entry point to get the API
- `NetworkDataAPIProvider` - Interface providing access to services

### Services
- `PlayerDataService` - CRUD operations for player data
  - `getPlayerDataAsync(UUID)` - Get player data
  - `savePlayerDataAsync(UUID, Document)` - Save player data
  - `updateFieldAsync(UUID, String, Object)` - Update specific field
  - `incrementFieldAsync(UUID, String, Number)` - Increment numeric field
  - `queryAsync(Bson, int)` - Query with MongoDB filters

### Database Access
- `MongoDatabase` - Direct MongoDB database access via `api.getDatabase()`
- Create and manage your own collections
- Full MongoDB API available

---

## 🔄 Version Compatibility

| NetworkDataAPI Version | Minecraft Version | Java Version |
|------------------------|-------------------|--------------|
| 1.0.x                  | 1.20+            | 17+          |

---

## 📖 Full Documentation

For complete API documentation, examples, and best practices:

- [API Documentation](https://github.com/YOUR_USERNAME/NetworkDataAPI/blob/main/API_DOCUMENTATION.md)
- [Quick Start Guide](https://github.com/YOUR_USERNAME/NetworkDataAPI/blob/main/QUICK_START.md)
- [Example Plugins](https://github.com/YOUR_USERNAME/NetworkDataAPI/blob/main/COSMETICS_PLUGIN_EXAMPLE.java)

---

## 🐛 Troubleshooting

### "NetworkDataAPI not available"
- Ensure NetworkDataAPI is installed on the server
- Check that NetworkDataAPI loads before your plugin
- Verify `depend` is in your plugin.yml

### "NoClassDefFoundError"
- Ensure you're using `scope>provided</scope>`
- Don't shade NetworkDataAPI into your JAR
- Make sure NetworkDataAPI is on the server

### Version Conflicts
- Always use the same version across all plugins
- Check which version is on the server
- Use `mvn dependency:tree` to check conflicts

---

## 💡 Best Practices

1. **Always check API availability**
   ```java
   if (!APIRegistry.isAvailable()) {
       // Handle missing API
   }
   ```

2. **Use async methods**
   ```java
   // ✅ Good - async
   playerData.getPlayerDataAsync(uuid).thenAccept(...);
   
   // ❌ Avoid - blocks thread
   Document data = playerData.getPlayerData(uuid);
   ```

3. **Handle errors gracefully**
   ```java
   playerData.getPlayerDataAsync(uuid)
       .thenAccept(data -> {
           // Success
       })
       .exceptionally(throwable -> {
           // Handle error
           return null;
       });
   ```

4. **Create your own collections**
   - Don't modify NetworkDataAPI's "players" collection directly
   - Create separate collections for your plugin's data
   - Use descriptive collection names

---

## 🎯 Example: Complete Integration

```java
package com.example.myplugin;

import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    
    private NetworkDataAPIProvider api;
    private PlayerDataService playerData;
    private MongoCollection<Document> myCollection;
    
    @Override
    public void onEnable() {
        // Initialize API
        if (!initializeAPI()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Your plugin logic here
        getLogger().info("MyPlugin enabled with NetworkDataAPI!");
    }
    
    private boolean initializeAPI() {
        if (!APIRegistry.isAvailable()) {
            getLogger().severe("NetworkDataAPI is required but not found!");
            return false;
        }
        
        api = APIRegistry.getAPI();
        
        if (!api.isHealthy()) {
            getLogger().warning("NetworkDataAPI database connection is not healthy!");
        }
        
        // Get services
        playerData = api.getPlayerDataService();
        
        // Get database for custom collections
        MongoDatabase database = api.getDatabase();
        myCollection = database.getCollection("myplugin_data");
        
        return true;
    }
    
    // Example: Get player coins using shared player data
    public void getPlayerCoins(UUID uuid) {
        playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
            int coins = data.getInteger("coins", 0);
            getLogger().info("Player has " + coins + " coins");
        });
    }
    
    // Example: Save plugin-specific data
    public void saveMyData(String key, Object value) {
        Document doc = new Document("key", key)
            .append("value", value)
            .append("timestamp", System.currentTimeMillis());
        myCollection.insertOne(doc);
    }
}
```

---

## 🚀 Ready to Build!

Add the dependency, import the API, and start building! 🎉

For questions or issues, check the [main repository](https://github.com/YOUR_USERNAME/NetworkDataAPI).

