package no.ntnu.principes.event.task;

import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event that is triggered when a task is created.
 */
public class TaskCreatedEvent extends PrincipesEvent<Task> {

  /**
   * Constructor for creating a new TaskCreatedEvent.
   *
   * @param task The task that has been created.
   */
  public TaskCreatedEvent(Task task) {
    super(task);
  }

  /**
   * Creates a new TaskCreatedEvent with the specified task.
   *
   * @param task The task that has been created.
   * @return A new instance of TaskCreatedEvent.
   */
  public static TaskCreatedEvent of(Task task) {
    return new TaskCreatedEvent(task);
  }
}
