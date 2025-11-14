package com.cynive.networkdataapi.bungee;

import com.cynive.networkdataapi.core.service.PlayerDataService;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for player connection events to manage data loading and saving.
 *
 * <p>This listener automatically:</p>
 * <ul>
 *   <li>Loads player data when they connect to the proxy</li>
 *   <li>Updates last login timestamp</li>
 *   <li>Pre-caches player data for instant access</li>
 * </ul>
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
     * Handles post login events.
     *
     * <p>Pre-loads player data asynchronously to ensure it's cached
     * and ready for backend servers to use.</p>
     *
     * @param event the post login event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event) {
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
                "lastProxyLogin",
                System.currentTimeMillis()
        );

        playerDataService.updateFieldAsync(
                player.getUniqueId(),
                "lastKnownName",
                player.getName()
        );
    }

    /**
     * Handles player disconnect events.
     *
     * <p>Updates the last logout timestamp when players disconnect from the proxy.</p>
     *
     * @param event the disconnect event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        var player = event.getPlayer();

        // Update last logout timestamp
        playerDataService.updateFieldAsync(
                player.getUniqueId(),
                "lastProxyLogout",
                System.currentTimeMillis()
        ).thenRun(() -> {
            logger.debug("Updated logout time for player: {}", player.getName());
        });
    }
}

