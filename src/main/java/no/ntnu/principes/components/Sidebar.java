package no.ntnu.principes.components;

import atlantafx.base.theme.Styles;
import java.util.Map;
import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.StageController;
import no.ntnu.principes.controller.StageManager;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/**
 * Creates a floating sidebar with navigation buttons that positions itself relative to the main
 * application window.
 * The sidebar is implemented as a separate utility stage.
 * It displays navigation options for Home, Tasks, and Household screens,
 * along with a Settings and a Sign-Out button.
 */
@Slf4j
public class Sidebar extends BaseComponent {
  public static final double SIDEBAR_WIDTH = 300;
  private final ConfigValueRepository configValueRepository;
  private final StageController stageController;

  private VBox rootContainer;
  private VBox topContent;
  private Button homeButton;
  private Button tasksButton;
  private Button householdButton;
  private Button aboutButton;
  private Button settingsButton;
  private Button signOutButton;
  private ChangeListener<String> screenIdListener;
  private Stage ownerWindow;
  private Stage sidebarStage;
  private final ChangeListener<Number> updateSidebarPositionListener = (obs, old, newVal) -> {
    if (newVal != null) {
      this.updateSidebarPosition();
    }
  };

  /**
   * Creates a new Sidebar component linked to a parent screen and controlled by the specified stage
   * controller.
   *
   * @param parentScreen    The base screen that owns this sidebar
   * @param stageController The controller for the stage where the sidebar will be displayed
   */
  public Sidebar(BaseScreen parentScreen, StageController stageController) {
    super("sidebar", parentScreen, false);
    this.configValueRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
    this.stageController = stageController;
  }

  /**
   * Initializes a sidebar for the given parent screen.
   * If a sidebar already exists, returns the existing instance; otherwise creates a new one.
   *
   * @param parentScreen The base screen to attach the sidebar to
   * @return The initialized sidebar instance
   */
  public static Sidebar init(BaseScreen parentScreen) {
    StageController sidebarController =
        StageManager.getInstance().getUtilityController("sidebar", SIDEBAR_WIDTH, 400);
    if (sidebarController.getScreenController().getMainContainer().getChildren().isEmpty()) {
      sidebarController.getScreenController()
          .utility(new Sidebar(parentScreen, sidebarController));
    }
    return (Sidebar) sidebarController.getScreenController().getMainContainer()
        .getChildren()
        .getFirst();
  }

  @Override
  protected void initializeComponent() {
    // Initialize the separate stage for the sidebar
    this.ownerWindow =
        StageManager.getInstance().getController(StageManager.MAIN_STAGE_NAME).getStage();
    this.sidebarStage = this.stageController.getStage();
    if (!Objects.equals(this.sidebarStage.getOwner(), this.ownerWindow)) {
      this.sidebarStage.initOwner(this.ownerWindow);
      this.sidebarStage.show();
    }

    // Calculate initial height
    double targetHeight = this.ownerWindow.getHeight() - (2 * 40);
    this.stageController.setWindowHeight(targetHeight);
    this.sidebarStage.setHeight(targetHeight);

    // Initialize root container
    this.rootContainer = new VBox(10);
    this.rootContainer.setPadding(InsetBuilder.symmetric(20, 40).build());
    this.rootContainer.setStyle(
        "-fx-background-color: -color-accent-1;"
            + "-fx-background-radius: 15px; -fx-border-radius: 15px;");
    StyleManager.apply(this.rootContainer, Styles.ROUNDED);

    // Initialize top content
    this.topContent = new VBox(10);
    VBox.setVgrow(this.topContent, Priority.NEVER);

    // Initialize components
    this.initializeTitle();
    this.initializeButtons();
    this.initializeSettingsButton();
    this.initializeSignOutButton();

    // Set up component hierarchy
    this.rootContainer.getChildren()
        .addAll(this.topContent, this.settingsButton, this.signOutButton);
    VBox.setMargin(this.signOutButton, new Insets(0, 0, 20, 0));

    // Push sign out button and settings to bottom
    VBox spacer = new VBox();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    this.rootContainer.getChildren().add(1, spacer);

    this.sidebarStage.getScene().setRoot(this.rootContainer);
    this.sidebarStage.getScene().setFill(null);

    // Initialize with zero opacity for animation
    this.rootContainer.setOpacity(0);

    this.bindSizeAndPosition();

    // Not logged in alert banner
    VBox alertBanner = new VBox();
    alertBanner.setPadding(InsetBuilder.symmetric(10, 5).build());
    alertBanner.setStyle(
        "-fx-background-color: -color-danger-muted; -fx-text-fill: -color-danger-fg;");
    Text alertText = new Text("You are not logged in.", Text.TextType.HELPER);
    alertBanner.getChildren().add(alertText);
    alertBanner.visibleProperty().bind(Bindings.createBooleanBinding(
        () -> !Auth.getInstance().isAuthenticated(),
        Auth.getInstance().isAuthenticatedProperty()));
    this.topContent.getChildren().add(alertBanner);
  }

