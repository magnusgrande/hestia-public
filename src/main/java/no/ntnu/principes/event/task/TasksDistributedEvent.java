package no.ntnu.principes.event.task;

import java.util.List;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event that is triggered when tasks are distributed to household members.
 */
public class TasksDistributedEvent extends PrincipesEvent<List<Task>> {

  /**
   * Constructor for creating a new TasksDistributedEvent.
   *
   * @param tasks The tasks that has been created.
   */
  public TasksDistributedEvent(List<Task> tasks) {
    super(tasks);
  }

  /**
   * Creates a new TasksDistributedEvent with the specified tasks.
   *
   * @param tasks The tasks that has been distributed.
   * @return A new instance of TasksDistributedEvent.
   */
  public static TasksDistributedEvent of(List<Task> tasks) {
    return new TasksDistributedEvent(tasks);
  }
}
