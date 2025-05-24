package no.ntnu.principes.components;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.view.BaseScreen;

/**
 * The {@code BaseComponent} class defines a foundation for reusable UI components.
 * It provides lifecycle management methods including initialization, mounting,
 * unmounting, and destruction. Components are tied to a parent screen and can register themselves
 * with it, unless explicitly disabled.
 *
 * <p>This class is designed to be extended by specific components that define custom behavior
 * during the different lifecycle stages.</p>
 */
@Slf4j
public abstract class BaseComponent extends HBox {
  @Getter
  private final String componentId;
  protected final BaseScreen parentScreen;
  private boolean initialized = false;
  private boolean destroyed = false;

  /**
   * Constructs and initializes a BaseComponent with a specified ID and parent screen.
   * Registers the component with the parent screen upon creation.
   *
   * @param componentId  A unique identifier for this component. Must not be null.
   * @param parentScreen The parent screen to which this component belongs. Must not be null.
   * @throws NullPointerException If either {@code componentId} or {@code parentScreen} is null.
   */
  public BaseComponent(String componentId, BaseScreen parentScreen) {
    this(componentId, parentScreen, true);
  }

  /**
   * Constructs a BaseComponent and optionally registers it with its parent screen.
   *
   * @param componentId        The unique identifier for this component, used for identification
   *                           purposes.
   * @param parentScreen       The parent screen to which this component belongs. Must not be null.
   * @param registerWithParent If true, the component will be automatically registered with the
   *                           provided parent screen.
   */
  public BaseComponent(String componentId, BaseScreen parentScreen, boolean registerWithParent) {
    log.debug("Creating BaseComponent: {} (id: {})", this.getClass().getSimpleName(),
        componentId);
    this.componentId = componentId;
    this.parentScreen = parentScreen;
    // Register with the parent
    if (registerWithParent) {
      this.parentScreen.registerComponent(this);
    }
    Cleaner.create().register(this, new CleaningAction(this));
  }


  /**
   * Prepares the component for use by performing setup tasks such as initialization and
   * event handler assignment, ensuring the component is initialized only once.
   *
   * <p>Subclasses should override {@link #initializeComponent()}
   * and {@link #setupEventHandlers()} to provide specific initialization logic.</p>
   *
   * @throws RuntimeException if an exception occurs during initialization.
   */
  private void initialize() {
    if (this.initialized) {
      log.warn("Component {} already initialized", this.componentId);
      return;
    }

    try {
      this.initializeComponent();
      this.setupEventHandlers();
      this.initialized = true;
      log.debug("Successfully initialized component: {}", this.componentId);
    } catch (Exception e) {
      log.error("Error initializing component {}: {}", this.componentId, e.getMessage());
      throw new RuntimeException("Failed to initialize component", e);
    }
  }

  /**
   * Prepares and configures the component for use. This method is intended to
   * handle any required setup of the component layout, structure, bindings, et.c..
   *
   * <p>Must be implemented by subclasses to define the initialization logic
   * for their specific components.</p>
   */
  protected abstract void initializeComponent();

  /**
   * Sets up event handlers for the component. This method is intended to be overridden by
   * subclasses to define specific event handling logic.
   *
   * <p>By default, this method does nothing.</p>
   */
  protected void setupEventHandlers() {
    log.debug("No event handlers set up for component: {}", this.componentId);
  }

  /**
   * Executed when the component or object is initialized and added to its parent or context.
   *
   * <p>This method should be overridden by subclasses to define specific mount behavior.</p>
   *
   * <p>This method is part of the component lifecycle and should not be called directly.</p>
   */
  protected abstract void onMount();

  /**
   * Executed when the component or object is removed from its parent or context.
   *
   * <p>This method should be overridden by subclasses to define specific unmount behavior.</p>
   *
   * <p>This method is part of the component lifecycle and should not be called directly.</p>
   */
  protected abstract void onUnmount();

  /**
   * Executed when the component or object is destroyed.
   *
   * <p>This method should be overridden by subclasses to define specific destroy behavior.</p>
   *
   * <p>This method is part of the component lifecycle and should not be called directly.</p>
   */
  protected abstract void onDestroy();

