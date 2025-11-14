package com.cynive.networkdataapi.core.rest;

import com.cynive.networkdataapi.core.config.ConfigurationManager;
import com.cynive.networkdataapi.core.service.PlayerDataService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.UUID;

import static spark.Spark.*;

/**
 * Lightweight REST API for external access to player data.
 *
 * <p>This service provides HTTP endpoints for external tools and services
 * to interact with the network data. Endpoints include:</p>
 * <ul>
 *   <li>GET /api/player/{uuid} - Get player data</li>
 *   <li>POST /api/player/{uuid} - Update player data</li>
 *   <li>DELETE /api/player/{uuid} - Delete player data</li>
 *   <li>GET /api/health - Health check</li>
 *   <li>GET /api/stats - API statistics</li>
 * </ul>
 *
 * <p><strong>Security:</strong> Supports API key authentication and IP whitelisting
 * when configured. Configure these in config.yml under rest-api section.</p>
 *
 * <p><strong>Example Usage:</strong></p>
 * <pre>{@code
 * // Get player data
 * curl -H "X-API-Key: your-api-key" http://localhost:8080/api/player/uuid-here
 *
 * // Update player data
 * curl -X POST -H "X-API-Key: your-api-key" \
 *      -H "Content-Type: application/json" \
 *      -d '{"coins": 1000, "level": 5}' \
 *      http://localhost:8080/api/player/uuid-here
 * }</pre>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class RESTApiService {

    private static final Logger logger = LoggerFactory.getLogger(RESTApiService.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final ConfigurationManager config;
    private final PlayerDataService playerDataService;
    private final boolean enabled;
    private final String apiKey;
    private final List<String> allowedIps;

    private boolean running = false;

    /**
     * Creates a new REST API service.
     *
     * @param config the configuration manager
     * @param playerDataService the player data service
     */
    public RESTApiService(ConfigurationManager config, PlayerDataService playerDataService) {
        this.config = config;
        this.playerDataService = playerDataService;
        this.enabled = config.getBoolean("rest-api.enabled", false);
        this.apiKey = config.getString("rest-api.api-key", "");
        this.allowedIps = config.getStringList("rest-api.allowed-ips");
    }

    /**
     * Starts the REST API server.
     */
    public void start() {
        if (!enabled) {
            logger.info("REST API is disabled in configuration");
            return;
        }

        int port = config.getInt("rest-api.port", 8080);

        try {
            port(port);

            // Configure exception handling
            exception(Exception.class, (e, req, res) -> {
                logger.error("Unhandled exception in REST API", e);
                res.status(500);
                res.body(gson.toJson(new ErrorResponse("Internal server error")));
            });

            // Configure request logging
            before((req, res) -> {
                logger.debug("REST API Request: {} {} from {}",
                        req.requestMethod(), req.pathInfo(), req.ip());
            });

            // Configure response type
            after((req, res) -> {
                res.type("application/json");
            });

            // Setup routes
            setupRoutes();

            // Wait for initialization
            awaitInitialization();

            running = true;
            logger.info("REST API started on port {}", port);

        } catch (Exception e) {
            logger.error("Failed to start REST API", e);
        }
    }

    /**
     * Stops the REST API server.
     */
    public void stop() {
        if (running) {
            stop();
            logger.info("REST API stopped");
            running = false;
        }
    }

    /**
     * Sets up all API routes.
     */
    private void setupRoutes() {
        // Health check endpoint
        get("/api/health", this::handleHealth);

        // Stats endpoint
        get("/api/stats", this::handleStats, gson::toJson);

        // Player data endpoints
        get("/api/player/:uuid", this::handleGetPlayer, gson::toJson);
        post("/api/player/:uuid", this::handleUpdatePlayer, gson::toJson);
        delete("/api/player/:uuid", this::handleDeletePlayer, gson::toJson);

        // 404 handler
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Endpoint not found"));
        });
    }

    /**
     * Handles health check requests.
     */
    private String handleHealth(Request req, Response res) {
        res.type("application/json");
        return "{\"status\":\"healthy\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }

    /**
     * Handles API statistics requests.
     */
    private Object handleStats(Request req, Response res) {
        if (!authenticate(req, res)) {
            return new ErrorResponse("Unauthorized");
        }

        return new StatsResponse(
                running,
                System.currentTimeMillis()
        );
    }

    /**
     * Handles get player data requests.
     */
    private Object handleGetPlayer(Request req, Response res) {
        if (!authenticate(req, res)) {
            return new ErrorResponse("Unauthorized");
        }

        try {
            UUID uuid = UUID.fromString(req.params(":uuid"));
            Document data = playerDataService.getPlayerData(uuid);

            if (data == null) {
                res.status(404);
                return new ErrorResponse("Player not found");
            }

            res.status(200);
            return documentToJson(data);

        } catch (IllegalArgumentException e) {
            res.status(400);
            return new ErrorResponse("Invalid UUID format");
        } catch (Exception e) {
            logger.error("Error getting player data", e);
            res.status(500);
            return new ErrorResponse("Internal server error");
        }
    }

    /**
     * Handles update player data requests.
     */
    private Object handleUpdatePlayer(Request req, Response res) {
        if (!authenticate(req, res)) {
            return new ErrorResponse("Unauthorized");
        }

        try {
            UUID uuid = UUID.fromString(req.params(":uuid"));

            // Parse request body
            @SuppressWarnings("unchecked")
            var updateData = gson.fromJson(req.body(), java.util.Map.class);

            if (updateData == null || updateData.isEmpty()) {
                res.status(400);
                return new ErrorResponse("Request body is required");
            }

            // Get existing data or create new
            Document data = playerDataService.getPlayerData(uuid);

            // Update fields
            updateData.forEach((key, value) -> {
                data.append(String.valueOf(key), value);
            });

            // Save
            playerDataService.savePlayerData(uuid, data);

            res.status(200);
            return new SuccessResponse("Player data updated successfully");

        } catch (IllegalArgumentException e) {
            res.status(400);
            return new ErrorResponse("Invalid UUID format");
        } catch (Exception e) {
            logger.error("Error updating player data", e);
            res.status(500);
            return new ErrorResponse("Internal server error");
        }
    }

    /**
     * Handles delete player data requests.
     */
    private Object handleDeletePlayer(Request req, Response res) {
        if (!authenticate(req, res)) {
            return new ErrorResponse("Unauthorized");
        }

        try {
            UUID uuid = UUID.fromString(req.params(":uuid"));
            boolean deleted = playerDataService.deletePlayerData(uuid);

            if (deleted) {
                res.status(200);
                return new SuccessResponse("Player data deleted successfully");
            } else {
                res.status(404);
                return new ErrorResponse("Player not found");
            }

        } catch (IllegalArgumentException e) {
            res.status(400);
            return new ErrorResponse("Invalid UUID format");
        } catch (Exception e) {
            logger.error("Error deleting player data", e);
            res.status(500);
            return new ErrorResponse("Internal server error");
        }
    }

    /**
     * Authenticates a request using API key and IP whitelist.
     */
    private boolean authenticate(Request req, Response res) {
        // Check IP whitelist
        if (!allowedIps.isEmpty() && !allowedIps.contains(req.ip())) {
            res.status(403);
            logger.warn("REST API access denied from IP: {}", req.ip());
            return false;
        }

        // Check API key
        if (!apiKey.isEmpty()) {
            String providedKey = req.headers("X-API-Key");
            if (providedKey == null || !providedKey.equals(apiKey)) {
                res.status(401);
                logger.warn("REST API access denied: invalid or missing API key from {}", req.ip());
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a MongoDB Document to JSON string.
     */
    private String documentToJson(Document doc) {
        return doc.toJson();
    }

    /**
     * Checks if the REST API is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    // Response classes

    private record ErrorResponse(String error) {}

    private record SuccessResponse(String message) {}

    private record StatsResponse(boolean running, long timestamp) {}
}

