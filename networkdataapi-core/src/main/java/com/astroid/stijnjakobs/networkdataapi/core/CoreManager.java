package com.astroid.stijnjakobs.networkdataapi.core;

import com.astroid.stijnjakobs.networkdataapi.core.async.AsyncExecutor;
import com.astroid.stijnjakobs.networkdataapi.core.cache.CacheManager;
import com.astroid.stijnjakobs.networkdataapi.core.config.ConfigurationManager;
import com.astroid.stijnjakobs.networkdataapi.core.database.DatabaseManager;
import com.astroid.stijnjakobs.networkdataapi.core.environment.EnvironmentDetector;
import com.astroid.stijnjakobs.networkdataapi.core.redis.RedisManager;
import com.astroid.stijnjakobs.networkdataapi.core.rest.RESTApiService;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.astroid.stijnjakobs.networkdataapi.core.service.RedisDataService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Core manager that orchestrates all components of the NetworkDataAPI.
 *
 * <p>This class is responsible for:</p>
 * <ul>
 *   <li>Loading configuration</li>
 *   <li>Initializing database connections</li>
 *   <li>Starting services (cache, async executor, REST API)</li>
 *   <li>Coordinating graceful shutdown</li>
 *   <li>Providing access to all API components</li>
 * </ul>
 *
 * <p><strong>Lifecycle:</strong></p>
 * <ol>
 *   <li>Call {@link #initialize(File)} to start all components</li>
 *   <li>Use getter methods to access services</li>
 *   <li>Call {@link #shutdown()} before plugin disable</li>
 * </ol>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
@Getter
public class CoreManager {

    private static final Logger logger = LoggerFactory.getLogger(CoreManager.class);

    private ConfigurationManager configurationManager;
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private CacheManager cacheManager;
    private AsyncExecutor asyncExecutor;
    private PlayerDataService playerDataService;
    private RedisDataService redisDataService;
    private RESTApiService restApiService;

    private boolean initialized = false;

    /**
     * Initializes all core components.
     *
     * @param dataFolder the plugin data folder for storing config
     * @throws Exception if initialization fails
     */
    public void initialize(File dataFolder) throws Exception {
        logger.info("=================================================");
        logger.info("  NetworkDataAPI - Enterprise Network Data Sync");
        logger.info("  Version: 1.0-SNAPSHOT");
        logger.info("  Environment: {}", EnvironmentDetector.getEnvironmentName());
        logger.info("=================================================");

        try {
            // Ensure data folder exists
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Load configuration
            logger.info("Loading configuration...");
            File configFile = new File(dataFolder, "config.yml");
            configurationManager = new ConfigurationManager(configFile);
            configurationManager.load();
            logger.info("Configuration loaded successfully");

            // Initialize async executor
            logger.info("Initializing async executor...");
            asyncExecutor = new AsyncExecutor(configurationManager);

            // Initialize cache manager
            logger.info("Initializing cache manager...");
            cacheManager = new CacheManager(configurationManager);

            // Initialize database manager
            logger.info("Connecting to MongoDB...");
            databaseManager = new DatabaseManager(configurationManager);
            databaseManager.connect();

            // Initialize Redis manager (if enabled)
            if (configurationManager.getBoolean("redis.enabled", false)) {
                logger.info("Connecting to Redis...");
                redisManager = new RedisManager(configurationManager);
                redisManager.connect();

                // Initialize Redis data service
                logger.info("Initializing Redis data service...");
                redisDataService = new RedisDataService(redisManager, asyncExecutor);
            } else {
                logger.info("Redis is disabled in configuration");
            }

            // Initialize services
            logger.info("Initializing player data service...");
            playerDataService = new PlayerDataService(databaseManager, cacheManager, asyncExecutor);

            // Initialize REST API (if enabled)
            logger.info("Initializing REST API...");
            restApiService = new RESTApiService(configurationManager, playerDataService);
            restApiService.start();

            // Schedule periodic cache maintenance
            scheduleMaintenanceTasks();

            initialized = true;
            logger.info("=================================================");
            logger.info("  NetworkDataAPI initialized successfully!");
            logger.info("=================================================");

        } catch (Exception e) {
            logger.error("Failed to initialize NetworkDataAPI", e);
            shutdown(); // Clean up any partially initialized components
            throw e;
        }
    }

    /**
     * Schedules periodic maintenance tasks.
     */
    private void scheduleMaintenanceTasks() {
        // Cache maintenance every 5 minutes
        asyncExecutor.schedule(() -> {
            cacheManager.performMaintenance();
            cacheManager.logStats();
            scheduleMaintenanceTasks(); // Reschedule
        }, 5, TimeUnit.MINUTES);

        // Database health check every minute
        asyncExecutor.schedule(() -> {
            if (!databaseManager.isHealthy()) {
                logger.warn("Database health check failed, attempting reconnect...");
                databaseManager.reconnect();
            }
        }, 1, TimeUnit.MINUTES);

        // Redis health check every minute (if enabled)
        if (redisManager != null) {
            asyncExecutor.schedule(() -> {
                if (!redisManager.isAlive()) {
                    logger.warn("Redis health check failed");
                }
            }, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * Shuts down all core components gracefully.
     */
    public void shutdown() {
        logger.info("Shutting down NetworkDataAPI...");

        // Stop REST API
        if (restApiService != null) {
            try {
                restApiService.stop();
            } catch (Exception e) {
                logger.error("Error stopping REST API", e);
            }
        }

        // Shutdown async executor
        if (asyncExecutor != null) {
            try {
                asyncExecutor.shutdown();
            } catch (Exception e) {
                logger.error("Error shutting down async executor", e);
            }
        }

        // Close database connection
        if (databaseManager != null) {
            try {
                databaseManager.shutdown();
            } catch (Exception e) {
                logger.error("Error shutting down database", e);
            }
        }

        // Close Redis connection
        if (redisManager != null) {
            try {
                redisManager.shutdown();
            } catch (Exception e) {
                logger.error("Error shutting down Redis", e);
            }
        }

        // Clear cache
        if (cacheManager != null) {
            try {
                cacheManager.invalidateAll();
            } catch (Exception e) {
                logger.error("Error clearing cache", e);
            }
        }

        initialized = false;
        logger.info("NetworkDataAPI shutdown complete");
    }

    /**
     * Checks if the core manager is initialized.
     *
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the configuration manager.
     *
     * @return the configuration manager
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Gets the database manager.
     *
     * @return the database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the Redis manager.
     *
     * @return the Redis manager
     */
    public RedisManager getRedisManager() {
        return redisManager;
    }

    /**
     * Gets the cache manager.
     *
     * @return the cache manager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Gets the async executor.
     *
     * @return the async executor
     */
    public AsyncExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    /**
     * Gets the player data service.
     *
     * @return the player data service
     */
    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }

    /**
     * Gets the Redis data service.
     *
     * @return the Redis data service
     */
    public RedisDataService getRedisDataService() {
        return redisDataService;
    }

    /**
     * Gets the REST API service.
     *
     * @return the REST API service
     */
    public RESTApiService getRestApiService() {
        return restApiService;
    }
}

