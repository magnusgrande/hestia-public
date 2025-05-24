package no.ntnu.principes.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The {@code ConfigValue} class represents a configuration entry with a unique ID,
 * a key identifying the configuration, a value stored as a String, and a timestamp
 * indicating when the configuration was created.
 *
 * <p>Includes utility methods to interpret the configuration value as
 * specific data types.</p>
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class ConfigValue {
  private Long id;
  private String key;
  private String value;
  private LocalDateTime createdAt;

  /**
   * Converts the stored string value to a boolean.
   *
   * @return {@code true} if the string value represents "true",
   * otherwise {@code false}. Returns {@code false} if the value is null or
   * does not match "true".
   */
  public boolean getBooleanValue() {
    return Boolean.parseBoolean(value);
  }

  /**
   * Converts the stored String value to an integer.
   *
   * @return the integer representation of the String value.
   * @throws NumberFormatException if the value cannot be parsed as an integer.
   */
  public int getIntValue() {
    return Integer.parseInt(value);
  }
}
