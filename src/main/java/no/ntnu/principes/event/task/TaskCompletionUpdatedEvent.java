package no.ntnu.principes.event.task;

import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event that is triggered when a task assignment is updated.
 * This event is used to notify listeners about changes in the task assignment status.
 * The event contains the updated task assignment information.
 */
public class TaskCompletionUpdatedEvent extends PrincipesEvent<TaskAssignment> {

  /**
   * Constructor for creating a new TaskCompletionUpdatedEvent.
   *
   * @param assignment The task assignment that has been updated.
   */
  public TaskCompletionUpdatedEvent(TaskAssignment assignment) {
    super(assignment);
  }

  /**
   * Creates a new TaskCompletionUpdatedEvent with the specified task assignment.
   *
   * @param assignment The task assignment that has been updated.
   * @return A new instance of TaskCompletionUpdatedEvent.
   */
  public static TaskCompletionUpdatedEvent of(TaskAssignment assignment) {
    return new TaskCompletionUpdatedEvent(assignment);
  }
}
