package no.ntnu.principes.domain.task;

import lombok.Getter;

/**
 * Categorizes tasks based on the estimated time required to complete them.
 * Provides predefined time ranges from very short (5-15 minutes) to very long (2+ hours).
 * Each category has a numeric value for computational purposes and a human-readable description.
 */
public enum TimeWeight {
  VERY_SHORT(1, "5m - 15m"),
  SHORT(2, "15m - 30m"),
  MEDIUM(3, "30m - 1h"),
  LONG(4, "1h - 2h"),
  VERY_LONG(5, "2h+");

  @Getter
  private final int value;
  private final String timeWeightAsString;

  /**
   * Creates a time weight with a numeric value and description.
   *
   * @param value              The numeric value representing this time category (1-5)
   * @param timeWeightAsString The human-readable time range description
   */
  TimeWeight(int value, String timeWeightAsString) {
    this.value = value;
    this.timeWeightAsString = timeWeightAsString;
  }

  /**
   * Finds the TimeWeight enum constant corresponding to a numeric value.
   *
   * @param timeWeight The numeric value to convert (1-5)
   * @return The matching TimeWeight, or VERY_LONG if no match is found
   */
  public static TimeWeight fromInt(int timeWeight) {
    for (TimeWeight tw : TimeWeight.values()) {
      if (tw.value == timeWeight) {
        return tw;
      }
    }
    return VERY_LONG;
  }

  /**
   * Returns the human-readable time range description.
   *
   * @return The time range as a string (e.g., "30m - 1h")
   */
  @Override
  public String toString() {
    return timeWeightAsString;
  }
}