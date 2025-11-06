package com.astroid.stijnjakobs.networkdataapi.core.redis;

import com.astroid.stijnjakobs.networkdataapi.core.config.ConfigurationManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;

/**
 * Manages Redis connection and pool access.
 *
 * <p>This class handles the lifecycle of the Redis client, including:
 * <ul>
 *   <li>Connection pool configuration</li>
 *   <li>Automatic reconnection and retry logic</li>
 *   <li>Graceful shutdown</li>
 *   <li>Thread-safe pool access</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe. Jedis pool
 * handles connection pooling and thread safety internally.</p>
 *
 * <p><strong>Resource Management:</strong> Call {@link #shutdown()} to properly
 * close connections before application termination.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class RedisManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisManager.class);

    private JedisPool jedisPool;

    private boolean connected = false;

    private final ConfigurationManager config;

    /**
     * Creates a new Redis manager.
     *
     * @param config the configuration manager
     */
    public RedisManager(ConfigurationManager config) {
        this.config = config;
    }

    /**
     * Gets the Jedis pool.
     *
     * @return the JedisPool instance
     */
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * Checks if Redis is connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Initializes the Redis connection pool.
     *
     * <p>This method configures the Jedis pool with connection pooling,
     * timeouts, and other settings from the configuration file.</p>
     *
     * @throws Exception if connection fails
     */
    public void connect() throws Exception {
        logger.info("Initializing Redis connection...");

        try {
            // Get connection settings
            String host = config.getString("redis.host", "localhost");
            int port = config.getInt("redis.port", 6379);
            String password = config.getString("redis.password", "");
            int database = config.getInt("redis.database", 0);
            int timeout = config.getInt("redis.timeout-ms", 2000);

            // Configure pool settings
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(config.getInt("redis.max-pool-size", 100));
            poolConfig.setMaxIdle(config.getInt("redis.max-idle", 50));
            poolConfig.setMinIdle(config.getInt("redis.min-idle", 10));
            poolConfig.setTestOnBorrow(config.getBoolean("redis.test-on-borrow", true));
            poolConfig.setTestOnReturn(config.getBoolean("redis.test-on-return", false));
            poolConfig.setTestWhileIdle(config.getBoolean("redis.test-while-idle", true));
            poolConfig.setMinEvictableIdleTime(Duration.ofMillis(
                    config.getLong("redis.min-evictable-idle-time-ms", 60000)));
            poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(
                    config.getLong("redis.time-between-eviction-runs-ms", 30000)));
            poolConfig.setBlockWhenExhausted(config.getBoolean("redis.block-when-exhausted", true));
            poolConfig.setMaxWait(Duration.ofMillis(
                    config.getLong("redis.max-wait-ms", 3000)));

            // Create the pool
            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
            }

            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                String pong = jedis.ping();
                if (!"PONG".equals(pong)) {
                    throw new Exception("Failed to ping Redis server");
                }
            }

            connected = true;
            logger.info("Successfully connected to Redis at {}:{} (database: {})", host, port, database);
            logger.info("Redis pool configured with max size: {}, max idle: {}, min idle: {}",
                    poolConfig.getMaxTotal(), poolConfig.getMaxIdle(), poolConfig.getMinIdle());

        } catch (Exception e) {
            connected = false;
            logger.error("Failed to connect to Redis", e);
            throw new Exception("Redis connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a Redis connection from the pool.
     *
     * <p><strong>Important:</strong> The returned Jedis instance must be closed
     * after use to return it to the pool. Use try-with-resources:</p>
     * <pre>{@code
     * try (Jedis jedis = redisManager.getResource()) {
     *     jedis.set("key", "value");
     * }
     * }</pre>
     *
     * @return a Jedis instance from the pool
     * @throws IllegalStateException if not connected
     */
    public Jedis getResource() {
        if (!connected || jedisPool == null) {
            throw new IllegalStateException("Redis is not connected");
        }
        return jedisPool.getResource();
    }

    /**
     * Gracefully shuts down the Redis connection pool.
     *
     * <p>This method closes all connections in the pool and releases resources.
     * It should be called during plugin shutdown.</p>
     */
    public void shutdown() {
        if (jedisPool != null) {
            logger.info("Closing Redis connection pool...");
            try {
                jedisPool.close();
                connected = false;
                logger.info("Redis connection pool closed successfully");
            } catch (Exception e) {
                logger.error("Error while closing Redis connection pool", e);
            }
        }
    }

    /**
     * Checks if the Redis connection is alive.
     *
     * @return true if connected and responsive
     */
    public boolean isAlive() {
        if (!connected || jedisPool == null) {
            return false;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            logger.warn("Redis health check failed", e);
            return false;
        }
    }

    /**
     * Gets pool statistics for monitoring.
     *
     * @return a string with pool statistics
     */
    public String getPoolStats() {
        if (jedisPool == null) {
            return "Pool not initialized";
        }

        return String.format("Active: %d, Idle: %d, Waiting: %d",
                jedisPool.getNumActive(),
                jedisPool.getNumIdle(),
                jedisPool.getNumWaiters());
    }
}

