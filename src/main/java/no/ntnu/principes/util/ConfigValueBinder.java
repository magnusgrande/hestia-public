package no.ntnu.principes.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.ConfigValue;
import no.ntnu.principes.repository.ConfigValueRepository;

/**
 * Binds JavaFX properties to configuration values stored in a repository.
 * Enables automatic UI updates when configuration values change.
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigValueBinder {
  private final ConfigValueRepository repository;

  /**
   * Binds a string property to a configuration value.
   *
   * @param key          The configuration key to bind to
   * @param defaultValue The fallback value if the configuration is missing
   * @param property     The string property to bind
   */
  public void bindString(String key, String defaultValue, StringProperty property) {
    ObjectProperty<ConfigValue> config = repository.getValueOrDefault(key, property.get());
    property.bind(Bindings.createStringBinding(() -> {
      if (config.get() != null) {
        return config.get().getValue();
      }
      return defaultValue;
    }, config));
  }

  /**
   * Binds a boolean property to a configuration value.
   *
   * @param key          The configuration key to bind to
   * @param defaultValue The fallback value if the configuration is missing
   * @param property     The boolean property to bind
   */
  public void bindBoolean(String key, boolean defaultValue, BooleanProperty property) {
    ObjectProperty<ConfigValue> config =
        repository.getValueOrDefault(key, String.valueOf(defaultValue));
    property.bind(Bindings.createObjectBinding(() -> {
      if (config.get() != null) {
        return Boolean.parseBoolean(config.get().getValue());
      }
      return defaultValue;
    }, config));
  }

  /**
   * Binds an integer property to a configuration value.
   *
   * @param key          The configuration key to bind to
   * @param defaultValue The fallback value if the configuration is missing
   * @param property     The integer property to bind
   */
  public void bindInteger(String key, int defaultValue, IntegerProperty property) {
    ObjectProperty<ConfigValue> config =
        repository.getValueOrDefault(key, String.valueOf(defaultValue));
    property.bind(Bindings.createObjectBinding(() -> {
      if (config.get() != null) {
        return Integer.parseInt(config.get().getValue());
      }
      return defaultValue;
    }, config));
  }
}