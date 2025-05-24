package no.ntnu.principes.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.domain.task.TaskStatus;

/**
 * Repository for managing the assignment of tasks to household members.
 * Provides methods for tracking task assignments, due dates, completion status,
 * and querying assignments by various criteria.
 */
@Slf4j
public class TaskAssignmentRepository extends AbstractRepository<TaskAssignment, Long> {
  private static final String SELECT_BY_ID = """
      SELECT id, task_id, member_id, assigned_at, completed_at, status, due_at
      FROM task_assignment WHERE id = ?
      """;

  private static final String SELECT_ALL = """
      SELECT id, task_id, member_id, assigned_at, completed_at, status , due_at
      FROM task_assignment
      """;

  private static final String INSERT = """
      INSERT INTO task_assignment (task_id, member_id, due_at, assigned_at, status)
      VALUES (?, ?, ?, ?, ?)
      """;

  private static final String UPDATE = """
      UPDATE task_assignment
      SET status = ?, member_id = ?, assigned_at = ?, due_at = ?, completed_at = ?
      WHERE id = ?
      """;

  private static final String DELETE = "DELETE FROM task_assignment WHERE id = ?";

  /**
   * Creates a new TaskAssignmentRepository with the specified data source.
   *
   * @param dataSource The JDBC data source for database connections
   */
  public TaskAssignmentRepository(DataSource dataSource) {
    super(dataSource, "task_assignment");
  }

  /**
   * Finds a task assignment by its ID.
   *
   * @param id The task assignment ID
   * @return An Optional containing the found task assignment or empty if not found
   */
  @Override
  public Optional<TaskAssignment> findById(Long id) {
    return this.queryOne(SELECT_BY_ID, id);
  }

  /**
   * Retrieves all task assignments.
   *
   * @return A list of all task assignments
   */
  @Override
  public List<TaskAssignment> findAll() {
    return this.queryList(SELECT_ALL);
  }

  /**
   * Formats a LocalDateTime for database storage.
   * Returns null if the input is null.
   *
   * @param datetime The LocalDateTime to format
   * @return A formatted string in "yyyy-MM-dd HH:mm:ss" format, or null
   */
  private String formatDatetime(LocalDateTime datetime) {
    // Format to YYYY-MM-DD HH:MM:SS (no milliseconds)
    if (datetime == null) {
      return null;
    }
    return datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  /**
   * Saves a task assignment, creating a new record or updating an existing one.
   * Handles date formatting for database storage.
   *
   * @param assignment The task assignment to save
   * @return The saved task assignment with ID populated
   */
  @Override
  public TaskAssignment save(TaskAssignment assignment) {
    if (assignment.getId() == null) {
      // (task_id, member_id, due_at, assigned_at, status)
      long id = this.executeInsert(INSERT,
          assignment.getTaskId(),
          assignment.getMemberId(),
          formatDatetime(assignment.getDueAt()),
          formatDatetime(assignment.getAssignedAt()),
          assignment.getStatus().toString()
      );
      assignment.setId(id);
    } else {
      // status = ?, member_id = ?, assigned_at = ?, due_at = ?, completed_at = ?
      this.executeUpdate(UPDATE,
          assignment.getStatus().toString(),
          assignment.getMemberId(),
          formatDatetime(assignment.getAssignedAt()),
          formatDatetime(assignment.getDueAt()),
          formatDatetime(assignment.getCompletedAt()),
          assignment.getId()
      );
    }
    return assignment;
  }

  /**
   * Deletes a task assignment by its ID.
   *
   * @param id The ID of the task assignment to delete
   */
  @Override
  public void deleteById(Long id) {
    this.executeUpdate(DELETE, id);
  }

  /**
   * Maps a database result set row to a TaskAssignment object.
   * Handles nullable date fields.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped TaskAssignment object
   * @throws SQLException If a database error occurs during mapping
   */
  @Override
  protected TaskAssignment mapRow(ResultSet rs) throws SQLException {
    return TaskAssignment.builder()
        .id(rs.getLong("id"))
        .taskId(rs.getLong("task_id"))
        .memberId(rs.getLong("member_id"))
        .assignedAt(rs.getTimestamp("assigned_at") != null
            ? rs.getTimestamp("assigned_at").toLocalDateTime() : null)
        .dueAt(rs.getTimestamp("due_at") != null
            ? rs.getTimestamp("due_at").toLocalDateTime() : null)
        .completedAt(rs.getTimestamp("completed_at") != null
            ? rs.getTimestamp("completed_at").toLocalDateTime() : null)
        .status(TaskStatus.valueOf(rs.getString("status")))
        .build();
  }

  /**
   * Finds all task assignments for a specific member, ordered by due date.
   *
   * @param memberId The ID of the member to find assignments for
   * @return A list of the member's task assignments
   */
  public List<TaskAssignment> findByMemberId(Long memberId) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE member_id = ?
        ORDER BY due_at DESC
        """;
    return this.queryList(sql, memberId);
  }

  /**
   * Finds task assignments for a member with a specific status, ordered by due date.
   *
   * @param memberId The ID of the member to find assignments for
   * @param status   The status to filter by (e.g., TODO, DONE)
   * @return A list of matching task assignments
   */
  public List<TaskAssignment> findByMemberIdAndStatus(Long memberId, TaskStatus status) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE member_id = ? AND status = ?
        ORDER BY due_at DESC
        """;
    return this.queryList(sql, memberId, status.toString());
  }