  /**
   * Binds the sidebar's size and position to the main window.
   * Ensures the sidebar stays properly positioned when the main window moves or resizes.
   */
  private void bindSizeAndPosition() {
    // Bind window position
    this.ownerWindow.xProperty().addListener(this.updateSidebarPositionListener);
    this.ownerWindow.yProperty().addListener(this.updateSidebarPositionListener);

    // Bind height to main window with dynamic updates
    this.ownerWindow.heightProperty().addListener((obs, old, newHeight) -> {
      int multiplier =
          this.ownerWindow.isMaximized() || this.ownerWindow.isFullScreen() ? 1 : 2;
      double newTargetHeight = newHeight.doubleValue() - (multiplier * 40);
      this.sidebarStage.setHeight(newTargetHeight);
      this.rootContainer.setMinHeight(newTargetHeight);
      this.rootContainer.setPrefHeight(newTargetHeight);
      this.rootContainer.setMaxHeight(newTargetHeight);
    });

    this.ownerWindow.fullScreenProperty().addListener((obs, old, isFullScreen) -> {
      log.debug("Sidebar full screen changed to: {}", isFullScreen);
      this.updateSidebarPosition();
    });

    this.ownerWindow.maximizedProperty().addListener((obs, old, isMaximized) -> {
      log.debug("Sidebar maximized changed to: {}", isMaximized);
      this.updateSidebarPosition();
    });
  }

  /**
   * Creates and adds the title component to the sidebar.
   * The title displays the household name retrieved from configuration.
   */
  private void initializeTitle() {
    Text title = new Text("", Text.TextType.SECTION_HEADER);
    title.getStyleClass().add(Styles.TEXT_BOLDER);
    title.setStyle("-fx-font-weight: 800");
    new ConfigValueBinder(this.configValueRepository).bindString("householdName", "Not Set",
        title.textProperty());
    this.topContent.getChildren().add(title);
  }

  /**
   * Creates and configures the navigation buttons for the sidebar.
   * Sets up Home, Tasks, and Household navigation buttons with appropriate styling.
   */
  private void initializeButtons() {
    // Initialize buttons
    this.homeButton = new Button("Home", Button.ButtonType.NAVIGATION);
    this.tasksButton = new Button("Tasks", Button.ButtonType.NAVIGATION);
    this.householdButton = new Button("Household", Button.ButtonType.NAVIGATION);
    this.aboutButton = new Button("About", Button.ButtonType.NAVIGATION);

    // Configure buttons
    StyleManager.growHorizontal(
        this.homeButton,
        this.tasksButton,
        this.householdButton,
        this.aboutButton
    );

    // Set button padding
    Insets buttonPadding = InsetBuilder.uniform(2).build();
    this.homeButton.setPadding(buttonPadding);
    this.tasksButton.setPadding(buttonPadding);
    this.householdButton.setPadding(buttonPadding);
    this.aboutButton.setPadding(buttonPadding);

    // Set icons
    FontIcon homeIcon = new FontIcon(Material2AL.HOME);
    FontIcon tasksIcon = new FontIcon(Material2AL.LIST_ALT);
    FontIcon householdIcon = new FontIcon(Material2AL.BUILD);
    FontIcon aboutIcon = new FontIcon(Material2AL.INFO);
    homeIcon.getStyleClass().add("sidebar-button-icon");
    tasksIcon.getStyleClass().add("sidebar-button-icon");
    householdIcon.getStyleClass().add("sidebar-button-icon");
    aboutIcon.getStyleClass().add("sidebar-button-icon");

    this.homeButton.setGraphic(homeIcon);
    this.tasksButton.setGraphic(tasksIcon);
    this.householdButton.setGraphic(householdIcon);
    this.aboutButton.setGraphic(aboutIcon);

    // Add buttons to container
    VBox buttons = new VBox(10);
    buttons.getChildren().addAll(
        this.homeButton,
        this.tasksButton,
        this.householdButton,
        this.aboutButton
    );
    VBox.setMargin(buttons, InsetBuilder.create().top(20).build());
    this.topContent.getChildren().add(buttons);
  }

