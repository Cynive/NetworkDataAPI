package com.astroid.stijnjakobs.networkdataapi.example;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Command handler for the example plugin.
 *
 * <p>Provides commands to demonstrate MongoDB operations:</p>
 * <ul>
 *   <li>/example insert &lt;name&gt; &lt;value&gt; - Insert a document</li>
 *   <li>/example query &lt;name&gt; - Query documents by name</li>
 *   <li>/example queryall - Query all documents</li>
 *   <li>/example queryvalue &lt;minValue&gt; - Query by value</li>
 *   <li>/example update &lt;name&gt; &lt;newValue&gt; - Update a document</li>
 *   <li>/example delete &lt;name&gt; - Delete a document</li>
 *   <li>/example stats - Show collection statistics</li>
 *   <li>/example help - Show help message</li>
 * </ul>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class ExampleCommand implements CommandExecutor, TabCompleter {

    private final ExampleDataManager dataManager;
    private final Logger logger;

    /**
     * Creates a new command handler.
     *
     * @param dataManager the data manager
     * @param logger      the logger
     */
    public ExampleCommand(ExampleDataManager dataManager, Logger logger) {
        this.dataManager = dataManager;
        this.logger = logger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "insert":
                return handleInsert(sender, args);

            case "query":
                return handleQuery(sender, args);

            case "queryall":
                return handleQueryAll(sender);

            case "queryvalue":
                return handleQueryValue(sender, args);

            case "update":
                return handleUpdate(sender, args);

            case "delete":
                return handleDelete(sender, args);

            case "stats":
                return handleStats(sender);

            case "help":
                showHelp(sender);
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sender.sendMessage(ChatColor.YELLOW + "Use /example help for available commands");
                return true;
        }
    }

    /**
     * Handles the insert command.
     */
    private boolean handleInsert(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /example insert <name> <value>");
            return true;
        }

        String name = args[1];
        int value;

        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Value must be a number!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Inserting document...");
        boolean success = dataManager.insertDocument(name, value);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "✓ Document inserted successfully!");
            sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + name);
            sender.sendMessage(ChatColor.GRAY + "Value: " + ChatColor.WHITE + value);
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        } else {
            sender.sendMessage(ChatColor.RED + "✗ Failed to insert document");
            sender.sendMessage(ChatColor.GRAY + "Check server logs for error details");
        }

        return true;
    }

    /**
     * Handles the query command.
     */
    private boolean handleQuery(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /example query <name>");
            return true;
        }

        String name = args[1];

        sender.sendMessage(ChatColor.YELLOW + "Querying documents with name: " + name);
        List<Document> results = dataManager.queryByName(name);

        if (results.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "No documents found with name: " + name);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Found " + results.size() + " document(s):");
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                sender.sendMessage(ChatColor.GRAY + "[" + (i + 1) + "] " +
                        ChatColor.WHITE + "Name: " + doc.getString("name") +
                        ChatColor.GRAY + ", Value: " + ChatColor.WHITE + doc.getInteger("value") +
                        ChatColor.GRAY + ", Updated: " + ChatColor.WHITE + doc.getBoolean("updated", false));
            }
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        }

        return true;
    }

    /**
     * Handles the queryall command.
     */
    private boolean handleQueryAll(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Querying all documents...");
        List<Document> results = dataManager.queryAll();

        if (results.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "No documents found in collection");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Found " + results.size() + " document(s):");
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                sender.sendMessage(ChatColor.GRAY + "[" + (i + 1) + "] " +
                        ChatColor.WHITE + "Name: " + doc.getString("name") +
                        ChatColor.GRAY + ", Value: " + ChatColor.WHITE + doc.getInteger("value") +
                        ChatColor.GRAY + ", Updated: " + ChatColor.WHITE + doc.getBoolean("updated", false));
            }
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        }

        return true;
    }

    /**
     * Handles the queryvalue command.
     */
    private boolean handleQueryValue(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /example queryvalue <minValue>");
            return true;
        }

        int minValue;

        try {
            minValue = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "MinValue must be a number!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Querying documents with value > " + minValue);
        List<Document> results = dataManager.queryByValueGreaterThan(minValue);

        if (results.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "No documents found with value > " + minValue);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Found " + results.size() + " document(s):");
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                sender.sendMessage(ChatColor.GRAY + "[" + (i + 1) + "] " +
                        ChatColor.WHITE + "Name: " + doc.getString("name") +
                        ChatColor.GRAY + ", Value: " + ChatColor.WHITE + doc.getInteger("value") +
                        ChatColor.GRAY + ", Updated: " + ChatColor.WHITE + doc.getBoolean("updated", false));
            }
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        }

        return true;
    }

    /**
     * Handles the update command.
     */
    private boolean handleUpdate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /example update <name> <newValue>");
            return true;
        }

        String name = args[1];
        int newValue;

        try {
            newValue = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "NewValue must be a number!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Updating document...");
        boolean success = dataManager.updateDocument(name, newValue);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "✓ Document updated successfully!");
            sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + name);
            sender.sendMessage(ChatColor.GRAY + "New Value: " + ChatColor.WHITE + newValue);
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        } else {
            sender.sendMessage(ChatColor.GOLD + "✗ No document found with name: " + name);
            sender.sendMessage(ChatColor.GRAY + "Check server logs for details");
        }

        return true;
    }

    /**
     * Handles the delete command.
     */
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /example delete <name>");
            return true;
        }

        String name = args[1];

        sender.sendMessage(ChatColor.YELLOW + "Deleting document...");
        boolean success = dataManager.deleteDocument(name);

        if (success) {
            sender.sendMessage(ChatColor.GREEN + "✓ Document deleted successfully!");
            sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + name);
            sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");
        } else {
            sender.sendMessage(ChatColor.GOLD + "✗ No document found with name: " + name);
            sender.sendMessage(ChatColor.GRAY + "Check server logs for details");
        }

        return true;
    }

    /**
     * Handles the stats command.
     */
    private boolean handleStats(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Retrieving collection statistics...");
        Document stats = dataManager.getStats();

        if (stats.containsKey("error")) {
            sender.sendMessage(ChatColor.RED + "✗ Failed to retrieve statistics");
            sender.sendMessage(ChatColor.GRAY + "Error: " + stats.getString("error"));
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.GREEN + "Collection Statistics");
        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.GRAY + "Database: " + ChatColor.WHITE + stats.getString("database"));
        sender.sendMessage(ChatColor.GRAY + "Collection: " + ChatColor.WHITE + stats.getString("collection"));
        sender.sendMessage(ChatColor.GRAY + "Total Documents: " + ChatColor.WHITE + stats.getLong("totalDocuments"));
        sender.sendMessage(ChatColor.GRAY + "Updated Documents: " + ChatColor.WHITE + stats.getLong("updatedDocuments"));
        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.GRAY + "Check server logs for detailed operation info");

        return true;
    }

    /**
     * Shows help message.
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.GREEN + "NetworkDataAPI Example Plugin");
        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.YELLOW + "Available Commands:");
        sender.sendMessage(ChatColor.GRAY + "/example insert <name> <value>" + ChatColor.WHITE + " - Insert a document");
        sender.sendMessage(ChatColor.GRAY + "/example query <name>" + ChatColor.WHITE + " - Query documents by name");
        sender.sendMessage(ChatColor.GRAY + "/example queryall" + ChatColor.WHITE + " - Query all documents");
        sender.sendMessage(ChatColor.GRAY + "/example queryvalue <min>" + ChatColor.WHITE + " - Query by value > min");
        sender.sendMessage(ChatColor.GRAY + "/example update <name> <value>" + ChatColor.WHITE + " - Update a document");
        sender.sendMessage(ChatColor.GRAY + "/example delete <name>" + ChatColor.WHITE + " - Delete a document");
        sender.sendMessage(ChatColor.GRAY + "/example stats" + ChatColor.WHITE + " - Show collection stats");
        sender.sendMessage(ChatColor.GRAY + "/example help" + ChatColor.WHITE + " - Show this help");
        sender.sendMessage(ChatColor.GREEN + "========================================");
        sender.sendMessage(ChatColor.GRAY + "All operations are logged to the server console");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList(
                    "insert", "query", "queryall", "queryvalue",
                    "update", "delete", "stats", "help"
            );
            String partial = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(cmd -> cmd.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument - provide hints
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "insert":
                    completions.add("<name>");
                    break;
                case "query":
                case "update":
                case "delete":
                    completions.add("<name>");
                    break;
                case "queryvalue":
                    completions.add("<minValue>");
                    break;
            }
        } else if (args.length == 3) {
            // Third argument - provide hints
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "insert":
                case "update":
                    completions.add("<value>");
                    break;
            }
        }

        return completions;
    }
}
