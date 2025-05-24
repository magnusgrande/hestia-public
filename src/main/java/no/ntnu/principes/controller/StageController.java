package no.ntnu.principes.controller;

import static no.ntnu.principes.controller.StageManager.MAIN_STAGE_NAME;

import java.util.Objects;
import java.util.UUID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.DevTaskGenerator;
import no.ntnu.principes.util.ScreenRegistry;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * Controls a JavaFX Stage with screen management capabilities.
 * Handles stage configuration, theme switching, event handlers, and global keyboard shortcuts.
 * Each StageController manages one Stage and its associated ScreenController.
 */
@Slf4j
public class StageController {
  private static final long THEME_TOGGLE_DELAY = 1000;
  private final ConfigValueRepository configValueRepository =
      DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
  @Getter
  private final UUID stageId;
  @Getter
  @Setter
  private Stage stage;
  @Getter
  private ScreenController screenController;
  @Getter
  private double windowWidth;
  @Getter
  private double windowHeight;
  @Getter
  @Setter
  private Scene scene;
  @Getter
  private String name;
  @Getter
  private boolean isUtility = false;

  private final BooleanProperty isDarkMode = new SimpleBooleanProperty(false);


  private long lastThemeToggleTime = 0;

  /**
   * Creates a new StageController with a new Stage.
   *
   * @param name   The name used to identify this controller
   * @param width  The initial width of the stage
   * @param height The initial height of the stage
   */
  public StageController(String name, double width, double height) {
    this.windowWidth = width;
    this.windowHeight = height;
    this.stage = new Stage();
    this.name = name;
    this.initializeScene();
    this.setupStage(name);
    this.applyTheme(null);
    this.stageId = UUID.randomUUID();
  }

  /**
   * Creates a new StageController with an existing Stage.
   *
   * @param name   The name used to identify this controller
   * @param stage  The JavaFX Stage to be managed
   * @param width  The initial width of the stage
   * @param height The initial height of the stage
   */
  public StageController(String name, Stage stage, double width, double height) {
    this.windowWidth = width;
    this.windowHeight = height;
    this.stage = stage;
    this.name = name;
    this.initializeScene();
    this.setupStage(name);
    this.applyTheme(null);
    this.stageId = UUID.randomUUID();
  }

  /**
   * Creates a new StageController with an existing Stage and utility flag.
   *
   * @param name      The name used to identify this controller
   * @param stage     The JavaFX Stage to be managed
   * @param width     The initial width of the stage
   * @param height    The initial height of the stage
   * @param isUtility Whether this stage is a utility stage (affects behavior)
   */
  public StageController(String name, Stage stage, double width, double height,
                         boolean isUtility) {
    this.windowWidth = width;
    this.windowHeight = height;
    this.stage = stage;
    this.name = name;
    this.isUtility = isUtility;
    this.initializeScene();
    this.setupStage(name);
    this.applyTheme(null);
    this.stageId = UUID.randomUUID();
  }

  /**
   * Applies the current theme (light or dark) to the application.
   * The theme preference is stored in user preferences.
   */
  private void applyTheme(Boolean isDarkMode) {
    if (isDarkMode == null) {
      isDarkMode =
          this.configValueRepository.getValueOrDefault("settings.darkmode", false)
              .getValue().getBooleanValue();
    }
    String css = Objects.requireNonNull(this.getClass()
            .getResource(isDarkMode ? "/no/ntnu/principes/css/primer-dark.css" :
                "/no/ntnu/principes/css/primer-light.css"))
        .toExternalForm();
    log.debug("Applying theme: {}", isDarkMode ? "dark" : "light");
    StyleManager.getThemeProvider().setTheme(isDarkMode ? "dark" : "light");
    Application.setUserAgentStylesheet(css);
  }

  /**
   * Initializes the Scene with a ScreenController and default styling.
   * Sets up global key listeners and event handlers.
   */
  private void initializeScene() {
    ConfigValueBinder binder = new ConfigValueBinder(this.configValueRepository);
    binder.bindBoolean("settings.darkmode", false, this.isDarkMode);
    this.isDarkMode.addListener((observable, oldValue, newValue) -> {
      applyTheme(newValue);
      log.debug("Applying theme: {}", newValue);
    });
    this.screenController = new ScreenController(this);
    this.scene =
        new Scene(this.screenController.getMainContainer(), this.windowWidth,
            this.windowHeight);
    this.scene.getStylesheets()
        .addAll(
            Objects.requireNonNull(
                getClass().getResource("/no/ntnu/principes/css/base.css")
            ).toExternalForm()
        );
    this.scene.getRoot().setStyle(
        "-fx-background-radius: 15px;-fx-border-radius: 15px;"
            + "-fx-background-color: -color-bg-default;");
    this.scene.setFill(null);
    this.setupGlobalKeyListeners();
    this.initializeEventHandlers();
  }