  private void initializeSettingsButton() {
    this.settingsButton = new Button("Settings", Button.ButtonType.OUTLINED);
    StyleManager.growHorizontal(this.settingsButton);
    this.settingsButton.setPadding(InsetBuilder.uniform(2).build());
    this.settingsButton.setAlignment(Pos.CENTER);
    FontIcon settingsIcon = new FontIcon(Material2MZ.SETTINGS);
    this.settingsButton.setGraphic(settingsIcon);
    this.settingsButton.getStyleClass().add("sidebar-action");

  }

  /**
   * Creates and configures the sign-out button for the sidebar.
   */
  private void initializeSignOutButton() {
    this.signOutButton = new Button("Sign Out", Button.ButtonType.OUTLINED);
    StyleManager.growHorizontal(this.signOutButton);
    this.signOutButton.setPadding(InsetBuilder.uniform(2).build());
    this.signOutButton.setAlignment(Pos.CENTER);
    FontIcon signOutIcon = new FontIcon(Material2AL.LOG_OUT);
    this.signOutButton.setGraphic(signOutIcon);

    this.signOutButton.getStyleClass().add("sidebar-action");
  }

  /**
   * Updates the sidebar's position relative to the main window.
   * Adjusts positioning based on whether the main window is maximized or in fullscreen mode.
   */
  private void updateSidebarPosition() {
    if (this.ownerWindow == null) {
      return;
    }

    double mainWindowX = this.ownerWindow.getX();
    double mainWindowY = this.ownerWindow.getY();

    int offsetX = (this.ownerWindow.isMaximized()
        || this.ownerWindow.isFullScreen()) ? 0 : 40;
    int offsetY = 40;

    double sidebarX = mainWindowX - offsetX;
    double sidebarY = mainWindowY + offsetY;

    this.sidebarStage.setX(sidebarX);
    this.sidebarStage.setY(sidebarY);
  }

  /**
   * Plays an animation to fade in and slide the sidebar into view.
   */
  private void animateIn() {
    FadeTransition ft = new FadeTransition();
    TranslateTransition tt = new TranslateTransition();
    ft.setNode(this.rootContainer);
    tt.setNode(this.rootContainer);
    ft.setFromValue(0);
    ft.setToValue(1);
    ft.setDuration(javafx.util.Duration.millis(200));
    tt.setFromX(40);
    tt.setToX(0);
    tt.setDuration(javafx.util.Duration.millis(200));
    ParallelTransition pt = new ParallelTransition(ft, tt);
    pt.play();
  }

  /**
   * Plays an animation to fade out and slide the sidebar out of view.
   * Hides the sidebar stage when the animation completes.
   */
  private void animateOut() {
    FadeTransition ft = new FadeTransition();
    TranslateTransition tt = new TranslateTransition();
    ft.setNode(this.rootContainer);
    tt.setNode(this.rootContainer);
    ft.setFromValue(1);
    ft.setToValue(0);
    ft.setDuration(javafx.util.Duration.millis(200));
    tt.setFromX(0);
    tt.setToX(40);
    tt.setDuration(javafx.util.Duration.millis(200));
    ParallelTransition pt = new ParallelTransition(ft, tt);
    pt.setOnFinished(e -> this.sidebarStage.hide());
    pt.play();
  }

