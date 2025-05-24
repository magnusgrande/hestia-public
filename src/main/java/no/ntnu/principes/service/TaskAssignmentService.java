package no.ntnu.principes.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.Points;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TaskCompletionUpdatedEvent;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.mapper.TaskAssigmentMapper;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.repository.PointsRepository;
import no.ntnu.principes.repository.TaskAssignmentRepository;
import no.ntnu.principes.repository.TaskRepository;
import no.ntnu.principes.util.AlertUtil;

/**
 * Manages task assignments and their lifecycle in the household system.
 * Handles task completion, assignment to members, points calculation,
 * and recurring task management.
 */
@Slf4j
public class TaskAssignmentService {
  private final TaskRepository taskRepository;
  private final TaskAssignmentRepository taskAssignmentRepository;
  private final MemberRepository memberRepository;
  private final PointsRepository pointsRepository;

  /**
   * Creates a new TaskAssignmentService with all required repositories.
   * Repositories are obtained from the central DatabaseManager.
   */
  public TaskAssignmentService() {
    DatabaseManager dbManager = DatabaseManager.getInstance();
    this.taskRepository = dbManager.getRepository(TaskRepository.class);
    this.taskAssignmentRepository = dbManager.getRepository(TaskAssignmentRepository.class);
    this.memberRepository = dbManager.getRepository(MemberRepository.class);
    this.pointsRepository = dbManager.getRepository(PointsRepository.class);
  }

  /**
   * Marks a task assignment as completed, awards points, and handles recurring tasks.
   *
   * @param assignmentId The ID of the assignment to complete
   * @return The updated task assignment with completed status
   * @throws IllegalArgumentException if the assignment or task is not found
   */
  public TaskAssignment completeTask(Long assignmentId) {
    log.info("Completing task assignment: {}", assignmentId);

    Optional<TaskAssignment> assignmentOpt =
        this.taskAssignmentRepository.findById(assignmentId);
    if (assignmentOpt.isEmpty()) {
      log.warn("Task assignment not found: {}", assignmentId);
      throw new IllegalArgumentException("Task assignment not found");
    }

    TaskAssignment assignment = assignmentOpt.get();
    if (assignment.isCompleted()) {
      log.warn("Task already completed: {}", assignmentId);
      return assignment;
    }

    // Get the task to calculate points
    Optional<Task> taskOpt = this.taskRepository.findById(assignment.getTaskId());
    if (taskOpt.isEmpty()) {
      log.warn("Task not found for assignment: {}", assignmentId);
      throw new IllegalArgumentException("Task not found");
    }

    Task task = taskOpt.get();

    // Calculate points based on weights
    int points = this.calculatePoints(task);

    // Award points
    Points awardedPoints = Points.builder()
        .memberId(assignment.getMemberId())
        .taskAssignmentId(assignment.getId())
        .value(points)
        .build();
    this.pointsRepository.createIfNotExist(awardedPoints);

    // Update assignment status
    assignment.setStatus(TaskStatus.DONE);
    assignment.setCompletedAt(LocalDateTime.now());
    TaskAssignment updatedAssignment = this.taskAssignmentRepository.save(assignment);

    // If task is recurring, create next occurrence
    if (task.isRecurring()) {
      this.createNextRecurrence(task, assignment.getMemberId());
    }
    PrincipesEventBus.getInstance().publish(TaskCompletionUpdatedEvent.of(assignment));

    return updatedAssignment;
  }

