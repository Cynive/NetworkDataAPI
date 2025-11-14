package com.cynive.networkdataapi.paper;

import com.cynive.networkdataapi.core.service.PlayerDataService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EXAMPLE listener for player connection events to manage data loading and saving.
 *
 * <p><strong>IMPORTANT:</strong> This listener is NOT automatically registered by NetworkDataAPI.
 * It serves as an example implementation for custom plugins that want to manage player data.</p>
 *
 * <p>NetworkDataAPI is a database connection layer only. Custom plugins should implement
 * their own listeners if they need to track player joins/quits.</p>
 *
 * <p>This example listener demonstrates how to:</p>
 * <ul>
 *   <li>Load player data when they join the server</li>
 *   <li>Update last login timestamp</li>
 *   <li>Pre-cache player data for instant access</li>
 * </ul>
 *
 * <p><strong>Usage in your custom plugin:</strong></p>
 * <pre>{@code
 * NetworkDataAPIProvider api = APIRegistry.getAPI();
 * PlayerDataService playerDataService = api.getPlayerDataService();
 * getServer().getPluginManager().registerEvents(
 *     new PlayerConnectionListener(playerDataService),
 *     this
 * );
 * }</pre>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class PlayerConnectionListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(PlayerConnectionListener.class);

    private final PlayerDataService playerDataService;

    /**
     * Creates a new player connection listener.
     *
     * @param playerDataService the player data service
     */
    public PlayerConnectionListener(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    /**
     * Handles player join events.
     *
     * <p>Pre-loads player data asynchronously to ensure it's cached
     * and ready for other plugins to use.</p>
     *
     * @param event the join event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // Load player data asynchronously
        playerDataService.getPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
            logger.debug("Loaded data for player: {}", player.getName());
        }).exceptionally(throwable -> {
            logger.error("Failed to load data for player: " + player.getName(), throwable);
            return null;
        });

        // Update last login timestamp
        playerDataService.updateFieldAsync(
                player.getUniqueId(),
                "lastLogin",
                System.currentTimeMillis()
        );

        playerDataService.updateFieldAsync(
                player.getUniqueId(),
                "lastKnownName",
                player.getName()
        );
    }

    /**
     * Handles player quit events.
     *
     * <p>Updates the last logout timestamp when players disconnect.</p>
     *
     * @param event the quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();

        // Update last logout timestamp
        playerDataService.updateFieldAsync(
                player.getUniqueId(),
                "lastLogout",
                System.currentTimeMillis()
        ).thenRun(() -> {
            logger.debug("Updated logout time for player: {}", player.getName());
        });
    }
}

