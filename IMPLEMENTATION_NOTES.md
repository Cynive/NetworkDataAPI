# NetworkDataAPI - Implementation Notes

**Date:** November 14, 2025  
**Version:** 1.0-SNAPSHOT

---

## ✅ Core Principles

NetworkDataAPI is designed with ONE primary purpose:

**Provide a shared MongoDB connection pool for all plugins on a Minecraft server.**

### What NetworkDataAPI IS:
- ✅ A **shared MongoDB connection pool** manager
- ✅ A **connection layer** between plugins and MongoDB
- ✅ A **high-level API** for common database operations
- ✅ An **automatic reconnection** handler
- ✅ A **caching layer** to reduce database load

### What NetworkDataAPI IS NOT:
- ❌ An automatic player data manager
- ❌ A player tracking system
- ❌ A statistics/economy/cosmetics plugin
- ❌ A system that creates default data

---

## 🎯 Design Philosophy

### 1. **No Automatic Data Creation**

NetworkDataAPI does NOT create any player data automatically. There are no default documents, no automatic player tracking, and no pre-defined data structures.

**Why?**
- Each plugin should control its own data
- Prevents unwanted data in the database
- Avoids conflicts between plugins
- Gives developers complete flexibility

### 2. **Action-Based Data Creation**

Data should only be created when it's actually needed:

```java
// ❌ WRONG: Create empty data on player join
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Document data = new Document("uuid", uuid).append("coins", 0);
    collection.insertOne(data); // Why create empty data?
}

// ✅ CORRECT: Create data when relevant
public void claimCosmetic(Player player, String cosmeticId) {
    Document data = collection.find(Filters.eq("uuid", uuid)).first();
    if (data == null) {
        // First cosmetic - NOW create the document
        data = new Document("uuid", uuid).append("cosmetics", List.of(cosmeticId));
        collection.insertOne(data);
    }
}
```

### 3. **Shared Connection Pool**

All plugins on a server share ONE connection pool:

**Without NetworkDataAPI:**
- Cosmetics Plugin: 10 connections
- Economy Plugin: 10 connections
- Stats Plugin: 10 connections
- **Total: 30 connections per server!**

**With NetworkDataAPI:**
- NetworkDataAPI: 1 shared pool (max 100 connections)
- All plugins use this shared pool
- **Total: Max 100 connections, shared efficiently!**

---

## 📚 API Usage Patterns

### Option 1: Dedicated Database per Plugin (RECOMMENDED)

Each plugin gets its own MongoDB database:

```java
NetworkDataAPIProvider api = APIRegistry.getAPI();

// Cosmetics plugin
MongoDatabase cosmeticsDB = api.getDatabase("cosmetics");
MongoCollection<Document> items = cosmeticsDB.getCollection("player_cosmetics");

// Economy plugin
MongoDatabase economyDB = api.getDatabase("economy");
MongoCollection<Document> balances = economyDB.getCollection("balances");
```

**Benefits:**
- Complete data isolation
- No conflicts possible
- Easier backups per plugin
- Clear separation of concerns

### Option 2: Shared Database with Own Collections

All plugins use the default database but with separate collections:

```java
NetworkDataAPIProvider api = APIRegistry.getAPI();
MongoDatabase db = api.getDatabase(); // Default from config

MongoCollection<Document> cosmetics = db.getCollection("cosmetics");
MongoCollection<Document> economy = db.getCollection("economy");
```

**Use when:**
- Smaller plugins
- Need cross-plugin queries
- Simpler setup

---

## 🔧 Implementation Details

### Removed Features (v1.0)

The following features were **removed** to keep NetworkDataAPI focused on being a connection layer:

1. **Automatic PlayerConnectionListener registration** - Removed
2. **Default player data creation** - Removed
3. **Automatic join/quit tracking** - Removed
4. **Game-specific default fields** (coins, level, experience) - Removed

### What Remains

