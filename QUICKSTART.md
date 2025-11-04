# Quick Start Guide - Example Plugin

## 🚀 5-Minute Quick Start

### Prerequisites
- NetworkDataAPI installed and configured
- Paper/Spigot server (1.20+)
- MongoDB running and accessible

### Step 1: Get the Example Plugin
The example plugin is included in this repository at `networkdataapi-example-plugin/`

### Step 2: Build the Plugin
```bash
cd networkdataapi-example-plugin
mvn clean package
```

### Step 3: Install
```bash
# Copy to your server
cp target/NetworkDataAPI-Example-1.0-SNAPSHOT.jar /path/to/server/plugins/

# Restart your server
```

### Step 4: Test It!

#### Insert Some Data
```
/example insert apple 100
/example insert banana 200
/example insert cherry 50
/example insert orange 150
```

#### Query the Data
```
/example queryall                    # See all documents
/example query apple                 # Find by name
/example queryvalue 100              # Find all with value > 100
```

#### Update Data
```
/example update apple 250            # Change apple's value to 250
/example query apple                 # Verify the update
```

#### Delete Data
```
/example delete cherry               # Remove cherry
/example queryall                    # Verify it's gone
```

#### Check Statistics
```
/example stats                       # See collection stats
```

### Step 5: Check the Logs
Look at your server console to see detailed logging for each operation:

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

## 🎓 What You Just Learned

### Database Concepts
- ✅ Created an isolated MongoDB database (`example_plugin`)
- ✅ Used a custom collection (`example_collection`)
- ✅ Performed CRUD operations (Create, Read, Update, Delete)

### MongoDB Operations
- ✅ **Insert**: Added documents with `insertOne()`
- ✅ **Query**: Found documents with `find()` and filters
- ✅ **Update**: Modified documents with `updateOne()`
- ✅ **Delete**: Removed documents with `deleteOne()`

### NetworkDataAPI Integration
- ✅ Leveraged shared MongoDB connection
- ✅ No separate database connection needed
- ✅ Automatic connection management
- ✅ Used the public API (`APIRegistry.getAPI()`)

## 📖 Next Steps

### 1. Study the Code
Start with these files in order:
1. **ExamplePlugin.java** - Main plugin class, shows API integration
2. **ExampleDataManager.java** - All MongoDB operations
3. **ExampleCommand.java** - Command handling

### 2. Customize for Your Plugin
```java
// Instead of "example_plugin", use your plugin name
MongoDatabase database = api.getDatabase("yourplugin_name");

// Instead of "example_collection", use your collection name
MongoCollection<Document> collection = database.getCollection("your_collection");

// Add your own fields to documents
Document doc = new Document()
    .append("yourField1", value1)
    .append("yourField2", value2)
    .append("timestamp", System.currentTimeMillis());
```

### 3. Add More Collections
```java
// You can have multiple collections
MongoCollection<Document> users = database.getCollection("users");
MongoCollection<Document> items = database.getCollection("items");
MongoCollection<Document> transactions = database.getCollection("transactions");
```

### 4. Add Indexes for Performance
```java
// Create indexes on frequently queried fields
collection.createIndex(Indexes.ascending("playerId"));
collection.createIndex(Indexes.descending("timestamp"));
collection.createIndex(Indexes.ascending("category", "type"));  // Compound index
```

### 5. Build Your Features
Use the example as a template for:
- **Cosmetics System**: Store owned cosmetics, equipped items
- **Economy Plugin**: Store balances, transactions
- **Guild System**: Store guilds, members, ranks
- **Stats Tracker**: Store player statistics
- **Punishment System**: Store bans, mutes, warnings

## 🔍 Debugging Tips

### Enable Detailed Logging
All operations in the example plugin already log detailed information. Just watch your server console!

### Test Each Operation
Use the in-game commands to test:
1. Insert a document
2. Query it back
3. Update it
4. Query again to verify
5. Delete it
6. Query to confirm deletion

### Common Issues

**Problem**: "NetworkDataAPI not found"
**Solution**: Ensure NetworkDataAPI is installed and loaded first

**Problem**: No documents found
**Solution**: Insert some data first with `/example insert`

**Problem**: Update doesn't work
**Solution**: Document must exist first - verify with `/example query`

## 📊 Understanding the Database Structure

### Database: `example_plugin`
- Created automatically when you first use it
- Completely isolated from other plugins
- Can be backed up independently

### Collection: `example_collection`
- Created automatically on first use
- Stores documents with this structure:
```json
{
  "_id": ObjectId("..."),           // Auto-generated MongoDB ID
  "name": "apple",                  // Your name field
  "value": 100,                     // Your value field
  "timestamp": 1699123456789,       // Creation timestamp
  "updated": false,                 // Update flag
  "lastModified": 1699123999999     // Last modification time (if updated)
}
```

### Indexes Created
- `name` (ascending) - Fast name lookups
- `value` (ascending) - Fast value queries
- `name, value` (compound) - Fast combined queries

## 💡 Pro Tips

### 1. Use Asynchronous Operations
For production plugins, wrap MongoDB operations in async tasks:
```java
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // MongoDB operations here
    Document doc = collection.find(Filters.eq("name", "example")).first();
    
    // Switch back to main thread for Bukkit API calls
    Bukkit.getScheduler().runTask(plugin, () -> {
        player.sendMessage("Found: " + doc.toJson());
    });
});
```

### 2. Use Batch Operations
For multiple inserts, use `insertMany()`:
```java
List<Document> documents = Arrays.asList(
    new Document("name", "item1").append("value", 10),
    new Document("name", "item2").append("value", 20),
    new Document("name", "item3").append("value", 30)
);
collection.insertMany(documents);
```

### 3. Use Projections
Only fetch the fields you need:
```java
Bson projection = Projections.fields(
    Projections.include("name", "value"),
    Projections.excludeId()
);
Document doc = collection.find(filter).projection(projection).first();
```

### 4. Use Aggregation
For complex queries:
```java
List<Bson> pipeline = Arrays.asList(
    Aggregates.match(Filters.gt("value", 50)),
    Aggregates.group("$name", Accumulators.avg("avgValue", "$value")),
    Aggregates.sort(Sorts.descending("avgValue"))
);
List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());
```

## 🎯 You're Ready!

You now know how to:
- ✅ Use NetworkDataAPI's shared connection
- ✅ Create your own database and collections
- ✅ Perform all CRUD operations
- ✅ Create indexes for performance
- ✅ Handle errors properly
- ✅ Log operations for debugging

**Start building your plugin!** Use this example as a reference and customize it for your needs.

## 📚 Additional Resources

- [Example Plugin README](networkdataapi-example-plugin/README.md)
- [Example Plugin Guide](EXAMPLE_PLUGIN_GUIDE.md)
- [API Documentation](API_DOCUMENTATION.md)
- [MongoDB Java Driver Docs](https://mongodb.github.io/mongo-java-driver/)

---

**Happy coding! 🚀**
