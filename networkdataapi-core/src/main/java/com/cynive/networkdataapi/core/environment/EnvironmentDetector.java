package com.cynive.networkdataapi.core.environment;

import lombok.Getter;

/**
 * Detects the runtime environment (Paper/Spigot or BungeeCord/Velocity).
 *
 * <p>This class automatically determines which server platform the plugin
 * is running on by checking for platform-specific classes in the classpath.</p>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe and the environment
 * is detected once during initialization.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class EnvironmentDetector {

    /**
     * Represents the detected server environment type.
     */
    public enum EnvironmentType {
        /**
         * Paper or Spigot server environment.
         */
        PAPER,

        /**
         * BungeeCord proxy environment.
         */
        BUNGEECORD,

        /**
         * Velocity proxy environment.
         */
        VELOCITY,

        /**
         * Unknown or unsupported environment.
         */
        UNKNOWN
    }

    @Getter
    private static EnvironmentType currentEnvironment;

    static {
        currentEnvironment = detectEnvironment();
    }

    /**
     * Detects the current server environment.
     *
     * @return the detected environment type
     */
    private static EnvironmentType detectEnvironment() {
        // Check for Paper/Spigot
        if (isClassPresent("org.bukkit.Bukkit")) {
            return EnvironmentType.PAPER;
        }

        // Check for BungeeCord
        if (isClassPresent("net.md_5.bungee.api.ProxyServer")) {
            return EnvironmentType.BUNGEECORD;
        }

        // Check for Velocity
        if (isClassPresent("com.velocitypowered.api.proxy.ProxyServer")) {
            return EnvironmentType.VELOCITY;
        }

        return EnvironmentType.UNKNOWN;
    }

    /**
     * Checks if a class is present in the classpath.
     *
     * @param className the fully qualified class name
     * @return true if the class exists, false otherwise
     */
    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if the current environment is Paper or Spigot.
     *
     * @return true if running on Paper/Spigot, false otherwise
     */
    public static boolean isPaper() {
        return currentEnvironment == EnvironmentType.PAPER;
    }

    /**
     * Checks if the current environment is BungeeCord.
     *
     * @return true if running on BungeeCord, false otherwise
     */
    public static boolean isBungeeCord() {
        return currentEnvironment == EnvironmentType.BUNGEECORD;
    }

    /**
     * Checks if the current environment is Velocity.
     *
     * @return true if running on Velocity, false otherwise
     */
    public static boolean isVelocity() {
        return currentEnvironment == EnvironmentType.VELOCITY;
    }

    /**
     * Checks if the current environment is a proxy (BungeeCord or Velocity).
     *
     * @return true if running on a proxy server, false otherwise
     */
    public static boolean isProxy() {
        return isBungeeCord() || isVelocity();
    }

    /**
     * Gets a human-readable name for the current environment.
     *
     * @return the environment name
     */
    public static String getEnvironmentName() {
        return switch (currentEnvironment) {
            case PAPER -> "Paper/Spigot";
            case BUNGEECORD -> "BungeeCord";
            case VELOCITY -> "Velocity";
            case UNKNOWN -> "Unknown";
        };
    }
}

