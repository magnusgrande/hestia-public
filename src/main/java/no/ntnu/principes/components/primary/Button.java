package no.ntnu.principes.components.primary;

import atlantafx.base.theme.Styles;
import java.util.List;
import javafx.scene.Node;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * A customized button component that supports multiple styles and optional icons.
 * It adjusts styling dynamically based on the specified {@link ButtonType}.
 */
public class Button extends StyledButton {
  private ButtonType type;

  /**
   * Constructs a button with the specified label text and style type.
   * Automatically configures styling, padding, and predefined style classes
   * based on the provided {@link ButtonType}.
   *
   * @param text The text label to display on the button. Cannot be null.
   * @param type The button's style type, determining its appearance. Supported types are
   *             {@link ButtonType#DEFAULT}, {@link ButtonType#OUTLINED}, {@link ButtonType#FLAT},
   *             and {@link ButtonType#NAVIGATION}.
   */
  public Button(String text, ButtonType type) {
    super(text,
        type != ButtonType.NAVIGATION ? List.of(StyleManager.Typography.BUTTON, "button") :
            List.of(StyleManager.Typography.BUTTON));
    this.type = type;
    this.setPadding(InsetBuilder.symmetric(30, 10).build());
    this.updateStyleClasses();
    this.applyStyleClasses();
  }

  /**
   * Returns the CSS style class associated with the specified {@link ButtonType}.
   * The style class determines the visual appearance of the button.
   *
   * @param type The {@link ButtonType} representing the button's style. Supported types are
   *             {@link ButtonType#OUTLINED}, {@link ButtonType#FLAT},
   *             {@link ButtonType#NAVIGATION}, and others, with a default fallback.
   * @return A {@code String} representing the corresponding CSS style class. Returns the default
   * style class if the provided {@code type} is not explicitly mapped.
   */
  private String getStyleClassForButtonType(ButtonType type) {
    return switch (type) {
      case OUTLINED -> Styles.BUTTON_OUTLINED;
      case FLAT -> Styles.FLAT;
      case NAVIGATION -> StyleManager.ButtonStyle.NAVIGATION;
      default -> Styles.ACCENT;
    };
  }

  /**
   * Updates the button's type, which determines its appearance and associated style classes.
   * If the specified type is the same as the current type, no changes are made.
   *
   * @param type The new {@link ButtonType} that defines the button's visual style.
   *             Supported values include {@code DEFAULT}, {@code OUTLINED}, {@code FLAT},
   *             and {@code NAVIGATION}.
   */
  public void setType(ButtonType type) {
    if (this.type == type) {
      return;
    }
    this.type = type;
    this.updateStyleClasses();
    this.applyStyleClasses();
  }

  /**
   * Sets the provided icon as the graphical content of the button.
   *
   * <p>This method replaces any existing graphic content with the specified {@link Node},
   * including images, SVGs, or other graphic elements.
   * </p>
   *
   * @param icon The {@link Node} to be used as the button's icon. Can be null,
   *             in which case any existing graphic content will be removed.
   */
  public void setIcon(Node icon) {
    this.setGraphic(icon);
  }

  /**
   * Updates the CSS style classes of the button to reflect its current {@link ButtonType}.
   *
   * <p>This method ensures that only the style class corresponding to the button's current type
   * is applied. If the button type changes, the previously applied style class is removed,
   * and the appropriate style class for the new type is added to the button.
   * </p>
   *
   * <p><b>Behavior Details:</b></p>
   * <ul>
   *   <li>If the button's type matches an entry in {@link ButtonType}, its corresponding
   *   style class is added using {@code getStyleClassForButtonType(ButtonType)}.</li>
   *   <li>Any style classes related to other {@link ButtonType} values will be removed from
   *   the button.</li>
   *   <li>If an unsupported or null {@link ButtonType} is set, this method may not alter the
   *   style classes as expected.</li>
   * </ul>
   *
   * <p><b>Edge Cases:</b></p>
   * <ul>
   *   <li>If the button type does not match any predefined {@link ButtonType}, the operation
   *   falls back to the default style class as defined in {@code getStyleClassForButtonType}.
   *   </li>
   *   <li>Repeated calls with the same {@code ButtonType} do not add duplicate style classes.
   *   </li>
   * </ul>
   *
   * @throws NullPointerException if the current button type or the derived style class is null.
   */
  @Override
  public void updateStyleClasses() {
    for (ButtonType buttonType : ButtonType.values()) {
      if (buttonType == this.type) {
        this.styleClasses.add(this.getStyleClassForButtonType(buttonType));
      } else {
        this.styleClasses.remove(this.getStyleClassForButtonType(buttonType));
      }
    }
  }

  /**
   * Defines the visual and functional types of a button, determining its appearance
   * and style behavior in conjunction with the {@code Button} class.
   *
   * <p>The {@code ButtonType} enum provides predefined constants for supported button styles.
   * Each type is associated with a specific set of CSS style classes that control the
   * button's appearance. It is utilized primarily in the {@code Button} class to configure
   * visual styling and behavior.</p>
   *
   * <p><b>Supported Button Types:</b></p>
   * <ul>
   *   <li>{@link #DEFAULT}: Standard button style.</li>
   *   <li>{@link #OUTLINED}: Button with an outlined border and transparent background.</li>
   *   <li>{@link #FLAT}: Minimalistic button with no additional borders or background styling.
   *   </li>
   *   <li>{@link #NAVIGATION}: Button intended for navigation elements, typically styled
   *   accordingly.</li>
   * </ul>
   */
  public enum ButtonType {
    DEFAULT("default"),
    OUTLINED("outlined"),
    FLAT("flat"),
    NAVIGATION("navigation");

    private final String value;

    ButtonType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

}
