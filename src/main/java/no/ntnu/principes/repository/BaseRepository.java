package no.ntnu.principes.repository;

import java.util.List;
import java.util.Optional;

/**
 * Defines core data access operations for entity persistence.
 * Provides standard CRUD methods that all repositories must implement.
 *
 * @param <T> The entity type this repository manages
 * @param <I> The type of the entity's identifier
 */
public interface BaseRepository<T, I> {
  /**
   * Finds an entity by its identifier.
   *
   * @param id The entity identifier
   * @return An Optional containing the found entity or empty if not found
   */
  Optional<T> findById(I id);

  /**
   * Retrieves all entities of the managed type.
   *
   * @return A list of all entities
   */
  List<T> findAll();

  /**
   * Persists an entity to the database.
   * Creates a new record if the entity has no ID, otherwise updates the existing record.
   *
   * @param entity The entity to save
   * @return The saved entity with any database-generated values (like ID) populated
   */
  T save(T entity);

  /**
   * Removes an entity from the database.
   *
   * @param id The identifier of the entity to delete
   */
  void deleteById(I id);
}