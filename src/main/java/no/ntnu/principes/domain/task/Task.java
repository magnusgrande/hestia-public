package no.ntnu.principes.domain.task;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Stores information about a task, including its ID, name, description, work weight, time weight,
 * creator ID, creation timestamp, recurrence status, and recurrence interval in days.
 * This class uses the {@code id} field to determine equality and hash code.
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class Task {
  private Long id;
  private String name;
  private String description;
  private WorkWeight workWeight;
  private TimeWeight timeWeight;
  private Long createdById;
  private LocalDateTime createdAt;
  private boolean isRecurring;
  private int recurrenceIntervalDays;

}
