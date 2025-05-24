package no.ntnu.principes.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TaskCreatedEvent;
import no.ntnu.principes.repository.TaskAssignmentRepository;
import no.ntnu.principes.repository.TaskRepository;
import no.ntnu.principes.util.AlertUtil;

/**
 * Manages the creation and update of task templates and their assignments.
 * Handles the initial setup of tasks with proper assignments to household members,
 * and updates to task properties and member assignments.
 */
@Slf4j
public class TaskTemplateService {
  private final TaskRepository taskRepository;
  private final TaskAssignmentRepository taskAssignmentRepository;

  /**
   * Creates a new TaskTemplateService with the required repositories.
   * Repositories are obtained from the central DatabaseManager.
   */
  public TaskTemplateService() {
    DatabaseManager dbManager = DatabaseManager.getInstance();
    this.taskRepository = dbManager.getRepository(TaskRepository.class);
    this.taskAssignmentRepository = dbManager.getRepository(TaskAssignmentRepository.class);
  }

  /**
   * Creates a new task with optional assignments to specific members.
   * If no members are specified, creates an unassigned task assignment.
   *
   * @param taskRequest       The request containing task details
   * @param assignToMemberIds List of member IDs to assign the task to, or null/empty for unassigned
   * @param pushEvents        Whether to push events after task creation
   * @return The created task
   */
  public Task createTask(CreateTaskRequest taskRequest, List<Long> assignToMemberIds,
                         boolean pushEvents) {
    log.info("Creating new task: {} with assignments to {} members",
        taskRequest.getName(), assignToMemberIds != null ? assignToMemberIds.size() : 0);
    Task newTask = Task.builder()
        .name(taskRequest.getName())
        .description(taskRequest.getDescription())
        .workWeight(taskRequest.getWorkWeight())
        .timeWeight(taskRequest.getTimeWeight())
        .createdById(taskRequest.getCreatedById())
        .isRecurring(taskRequest.isRecurring())
        .recurrenceIntervalDays(taskRequest.getRecurrenceIntervalDays())
        .build();

    // Save
    Task savedTask = this.taskRepository.save(newTask);

    // Create assignments if members
    if (assignToMemberIds != null && !assignToMemberIds.isEmpty()) {
      for (Long memberId : assignToMemberIds) {
        TaskAssignment assignment = TaskAssignment.builder()
            .taskId(savedTask.getId())
            .memberId(memberId)
            .dueAt(taskRequest.getDueAt())
            .status(TaskStatus.TODO)
            .build();
        this.taskAssignmentRepository.save(assignment);
      }
    } else {
      TaskAssignment assignment = TaskAssignment.builder()
          .taskId(savedTask.getId())
          .dueAt(taskRequest.getDueAt())
          .status(TaskStatus.TODO)
          .build();
      this.taskAssignmentRepository.save(assignment);
    }
    if (pushEvents) {
      PrincipesEventBus.getInstance().publish(TaskCreatedEvent.of(savedTask));
    }
    AlertUtil.success("Task created successfully",
        "The task '" + savedTask.getName() + "' has been created.");
    return savedTask;
  }

  /**
   * Creates a new task with optional assignments to specific members.
   * If no members are specified, creates an unassigned task assignment.
   *
   * @param taskRequest       The request containing task details
   * @param assignToMemberIds List of member IDs to assign the task to, or null/empty for unassigned
   * @return The created task
   */
  public Task createTask(CreateTaskRequest taskRequest, List<Long> assignToMemberIds) {
    return createTask(taskRequest, assignToMemberIds, true);
  }

  /**
   * Updates an existing task and its assignments.
   * Handles changes to task properties and member assignments, preserving or updating
   * existing assignments as needed.
   *
   * @param taskId            The ID of the task to update
   * @param taskRequest       The request containing updated task details
   * @param assignToMemberIds List of member IDs to assign the task to, or null/empty for unassigned
   * @return The updated task
   * @throws IllegalArgumentException if the task is not found
   */
  public Task updateTask(Long taskId, CreateTaskRequest taskRequest,
                         List<Long> assignToMemberIds) {
    log.info("Updating task: {} with assignments to {} members",
        taskId, assignToMemberIds != null ? assignToMemberIds.size() : 0);

    // Fetch existing task
    Optional<Task> existingTaskOpt = this.taskRepository.findById(taskId);
    if (existingTaskOpt.isEmpty()) {
      throw new IllegalArgumentException("Task not found: " + taskId);
    }

    Task existingTask = existingTaskOpt.get();

    // Update task properties
    existingTask.setName(taskRequest.getName());
    existingTask.setDescription(taskRequest.getDescription());
    existingTask.setWorkWeight(taskRequest.getWorkWeight());
    existingTask.setTimeWeight(taskRequest.getTimeWeight());
    existingTask.setRecurring(taskRequest.isRecurring());
    existingTask.setRecurrenceIntervalDays(taskRequest.getRecurrenceIntervalDays());

    // Save updated task
    Task savedTask = this.taskRepository.save(existingTask);

    // Get existing assignments
    List<TaskAssignment> existingAssignments =
        this.taskAssignmentRepository.findByTaskIds(List.of(taskId));

    // Remove assignments that no longer exist
    for (TaskAssignment assignment : existingAssignments) {
      if (assignment.getMemberId() != null
          && (assignToMemberIds == null
          || !assignToMemberIds.contains(assignment.getMemberId()))) {
        this.taskAssignmentRepository.deleteById(assignment.getId());
      }
    }

    // Get the due date from the request
    LocalDateTime requestedDueDate = taskRequest.getDueAt();

    // Update or create assignments
    if (assignToMemberIds != null && !assignToMemberIds.isEmpty()) {
      // Get IDs of members who already have assignments
      List<Long> existingMemberIds = existingAssignments.stream()
          .map(TaskAssignment::getMemberId)
          .filter(Objects::nonNull)
          .toList();

      // Create new assignments for members who don't already have one
      for (Long memberId : assignToMemberIds) {
        if (!existingMemberIds.contains(memberId)) {
          TaskAssignment assignment = TaskAssignment.builder()
              .taskId(savedTask.getId())
              .memberId(memberId)
              .dueAt(requestedDueDate)
              .status(TaskStatus.TODO)
              .build();
          this.taskAssignmentRepository.save(assignment);
        } else {
          // Update due date for existing assignments, only if a new due date is provided
          if (requestedDueDate != null) {
            existingAssignments.stream()
                .filter(a -> Objects.equals(a.getMemberId(), memberId))
                .findFirst()
                .ifPresent(a -> {
                  a.setDueAt(requestedDueDate);
                  this.taskAssignmentRepository.save(a);
                });
          }
        }
      }
    } else if (existingAssignments.isEmpty()) {
      // Create an unassigned assignment if no assignments exist
      TaskAssignment assignment = TaskAssignment.builder()
          .taskId(savedTask.getId())
          .dueAt(requestedDueDate)
          .status(TaskStatus.TODO)
          .build();
      this.taskAssignmentRepository.save(assignment);
    } else if (requestedDueDate != null) {
      // Update due date for all existing assignments, only if a new due date is provided
      for (TaskAssignment assignment : existingAssignments) {
        assignment.setDueAt(requestedDueDate);
        this.taskAssignmentRepository.save(assignment);
      }
    }

    PrincipesEventBus.getInstance().publish(TaskCreatedEvent.of(savedTask));
    return savedTask;
  }

  public List<Task> getUnassignedTasks() {
    return this.taskRepository.findUnassignedTasks();
  }

  public boolean hasCreatedTasks() {
    return this.taskRepository.count() > 0;
  }
}