  /**
   * Assigns a task to a specific household member.
   * If the task is already assigned to someone else or unassigned, updates the assignment.
   *
   * @param taskId       The ID of the task to assign
   * @param memberId     The ID of the member to assign the task to
   * @param publishEvent Whether to publish an event after assignment
   * @return The created or updated task assignment
   * @throws IllegalArgumentException if the task or member is not found
   */
  public TaskAssignment assignTask(Long taskId, Long memberId, boolean publishEvent) {
    log.trace("Assigning task {} to member {}", taskId, memberId);

    // Verify task exists
    Task task = this.taskRepository.findById(taskId)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    // Verify member exists
    Optional<Profile> memberOpt = this.memberRepository.findById(memberId);
    if (memberOpt.isEmpty()) {
      throw new IllegalArgumentException("Member not found");
    }

    // Verify the assignment does not already exist
    List<TaskAssignment> assignments =
        this.taskAssignmentRepository.findByTaskIds(List.of(task.getId()));
    if (assignments.isEmpty()) {
      // Create assignment
      TaskAssignment assignment = TaskAssignment.builder()
          .taskId(task.getId())
          .memberId(memberId)
          .assignedAt(LocalDateTime.now())
          .status(TaskStatus.TODO)
          .build();

      var ass = this.taskAssignmentRepository.save(assignment);
      if (publishEvent) {
        PrincipesEventBus.getInstance().publish(
            TasksDistributedEvent.of(List.of(task)));
      }
      AlertUtil.success("Task assigned to " + memberOpt.get().getName());
      return ass;
    }

    // First, check if there's an unassigned assignment to claim
    TaskAssignment unassignedAssignment = assignments.stream()
        .filter(a -> a.getMemberId() == null || a.getMemberId() == 0)
        .findFirst()
        .orElse(null);

    if (unassignedAssignment != null) {
      // Update the existing unassigned assignment with the new member ID
      unassignedAssignment.setMemberId(memberId);
      unassignedAssignment.setAssignedAt(LocalDateTime.now());

      var ass = this.taskAssignmentRepository.save(unassignedAssignment);

      if (publishEvent) {
        PrincipesEventBus.getInstance().publish(
            TasksDistributedEvent.of(List.of(task)));
      }
      AlertUtil.success("Task assigned to " + memberOpt.get().getName());
      return ass;
    }

    // If no unassigned assignment exists, check if the user already has an assignment
    TaskAssignment existingAssignment = assignments.stream()
        .filter(a -> a.getMemberId().equals(memberId))
        .findFirst()
        .orElse(null);

    if (existingAssignment != null) {
      // User already has an assignment for this task
      return existingAssignment;
    }

    // Create a new assignment if no unassigned one exists and user doesn't have one
    TaskAssignment newAssignment = TaskAssignment.builder()
        .taskId(taskId)
        .memberId(memberId)
        .assignedAt(LocalDateTime.now())
        .dueAt(assignments.getFirst().getDueAt())
        .status(TaskStatus.TODO)
        .build();

    var ass = this.taskAssignmentRepository.save(newAssignment);

    if (publishEvent) {
      PrincipesEventBus.getInstance().publish(
          TasksDistributedEvent.of(List.of(task)));
    }
    AlertUtil.success("Task assigned to " + memberOpt.get().getName());
    return ass;
  }

  /**
   * Assigns a task to a specific household member.
   * If the task is already assigned to someone else or unassigned, updates the assignment.
   *
   * @param taskId   The ID of the task to assign
   * @param memberId The ID of the member to assign the task to
   * @return The created or updated task assignment
   * @throws IllegalArgumentException if the task or member is not found
   */
  public TaskAssignment assignTask(Long taskId, Long memberId) {
    return this.assignTask(taskId, memberId, true);
  }

  /**
   * Gets all task assignments for a specific member.
   *
   * @param memberId The ID of the member
   * @return A list of task assignment DTOs
   */
  public List<TaskAssignmentDto> getTasksForMember(Long memberId) {
    return TaskAssigmentMapper.mapAssignments(
        this.taskAssignmentRepository.findByMemberId(memberId));
  }

  /**
   * Gets immediate tasks (due within 2 days) for a specific member.
   *
   * @param memberId The ID of the member
   * @return A list of immediate task assignment DTOs
   */
  public List<TaskAssignmentDto> getImmediateTasksForMember(Long memberId) {
    return TaskAssigmentMapper.mapAssignments(
        this.taskAssignmentRepository.findImmediateTasksForMember(memberId));
  }

