package no.ntnu.principes.util.styles;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Utility class for managing JavaFX node styles and layout constraints.
 * Provides methods for applying CSS styles, setting growth behaviors,
 * and configuring padding and margins for UI elements.
 */
public class StyleManager {

  /*
   * Utilities
   */

  public static ThemeProvider getThemeProvider() {
    return ThemeProvider.getInstance();
  }

  /**
   * Applies CSS style classes to a node.
   *
   * @param node   The node to apply styles to
   * @param styles The style classes to apply
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager apply(Node node, String... styles) {
    return new NodeStyleManager(node).apply(styles);
  }

  /**
   * Removes CSS style classes from a node.
   *
   * @param node   The node to remove styles from
   * @param styles The style classes to remove
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager unapply(Node node, String... styles) {
    return new NodeStyleManager(node).unapply(styles);
  }

  /**
   * Sets a node to grow horizontally with the specified priority.
   *
   * @param node     The node to configure
   * @param priority The growth priority
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager growHorizontal(Region node, Priority priority) {
    return new NodeStyleManager(node).growHorizontal(priority);
  }

  /**
   * Sets multiple nodes to grow horizontally with ALWAYS priority.
   *
   * @param nodes The nodes to configure
   * @return An array of NodeStyleManagers for further styling
   */
  public static NodeStyleManager[] growHorizontal(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager[] managers = new NodeStyleManager[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      managers[i] = new NodeStyleManager(nodes[i]).growHorizontal(Priority.ALWAYS);
    }
    return managers;
  }

  /**
   * Sets multiple nodes to never grow horizontally.
   *
   * @param nodes The nodes to configure
   * @return An array of NodeStyleManagers for further styling
   */
  public static NodeStyleManager[] shrinkHorizontal(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager[] managers = new NodeStyleManager[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      managers[i] = new NodeStyleManager(nodes[i]).shrinkHorizontal();
    }
    return managers;
  }

  /**
   * Sets a node to grow vertically with the specified priority.
   *
   * @param node     The node to configure
   * @param priority The growth priority
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager growVertical(Region node, Priority priority) {
    return new NodeStyleManager(node).growVertical(priority);
  }

  /**
   * Sets multiple nodes to grow vertically with ALWAYS priority.
   *
   * @param nodes The nodes to configure
   * @return The NodeStyleManager for the last node
   */
  public static NodeStyleManager growVertical(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager lastManager = null;
    for (Region node : nodes) {
      lastManager = new NodeStyleManager(node).growVertical(Priority.ALWAYS);
    }
    return lastManager;
  }

  /**
   * Sets multiple nodes to never grow vertically.
   *
   * @param nodes The nodes to configure
   * @return The NodeStyleManager for the last node
   */
  public static NodeStyleManager shrinkVertical(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager lastManager = null;
    for (Region node : nodes) {
      lastManager = new NodeStyleManager(node).shrinkVertical();
    }
    return lastManager;
  }

  /**
   * Sets multiple nodes to grow both horizontally and vertically.
   *
   * @param nodes The nodes to configure
   * @return The NodeStyleManager for the last node
   */
  public static NodeStyleManager grow(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager lastManager = null;
    for (Region node : nodes) {
      lastManager = new NodeStyleManager(node).grow();
    }
    return lastManager;
  }

  /**
   * Sets multiple nodes to never grow in either direction.
   *
   * @param nodes The nodes to configure
   * @return The NodeStyleManager for the last node
   */
  public static NodeStyleManager shrink(Region... nodes) {
    if (nodes.length == 0) {
      return null;
    }
    NodeStyleManager lastManager = null;
    for (Region node : nodes) {
      lastManager = new NodeStyleManager(node).shrink();
    }
    return lastManager;
  }

  /**
   * Sets uniform padding on all sides of a node.
   *
   * @param node    The node to configure
   * @param padding The padding value to apply to all sides
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager padding(Region node, double padding) {
    return new NodeStyleManager(node).padding(padding);
  }

  /**
   * Sets custom padding on each side of a node.
   *
   * @param node   The node to configure
   * @param top    The top padding value
   * @param right  The right padding value
   * @param bottom The bottom padding value
   * @param left   The left padding value
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager padding(Region node, double top, double right, double bottom,
                                         double left) {
    return new NodeStyleManager(node).padding(top, right, bottom, left);
  }

  /**
   * Sets padding on a node using an Insets object.
   *
   * @param node   The node to configure
   * @param insets The Insets object defining the padding
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager padding(Region node, Insets insets) {
    return new NodeStyleManager(node).padding(insets);
  }

  /**
   * Sets symmetric padding on a node.
   *
   * @param node       The node to configure
   * @param horizontal The horizontal (left and right) padding value
   * @param vertical   The vertical (top and bottom) padding value
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager padding(Region node, double horizontal, double vertical) {
    return new NodeStyleManager(node).padding(horizontal, vertical);
  }

  /**
   * Sets uniform margin on all sides of a node.
   *
   * @param node   The node to configure
   * @param margin The margin value to apply to all sides
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager margin(Region node, double margin) {
    return new NodeStyleManager(node).margin(margin);
  }

  /**
   * Sets custom margin on each side of a node.
   *
   * @param node   The node to configure
   * @param top    The top margin value
   * @param right  The right margin value
   * @param bottom The bottom margin value
   * @param left   The left margin value
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager margin(Region node, double top, double right, double bottom,
                                        double left) {
    return new NodeStyleManager(node).margin(top, right, bottom, left);
  }

  /**
   * Sets margin on a node using an Insets object.
   *
   * @param node   The node to configure
   * @param insets The Insets object defining the margin
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager margin(Region node, Insets insets) {
    return new NodeStyleManager(node).margin(insets);
  }

  /**
   * Sets symmetric margin on a node.
   *
   * @param node       The node to configure
   * @param horizontal The horizontal (left and right) margin value
   * @param vertical   The vertical (top and bottom) margin value
   * @return A NodeStyleManager for further styling
   */
  public static NodeStyleManager margin(Region node, double horizontal, double vertical) {
    return new NodeStyleManager(node).margin(horizontal, vertical);
  }

