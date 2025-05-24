package no.ntnu.principes.components;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import no.ntnu.principes.controller.StageController;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * Creates a custom window title bar with macOS-style control buttons.
 * Provides window dragging functionality and animated visual feedback.
 * Used to replace the default system window decoration in undecorated windows.
 */
public class WindowTitleBar extends HBox {
  private static final double TITLE_BAR_HEIGHT = 30;
  private static final double BUTTON_SIZE = 12;
  private static final double BUTTON_RADIUS = 6;
  private static final double WINDOW_CONTROLS_SPACING = 6;
  private static final Duration ANIMATION_DURATION = Duration.millis(100);
  private static final double SCALE_FACTOR = 0.99;
  private static final double BUTTON_SCALE_FACTOR = 1.2;
  private static final double BUTTON_OPACITY_NORMAL = 0.5;
  private static final double BUTTON_OPACITY_HOVER = 1.0;
  private static final String[] BUTTON_COLORS =
      new String[] {"-color-danger-4", "-color-chart-2", "-color-success-3"};

  private double offsetX = 0;
  private double offsetY = 0;
  private final StageController stageController;
  private final BooleanProperty isFullscreenOrMaximized = new SimpleBooleanProperty(false);

  /**
   * Creates a new WindowTitleBar for the specified stage.
   *
   * @param stageController The controller for the stage that this title bar belongs to
   */
  public WindowTitleBar(StageController stageController) {
    this.stageController = stageController;
    this.initialize();
  }

  /**
   * Initializes the title bar with window controls and drag area.
   * Sets up the visual appearance and behavior of the title bar.
   */
  private void initialize() {
    this.configureTitleBar();
    // Left buttons macOS style
    HBox windowControls = this.createWindowControls();

    // For moving the window
    Region dragArea = this.createDragArea();
    this.setupDragBehavior(); // Need to handle ourselves, since undecorated / transparent stage
    // removes the default titlebar and behavior

    this.getChildren().addAll(windowControls, dragArea);
    this.isFullscreenOrMaximized.bind(this.stageController.getStage().fullScreenProperty()
        .or(this.stageController.getStage().maximizedProperty()));
  }

  /**
   * Configures the title bar's visual appearance.
   * Sets the background color, padding, height, and view order.
   */
  private void configureTitleBar() {
    this.setStyle(String.format("-fx-background-color: %s;", this.getBarBackgroundColor()));
    this.setPadding(InsetBuilder.symmetric(15, 10).build());
    this.setPrefHeight(TITLE_BAR_HEIGHT);
    this.setMaxHeight(TITLE_BAR_HEIGHT);
    this.setViewOrder(-1);
    this.setMaxWidth(Double.MAX_VALUE);
    StyleManager.growHorizontal(this);
  }

  /**
   * Creates the window control buttons (close, minimize, maximize).
   * Sets up macOS-style appearance and behavior for the buttons.
   *
   * @return An HBox containing the window control buttons
   */
  private HBox createWindowControls() {
    HBox windowControls = new HBox(WINDOW_CONTROLS_SPACING);
    windowControls.setPadding(InsetBuilder.create().right(8).build());

    // btns
    Button closeButton = this.createWindowControlButton("-color-fg-muted", BUTTON_COLORS[0],
        e -> this.stageController.getStage().close());
    Button minimizeButton = this.createWindowControlButton("-color-fg-muted", BUTTON_COLORS[1],
        e -> this.stageController.getStage().setIconified(true));
    Button maximizeButton = this.createWindowControlButton("-color-fg-muted", BUTTON_COLORS[2],
        e -> this.stageController.getStage()
            .setMaximized(!this.stageController.getStage().isMaximized()));

    // When the titlebar is hovered, show the buttons colors, like on mcaos
    this.setupWindowControlsHoverEffect(this, closeButton, minimizeButton,
        maximizeButton);
    windowControls.getChildren().addAll(closeButton, minimizeButton, maximizeButton);

    return windowControls;
  }

  /**
   * Creates a window control button with specified styles and action.
   *
   * @param baseColor  The default color for the button
   * @param hoverColor The color to display when hovering over the button
   * @param action     The action to perform when the button is clicked
   * @return A configured Button instance
   */
  private Button createWindowControlButton(
      String baseColor,
      String hoverColor,
      javafx.event.EventHandler<javafx.event.ActionEvent> action) {
    Button button = new Button();
    button.setShape(new Circle(BUTTON_RADIUS));
    button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
    button.setStyle(this.createButtonStyle(baseColor));
    button.setOpacity(BUTTON_OPACITY_NORMAL);
    button.setCursor(Cursor.HAND);
    button.setOnAction(action);

    this.setupButtonHoverEffects(button, hoverColor); // Sclae and fade effects

    return button;
  }

  /**
   * Sets up hover effects for window control buttons.
   * Creates scale and fade animations that trigger on mouse enter/exit.
   *
   * @param button     The button to apply hover effects to
   * @param hoverColor The color to display when hovering
   */
  private void setupButtonHoverEffects(Button button, String hoverColor) {
    button.setOnMouseEntered(e -> {
      button.setStyle(this.createButtonStyle(hoverColor));
      this.createButtonAnimation(button, false).play();
    });

    button.setOnMouseExited(e -> {
      this.createButtonAnimation(button, true).play();
    });
  }

  /**
   * Creates the CSS style string for a window control button.
   *
   * @param color The color to use for the button
   * @return A CSS style string
   */
  private String createButtonStyle(String color) {
    return String.format("""
        -fx-background-color: %s;
        -fx-background-radius: %f;
        -fx-border-radius: %f;
        -fx-border-width: 0;
        -fx-padding: 0;
        """, color, BUTTON_RADIUS, BUTTON_RADIUS);
  }

