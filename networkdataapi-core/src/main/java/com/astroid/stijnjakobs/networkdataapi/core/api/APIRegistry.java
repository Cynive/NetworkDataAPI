package com.astroid.stijnjakobs.networkdataapi.core.api;

import com.astroid.stijnjakobs.networkdataapi.core.CoreManager;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;

/**
 * Public API registry for accessing NetworkDataAPI services.
 *
 * <p>This class provides a clean, documented interface for other plugins
 * to interact with NetworkDataAPI. All public methods are thread-safe
 * and can be called from any thread.</p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // In your plugin's onEnable:
 * NetworkDataAPIProvider api = APIRegistry.getAPI();
 *
 * // Get player data service
 * PlayerDataService playerData = api.getPlayerDataService();
 *
 * // Use the service
 * playerData.getPlayerDataAsync(playerUUID).thenAccept(data -> {
 *     // Handle player data
 * });
 * }</pre>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class APIRegistry {

    private static CoreManager coreManager;

    /**
     * Registers the core manager (called internally by the plugin).
     *
     * @param manager the core manager instance
     */
    public static void register(CoreManager manager) {
        coreManager = manager;
    }

    /**
     * Unregisters the core manager (called internally during shutdown).
     */
    public static void unregister() {
        coreManager = null;
    }

    /**
     * Gets the NetworkDataAPI instance.
     *
     * <p>This provides access to all API services. Other plugins should
     * use this method to interact with NetworkDataAPI.</p>
     *
     * @return the API provider, or null if not initialized
     */
    public static NetworkDataAPIProvider getAPI() {
        if (coreManager == null || !coreManager.isInitialized()) {
            return null;
        }
        return new NetworkDataAPIProviderImpl(coreManager);
    }

    /**
     * Checks if the API is available.
     *
     * @return true if API is available, false otherwise
     */
    public static boolean isAvailable() {
        return coreManager != null && coreManager.isInitialized();
    }

    /**
     * Internal implementation of the API provider.
     */
    private static class NetworkDataAPIProviderImpl implements NetworkDataAPIProvider {

        private final CoreManager coreManager;

        public NetworkDataAPIProviderImpl(CoreManager coreManager) {
            this.coreManager = coreManager;
        }

        @Override
        public PlayerDataService getPlayerDataService() {
            return coreManager.getPlayerDataService();
        }

        @Override
        public com.mongodb.client.MongoDatabase getDatabase() {
            return coreManager.getDatabaseManager().getDatabase();
        }

        @Override
        public com.mongodb.client.MongoDatabase getDatabase(String databaseName) {
            return coreManager.getDatabaseManager().getClient().getDatabase(databaseName);
        }

        @Override
        public String getVersion() {
            return "1.0-SNAPSHOT";
        }

        @Override
        public boolean isHealthy() {
            return coreManager.getDatabaseManager().isHealthy();
        }
    }
}