  /*
   * CSS class constants
   */

  /**
   * CSS classes for button styling.
   */
  public static final class ButtonStyle {
    /**
     * Style class for selectable buttons.
     */
    public static final String SELECTABLE = "btn-selectable";
    /**
     * Style class for navigation buttons.
     */
    public static final String NAVIGATION = "btn-navigation";

    /**
     * Pseudo-classes for button states.
     */
    public static final class PseudoClass {
      /**
       * Selected state for buttons.
       */
      public static final String SELECTED = "selected";
      /**
       * Hovered state for buttons.
       */
      public static final String HOVERED = "hovered";
    }
  }

  /**
   * CSS classes for input field styling.
   */
  public static final class InputStyle {
    /**
     * Base style class for text inputs.
     */
    public static final String INPUT = "text-input";

    /**
     * Pseudo-classes for input states.
     */
    public static final class PseudoClass {
      /**
       * Error state for inputs.
       */
      public static final String ERROR = "error";
      /**
       * Disabled state for inputs.
       */
      public static final String DISABLED = "disabled";
      /**
       * Valid/success state for inputs.
       */
      public static final String VALID = "success";
    }
  }

  /**
   * CSS classes for typography styling.
   */
  public static final class Typography {
    /**
     * Style for page titles.
     */
    public static final String PAGE_TITLE = "text-page-title";
    /**
     * Style for section headers.
     */
    public static final String SECTION_HEADER = "text-section-header";
    /**
     * Style for subheaders.
     */
    public static final String SUBHEADER = "text-subheader";
    /**
     * Style for body text.
     */
    public static final String BODY = "text-body";
    /**
     * Style for helper/hint text.
     */
    public static final String HELPER = "text-helper";
    /**
     * Style for button text.
     */
    public static final String BUTTON = "text-button";
    /**
     * Style for form labels.
     */
    public static final String FORM_LABEL = "text-form-label";
    /**
     * Style for error messages.
     */
    public static final String ERROR_MESSAGE = "text-error-message";
    /**
     * Style for notification text.
     */
    public static final String NOTIFICATION = "text-notification";
  }

  /**
   * CSS classes for overlay styling.
   */
  public static final class Overlay {
    /**
     * Base style for overlay elements.
     */
    public static final String OVERLAY = "is-overlay";
    /**
     * Style for card elements within overlays.
     */
    public static final String CARD = "overlay-card";
  }

  /**
   * Inner class for chained styling operations on a single node.
   * Provides a fluent API for applying multiple styling operations.
   */
  public static class NodeStyleManager {
    private final Node node;

    /**
     * Creates a style manager for a specific node.
     *
     * @param node The node to style
     */
    public NodeStyleManager(Node node) {
      this.node = node;
    }

    /**
     * Applies CSS style classes to the node.
     *
     * @param styles The style classes to apply
     * @return This manager for method chaining
     */
    public NodeStyleManager apply(String... styles) {
      node.getStyleClass().addAll(styles);
      return this;
    }

    /**
     * Removes CSS style classes from the node.
     *
     * @param styles The style classes to remove
     * @return This manager for method chaining
     */
    public NodeStyleManager unapply(String... styles) {
      node.getStyleClass().removeAll(styles);
      return this;
    }

    /**
     * Sets the node to grow horizontally with the specified priority.
     * Clears any existing horizontal constraints first.
     *
     * @param priority The growth priority
     * @return This manager for method chaining
     */
    public NodeStyleManager growHorizontal(Priority priority) {
      if (node instanceof Region region) {
        HBox.clearConstraints(region);
        HBox.setHgrow(region, priority);
        if (!region.maxWidthProperty()
            .isBound()) { // If we have a binding, we don't want to override it
          region.setMaxWidth(Double.MAX_VALUE);
        }
      }
      return this;
    }

