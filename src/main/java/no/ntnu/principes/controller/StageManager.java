package no.ntnu.principes.controller;

import java.util.HashMap;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Manages JavaFX Stage instances through their StageController wrappers.
 * Provides centralized access to named stages and coordinates their behavior.
 * Implemented as a singleton to ensure a single point of management for all application stages.
 */
public class StageManager {
  public static final String MAIN_STAGE_NAME = "main";
  private static final double DEFAULT_WINDOW_WIDTH = 1350;
  private static final double DEFAULT_WINDOW_HEIGHT = 840;

  private final HashMap<String, StageController> controllers = new HashMap<>();
  private final double windowWidth;
  private final double windowHeight;

  /**
   * Creates a new StageManager with specified window dimensions.
   *
   * @param width  Initial default width for new stages
   * @param height Initial default height for new stages
   */
  public StageManager(double width, double height) {
    this.windowWidth = width;
    this.windowHeight = height;
  }

  /**
   * Gets the singleton instance of StageManager.
   *
   * @return The StageManager instance with default dimensions
   */
  public static StageManager getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Gets a StageController by name, creating a new one if it doesn't exist.
   * Uses default dimensions and a new Stage.
   *
   * @param name The unique name for the controller
   * @return The existing or newly created StageController
   */
  public StageController getController(String name) {
    return this.getController(name, new Stage(), this.windowWidth, this.windowHeight, false);
  }

  /**
   * Checks if a StageController with the given name exists.
   *
   * @param name The name to check
   * @return true if a controller with that name exists, false otherwise
   */
  public boolean hasController(String name) {
    return this.controllers.containsKey(name);
  }

  /**
   * Gets a StageController by name with custom parameters, creating a new one if it doesn't exist.
   *
   * @param name      The unique name for the controller
   * @param stage     The JavaFX Stage to use
   * @param width     The width for the stage
   * @param height    The height for the stage
   * @param isUtility Whether this is a utility stage
   * @return The existing or newly created StageController
   */
  public StageController getController(String name, Stage stage, double width, double height,
                                       boolean isUtility) {
    if (!this.controllers.containsKey(name)) {
      stage.initStyle(StageStyle.TRANSPARENT);
      StageController controller = new StageController(name, stage, width, height, isUtility);
      controller.registerScreens();
      this.controllers.put(name, controller);
      return controller;
    }
    return this.controllers.get(name);
  }

  /**
   * Gets a StageController by name with custom dimensions, creating a new one if it doesn't exist.
   * Uses a new Stage.
   *
   * @param name   The unique name for the controller
   * @param width  The width for the stage
   * @param height The height for the stage
   * @return The existing or newly created StageController
   */
  public StageController getController(String name, double width, double height) {
    return this.getController(name, new Stage(), width, height, false);
  }

  /**
   * Gets a utility StageController by name, creating a new one if it doesn't exist.
   * Utility stages have different behavior than regular stages.
   *
   * @param name   The unique name for the controller
   * @param width  The width for the stage
   * @param height The height for the stage
   * @return The existing or newly created utility StageController
   */
  public StageController getUtilityController(String name, double width, double height) {
    return this.getController(name, new Stage(), width, height, true);
  }

  /**
   * Gets the main StageController, creating it if it doesn't exist.
   * The main stage has special handling for application-wide stage events:
   * - It closes all other stages when closed
   * - It hides/shows all other stages when hidden/shown
   *
   * @param stage The JavaFX Stage to use as the main stage
   * @return The main StageController
   */
  public StageController getMainController(Stage stage) {
    if (this.controllers.containsKey(MAIN_STAGE_NAME)) {
      return this.controllers.get(MAIN_STAGE_NAME);
    }
    stage.initStyle(StageStyle.TRANSPARENT);
    StageController controller =
        new StageController(MAIN_STAGE_NAME, stage, this.windowWidth, this.windowHeight);

    // Close all other stages when the main stage is closed
    stage.setOnCloseRequest(event -> {
      for (String name : this.controllers.keySet()) {
        if (!name.equals(MAIN_STAGE_NAME)) {
          this.removeController(name);
        }
      }
    });

    // Hide or show all stages when the main stage is hidden or shown
    stage.setOnHiding(event -> {
      for (StageController stageController : this.controllers.values()) {
        if (!stageController.getName().equals(MAIN_STAGE_NAME)) {
          stageController.getStage().hide();
        }
      }
    });
    stage.setOnShowing(event -> {
      for (StageController stageController : this.controllers.values()) {
        if (!stageController.getName().equals(MAIN_STAGE_NAME)) {
          stageController.getStage().show();
        }
      }
    });
    this.controllers.put(MAIN_STAGE_NAME, controller);
    controller.registerScreens();
    return controller;
  }

  /**
   * Removes a StageController by name, closing its stage and cleaning up resources.
   *
   * @param name The name of the controller to remove
   */
  public void removeController(String name) {
    StageController controller = this.controllers.get(name);
    if (controller != null) {
      controller.close();
      this.controllers.remove(name);
    }
  }

  /**
   * Private holder class for lazy singleton initialization.
   */
  private static class Holder {
    private static final StageManager INSTANCE =
        new StageManager(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
  }
}