package no.ntnu.principes.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;

/**
 * Represents a request to create a new task with specific details such as name, description,
 * weight, due date, and recurrence options.
 */
@Data
@Builder
public class CreateTaskRequest {
  private String name;
  private String description;
  private WorkWeight workWeight;
  private TimeWeight timeWeight;
  private Long createdById;
  private LocalDateTime dueAt;
  private boolean isRecurring;
  private int recurrenceIntervalDays = 0;
}
