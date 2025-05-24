package no.ntnu.principes.controller.screen;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages the creation, retrieval, and lifecycle of screen contexts associated with unique
 * controllers.
 * Provides an interface to manage contexts for screens by controller identifiers.
 */
public class ScreenContextManager {
  private final ConcurrentMap<UUID, ScreenContextHolder> holders;

  /**
   * Initializes the {@code ScreenContextManager} as a singleton and prepares a concurrent
   * map for managing {@code ScreenContextHolder} instances associated with unique controller IDs.
   * This constructor is private to enforce singleton usage through {@link #getInstance()}.
   */
  private ScreenContextManager() {
    this.holders = new ConcurrentHashMap<>();
  }

  /**
   * Retrieves the {@link ScreenContextHolder} associated with the specified controller ID.
   * If no holder exists for the given ID, a new one is created and returned.
   *
   * @param controllerId The UUID representing the unique identifier for a screen controller.
   *                     Must not be null.
   * @return The {@link ScreenContextHolder} associated with the given controller ID.
   * A new instance is created if none exists.
   */
  public static ScreenContextHolder getHolder(UUID controllerId) {
    return getInstance().getOrCreateHolder(controllerId);
  }

  /**
   * Retrieves the singleton instance of {@code ScreenContextManager}, ensuring that only
   * one instance of the manager exists throughout the application lifecycle.
   *
   * @return The single {@code ScreenContextManager} instance, responsible for managing
   * screen contexts associated with unique controllers.
   */
  public static ScreenContextManager getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Retrieves the {@link ScreenContextHolder} associated with the specified controller ID.
   * If no holder exists for the given controller ID, a new {@link ScreenContextHolder} is
   * created, stored, and returned.
   *
   * @param controllerId The unique {@link UUID} for the screen controller. Cannot be null.
   *                     Used as a key to identify and store the corresponding holder.
   * @return The {@link ScreenContextHolder} associated with the given controller ID.
   * A new instance is created and stored if no holder exists for the ID.
   * @throws NullPointerException If {@code controllerId} is null.
   */
  public ScreenContextHolder getOrCreateHolder(UUID controllerId) {
    return holders.computeIfAbsent(controllerId, k -> new ScreenContextHolder());
  }

  /**
   * Removes the screen context associated with the given controller ID from the internal storage.
   * This operation effectively frees any resources held by the corresponding
   * {@code ScreenContextHolder}.
   * If no context exists for the specified controller ID, the method does nothing.
   *
   * @param controllerId The unique {@code UUID} identifier for the screen controller to clear.
   *                     Must not be null.
   * @throws NullPointerException If {@code controllerId} is null.
   */
  public void clearContext(UUID controllerId) {
    holders.remove(controllerId);
  }

  /**
   * Clears all stored screen context holders managed by this instance.
   *
   * <p>This method removes all entries in the internal map used to store
   * {@code ScreenContextHolder} instances.
   * </p>
   */
  public void clearAllContexts() {
    holders.clear();
  }

  /**
   * Provides a static, thread-safe reference to the singleton instance of
   * {@link ScreenContextManager}. The nested class ensures lazy initialization
   * and avoids synchronization overhead by utilizing the JVM's class-loading mechanism.
   * Also called the {@code Singleton Holder pattern}.
   */
  private static class Holder {
    private static final ScreenContextManager INSTANCE = new ScreenContextManager();
  }


  /**
   * Manages a collection of {@link ScreenContext} objects, each identified by a unique screen ID.
   *
   * <p>This class provides thread-safe operations for creating, retrieving, and managing
   * screen-specific contexts within the application.
   * </p>
   */
  public static class ScreenContextHolder {
    private final ConcurrentMap<String, ScreenContext> contexts;

    /**
     * Creates a new {@code ScreenContextHolder} for managing multiple {@link ScreenContext}
     * objects.
     *
     * <p>Initializes an internal thread-safe {@link ConcurrentHashMap} to store screen contexts,
     * with each context mapped to a unique screen ID.
     * </p>
     */
    public ScreenContextHolder() {
      this.contexts = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves an existing {@link ScreenContext} for the given screen ID or creates a new one
     * if it does not already exist.
     *
     * <p>This method ensures that each screen ID is associated with a unique {@code ScreenContext}.
     * If the context for the provided screen ID is not already stored, it will initialize and map
     * a new instance of {@code ScreenContext} to the given ID.</p>
     *
     * @param screenId The unique identifier for the screen. Must not be null or empty.
     *                 Used to retrieve or initialize the corresponding {@code ScreenContext}.
     * @return The {@code ScreenContext} object associated with the given screen ID.
     * If the context does not already exist, a new one is created and returned.
     */
    public ScreenContext getOrCreateContext(String screenId) {
      return contexts.computeIfAbsent(screenId, ScreenContext::new);
    }

    /**
     * Removes the {@link ScreenContext} associated with the provided screen ID from the internal
     * storage.
     *
     * <p>After this operation, the screen ID will no longer be mapped to a {@code ScreenContext}.
     * If no context exists for the specified screen ID, the method will have no effect.</p>
     *
     * @param screenId The unique identifier for the screen. Must not be null or empty.
     *                 Used to locate and remove the associated {@code ScreenContext}.
     * @throws NullPointerException If {@code screenId} is null.
     */
    public void clearContext(String screenId) {
      contexts.remove(screenId);
    }

    /**
     * Removes all screen contexts stored in this {@code ScreenContextHolder}.
     *
     * <p>This method clears the internal storage of all {@link ScreenContext} objects, effectively
     * resetting the state of the {@code ScreenContextHolder}. Any further access to previously
     * stored contexts will result in their recreation using {@link #getOrCreateContext(String)}.
     */
    public void clearAllContexts() {
      contexts.clear();
    }
  }
}
