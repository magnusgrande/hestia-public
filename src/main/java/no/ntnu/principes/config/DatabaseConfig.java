package no.ntnu.principes.config;

import java.nio.file.Path;
import java.sql.Connection;
import javax.sql.DataSource;

/**
 * Interface for database configuration and operations.
 * Defines the contract for accessing, initializing, and managing database connections.
 * Implementations provide database-specific functionality while sharing a common interface.
 */
public interface DatabaseConfig {
  /**
   * Gets the configured data source for database connections.
   *
   * @return The data source to use for obtaining database connections
   */
  DataSource getDataSource();

  /**
   * Initializes the database schema for a new or existing database.
   *
   * @param conn An open database connection
   */
  void initializeSchema(Connection conn);

  /**
   * Initializes the data source with appropriate configuration settings.
   */
  void initializeDataSource();

  /**
   * Creates a backup of the current database.
   * Implementation-specific method for database backup.
   */
  void backup(Path backupPath);

  /**
   * Closes the data source, releasing any resources if necessary.
   * Should be called when the application is shutting down.
   */
  void closeDataSource();
}