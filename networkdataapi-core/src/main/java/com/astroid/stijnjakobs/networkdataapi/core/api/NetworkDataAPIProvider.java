package com.astroid.stijnjakobs.networkdataapi.core.api;

import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.astroid.stijnjakobs.networkdataapi.core.service.RedisDataService;
import com.mongodb.client.MongoDatabase;
import redis.clients.jedis.JedisPool;

/**
 * Public API interface for NetworkDataAPI.
 *
 * <p>This interface defines the contract for accessing NetworkDataAPI services
 * from external plugins. It provides a stable API that won't change between versions.</p>
 *
 * <p><strong>How to use in your plugin:</strong></p>
 * <ol>
 *   <li>Add NetworkDataAPI as a dependency in your plugin.yml:
 *   <pre>{@code
 *   depend:
 *     - NetworkDataAPI
 *   }</pre>
 *   </li>
 *   <li>Get the API instance:
 *   <pre>{@code
 *   NetworkDataAPIProvider api = APIRegistry.getAPI();
 *   if (api == null) {
 *       getLogger().severe("NetworkDataAPI not found!");
 *       return;
 *   }
 *   }</pre>
 *   </li>
 *   <li>Use the services:
 *   <pre>{@code
 *   PlayerDataService playerData = api.getPlayerDataService();
 *   playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
 *       // Handle player data
 *   });
 *   }</pre>
 *   </li>
 * </ol>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public interface NetworkDataAPIProvider {

    /**
     * Gets the player data service for managing player information.
     *
     * <p>The player data service provides methods to:</p>
     * <ul>
     *   <li>Read and write player data</li>
     *   <li>Update specific fields</li>
     *   <li>Query player data with filters</li>
     *   <li>Perform bulk operations</li>
     * </ul>
     *
     * <p>All operations are thread-safe and most have async variants.</p>
     *
     * @return the player data service
     */
    PlayerDataService getPlayerDataService();

    /**
     * Gets direct access to the MongoDB database.
     *
     * <p>This allows plugins to create and manage their own collections
     * while using the shared connection pool provided by NetworkDataAPI.</p>
     *
     * <p><strong>Example - Creating a custom collection:</strong></p>
     * <pre>{@code
     * MongoDatabase database = api.getDatabase();
     * MongoCollection<Document> cosmetics = database.getCollection("cosmetics");
     *
     * Document cosmetic = new Document("name", "Party Hat")
     *     .append("price", 1000)
     *     .append("rarity", "RARE");
     * cosmetics.insertOne(cosmetic);
     * }</pre>
     *
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>No need to create separate database connections</li>
     *   <li>Uses the shared connection pool (efficient)</li>
     *   <li>Automatic connection management and retries</li>
     *   <li>Full MongoDB API access</li>
     * </ul>
     *
     * @return the MongoDB database instance (default database from config)
     */
    MongoDatabase getDatabase();

    /**
     * Gets access to a custom MongoDB database using the shared connection.
     *
     * <p>This allows plugins to create and use their own separate databases
     * while still using the shared connection pool provided by NetworkDataAPI.</p>
     *
     * <p><strong>Example - Plugin with its own database:</strong></p>
     * <pre>{@code
     * // Each plugin can have its own database
     * MongoDatabase cosmeticsDB = api.getDatabase("cosmetics_plugin");
     * MongoDatabase guildsDB = api.getDatabase("guilds_plugin");
     * MongoDatabase punishmentsDB = api.getDatabase("punishments_plugin");
     *
     * // Use your own database
     * MongoCollection<Document> items = cosmeticsDB.getCollection("items");
     * items.insertOne(new Document("name", "Crown").append("price", 5000));
     * }</pre>
     *
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>Complete database isolation per plugin</li>
     *   <li>No conflicts with other plugins</li>
     *   <li>Uses the shared connection pool (efficient)</li>
     *   <li>Automatic connection management and retries</li>
     *   <li>Full MongoDB API access</li>
     * </ul>
     *
     * <p><strong>Note:</strong> The database will be created automatically when
     * you first write data to it. No manual creation needed.</p>
     *
     * @param databaseName the name of the database to access
     * @return the MongoDB database instance
     */
    MongoDatabase getDatabase(String databaseName);

    /**
     * Gets the current version of NetworkDataAPI.
     *
     * @return the version string
     */
    String getVersion();

    /**
     * Checks if the API and database connection are healthy.
     *
     * <p>This can be used to verify connectivity before performing
     * critical operations.</p>
     *
     * @return true if healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Gets the Redis data service for caching and messaging.
     *
     * <p>The Redis data service provides methods for:</p>
     * <ul>
     *   <li>String operations (get, set, with TTL)</li>
     *   <li>Hash operations (field-value storage)</li>
     *   <li>Set operations (unique members)</li>
     *   <li>List operations (ordered data)</li>
     *   <li>Pub/Sub messaging</li>
     *   <li>Counter operations</li>
     * </ul>
     *
     * <p><strong>Example - Caching player data:</strong></p>
     * <pre>{@code
     * RedisDataService redis = api.getRedisDataService();
     *
     * // Cache with 5 minute TTL
     * redis.setWithExpiry("player:" + uuid, playerData, 300);
     *
     * // Retrieve cached data
     * String data = redis.get("player:" + uuid);
     * }</pre>
     *
     * <p><strong>Example - Pub/Sub messaging:</strong></p>
     * <pre>{@code
     * // Publish to other servers
     * redis.publish("player-join", uuid.toString());
     * }</pre>
     *
     * @return the Redis data service, or null if Redis is disabled
     */
    RedisDataService getRedisDataService();

    /**
     * Gets direct access to the Redis connection pool.
     *
     * <p>This allows plugins to use the shared Redis connection pool
     * for custom operations not covered by RedisDataService.</p>
     *
     * <p><strong>Example - Custom Redis operations:</strong></p>
     * <pre>{@code
     * JedisPool pool = api.getRedisPool();
     * try (Jedis jedis = pool.getResource()) {
     *     // Custom Redis commands
     *     jedis.zadd("leaderboard", 1000, "player1");
     *     Set<String> top10 = jedis.zrevrange("leaderboard", 0, 9);
     * }
     * }</pre>
     *
     * <p><strong>Benefits:</strong></p>
     * <ul>
     *   <li>No separate Redis connection needed</li>
     *   <li>Uses shared connection pool (efficient)</li>
     *   <li>Automatic reconnection</li>
     *   <li>Full Jedis API access</li>
     * </ul>
     *
     * <p><strong>Important:</strong> Always use try-with-resources to ensure
     * connections are returned to the pool!</p>
     *
     * @return the Jedis pool, or null if Redis is disabled
     */
    JedisPool getRedisPool();

    /**
     * Checks if Redis is enabled and connected.
     *
     * @return true if Redis is available
     */
    boolean isRedisEnabled();
}
