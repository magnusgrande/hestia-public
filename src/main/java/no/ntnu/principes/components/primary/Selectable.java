package no.ntnu.principes.components.primary;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * A button component with a selectable state and custom styles.
 *
 * <p>This class extends {@link javafx.scene.control.Button} to support toggling
 * its selection state and dynamically applying associated style classes. Styles are
 * managed based on the button's "selected" state and mouse hover events.</p>
 */
public class Selectable extends javafx.scene.control.Button {
  private final List<String> styleClasses =
      new ArrayList<>(List.of(StyleManager.ButtonStyle.SELECTABLE));
  private boolean selected = false;

  /**
   * Constructs a {@code Selectable} button with the provided text and selection state.
   *
   * <p>Initializes a button that supports toggling selection state, manages dynamic style
   * updates based on selection and hover events, and applies default styling.</p>
   *
   * @param text     The text to be displayed on the button.
   * @param selected {@code true} if the button should be initially selected;
   *                 {@code false} otherwise.
   */
  public Selectable(String text, boolean selected) {
    super(text);
    this.selected = selected;
    this.initialize();
  }

  /**
   * Constructs a {@code Selectable} button with the specified text label.
   *
   * <p>The button supports a selectable state and dynamically applies
   * specific style classes for the "selected" state. It also configures
   * default padding, alignment, and other properties for consistent behavior
   * within a JavaFX layout.</p>
   *
   * @param text The text to display on the button. Cannot be null or empty.
   *             If null,button text will be blank.
   */
  public Selectable(String text) {
    super(text);
    this.initialize();
  }

  /**
   * Initializes the button with default properties, style classes, and event handlers.
   *
   * <p>This method sets up the button's appearance, accessibility role, alignment, padding,
   * maximum width, and text behavior. It also attaches mouse hover event handlers
   * for dynamically adding or removing style classes and establishes grow priority for
   * layout containers. The visual style of the button is updated to reflect its selected state.
   * </p>
   */
  private void initialize() {
    this.updateStyleClasses();
    this.setAccessibleRole(AccessibleRole.BUTTON);
    this.setMnemonicParsing(true);
    this.setPadding(new javafx.geometry.Insets(5, 30, 5, 30));
    this.setMaxWidth(Double.MAX_VALUE);
    this.setTextAlignment(TextAlignment.CENTER);
    this.setAlignment(Pos.CENTER);
    this.setOnMouseEntered(e -> {
      if (!this.selected) {
        this.getStyleClass().add(StyleManager.ButtonStyle.PseudoClass.HOVERED);
      }
    });
    this.setOnMouseExited(e -> {
      this.getStyleClass().remove(StyleManager.ButtonStyle.PseudoClass.HOVERED);
    });
    HBox.setHgrow(this, Priority.ALWAYS);
    this.applyStyleClasses();
  }

  /**
   * Updates the list of style classes applied to the button based on its current selection state.
   *
   * <p>If the button is selected and the "selected" style class is not already present, it is
   * added.
   * If the button is not selected, the "selected" style class is removed.
   * </p>
   *
   * <p>Note: This method only modifies the internal list of style classes (`styleClasses`)
   * but does not directly apply the changes to the button's actual style classes. To reflect the
   * updates visually, {@link #applyStyleClasses()} must be called after this method.</p>
   */
  private void updateStyleClasses() {
    if (this.selected
        && !this.styleClasses.contains(StyleManager.ButtonStyle.PseudoClass.SELECTED)) {
      this.styleClasses.add(StyleManager.ButtonStyle.PseudoClass.SELECTED);
    } else if (!this.selected) {
      this.styleClasses.remove(StyleManager.ButtonStyle.PseudoClass.SELECTED);
    }
  }

  /**
   * Checks whether the button is currently in the selected state.
   *
   * @return {@code true} if the button is in the selected state; {@code false} otherwise.
   */
  public boolean isSelected() {
    return this.selected;
  }

  /**
   * Updates the selection state of the button and applies the corresponding style changes.
   *
   * <p>When the selection state changes, the button's style classes are updated
   * to reflect the "selected" or "unselected" state. If the new state is the same
   * as the current state, no updates are performed.</p>
   *
   * @param selected A boolean value indicating the new selection state.
   *                 {@code true} sets the button to the "selected" state,
   *                 and {@code false} sets it to the "unselected" state.
   */
  public void setSelected(boolean selected) {
    if (this.selected == selected) {
      return;
    }
    this.selected = selected;
    this.updateStyleClasses();
    this.applyStyleClasses();
  }

  /**
   * Updates the visual style classes of the button to reflect its current internal list of style
   * classes.
   *
   * <p>This method synchronizes the button's style by replacing its existing style classes with
   * the contents of the internal {@code styleClasses} list. It ensures the button's appearance
   * matches its dynamic state, such as selection or hover effects.</p>
   */
  private void applyStyleClasses() {
    this.getStyleClass().setAll(styleClasses);
  }
}
