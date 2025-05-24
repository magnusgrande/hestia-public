package no.ntnu.principes.components.primary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.AccessibleRole;

/**
 * A customizable button with dynamic CSS style class management.
 *
 * <p>This class extends the JavaFX {@link javafx.scene.control.Button} and implements
 * {@link StyleableComponent} to allow dynamic updates and application of CSS style classes.
 * It simplifies the process of setting predefined or dynamically modified styling for buttons
 * via its built-in style management mechanism.
 * </p>
 */
public abstract class StyledButton extends javafx.scene.control.Button
    implements StyleableComponent {
  protected final Set<String> styleClasses = new HashSet<>();

  /**
   * Constructs a styled button with the specified text and an initial set of CSS style classes.
   *
   * <p>The button's styling can be dynamically modified and updated by managing the underlying
   * CSS style classes defined in {@code initialClasses}.
   * </p>
   *
   * @param text           the text to display on the button; cannot be {@code null}.
   * @param initialClasses a list of initial CSS style class names to apply to the button.
   *                       If {@code null} or empty, no additional styling classes are applied by
   *                       default.
   */
  protected StyledButton(String text, List<String> initialClasses) {
    super(text);
    this.styleClasses.addAll(initialClasses);
    this.initialize();
  }

  /**
   * Constructs a styled button initialized with the specified display text.
   *
   * <p>This constructor initializes the button with the provided text and applies any default
   * styling
   * through the {@code initialize()} method, which sets up default style classes and
   * accessibility properties.
   * </p>
   *
   * @param text the text to display on the button. Must not be {@code null}.
   *             If {@code null}, a {@link NullPointerException} may be thrown during runtime.
   */
  protected StyledButton(String text) {
    super(text);
    this.initialize();
  }

  /**
   * Initializes the button with default style classes and accessibility settings.
   */
  protected void initialize() {
    this.updateStyleClasses();
    this.setAccessibleRole(AccessibleRole.BUTTON);
    this.applyStyleClasses();
  }

  /**
   * Applies the stored set of CSS style classes to the component.
   */
  @Override
  public void applyStyleClasses() {
    this.getStyleClass().setAll(this.styleClasses);
  }
}