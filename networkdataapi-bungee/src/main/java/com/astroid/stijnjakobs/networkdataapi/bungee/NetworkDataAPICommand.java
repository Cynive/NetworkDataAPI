package com.astroid.stijnjakobs.networkdataapi.bungee;

import com.astroid.stijnjakobs.networkdataapi.core.CoreManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for NetworkDataAPI administrative commands on BungeeCord.
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
public class NetworkDataAPICommand extends Command implements TabExecutor {

    private final CoreManager coreManager;

    /**
     * Creates a new command handler.
     *
     * @param coreManager the core manager
     */
    public NetworkDataAPICommand(CoreManager coreManager) {
        super("networkdataapi", "networkdataapi.admin", "ndapi", "napi");
        this.coreManager = coreManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("networkdataapi.admin")) {
            sender.sendMessage(new TextComponent(ChatColor.RED +
                    "You don't have permission to use this command."));
            return;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "cache" -> handleCache(sender, args);
            default -> sendHelp(sender);
        }
    }

    /**
     * Handles the status command.
     */
    private void handleStatus(CommandSender sender) {
        sender.sendMessage(new TextComponent(ChatColor.GOLD + "=== NetworkDataAPI Status ==="));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Version: " +
                ChatColor.WHITE + "1.0-SNAPSHOT"));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Database: " +
                (coreManager.getDatabaseManager().isHealthy()
                        ? ChatColor.GREEN + "Connected"
                        : ChatColor.RED + "Disconnected")));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Cache Enabled: " +
                ChatColor.WHITE + coreManager.getCacheManager().isEnabled()));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Cache Size: " +
                ChatColor.WHITE + coreManager.getCacheManager().size()));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Active Threads: " +
                ChatColor.WHITE + coreManager.getAsyncExecutor().getActiveThreadCount()));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Queue Size: " +
                ChatColor.WHITE + coreManager.getAsyncExecutor().getQueueSize()));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "REST API: " +
                (coreManager.getRestApiService().isRunning()
                        ? ChatColor.GREEN + "Running"
                        : ChatColor.RED + "Disabled")));
    }

    /**
     * Handles the reload command.
     */
    private void handleReload(CommandSender sender) {
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Reloading configuration..."));

        try {
            coreManager.getConfigurationManager().load();
            sender.sendMessage(new TextComponent(ChatColor.GREEN +
                    "Configuration reloaded successfully!"));
            sender.sendMessage(new TextComponent(ChatColor.YELLOW +
                    "Note: Some changes require a full restart."));
        } catch (Exception e) {
            sender.sendMessage(new TextComponent(ChatColor.RED +
                    "Failed to reload configuration: " + e.getMessage()));
        }
    }

    /**
     * Handles cache subcommands.
     */
    private void handleCache(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED +
                    "Usage: /networkdataapi cache <stats|clear>"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "stats" -> {
                var stats = coreManager.getCacheManager().getStats();
                sender.sendMessage(new TextComponent(ChatColor.GOLD + "=== Cache Statistics ==="));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Size: " +
                        ChatColor.WHITE + coreManager.getCacheManager().size()));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Hit Count: " +
                        ChatColor.WHITE + stats.hitCount()));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Miss Count: " +
                        ChatColor.WHITE + stats.missCount()));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Hit Rate: " +
                        ChatColor.WHITE + String.format("%.2f%%", stats.hitRate() * 100)));
                sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Evictions: " +
                        ChatColor.WHITE + stats.evictionCount()));
            }
            case "clear" -> {
                coreManager.getCacheManager().invalidateAll();
                sender.sendMessage(new TextComponent(ChatColor.GREEN +
                        "Cache cleared successfully!"));
            }
            default -> sender.sendMessage(new TextComponent(ChatColor.RED +
                    "Usage: /networkdataapi cache <stats|clear>"));
        }
    }

    /**
     * Sends help information.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent(ChatColor.GOLD + "=== NetworkDataAPI Commands ==="));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "/networkdataapi status " +
                ChatColor.WHITE + "- Show API status"));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "/networkdataapi reload " +
                ChatColor.WHITE + "- Reload configuration"));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "/networkdataapi cache stats " +
                ChatColor.WHITE + "- Show cache statistics"));
        sender.sendMessage(new TextComponent(ChatColor.YELLOW + "/networkdataapi cache clear " +
                ChatColor.WHITE + "- Clear the cache"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
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

