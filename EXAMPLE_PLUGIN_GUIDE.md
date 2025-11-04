# Example Plugin Guide

## Overview

The `networkdataapi-example-plugin` module demonstrates how to create a minimal plugin that leverages the NetworkDataAPI shared MongoDB connection to create and manage its own database collections.

## What It Demonstrates

This example plugin showcases the following capabilities:

### 1. **Shared MongoDB Connection**
   - Uses NetworkDataAPI's existing MongoDB connection
   - No need to create a separate database connection
   - Automatically benefits from connection pooling and retry logic

### 2. **Isolated Database**
   - Creates its own dedicated MongoDB database (`example_plugin`)
   - Complete data isolation from other plugins
   - No conflicts with other plugin data

### 3. **Custom Collections**
   - Creates a sample collection (`example_collection`)
   - Stores documents with fields: `name`, `value`, `timestamp`, `updated`
   - Demonstrates proper collection initialization

### 4. **CRUD Operations**
   - **Create**: Insert new documents
   - **Read**: Query documents by name, value, or get all
   - **Update**: Modify existing documents
   - **Delete**: Remove documents

### 5. **Performance Optimization**
   - Creates indexes on frequently queried fields
   - Uses compound indexes for complex queries
   - Demonstrates MongoDB best practices

### 6. **Comprehensive Logging**
   - All operations are logged with detailed information
   - Easy to debug and understand what's happening
   - Helps developers learn MongoDB operations

## Directory Structure

```
networkdataapi-example-plugin/
├── src/main/java/com/astroid/stijnjakobs/networkdataapi/example/
│   ├── ExamplePlugin.java          # Main plugin class
│   ├── ExampleDataManager.java     # MongoDB operations handler
│   └── ExampleCommand.java         # Command handler for testing
├── src/main/resources/
│   └── plugin.yml                  # Plugin configuration
├── pom.xml                         # Maven build configuration
└── README.md                       # Plugin documentation
```

## Key Classes

### ExamplePlugin.java
The main plugin class that:
- Checks for NetworkDataAPI availability
- Gets the API instance from APIRegistry
- Creates an isolated database using `api.getDatabase("example_plugin")`
- Initializes the data manager
- Registers commands

**Key Code:**
```java
// Get API instance
NetworkDataAPIProvider api = APIRegistry.getAPI();

// Get dedicated database for this plugin
MongoDatabase database = api.getDatabase("example_plugin");
```

### ExampleDataManager.java
Manages all MongoDB operations:
- Collection initialization
- Index creation for performance
- Insert, query, update, and delete operations
- Statistics retrieval
- Comprehensive operation logging

**Key Features:**
- Creates indexes on `name` and `value` fields
- Demonstrates MongoDB filter operations
- Shows update operations with multiple fields
- Includes error handling and logging

### ExampleCommand.java
In-game command handler that provides:
- `/example insert <name> <value>` - Insert a document
- `/example query <name>` - Query by name
- `/example queryall` - Query all documents
- `/example queryvalue <min>` - Query by value
- `/example update <name> <value>` - Update a document
- `/example delete <name>` - Delete a document
- `/example stats` - Show collection statistics

## Building the Example Plugin

### Prerequisites
- Maven 3.6+
- Java 17+
- Network access to Maven repositories (Spigot/Paper API)

### Build Command
```bash
cd networkdataapi-example-plugin
mvn clean package
```

The compiled JAR will be located at:
```
networkdataapi-example-plugin/target/NetworkDataAPI-Example-1.0-SNAPSHOT.jar
```

## Installation

1. **Install NetworkDataAPI** first (prerequisite)
2. **Place** `NetworkDataAPI-Example-1.0-SNAPSHOT.jar` in your `plugins/` folder
3. **Start** your server
4. **Use** `/example help` to see available commands

## Usage Examples

### Insert Sample Data
```
/example insert apple 100
/example insert banana 200
/example insert cherry 50
/example insert orange 150
```