  /**
   * Creates the drag area region of the title bar.
   * This area allows the user to drag the window and double-click to maximize/restore.
   *
   * @return A configured Region used as the drag area
   */
  private Region createDragArea() {
    Region dragArea = new Region();
    HBox.setHgrow(dragArea, Priority.ALWAYS);
    dragArea.setCursor(Cursor.MOVE);
    dragArea.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        this.stageController.getStage().setMaximized(
            !this.stageController.getStage().isMaximized()
        );
      }
    });
    this.isFullscreenOrMaximized.addListener((observable, oldValue, newValue) -> {
      dragArea.setCursor(newValue ? Cursor.DEFAULT : Cursor.MOVE);
    });
    return dragArea;
  }

  /**
   * Sets up the window drag behavior for the title bar.
   * Allows the user to move the window by dragging the title bar.
   */
  private void setupDragBehavior() {
    this.setOnMousePressed(event -> {
      if (this.stageController.getStage().isFullScreen()
          || this.stageController.getStage().isMaximized()) {
        return;
      }
      this.offsetX = event.getSceneX();
      this.offsetY = event.getSceneY();
      this.createScaleTransition(SCALE_FACTOR, true).play();
    });

    this.setOnMouseReleased(event -> {
      if (this.stageController.getStage().isFullScreen()
          || this.stageController.getStage().isMaximized()) {
        return;
      }
      this.createScaleTransition(1.0, false).play();
    });

    this.setOnMouseDragged(event -> {
      if (this.stageController.getStage().isFullScreen()
          || this.stageController.getStage().isMaximized()) {
        return;
      }
      this.stageController.getStage().setX(event.getScreenX() - this.offsetX);
      this.stageController.getStage().setY(event.getScreenY() - this.offsetY);
    });
  }

  /**
   * Creates a scale transition animation for the window.
   * Used to provide visual feedback when dragging the window.
   *
   * @param targetScale The target scale to animate to
   * @param isPressed   Whether the mouse is being pressed or released
   * @return A configured ScaleTransition
   */
  private ScaleTransition createScaleTransition(double targetScale, boolean isPressed) {
    ScaleTransition scaleTransition = new ScaleTransition(ANIMATION_DURATION);
    scaleTransition.setNode(this.stageController.getStage().getScene().getRoot());
    scaleTransition.setFromX(isPressed ? 1 : SCALE_FACTOR);
    scaleTransition.setFromY(isPressed ? 1 : SCALE_FACTOR);
    scaleTransition.setToX(targetScale);
    scaleTransition.setToY(targetScale);
    scaleTransition.setInterpolator(isPressed ? Interpolator.EASE_IN : Interpolator.EASE_OUT);
    return scaleTransition;
  }

  /**
   * Creates an animation for window control buttons.
   * Combines scale and fade transitions
   *
   * @param button  The button to animate
   * @param reverse Whether to play the animation in reverse
   * @return A configured ParallelTransition
   */
  private ParallelTransition createButtonAnimation(Button button, boolean reverse) {
    ScaleTransition scaleTransition = new ScaleTransition(ANIMATION_DURATION);
    scaleTransition.setNode(button);
    scaleTransition.setFromX(reverse ? BUTTON_SCALE_FACTOR : 1);
    scaleTransition.setFromY(reverse ? BUTTON_SCALE_FACTOR : 1);
    scaleTransition.setToX(reverse ? 1 : BUTTON_SCALE_FACTOR);
    scaleTransition.setToY(reverse ? 1 : BUTTON_SCALE_FACTOR);

    FadeTransition fadeTransition = new FadeTransition(ANIMATION_DURATION);
    fadeTransition.setNode(button);
    fadeTransition.setFromValue(reverse ? BUTTON_OPACITY_HOVER : BUTTON_OPACITY_NORMAL);
    fadeTransition.setToValue(reverse ? BUTTON_OPACITY_NORMAL : BUTTON_OPACITY_HOVER);

    ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
    parallelTransition.setInterpolator(Interpolator.EASE_BOTH);
    return parallelTransition;
  }

  /**
   * Sets up hover effects for the window control area.
   * Changes button colors when hovering over the title bar, to mimic macos behavior.
   *
   * @param nodeToApply    The node to apply hover effects to
   * @param closeButton    The close button
   * @param minimizeButton The minimize button
   * @param maximizeButton The maximize button
   */
  private void setupWindowControlsHoverEffect(HBox nodeToApply, Button closeButton,
                                              Button minimizeButton, Button maximizeButton) {
    String property = "-fx-background-color";
    nodeToApply.setOnMouseEntered(e -> {
      Styles.appendStyle(closeButton, property, BUTTON_COLORS[0]);
      Styles.appendStyle(minimizeButton, property, BUTTON_COLORS[1]);
      Styles.appendStyle(maximizeButton, property, BUTTON_COLORS[2]);
    });

    nodeToApply.setOnMouseExited(e -> {
      Styles.appendStyle(closeButton, property, "-color-fg-muted;");
      Styles.appendStyle(minimizeButton, property, "-color-fg-muted;");
      Styles.appendStyle(maximizeButton, property, "-color-fg-muted;");
    });
  }

  /**
   * Gets the background color for the title bar.
   * Returns transparent for the main stage, or the default background color otherwise.
   *
   * @return The CSS color string for the title bar background
   */
  private String getBarBackgroundColor() {
    return this.stageController.isMainStage() ? "transparent" : "-color-bg-default";
  }
}