1. **PlayerDataService** - Optional service for shared player data
2. **Direct MongoDB access** - Full MongoDB API available
3. **Connection pooling** - Shared pool for all plugins
4. **Caching** - Optional caching layer
5. **REST API** - Optional HTTP endpoints

---

## 📖 Documentation Structure

### For End Users:
- **README.md** - Quick overview and installation
- **Installation guide** - Step-by-step setup

### For Plugin Developers:
- **API_DOCUMENTATION.md** - Complete API reference
- **EXAMPLE_PLUGIN_GUIDE.md** - Working example walkthrough
- **Example Plugin** - Full working code in `networkdataapi-example-plugin/`

### For Contributors:
- **CONTRIBUTING.md** - Contribution guidelines
- **Code structure** - Well-documented source code

---

## 🎓 Best Practices

### For Plugin Developers Using NetworkDataAPI:

1. **Always use async operations**
   ```java
   CompletableFuture.supplyAsync(() -> {
       return collection.find(filter).first();
   }).thenAccept(data -> {
       // Process result
   });
   ```

2. **Create indexes for frequently queried fields**
   ```java
   collection.createIndex(Indexes.ascending("uuid"));
   collection.createIndex(Indexes.descending("coins"));
   ```

3. **Use dedicated databases for large plugins**
   ```java
   MongoDatabase myDB = api.getDatabase("my_plugin");
   ```

4. **Handle errors gracefully**
   ```java
   .exceptionally(throwable -> {
       logger.error("Database error", throwable);
       return null;
   });
   ```

5. **Create data only when needed** (action-based)
   - Not on player join
   - Only when player performs relevant action

---

## 🚀 Real-World Example

### Cosmetics Plugin Architecture

```java
public class CosmeticsPlugin extends JavaPlugin {
    private MongoDatabase database;
    private MongoCollection<Document> playerCosmetics;
    
    @Override
    public void onEnable() {
        NetworkDataAPIProvider api = APIRegistry.getAPI();
        
        // Get dedicated database
        database = api.getDatabase("cosmetics");
        playerCosmetics = database.getCollection("player_cosmetics");
        
        // Create indexes
        playerCosmetics.createIndex(Indexes.ascending("uuid"));
    }
    
    // Data created when player claims cosmetic
    public void claimCosmetic(Player player, String cosmeticId) {
        UUID uuid = player.getUniqueId();
        
        Document data = playerCosmetics.find(
            Filters.eq("uuid", uuid.toString())
        ).first();
        
        if (data == null) {
            // First cosmetic - create document NOW
            data = new Document()
                .append("uuid", uuid.toString())
                .append("claimed", List.of(cosmeticId))
                .append("equipped", cosmeticId)
                .append("firstClaim", System.currentTimeMillis());
            
            playerCosmetics.insertOne(data);
        } else {
            // Add to existing
            playerCosmetics.updateOne(
                Filters.eq("uuid", uuid.toString()),
                Updates.addToSet("claimed", cosmeticId)
            );
        }
    }
}
```

---

## 📊 Performance Considerations

### Connection Pool Sizing

Default configuration provides:
- **Min pool size:** 10 connections
- **Max pool size:** 100 connections
- Shared across ALL plugins

**This means:**
- With 5 plugins, each effectively has access to 100 connections
- Much better than 5 × 10 = 50 separate connections
- Automatic load balancing across plugins

### Caching Strategy

NetworkDataAPI includes built-in caching:
- **Default:** 10,000 entries max
- **Expiry:** 5 minutes after write, 10 minutes after access
- **Reduces database load by 80%+**

---

## ✨ Summary

NetworkDataAPI is a **pure connection layer** that:
1. Provides shared MongoDB connection pool
2. Offers high-level API for convenience
3. Handles automatic reconnection
4. Provides optional caching
5. Lets plugins control their own data

**It does NOT:**
1. Track players automatically
2. Create default data
3. Make decisions about data structure
4. Impose any data schema

**Result:** Maximum flexibility + Minimum resource usage

---

*For more information, see the complete API documentation in `API_DOCUMENTATION.md`*

