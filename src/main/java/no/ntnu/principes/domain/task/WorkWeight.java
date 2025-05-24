package no.ntnu.principes.domain.task;

import lombok.Getter;

/**
 * Categorizes tasks based on their difficulty or effort level.
 * Provides predefined difficulty levels from very easy to very hard.
 * Each category has a numeric value for computational purposes and a human-readable description.
 */
public enum WorkWeight {
  VERY_EASY(1, "Very easy"),
  EASY(2, "Easy"),
  MEDIUM(3, "Medium"),
  HARD(4, "Hard"),
  VERY_HARD(5, "Very hard");

  @Getter
  private final int value;
  private final String workWeightAsString;

  /**
   * Creates a work weight with a numeric value and description.
   *
   * @param value              The numeric value representing this difficulty level (1-5)
   * @param workWeightAsString The human-readable difficulty description
   */
  WorkWeight(int value, String workWeightAsString) {
    this.value = value;
    this.workWeightAsString = workWeightAsString;
  }

  /**
   * Finds the WorkWeight enum constant corresponding to a numeric value.
   *
   * @param workWeight The numeric value to convert (1-5)
   * @return The matching WorkWeight, or EASY if no match is found
   */
  public static WorkWeight fromInt(int workWeight) {
    for (WorkWeight ww : WorkWeight.values()) {
      if (ww.value == workWeight) {
        return ww;
      }
    }
    return EASY;
  }

  /**
   * Returns the human-readable difficulty description.
   *
   * @return The difficulty level as a string (e.g., "Medium")
   */
  @Override
  public String toString() {
    return workWeightAsString;
  }
}