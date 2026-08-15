package Offload;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

/**
 * Manages background concurrency using a managed Thread Pool Executor.
 *
 * In a JavaFX application, long-running operations (such as database I/O,
 * network SMTP email dispatch, or large file conversions) must never run on the
 * JavaFX Application Thread, as doing so freezes the UI rendering pipeline.
 *
 * This singleton utility offloads tasks onto daemon background worker threads
 * managed by an ExecutorService thread pool, recycling threads efficiently
 * instead of incurring repetitive OS thread creation overhead.
 */
public class SeprateTask {

    private static SeprateTask instance;
    private final ExecutorService executorService;

    // Private constructor initializes a thread pool with a custom Daemon ThreadFactory
    private SeprateTask() {
        ThreadFactory daemonThreadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            // Daemon status ensures background tasks do not block the JVM from terminating upon window close
            thread.setDaemon(true);
            thread.setName("SeprateTask-Worker-" + thread.getId());
            return thread;
        };

        // Cached thread pool reuses idle threads and dynamically creates new ones under load
        this.executorService = Executors.newCachedThreadPool(daemonThreadFactory);

        // Register shutdown hook for clean resource disposal upon JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Retrieves the singleton instance of SeprateTask.
     *
     * @return the shared SeprateTask instance
     */
    public static synchronized SeprateTask getInstance() {
        if (instance == null) {
            instance = new SeprateTask();
        }
        return instance;
    }

    /**
     * Executes a task asynchronously on a background thread from the pool.
     *
     * @param task the Runnable task to execute
     */
    public void offload(Runnable task) {
        if (task == null) {
            return;
        }

        executorService.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[SeprateTask] Exception in background task: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }



    /**
     * Gracefully shuts down the background executor service.
     */
    public void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
