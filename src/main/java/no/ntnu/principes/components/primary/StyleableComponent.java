package no.ntnu.principes.components.primary;

/**
 * Provides hooks for applying and updating CSS style classes on components.
 *
 * <p>This interface allows a component to manage dynamic updates to its style classes,
 * ensuring style consistency. It is implemented by UI components that
 * require specific styles based on their state or context.
 * </p>
 */
public interface StyleableComponent {
  /**
   * Applies the current set of CSS style classes to the UI component.
   */
  void applyStyleClasses();

  /**
   * Dynamically updates the CSS style classes of a component based on its state or type.
   */
  void updateStyleClasses();
}