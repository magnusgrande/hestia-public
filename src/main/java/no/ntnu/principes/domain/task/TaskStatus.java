package no.ntnu.principes.domain.task;

/**
 * Defines the possible statuses of a task or task assignment.
 *
 * <p>These statuses reflect the progression or state of a task:
 * <ul>
 *   <li>{@code TODO} - The task is yet to be started or is actively pending.</li>
 *   <li>{@code CANCELLED} - The task has been cancelled and will not be completed.</li>
 *   <li>{@code DONE} - The task has been completed.</li>
 * </ul>
 * This enumeration can be used to track and query the state of a {@link TaskAssignment}.
 * </p>
 */
public enum TaskStatus {
  TODO,
  CANCELLED,
  DONE
}
