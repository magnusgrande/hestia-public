package no.ntnu.principes.components.primary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.AccessibleRole;

/**
 * A customizable text component with specific styles based on its type.
 *
 * <p>This class extends {@link javafx.scene.control.Label} and allows pre-defined
 * and dynamic CSS style classes to be applied based on the {@link TextType} and
 * additional initial style classes. It provides methods for initializing and
 * updating style information and is integrated with the {@link StyleableComponent}
 * interface.
 * </p>
 */
public abstract class StyledText extends javafx.scene.control.Label
    implements StyleableComponent {
  protected final Set<String> styleClasses = new HashSet<>();
  protected final TextType type;

  /**
   * Creates a styled text element with a specified text value, type, and initial style classes.
   *
   * <p>This constructor initializes a {@link javafx.scene.control.Label} with customizable
   * appearance based on the {@link TextType} parameter and additional style classes.
   * It also performs setup for accessibility and applies the provided styles to the component.
   * </p>
   *
   * @param text           the text content to be displayed
   * @param type           the visual type of the text, determining its default style class.
   *                       Accepted values are defined in {@link TextType}.
   * @param initialClasses a list of additional CSS style classes to apply.
   *                       This list may be empty but cannot be null.
   *                       Duplicate classes will be merged.
   */
  protected StyledText(String text, TextType type, List<String> initialClasses) {
    super(text);
    this.type = type;
    this.styleClasses.addAll(initialClasses);
    this.initialize();
  }

  /**
   * Constructs a styled text element with the specified content and visual type.
   *
   * <p>This method initializes a text-based UI component extending
   * {@link javafx.scene.control.Label}.
   * The appearance of the text is determined by the provided {@link TextType}, which defines a
   * default style class, and additional setup is performed for styling and accessibility features.
   * </p>
   *
   * @param text the content to display as the label. This value cannot be null and determines the
   *             visible text.
   * @param type the category of text, used to assign default styling via its {@link TextType}
   *             value.
   *             The valid types include titles, headers, body text, buttons, and notifications.
   *             Refer to {@link TextType} for a complete list of supported types.
   */
  protected StyledText(String text, TextType type) {
    super(text);
    this.type = type;
    this.initialize();
  }

  /**
   * Initializes the component by setting up style classes and accessibility features.
   */
  protected void initialize() {
    this.updateStyleClasses();
    this.setAccessibleRole(AccessibleRole.BUTTON);
    this.applyStyleClasses();
  }

  /**
   * Updates the style classes based on the text type and additional classes.
   */
  @Override
  public void applyStyleClasses() {
    this.getStyleClass().setAll(this.styleClasses);
  }

  /**
   * Defines categories of styled text elements for a UI component.
   *
   * <p>Each value in the enumeration corresponds to a distinct visual role and
   * default styling in the application's design system. These roles include
   * titles, headers, buttons, notifications, and other UI text components.
   * The {@link TextType} is used to determine the default CSS style class
   * applied to a text element.
   * </p>
   *
   * <p><b>Available Types:</b></p>
   * <ul>
   *   <li>
   *     <b>PAGE_TITLE</b>: Large, prominent text for page titles.
   *   </li>
   *   <li>
   *     <b>SECTION_HEADER</b>: Medium-sized text for section headers.
   *   </li>
   *   <li>
   *     <b>SUBHEADER</b>: Smaller text for subheadings.
   *   </li>
   *   <li>
   *     <b>BODY</b>: Standard size for general body text.
   *   </li>
   *   <li>
   *     <b>HELPER</b>: Minimal, subtle text for hints or contextual help.
   *   </li>
   *   <li>
   *     <b>BUTTON</b>: Text styled for use on buttons.
   *   </li>
   *   <li>
   *     <b>FORM_LABEL</b>: Text for labeling form inputs.
   *   </li>
   *   <li>
   *     <b>ERROR_MESSAGE</b>: Text with distinct styling for displaying error messages.
   *   </li>
   *   <li><b>NOTIFICATION</b>: Styled text used for system notifications.</li>
   * </ul>
   */
  public enum TextType {
    PAGE_TITLE,
    SECTION_HEADER,
    SUBHEADER,
    BODY,
    HELPER,
    BUTTON,
    FORM_LABEL,
    ERROR_MESSAGE,
    NOTIFICATION
  }
}