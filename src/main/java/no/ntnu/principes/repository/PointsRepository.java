package no.ntnu.principes.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.Points;

/**
 * Repository for managing household member points.
 * Stores and retrieves points earned by members for completing tasks,
 * with methods for calculating totals, generating leaderboards, and filtering by time periods.
 */
@Slf4j
public class PointsRepository extends AbstractRepository<Points, Long> {
  private static final String SELECT_BY_ID = """
      SELECT id, value, created_at, member_id, task_assignment_id
      FROM points WHERE id = ?
      """;

  private static final String SELECT_ALL = """
      SELECT id, value, created_at, member_id, task_assignment_id
      FROM points
      """;

  private static final String INSERT = """
      INSERT INTO points (value, member_id, task_assignment_id)
      VALUES (?, ?, ?)
      """;

  private static final String DELETE = "DELETE FROM points WHERE id = ?";

  /**
   * Creates a new PointsRepository with the specified data source.
   *
   * @param dataSource The JDBC data source for database connections
   */
  public PointsRepository(DataSource dataSource) {
    super(dataSource, "points");
  }

  /**
   * Finds points by their ID.
   *
   * @param id The points ID
   * @return An Optional containing the found points or empty if not found
   */
  @Override
  public Optional<Points> findById(Long id) {
    return this.queryOne(SELECT_BY_ID, id);
  }

  /**
   * Retrieves all points records.
   *
   * @return A list of all points records
   */
  @Override
  public List<Points> findAll() {
    return this.queryList(SELECT_ALL);
  }

  /**
   * Saves points, creating a new record if it doesn't exist.
   * Update functionality is not implemented as points are typically immutable once created.
   *
   * @param points The points to save
   * @return The saved points with ID populated
   */
  @Override
  public Points save(Points points) {
    if (points.getId() == null) {
      long id = this.executeInsert(INSERT,
          points.getValue(),
          points.getMemberId(),
          points.getTaskAssignmentId()
      );
      points.setId(id);
    }
    return points;
  }

  /**
   * Deletes points by their ID.
   *
   * @param id The ID of the points to delete
   */
  @Override
  public void deleteById(Long id) {
    this.executeUpdate(DELETE, id);
  }

  /**
   * Maps a database result set row to a Points object.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped Points object
   * @throws SQLException If a database error occurs during mapping
   */
  @Override
  protected Points mapRow(ResultSet rs) throws SQLException {
    return Points.builder()
        .id(rs.getLong("id"))
        .value(rs.getInt("value"))
        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
        .memberId(rs.getLong("member_id"))
        .taskAssignmentId(rs.getLong("task_assignment_id"))
        .build();
  }

  /**
   * Calculates the total points earned by a specific member.
   * Returns 0 if the member has no points or if an error occurs.
   *
   * @param memberId The ID of the member to total points for
   * @return The sum of all points earned by the member
   */
  public int getTotalPointsForMember(Long memberId) {
    String sql = "SELECT SUM(value) FROM points WHERE member_id = ?";
    try {
      return this.queryOneRaw(sql, memberId)
          .map(rs -> (int) rs[0])
          .orElse(0);
    } catch (Exception e) {
      log.error("Error calculating total points", e);
      return 0;
    }
  }

  /**
   * Finds all points earned by a specific member, ordered by most recent first.
   *
   * @param memberId The ID of the member to find points for
   * @return A list of points earned by the member
   */
  public List<Points> findByMemberId(Long memberId) {
    String sql = """
        SELECT id, value, created_at, member_id, task_assignment_id
        FROM points
        WHERE member_id = ?
        ORDER BY created_at DESC
        """;
    return this.queryList(sql, memberId);
  }

  /**
   * Creates a points record if one doesn't exist for the member-task combination,
   * or adds to the existing value if it does exist.
   * Uses the database's ON CONFLICT clause for atomic upsert.
   *
   * @param awardedPoints The points to create or update
   */
  public void createIfNotExist(Points awardedPoints) {
    String sql = """
        INSERT INTO points (value, member_id, task_assignment_id)
        VALUES (?, ?, ?)
        ON CONFLICT (member_id, task_assignment_id)
        DO UPDATE SET value = value + EXCLUDED.value
        """;
    this.executeUpdate(sql,
        awardedPoints.getValue(),
        awardedPoints.getMemberId(),
        awardedPoints.getTaskAssignmentId());
  }

  /**
   * Deletes all points associated with a specific member.
   *
   * @param memberId The ID of the member whose points should be deleted
   * @return The number of points records deleted
   */
  public int deleteByMemberId(Long memberId) {
    String sql = "DELETE FROM points WHERE member_id = ?";
    return this.executeUpdate(sql, memberId);
  }

  /**
   * Deletes all points associated with a specific task's assignments.
   *
   * @param taskId The ID of the task whose points should be deleted
   * @return The number of points records deleted
   */
  public int deleteByTaskId(Long taskId) {
    String sql =
        "DELETE FROM points WHERE task_assignment_id in (SELECT id FROM task_assignment"
            + " WHERE task_id = ?)";
    return this.executeUpdate(sql, taskId);
  }
}