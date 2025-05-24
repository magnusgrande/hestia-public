package no.ntnu.principes.event;

import lombok.Getter;

/**
 * Event representing a change in the name of a household.
 *
 * <p>This event is triggered whenever the name of a household is updated
 * within the domain, encapsulating the new household name. Subscribing
 * listeners can handle this event to perform tasks such as updating
 * related data or notifying other components.
 * </p>
 * Extends the {@code PrincipesEvent} class, carrying a {@code String} payload
 * that contains the new household name.
 */
@Getter
public class HouseholdNameChangedEvent extends PrincipesEvent<String> {

  /**
   * Creates a new {@code HouseholdNameChangedEvent} instance with the specified new household name.
   *
   * @param newName The new name of the household to be set in the event.
   */
  public HouseholdNameChangedEvent(String newName) {
    super(newName);
  }

  /**
   * Creates and returns a new {@code HouseholdNameChangedEvent} instance
   * with the specified new household name.
   *
   * <p>Looks cleaner with HousehouldNameChangedEvent.to("newName") than
   * new HouseholdNameChangedEvent("newName").
   * Also, it is more readable and easier to understand.
   * </p>
   *
   * @param newName The new name of the household to be set in the event.
   * @return A {@code HouseholdNameChangedEvent} instance encapsulating the new household name.
   */
  public static HouseholdNameChangedEvent to(String newName) {
    return new HouseholdNameChangedEvent(newName);
  }
}
