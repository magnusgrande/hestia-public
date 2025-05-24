package no.ntnu.principes.components.primary;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * A configurable UI badge component for displaying text with different visual styles.
 * The badge supports various predefined styling variants such as warning, success, and danger.
 * It is implemented as an {@link HBox} with inner content wrapped in a styled container.
 */
@Getter
public class Badge extends HBox {
  private final HBox innerBox = new HBox();
  private Variant variant;

  /**
   * Constructs a Badge component with specified text and styling variant for the UI.
   *
   * @param text    the text to display inside the badge; cannot be null.
   * @param variant the visual styling variant for the badge; determines the appearance such as
   *                color or border.
   *                Supported variants include DEFAULT, INFO, WARNING, SUCCESS, DANGER, OUTLINED,
   *                and PRIMARY.
   */
  public Badge(String text, Variant variant) {
    super();
    innerBox.getStyleClass().add("badge");
    VBox.setVgrow(innerBox, Priority.NEVER);
    VBox.setVgrow(this, Priority.NEVER);
    StyleManager.padding(innerBox, 15, -5).shrink();
    this.getChildren().add(innerBox);
    Label textNode = new Label(text);
    StyleManager.padding(textNode, InsetBuilder.uniform(-5).build()).shrink();
    innerBox.getChildren().add(textNode);
    this.variant = variant;
    this.setAlignment(Pos.CENTER);
    innerBox.setAlignment(Pos.CENTER);
    this.updateStyle();
  }

  /**
   * Updates the visual style of the badge based on the {@code variant} field.
   *
   * <p>This method removes any existing styling classes from the badge and applies the style class
   * corresponding to the current {@link Variant} value. The supported variant styles include
   * "badge-default", "badge-info", "badge-warning", "badge-success", "badge-danger",
   * "badge-outlined", and "badge-primary". The style is applied to the {@code innerBox} component
   * of the badge.
   * </p>
   */
  private void updateStyle() {
    this.getStyleClass()
        .removeAll("badge-default", "badge-info", "badge-warning", "badge-success",
            "badge-danger", "badge-outlined", "badge-primary");
    switch (this.variant) {
      case DEFAULT:
        innerBox.getStyleClass().add("badge-default");
        break;
      case INFO:
        innerBox.getStyleClass().add("badge-info");
        break;
      case WARNING:
        innerBox.getStyleClass().add("badge-warning");
        break;
      case SUCCESS:
        innerBox.getStyleClass().add("badge-success");
        break;
      case DANGER:
        innerBox.getStyleClass().add("badge-danger");
        break;
      case OUTLINED:
        innerBox.getStyleClass().add("badge-outlined");
        break;
      case PRIMARY:
        innerBox.getStyleClass().add("badge-primary");
        break;
      default:
        // No specific style applied, but existing styles will still be removed.
        break;
    }
  }

  /**
   * Updates the badge's visual appearance by setting the styling variant.
   *
   * <p>This method assigns a {@link Variant} to the badge, which determines its appearance
   * (e.g., color or border style), and refreshes the style to reflect the selected variant.
   * </p>
   *
   * @param variant the visual styling variant to apply to the badge.
   *                Supported values include {@code DEFAULT}, {@code INFO}, {@code WARNING},
   *                {@code SUCCESS}, {@code DANGER}, {@code OUTLINED}, and {@code PRIMARY}.
   *                If {@code null}, no specific styling will be applied, but existing styles will
   *                still be removed.
   */
  public void setVariant(Variant variant) {
    this.variant = variant;
    this.updateStyle();
  }

  /**
   * Defines the available visual styling variants for UI components such as badges and buttons.
   * Each variant corresponds to a specific pre-defined appearance, including color and border
   * styles, used to differentiate component types and states.
   *
   * <p>Supported variants:
   * <ul>
   *   <li>{@code DEFAULT} - The standard appearance.</li>
   *   <li>{@code OUTLINED} - A visual style with an outline border.</li>
   *   <li>{@code INFO} - Style intended for informational components.</li>
   *   <li>{@code WARNING} - Style indicating warnings or caution.</li>
   *   <li>{@code SUCCESS} - Style representing success states.</li>
   *   <li>{@code DANGER} - Style indicating errors or critical states.</li>
   *   <li>{@code PRIMARY} - A primary style for emphasizing components.</li>
   * </ul>
   * </p>
   */
  public enum Variant {
    OUTLINED,
    DEFAULT,
    INFO,
    WARNING,
    SUCCESS,
    DANGER,
    PRIMARY;

    /**
     * Returns the badge variant based on a 1-6 weight.
     *
     * @param weight the weight of the variant, which should be between 1 and 6.
     * @return the badge variant corresponding to the weight.
     */
    public static Variant getWeightVariant(int weight) {
      if (weight < 1 || weight > 6) {
        throw new IllegalArgumentException("Weight must be between 1 and 6");
      }
      return switch (weight) {
        case 1, 2 -> Variant.SUCCESS;
        case 3, 4 -> Variant.WARNING;
        case 5, 6 -> Variant.DANGER;
        default -> Variant.DEFAULT;
      };
    }
  }

}
