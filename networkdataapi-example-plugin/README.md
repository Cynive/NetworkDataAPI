# NetworkDataAPI Example Plugin

This is a minimal example plugin demonstrating how to leverage the NetworkDataAPI shared MongoDB connection to create and manage custom database collections.

## Features

- ✅ Uses NetworkDataAPI's shared MongoDB connection (no separate connection needed)
- ✅ Creates an isolated MongoDB database (`example_plugin`) for plugin data
- ✅ Includes a sample collection (`example_collection`) with basic fields
- ✅ Demonstrates all CRUD operations (Create, Read, Update, Delete)
- ✅ Comprehensive logging for easy debugging
- ✅ Indexes created for optimal query performance
- ✅ In-game commands for testing all operations

## Requirements

- **NetworkDataAPI** plugin installed and configured
- **Paper/Spigot** 1.20+ server
- **MongoDB** 4.0+ (configured in NetworkDataAPI)

## Installation

1. Ensure NetworkDataAPI is installed and configured
2. Place `NetworkDataAPI-Example-1.0-SNAPSHOT.jar` in your `plugins/` folder
3. Restart your server
4. Use `/example help` to see available commands

## Commands

All commands require the `networkdataapi.example.use` permission (default: op).

| Command | Description |
|---------|-------------|
| `/example insert <name> <value>` | Insert a new document |
| `/example query <name>` | Query documents by name |
| `/example queryall` | Query all documents in collection |
| `/example queryvalue <min>` | Query documents with value > min |
| `/example update <name> <newValue>` | Update a document's value |
| `/example delete <name>` | Delete a document by name |
| `/example stats` | Show collection statistics |
| `/example help` | Show help message |

## Example Usage

```bash
# Insert some sample data
/example insert apple 100
/example insert banana 200
/example insert cherry 50

# Query all documents
/example queryall

# Query by name
/example query apple

# Query by value (find all with value > 75)
/example queryvalue 75

# Update a document
/example update apple 150

# Delete a document
/example delete cherry

# View statistics
/example stats
```

## Database Structure

### Database: `example_plugin`
An isolated MongoDB database created specifically for this plugin.

### Collection: `example_collection`
Stores example documents with the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Auto-generated MongoDB ID |
| `name` | String | Name field (indexed) |
| `value` | Integer | Value field (indexed) |
| `timestamp` | Long | Creation timestamp |
| `updated` | Boolean | Whether document has been updated |
| `lastModified` | Long | Last modification timestamp (optional) |

### Indexes

For optimal query performance, the following indexes are created:
- Single field index on `name`
- Single field index on `value`
- Compound index on `name` and `value`

## Code Structure

```
com.astroid.stijnjakobs.networkdataapi.example/
├── ExamplePlugin.java          # Main plugin class
├── ExampleDataManager.java     # MongoDB operations handler
└── ExampleCommand.java         # Command handler
```

### ExamplePlugin.java
Main plugin class that:
- Checks for NetworkDataAPI availability
- Gets the shared MongoDB connection
- Creates an isolated database for the plugin
- Initializes the data manager
- Registers commands

### ExampleDataManager.java
Handles all MongoDB operations:
- Collection initialization
- Index creation
- Insert operations
- Query operations (by name, by value, all)
- Update operations
- Delete operations
- Statistics retrieval
- Comprehensive logging

### ExampleCommand.java
Command handler that:
- Provides in-game commands for testing
- Validates user input
- Displays operation results
- Provides tab completion

## Key Concepts Demonstrated

### 1. Getting the Shared Connection

```java
// Get NetworkDataAPI instance
NetworkDataAPIProvider api = APIRegistry.getAPI();

// Get dedicated database for this plugin
MongoDatabase database = api.getDatabase("example_plugin");
```

### 2. Creating Collections

```java
// Get or create a collection
MongoCollection<Document> collection = database.getCollection("example_collection");
```

### 3. Creating Indexes

```java
// Single field index
collection.createIndex(Indexes.ascending("name"));

// Compound index
collection.createIndex(Indexes.ascending("name", "value"));
```

### 4. Insert Operations

```java
Document document = new Document()
    .append("name", "example")
    .append("value", 100)
    .append("timestamp", System.currentTimeMillis());

InsertOneResult result = collection.insertOne(document);
```

### 5. Query Operations

```java
// Query by filter
Bson filter = Filters.eq("name", "example");
List<Document> results = new ArrayList<>();
collection.find(filter).into(results);

// Query with comparison
Bson filter = Filters.gt("value", 50);
collection.find(filter).into(results);
```

### 6. Update Operations

```java
Bson filter = Filters.eq("name", "example");
Bson update = Updates.combine(
    Updates.set("value", 200),
    Updates.set("updated", true),
    Updates.set("lastModified", System.currentTimeMillis())
);

UpdateResult result = collection.updateOne(filter, update);
```

### 7. Delete Operations

```java
Bson filter = Filters.eq("name", "example");
DeleteResult result = collection.deleteOne(filter);
```

## Benefits of Using NetworkDataAPI

1. **No Separate Connection**: Uses the shared connection pool - efficient and reduces overhead
2. **Automatic Connection Management**: NetworkDataAPI handles reconnection and error recovery
3. **Isolated Database**: Each plugin can have its own database for complete data isolation
4. **Easy Setup**: Just one line of code to get started: `api.getDatabase("your_plugin_name")`
5. **Full MongoDB API**: Access to all MongoDB operations without restrictions
6. **Thread-Safe**: The MongoDB driver handles concurrency automatically

## Logging

All database operations are logged to the server console for easy debugging:

```
[ExamplePlugin] ========================================
[ExamplePlugin] INSERT OPERATION
[ExamplePlugin] ========================================
[ExamplePlugin] Creating document: {"name":"apple","value":100,"timestamp":1699123456789,"updated":false}
[ExamplePlugin] Insert successful!
[ExamplePlugin] Inserted ID: BsonObjectId{value=...}
[ExamplePlugin] Document inserted into collection 'example_collection'
[ExamplePlugin] ========================================
```

## Customization

To adapt this example for your own plugin:

1. Change the database name from `example_plugin` to your plugin name
2. Modify the collection schema to fit your data structure
3. Add your own fields and indexes
4. Implement your own business logic
5. Update commands and permissions

## License

This example plugin is licensed under the MIT License, same as NetworkDataAPI.

## Support

For questions or issues:
- Check the main NetworkDataAPI documentation
- Review the code comments in the example plugin
- Open an issue on the GitHub repository

---

**Built with ❤️ as an educational resource for the Minecraft plugin development community**
