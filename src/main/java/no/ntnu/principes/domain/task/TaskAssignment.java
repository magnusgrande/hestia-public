package no.ntnu.principes.domain.task;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a task assignment with an identifier, task ID, member ID, timestamps for assignment,
 * due date, completion date, and status.
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class TaskAssignment {
  private Long id;
  private Long taskId;
  private Long memberId;
  private LocalDateTime assignedAt;
  private LocalDateTime dueAt;
  private LocalDateTime completedAt;
  private TaskStatus status;

  /**
   * Checks if the task assignment is marked as completed.
   *
   * @return {@code true} if the task status is {@code TaskStatus.DONE}, otherwise {@code false}.
   */
  public boolean isCompleted() {
    return this.status == TaskStatus.DONE;
  }

  /**
   * Checks if the current task assignment is in a "pending" state.
   *
   * @return {@code true} if the task assignment's status is {@code TODO}; {@code false} otherwise.
   */
  public boolean isPending() {
    return this.status == TaskStatus.TODO;
  }

  /**
   * Checks if the task assignment has been cancelled.
   *
   * @return true if the task status is {@link TaskStatus#CANCELLED}; false otherwise.
   */
  public boolean isCancelled() {
    return this.status == TaskStatus.CANCELLED;
  }
}
