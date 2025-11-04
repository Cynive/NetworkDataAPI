package com.astroid.stijnjakobs.networkdataapi.core.database;

import com.astroid.stijnjakobs.networkdataapi.core.config.ConfigurationManager;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Manages MongoDB connection and database access.
 *
 * <p>This class handles the lifecycle of the MongoDB client, including:
 * <ul>
 *   <li>Connection pool configuration</li>
 *   <li>Automatic reconnection and retry logic</li>
 *   <li>Graceful shutdown</li>
 *   <li>Thread-safe database access</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe. The MongoDB driver
 * handles connection pooling and thread safety internally.</p>
 *
 * <p><strong>Resource Management:</strong> Call {@link #shutdown()} to properly
 * close connections before application termination.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private MongoClient mongoClient;

    @Getter
    private MongoDatabase database;

    @Getter
    private boolean connected = false;

    private final ConfigurationManager config;

    /**
     * Creates a new database manager.
     *
     * @param config the configuration manager
     */
    public DatabaseManager(ConfigurationManager config) {
        this.config = config;
    }

    /**
     * Initializes the database connection.
     *
     * <p>This method configures the MongoDB client with connection pooling,
     * timeouts, and other settings from the configuration file.</p>
     *
     * @throws Exception if connection fails
     */
    public void connect() throws Exception {
        logger.info("Initializing MongoDB connection...");

        try {
            // Build connection string
            String uri = config.getString("mongodb.uri", "mongodb://localhost:27017");
            String username = config.getString("mongodb.username", "");
            String password = config.getString("mongodb.password", "");

            // If username/password provided, insert into URI
            if (!username.isEmpty() && !password.isEmpty()) {
                uri = uri.replace("://", "://" + username + ":" + password + "@");
            }

            ConnectionString connectionString = new ConnectionString(uri);

            // Configure connection pool settings
            ConnectionPoolSettings poolSettings = ConnectionPoolSettings.builder()
                    .maxSize(config.getInt("mongodb.max-pool-size", 100))
                    .minSize(config.getInt("mongodb.min-pool-size", 10))
                    .maxConnectionIdleTime(config.getLong("mongodb.max-connection-idle-time-ms", 60000), TimeUnit.MILLISECONDS)
                    .maxConnectionLifeTime(config.getLong("mongodb.max-connection-life-time-ms", 600000), TimeUnit.MILLISECONDS)
                    .build();

            // Configure socket settings
            SocketSettings socketSettings = SocketSettings.builder()
                    .connectTimeout(config.getInt("mongodb.connection-timeout-ms", 10000), TimeUnit.MILLISECONDS)
                    .readTimeout(config.getInt("mongodb.socket-timeout-ms", 5000), TimeUnit.MILLISECONDS)
                    .build();

            // Build client settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToConnectionPoolSettings(builder -> builder.applySettings(poolSettings))
                    .applyToSocketSettings(builder -> builder.applySettings(socketSettings))
                    .build();

            // Create client
            mongoClient = MongoClients.create(settings);

            // Get database
            String databaseName = config.getString("mongodb.database", "minecraft_network");
            database = mongoClient.getDatabase(databaseName);

            // Test connection
            database.listCollectionNames().first();

            connected = true;
            logger.info("Successfully connected to MongoDB database: {}", databaseName);
            logger.info("Connection pool configured with max size: {}, min size: {}",
                    config.getInt("mongodb.max-pool-size", 100),
                    config.getInt("mongodb.min-pool-size", 10));

        } catch (Exception e) {
            connected = false;
            logger.error("Failed to connect to MongoDB", e);
            throw new Exception("Database connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gracefully shuts down the database connection.
     *
     * <p>This method closes all connections in the pool and releases resources.
     * It should be called during plugin shutdown.</p>
     */
    public void shutdown() {
        if (mongoClient != null) {
            logger.info("Closing MongoDB connection...");
            try {
                mongoClient.close();
                connected = false;
                logger.info("MongoDB connection closed successfully");
            } catch (Exception e) {
                logger.error("Error while closing MongoDB connection", e);
            }
        }
    }

    /**
     * Gets the MongoDB client instance.
     *
     * <p>Advanced users can use this to perform custom operations not covered
     * by the service layer.</p>
     *
     * @return the MongoClient instance, or null if not connected
     */
    public MongoClient getClient() {
        return mongoClient;
    }

    /**
     * Checks if the database is currently connected and operational.
     *
     * <p>This performs a lightweight ping to verify the connection is alive.</p>
     *
     * @return true if connected and operational, false otherwise
     */
    public boolean isHealthy() {
        if (!connected || mongoClient == null) {
            return false;
        }

        try {
            database.listCollectionNames().first();
            return true;
        } catch (Exception e) {
            logger.warn("Database health check failed", e);
            return false;
        }
    }

    /**
     * Attempts to reconnect to the database.
     *
     * <p>This method is useful for recovery scenarios when the connection is lost.</p>
     *
     * @return true if reconnection was successful, false otherwise
     */
    public boolean reconnect() {
        logger.info("Attempting to reconnect to MongoDB...");
        shutdown();

        try {
            connect();
            return connected;
        } catch (Exception e) {
            logger.error("Reconnection failed", e);
            return false;
        }
    }
}

