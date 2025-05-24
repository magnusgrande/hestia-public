package no.ntnu.principes.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * A Singleton-based event bus for managing event publishing and subscribing in the Principes
 * system.
 *
 * <p>This class provides mechanisms for listeners to subscribe to specific event types
 * and get notified when these events are published.
 * </p>
 * The <code>PrincipesEventBus</code> ensures thread safety through the use of the Holder pattern
 * for lazy initialization, ensuring only a single instance is created.
 */
@Slf4j
public class PrincipesEventBus {

  /**
   * Map from event class to a map of listener IDs to listeners.
   * This structure prevents duplicate registrations of the same method reference.
   */
  private final Map<
      Class<? extends PrincipesEvent<?>>,
      Map<String, PrincipesEventListener<? extends PrincipesEvent<?>>>
      > listenerMap = new HashMap<>();

  private PrincipesEventBus() {
  } // no public instantiation

  /**
   * Get the singleton instance of {@code PrincipesEventBus} through the Holder pattern.
   *
   * @return The singleton instance of the {@code PrincipesEventBus}.
   */
  public static PrincipesEventBus getInstance() {
    return Holder.instance;
  }

  /**
   * Generates a unique identifier for a listener that can identify the same
   * method reference or lambda, even when subscribed from different locations.
   *
   * @param listener The listener to generate an ID for
   * @return A unique ID string that identifies the core method
   */
  private String getListenerId(PrincipesEventListener<?> listener) {
    // For normal class instances, use the hashcode
    if (!isLambdaOrMethodReference(listener)) {
      return listener.getClass() + "@" + listener.hashCode();
    }

    // For lambdas and method references
    String className = listener.getClass().getName();
    int lambdaIndex = className.indexOf("$$Lambda$");
    if (lambdaIndex > 0) {
      className = className.substring(0, lambdaIndex);
    }
    return className;
  }

  /**
   * Determines whether a listener coulb be lambda or method reference.
   *
   * @param listener The listener to check
   * @return true if the listener is probably a lambda or method reference
   */
  private boolean isLambdaOrMethodReference(PrincipesEventListener<?> listener) {
    String className = listener.getClass().getName();
    // Lambda classes contain "$$Lambda$" in their name
    if (className.contains("$$Lambda$")) {
      return true;
    }

    // Check for synthetic class
    if (listener.getClass().isSynthetic()) {
      return true;
    }

    // Check if it's a direct implementation of the interface with no other methods
    // onEvent + maybe writeReplace
    return listener.getClass().getInterfaces().length == 1
        && listener.getClass().getInterfaces()[0].equals(PrincipesEventListener.class)
        && listener.getClass().getDeclaredMethods().length <= 2;
  }

  /**
   * Subscribes a listener to a specific event type. When an event of the specified
   * type is published, the subscribed listener will be notified.
   *
   * @param <D>        The type of event being subscribed to, extending {@code PrincipesEvent}.
   * @param eventClass The class type of the event to subscribe to.
   * @param listener   The listener to register for handling the specified event type.
   */
  public <D extends PrincipesEvent<?>> PrincipesEventBus subscribe(
      Class<D> eventClass,
      PrincipesEventListener<D> listener) {

    String listenerId = getListenerId(listener);
    log.debug("Subscribing listener {} (ID: {}) to event class {}",
        listener.getClass().getSimpleName(),
        listenerId,
        eventClass.getSimpleName());

    // Get or create listener map for this event class
    Map<String, PrincipesEventListener<? extends PrincipesEvent<?>>> listeners =
        listenerMap.computeIfAbsent(eventClass, k -> new HashMap<>());

    // Check if this exact listener ID is already registered
    if (listeners.containsKey(listenerId)) {
      log.debug("Listener {} already registered for event class {}, skipping",
          listenerId, eventClass.getSimpleName());
    } else {
      listeners.put(listenerId, listener);
      log.debug("Listener {} successfully registered for event class {}",
          listenerId, eventClass.getSimpleName());
    }

    return this;
  }

  /**
   * Subscribes a listener to one or more event classes in the Principes event system.
   * The listener will be notified whenever an event of the subscribed classes is published.
   *
   * @param <D>          The type of event extending {@code PrincipesEvent} that the listener
   *                     handles.
   * @param listener     The event listener to be subscribed. Must handle events of type {@code D}.
   * @param eventClasses A list of event classes that the listener should listen to.
   *                     Each class represents an event type the listener will react to.
   */
  public <D extends PrincipesEvent<?>> PrincipesEventBus subscribe(
      PrincipesEventListener<D> listener,
      List<Class<D>> eventClasses) {
    log.debug("Subscribing listener {} to event classes {}",
        listener.getClass().getSimpleName(),
        eventClasses);
    for (Class<D> eventClass : eventClasses) {
      this.subscribe(eventClass, listener);
    }
    return this;
  }

