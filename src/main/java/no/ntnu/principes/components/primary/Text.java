package no.ntnu.principes.components.primary;

import java.util.List;

/**
 * A styled text component with customizable behavior and predefined presentation types.
 *
 * <p>This class extends {@link StyledText} and provides specific styling based on the
 * {@link TextType} parameter. Additional style classes can be provided during initialization.
 * It supports dynamic updates to reflect changes in the component's type or style.
 * </p>
 */
public class Text extends StyledText {

  /**
   * Constructs a text component with specified content and type for styled UI elements.
   *
   * <p>This method initializes a styled text element with predefined visual formatting determined
   * by the {@link TextType}. The text content is displayed as provided, and the styling is
   * applied automatically based on the specified text type.
   * </p>
   *
   * @param text the content to be displayed. Must not be null.
   * @param type the styling category from {@link TextType} that determines the visual formatting
   *             of this text component.
   */
  public Text(String text, TextType type) {
    super(text, type);
  }

  /**
   * Constructs a new {@code Text} object with specified content, type, and optional CSS classes.
   *
   * @param text           The textual content to display. Cannot be null.
   * @param type           The {@link TextType} that determines the formatting style.
   *                       Must be a valid constant of {@code TextType}.
   * @param initialClasses Optional CSS class names to style the text.
   *                       Multiple classes can be provided.
   */
  public Text(String text, TextType type, String... initialClasses) {
    super(text, type, List.of(initialClasses));
  }

  /**
   * Maps a {@link TextType} to its corresponding CSS style class name.
   *
   * <p>The method returns a specific CSS class name based on the current {@code TextType} of the
   * {@link Text} component.
   * If the {@code TextType} is not recognized or is {@code null}, an empty string is returned.
   * </p>
   *
   * @return A {@code String} representing the CSS class name associated with the current
   * {@code TextType}. Returns an empty string if the {@code TextType} is unsupported or not set.
   */
  private String getStyleClassFromTextType() {
    return switch (this.type) {
      case PAGE_TITLE -> "text-page-title";
      case SECTION_HEADER -> "text-section-header";
      case SUBHEADER -> "text-subheader";
      case BODY -> "text-body";
      case HELPER -> "text-helper";
      case BUTTON -> "text-button";
      case FORM_LABEL -> "text-form-label";
      case ERROR_MESSAGE -> "text-error-message";
      case NOTIFICATION -> "text-notification";
    };
  }

  /**
   * Updates the {@code styleClasses} list by adding a CSS class derived from the current
   * {@link TextType}.
   *
   * @throws NullPointerException if {@code styleClasses} is null.
   */
  @Override
  public void updateStyleClasses() {
    this.styleClasses.add(this.getStyleClassFromTextType());
  }
}
