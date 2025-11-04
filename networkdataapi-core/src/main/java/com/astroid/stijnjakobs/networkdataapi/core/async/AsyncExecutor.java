package com.astroid.stijnjakobs.networkdataapi.core.async;

import com.astroid.stijnjakobs.networkdataapi.core.config.ConfigurationManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Manages asynchronous task execution with configurable thread pools.
 *
 * <p>This executor provides non-blocking operations for database and I/O tasks,
 * preventing the main server thread from being blocked. It supports:</p>
 * <ul>
 *   <li>Asynchronous task execution with callbacks</li>
 *   <li>CompletableFuture-based async operations</li>
 *   <li>Configurable thread pool sizing</li>
 *   <li>Graceful shutdown with task completion</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe. Multiple threads
 * can submit tasks concurrently.</p>
 *
 * <p><strong>Resource Management:</strong> Call {@link #shutdown()} before
 * application termination to ensure all tasks complete gracefully.</p>
 *
 * @author Stijn Jakobs
 * @version 1.0
 * @since 1.0
 */
public class AsyncExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);

    @Getter
    private ThreadPoolExecutor executor;

    private final ConfigurationManager config;

    /**
     * Creates a new async executor.
     *
     * @param config the configuration manager
     */
    public AsyncExecutor(ConfigurationManager config) {
        this.config = config;
        initialize();
    }

    /**
     * Initializes the thread pool executor with configuration settings.
     */
    private void initialize() {
        int corePoolSize = config.getInt("async.core-pool-size", 4);
        int maxPoolSize = config.getInt("async.max-pool-size", 16);
        long keepAliveSeconds = config.getLong("async.keep-alive-seconds", 60);

        executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private int counter = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "NetworkDataAPI-Async-" + counter++);
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        logger.info("AsyncExecutor initialized with core pool size: {}, max pool size: {}",
                corePoolSize, maxPoolSize);
    }

    /**
     * Executes a task asynchronously without a return value.
     *
     * <p>Use this for fire-and-forget operations where you don't need
     * to handle the result.</p>
     *
     * @param task the task to execute
     */
    public void execute(Runnable task) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error executing async task", e);
            }
        });
    }

    /**
     * Executes a task asynchronously and returns a CompletableFuture.
     *
     * <p>Use this when you need to chain operations or handle results
     * asynchronously.</p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * asyncExecutor.supply(() -> {
     *     return database.findDocument("players", uuid);
     * }).thenAccept(document -> {
     *     // Handle result on main thread or async
     *     System.out.println("Found: " + document);
     * }).exceptionally(throwable -> {
     *     // Handle errors
     *     logger.error("Error", throwable);
     *     return null;
     * });
     * }</pre>
     *
     * @param <T> the result type
     * @param supplier the task that produces a result
     * @return a CompletableFuture with the result
     */
    public <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                logger.error("Error in async supplier", e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Executes a task asynchronously and handles the result with a callback.
     *
     * <p>This is a convenience method for simple callback-based async operations.</p>
     *
     * @param <T> the result type
     * @param supplier the task that produces a result
     * @param callback the callback to handle the result
     */
    public <T> void supplyAsync(Supplier<T> supplier, Consumer<T> callback) {
        supply(supplier).thenAccept(callback).exceptionally(throwable -> {
            logger.error("Error in async operation", throwable);
            return null;
        });
    }

    /**
     * Executes a task asynchronously with both success and error callbacks.
     *
     * @param <T> the result type
     * @param supplier the task that produces a result
     * @param onSuccess callback for successful completion
     * @param onError callback for errors
     */
    public <T> void supplyAsync(Supplier<T> supplier, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        supply(supplier)
                .thenAccept(onSuccess)
                .exceptionally(throwable -> {
                    onError.accept(throwable);
                    return null;
                });
    }

    /**
     * Schedules a task to run after a delay.
     *
     * @param task the task to execute
     * @param delay the delay before execution
     * @param unit the time unit of the delay
     * @return a ScheduledFuture representing the pending task
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "NetworkDataAPI-Scheduler");
            thread.setDaemon(true);
            return thread;
        });

        return scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in scheduled task", e);
            } finally {
                scheduler.shutdown();
            }
        }, delay, unit);
    }

    /**
     * Gets the current number of active threads in the pool.
     *
     * @return the active thread count
     */
    public int getActiveThreadCount() {
        return executor.getActiveCount();
    }

    /**
     * Gets the current size of the task queue.
     *
     * @return the queue size
     */
    public int getQueueSize() {
        return executor.getQueue().size();
    }

    /**
     * Gets the total number of completed tasks.
     *
     * @return the completed task count
     */
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    /**
     * Gracefully shuts down the executor.
     *
     * <p>This method initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted. It waits up to
     * 30 seconds for tasks to complete before forcing shutdown.</p>
     */
    public void shutdown() {
        logger.info("Shutting down AsyncExecutor...");
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.error("Executor did not terminate after forced shutdown");
                }
            }
            logger.info("AsyncExecutor shutdown complete");
        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

