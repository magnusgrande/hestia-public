package no.ntnu.principes.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.profile.Profile;

/**
 * Repository for managing household members/user profiles.
 * Provides methods for basic CRUD operations and specialized queries
 * for finding members by name or task status.
 */
@Slf4j
public class MemberRepository extends AbstractRepository<Profile, Long> {
  private static final String SELECT_BY_ID =
      "SELECT id, name, avatar_hash, created_at FROM member WHERE id = ?";
  private static final String SELECT_ALL =
      "SELECT id, name, avatar_hash, created_at FROM member";
  private static final String INSERT =
      "INSERT INTO member (name, avatar_hash) VALUES (?, ?)";
  private static final String UPDATE =
      "UPDATE member SET name = ?, avatar_hash = ? WHERE id = ?";
  private static final String DELETE =
      "DELETE FROM member WHERE id = ?";

  /**
   * Creates a new MemberRepository with the specified data source.
   *
   * @param dataSource The JDBC data source for database connections
   */
  public MemberRepository(DataSource dataSource) {
    super(dataSource, "member");
  }

  /**
   * Finds a member by their ID.
   *
   * @param id The member ID
   * @return An Optional containing the found member or empty if not found
   */
  @Override
  public Optional<Profile> findById(Long id) {
    return this.queryOne(SELECT_BY_ID, id);
  }

  /**
   * Finds a member by their name.
   *
   * @param name The member name to search for
   * @return An Optional containing the found member or empty if not found
   */
  public Optional<Profile> findByName(String name) {
    String sql = "SELECT id, name, avatar_hash, created_at FROM member WHERE name = ?";
    return this.queryOne(sql, name);
  }

  /**
   * Retrieves all members.
   *
   * @return A list of all members
   */
  @Override
  public List<Profile> findAll() {
    return this.queryList(SELECT_ALL);
  }

  /**
   * Saves a member, updating if it exists or creating if it doesn't.
   *
   * @param member The member to save
   * @return The saved member with ID populated
   */
  @Override
  public Profile save(Profile member) {
    if (member.getId() == null) {
      long id = this.executeInsert(INSERT, member.getName(), member.getAvatarHash());
      member.setId(id);
    } else {
      this.executeUpdate(UPDATE, member.getName(), member.getAvatarHash(), member.getId());
    }
    return member;
  }

  /**
   * Deletes a member by their ID.
   *
   * @param id The ID of the member to delete
   */
  @Override
  public void deleteById(Long id) {
    this.executeUpdate(DELETE, id);
  }

  /**
   * Maps a database result set row to a Profile object.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped Profile object
   * @throws SQLException If a database error occurs during mapping
   */
  @Override
  protected Profile mapRow(ResultSet rs) throws SQLException {
    return Profile.builder()
        .id(rs.getLong("id"))
        .name(rs.getString("name"))
        .avatarHash(rs.getString("avatar_hash"))
        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
        .build();
  }

  /**
   * Finds all members who have pending tasks (status = TODO).
   *
   * @return A list of members with pending tasks
   */
  public List<Profile> findMembersWithPendingTasks() {
    String sql = """
        SELECT DISTINCT m.id, m.name, m.avatar_hash, m.created_at
        FROM member m
        JOIN task_assignment ta ON m.id = ta.member_id
        WHERE ta.status = 'TODO'
        """;
    return this.queryList(sql);
  }

  /**
   * Finds all members who have completed tasks (status = DONE).
   *
   * @return A list of members with completed tasks
   */
  public List<Profile> findMembersWithCompletedTasks() {
    String sql = """
        SELECT DISTINCT m.id, m.name, m.avatar_hash, m.created_at
        FROM member m
        JOIN task_assignment ta ON m.id = ta.member_id
        WHERE ta.status = 'DONE'
        """;
    return this.queryList(sql);
  }

  /**
   * Finds members by multiple IDs in a single query.
   * Optimized for bulk loading of member data.
   *
   * @param list A list of member IDs to find
   * @return A list of members matching the provided IDs
   */
  public List<Profile> findAllById(List<Long> list) {
    String sql = "SELECT id, name, avatar_hash, created_at FROM member WHERE id IN ("
        + String.join(",", Collections.nCopies(list.size(), "?")) + ")";
    return this.queryList(sql, list.toArray());
  }
}