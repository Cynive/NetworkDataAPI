package com.cynive.networkdataapi.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages MongoDB operations for the example plugin.
 *
 * <p>This class demonstrates how to:</p>
 * <ul>
 *   <li>Create and manage collections</li>
 *   <li>Insert documents</li>
 *   <li>Query documents with filters</li>
 *   <li>Update documents</li>
 *   <li>Delete documents</li>
 *   <li>Create indexes for performance</li>
 * </ul>
 *
 * <p>All operations are logged for easy debugging and learning.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class ExampleDataManager {

    private final MongoDatabase database;
    private final MongoCollection<Document> exampleCollection;
    private final Logger logger;

    /**
     * Creates a new data manager.
     *
     * @param database the MongoDB database
     * @param logger   the logger for output
     */
    public ExampleDataManager(MongoDatabase database, Logger logger) {
        this.database = database;
        this.logger = logger;

        // Get or create the example collection
        this.exampleCollection = database.getCollection("example_collection");
        logger.info("Collection 'example_collection' initialized");

        // Create indexes for better query performance
        createIndexes();
    }

    /**
     * Creates indexes on the collection for better performance.
     */
    private void createIndexes() {
        logger.info("Creating indexes on example_collection...");

        // Index on 'name' field for faster name-based queries
        exampleCollection.createIndex(Indexes.ascending("name"));
        logger.info("Created index on 'name' field");

        // Index on 'value' field for faster value-based queries
        exampleCollection.createIndex(Indexes.ascending("value"));
        logger.info("Created index on 'value' field");

        // Compound index on both name and value
        exampleCollection.createIndex(Indexes.ascending("name", "value"));
        logger.info("Created compound index on 'name' and 'value' fields");
    }

    /**
     * Inserts a new document into the collection.
     *
     * @param name  the name field value
     * @param value the value field value
     * @return true if insert was successful, false otherwise
     */
    public boolean insertDocument(String name, int value) {
        try {
            logger.info("========================================");
            logger.info("INSERT OPERATION");
            logger.info("========================================");

            Document document = new Document()
                    .append("name", name)
                    .append("value", value)
                    .append("timestamp", System.currentTimeMillis())
                    .append("updated", false);

            logger.info("Creating document: " + document.toJson());

            InsertOneResult result = exampleCollection.insertOne(document);

            logger.info("Insert successful!");
            logger.info("Inserted ID: " + result.getInsertedId());
            logger.info("Document inserted into collection 'example_collection'");
            logger.info("========================================");

            return true;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("INSERT FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Queries documents by name.
     *
     * @param name the name to search for
     * @return list of matching documents
     */
    public List<Document> queryByName(String name) {
        try {
            logger.info("========================================");
            logger.info("QUERY OPERATION");
            logger.info("========================================");

            logger.info("Querying documents with name: " + name);

            Bson filter = Filters.eq("name", name);
            List<Document> results = new ArrayList<>();
            exampleCollection.find(filter).into(results);

            logger.info("Query successful!");
            logger.info("Found " + results.size() + " document(s)");

            if (!results.isEmpty()) {
                logger.info("Results:");
                for (int i = 0; i < results.size(); i++) {
                    logger.info("  [" + (i + 1) + "] " + results.get(i).toJson());
                }
            }

            logger.info("========================================");

            return results;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("QUERY FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Queries all documents in the collection.
     *
     * @return list of all documents
     */
    public List<Document> queryAll() {
        try {
            logger.info("========================================");
            logger.info("QUERY ALL OPERATION");
            logger.info("========================================");

            logger.info("Querying all documents in collection...");

            List<Document> results = new ArrayList<>();
            exampleCollection.find().into(results);

            logger.info("Query successful!");
            logger.info("Found " + results.size() + " document(s)");

            if (!results.isEmpty()) {
                logger.info("Results:");
                for (int i = 0; i < results.size(); i++) {
                    logger.info("  [" + (i + 1) + "] " + results.get(i).toJson());
                }
            }

            logger.info("========================================");

            return results;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("QUERY ALL FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Queries documents with value greater than specified amount.
     *
     * @param minValue the minimum value
     * @return list of matching documents
     */
    public List<Document> queryByValueGreaterThan(int minValue) {
        try {
            logger.info("========================================");
            logger.info("QUERY OPERATION (Value > " + minValue + ")");
            logger.info("========================================");

            logger.info("Querying documents with value > " + minValue);

            Bson filter = Filters.gt("value", minValue);
            List<Document> results = new ArrayList<>();
            exampleCollection.find(filter).into(results);

            logger.info("Query successful!");
            logger.info("Found " + results.size() + " document(s)");

            if (!results.isEmpty()) {
                logger.info("Results:");
                for (int i = 0; i < results.size(); i++) {
                    logger.info("  [" + (i + 1) + "] " + results.get(i).toJson());
                }
            }

            logger.info("========================================");

            return results;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("QUERY FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Updates a document by name.
     *
     * @param name     the name of the document to update
     * @param newValue the new value to set
     * @return true if update was successful, false otherwise
     */
    public boolean updateDocument(String name, int newValue) {
        try {
            logger.info("========================================");
            logger.info("UPDATE OPERATION");
            logger.info("========================================");

            logger.info("Updating document with name: " + name);
            logger.info("New value: " + newValue);

            Bson filter = Filters.eq("name", name);
            Bson update = Updates.combine(
                    Updates.set("value", newValue),
                    Updates.set("updated", true),
                    Updates.set("lastModified", System.currentTimeMillis())
            );

            UpdateOptions options = new UpdateOptions().upsert(false);
            UpdateResult result = exampleCollection.updateOne(filter, update, options);

            logger.info("Update operation complete!");
            logger.info("Matched documents: " + result.getMatchedCount());
            logger.info("Modified documents: " + result.getModifiedCount());

            if (result.getMatchedCount() > 0) {
                logger.info("Document updated successfully!");
            } else {
                logger.warning("No document found with name: " + name);
            }

            logger.info("========================================");

            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("UPDATE FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a document by name.
     *
     * @param name the name of the document to delete
     * @return true if delete was successful, false otherwise
     */
    public boolean deleteDocument(String name) {
        try {
            logger.info("========================================");
            logger.info("DELETE OPERATION");
            logger.info("========================================");

            logger.info("Deleting document with name: " + name);

            Bson filter = Filters.eq("name", name);
            DeleteResult result = exampleCollection.deleteOne(filter);

            logger.info("Delete operation complete!");
            logger.info("Deleted documents: " + result.getDeletedCount());

            if (result.getDeletedCount() > 0) {
                logger.info("Document deleted successfully!");
            } else {
                logger.warning("No document found with name: " + name);
            }

            logger.info("========================================");

            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("DELETE FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets statistics about the collection.
     *
     * @return statistics document
     */
    public Document getStats() {
        try {
            logger.info("========================================");
            logger.info("COLLECTION STATISTICS");
            logger.info("========================================");

            long totalDocuments = exampleCollection.countDocuments();
            logger.info("Total documents: " + totalDocuments);

            // Count updated documents
            long updatedDocuments = exampleCollection.countDocuments(Filters.eq("updated", true));
            logger.info("Updated documents: " + updatedDocuments);

            // Get collection name
            String collectionName = exampleCollection.getNamespace().getCollectionName();
            logger.info("Collection name: " + collectionName);

            // Get database name
            String databaseName = database.getName();
            logger.info("Database name: " + databaseName);

            Document stats = new Document()
                    .append("database", databaseName)
                    .append("collection", collectionName)
                    .append("totalDocuments", totalDocuments)
                    .append("updatedDocuments", updatedDocuments)
                    .append("timestamp", System.currentTimeMillis());

            logger.info("========================================");

            return stats;
        } catch (Exception e) {
            logger.severe("========================================");
            logger.severe("STATS RETRIEVAL FAILED!");
            logger.severe("Error: " + e.getMessage());
            logger.severe("========================================");
            e.printStackTrace();
            return new Document("error", e.getMessage());
        }
    }
}