  /**
   * Finds all task assignments with a specific status.
   *
   * @param status The status to filter by (e.g., TODO, DONE)
   * @return A list of matching task assignments
   */
  public List<TaskAssignment> findByStatus(TaskStatus status) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE status = ?
        """;
    return this.queryList(sql, status.toString());
  }

  /**
   * Finds pending task assignments for a specific task.
   *
   * @param taskId The ID of the task to find pending assignments for
   * @return A list of pending (TODO status) task assignments
   */
  public List<TaskAssignment> findPendingByTaskId(Long taskId) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE task_id = ? AND status = 'TODO'
        """;
    return this.queryList(sql, taskId);
  }

  /**
   * Finds task assignments for multiple tasks.
   *
   * @param taskIds A list of task IDs to find assignments for
   * @return A list of matching task assignments, or all assignments if taskIds is empty
   */
  public List<TaskAssignment> findByTaskIds(List<Long> taskIds) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        """;
    if (taskIds != null && !taskIds.isEmpty()) {
      sql += "\nWHERE task_id IN ("
          + String.join(", ", taskIds.stream().map(id -> "?").toList()) + ")";
      return this.queryList(sql, taskIds.toArray());
    }
    return this.queryList(sql);
  }

  /**
   * Calculates the completion rate for a member's task assignments.
   * The rate is the number of completed tasks divided by the total number of tasks.
   * Returns 0.0 if the member has no tasks or if an error occurs.
   *
   * @param memberId The ID of the member to calculate the completion rate for
   * @return The completion rate as a value between 0.0 and 1.0
   */
  public double getCompletionRateForMember(Long memberId) {
    String sql = """
        SELECT
            CAST(COUNT(CASE WHEN status = 'DONE' THEN 1 END) AS FLOAT) /
            CAST(COUNT(*) AS FLOAT) as completion_rate
        FROM task_assignment
        WHERE member_id = ?
        """;
    try {
      return this.queryOneRaw(sql, memberId)
          .map(rs -> (double) rs[0])
          .orElse(0.0);
    } catch (Exception e) {
      log.error("Error calculating completion rate", e);
      return 0.0;
    }
  }

  /**
   * Finds a task assignment by both task and member IDs.
   *
   * @param taskId   The ID of the task
   * @param memberId The ID of the member
   * @return An Optional containing the matching assignment or empty if not found
   */
  public Optional<TaskAssignment> findByTaskAndMember(Long taskId, Long memberId) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE task_id = ? AND member_id = ?
        """;
    return this.queryOne(sql, taskId, memberId);
  }

  /**
   * Finds unassigned task assignments for a specific task.
   *
   * @param taskId The ID of the task to find unassigned assignments for
   * @return A list of task assignments with null member IDs
   */
  public List<TaskAssignment> findByUnassignedTask(Long taskId) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE task_id = ? AND member_id is null
        ORDER BY due_at ASC
        """;
    return this.queryList(sql, taskId);
  }

  /**
   * Finds the most recently completed assignment for a task.
   *
   * @param id The ID of the task
   * @return An Optional containing the most recently completed assignment or empty if none
   */
  public Optional<TaskAssignment> findLastCompletedAssignment(Long id) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE task_id = ?
        ORDER BY completed_at DESC
        LIMIT 1
        """;
    return this.queryOne(sql, id);
  }

  /**
   * Finds immediate tasks for a member (due within 2 days or with no due date).
   * Results are ordered by status, completion date, and due date.
   *
   * @param memberId The ID of the member to find immediate tasks for
   * @return A list of immediate task assignments
   */
  public List<TaskAssignment> findImmediateTasksForMember(Long memberId) {
    String sql = """
        SELECT DISTINCT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE member_id = ? AND (due_at IS NULL OR due_at <= DATETIME('now', '+2 days'))
        ORDER BY status DESC, due_at ASC
        """;
    return this.queryList(sql, memberId);
  }

  /**
   * Deletes all task assignments for a specific member.
   *
   * @param memberId The ID of the member whose assignments should be deleted
   * @return The number of assignments deleted
   */
  public int deleteByMemberId(Long memberId) {
    String sql = "DELETE FROM task_assignment WHERE member_id = ?";
    return this.executeUpdate(sql, memberId);
  }

  /**
   * Deletes all assignments for a specific task.
   *
   * @param taskId The ID of the task whose assignments should be deleted
   * @return The number of assignments deleted
   */
  public int deleteByTaskId(Long taskId) {
    String sql = "DELETE FROM task_assignment WHERE task_id = ?";
    return this.executeUpdate(sql, taskId);
  }

  /**
   * Finds task assignments for a specific member with a specific due date.
   *
   * @param memberId The ID of the member to find assignments for
   * @param dueDate  The due date to filter by
   * @return A list of matching task assignments
   */
  public List<TaskAssignment> findByMemberIdAndDueAt(Long memberId, LocalDate dueDate) {
    String sql = """
        SELECT id, task_id, member_id, assigned_at, due_at, completed_at, status
        FROM task_assignment
        WHERE member_id = ? AND DATE(due_at) = DATE(?)
        ORDER BY status DESC, due_at ASC
        """;
    return this.queryList(sql, memberId, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }
}