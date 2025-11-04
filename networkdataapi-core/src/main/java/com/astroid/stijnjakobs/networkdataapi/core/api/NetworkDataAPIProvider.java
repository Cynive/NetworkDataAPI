package com.astroid.stijnjakobs.networkdataapi.core.api;

import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.mongodb.client.MongoDatabase;

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
     * @return the MongoDB database instance
     */
    MongoDatabase getDatabase();

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
}