    /**
     * Sets the node to grow vertically with the specified priority.
     * Clears any existing vertical constraints first.
     *
     * @param priority The growth priority
     * @return This manager for method chaining
     */
    public NodeStyleManager growVertical(Priority priority) {
      if (node instanceof Region region) {
        VBox.clearConstraints(region);
        VBox.setVgrow(region, priority);
        if (!region.maxHeightProperty()
            .isBound()) { // If we have a binding, we don't want to override it
          region.setMaxHeight(Double.MAX_VALUE);
        }
      }
      return this;
    }

    /**
     * Sets the node to never grow horizontally.
     *
     * @return This manager for method chaining
     */
    public NodeStyleManager shrinkHorizontal() {
      return growHorizontal(Priority.NEVER);
    }

    /**
     * Sets the node to never grow vertically.
     *
     * @return This manager for method chaining
     */
    public NodeStyleManager shrinkVertical() {
      return growVertical(Priority.NEVER);
    }

    /**
     * Sets the node to grow in both directions.
     *
     * @return This manager for method chaining
     */
    public NodeStyleManager grow() {
      growHorizontal(Priority.ALWAYS);
      growVertical(Priority.ALWAYS);
      return this;
    }

    /**
     * Sets the node to never grow in either direction.
     *
     * @return This manager for method chaining
     */
    public NodeStyleManager shrink() {
      shrinkHorizontal();
      shrinkVertical();
      return this;
    }

    /**
     * Sets uniform padding on all sides of the node.
     *
     * @param padding The padding value to apply to all sides
     * @return This manager for method chaining
     */
    public NodeStyleManager padding(double padding) {
      if (node instanceof Region) {
        ((Region) node).setPadding(new Insets(padding));
      }
      return this;
    }

    /**
     * Sets custom padding on each side of the node.
     *
     * @param top    The top padding value
     * @param right  The right padding value
     * @param bottom The bottom padding value
     * @param left   The left padding value
     * @return This manager for method chaining
     */
    public NodeStyleManager padding(double top, double right, double bottom, double left) {
      if (node instanceof Region) {
        ((Region) node).setPadding(new Insets(top, right, bottom, left));
      }
      return this;
    }

    /**
     * Sets padding on the node using an Insets object.
     *
     * @param insets The Insets object defining the padding
     * @return This manager for method chaining
     */
    public NodeStyleManager padding(Insets insets) {
      if (node instanceof Region) {
        ((Region) node).setPadding(insets);
      }
      return this;
    }

    /**
     * Sets symmetric padding on the node.
     *
     * @param horizontal The horizontal (left and right) padding value
     * @param vertical   The vertical (top and bottom) padding value
     * @return This manager for method chaining
     */
    public NodeStyleManager padding(double horizontal, double vertical) {
      return padding(vertical, horizontal, vertical, horizontal);
    }

    /**
     * Sets uniform margin on all sides of the node.
     * Applies to both HBox and VBox containers.
     *
     * @param margin The margin value to apply to all sides
     * @return This manager for method chaining
     */
    public NodeStyleManager margin(double margin) {
      if (node instanceof Region region) {
        HBox.setMargin(region, new Insets(margin));
        VBox.setMargin(region, new Insets(margin));
      }
      return this;
    }

    /**
     * Sets custom margin on each side of the node.
     * Applies to both HBox and VBox containers.
     *
     * @param top    The top margin value
     * @param right  The right margin value
     * @param bottom The bottom margin value
     * @param left   The left margin value
     * @return This manager for method chaining
     */
    public NodeStyleManager margin(double top, double right, double bottom, double left) {
      if (node instanceof Region region) {
        Insets insets = new Insets(top, right, bottom, left);
        HBox.setMargin(region, insets);
        VBox.setMargin(region, insets);
      }
      return this;
    }

    /**
     * Sets margin on the node using an Insets object.
     * Applies to both HBox and VBox containers.
     *
     * @param insets The Insets object defining the margin
     * @return This manager for method chaining
     */
    public NodeStyleManager margin(Insets insets) {
      if (node instanceof Region region) {
        HBox.setMargin(region, insets);
        VBox.setMargin(region, insets);
      }
      return this;
    }

    /**
     * Sets symmetric margin on the node.
     *
     * @param horizontal The horizontal (left and right) margin value
     * @param vertical   The vertical (top and bottom) margin value
     * @return This manager for method chaining
     */
    public NodeStyleManager margin(double horizontal, double vertical) {
      return margin(vertical, horizontal, vertical, horizontal);
    }
  }

  public static class ThemeProvider {
    private final StringProperty theme = new SimpleStringProperty("default");

    private ThemeProvider() {
      // Private constructor to prevent instantiation
    }

    public String getTheme() {
      return theme.get();
    }

    public StringProperty themeProperty() {
      return theme;
    }

    public void setTheme(String theme) {
      this.theme.set(theme);
    }

    public static ThemeProvider getInstance() {
      return Holder.INSTANCE;
    }

    private static class Holder {
      private static final ThemeProvider INSTANCE = new ThemeProvider();
    }
  }
}