package no.ntnu.principes.components.primary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.control.Control;

/**
 * A base class for creating styled JavaFX controls with customizable style classes.
 *
 * <p>This class manages CSS styling for JavaFX {@link Control} components, allowing dynamic updates
 * to style classes based on component state or configuration. It ensures that style classes
 * are applied consistently across instances.
 * </p>
 *
 * @param <T> the type of {@link Control} that this styled control wraps.
 */
public abstract class StyledControl<T extends Control> extends Control
    implements StyleableComponent {
  protected final Set<String> styleClasses = new HashSet<>();
  protected final T control;

  /**
   * Initializes a styled JavaFX control with a set of initial CSS style classes.
   * This constructor configures the control with specified style classes and handles
   * applying these styles during initialization.
   *
   * @param control        the JavaFX {@link Control} to be styled; cannot be null.
   * @param initialClasses a list of initial CSS style class names to apply to the control.
   *                       If the list is empty, no initial styles are applied.
   */
  protected StyledControl(T control, List<String> initialClasses) {
    this.control = control;
    this.styleClasses.addAll(initialClasses);
    this.initialize();
  }

  /**
   * Constructs a styled JavaFX control, initializing it with default behaviors.
   * This constructor wraps a JavaFX {@link Control}, allowing for dynamic style management.
   *
   * @param control the JavaFX {@link Control} to be styled; must not be null.
   *                The provided control will be managed and styled by this class.
   *                If the control is null, this may lead to unexpected behavior or errors.
   */
  protected StyledControl(T control) {
    this.control = control;
    this.initialize();
  }

  /**
   * Initializes the styled control by applying the specified style classes and setting.
   */
  protected void initialize() {
    this.updateStyleClasses();
    this.setAccessibleRole(this.control.getAccessibleRole());
    this.applyStyleClasses();
  }

  /**
   * Applies the current set of style classes to the styled JavaFX control.
   */
  @Override
  public void applyStyleClasses() {
    this.getStyleClass().setAll(this.styleClasses);
  }
}