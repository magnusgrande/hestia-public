package no.ntnu.principes.event.task;

import java.util.List;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event that is triggered when multiple tasks are created.
 */
public class TasksCreatedEvent extends PrincipesEvent<List<Task>> {

  /**
   * Constructor for creating a new TaskCreatedEvent.
   *
   * @param task The tasks that have been created.
   */
  public TasksCreatedEvent(List<Task> task) {
    super(task);
  }

  /**
   * Creates a new TasksCreatedEvent with the specified tasks.
   *
   * @param task The task that has been created.
   * @return A new instance of TaskCreatedEvent.
   */
  public static TasksCreatedEvent of(List<Task> task) {
    return new TasksCreatedEvent(task);
  }
}
