package no.ntnu.principes.event.navigation;

import java.util.Map;
import java.util.Optional;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents a navigation event used for managing route transitions within an application.
 * It encapsulates a payload describing the type of navigation action, the target route,
 * and any additional parameters required for the navigation.
 *
 * <p>This class allows the creation of events to either push a new route, replace the current
 * route, or navigate back (pop).</p>
 */
public class NavigateEvent extends PrincipesEvent<NavigateEvent.NavigationPayload> {
  /**
   * Constructs a {@code NavigateEvent} with the specified navigation payload.
   * The payload contains details about the navigation type, target route,
   * and optional parameters for the navigation operation.
   *
   * @param payload the {@link NavigateEvent.NavigationPayload} containing information
   *                about the navigation action, including the route, action type
   *                (e.g., PUSH, REPLACE, POP), and any additional contextual parameters.
   */
  public NavigateEvent(NavigateEvent.NavigationPayload payload) {
    super(payload);
  }

  /**
   * Creates a navigation event for navigating to the specified route by adding it
   * to the navigation stack.
   *
   * @param route the target path or identifier of the route to be navigated to
   * @return a {@code NavigateEvent} representing a PUSH navigation event with the specified route
   */
  public static NavigateEvent push(String route) {
    return new NavigateEvent(new NavigationPayload(route, NavigationType.PUSH, Map.of()));
  }

  /**
   * Creates a navigation event to replace the current route with the specified route.
   * This operation replaces the top of the navigation stack without adding a new entry.
   *
   * @param route the target path or identifier of the route to navigate to
   * @return a {@code NavigateEvent} representing a REPLACE  event with the specified route
   */
  public static NavigateEvent replace(String route) {
    return new NavigateEvent(new NavigationPayload(route, NavigationType.REPLACE, Map.of()));
  }

  /**
   * Creates a navigation event for navigating back by removing the last route
   * from the navigation stack.
   *
   * @return a {@code NavigateEvent} representing a POP navigation event.
   */
  public static NavigateEvent pop() {
    return new NavigateEvent(new NavigationPayload(null, NavigationType.POP, Map.of()));
  }

  /**
   * Creates a navigation event for navigating back to the specified route by removing
   * the route from the navigation stack.
   *
   * @param route the target path or identifier of the route to navigate back to
   * @return a {@code NavigateEvent} representing a POP navigation event with the specified route
   */
  public static NavigateEvent pop(String route) {
    return new NavigateEvent(new NavigationPayload(route, NavigationType.POP, Map.of()));
  }

  /**
   * Defines the types of navigation actions available within the application.
   * Used to classify and determine the navigation behavior during route transitions.
   *
   * <ul>
   *  <li>PUSH: Adds a new route to the navigation stack.</li>
   *  <li>REPLACE: Replaces the current route with a new one without adding to the stack.</li>
   *  <li>POP: Removes the last route from the navigation stack or navigates back.</li>
   * </ul>
   *
   * <p>This enum facilitates consistent handling of navigation operations in the application.</p>
   */
  public enum NavigationType {
    PUSH, REPLACE, POP,
  }

  /**
   * Represents the payload for a navigation event, providing details about the navigation
   * action, target route, and optional parameters for the navigation.
   *
   * <p>This payload is used to encapsulate data needed for navigation transitions, including:
   * <ul>
   *     <li>The target route or identifier to navigate to</li>
   *     <li>The type of navigation action to perform (e.g., PUSH, REPLACE, POP)</li>
   *     <li>Additional parameters as a map of key-value pairs for contextual information</li>
   * </ul>
   * </p>
   *
   * @param route  the target route or identifier for the navigation action
   * @param type   the type of navigation action to perform (e.g., PUSH, REPLACE, POP)
   * @param params a map of optional parameters to accompany the navigation action
   */
  public record NavigationPayload(String route, NavigationType type, Map<String, Object> params) {

    /**
     * Retrieves a parameter from the navigation payload by its key and type if it exists.
     *
     * <p>This method checks if the specified key exists in the parameters map and
     * attempts to cast the value to the specified type. If the key does not exist
     * or the value cannot be cast to the specified type, an empty Optional is returned.</p>
     *
     * @param key  the key of the parameter to retrieve
     * @param type the class type to which the parameter value should be cast
     * @param <T>  the type of the parameter value
     * @return an Optional containing the parameter value if it exists and is of the specified type,
     * or an empty Optional if the key does not exist or the value cannot be cast
     * to the specified type
     */
    public <T> Optional<T> getParam(String key, Class<T> type) {
      if (params == null) {
        return Optional.empty();
      }
      Object value = params.get(key);
      if (value == null) {
        return Optional.empty();
      }
      if (type.isInstance(value)) {
        return Optional.of(type.cast(value));
      } else {
        return Optional.empty();
      }
    }
  }
}
