package no.ntnu.principes.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;

/**
 * A Data Transfer Object (DTO) for representing task assignments, including details such as
 * the task, member assigned, assignment times, status, and completion information.
 */
@Data
@Builder
public class TaskAssignmentDto {
  private Long id;
  private TaskDto task;
  private ProfileDto member;
  private LocalDateTime assignedAt;
  private LocalDateTime dueAt;
  private LocalDateTime completedAt;
  private TaskStatus status;

  /**
   * Retrieves the task associated with this task assignment.
   * If the task is not set (i.e., {@code null}), returns a default {@code TaskDTO} instance
   * with placeholder values.
   *
   * <p>The placeholder task can be identified by the {@link TaskDto#getId()} {@code == -1L}</p>
   *
   * @return the associated {@code TaskDTO} if present, or a default {@code TaskDTO} with
   * placeholder values if the task is {@code null}.
   */
  public TaskDto getTask() {
    return this.task != null ? this.task : TaskDto.builder()
        .name("Unknown")
        .description("Unknown")
        .workWeight(WorkWeight.EASY)
        .timeWeight(TimeWeight.SHORT)
        .id(-1L)
        .build();
  }

  /**
   * Checks if the task associated with this TaskAssignmentDTO is completed.
   *
   * @return {@code true} if the task's status is {@code TaskStatus.DONE}, otherwise {@code false}.
   */
  public boolean isCompleted() {
    return this.status == TaskStatus.DONE;
  }

  /**
   * Checks if the task assignment is currently pending.
   *
   * @return {@code true} if the task assignment's status is {@code TaskStatus.TODO};
   * {@code false} otherwise.
   */
  public boolean isPending() {
    return this.status == TaskStatus.TODO;
  }

  /**
   * Checks if the task assignment has a status indicating it has been cancelled.
   *
   * @return {@code true} if the task assignment status is {@code TaskStatus.CANCELLED};
   * {@code false} otherwise.
   */
  public boolean isCancelled() {
    return this.status == TaskStatus.CANCELLED;
  }
}
