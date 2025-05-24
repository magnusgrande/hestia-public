package no.ntnu.principes.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.config.DatabaseConfig;
import no.ntnu.principes.config.SQLiteDatabaseConfig;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.repository.PointsRepository;
import no.ntnu.principes.repository.TaskAssignmentRepository;
import no.ntnu.principes.repository.TaskRepository;

/**
 * Central manager for database repositories.
 * Provides a singleton registry for accessing repository instances.
 * Lazily initializes repositories when requested and allows configuring the data source.
 */
@Slf4j
public class DatabaseManager {
  private final Map<Class<?>, Object> repositories = new HashMap<>();
  private DataSource dataSource;
  private static final DatabaseConfig defaultConfig = new SQLiteDatabaseConfig();

  /**
   * Private constructor for singleton pattern.
   * Initializes repositories with the default data source.
   */
  private DatabaseManager() {
    this.initializeRepositories();
  }

  /**
   * Sets a custom data source for all repositories.
   * Reinitializes all repositories to use the new data source.
   *
   * @param dataSource The data source to use for database connections
   * @throws IllegalArgumentException if the data source is null
   */
  public void useDataSource(DataSource dataSource) {
    if (dataSource == null) {
      throw new IllegalArgumentException("DataSource cannot be null");
    }
    this.dataSource = dataSource;
    this.initializeRepositories();
  }

  public void useConfig(DatabaseConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("DatabaseConfig cannot be null");
    }
    this.dataSource = config.getDataSource();
    this.initializeRepositories();
  }

  /**
   * Gets the singleton instance of the DatabaseManager.
   * Thread-safe implementation using the holder pattern.
   *
   * @return The DatabaseManager instance
   */
  public static DatabaseManager getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Initializes all repository instances with the current data source.
   * Creates new instances of each repository type and stores them in the registry.
   */
  private void initializeRepositories() {
    this.repositories.put(MemberRepository.class, new MemberRepository(this.dataSource));
    this.repositories.put(TaskRepository.class, new TaskRepository(this.dataSource));
    this.repositories.put(TaskAssignmentRepository.class,
        new TaskAssignmentRepository(this.dataSource));
    this.repositories.put(PointsRepository.class, new PointsRepository(this.dataSource));
    this.repositories.put(ConfigValueRepository.class,
        new ConfigValueRepository(this.dataSource));
    log.info("Repositories initialized successfully");
  }

  /**
   * Gets a repository instance by its class type.
   * Lazily initializes the default data source if none has been set.
   *
   * @param <T>             The repository type
   * @param repositoryClass The class of the repository to get
   * @return The repository instance
   * @throws IllegalArgumentException if the requested repository type is not registered
   */
  @SuppressWarnings("unchecked")
  public <T> T getRepository(Class<T> repositoryClass) {
    if (this.dataSource == null) {
      this.useConfig(defaultConfig);
    }
    return (T) Optional.ofNullable(this.repositories.get(repositoryClass))
        .orElseThrow(() -> new IllegalArgumentException(
            "Repository not found: " + repositoryClass.getName()));
  }

  /**
   * Private holder class for lazy, thread-safe singleton initialization.
   */
  private static class Holder {
    private static final DatabaseManager INSTANCE = new DatabaseManager();
  }
}