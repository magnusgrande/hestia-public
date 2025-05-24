package no.ntnu.principes.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation of the repository pattern with JDBC.
 * Provides reusable database operations for concrete repositories.
 * Handles connection management, statement preparation, and result set processing.
 *
 * @param <T> The entity type this repository manages
 * @param <I> The type of the entity's identifier
 */
@Slf4j
public abstract class AbstractRepository<T, I> implements BaseRepository<T, I> {
  protected final DataSource dataSource;
  protected final String tableName;

  /**
   * Creates a new repository for the specified table.
   *
   * @param dataSource The JDBC data source for database connections
   * @param tableName  The name of the database table
   */
  protected AbstractRepository(DataSource dataSource, String tableName) {
    this.dataSource = dataSource;
    this.tableName = tableName;
  }

  /**
   * Executes a query expecting a single result.
   *
   * @param sql    The SQL query to execute
   * @param params The parameters to bind to the query
   * @return An Optional containing the mapped entity or empty if no results
   * @throws RuntimeException If a database error occurs
   */
  protected Optional<T> queryOne(String sql, Object... params) {
    try (PreparedStatement stmt = this.prepareStatement(sql, params)) {
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapRow(rs));
      }
      return Optional.empty();
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Executes a query expecting multiple results.
   *
   * @param sql    The SQL query to execute
   * @param params The parameters to bind to the query
   * @return A list of mapped entities
   * @throws RuntimeException If a database error occurs
   */
  protected List<T> queryList(String sql, Object... params) {
    try (PreparedStatement stmt = this.prepareStatement(sql, params)) {
      ResultSet rs = stmt.executeQuery();
      log.debug("Query executed: {}", rs.getStatement());
      List<T> results = new ArrayList<>();
      while (rs.next()) {
        results.add(mapRow(rs));
      }
      return results;
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Executes an INSERT statement and returns the generated key.
   *
   * @param sql    The SQL insert statement to execute
   * @param params The parameters to bind to the statement
   * @return The generated primary key
   * @throws RuntimeException If a database error occurs or key retrieval fails
   */
  protected long executeInsert(String sql, Object... params) {
    try (PreparedStatement stmt = this.prepareStatement(sql, params, true)) {
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
      throw new SQLException("Failed to get generated key");
    } catch (SQLException e) {
      log.error("Error executing insert: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Executes an UPDATE or DELETE statement.
   *
   * @param sql    The SQL statement to execute
   * @param params The parameters to bind to the statement
   * @return The number of affected rows
   * @throws RuntimeException If a database error occurs
   */
  protected int executeUpdate(String sql, Object... params) {
    try (PreparedStatement stmt = this.prepareStatement(sql, params)) {
      return stmt.executeUpdate();
    } catch (SQLException e) {
      log.error("Error executing update: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Creates a prepared statement with parameters and optional key retrieval.
   *
   * @param sql        The SQL statement
   * @param params     The parameters to bind
   * @param returnKeys Whether to return generated keys
   * @return The prepared statement
   * @throws SQLException If a database error occurs
   */
  private PreparedStatement prepareStatement(String sql, Object[] params, boolean returnKeys)
      throws SQLException {
    Connection connection = this.dataSource.getConnection();
    PreparedStatement stmt = returnKeys
        ? connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        : connection.prepareStatement(sql);

    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        stmt.setObject(i + 1, params[i]);
      }
    }
    return stmt;
  }

  /**
   * Creates a prepared statement with parameters.
   *
   * @param sql    The SQL statement
   * @param params The parameters to bind
   * @return The prepared statement
   * @throws SQLException If a database error occurs
   */
  private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
    return this.prepareStatement(sql, params, false);
  }

  /**
   * Maps a database result set row to an entity object.
   * Implemented by concrete repositories based on their specific entity structure.
   *
   * @param rs The result set positioned at the row to map
   * @return The mapped entity object
   * @throws SQLException If a database error occurs during mapping
   */
  protected abstract T mapRow(ResultSet rs) throws SQLException;

  /**
   * Executes a query for a single row and returns the raw result set.
   *
   * @param sql    The SQL query
   * @param params The parameters to bind to the query
   * @return An Optional containing the raw result set or empty if no result
   * @throws RuntimeException If a database error occurs
   */
  protected Optional<Object[]> queryOneRaw(String sql, Object... params) {
    try (Connection connection = this.dataSource.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        stmt.setObject(i + 1, params[i]);
      }
      ResultSet rs = stmt.executeQuery();
      return getObjects(rs);
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  @NotNull
  private Optional<Object[]> getObjects(ResultSet rs) throws SQLException {
    if (rs.next()) {
      int columnCount = rs.getMetaData().getColumnCount();
      Object[] row = new Object[columnCount];
      for (int i = 0; i < columnCount; i++) {
        row[i] = rs.getObject(i + 1); // ResultSet column indices are 1-based
      }
      return Optional.of(row);
    }
    return Optional.empty();
  }

  /**
   * Executes a query for a single row and returns the raw result set.
   *
   * @param sql The SQL query
   * @return An Optional containing the raw result set or empty if no result
   * @throws RuntimeException If a database error occurs
   */
  protected Optional<Object[]> queryOneRaw(String sql) {
    try (Connection connection = this.dataSource.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      return getObjects(rs);
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Executes a query and returns a list of raw result sets.
   *
   * @param sql The SQL query
   * @return A list of result sets
   * @throws RuntimeException If a database error occurs
   */
  protected List<Object[]> queryListRaw(String sql) {
    try (Connection connection = this.dataSource.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

      List<Object[]> results = new ArrayList<>();
      while (rs.next()) {
        int columnCount = rs.getMetaData().getColumnCount();
        Object[] row = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
          row[i] = rs.getObject(i + 1); // ResultSet column indices are 1-based
        }
        results.add(row);
      }
      return results;
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }

  /**
   * Executes a query and returns a list of raw result sets.
   *
   * @param sql    The SQL query
   * @param params The parameters to bind to the query
   * @return A list of result sets
   * @throws RuntimeException If a database error occurs
   */
  protected List<Object[]> queryListRaw(String sql, Object... params) {
    try (Connection connection = this.dataSource.getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        stmt.setObject(i + 1, params[i]);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        List<Object[]> results = new ArrayList<>();
        while (rs.next()) {
          int columnCount = rs.getMetaData().getColumnCount();
          Object[] row = new Object[columnCount];
          for (int i = 0; i < columnCount; i++) {
            row[i] = rs.getObject(i + 1); // ResultSet column indices are 1-based
          }
          results.add(row);
        }
        return results;
      }
    } catch (SQLException e) {
      log.error("Error executing query: {}", sql, e);
      throw new RuntimeException("Database error", e);
    }
  }
}