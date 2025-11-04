package com.astroid.stijnjakobs.networkdataapi.paper;

import com.astroid.stijnjakobs.networkdataapi.core.CoreManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for NetworkDataAPI administrative commands.
 *
 * <p>Available commands:</p>
 * <ul>
 *   <li>/networkdataapi status - Shows API status and statistics</li>
 *   <li>/networkdataapi reload - Reloads the configuration</li>
 *   <li>/networkdataapi cache stats - Shows cache statistics</li>
 *   <li>/networkdataapi cache clear - Clears the cache</li>
 * </ul>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class NetworkDataAPICommand implements CommandExecutor, TabCompleter {

    private final CoreManager coreManager;

    /**
     * Creates a new command handler.
     *
     * @param coreManager the core manager
     */
    public NetworkDataAPICommand(CoreManager coreManager) {
        this.coreManager = coreManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("networkdataapi.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "cache" -> handleCache(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    /**
     * Handles the status command.
     */
    private void handleStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== NetworkDataAPI Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + "1.0-SNAPSHOT");
        sender.sendMessage(ChatColor.YELLOW + "Database: " +
                (coreManager.getDatabaseManager().isHealthy()
                        ? ChatColor.GREEN + "Connected"
                        : ChatColor.RED + "Disconnected"));
        sender.sendMessage(ChatColor.YELLOW + "Cache Enabled: " + ChatColor.WHITE +
                coreManager.getCacheManager().isEnabled());
        sender.sendMessage(ChatColor.YELLOW + "Cache Size: " + ChatColor.WHITE +
                coreManager.getCacheManager().size());
        sender.sendMessage(ChatColor.YELLOW + "Active Threads: " + ChatColor.WHITE +
                coreManager.getAsyncExecutor().getActiveThreadCount());
        sender.sendMessage(ChatColor.YELLOW + "Queue Size: " + ChatColor.WHITE +
                coreManager.getAsyncExecutor().getQueueSize());
        sender.sendMessage(ChatColor.YELLOW + "REST API: " +
                (coreManager.getRestApiService().isRunning()
                        ? ChatColor.GREEN + "Running"
                        : ChatColor.RED + "Disabled"));
    }

    /**
     * Handles the reload command.
     */
    private void handleReload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading configuration...");

        try {
            coreManager.getConfigurationManager().load();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
            sender.sendMessage(ChatColor.YELLOW + "Note: Some changes require a full restart.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload configuration: " + e.getMessage());
        }
    }

    /**
     * Handles cache subcommands.
     */
    private void handleCache(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /networkdataapi cache <stats|clear>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "stats" -> {
                var stats = coreManager.getCacheManager().getStats();
                sender.sendMessage(ChatColor.GOLD + "=== Cache Statistics ===");
                sender.sendMessage(ChatColor.YELLOW + "Size: " + ChatColor.WHITE +
                        coreManager.getCacheManager().size());
                sender.sendMessage(ChatColor.YELLOW + "Hit Count: " + ChatColor.WHITE +
                        stats.hitCount());
                sender.sendMessage(ChatColor.YELLOW + "Miss Count: " + ChatColor.WHITE +
                        stats.missCount());
                sender.sendMessage(ChatColor.YELLOW + "Hit Rate: " + ChatColor.WHITE +
                        String.format("%.2f%%", stats.hitRate() * 100));
                sender.sendMessage(ChatColor.YELLOW + "Evictions: " + ChatColor.WHITE +
                        stats.evictionCount());
            }
            case "clear" -> {
                coreManager.getCacheManager().invalidateAll();
                sender.sendMessage(ChatColor.GREEN + "Cache cleared successfully!");
            }
            default -> sender.sendMessage(ChatColor.RED +
                    "Usage: /networkdataapi cache <stats|clear>");
        }
    }

    /**
     * Sends help information.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== NetworkDataAPI Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/networkdataapi status " +
                ChatColor.WHITE + "- Show API status");
        sender.sendMessage(ChatColor.YELLOW + "/networkdataapi reload " +
                ChatColor.WHITE + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/networkdataapi cache stats " +
                ChatColor.WHITE + "- Show cache statistics");
        sender.sendMessage(ChatColor.YELLOW + "/networkdataapi cache clear " +
                ChatColor.WHITE + "- Clear the cache");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("networkdataapi.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("status", "reload", "cache");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("cache")) {
            return Arrays.asList("stats", "clear");
        }

        return new ArrayList<>();
    }
}

