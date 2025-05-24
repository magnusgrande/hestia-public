package no.ntnu.principes.event;

import lombok.Data;

/**
 * A generic base class for defining events within the application.
 *
 * <p>The class is parameterized to carry a payload of any type, encapsulating
 * the event-specific data. It serves as a foundation for creating custom
 * event types with additional fields and methods as needed.
 * </p>
 * Subclasses can extend this class to represent specific event types,
 * encapsulating event-related context and data while leveraging
 * encapsulation and immutability.
 *
 * @param <T> The type of payload data associated with the event.
 */
@Data
public abstract class PrincipesEvent<T> {
  private final T data;
}