  /**
   * Mounts the component, if not already mounted or destroyed.
   *
   * <p>This method initializes the component if it hasn't been initialized yet,
   * and then calls the {@link #onMount()} method to perform any additional setup.
   * This should be called by the parent screens lifecycle methods</p>
   */
  public final void mount() {
    if (this.destroyed) {
      log.warn("Attempting to mount destroyed component: {}", this.componentId);
      return;
    }

    if (!this.initialized) {
      this.initialize();
    }

    try {
      this.onMount();
      log.debug("Successfully mounted component: {}", this.componentId);
    } catch (Exception e) {
      log.error("Error mounting component {}: {}", this.componentId, e.getMessage());
      throw new RuntimeException("Failed to mount component", e);
    }
  }

  /**
   * Safely unmounts the component by performing cleanup or teardown operations.
   * Ensures the component does not proceed if it has not been initialized or has already been
   * destroyed.
   *
   * <p>This method should be called by the parent screens lifecycle methods.</p>
   */
  public final void unmount() {
    if (!this.initialized || this.destroyed) {
      return;
    }

    try {
      this.onUnmount();
      log.debug("Successfully unmounted component: {}", this.componentId);
    } catch (Exception e) {
      log.error("Error unmounting component {}: {}", this.componentId, e.getMessage());
    }
  }

  /**
   * Destroys the component, releasing any resources and performing cleanup operations.
   * This method should be called when the component is no longer needed.
   *
   * <p>Once destroyed, the component cannot be mounted or used again.</p>
   */
  public final void destroy() {
    if (this.destroyed) {
      return;
    }

    try {
      this.onDestroy();
      this.destroyed = true;
      log.debug("Successfully destroyed component: {}", this.componentId);
    } catch (Exception e) {
      log.error("Error destroying component {}: {}", this.componentId, e.getMessage());
    }
  }

  /**
   * Checks whether the object has been initialized.
   *
   * @return {@code true} if the object has been initialized; {@code false} otherwise.
   */
  protected boolean isInitialized() {
    return this.initialized;
  }

  /**
   * Checks whether the object has been marked as destroyed.
   *
   * @return <code>true</code> if the object is in a destroyed state; <code>false</code> otherwise.
   */
  protected boolean isDestroyed() {
    return this.destroyed;
  }

  @Override
  public int hashCode() {
    return this.componentId.hashCode();
  }

  /**
   * Handles the cleanup of a {@link BaseComponent} by invoking its destroy method.
   * The associated component is retrieved via a {@link WeakReference}, ensuring that
   * the cleanup action does not prevent garbage collection of the component.
   *
   * <p>If the component has already been garbage collected, no action will be performed.
   * Any exceptions thrown during the cleanup process are logged but not rethrown.</p>
   */
  @Slf4j
  private static class CleaningAction implements Runnable {
    private final WeakReference<BaseComponent> componentRef;

    /**
     * Initializes the cleanup action tied to a specified {@link BaseComponent}.
     * The action ensures that the component can be safely destroyed when executed,
     * while using a {@link WeakReference} to avoid preventing garbage collection of the component.
     *
     * @param component the {@link BaseComponent} to be associated with this cleanup action.
     *                  Must not be null. If the provided object has already been garbage
     *                  collected before execution, no operation will be performed.
     */
    CleaningAction(BaseComponent component) {
      this.componentRef = new WeakReference<>(component);
    }

    /**
     * Executes the cleanup process for the {@link BaseComponent}, invoking its
     * {@link BaseComponent#destroy()} method.
     * The component is retrieved using a {@link WeakReference} to avoid interfering with garbage
     * collection.
     *
     * <p>If the component has already been garbage collected, the method performs no action.
     * Any exceptions encountered during the cleanup process are logged but not propagated.</p>
     */
    @Override
    public void run() {
      // Get the component from the weak reference
      BaseComponent component = componentRef.get();
      if (component != null) {
        try {
          log.debug("Cleanup called for component: {}", component.getComponentId());
          component.destroy();
        } catch (Exception e) {
          // Log any cleanup errors
          log.error("Error during component cleanup: {}", e.getMessage());
        }
      }
    }
  }
}