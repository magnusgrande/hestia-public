package no.ntnu.principes.event;

/**
 * Represents a listener interface in the Principes event system.
 *
 * <p>Implementations of this interface are used to define the logic for handling
 * specific types of events published within the system. Subscribing these
 * listeners to the {@code PrincipesEventBus} allows them to react to specific
 * events as they are published.
 * </p>
 *
 * @param <T> The type of event this listener handles, extending {@code PrincipesEvent}.
 */
public interface PrincipesEventListener<T extends PrincipesEvent<?>> {
  /**
   * Handles an event of the specified type.
   * This method is invoked when an event of type {@code T} is published
   * and the listener is subscribed to that event type on the {@code PrincipesEventBus}.
   *
   * @param event The event instance to be processed, containing its data and type.
   */
  void onEvent(T event);
}