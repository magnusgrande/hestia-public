package no.ntnu.principes.components.validations;

import java.util.function.Function;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.synedra.validatorfx.Decoration;
import net.synedra.validatorfx.Severity;
import net.synedra.validatorfx.StyleClassDecoration;
import net.synedra.validatorfx.ValidationMessage;

/**
 * FormDecoration is the default DefaultDecoration provided by atlantafx, but modified to fit the
 * needs of the project, as the class itself is not extendable.
 *
 * @author r.lichtenberger@synedra.com
 */
public class FormDecoration {

  private static final String POPUP_SHADOW_EFFECT =
      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 5);";
  private static final String TOOLTIP_COMMON_EFFECTS =
      "-fx-font-weight: bold; -fx-padding: 5; -fx-border-width:1;";

  private static final String ERROR_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
      + "-fx-background-color: #FBEFEF; -fx-text-fill: #cc0033; -fx-border-color:#cc0033;";

  private static final String WARNING_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
      + "-fx-background-color: #FFFFCC; -fx-text-fill: #CC9900; -fx-border-color: #CC9900;";
  private static final int ICON_SIZE = 24;

  private static Function<ValidationMessage, Decoration> factory;

  private FormDecoration() {
  }

  /**
   * Returns the default factory for creating decorations.
   *
   * @return the default factory
   */
  public static Function<ValidationMessage, Decoration> getFactory() {
    if (factory == null) {
      factory = FormDecoration::createGraphicDecoration;
    }
    return factory;
  }

  /**
   * Sets the factory for creating decorations.
   *
   * @param factory the factory to set
   */
  public static void setFactory(Function<ValidationMessage, Decoration> factory) {
    FormDecoration.factory = factory;
  }

  /**
   * Creates a new decoration based on the provided validation message.
   *
   * @param message the validation message
   * @return the created decoration
   */
  public static StyleClassDecoration createStyleClassDecoration(ValidationMessage message) {
    return new StyleClassDecoration(
        "validatorfx-" + message.getSeverity().toString().toLowerCase());
  }

  /**
   * Creates a new graphic decoration based on the provided validation message.
   *
   * @param message the validation message
   * @return the created graphic decoration
   */
  public static ExtendedGraphicDecoration createGraphicDecoration(ValidationMessage message) {
    return new ExtendedGraphicDecoration(createDecorationNode(message), Pos.TOP_LEFT);
  }

  /**
   * Creates a new graphic decoration based on the provided validation message.
   *
   * @param message the validation message
   * @return the created graphic decoration
   */
  private static Node createDecorationNode(ValidationMessage message) {
    Node graphic =
        Severity.ERROR == message.getSeverity() ? createErrorNode() : createWarningNode();
    graphic.getStyleClass().add("shadow_effect");
    Label label = new Label();
    label.setGraphic(graphic);
    label.setTooltip(createTooltip(message));
    label.setAlignment(Pos.CENTER);
    return label;
  }

  /**
   * Creates a new tooltip based on the provided validation message.
   *
   * @param message the validation message
   * @return the created tooltip
   */
  private static Tooltip createTooltip(ValidationMessage message) {
    Tooltip tooltip = new Tooltip(message.getText());
    tooltip.setOpacity(.9);
    tooltip.setAutoFix(true);
    tooltip.setStyle(
        Severity.ERROR == message.getSeverity() ? ERROR_TOOLTIP_EFFECT : WARNING_TOOLTIP_EFFECT);
    return tooltip;
  }

  /**
   * Creates a new error node.
   *
   * @return the created error node
   */
  private static Node createErrorNode() {
    Image icon = new Image(
        FormDecoration.class.getResourceAsStream("/no/ntnu/principes/images/error-icon.png"));
    ImageView imageView = new ImageView(icon);
    imageView.setFitHeight(ICON_SIZE);
    imageView.setFitWidth(ICON_SIZE);

    return imageView;
  }

  /**
   * Creates a new warning node.
   *
   * @return the created warning node
   */
  private static Node createWarningNode() {
    Image icon = new Image(
        FormDecoration.class.getResourceAsStream("/no/ntnu/principes/images/warning-icon.png"));
    ImageView imageView = new ImageView(icon);
    imageView.setFitHeight(ICON_SIZE);
    imageView.setFitWidth(ICON_SIZE);

    return imageView;
  }
}
