package no.ntnu.principes.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;

/**
 * Represents detailed information about a task, including its metadata, weight, creator,
 * assignments, and recurrence settings.
 */
@Data
@Builder
public class TaskDto {
  private Long id;
  private String name;
  private String description;
  private WorkWeight workWeight;
  private TimeWeight timeWeight;
  private ProfileDto createdBy;
  private LocalDateTime createdAt;
  private List<TaskAssignmentDto> assignments;
  private boolean isRecurring;
  private int recurrenceIntervalDays;
}