  /**
   * Gets all tasks due at a specific date for a specific member.
   *
   * @param memberId The ID of the member
   * @param dueDate  The due date to filter by
   * @return A list of task assignment DTOs
   */
  public List<TaskAssignmentDto> getTasksForMemberDueAt(Long memberId, LocalDate dueDate) {
    return TaskAssigmentMapper.mapAssignments(
        this.taskAssignmentRepository.findByMemberIdAndDueAt(memberId, dueDate));
  }

  /**
   * Marks a task assignment as cancelled.
   *
   * @param assignmentId The ID of the assignment to cancel
   * @return The updated task assignment with cancelled status
   * @throws IllegalArgumentException if the assignment is not found
   */
  public TaskAssignment cancelTask(Long assignmentId) {
    log.info("Cancelling task assignment: {}", assignmentId);

    Optional<TaskAssignment> assignmentOpt =
        this.taskAssignmentRepository.findById(assignmentId);
    if (assignmentOpt.isEmpty()) {
      throw new IllegalArgumentException("Task assignment not found");
    }

    TaskAssignment assignment = assignmentOpt.get();
    assignment.setStatus(TaskStatus.CANCELLED);
    return this.taskAssignmentRepository.save(assignment);
  }

  /**
   * Automatically assigns a task to the member with the lowest current workload.
   * Workload is calculated based on the total weight of a member's pending tasks.
   *
   * @param taskId       The ID of the task to assign
   * @param publishEvent Whether to publish an event after assignment
   * @return The created task assignment
   * @throws IllegalArgumentException if the task is not found
   * @throws IllegalStateException    if no members are available or selection fails
   */
  public TaskAssignment autoAssignTask(Long taskId, boolean publishEvent) {
    log.info("Auto-assigning task: {}", taskId);

    // Get the task to be assigned
    Optional<Task> taskToAssign = this.taskRepository.findById(taskId);
    if (taskToAssign.isEmpty()) {
      throw new IllegalArgumentException("Task not found");
    }

    // Get all members
    List<Profile> members = this.memberRepository.findAll();
    if (members.isEmpty()) {
      throw new IllegalStateException("No members available for task assignment");
    }

    // Calculate total weight for each member's pending tasks
    Profile selectedMember = null;
    int lowestTotalWeight = Integer.MAX_VALUE;

    for (Profile member : members) {
      int totalWeight = this.calculateTotalWeightForUser(member.getId());

      log.trace("Member {} has total weight of {}",
          member.getName(), totalWeight);

      if (totalWeight < lowestTotalWeight) {
        lowestTotalWeight = totalWeight;
        selectedMember = member;
      }
    }

    if (selectedMember == null) {
      throw new IllegalStateException("Could not select a member for task assignment");
    }

    log.trace("Selected member {} with lowest total weight of {} for task {}",
        selectedMember.getName(), lowestTotalWeight, taskToAssign.get().getName());

    // Create the assignment
    return this.assignTask(taskId, selectedMember.getId(), publishEvent);
  }

  /**
   * Automatically assigns a task to the member with the lowest current workload.
   * Workload is calculated based on the total weight of a member's pending tasks.
   *
   * @param taskId The ID of the task to assign
   * @return The created task assignment
   * @throws IllegalArgumentException if the task is not found
   * @throws IllegalStateException    if no members are available or selection fails
   */
  public TaskAssignment autoAssignTask(Long taskId) {
    return this.autoAssignTask(taskId, true);
  }

  /**
   * Calculates points awarded for completing a task based on its weights.
   * The formula is (workWeight + timeWeight) * 10.
   *
   * @param task The completed task
   * @return The number of points to award
   */
  private int calculatePoints(Task task) {
    // Base points formula: (workWeight + timeWeight) * 10
    return (task.getWorkWeight().getValue() + task.getTimeWeight().getValue()) * 10;
  }