  /**
   * Unsubscribes a listener from a specific event type. Removes the given listener from
   * receiving notifications for the specified event class if it is currently registered.
   *
   * @param <D>        The type of the event, extending {@code PrincipesEvent}.
   * @param eventClass The class of the event to unsubscribe from. Identifies the event type.
   * @param listener   The listener to remove from the specified event class.
   * @return true if the listener was successfully removed; false if the listener was not
   * registered for the specified event class.
   */
  public <D extends PrincipesEvent<?>> boolean unsubscribe(Class<D> eventClass,
                                                           PrincipesEventListener<D> listener) {
    String listenerId = getListenerId(listener);
    log.debug("Unsubscribing listener {} (ID: {}) from event class {}",
        listener.getClass().getSimpleName(),
        listenerId,
        eventClass.getSimpleName());

    if (listenerMap.containsKey(eventClass)) {
      Map<String, PrincipesEventListener<? extends PrincipesEvent<?>>> listeners =
          listenerMap.get(eventClass);

      return listeners.remove(listenerId) != null;
    }

    return false;
  }

  /**
   * Unsubscribes a listener from a list of event classes. Removes the specified listener
   * from receiving any events of the given types.
   *
   * @param <D>          The type of the event, extending {@code PrincipesEvent}.
   * @param listener     The event listener to unsubscribe. Must be capable of handling the given
   *                     event types.
   * @param eventClasses A list of event classes from which the listener should be unsubscribed.
   *                     Each class represents an event type.
   */
  public <D extends PrincipesEvent<?>> void unsubscribe(PrincipesEventListener<D> listener,
                                                        List<Class<D>> eventClasses) {
    log.debug("Unsubscribing listener {} from event classes {}",
        listener.getClass().getSimpleName(),
        eventClasses);
    int removed = 0;
    for (Class<D> eventClass : eventClasses) {
      if (this.unsubscribe(eventClass, listener)) {
        removed++;
      }
    }
    log.debug("Removed {} listeners", removed);
  }

  /**
   * Publishes the specified event to all registered listeners of its type.
   * Notifies each listener subscribed to the event's class by invoking their
   * respective {@code onEvent} method.
   *
   * @param <D>   The type of the event being published, extending {@code PrincipesEvent}.
   * @param event The event instance to be published. Contains the data and type of the event.
   */
  @SuppressWarnings("unchecked")
  public <D extends PrincipesEvent<?>> void publish(D event) {
    Class<?> eventClass = event.getClass();
    log.debug("Publishing event of type {}", eventClass.getSimpleName());

    if (listenerMap.containsKey(eventClass)) {
      Map<String, PrincipesEventListener<? extends PrincipesEvent<?>>> listeners =
          listenerMap.get(eventClass);

      log.debug("Found {} listeners for event type {}",
          listeners.size(), eventClass.getSimpleName());

      for (Map.Entry<String, PrincipesEventListener<? extends PrincipesEvent<?>>> entry :
          listeners.entrySet()) {

        String listenerId = entry.getKey();
        PrincipesEventListener<? extends PrincipesEvent<?>> listener = entry.getValue();

        log.debug("[{}]: Publishing event to listener {}, eventData: {}",
            eventClass.getSimpleName(), listenerId, event);
        ((PrincipesEventListener<D>) listener).onEvent(event);
        log.debug("[{}]: Event published to listener {}, eventData: {}",
            eventClass.getSimpleName(), listenerId, event);
      }
    } else {
      log.debug("No listeners found for event type {}", eventClass.getSimpleName());
    }
  }

  /**
   * This inner static class is here to implement a lazy-loading Singleton
   * using the Holder pattern.
   * (<a href="https://stackoverflow.com/questions/15019306/regarding-static-holder-singleton-pattern">StackOverflow link</a>)
   * <p/>
   * The idea is that the Singleton instance will only be created when it's actually needed.
   * By using the JVMs class initialization process, it ensures thread safety
   * without the use of synchronized blocks.
   */
  private static class Holder {
    private static final PrincipesEventBus instance = new PrincipesEventBus();
  }
}