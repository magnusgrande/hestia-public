package no.ntnu.principes.view;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.controller.screen.ScreenContext;
import no.ntnu.principes.controller.screen.ScreenController;

/**
 * Base class for all application screens providing lifecycle management and component handling.
 * Extends HBox to serve as a container for screen content and manages screen initialization,
 * navigation events, and component lifecycle.
 */
@Slf4j
public abstract class BaseScreen extends HBox {
  protected static final double CONTENT_WIDTH = 468;
  protected static final double RIGHT_BAR_WIDTH = 500;

  @Getter
  protected final ScreenController controller;
  @Getter
  private final String screenId;
  private boolean initialized = false;

  private final List<BaseComponent> _registeredComponents = new ArrayList<>();

  /**
   * Creates a new screen with the specified controller and screen ID.
   * Initializes the base layout for the screen.
   *
   * @param controller The screen controller managing this screen
   * @param screenId   Unique identifier for this screen
   */
  public BaseScreen(ScreenController controller, String screenId) {
    log.debug("Creating BaseScreen: {} (id: {})", this.getClass().getSimpleName(), screenId);
    this.screenId = screenId;
    this.controller = controller;

    this.initializeBaseLayout();
  }

  /**
   * Sets up the base layout properties for the screen.
   * Configures spacing and width constraints.
   */
  private void initializeBaseLayout() {
    log.debug("Initializing base layout for screen: {}", this.screenId);
    this.setSpacing(0);
    this.setMaxWidth(Double.MAX_VALUE);
  }

  /**
   * Initializes screen-specific content and behavior.
   * Called once before the first navigation to this screen.
   * Subclasses should override this method to set up their specific UI components.
   */
  protected void initializeScreen() {
    log.debug("Default initializeScreen called for: {}", this.screenId);
    // Default empty implementation
  }

  /**
   * Handles the screen initialization process.
   * Marks the screen as initialized after initialization completes.
   */
  private void handleInitializeScreen() {
    this.initializeScreen();
    this.initialized = true;
  }

  /**
   * Retrieves the context for this screen from the controller.
   * The context contains parameters and state for the screen.
   *
   * @return The screen context for this screen
   */
  protected ScreenContext getContext() {
    log.debug("Getting context for screen: {}", this.screenId);
    ScreenContext context = this.getController().getContext(this.screenId);
    log.debug("Context parameters for {}: {}", this.screenId, context.toString());
    return context;
  }

  /**
   * Called when navigation to this screen occurs.
   * Subclasses should override to perform actions when the screen becomes active.
   */
  protected void onNavigatedTo() {
    log.warn("Default onNavigatedTo called for: {}", this.screenId);
    // Default empty implementation
  }

  /**
   * Called when navigation away from this screen occurs.
   * Subclasses should override to perform cleanup when the screen becomes inactive.
   */
  protected void onNavigatedFrom() {
    log.warn("Default onNavigatedFrom called for: {}", this.screenId);
    // Default empty implementation
  }

  /**
   * Performs final cleanup when the screen is no longer needed.
   * Subclasses should override to release resources.
   */
  protected void cleanup() {
    log.warn("Default cleanup called for: {}", this.screenId);
    // Default empty implementation
  }

  /**
   * Handles the navigation to this screen.
   * Initializes the screen if needed, mounts components, and calls onNavigatedTo.
   * This method is called by the screen controller and should not be overridden.
   */
  public final void handleNavigatedTo() {
    log.debug("Screen {} handling navigation TO", this.screenId);
    try {
      // Initialize screen if not already done
      if (!this.initialized) {
        this.handleInitializeScreen();
      }
      this.mountComponents();
      // Call onNavigatedTo for screen
      this.onNavigatedTo();
      log.debug("Successfully handled navigation TO screen: {}", this.screenId);
    } catch (Exception e) {
      log.debug("Error in handleNavigatedTo for screen {}: {}", this.screenId,
          e.getMessage());
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Handles the navigation away from this screen.
   * Unmounts components and calls onNavigatedFrom.
   * This method is called by the screen controller and should not be overridden.
   */
  public final void handleNavigatedFrom() {
    log.debug("Screen {} handling navigation FROM", this.screenId);
    try {
      this.unmountComponents();
      this.onNavigatedFrom();
      log.debug("Successfully handled navigation FROM screen: {}", this.screenId);
    } catch (Exception e) {
      log.debug("Error in handleNavigatedFrom for screen {}: {}", this.screenId,
          e.getMessage());
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Handles final cleanup of this screen.
   * This method is called by the screen controller and should not be overridden.
   */
  public final void handleCleanup() {
    log.info("Screen {} handling cleanup", this.screenId);
    try {
      this.cleanup();
      log.debug("Successfully cleaned up screen: {}", this.screenId);
    } catch (Exception e) {
      log.debug("Error in handleCleanup for screen {}: {}", this.screenId, e.getMessage());
      log.error(e.getMessage(), e);
    }
  }

  /**
   * Registers a component with this screen.
   * Registered components will be mounted and unmounted with the screen.
   *
   * @param component The component to register
   */
  public void registerComponent(BaseComponent component) {
    log.warn("Registering component {} for screen: {}", component.getComponentId(),
        this.screenId);
    this._registeredComponents.add(component);
  }

  /**
   * Mounts all registered components.
   * Called during navigation to this screen.
   */
  private void mountComponents() {
    Platform.runLater(() -> {
      log.debug("Mounting {} components for screen: {}", this._registeredComponents.size(),
          this.screenId);
      for (BaseComponent component : this._registeredComponents) {
        component.mount();
      }
    });
  }

  /**
   * Unmounts all registered components.
   * Called during navigation away from this screen.
   */
  private void unmountComponents() {
    Platform.runLater(() -> {
      log.debug("Unmounting {} components for screen: {}", this._registeredComponents.size(),
          this.screenId);
      for (BaseComponent component : this._registeredComponents) {
        component.unmount();
      }
    });
  }

  /**
   * Checks if this screen has been initialized.
   *
   * @return True if the screen has been initialized, false otherwise
   */
  protected boolean isInitialized() {
    return this.initialized;
  }

  @Override
  public int hashCode() {
    return this.screenId.hashCode();
  }
}