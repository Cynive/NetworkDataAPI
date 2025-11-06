package com.astroid.stijnjakobs.networkdataapi.core.service;

import com.astroid.stijnjakobs.networkdataapi.core.async.AsyncExecutor;
import com.astroid.stijnjakobs.networkdataapi.core.redis.RedisManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing Redis data operations.
 *
 * <p>This service provides high-level methods for interacting with Redis,
 * including string operations, hash operations, set operations, and more.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Thread-safe operations</li>
 *   <li>Async variants for all operations</li>
 *   <li>Automatic connection handling</li>
 *   <li>Comprehensive error handling</li>
 *   <li>Support for TTL and expiration</li>
 * </ul>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class RedisDataService {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataService.class);

    private final RedisManager redisManager;
    private final AsyncExecutor asyncExecutor;

    /**
     * Creates a new Redis data service.
     *
     * @param redisManager the Redis manager
     * @param asyncExecutor the async executor
     */
    public RedisDataService(RedisManager redisManager, AsyncExecutor asyncExecutor) {
        this.redisManager = redisManager;
        this.asyncExecutor = asyncExecutor;
    }

    // ========== String Operations ==========

    /**
     * Sets a string value in Redis.
     *
     * @param key the key
     * @param value the value
     */
    public void set(String key, String value) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            logger.error("Failed to set key: {}", key, e);
        }
    }

    /**
     * Sets a string value with expiration in Redis.
     *
     * @param key the key
     * @param value the value
     * @param ttlSeconds time to live in seconds
     */
    public void setWithExpiry(String key, String value, long ttlSeconds) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        } catch (Exception e) {
            logger.error("Failed to set key with expiry: {}", key, e);
        }
    }

    /**
     * Gets a string value from Redis.
     *
     * @param key the key
     * @return the value, or null if not found
     */
    public String get(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            logger.error("Failed to get key: {}", key, e);
            return null;
        }
    }

    /**
     * Sets a string value asynchronously.
     *
     * @param key the key
     * @param value the value
     * @return a CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> setAsync(String key, String value) {
        return asyncExecutor.supply(() -> {
            set(key, value);
            return null;
        });
    }

    /**
     * Gets a string value asynchronously.
     *
     * @param key the key
     * @return a CompletableFuture with the value
     */
    public CompletableFuture<String> getAsync(String key) {
        return asyncExecutor.supply(() -> get(key));
    }

    /**
     * Deletes one or more keys.
     *
     * @param keys the keys to delete
     * @return the number of keys deleted
     */
    public long delete(String... keys) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.del(keys);
        } catch (Exception e) {
            logger.error("Failed to delete keys: {}", Arrays.toString(keys), e);
            return 0;
        }
    }

    /**
     * Checks if a key exists.
     *
     * @param key the key
     * @return true if the key exists
     */
    public boolean exists(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("Failed to check key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Sets the expiration for a key.
     *
     * @param key the key
     * @param seconds time to live in seconds
     * @return true if successful
     */
    public boolean expire(String key, long seconds) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.expire(key, seconds) == 1;
        } catch (Exception e) {
            logger.error("Failed to set expiration for key: {}", key, e);
            return false;
        }
    }

    /**
     * Gets the time to live for a key.
     *
     * @param key the key
     * @return the TTL in seconds, -1 if no expiry, -2 if key doesn't exist
     */
    public long getTTL(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.ttl(key);
        } catch (Exception e) {
            logger.error("Failed to get TTL for key: {}", key, e);
            return -2;
        }
    }

    // ========== Hash Operations ==========

    /**
     * Sets a field in a hash.
     *
     * @param key the hash key
     * @param field the field name
     * @param value the field value
     */
    public void hset(String key, String field, String value) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error("Failed to set hash field: {} -> {}", key, field, e);
        }
    }

    /**
     * Gets a field from a hash.
     *
     * @param key the hash key
     * @param field the field name
     * @return the field value, or null if not found
     */
    public String hget(String key, String field) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.hget(key, field);
        } catch (Exception e) {
            logger.error("Failed to get hash field: {} -> {}", key, field, e);
            return null;
        }
    }

    /**
     * Sets multiple fields in a hash.
     *
     * @param key the hash key
     * @param hash the map of field-value pairs
     */
    public void hmset(String key, Map<String, String> hash) {
        try (Jedis jedis = redisManager.getResource()) {
            jedis.hmset(key, hash);
        } catch (Exception e) {
            logger.error("Failed to set hash fields: {}", key, e);
        }
    }

    /**
     * Gets all fields and values from a hash.
     *
     * @param key the hash key
     * @return a map of field-value pairs
     */
    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error("Failed to get all hash fields: {}", key, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Deletes one or more fields from a hash.
     *
     * @param key the hash key
     * @param fields the fields to delete
     * @return the number of fields deleted
     */
    public long hdel(String key, String... fields) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.hdel(key, fields);
        } catch (Exception e) {
            logger.error("Failed to delete hash fields: {}", key, e);
            return 0;
        }
    }

    /**
     * Checks if a field exists in a hash.
     *
     * @param key the hash key
     * @param field the field name
     * @return true if the field exists
     */
    public boolean hexists(String key, String field) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.hexists(key, field);
        } catch (Exception e) {
            logger.error("Failed to check hash field existence: {} -> {}", key, field, e);
            return false;
        }
    }

    /**
     * Increments a hash field by a value.
     *
     * @param key the hash key
     * @param field the field name
     * @param increment the increment value
     * @return the new value after incrementing
     */
    public long hincrBy(String key, String field, long increment) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.hincrBy(key, field, increment);
        } catch (Exception e) {
            logger.error("Failed to increment hash field: {} -> {}", key, field, e);
            return 0;
        }
    }

    // ========== Set Operations ==========

    /**
     * Adds one or more members to a set.
     *
     * @param key the set key
     * @param members the members to add
     * @return the number of members added
     */
    public long sadd(String key, String... members) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.sadd(key, members);
        } catch (Exception e) {
            logger.error("Failed to add to set: {}", key, e);
            return 0;
        }
    }

    /**
     * Removes one or more members from a set.
     *
     * @param key the set key
     * @param members the members to remove
     * @return the number of members removed
     */
    public long srem(String key, String... members) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.srem(key, members);
        } catch (Exception e) {
            logger.error("Failed to remove from set: {}", key, e);
            return 0;
        }
    }

    /**
     * Gets all members of a set.
     *
     * @param key the set key
     * @return a set of all members
     */
    public Set<String> smembers(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.smembers(key);
        } catch (Exception e) {
            logger.error("Failed to get set members: {}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * Checks if a member exists in a set.
     *
     * @param key the set key
     * @param member the member to check
     * @return true if the member exists
     */
    public boolean sismember(String key, String member) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error("Failed to check set membership: {}", key, e);
            return false;
        }
    }

    /**
     * Gets the number of members in a set.
     *
     * @param key the set key
     * @return the number of members
     */
    public long scard(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("Failed to get set cardinality: {}", key, e);
            return 0;
        }
    }

    // ========== List Operations ==========

    /**
     * Pushes one or more values to the head of a list.
     *
     * @param key the list key
     * @param values the values to push
     * @return the length of the list after the push
     */
    public long lpush(String key, String... values) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.lpush(key, values);
        } catch (Exception e) {
            logger.error("Failed to push to list: {}", key, e);
            return 0;
        }
    }

    /**
     * Pushes one or more values to the tail of a list.
     *
     * @param key the list key
     * @param values the values to push
     * @return the length of the list after the push
     */
    public long rpush(String key, String... values) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.rpush(key, values);
        } catch (Exception e) {
            logger.error("Failed to push to list: {}", key, e);
            return 0;
        }
    }

    /**
     * Pops a value from the head of a list.
     *
     * @param key the list key
     * @return the popped value, or null if the list is empty
     */
    public String lpop(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.lpop(key);
        } catch (Exception e) {
            logger.error("Failed to pop from list: {}", key, e);
            return null;
        }
    }

    /**
     * Pops a value from the tail of a list.
     *
     * @param key the list key
     * @return the popped value, or null if the list is empty
     */
    public String rpop(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.rpop(key);
        } catch (Exception e) {
            logger.error("Failed to pop from list: {}", key, e);
            return null;
        }
    }

    /**
     * Gets a range of elements from a list.
     *
     * @param key the list key
     * @param start the start index
     * @param end the end index (-1 for end of list)
     * @return a list of elements
     */
    public List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("Failed to get list range: {}", key, e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the length of a list.
     *
     * @param key the list key
     * @return the length of the list
     */
    public long llen(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.llen(key);
        } catch (Exception e) {
            logger.error("Failed to get list length: {}", key, e);
            return 0;
        }
    }

    // ========== Pub/Sub Operations ==========

    /**
     * Publishes a message to a channel.
     *
     * @param channel the channel name
     * @param message the message to publish
     * @return the number of subscribers that received the message
     */
    public long publish(String channel, String message) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.publish(channel, message);
        } catch (Exception e) {
            logger.error("Failed to publish message to channel: {}", channel, e);
            return 0;
        }
    }

    /**
     * Publishes a message asynchronously.
     *
     * @param channel the channel name
     * @param message the message to publish
     * @return a CompletableFuture with the number of subscribers
     */
    public CompletableFuture<Long> publishAsync(String channel, String message) {
        return asyncExecutor.supply(() -> publish(channel, message));
    }

    // ========== Counter Operations ==========

    /**
     * Increments a counter by 1.
     *
     * @param key the counter key
     * @return the new value after incrementing
     */
    public long incr(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.incr(key);
        } catch (Exception e) {
            logger.error("Failed to increment counter: {}", key, e);
            return 0;
        }
    }

    /**
     * Increments a counter by a specific value.
     *
     * @param key the counter key
     * @param increment the increment value
     * @return the new value after incrementing
     */
    public long incrBy(String key, long increment) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.incrBy(key, increment);
        } catch (Exception e) {
            logger.error("Failed to increment counter: {}", key, e);
            return 0;
        }
    }

    /**
     * Decrements a counter by 1.
     *
     * @param key the counter key
     * @return the new value after decrementing
     */
    public long decr(String key) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.decr(key);
        } catch (Exception e) {
            logger.error("Failed to decrement counter: {}", key, e);
            return 0;
        }
    }

    /**
     * Decrements a counter by a specific value.
     *
     * @param key the counter key
     * @param decrement the decrement value
     * @return the new value after decrementing
     */
    public long decrBy(String key, long decrement) {
        try (Jedis jedis = redisManager.getResource()) {
            return jedis.decrBy(key, decrement);
        } catch (Exception e) {
            logger.error("Failed to decrement counter: {}", key, e);
            return 0;
        }
    }
}

