package com.cynive.networkdataapi.example;

import com.cynive.networkdataapi.core.api.APIRegistry;
import com.cynive.networkdataapi.core.api.NetworkDataAPIProvider;
import com.mongodb.client.MongoDatabase;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin demonstrating NetworkDataAPI usage.
 *
 * <p>This plugin shows how to leverage the shared MongoDB connection provided
 * by NetworkDataAPI to create and manage custom database collections.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Uses NetworkDataAPI's shared MongoDB connection</li>
 *   <li>Creates an isolated database for plugin data</li>
 *   <li>Demonstrates insert, query, and update operations</li>
 *   <li>Comprehensive logging for debugging</li>
 * </ul>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public final class ExamplePlugin extends JavaPlugin {

    private static ExamplePlugin instance;
    private NetworkDataAPIProvider api;
    private MongoDatabase database;
    private ExampleDataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Check if NetworkDataAPI is available
            if (!APIRegistry.isAvailable()) {
                getLogger().severe("========================================");
                getLogger().severe("NetworkDataAPI not found!");
                getLogger().severe("Please install NetworkDataAPI first.");
                getLogger().severe("========================================");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            getLogger().info("========================================");
            getLogger().info("NetworkDataAPI Example Plugin");
            getLogger().info("========================================");

            // Get API instance
            api = APIRegistry.getAPI();
            getLogger().info("Successfully hooked into NetworkDataAPI v" + api.getVersion());

            // Verify database health
            if (!api.isHealthy()) {
                getLogger().warning("NetworkDataAPI database connection is not healthy!");
                getLogger().warning("The plugin will continue but database operations may fail.");
            }

            // Get dedicated database for this plugin
            database = api.getDatabase("example_plugin");
            getLogger().info("Using dedicated MongoDB database: example_plugin");

            // Initialize data manager
            dataManager = new ExampleDataManager(database, getLogger());
            getLogger().info("Data manager initialized successfully");

            // Register commands
            ExampleCommand commandExecutor = new ExampleCommand(dataManager, getLogger());
            getCommand("example").setExecutor(commandExecutor);
            getCommand("example").setTabCompleter(commandExecutor);
            getLogger().info("Commands registered successfully");

            getLogger().info("========================================");
            getLogger().info("Example Plugin enabled successfully!");
            getLogger().info("Use /example help for available commands");
            getLogger().info("========================================");

        } catch (Exception e) {
            getLogger().severe("========================================");
            getLogger().severe("Failed to enable Example Plugin!");
            getLogger().severe("Error: " + e.getMessage());
            getLogger().severe("========================================");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("========================================");
        getLogger().info("Disabling Example Plugin...");
        getLogger().info("========================================");

        // Clean shutdown - NetworkDataAPI handles connection cleanup
        if (dataManager != null) {
            getLogger().info("Data manager shutdown complete");
        }

        getLogger().info("Example Plugin disabled successfully");
    }

    /**
     * Gets the plugin instance.
     *
     * @return the ExamplePlugin instance
     */
    public static ExamplePlugin getInstance() {
        return instance;
    }

    /**
     * Gets the NetworkDataAPI provider.
     *
     * @return the API provider
     */
    public NetworkDataAPIProvider getAPI() {
        return api;
    }

    /**
     * Gets the MongoDB database for this plugin.
     *
     * @return the MongoDB database
     */
    public MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Gets the data manager.
     *
     * @return the data manager
     */
    public ExampleDataManager getDataManager() {
        return dataManager;
    }
}
