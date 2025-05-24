package no.ntnu.principes.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javafx.application.Platform;

/**
 * Provides throttling and debouncing utilities for controlling execution frequency.
 * Throttling limits function calls to a specified rate, while debouncing delays
 * execution until a quiet period has elapsed.
 */
public class Throttle {
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final ConcurrentHashMap<String, Long> lastExecutionTimes =
      new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Object> pendingResults = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, ScheduledFuture<?>> pendingTasks =
      new ConcurrentHashMap<>();

  private static boolean DEBUG = false;

  /**
   * Enables or disables debug logging.
   *
   * @param debug True to enable logging, false to disable
   */
  public static void setDebug(boolean debug) {
    DEBUG = debug;
  }

  /**
   * Logs a debug message if debug mode is enabled.
   *
   * @param message The message to log
   */
  private static void log(String message) {
    if (DEBUG) {
      System.out.println("[Throttle] " + message);
    }
  }

  /**
   * Throttles a function that returns a value.
   * Executes the function only if the specified delay has passed since last execution.
   *
   * @param <T>      The return type of the function
   * @param key      Unique identifier for this throttled operation
   * @param supplier The function to throttle
   * @param delayMs  Minimum time between executions in milliseconds
   * @return The result of executing the function, or the cached result if throttled
   */
  public static <T> T throttle(String key, Supplier<T> supplier, long delayMs) {
    long currentTime = System.currentTimeMillis();
    Long lastExecution = lastExecutionTimes.get(key);

    if (lastExecution == null || currentTime - lastExecution >= delayMs) {
      T result = supplier.get();
      lastExecutionTimes.put(key, currentTime);
      pendingResults.put(key, result);
      return result;
    }

    return (T) pendingResults.getOrDefault(key, null);
  }

  /**
   * Throttles a function with no return value.
   * Executes the function only if the specified delay has passed since last execution.
   *
   * @param key      Unique identifier for this throttled operation
   * @param runnable The function to throttle
   * @param delayMs  Minimum time between executions in milliseconds
   */
  public static void throttle(String key, Runnable runnable, long delayMs) {
    long currentTime = System.currentTimeMillis();
    Long lastExecution = lastExecutionTimes.get(key);

    if (lastExecution == null || currentTime - lastExecution >= delayMs) {
      runnable.run();
      lastExecutionTimes.put(key, currentTime);
    }
  }

  /**
   * Debounces a function execution.
   * Delays execution until a quiet period has elapsed, canceling pending executions.
   * Function is executed on the JavaFX application thread.
   *
   * @param key      Unique identifier for this debounced operation
   * @param runnable The function to debounce
   * @param delayMs  Delay in milliseconds before execution
   */
  public static void debounce(String key, Runnable runnable, long delayMs) {
    log("Debounce called for key: " + key + " with delay: " + delayMs + "ms");

    // Cancel any pending execution for this key
    ScheduledFuture<?> existingTask = pendingTasks.get(key);
    if (existingTask != null) {
      existingTask.cancel(false);
      log("Cancelled existing task for key: " + key);
    }

    // Schedule new execution
    ScheduledFuture<?> future = scheduler.schedule(() -> {
      try {
        log("Executing debounced function for key: " + key);
        Platform.runLater(() -> {
          // Ensure the runnable is executed on the JavaFX thread
          runnable.run();
        });
        pendingTasks.remove(key);
      } catch (Exception e) {
        log("Error in debounced function: " + e.getMessage());
        e.printStackTrace();
      }
    }, delayMs, TimeUnit.MILLISECONDS);

    pendingTasks.put(key, future);
    log("Scheduled new task for key: " + key);
  }

  /**
   * Clears all tracking data for a specific key.
   * Cancels any pending debounced tasks for this key.
   *
   * @param key The key to clear data for
   */
  public static void clear(String key) {
    lastExecutionTimes.remove(key);
    pendingResults.remove(key);
    ScheduledFuture<?> task = pendingTasks.remove(key);
    if (task != null) {
      task.cancel(false);
    }
  }

  /**
   * Clears all throttling and debouncing data.
   * Cancels all pending debounced tasks.
   */
  public static void clearAll() {
    lastExecutionTimes.clear();
    pendingResults.clear();
    pendingTasks.forEach((key, task) -> task.cancel(false));
    pendingTasks.clear();
  }

  /**
   * Shuts down the throttling/debouncing system.
   * Clears all data and shuts down the executor service.
   */
  public static void shutdown() {
    clearAll();
    scheduler.shutdown();
  }
}