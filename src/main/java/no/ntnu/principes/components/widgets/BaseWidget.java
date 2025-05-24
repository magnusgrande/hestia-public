package no.ntnu.principes.components.widgets;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

/**
 * Extends {@link BaseComponent} to provide a styled widget with designated dimensions,
 * container setup, and common behaviors for widgets in an application.
 *
 * <p>The `BaseWidget` sets up a consistent structure for child widgets, including predefined
 * dimensions, styled containers, and lifecycle methods inherited from the base class.
 * Supports customization through extensions.
 * </p>
 */
public class BaseWidget extends BaseComponent {
  // Constants
  protected final int BORDER_RADIUS = 10;
  protected final int FONT_SIZE = 86;
  protected final double SUB_FONT_SIZE_SCALE = 0.325;

  // Component state
  protected final VBox contentContainer;
  protected final VBox textContainer;
  protected final Text titleText;
  protected final Text descriptionText;

  private final StringProperty titleProperty = new SimpleStringProperty("");
  private final StringProperty descriptionProperty = new SimpleStringProperty("");

  /**
   * Constructs a new `BaseWidget` with just a title header.
   *
   * @param componentId  a unique identifier for this widget instance.
   * @param widgetHeader the text to display as the widget's header.
   * @param parentScreen the parent {@link BaseScreen} to which this widget belongs.
   */
  public BaseWidget(String componentId, String widgetHeader, BaseScreen parentScreen) {
    this(componentId, widgetHeader, "", parentScreen);
  }

  /**
   * Constructs a new `BaseWidget` with a title and optional description.
   *
   * @param componentId       a unique identifier for this widget instance.
   * @param widgetHeader      the text to display as the widget's header.
   * @param widgetDescription the text to display as the widget's description. Can be empty.
   * @param parentScreen      the parent {@link BaseScreen} to which this widget belongs.
   */
  public BaseWidget(String componentId, String widgetHeader, String widgetDescription,
                    BaseScreen parentScreen) {
    super(componentId, parentScreen);

    // Initialize contentContainer
    this.contentContainer = new VBox();
    this.setupContainer();

    // Create text components with bindings
    this.textContainer = new VBox();
    this.textContainer.setPadding(InsetBuilder.uniform(20).bottom(0).build());
    this.titleText = new Text("", StyledText.TextType.SUBHEADER);
    this.descriptionText = new Text("", StyledText.TextType.BODY);

    // Bind text values to properties
    this.titleText.textProperty().bind(this.titleProperty);
    this.descriptionText.textProperty().bind(this.descriptionProperty);

    this.setTitle(widgetHeader);
    this.setDescription(widgetDescription);

    // Style the descrition text
    this.descriptionText.setStyle(
        "-fx-font-size: " + (FONT_SIZE * 0.175) + "px;-fx-text-fill: -color-fg-subtle;");

    // Bind visibility of description text to only shw when not empyu
    this.descriptionText.visibleProperty().bind(
        Bindings.createBooleanBinding(
            () -> !this.descriptionProperty.get().isEmpty(),
            this.descriptionProperty
        )
    );
    this.descriptionText.managedProperty().bind(this.descriptionText.visibleProperty());

    this.textContainer.getChildren().addAll(this.titleText, this.descriptionText);

    this.contentContainer.getChildren().addFirst(this.textContainer);
    this.getChildren().addAll(this.contentContainer);

  }

  /**
   * Returns the title property.
   *
   * @return the title property
   */
  public StringProperty titleProperty() {
    return titleProperty;
  }

  /**
   * Gets the current title value.
   *
   * @return the current title
   */
  public String getTitle() {
    return titleProperty.get();
  }

  /**
   * Sets a new title value.
   *
   * @param title the new title
   */
  public void setTitle(String title) {
    this.titleProperty.set(title);
  }

  /**
   * Returns the description property.
   *
   * @return the description property
   */
  public StringProperty descriptionProperty() {
    return descriptionProperty;
  }

  /**
   * Gets the current description value.
   *
   * @return the current description
   */
  public String getDescription() {
    return descriptionProperty.get();
  }

  /**
   * Sets a new description value.
   * If the description is null or empty, the description text will be hidden.
   * Otherwise, it will be shown.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.descriptionProperty.set(description != null ? description : "");
  }

  /**
   * Initialize the component. This method is called when the component is created.
   * Subclasses should override this method to perform any initialization
   */
  @Override
  protected void initializeComponent() {
    // noop
  }

  /**
   * Configures the layout and styling of the `contentContainer` for the widget.
   *
   * <p>Sets up alignment, padding, spacing, preferred dimensions, and visual styles
   * such as borders and background. This method ensures that the widget's content
   * container adheres to consistent design rules and dimensions across all widgets.</p>
   *
   * <p>Subclasses can override this method to provide custom styling or layout
   * behaviors if needed.</p>
   */
  protected void setupContainer() {
    this.contentContainer.setAlignment(Pos.CENTER);
    this.contentContainer.setSpacing(10);
    StyleManager.growHorizontal(this.contentContainer);
    this.contentContainer.setStyle(
        "-fx-border-width: 1px;-fx-border-color: -color-border-default;-fx-background-color: -color-bg-overlay;-fx-background-radius: " +
            BORDER_RADIUS + "px;-fx-border-radius: " + BORDER_RADIUS + "px;");
  }

  @Override
  protected void onMount() {

  }

  @Override
  protected void onUnmount() {

  }

  @Override
  protected void onDestroy() {

  }

  /**
   * Set the widget's width in pixels.
   *
   * @param width the desired width for the widget, in pixels. Must be a positive value.
   */
  public void setWidgetWidth(double width) {
    this.setWidth(width);
    this.setPrefWidth(width);
    this.contentContainer.setPrefWidth(width);
    this.fitContent();
  }

  /**
   * Set the widget's height in pixels.
   *
   * @param height the desired height for the widget, in pixels. Must be a positive value.
   */
  public void setWidgetHeight(double height) {
    this.setHeight(height);
    this.contentContainer.setPrefHeight(height);
  }

  /**
   * Adjusts the height of the content area to match the widget's height.
   */
  protected void fitContent() {
    this.contentContainer.requestLayout();
  }

  /**
   * Refreshes the widget's data or state.
   */
  public boolean refresh() {
    return true;
  }
}