  /**
   * Registers all available screens with the ScreenController.
   * Uses {@link ScreenRegistry} to automatically find and register screens.
   */
  public void registerScreens() {
    // Automatically register all screens
    ScreenRegistry.registerScreens(this.screenController);
  }

  /**
   * Sets up the Stage with the scene and application icon.
   *
   * @param name The name of the stage (not used in current implementation)
   */
  private void setupStage(String name) {
    this.stage.setScene(this.scene);
    this.stage.getIcons().add(
        new Image(
            Objects.requireNonNull(
                getClass().getResource("/no/ntnu/principes/images/hestia.png")
            ).toExternalForm()
        )
    );
    this.name = name;
  }

  /**
   * Sets up keyboard shortcuts for the application.
   * {@code Ctrl+D} toggles between light and dark themes with seizure protection.
   * {@code Ctrl+K} opens the debug window.
   */
  private void setupGlobalKeyListeners() {
    this.scene.setOnKeyPressed(e -> {
      if (e.isControlDown() && e.getCode().toString().equals("D") && false) { // Never run
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastThemeToggleTime > THEME_TOGGLE_DELAY) {
          this.configValueRepository.setConfigValue("settings.darkmode", !this.isDarkMode.get());
          lastThemeToggleTime = currentTime;
        } else {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Not so fast!");
          alert.setHeaderText("Seizure warning!");
          alert.setContentText(
              "To avoid triggering seizures, you can only toggle the theme again in "
                  + (THEME_TOGGLE_DELAY - (currentTime - lastThemeToggleTime)) + "ms.");
          alert.show();
        }
      } else if (e.isControlDown() && e.getCode().toString().equals("K")) {
        StageController window = StageManager.getInstance()
            .getController("debugWindow", 800, 1000);
        window.initializeEventHandlers();
        window.stage.show();
        NavigationService.openModal("debugOverlay", "stageController");
      } else if (e.isControlDown() && e.isShiftDown() && e.getCode().toString().equals("T")) {
        Platform.runLater(DevTaskGenerator::generateTasks);
      }
    });
  }

  /**
   * Initializes event handlers for this stage.
   * Only sets up event handlers for the main stage.
   */
  private void initializeEventHandlers() {
    if (!this.isMainStage()) {
      return;
    }
    PrincipesEventBus.getInstance()
        .subscribe(CloseModalEvent.class, this::onDebugOverlayModalClosed);
  }

  /**
   * Removes event handlers for this stage.
   * Only removes event handlers for the main stage.
   */
  private void tearDownEventHandlers() {
    if (!this.isMainStage()) {
      return;
    }
    PrincipesEventBus.getInstance()
        .unsubscribe(CloseModalEvent.class, this::onDebugOverlayModalClosed);
  }

  /**
   * Handles the debug overlay modal closed event.
   * If the callback ID matches "stageController", removes the debug window.
   *
   * @param event The CloseModalEvent that occurred
   */
  private void onDebugOverlayModalClosed(CloseModalEvent event) {
    if (event.getData().getCallbackId().equals("stageController")) {
      System.out.printf("Debug overlay closed with status: %s, success: %s and data: %s%n",
          event.getData().getStatus(), event.getData().isSuccess(),
          event.getData().getResult());
      StageManager.getInstance().removeController("debugWindow");
    }
  }

  /**
   * Sets the window height and updates the stage accordingly.
   *
   * @param windowHeight The new window height
   */
  public void setWindowHeight(double windowHeight) {
    this.windowHeight = windowHeight;
    this.stage.setHeight(this.windowHeight);
    this.stage.getScene().getWindow().setWidth(this.windowWidth);
  }

  /**
   * Sets the window width and updates the stage accordingly.
   *
   * @param windowWidth The new window width
   */
  public void setWindowWidth(double windowWidth) {
    this.windowWidth = windowWidth;
    this.stage.setWidth(this.windowWidth);
    this.stage.getScene().getWindow().setWidth(this.windowWidth);
  }

  /**
   * Checks if this is the main stage.
   *
   * @return true if this is the main stage, false otherwise
   */
  public boolean isMainStage() {
    return this.name.equals(MAIN_STAGE_NAME);
  }

  /**
   * Closes the stage, its screen controller, and tears down event handlers.
   * Performs cleanup operations for proper resource management.
   */
  public void close() {
    this.getStage().close();
    this.screenController.close();
    this.tearDownEventHandlers();
  }
}