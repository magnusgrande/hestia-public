package no.ntnu.principes.domain.onboarding;

import java.util.ArrayList;
import java.util.List;
import no.ntnu.principes.domain.household.HouseholdType;

/**
 * Stores and manages household information collected during the onboarding process.
 * Maintains household type, name, and a list of household members that can be modified
 * throughout the onboarding flow.
 */
public class OnboardingDetails {
  private HouseholdType householdType;
  private String householdName;
  private final List<String> members;

  /**
   * Creates an OnboardingDetails with specified initial values.
   *
   * @param householdType The type of household being created
   * @param householdName The name of the household
   * @param members       List of household member names
   */
  public OnboardingDetails(HouseholdType householdType, String householdName,
                           List<String> members) {
    this.householdType = householdType;
    this.householdName = householdName;
    this.members = members;
  }

  /**
   * Creates an empty OnboardingDetails with default values.
   * Initializes with null household type and name, and three empty member slots.
   */
  public OnboardingDetails() {
    this(null, null, new ArrayList<>(List.of("", "", "")));
  }

  /**
   * Gets the household type.
   *
   * @return The selected household type
   */
  public HouseholdType householdType() {
    return householdType;
  }

  /**
   * Gets the household name.
   *
   * @return The household name
   */
  public String householdName() {
    return householdName;
  }

  /**
   * Gets the list of household members.
   *
   * @return List of member names
   */
  public List<String> members() {
    return members;
  }

  /**
   * Updates the household type.
   *
   * @param householdType The new household type
   */
  public void setHouseholdType(HouseholdType householdType) {
    this.householdType = householdType;
  }

  /**
   * Updates the household name.
   *
   * @param householdName The new household name
   */
  public void setHouseholdName(String householdName) {
    this.householdName = householdName;
  }

  /**
   * Adds a new empty member slot to the household.
   */
  public void newMember() {
    this.members.add("");
  }

  /**
   * Removes a member from the household at the specified index.
   *
   * @param index The position of the member to remove
   */
  public void removeMember(int index) {
    this.members.remove(index);
  }

  /**
   * Updates a member's name at the specified index.
   *
   * @param index The position of the member to update
   * @param name  The new name for the member
   */
  public void setMember(int index, String name) {
    this.members.set(index, name);
  }

  /**
   * Removes all blank member entries and returns the cleaned instance.
   * Used to prepare the data for final submission.
   *
   * @return The current instance with blank members removed
   */
  public OnboardingDetails clean() {
    // Remove blank members
    this.members.removeIf(String::isBlank);
    return this;
  }
}