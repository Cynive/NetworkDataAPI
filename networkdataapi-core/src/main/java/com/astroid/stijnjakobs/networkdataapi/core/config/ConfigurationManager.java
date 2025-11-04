package com.astroid.stijnjakobs.networkdataapi.core.config;

import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration manager for NetworkDataAPI.
 * Handles loading, parsing, and providing access to configuration values.
 *
 * <p>Supports YAML-like configuration with nested keys using dot notation.
 * Thread-safe for reading operations after initial load.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
@Getter
public class ConfigurationManager {

    private final Map<String, Object> configData = new HashMap<>();
    private final File configFile;

    /**
     * Creates a new configuration manager.
     *
     * @param configFile the configuration file to load
     */
    public ConfigurationManager(File configFile) {
        this.configFile = configFile;
    }

    /**
     * Loads the configuration from file.
     * Creates a default configuration if the file doesn't exist.
     *
     * @throws IOException if an I/O error occurs
     */
    public void load() throws IOException {
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        parseConfig();
    }

    /**
     * Creates a default configuration file with sensible defaults.
     *
     * @throws IOException if an I/O error occurs
     */
    private void createDefaultConfig() throws IOException {
        configFile.getParentFile().mkdirs();

        String defaultConfig = """
                # NetworkDataAPI Configuration
                # MongoDB Connection Settings
                mongodb:
                  uri: "mongodb://localhost:27017"
                  database: "minecraft_network"
                  username: ""
                  password: ""
                  # Connection pool settings
                  max-pool-size: 100
                  min-pool-size: 10
                  connection-timeout-ms: 10000
                  socket-timeout-ms: 5000
                  server-selection-timeout-ms: 5000
                  max-connection-idle-time-ms: 60000
                  max-connection-life-time-ms: 600000
                
                # Cache Settings
                cache:
                  enabled: true
                  # Maximum cache size (number of entries)
                  max-size: 10000
                  # Cache expiration time in minutes
                  expire-after-write-minutes: 5
                  # Cache expiration for idle entries in minutes
                  expire-after-access-minutes: 10
                
                # REST API Settings (optional)
                rest-api:
                  enabled: false
                  port: 8080
                  # API key for authentication (leave empty to disable)
                  api-key: ""
                  # Allowed IP addresses (empty = allow all)
                  allowed-ips:
                    - "127.0.0.1"
                
                # Async Executor Settings
                async:
                  # Core thread pool size
                  core-pool-size: 4
                  # Maximum thread pool size
                  max-pool-size: 16
                  # Thread keep-alive time in seconds
                  keep-alive-seconds: 60
                
                # Logging Settings
                logging:
                  # Log level: TRACE, DEBUG, INFO, WARN, ERROR
                  level: "INFO"
                  # Enable debug mode for detailed logging
                  debug: false
                
                # Environment Detection (auto-detected, manual override)
                environment:
                  # Options: AUTO, PAPER, BUNGEECORD
                  type: "AUTO"
                """;

        Files.writeString(configFile.toPath(), defaultConfig);
    }

    /**
     * Parses the configuration file into a map structure.
     *
     * @throws IOException if an I/O error occurs
     */
    private void parseConfig() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            String currentSection = "";
            int currentIndent = 0;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Calculate indentation
                int indent = 0;
                while (indent < line.length() && line.charAt(indent) == ' ') {
                    indent++;
                }

                String trimmed = line.trim();

                // Handle list items
                if (trimmed.startsWith("-")) {
                    String listItem = trimmed.substring(1).trim();
                    if (listItem.startsWith("\"") && listItem.endsWith("\"")) {
                        listItem = listItem.substring(1, listItem.length() - 1);
                    }

                    @SuppressWarnings("unchecked")
                    java.util.List<String> list = (java.util.List<String>) configData.computeIfAbsent(
                            currentSection, k -> new java.util.ArrayList<String>());
                    list.add(listItem);
                    continue;
                }

                // Handle key-value pairs
                if (trimmed.contains(":")) {
                    String[] parts = trimmed.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts.length > 1 ? parts[1].trim() : "";

                    // Remove quotes from string values
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    // Update current section
                    if (value.isEmpty() || value.equals("")) {
                        if (indent == 0) {
                            currentSection = key;
                            currentIndent = indent;
                        } else if (indent > currentIndent) {
                            currentSection = currentSection.isEmpty() ? key : currentSection + "." + key;
                            currentIndent = indent;
                        } else {
                            // Going back to previous level
                            String[] sectionParts = currentSection.split("\\.");
                            int levelsUp = (currentIndent - indent) / 2;
                            currentSection = String.join(".", java.util.Arrays.copyOfRange(sectionParts, 0, Math.max(0, sectionParts.length - levelsUp)));
                            currentSection = currentSection.isEmpty() ? key : currentSection + "." + key;
                            currentIndent = indent;
                        }
                    } else {
                        // Store the value
                        String fullKey = currentSection.isEmpty() ? key : currentSection + "." + key;
                        configData.put(fullKey, parseValue(value));
                    }
                }
            }
        }
    }

    /**
     * Parses a configuration value to its appropriate type.
     *
     * @param value the string value to parse
     * @return the parsed value (String, Integer, Long, Boolean, or Double)
     */
    private Object parseValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * Gets a string value from the configuration.
     *
     * @param path the configuration path (e.g., "mongodb.uri")
     * @param defaultValue the default value if the path doesn't exist
     * @return the configuration value or default value
     */
    public String getString(String path, String defaultValue) {
        Object value = configData.get(path);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Gets an integer value from the configuration.
     *
     * @param path the configuration path
     * @param defaultValue the default value if the path doesn't exist
     * @return the configuration value or default value
     */
    public int getInt(String path, int defaultValue) {
        Object value = configData.get(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Gets a long value from the configuration.
     *
     * @param path the configuration path
     * @param defaultValue the default value if the path doesn't exist
     * @return the configuration value or default value
     */
    public long getLong(String path, long defaultValue) {
        Object value = configData.get(path);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    /**
     * Gets a boolean value from the configuration.
     *
     * @param path the configuration path
     * @param defaultValue the default value if the path doesn't exist
     * @return the configuration value or default value
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = configData.get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Gets a double value from the configuration.
     *
     * @param path the configuration path
     * @param defaultValue the default value if the path doesn't exist
     * @return the configuration value or default value
     */
    public double getDouble(String path, double defaultValue) {
        Object value = configData.get(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Gets a list of strings from the configuration.
     *
     * @param path the configuration path
     * @return the list of strings, or an empty list if not found
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getStringList(String path) {
        Object value = configData.get(path);
        if (value instanceof java.util.List) {
            return (java.util.List<String>) value;
        }
        return new java.util.ArrayList<>();
    }
}

