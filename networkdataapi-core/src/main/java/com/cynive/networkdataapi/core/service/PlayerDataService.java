package com.cynive.networkdataapi.core.service;

import com.cynive.networkdataapi.core.async.AsyncExecutor;
import com.cynive.networkdataapi.core.cache.CacheManager;
import com.cynive.networkdataapi.core.database.DatabaseManager;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for managing player data in MongoDB with caching support.
 *
 * <p>This service provides high-level CRUD operations for player data,
 * including:</p>
 * <ul>
 *   <li>Asynchronous data loading and saving</li>
 *   <li>Automatic caching for frequently accessed data</li>
 *   <li>Batch operations for multiple players</li>
 *   <li>Field-level updates without loading entire documents</li>
 *   <li>Query support with MongoDB filters</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> All methods are thread-safe and can be
 * called from any thread. Database operations are executed asynchronously
 * to avoid blocking the main server thread.</p>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * PlayerDataService service = NetworkDataAPI.getInstance().getPlayerDataService();
 *
 * // Async get player data
 * service.getPlayerDataAsync(playerUUID).thenAccept(data -> {
 *     int coins = data.getInteger("coins", 0);
 *     System.out.println("Player has " + coins + " coins");
 * });
 *
 * // Update specific field
 * service.updateField(playerUUID, "lastLogin", System.currentTimeMillis());
 *
 * // Save complete player data
 * Document playerData = new Document("coins", 1000)
 *     .append("level", 5)
 *     .append("lastLogin", System.currentTimeMillis());
 * service.savePlayerData(playerUUID, playerData);
 * }</pre>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class PlayerDataService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerDataService.class);
    private static final String COLLECTION_NAME = "players";

    private final DatabaseManager databaseManager;
    private final CacheManager cacheManager;
    private final AsyncExecutor asyncExecutor;

    /**
     * Creates a new player data service.
     *
     * @param databaseManager the database manager
     * @param cacheManager the cache manager
     * @param asyncExecutor the async executor
     */
    public PlayerDataService(DatabaseManager databaseManager, CacheManager cacheManager, AsyncExecutor asyncExecutor) {
        this.databaseManager = databaseManager;
        this.cacheManager = cacheManager;
        this.asyncExecutor = asyncExecutor;
    }

    /**
     * Gets the players collection from the database.
     *
     * @return the MongoCollection for players
     */
    private MongoCollection<Document> getCollection() {
        return databaseManager.getDatabase().getCollection(COLLECTION_NAME);
    }

    /**
     * Generates a cache key for a player UUID.
     *
     * @param uuid the player UUID
     * @return the cache key
     */
    private String getCacheKey(UUID uuid) {
        return "player:" + uuid.toString();
    }

    /**
     * Gets player data synchronously (blocking).
     *
     * <p><strong>Warning:</strong> This method blocks the calling thread.
     * Use {@link #getPlayerDataAsync(UUID)} instead when possible.</p>
     *
     * @param uuid the player UUID
     * @return the player data document, or a new empty document if not found
     */
    public Document getPlayerData(UUID uuid) {
        String cacheKey = getCacheKey(uuid);

        // Check cache first
        Document cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return new Document(cached); // Return a copy to prevent modifications
        }

        // Load from database
        Document data = getCollection()
                .find(Filters.eq("_id", uuid.toString()))
                .first();

        if (data == null) {
            // Return empty document - plugins create their own data!
            data = new Document("_id", uuid.toString());
        }

        // Cache the result
        cacheManager.put(cacheKey, data);

        return new Document(data);
    }

    /**
     * Gets player data asynchronously (non-blocking).
     *
     * <p>This is the recommended method for retrieving player data as it
     * doesn't block the server thread.</p>
     *
     * @param uuid the player UUID
     * @return a CompletableFuture with the player data
     */
    public CompletableFuture<Document> getPlayerDataAsync(UUID uuid) {
        return asyncExecutor.supply(() -> getPlayerData(uuid));
    }

    /**
     * Gets player data asynchronously with a callback.
     *
     * @param uuid the player UUID
     * @param callback the callback to handle the result
     */
    public void getPlayerDataAsync(UUID uuid, Consumer<Document> callback) {
        asyncExecutor.supplyAsync(() -> getPlayerData(uuid), callback);
    }

    /**
     * Saves player data synchronously (blocking).
     *
     * <p><strong>Warning:</strong> This method blocks the calling thread.
     * Use {@link #savePlayerDataAsync(UUID, Document)} instead when possible.</p>
     *
     * @param uuid the player UUID
     * @param data the player data to save
     */
    public void savePlayerData(UUID uuid, Document data) {
        data.put("_id", uuid.toString());
        data.put("lastUpdated", System.currentTimeMillis());

        getCollection().replaceOne(
                Filters.eq("_id", uuid.toString()),
                data,
                new ReplaceOptions().upsert(true)
        );

        // Update cache
        cacheManager.put(getCacheKey(uuid), data);
    }

    /**
     * Saves player data asynchronously (non-blocking).
     *
     * @param uuid the player UUID
     * @param data the player data to save
     * @return a CompletableFuture that completes when the save is done
     */
    public CompletableFuture<Void> savePlayerDataAsync(UUID uuid, Document data) {
        return asyncExecutor.supply(() -> {
            savePlayerData(uuid, data);
            return null;
        });
    }

    /**
     * Saves player data asynchronously with a callback.
     *
     * @param uuid the player UUID
     * @param data the player data to save
     * @param callback the callback to execute when save is complete
     */
    public void savePlayerDataAsync(UUID uuid, Document data, Runnable callback) {
        savePlayerDataAsync(uuid, data).thenRun(callback);
    }

    /**
     * Updates a specific field in player data without loading the entire document.
     *
     * <p>This is more efficient than loading, modifying, and saving when you
     * only need to update a single field.</p>
     *
     * @param uuid the player UUID
     * @param field the field name to update
     * @param value the new value
     */
    public void updateField(UUID uuid, String field, Object value) {
        getCollection().updateOne(
                Filters.eq("_id", uuid.toString()),
                Updates.combine(
                        Updates.set(field, value),
                        Updates.set("lastUpdated", System.currentTimeMillis())
                )
        );

        // Invalidate cache to ensure fresh data on next read
        cacheManager.invalidate(getCacheKey(uuid));
    }

    /**
     * Updates a specific field asynchronously.
     *
     * @param uuid the player UUID
     * @param field the field name to update
     * @param value the new value
     * @return a CompletableFuture that completes when the update is done
     */
    public CompletableFuture<Void> updateFieldAsync(UUID uuid, String field, Object value) {
        return asyncExecutor.supply(() -> {
            updateField(uuid, field, value);
            return null;
        });
    }

    /**
     * Updates multiple fields in player data.
     *
     * @param uuid the player UUID
     * @param updates a map of field names to new values
     */
    public void updateFields(UUID uuid, Map<String, Object> updates) {
        List<Bson> updateOps = new ArrayList<>();
        updates.forEach((field, value) -> updateOps.add(Updates.set(field, value)));
        updateOps.add(Updates.set("lastUpdated", System.currentTimeMillis()));

        getCollection().updateOne(
                Filters.eq("_id", uuid.toString()),
                Updates.combine(updateOps)
        );

        cacheManager.invalidate(getCacheKey(uuid));
    }

    /**
     * Updates multiple fields asynchronously.
     *
     * @param uuid the player UUID
     * @param updates a map of field names to new values
     * @return a CompletableFuture that completes when the update is done
     */
    public CompletableFuture<Void> updateFieldsAsync(UUID uuid, Map<String, Object> updates) {
        return asyncExecutor.supply(() -> {
            updateFields(uuid, updates);
            return null;
        });
    }

    /**
     * Increments a numeric field by a specific amount.
     *
     * @param uuid the player UUID
     * @param field the field name to increment
     * @param amount the amount to add (can be negative)
     */
    public void incrementField(UUID uuid, String field, Number amount) {
        getCollection().updateOne(
                Filters.eq("_id", uuid.toString()),
                Updates.combine(
                        Updates.inc(field, amount),
                        Updates.set("lastUpdated", System.currentTimeMillis())
                )
        );

        cacheManager.invalidate(getCacheKey(uuid));
    }

    /**
     * Increments a numeric field asynchronously.
     *
     * @param uuid the player UUID
     * @param field the field name to increment
     * @param amount the amount to add (can be negative)
     * @return a CompletableFuture that completes when the increment is done
     */
    public CompletableFuture<Void> incrementFieldAsync(UUID uuid, String field, Number amount) {
        return asyncExecutor.supply(() -> {
            incrementField(uuid, field, amount);
            return null;
        });
    }

    /**
     * Deletes player data.
     *
     * @param uuid the player UUID
     * @return true if data was deleted, false if it didn't exist
     */
    public boolean deletePlayerData(UUID uuid) {
        boolean deleted = getCollection().deleteOne(Filters.eq("_id", uuid.toString()))
                .getDeletedCount() > 0;

        cacheManager.invalidate(getCacheKey(uuid));
        return deleted;
    }

    /**
     * Deletes player data asynchronously.
     *
     * @param uuid the player UUID
     * @return a CompletableFuture with true if deleted, false otherwise
     */
    public CompletableFuture<Boolean> deletePlayerDataAsync(UUID uuid) {
        return asyncExecutor.supply(() -> deletePlayerData(uuid));
    }

    /**
     * Checks if player data exists in the database.
     *
     * @param uuid the player UUID
     * @return true if data exists, false otherwise
     */
    public boolean exists(UUID uuid) {
        return getCollection().countDocuments(Filters.eq("_id", uuid.toString())) > 0;
    }

    /**
     * Checks if player data exists asynchronously.
     *
     * @param uuid the player UUID
     * @return a CompletableFuture with true if exists, false otherwise
     */
    public CompletableFuture<Boolean> existsAsync(UUID uuid) {
        return asyncExecutor.supply(() -> exists(uuid));
    }

    /**
     * Queries player data with a custom filter.
     *
     * @param filter the MongoDB filter
     * @param limit maximum number of results (0 = no limit)
     * @return a list of matching player documents
     */
    public List<Document> query(Bson filter, int limit) {
        List<Document> results = new ArrayList<>();
        var cursor = limit > 0
                ? getCollection().find(filter).limit(limit)
                : getCollection().find(filter);

        cursor.forEach(results::add);
        return results;
    }

    /**
     * Queries player data asynchronously.
     *
     * @param filter the MongoDB filter
     * @param limit maximum number of results (0 = no limit)
     * @return a CompletableFuture with the query results
     */
    public CompletableFuture<List<Document>> queryAsync(Bson filter, int limit) {
        return asyncExecutor.supply(() -> query(filter, limit));
    }
}

