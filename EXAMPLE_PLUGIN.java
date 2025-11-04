package com.example.integration;

import com.astroid.stijnjakobs.networkdataapi.core.api.APIRegistry;
import com.astroid.stijnjakobs.networkdataapi.core.api.NetworkDataAPIProvider;
import com.astroid.stijnjakobs.networkdataapi.core.service.PlayerDataService;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example plugin demonstrating NetworkDataAPI integration.
 *
 * This plugin shows how to:
 * - Connect to NetworkDataAPI
 * - Get and save player data
 * - Update specific fields
 * - Query player data
 * - Handle async operations properly
 */
public class ExamplePlugin extends JavaPlugin implements Listener {

    private NetworkDataAPIProvider api;
    private PlayerDataService playerData;

    @Override
    public void onEnable() {
        // Check if NetworkDataAPI is available
        if (!APIRegistry.isAvailable()) {
            getLogger().severe("========================================");
            getLogger().severe("  NetworkDataAPI not found!");
            getLogger().severe("  This plugin requires NetworkDataAPI");
            getLogger().severe("  Download: github.com/astroid/NetworkDataAPI");
            getLogger().severe("========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Get API instance
        api = APIRegistry.getAPI();
        playerData = api.getPlayerDataService();

        // Verify connection health
        if (!api.isHealthy()) {
            getLogger().warning("NetworkDataAPI database is not healthy!");
            getLogger().warning("Some features may not work correctly.");
        }

        getLogger().info("Successfully connected to NetworkDataAPI v" + api.getVersion());

        // Register event listener
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("ExamplePlugin enabled!");
    }

    /**
     * Example: Handle player join and display their stats
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Get player data asynchronously (recommended)
        playerData.getPlayerDataAsync(uuid).thenAccept(data -> {
            // This runs async - safe for database operations

            int coins = data.getInteger("coins", 0);
            int level = data.getInteger("level", 1);
            int kills = data.getInteger("kills", 0);
            int deaths = data.getInteger("deaths", 0);

            // Switch back to main thread for player interaction
            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(ChatColor.GOLD + "===== Your Stats =====");
                player.sendMessage(ChatColor.YELLOW + "Coins: " + ChatColor.WHITE + coins);
                player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
                player.sendMessage(ChatColor.YELLOW + "K/D: " + ChatColor.WHITE +
                        kills + "/" + deaths);
                player.sendMessage(ChatColor.GOLD + "====================");
            });

        }).exceptionally(throwable -> {
            // Handle errors
            getLogger().severe("Failed to load data for " + player.getName());
            throwable.printStackTrace();
            return null;
        });

        // Update last login timestamp
        playerData.updateFieldAsync(uuid, "lastLogin", System.currentTimeMillis());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only!");
            return true;
        }

        UUID uuid = player.getUniqueId();

        switch (command.getName().toLowerCase()) {
            case "balance" -> handleBalanceCommand(player);
            case "addcoins" -> handleAddCoinsCommand(player, args);
            case "stats" -> handleStatsCommand(player);
            case "leaderboard" -> handleLeaderboardCommand(player);
            case "resetdata" -> handleResetDataCommand(player);
        }

        return true;
    }

    /**
     * Example: Check player's coin balance
     */
    private void handleBalanceCommand(Player player) {
        playerData.getPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
            int coins = data.getInteger("coins", 0);

            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(ChatColor.GOLD + "Your balance: " +
                        ChatColor.YELLOW + coins + " coins");
            });
        });
    }

    /**
     * Example: Add coins to player's balance
     */
    private void handleAddCoinsCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /addcoins <amount>");
            return;
        }

        try {
            int amount = Integer.parseInt(args[0]);

            // Use increment for atomic operations
            playerData.incrementFieldAsync(player.getUniqueId(), "coins", amount)
                .thenRun(() -> {
                    Bukkit.getScheduler().runTask(this, () -> {
                        player.sendMessage(ChatColor.GREEN + "Added " + amount + " coins!");
                    });
                })
                .exceptionally(throwable -> {
                    Bukkit.getScheduler().runTask(this, () -> {
                        player.sendMessage(ChatColor.RED + "Failed to add coins!");
                    });
                    return null;
                });

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
        }
    }

    /**
     * Example: Display detailed player stats
     */
    private void handleStatsCommand(Player player) {
        playerData.getPlayerDataAsync(player.getUniqueId()).thenAccept(data -> {
            // Build stats message
            StringBuilder stats = new StringBuilder();
            stats.append(ChatColor.GOLD).append("===== Your Statistics =====\n");
            stats.append(ChatColor.YELLOW).append("Coins: ").append(ChatColor.WHITE)
                .append(data.getInteger("coins", 0)).append("\n");
            stats.append(ChatColor.YELLOW).append("Level: ").append(ChatColor.WHITE)
                .append(data.getInteger("level", 1)).append("\n");
            stats.append(ChatColor.YELLOW).append("Experience: ").append(ChatColor.WHITE)
                .append(data.getInteger("experience", 0)).append("\n");
            stats.append(ChatColor.YELLOW).append("Kills: ").append(ChatColor.WHITE)
                .append(data.getInteger("kills", 0)).append("\n");
            stats.append(ChatColor.YELLOW).append("Deaths: ").append(ChatColor.WHITE)
                .append(data.getInteger("deaths", 0)).append("\n");

            // Calculate K/D ratio
            int kills = data.getInteger("kills", 0);
            int deaths = data.getInteger("deaths", 0);
            double kd = deaths > 0 ? (double) kills / deaths : kills;
            stats.append(ChatColor.YELLOW).append("K/D Ratio: ").append(ChatColor.WHITE)
                .append(String.format("%.2f", kd)).append("\n");

            stats.append(ChatColor.GOLD).append("===========================");

            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(stats.toString());
            });
        });
    }

    /**
     * Example: Display top 10 richest players (query demonstration)
     */
    private void handleLeaderboardCommand(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading leaderboard...");

        // Query top 10 players by coins
        playerData.queryAsync(Filters.exists("coins"), 10).thenAccept(results -> {
            // Sort by coins (descending)
            results.sort((a, b) ->
                Integer.compare(b.getInteger("coins", 0), a.getInteger("coins", 0)));

            StringBuilder leaderboard = new StringBuilder();
            leaderboard.append(ChatColor.GOLD).append("===== Top 10 Richest Players =====\n");

            int rank = 1;
            for (Document doc : results) {
                String name = doc.getString("lastKnownName");
                int coins = doc.getInteger("coins", 0);

                if (name != null) {
                    leaderboard.append(ChatColor.YELLOW).append(rank).append(". ")
                        .append(ChatColor.WHITE).append(name).append(": ")
                        .append(ChatColor.GOLD).append(coins).append(" coins\n");
                    rank++;
                }
            }

            leaderboard.append(ChatColor.GOLD).append("=================================");

            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage(leaderboard.toString());
            });
        });
    }

    /**
     * Example: Reset player data
     */
    private void handleResetDataCommand(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Resetting your data...");

        // Create fresh player data
        Document freshData = new Document()
            .append("coins", 0)
            .append("level", 1)
            .append("experience", 0)
            .append("kills", 0)
            .append("deaths", 0)
            .append("firstJoin", System.currentTimeMillis())
            .append("lastLogin", System.currentTimeMillis());

        playerData.savePlayerDataAsync(player.getUniqueId(), freshData)
            .thenRun(() -> {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.sendMessage(ChatColor.GREEN + "Your data has been reset!");
                });
            })
            .exceptionally(throwable -> {
                Bukkit.getScheduler().runTask(this, () -> {
                    player.sendMessage(ChatColor.RED + "Failed to reset data!");
                });
                return null;
            });
    }

    /**
     * Example: Record a player kill (demonstrates field updates)
     */
    public void recordKill(Player killer, Player victim) {
        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        // Increment killer's kills
        playerData.incrementFieldAsync(killerUUID, "kills", 1);

        // Increment victim's deaths
        playerData.incrementFieldAsync(victimUUID, "deaths", 1);

        // Update killer's K/D ratio
        playerData.getPlayerDataAsync(killerUUID).thenAccept(data -> {
            int kills = data.getInteger("kills", 0);
            int deaths = data.getInteger("deaths", 0);
            double kd = deaths > 0 ? (double) kills / deaths : kills;

            playerData.updateFieldAsync(killerUUID, "kd_ratio", kd);
        });
    }

    /**
     * Example: Batch update multiple fields at once
     */
    public void levelUp(Player player) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("level", 2);  // These would be calculated based on current level
        updates.put("experience", 0);
        updates.put("coins", 100);  // Level up reward

        playerData.updateFieldsAsync(player.getUniqueId(), updates)
            .thenRun(() -> {
                player.sendMessage(ChatColor.GREEN + "Level up!");
            });
    }

    @Override
    public void onDisable() {
        getLogger().info("ExamplePlugin disabled!");
    }
}