### Query Data
```
# Query by name
/example query apple

# Query all documents
/example queryall

# Query documents with value > 100
/example queryvalue 100
```

### Update Data
```
/example update apple 250
```

### Delete Data
```
/example delete cherry
```

### View Statistics
```
/example stats
```

## Learning Points

### 1. How to Get the NetworkDataAPI Instance
```java
import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;

// In your plugin's onEnable()
if (!APIRegistry.isAvailable()) {
    getLogger().severe("NetworkDataAPI not found!");
    getServer().getPluginManager().disablePlugin(this);
    return;
}

NetworkDataAPIProvider api = APIRegistry.getAPI();
```

### 2. How to Get Your Own Database
```java
// Get a dedicated database for your plugin
MongoDatabase database = api.getDatabase("your_plugin_name");
```

### 3. How to Create Collections
```java
MongoCollection<Document> collection = database.getCollection("your_collection");
```

### 4. How to Create Indexes
```java
import com.mongodb.client.model.Indexes;

// Single field index
collection.createIndex(Indexes.ascending("fieldName"));

// Compound index
collection.createIndex(Indexes.ascending("field1", "field2"));
```

### 5. How to Insert Documents
```java
import org.bson.Document;

Document doc = new Document()
    .append("name", "example")
    .append("value", 100)
    .append("timestamp", System.currentTimeMillis());

collection.insertOne(doc);
```

### 6. How to Query Documents
```java
import com.mongodb.client.model.Filters;

// Query by exact match
Bson filter = Filters.eq("name", "example");
List<Document> results = new ArrayList<>();
collection.find(filter).into(results);

// Query with comparison
Bson filter = Filters.gt("value", 50);
collection.find(filter).into(results);
```

### 7. How to Update Documents
```java
import com.mongodb.client.model.Updates;

Bson filter = Filters.eq("name", "example");
Bson update = Updates.combine(
    Updates.set("value", 200),
    Updates.set("updated", true),
    Updates.set("lastModified", System.currentTimeMillis())
);

collection.updateOne(filter, update);
```

### 8. How to Delete Documents
```java
Bson filter = Filters.eq("name", "example");
collection.deleteOne(filter);
```

## Benefits of This Approach

### ✅ No Separate Database Connection
Your plugin doesn't need its own MongoDB connection. Just use NetworkDataAPI's shared connection.

### ✅ Automatic Connection Management
NetworkDataAPI handles:
- Connection pooling
- Automatic reconnection
- Error retry logic
- Resource cleanup

### ✅ Complete Data Isolation
Each plugin can have its own database, preventing conflicts.

### ✅ Full MongoDB API Access
Use all MongoDB operations without restrictions.

### ✅ Easy Setup
Just one line of code to get started: `api.getDatabase("your_plugin_name")`

### ✅ Better Resource Usage
All plugins share the same connection pool, reducing overhead.

## Use Cases

This pattern is perfect for:
- **Cosmetics Plugins**: Store owned cosmetics, equipped items
- **Economy Plugins**: Store player balances, transactions
- **Guilds/Clans Plugins**: Store guild data, members, ranks
- **Stats Plugins**: Store player statistics, achievements
- **Punishment Systems**: Store bans, mutes, warnings
- **Custom Game Modes**: Store game data, player progress

## Next Steps

1. **Study the Code**: Review each class to understand the implementation
2. **Test It**: Run the commands in-game and watch the server logs
3. **Customize It**: Adapt the code for your own plugin's needs
4. **Build Your Plugin**: Use this as a template for your own database operations

## Support

- Read the main [API_DOCUMENTATION.md](../API_DOCUMENTATION.md)
- Review the example plugin's [README.md](networkdataapi-example-plugin/README.md)
- Check the code comments for detailed explanations
- Open an issue on GitHub for questions

---

**This example plugin is a complete, working reference for building plugins that use NetworkDataAPI!**