  /**
   * Calculates the total weight of a list of tasks.
   * Weight is calculated as the sum of work weight and time weight for each task.
   *
   * @param tasks List of tasks to calculate total weight for
   * @return Total weight of all tasks
   */
  private int calculateTotalWeight(List<Task> tasks) {
    return tasks.stream()
        .mapToInt(task -> task.getWorkWeight().getValue() + task.getTimeWeight().getValue())
        .sum();
  }

  private int calculateTotalWeightFromDto(List<TaskDto> tasks) {
    return tasks.stream()
        .mapToInt(task -> task.getWorkWeight().getValue() + task.getTimeWeight().getValue())
        .sum();
  }

  public int calculateTotalWeightForUser(Long memberId) {
    List<TaskAssignmentDto> tasks = this.getTasksForMember(memberId);
    return this.calculateTotalWeightFromDto(
        tasks.stream().filter(TaskAssignmentDto::isPending).map(TaskAssignmentDto::getTask)
            .toList());
  }

  /**
   * Creates the next recurrence for a recurring task.
   * Sets the due date based on the recurrence interval from the last completed assignment.
   *
   * @param completedTask The recurring task
   * @param memberId      The ID of the member who completed the task
   */
  private void createNextRecurrence(Task completedTask, Long memberId) {
    // Create next occurrence recurrence interval
    TaskAssignment lastAssignment =
        this.taskAssignmentRepository.findLastCompletedAssignment(completedTask.getId())
            .orElse(null);
    if (lastAssignment == null) {
      log.warn("No last assignment found for task: {}", completedTask.getId());
      return;
    }
    LocalDateTime nextDueDate = lastAssignment.getDueAt()
        .plusDays(completedTask.getRecurrenceIntervalDays());

    TaskAssignment nextTaskAssigment = TaskAssignment.builder()
        .taskId(completedTask.getId())
        .dueAt(nextDueDate)
        .status(TaskStatus.TODO)
        .build();

    // Save the unassigned assigment
    this.taskAssignmentRepository.save(nextTaskAssigment);
  }

  /**
   * Gets all task assignments with a specific status.
   *
   * @param status The status to filter by
   * @return A list of task assignment DTOs
   */
  public List<TaskAssignmentDto> getTasksByStatus(TaskStatus status) {
    return TaskAssigmentMapper.mapAssignments(
        this.taskAssignmentRepository.findByStatus(status)
    );
  }

  /**
   * Gets all task assignments.
   *
   * @return A list of all task assignment DTOs
   */
  public List<TaskAssignmentDto> getAllTasks() {
    return TaskAssigmentMapper.mapAssignments(
        this.taskAssignmentRepository.findAll()
    );
  }

  /**
   * Marks a completed task as incomplete.
   * Resets the task status to TODO and clears the completion date.
   *
   * @param id The ID of the assignment to uncomplete
   * @throws IllegalArgumentException if the assignment is not found
   */
  public void uncompleteTask(Long id) {
    log.info("Uncompleting task assignment: {}", id);

    Optional<TaskAssignment> assignmentOpt =
        this.taskAssignmentRepository.findById(id);
    if (assignmentOpt.isEmpty()) {
      log.warn("Task assignment not found: {}", id);
      throw new IllegalArgumentException("Task assignment not found");
    }
    TaskAssignment assignment = assignmentOpt.get();
    if (!assignment.isCompleted()) {
      log.warn("Task is not completed: {}", id);
      return;
    }
    assignment.setStatus(TaskStatus.TODO);
    assignment.setCompletedAt(null);
    this.taskAssignmentRepository.save(assignment);
    log.info("Task assignment uncompleted: {}", id);
    PrincipesEventBus.getInstance().publish(TaskCompletionUpdatedEvent.of(assignment));
  }
}