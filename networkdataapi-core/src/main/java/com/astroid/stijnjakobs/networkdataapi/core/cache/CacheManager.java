package com.astroid.stijnjakobs.networkdataapi.core.cache;

import com.astroid.stijnjakobs.networkdataapi.core.config.ConfigurationManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Manages in-memory caching using Caffeine for high-performance data access.
 *
 * <p>The cache manager reduces database load by storing frequently accessed
 * data in memory. It provides:</p>
 * <ul>
 *   <li>Automatic expiration based on write and access time</li>
 *   <li>Maximum size limits to prevent memory exhaustion</li>
 *   <li>Cache statistics for monitoring hit rates</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 *
 * <p><strong>Performance:</strong> Caffeine is a high-performance caching library
 * that uses techniques like TinyLFU for optimal eviction policies.</p>
 *
 * <p><strong>Thread Safety:</strong> This class is fully thread-safe.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

    private final Cache<String, Object> cache;
    private final boolean enabled;

    /**
     * Creates a new cache manager with the given configuration.
     *
     * @param config the configuration manager
     */
    public CacheManager(ConfigurationManager config) {
        this.enabled = config.getBoolean("cache.enabled", true);

        if (enabled) {
            int maxSize = config.getInt("cache.max-size", 10000);
            long expireAfterWrite = config.getLong("cache.expire-after-write-minutes", 5);
            long expireAfterAccess = config.getLong("cache.expire-after-access-minutes", 10);

            this.cache = Caffeine.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                    .expireAfterAccess(expireAfterAccess, TimeUnit.MINUTES)
                    .recordStats()
                    .build();

            logger.info("Cache initialized with max size: {}, expire after write: {}m, expire after access: {}m",
                    maxSize, expireAfterWrite, expireAfterAccess);
        } else {
            this.cache = null;
            logger.info("Cache is disabled");
        }
    }

    /**
     * Gets a value from the cache.
     *
     * @param <T> the value type
     * @param key the cache key
     * @return the cached value, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!enabled || cache == null) {
            return null;
        }
        return (T) cache.getIfPresent(key);
    }

    /**
     * Gets a value from the cache, or computes it if absent.
     *
     * <p>This method is useful for read-through caching where the value
     * is loaded from the database if not in cache.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * Document data = cacheManager.get("player:" + uuid, key -> {
     *     return database.getCollection("players")
     *         .find(eq("_id", uuid))
     *         .first();
     * });
     * }</pre>
     *
     * @param <T> the value type
     * @param key the cache key
     * @param loader the function to load the value if not cached
     * @return the cached or loaded value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Function<String, T> loader) {
        if (!enabled || cache == null) {
            return loader.apply(key);
        }
        return (T) cache.get(key, k -> loader.apply(k));
    }

    /**
     * Puts a value into the cache.
     *
     * @param key the cache key
     * @param value the value to cache
     */
    public void put(String key, Object value) {
        if (enabled && cache != null && value != null) {
            cache.put(key, value);
        }
    }

    /**
     * Removes a value from the cache.
     *
     * @param key the cache key to remove
     */
    public void invalidate(String key) {
        if (enabled && cache != null) {
            cache.invalidate(key);
        }
    }

    /**
     * Removes all entries matching the key pattern.
     *
     * <p>Example: invalidatePattern("player:*") removes all player cache entries.</p>
     *
     * @param pattern the key pattern (supports * wildcard)
     */
    public void invalidatePattern(String pattern) {
        if (!enabled || cache == null) {
            return;
        }

        String regex = pattern.replace("*", ".*");
        cache.asMap().keySet().removeIf(key -> key.matches(regex));
    }

    /**
     * Clears all entries from the cache.
     */
    public void invalidateAll() {
        if (enabled && cache != null) {
            cache.invalidateAll();
            logger.info("Cache cleared");
        }
    }

    /**
     * Gets the current size of the cache.
     *
     * @return the number of entries in the cache
     */
    public long size() {
        if (!enabled || cache == null) {
            return 0;
        }
        return cache.estimatedSize();
    }

    /**
     * Gets cache statistics.
     *
     * <p>Statistics include:</p>
     * <ul>
     *   <li>Hit count and rate</li>
     *   <li>Miss count and rate</li>
     *   <li>Load success and failure counts</li>
     *   <li>Eviction count</li>
     * </ul>
     *
     * @return the cache statistics
     */
    public CacheStats getStats() {
        if (!enabled || cache == null) {
            return CacheStats.empty();
        }
        return cache.stats();
    }

    /**
     * Logs current cache statistics.
     */
    public void logStats() {
        if (!enabled || cache == null) {
            return;
        }

        CacheStats stats = cache.stats();
        logger.info("Cache Statistics - Size: {}, Hits: {}, Misses: {}, Hit Rate: {:.2f}%, Evictions: {}",
                cache.estimatedSize(),
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100,
                stats.evictionCount());
    }

    /**
     * Performs cache maintenance.
     *
     * <p>This method should be called periodically to clean up expired entries
     * and update statistics.</p>
     */
    public void performMaintenance() {
        if (enabled && cache != null) {
            cache.cleanUp();
        }
    }

    /**
     * Checks if caching is enabled.
     *
     * @return true if caching is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
}

