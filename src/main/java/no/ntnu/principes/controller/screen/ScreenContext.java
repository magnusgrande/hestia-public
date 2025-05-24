package no.ntnu.principes.controller.screen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages a context for a specific screen by storing and retrieving parameters
 * tied to a unique screen identifier.
 */
@Slf4j
public class ScreenContext {
  private final Map<String, Object> parameters;
  @Getter
  private final String screenId;

  /**
   * Initializes a new screen context identified by a unique screen ID and prepares
   * storage for parameters associated with the screen.
   *
   * @param screenId The unique identifier for the screen. Must not be null or empty.
   *                 This ID is used to differentiate between contexts for different screens.
   */
  public ScreenContext(String screenId) {
    this.screenId = screenId;
    this.parameters = new HashMap<>();
  }

  /**
   * Adds or updates a parameter in the context associated with the screen.
   *
   * @param key   A non-null string that identifies the parameter. Must not be empty.
   * @param value The value to associate with the key. Can be any object, including null, which will
   *              overwrite any existing value for the key.
   */
  public void setParameter(String key, Object value) {
    parameters.put(key, value);
  }

  /**
   * Retrieves the value associated with the specified key from the parameter map,
   * cast to the expected type.
   *
   * @param <T> The expected type of the value.
   * @param key The key to look up in the parameter map. Must not be null.
   *            If the key does not exist, this method returns null.
   * @return The value associated with the key, cast to the expected type,
   * or null if the key is not present in the map.
   * @throws ClassCastException If the value associated with the key cannot be cast
   *                            to the specified type.
   */
  @SuppressWarnings("unchecked")
  public <T> T getParameter(String key) {
    return (T) parameters.get(key);
  }

  /**
   * Removes all parameters stored in the current screen context.
   */
  public void clearParameters() {
    parameters.clear();
  }

  /**
   * Returns an unmodifiable view of the parameters map.
   *
   * @return a {@code Map<String, Object>} containing the current parameters;
   * modifications to the returned map are not allowed.
   */
  public Map<String, Object> getParameters() {
    // Create a copy of the parameters map to prevent modification
    return Map.copyOf(parameters);
  }

  /**
   * Retrieves a filtered map of parameters based on the specified keys.
   * Only includes entries where the key exists in the `parameters` map.
   *
   * @param keys The keys to filter and retrieve from the parameters map. Non-existent keys are
   *             skipped.
   * @return A map containing key-value pairs for all valid keys found in the `parameters` map.
   * If no keys match, returns an empty map.
   */
  public Map<String, Object> getParameters(String... keys) {
    if (keys == null || keys.length == 0) {
      return Map.copyOf(parameters);
    }
    Map<String, Object> filteredParameters = new HashMap<>();
    for (String key : keys) {
      if (parameters.containsKey(key)) {
        filteredParameters.put(key, parameters.get(key));
      }
    }
    if (filteredParameters.size() != keys.length) {
      log.warn("Called getParameters with keys that do not exist in the context. Missing keys: {}",
          Arrays.stream(keys).filter(k -> !parameters.containsKey(k)).toList());
    }
    return filteredParameters;
  }

  @Override
  public String toString() {
    return "ScreenContext{" + "parameters=" + parameters + ", screenId='" + screenId + '\'' + '}';
  }
}
