package com.cynive.networkdataapi.paper;

import com.cynive.networkdataapi.core.CoreManager;
import com.cynive.networkdataapi.core.api.APIRegistry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * NetworkDataAPI implementation for Paper/Spigot servers.
 *
 * <p>This plugin provides enterprise-level data synchronization for Minecraft
 * networks using MongoDB as the backend. It supports:</p>
 * <ul>
 *   <li>Asynchronous player data operations</li>
 *   <li>High-performance in-memory caching</li>
 *   <li>Connection pooling and automatic reconnection</li>
 *   <li>Optional REST API for external integrations</li>
 *   <li>Clean public API for other plugins</li>
 * </ul>
 *
 * <p><strong>For Plugin Developers:</strong></p>
 * <p>To use this API in your plugin, add it as a dependency in your plugin.yml
 * and use the {@link APIRegistry} to access services:</p>
 * <pre>{@code
 * NetworkDataAPIProvider api = APIRegistry.getAPI();
 * PlayerDataService playerData = api.getPlayerDataService();
 * }</pre>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public final class NetworkDataAPI extends JavaPlugin {

    private static NetworkDataAPI instance;
    private CoreManager coreManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Initialize core manager
            coreManager = new CoreManager();
            coreManager.initialize(getDataFolder());

            // Register API
            APIRegistry.register(coreManager);

            // Register event listeners
            registerListeners();

            // Register commands
            registerCommands();

            getLogger().info("NetworkDataAPI has been enabled successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to enable NetworkDataAPI: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling NetworkDataAPI...");

        // Unregister API
        APIRegistry.unregister();

        // Shutdown core manager
        if (coreManager != null) {
            coreManager.shutdown();
        }

        getLogger().info("NetworkDataAPI has been disabled.");
    }

    /**
     * Registers event listeners for automatic data management.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new PlayerConnectionListener(coreManager.getPlayerDataService()),
                this
        );
    }

    /**
     * Registers plugin commands.
     */
    private void registerCommands() {
        NetworkDataAPICommand commandExecutor = new NetworkDataAPICommand(coreManager);
        getCommand("networkdataapi").setExecutor(commandExecutor);
        getCommand("networkdataapi").setTabCompleter(commandExecutor);
    }

    /**
     * Gets the plugin instance.
     *
     * @return the NetworkDataAPI instance
     */
    public static NetworkDataAPI getInstance() {
        return instance;
    }

    /**
     * Gets the core manager instance.
     *
     * <p><strong>Note:</strong> External plugins should use {@link APIRegistry#getAPI()}
     * instead of accessing the core manager directly.</p>
     *
     * @return the core manager
     */
    public CoreManager getCoreManager() {
        return coreManager;
    }
}