  /**
   * Sets up event handlers for the sidebar buttons.
   * Configures navigation actions for each button.
   */
  @Override
  protected void setupEventHandlers() {
    // Setup button click handlers
    this.homeButton.setOnAction(e -> NavigationService.navigate("main"));
    this.tasksButton.setOnAction(e -> NavigationService.navigate("tasks"));
    this.householdButton.setOnAction(
        e -> NavigationService.navigate("household", Map.of("tabIndex", 0)));
    this.aboutButton.setOnAction(e -> NavigationService.navigate("about"));
    this.settingsButton.setOnAction(
        e -> NavigationService.navigate("household", Map.of("tabIndex", 1)));
    this.signOutButton.setOnMouseReleased(e -> {
      NavigationService.clear(
          StageManager.getInstance()
              .getController(StageManager.MAIN_STAGE_NAME)
              .getStageId()
      );
      NavigationService.navigate("selectProfile");
      Thread thread = new Thread(() -> {
        try {
          Thread.sleep(400);
          Platform.runLater(() -> {
            Auth.getInstance().deauthenticate();
          });
        } catch (InterruptedException ex) {
          log.error("Error while sleeping", ex);
        }
      });
      thread.start();

    });

    // Setup screen change listener
    this.screenIdListener = (obs, oldVal, newVal) -> {
      log.debug("Screen ID changed to: {} from {}", newVal, oldVal);
      this.updateButtonStates(newVal);
    };
  }

  /**
   * Updates the visual state of the navigation buttons based on the current screen.
   * Highlights the button corresponding to the currently active screen.
   *
   * @param screenId The ID of the currently active screen
   */
  private void updateButtonStates(String screenId) {
    // Set active button based on screen
    if (screenId == null) {
      return;
    }
    switch (screenId) {
      case "main" -> {
        StyleManager.apply(this.homeButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
        this.homeButton.requestFocus();
      }
      case "tasks", "taskDetails" -> {
        StyleManager.apply(this.tasksButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
        this.tasksButton.requestFocus();
      }
      case "household" -> {
        StyleManager.apply(this.householdButton,
            StyleManager.ButtonStyle.PseudoClass.SELECTED);
        this.householdButton.requestFocus();
      }
      case "about" -> {
        StyleManager.apply(this.aboutButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
        this.aboutButton.requestFocus();
      }
      default -> {
        log.warn("Screen ID {} not recognized", screenId);
        Platform.runLater(this::animateOut);
      }
    }
    // Reset all other buttons
    if (!screenId.equals("main")) {
      StyleManager.unapply(this.homeButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
    }
    if (!screenId.equals("tasks") && !screenId.equals("taskDetails")) {
      StyleManager.unapply(this.tasksButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
    }
    if (!screenId.equals("household")) {
      StyleManager.unapply(this.householdButton,
          StyleManager.ButtonStyle.PseudoClass.SELECTED);
    }
    if (!screenId.equals("about")) {
      StyleManager.unapply(this.aboutButton, StyleManager.ButtonStyle.PseudoClass.SELECTED);
    }
  }

  @Override
  protected void onMount() {
    this.ownerWindow =
        StageManager.getInstance().getController(StageManager.MAIN_STAGE_NAME).getStage();
    this.sidebarStage = this.stageController.getStage();
    this.sidebarStage.setHeight(this.ownerWindow.getHeight() - (2 * 40));
    if (this.sidebarStage.getOwner() == null) {
      this.sidebarStage.initOwner(this.ownerWindow);
    }
    this.sidebarStage.show();
    this.bindSizeAndPosition();
    this.updateSidebarPosition();
    var screenController =
        StageManager.getInstance().getController(StageManager.MAIN_STAGE_NAME)
            .getScreenController();
    screenController.getCurrentScreenId().addListener(this.screenIdListener);
    this.updateButtonStates(screenController.getCurrentScreenId().get());

    Platform.runLater(() -> {
      this.updateSidebarPosition();
      this.sidebarStage.show();
      if (this.rootContainer.getOpacity() == 0) {
        this.animateIn();
      }
    });
  }

  @Override
  protected void onUnmount() {
    StageManager.getInstance().getController(StageManager.MAIN_STAGE_NAME).getScreenController()
        .getCurrentScreenId()
        .removeListener(this.screenIdListener);
    Platform.runLater(this::animateOut);
  }

  @Override
  protected void onDestroy() {
    Platform.runLater(() -> this.sidebarStage.close());
  }
}
