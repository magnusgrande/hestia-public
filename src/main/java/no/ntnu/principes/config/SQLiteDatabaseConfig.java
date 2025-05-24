package no.ntnu.principes.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * SQLite implementation of the DatabaseConfig interface.
 * Creates and manages a SQLite database connection with write-ahead logging,
 * foreign key enforcement, and busy timeout handling.
 * Uses a thread-safe singleton pattern for the data source.
 */
@Slf4j
public class SQLiteDatabaseConfig implements DatabaseConfig {
  private static final String DB_NAME = "db.sqlite";
  private volatile SQLiteDataSource dataSource;
  // Volatile means it's safe to read/write from multiple threads

  /**
   * Gets the SQLite data source using double-checked locking for thread safety.
   * Initializes the data source if it hasn't been created yet.
   *
   * @return The SQLite data source
   */
  public DataSource getDataSource() {
    if (dataSource == null) {
      synchronized (DatabaseConfig.class) {
        // Synchronized blocks other threads from entering this block concurrently.
        if (dataSource == null) {
          initializeDataSource();
        }
      }
    }
    return dataSource;
  }

  /**
   * Initializes the SQLite data source with optimal configuration settings.
   * Creates the data directory if it doesn't exist, configures SQLite settings,
   * and initializes the database schema.
   *
   * @throws RuntimeException If database initialization fails
   */
  public void initializeDataSource() {
    try {
      // Create data directory if it doesn't exist
      Path dataDir = Paths.get("data");
      if (!dataDir.toFile().exists()) {
        dataDir.toFile().mkdirs();
      }

      // Configure SQLite
      SQLiteConfig config = new SQLiteConfig();
      config.setJournalMode(
          SQLiteConfig.JournalMode.WAL); // Write-Ahead Logging for better concurrency
      config.enforceForeignKeys(true);
      config.setBusyTimeout(5000); // 5 second timeout

      // Initialize datasource
      dataSource = new SQLiteDataSource(config);
      dataSource.setUrl("jdbc:sqlite:" + dataDir.resolve(DB_NAME));

      // Test connection and create schema if needed
      try (Connection conn = dataSource.getConnection()) {
        initializeSchema(conn);
      }

      log.info("SQLite database initialized successfully at {}", dataDir.resolve(DB_NAME));
    } catch (Exception e) {
      log.error("Failed to initialize SQLite database", e);
      throw new RuntimeException("Could not initialize database", e);
    }
  }

  /**
   * Initializes the database schema by executing SQL statements from the schema.sql resource file.
   * Splits the schema file on semicolons and executes each statement separately.
   *
   * @param conn The database connection
   * @throws RuntimeException If schema initialization fails
   */
  public void initializeSchema(Connection conn) {
    // Read schema.sql from resources
    try {
      String schema = new String(
          Objects.requireNonNull(
              DatabaseConfig.class.getResourceAsStream(
                  "/no/ntnu/principes/config/schema.sql"
              )
          ).readAllBytes()
      );

      // Split on semicolon and execute each statement
      try (Statement stmt = conn.createStatement()) {
        for (String sql : schema.split(";")) {
          if (!sql.trim().isEmpty()) {
            stmt.execute(sql);
          }
        }
      }
      log.info("Database schema initialized successfully");
    } catch (Exception e) {
      log.error("Failed to initialize database schema", e);
      throw new RuntimeException("Could not initialize database schema", e);
    }
  }

  /**
   * Creates a backup of the database to the specified path.
   * Uses SQLite's built-in backup command.
   *
   * @param backupPath The path where the backup will be saved
   * @throws RuntimeException If the backup operation fails
   */
  public void backup(Path backupPath) {
    try (Connection conn = getDataSource().getConnection()) {
      Statement stmt = conn.createStatement();
      stmt.execute("backup to " + backupPath.toString());
    } catch (SQLException e) {
      log.error("Failed to backup database", e);
      throw new RuntimeException("Could not backup database", e);
    }
  }

  /**
   * Closes the data source by setting it to null.
   * SQLite doesn't require explicit closure, but this method is provided for consistency.
   */
  public void closeDataSource() {
    // SQLite doesn't need explicit closure, but we'll keep the method for consistency
    dataSource = null;
  }
}