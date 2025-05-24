package no.ntnu.principes.event.navigation;

import java.util.UUID;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event that clears the navigation stack.
 */
public class NavigationClearStackEvent extends PrincipesEvent<UUID> {
  /**
   * Creates a navigation event that clears the entire navigation stack.
   * This event is used when resetting a navigation hierarchy is required.
   *
   * @param payload a {@code UUID} representing the unique identifier associated with this event,
   *                which can be used for tracking or correlation purposes.
   */
  public NavigationClearStackEvent(UUID payload) {
    super(payload);
  }
}
