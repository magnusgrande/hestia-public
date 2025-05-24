package no.ntnu.principes.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;

/**
 * Repository for managing household tasks.
 * Handles task creation, updates, and queries with support for recurring tasks,
 * difficulty weighting, and time estimates.
 */
@Slf4j
public class TaskRepository extends AbstractRepository<Task, Long> {
  private static final String SELECT_BY_ID = """
      SELECT id, name, description, work_weight, time_weight, created_by_id,
             created_at, is_recurring, recurrence_interval_days
      FROM task WHERE id = ?
      """;

  private static final String SELECT_ALL = """
      SELECT id, name, description, work_weight, time_weight, created_by_id,
             created_at, is_recurring, recurrence_interval_days
      FROM task
      """;

  private static final String INSERT = """
      INSERT INTO task (name, description, work_weight, time_weight, created_by_id, is_recurring,
      recurrence_interval_days)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """;

  private static final String UPDATE = """
      UPDATE task
      SET name = ?, description = ?, work_weight = ?, time_weight = ?,
          is_recurring = ?, recurrence_interval_days = ?
      WHERE id = ?
      """;

  private static final String DELETE = "DELETE FROM task WHERE id = ?";

  /**
   * Creates a new TaskRepository with the specified data source.
   *
   * @param dataSource The JDBC data source for database connections
   */
  public TaskRepository(DataSource dataSource) {
    super(dataSource, "task");
  }

  /**
   * Finds a task by its ID.
   *
   * @param id The task ID
   * @return An Optional containing the found task or empty if not found
   */
  @Override
  public Optional<Task> findById(Long id) {
    return this.queryOne(SELECT_BY_ID, id);
  }

  /**
   * Retrieves all tasks.
   *
   * @return A list of all tasks
   */
  @Override
  public List<Task> findAll() {
    return this.queryList(SELECT_ALL);
  }

  /**
   * Saves a task, creating a new record or updating an existing one.
   * Converts enum values (WorkWeight, TimeWeight) to their numeric representations.
   *
   * @param task The task to save
   * @return The saved task with ID populated
   */
  @Override
  public Task save(Task task) {
    if (task.getId() == null) {
      long id = this.executeInsert(INSERT,
          task.getName(),
          task.getDescription(),
          task.getWorkWeight().getValue(),
          task.getTimeWeight().getValue(),
          task.getCreatedById(),
          task.isRecurring(),
          task.getRecurrenceIntervalDays()
      );
      task.setId(id);
    } else {
      this.executeUpdate(UPDATE,
          task.getName(),
          task.getDescription(),
          task.getWorkWeight().getValue(),
          task.getTimeWeight().getValue(),
          task.isRecurring(),
          task.getRecurrenceIntervalDays(),
          task.getId()
      );
    }
    return task;
  }

  /**
   * Deletes a task by its ID.
   *
   * @param id The ID of the task to delete
   */
  @Override
  public void deleteById(Long id) {
    this.executeUpdate(DELETE, id);
  }

  /**
   * Parses a LocalDateTime from a database string in various formats.
   * Attempts multiple format patterns to handle different datetime representations.
   *
   * @param rs     The result set containing the datetime field
   * @param column The column name of the datetime field
   * @return The parsed LocalDateTime, or null if parsing fails or the value is null
   * @throws SQLException If a database error occurs accessing the column
   */
  private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
    // Handle as string of format: 2025-01-08T23:59:59.000 or 2025-01-29 15:20:11
    // or 2025-01-08T23:59:59
    String dateTimeString = rs.getString(column);
    if (dateTimeString == null) {
      return null;
    }

    List<DateTimeFormatter> formatters =
        List.of(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    for (DateTimeFormatter formatter : formatters) {
      try {
        return LocalDateTime.parse(dateTimeString, formatter);
      } catch (Exception ignored) {
      }
    }
    return null;
  }

  /**
   * Maps a database result set row to a Task object.
   * Converts numeric weight values to their enum representations.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped Task object
   * @throws SQLException If a database error occurs during mapping
   */
  @Override
  protected Task mapRow(ResultSet rs) throws SQLException {
    return Task.builder()
        .id(rs.getLong("id"))
        .name(rs.getString("name"))
        .description(rs.getString("description"))
        .workWeight(WorkWeight.fromInt(rs.getInt("work_weight")))
        .timeWeight(TimeWeight.fromInt(rs.getInt("time_weight")))
        .createdById(rs.getLong("created_by_id"))
        .createdAt(this.getLocalDateTime(rs, "created_at"))
        .isRecurring(rs.getBoolean("is_recurring"))
        .recurrenceIntervalDays(rs.getInt("recurrence_interval_days"))
        .build();
  }

  /**
   * Finds all tasks assigned to a specific member.
   *
   * @param memberId The ID of the member to find tasks for
   * @return A list of tasks assigned to the member
   */
  public List<Task> findTasksByMember(Long memberId) {
    String sql = """
        SELECT t.id, t.name, t.description, t.work_weight, t.time_weight,
               t.created_by_id, t.created_at, t.is_recurring,
               t.recurrence_interval_days
        FROM task t
        JOIN task_assignment ta ON t.id = ta.task_id
        WHERE ta.member_id = ?
        """;
    return this.queryList(sql, memberId);
  }

  /**
   * Finds all recurring tasks.
   *
   * @return A list of tasks with recurring flag set to true
   */
  public List<Task> findRecurringTasks() {
    String sql = """
        SELECT id, name, description, work_weight, time_weight, created_by_id,
               created_at, is_recurring, recurrence_interval_days
        FROM task
        WHERE is_recurring = true
        """;
    return this.queryList(sql);
  }

  /**
   * Finds tasks by multiple IDs in a single query.
   *
   * @param list A list of task IDs to find
   * @return A list of tasks matching the provided IDs
   */
  public List<Task> findByIds(List<Long> list) {
    String sql = SELECT_ALL + """
        WHERE id IN (""" + String.join(", ", list.stream().map(id -> "?").toList()) + ")";
    return this.queryList(sql, list.toArray());
  }

  public List<Task> findUnassignedTasks() {
    String sql = """
        SELECT t.id, t.name, t.description, t.work_weight, t.time_weight, t.created_by_id,\s
               t.created_at, t.is_recurring, t.recurrence_interval_days\s
        FROM task t
        WHERE\s
        -- Case 1: No assignments at all for this task
        NOT EXISTS (
            SELECT 1 FROM task_assignment ta WHERE ta.task_id = t.id
        )
        OR\s
        -- Case 2: Has at least one assignment with NULL member_id
        EXISTS (
            SELECT 1 FROM task_assignment ta\s
            WHERE ta.task_id = t.id AND ta.member_id IS NULL AND ta.status = 'TODO'
        )
        OR\s
        -- Case 3: Recurring tasks that need a new assignment
        (t.is_recurring = TRUE AND NOT EXISTS (
            SELECT 1 FROM task_assignment ta\s
            WHERE ta.task_id = t.id AND ta.status = 'TODO'
        ))
        """;
    return this.queryList(sql);
  }

  public int count() {
    String sql = "SELECT COUNT(*) FROM task";
    return this.queryOneRaw(sql)
        .map(rs -> (int) rs[0])
        .orElse(0);
  }
}