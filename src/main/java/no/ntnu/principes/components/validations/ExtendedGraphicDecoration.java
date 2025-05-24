package no.ntnu.principes.components.validations;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import net.synedra.validatorfx.GraphicDecoration;

/**
 * Extended version of GraphicDecoration that adds styles to target nodes to indicate validation
 * errors.
 */
public class ExtendedGraphicDecoration extends GraphicDecoration {
  /**
   * Creates a new ExtendedGraphicDecoration with the specified decoration node.
   *
   * @param decorationNode The node to use as the decoration
   */
  public ExtendedGraphicDecoration(Node decorationNode) {
    super(decorationNode);
  }

  /**
   * Creates a new ExtendedGraphicDecoration with the specified decoration node and position.
   *
   * @param decorationNode The node to use as the decoration
   * @param position       The position of the decoration
   */
  public ExtendedGraphicDecoration(Node decorationNode, Pos position) {
    super(decorationNode, position);
  }

  /**
   * Creates a new ExtendedGraphicDecoration with the specified decoration node, position, and
   * offsets.
   *
   * @param decorationNode The node to use as the decoration
   * @param position       The position of the decoration
   * @param xOffset        The x offset of the decoration
   * @param yOffset        The y offset of the decoration
   */
  public ExtendedGraphicDecoration(Node decorationNode, Pos position, double xOffset,
                                   double yOffset) {
    super(decorationNode, position, xOffset, yOffset);
  }

  @Override
  public void add(Node target) {
    super.add(target);
    if (target instanceof TextField textField) {
      textField.pseudoClassStateChanged(Styles.STATE_DANGER, true);
      textField.setStyle("-fx-border-color: -color-danger-emphasis;-fx-border-width: 2px;");
    } else if (target instanceof DatePicker datePicker) {
      datePicker.pseudoClassStateChanged(Styles.STATE_DANGER, true);
      datePicker.setStyle("-fx-border-color: -color-danger-emphasis;-fx-border-width: 2px;");
    } else if (target instanceof ComboBox<?> comboBox) {
      comboBox.pseudoClassStateChanged(Styles.STATE_DANGER, true);
      comboBox.setStyle("-fx-border-color: -color-danger-emphasis;-fx-border-width: 2px;");
    }
  }

  @Override
  public void remove(Node target) {
    super.remove(target);
    if (target instanceof TextField textField) {
      textField.pseudoClassStateChanged(Styles.STATE_DANGER, false);
      textField.setStyle("");
    } else if (target instanceof DatePicker datePicker) {
      datePicker.pseudoClassStateChanged(Styles.STATE_DANGER, false);
      datePicker.setStyle("");
    } else if (target instanceof ComboBox<?> comboBox) {
      comboBox.pseudoClassStateChanged(Styles.STATE_DANGER, false);
      comboBox.setStyle("");
    }
  }
}
