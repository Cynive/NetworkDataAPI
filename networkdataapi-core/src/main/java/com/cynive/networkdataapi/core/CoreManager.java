package com.cynive.networkdataapi.core;

import com.cynive.networkdataapi.core.async.AsyncExecutor;
import com.cynive.networkdataapi.core.cache.CacheManager;
import com.cynive.networkdataapi.core.config.ConfigurationManager;
import com.cynive.networkdataapi.core.database.DatabaseManager;
import com.cynive.networkdataapi.core.environment.EnvironmentDetector;
import com.cynive.networkdataapi.core.rest.RESTApiService;
import com.cynive.networkdataapi.core.service.PlayerDataService;
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
    private CacheManager cacheManager;
    private AsyncExecutor asyncExecutor;
    private PlayerDataService playerDataService;
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
                logger.error("Error closing database connection", e);
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
}

