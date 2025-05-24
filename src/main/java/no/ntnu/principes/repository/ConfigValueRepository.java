package no.ntnu.principes.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.ConfigValue;

/**
 * Repository for managing application configuration values.
 * Provides caching for config values and JavaFX property bindings to enable reactive UI updates.
 * Supports string, integer, and boolean configuration values with default fallbacks.
 */
@Slf4j
public class ConfigValueRepository extends AbstractRepository<ConfigValue, Long> {
  private static final HashMap<String, ObjectProperty<ConfigValue>> configCache = new HashMap<>();
  private static final String SELECT_BY_ID = """
      SELECT id, key, value, created_at
      FROM config_value WHERE id = ?
      """;

  private static final String SELECT_ALL = """
      SELECT id, key, value, created_at
      FROM config_value
      """;

  private static final String INSERT = """
      INSERT INTO config_value (key, value)
      VALUES (?, ?)
      """;

  private static final String UPDATE = """
      UPDATE config_value
      SET value = ?
      WHERE key = ?
      """;

  private static final String DELETE = "DELETE FROM config_value WHERE id = ?";

  private static final String SELECT_BY_KEY = """
      SELECT id, key, value, created_at 
      FROM config_value WHERE key = ?
      """;

  /**
   * Creates a new ConfigValueRepository with the specified data source.
   *
   * @param dataSource The JDBC data source for database connections
   */
  public ConfigValueRepository(DataSource dataSource) {
    super(dataSource, "config_value");
  }

  /**
   * Finds a configuration value by its ID.
   *
   * @param id The configuration value ID
   * @return An Optional containing the found config value or empty if not found
   */
  @Override
  public Optional<ConfigValue> findById(Long id) {
    return this.queryOne(SELECT_BY_ID, id);
  }

  /**
   * Retrieves all configuration values.
   *
   * @return A list of all configuration values
   */
  @Override
  public List<ConfigValue> findAll() {
    return this.queryList(SELECT_ALL);
  }

  /**
   * Saves a configuration value, updating if it exists or creating if it doesn't.
   * Uses the config key to determine if an update or insert is needed.
   *
   * @param config The configuration value to save
   * @return The saved configuration value with ID populated
   */
  @Override
  public ConfigValue save(ConfigValue config) {
    // First try to find if the key exists
    Optional<ConfigValue> existing = this.findByKey(config.getKey());

    if (existing.isPresent()) {
      // Update existing value
      this.executeUpdate(UPDATE, config.getValue(), config.getKey());
      config.setId(existing.get().getId());
    } else {
      // Insert new value
      long id = this.executeInsert(INSERT, config.getKey(), config.getValue());
      config.setId(id);
    }
    return config;
  }

  /**
   * Deletes a configuration value by its ID.
   *
   * @param id The ID of the configuration value to delete
   */
  @Override
  public void deleteById(Long id) {
    this.executeUpdate(DELETE, id);
  }

  /**
   * Maps a database result set row to a ConfigValue object.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped ConfigValue object
   * @throws SQLException If a database error occurs during mapping
   */
  @Override
  protected ConfigValue mapRow(ResultSet rs) throws SQLException {
    return ConfigValue.builder()
        .id(rs.getLong("id"))
        .key(rs.getString("key"))
        .value(rs.getString("value"))
        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
        .build();
  }

  /**
   * Finds a configuration value by its key.
   *
   * @param key The configuration key
   * @return An Optional containing the found config value or empty if not found
   */
  public Optional<ConfigValue> findByKey(String key) {
    return this.queryOne(SELECT_BY_KEY, key);
  }

  /**
   * Gets a configuration value as a JavaFX property, with a default if not found.
   * Uses an in-memory cache to avoid repeated database lookups.
   *
   * @param key          The configuration key
   * @param defaultValue The default value to use if the key is not found
   * @return An ObjectProperty containing the config value or a new one with the default value
   */
  public ObjectProperty<ConfigValue> getValueOrDefault(String key, String defaultValue) {
    if (configCache.containsKey(key)) {
      return configCache.get(key);
    }
    Optional<ConfigValue> config = this.findByKey(key);
    if (config.isPresent()) {
      ObjectProperty<ConfigValue> property;
      if (configCache.containsKey(key)) {
        property = configCache.get(key);
        property.setValue(config.get());
      } else {
        property = new SimpleObjectProperty<>(config.get());
        configCache.put(key, property);
      }
      return property;
    }
    return new SimpleObjectProperty<>(ConfigValue.builder().key(key).value(defaultValue).build());
  }

  /**
   * Gets an integer configuration value as a JavaFX property, with a default if not found.
   *
   * @param key          The configuration key
   * @param defaultValue The default integer value to use if the key is not found
   * @return An ObjectProperty containing the config value
   */
  public ObjectProperty<ConfigValue> getValueOrDefault(String key, int defaultValue) {
    return this.getValueOrDefault(key, String.valueOf(defaultValue));
  }

  /**
   * Gets a boolean configuration value as a JavaFX property, with a default if not found.
   *
   * @param key          The configuration key
   * @param defaultValue The default boolean value to use if the key is not found
   * @return An ObjectProperty containing the config value
   */
  public ObjectProperty<ConfigValue> getValueOrDefault(String key, boolean defaultValue) {
    return this.getValueOrDefault(key, String.valueOf(defaultValue));
  }

  /**
   * Sets a configuration value in the database and updates the cache.
   * Creates a new entry if the key doesn't exist.
   *
   * @param key   The configuration key
   * @param value The string value to set
   */
  public void setConfigValue(String key, String value) {
    Optional<ConfigValue> existing = this.findByKey(key);
    ConfigValue config;
    if (existing.isPresent()) {
      config = existing.get();
      config.setValue(value);
      this.save(config);
    } else {
      config = ConfigValue.builder()
          .key(key)
          .value(value)
          .build();
      this.save(config);
    }
    if (configCache.containsKey(key)) {
      configCache.get(key).setValue(config);
    } else {
      configCache.put(key, new SimpleObjectProperty<>(config));
    }
  }

  /**
   * Sets an integer configuration value.
   *
   * @param key   The configuration key
   * @param value The integer value to set
   */
  public void setConfigValue(String key, int value) {
    this.setConfigValue(key, String.valueOf(value));
  }

  /**
   * Sets a boolean configuration value.
   *
   * @param key   The configuration key
   * @param value The boolean value to set
   */
  public void setConfigValue(String key, boolean value) {
    this.setConfigValue(key, String.valueOf(value));
  }
}