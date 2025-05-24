package no.ntnu.principes.controller.screen;

import static no.ntnu.principes.components.Sidebar.SIDEBAR_WIDTH;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.components.WindowTitleBar;
import no.ntnu.principes.controller.StageController;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.event.navigation.NavigateEvent;
import no.ntnu.principes.event.navigation.NavigationClearStackEvent;
import no.ntnu.principes.event.navigation.OpenModalEvent;
import no.ntnu.principes.util.ModalResult;
import no.ntnu.principes.util.ObservableStack;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.view.BaseModal;
import no.ntnu.principes.view.BaseScreen;
import no.ntnu.principes.view.main.DashboardScreen;

/**
 * Controls the navigation and display of screens and modals in the application.
 * Manages screen transitions, modal dialogs, and maintains navigation history.
 */
@Slf4j
public class ScreenController {
  @Getter
  private final UUID screenControllerId = UUID.randomUUID();
  private static final String BORDER_RADIUS_STYLE =
      "-fx-background-radius: 15px;-fx-border-radius: 15px;";
  private static final double CORNER_ARC_SIZE = 30.0;

  @Getter
  private final Stage stage;
  @Getter
  private final StageController stageController;
  @Getter
  private final HashMap<String, Parent> screens;
  @Getter
  private final ObservableStack<String> navigationStack;

  // UI
  @Getter
  private final StackPane mainContainer;
  private final StackPane screenContainer;
  private final StackPane modalContainer;
  private final WindowTitleBar titleBar;

  // State
  private final Stack<String> modalStack;
  private final StringProperty currentScreenId = new SimpleStringProperty();

  /**
   * Manages the initialization and hierarchical layout of the graphical user interface,
   * including the setup of containers for main screens, modals, and the title bar.
   * also links its operations to a specified {@code StageController}.
   *
   * @param stageController The {@code StageController} for which this ScreenController is
   *                        managing screens and modals for.
   */
  public ScreenController(StageController stageController) {
    log.info("Initializing ScreenController for stage: {}", stageController.getName());
    this.stageController = stageController;
    this.stage = stageController.getStage();

    this.screens = new HashMap<>();
    this.modalStack = new Stack<>();
    this.navigationStack = new ObservableStack<>();

    // Initialize UI
    this.titleBar = new WindowTitleBar(this.stageController);
    this.mainContainer = new StackPane();
    this.screenContainer = new StackPane();
    this.modalContainer = this.createModalContainer();

    this.initializeContainers();
    this.initializeListeners();
  }

  // Scenes node contianers

  /**
   * Initializes and configures the primary containers for the UI, including their layout structure
   * and styling.
   */
  private void initializeContainers() {
    this.setupMainContainer();
    this.setupScreenContainer();
    this.applyContainerStyles();
    this.setupContainerLayout();
  }

  /**
   * Sets up the main container for the screen controller.
   */
  private void setupMainContainer() {
    this.mainContainer.setAlignment(Pos.TOP_LEFT);
    this.mainContainer.maxWidth(Double.MAX_VALUE);
    this.mainContainer.maxHeight(Double.MAX_VALUE);
    this.mainContainer.setStyle("-fx-background-color: -color-bg-default;");

    // Clip the main container to round the corners. Yay, looks like Mac OS!
    Rectangle clip = new Rectangle();
    clip.widthProperty().bind(this.mainContainer.widthProperty());
    clip.heightProperty().bind(this.mainContainer.heightProperty());
    clip.setArcWidth(CORNER_ARC_SIZE);
    clip.setArcHeight(CORNER_ARC_SIZE);
    this.mainContainer.setClip(clip);

    VBox.setVgrow(this.mainContainer, javafx.scene.layout.Priority.ALWAYS);
    HBox.setHgrow(this.mainContainer, javafx.scene.layout.Priority.ALWAYS);
  }

  /**
   * Sets up the screen container.
   */
  private void setupScreenContainer() {
    this.screenContainer.setStyle(BORDER_RADIUS_STYLE);
  }

  /**
   * Creates the modal container for displaying modals.
   *
   * @return The modal container as a {@link StackPane}.
   */
  private StackPane createModalContainer() {
    StackPane container = new StackPane();
    container.setPickOnBounds(false);
    container.setMouseTransparent(true);
    container.setPrefWidth(Double.MAX_VALUE);
    container.setPrefHeight(Double.MAX_VALUE);
    container.maxWidthProperty().bind(this.mainContainer.widthProperty());
    container.maxHeightProperty().bind(this.mainContainer.heightProperty());
    container.paddingProperty().bind(Bindings.createObjectBinding(() -> {
      String currentScreenId = this.currentScreenId.get();
      if (currentScreenId == null) {
        return InsetBuilder.create().top(0).build();
      }

      BaseScreen currentScreen = (BaseScreen) this.screens.get(currentScreenId);
      System.out.println(
          "Modal Padding Binding, currentScreen: " + currentScreen + ", currentScreenId: "
              + this.currentScreenId + ", isDashboard: "
              + (currentScreen instanceof DashboardScreen));
      if (currentScreen instanceof DashboardScreen) {
        return InsetBuilder.create().left(SIDEBAR_WIDTH - 40).build();
      }
      return InsetBuilder.create().top(0).build();
    }, this.currentScreenId));
    return container;
  }

  /**
   * Applies styles to the main and modal containers.
   */
  private void applyContainerStyles() {
    this.mainContainer.setStyle(BORDER_RADIUS_STYLE);
    this.modalContainer.setStyle(BORDER_RADIUS_STYLE);
  }


  /**
   * Sets up the layout of the main container, including the title bar and screen/modal containers.
   *
   * <p>This only affects screen controllers on non-utility stages.</p>
   */
  private void setupContainerLayout() {
    if (!this.stageController.isUtility()) {
      this.mainContainer.getChildren().addAll(this.titleBar);
      this.titleBar.toFront();
      this.mainContainer.getChildren().addAll(this.screenContainer, this.modalContainer);
    }
  }

  // Nav

  /**
   * Subscribes to the shared event bus, listening for navigation related events.
   *
   * <p>The supported events include:</p>
   * <ul>
   *   <li>{@code NavigateEvent} - Handled if the stage is the main stage.</li>
   *   <li>{@code CloseModalEvent} - Triggers modal closing logic.</li>
   *   <li>{@code OpenModalEvent} - Triggers modal opening logic.</li>
   *   <li>{@code NavigationClearStackEvent} - Clears navigation stack.</li>
   * </ul>
   */
  private void initializeListeners() {
    if (this.getStageController().isMainStage()) {
      PrincipesEventBus.getInstance()
          .subscribe(NavigateEvent.class, this::handleNavigateEvent);
    }
    PrincipesEventBus.getInstance()
        .subscribe(CloseModalEvent.class, this::handleCloseModalEvent)
        .subscribe(OpenModalEvent.class, this::handleOpenModalEvent)
        .subscribe(NavigationClearStackEvent.class, this::handleClearEvent);
  }

  /**
   * Clears internal data if the event's target is this stage controller.
   *
   * @param navigationClearStackEvent An event that contains data specifying which stage to clear.
   *                                  The `getData()` method must return a String representing
   *                                  the stage ID.
   */
  private void handleClearEvent(NavigationClearStackEvent navigationClearStackEvent) {
    if (navigationClearStackEvent.getData().equals(this.getStageController().getStageId())) {
      this.clear();
    }
  }

  /**
   * Unsubscribes event listeners from the {@code PrincipesEventBus} associated with
   * this controller. Ensures cleanup of event subscriptions.
   *
   * @throws NullPointerException if {@code getStageController()} or any required dependencies
   *                              return null unexpectedly.
   */
  private void tearDownListeners() {
    if (this.getStageController().isMainStage()) {
      PrincipesEventBus.getInstance()
          .unsubscribe(NavigateEvent.class, this::handleNavigateEvent);
    }
    PrincipesEventBus.getInstance()
        .unsubscribe(CloseModalEvent.class, this::handleCloseModalEvent);
    PrincipesEventBus.getInstance()
        .unsubscribe(OpenModalEvent.class, this::handleOpenModalEvent);
  }

  /**
   * Handles navigation events and performs the appropriate navigation actions.
   *
   * @param event The {@code NavigateEvent} instance containing data about the type of navigation
   *              operation
   */
  private void handleNavigateEvent(NavigateEvent event) {
    if (event.getData().type() == NavigateEvent.NavigationType.PUSH) {
      this.navigate(event.getData().route(), event.getData().params());
    } else if (event.getData().type() == NavigateEvent.NavigationType.POP) {
      if (event.getData().route() == null) {
        this.pop();
      } else {
        this.pop(event.getData().route());
      }
    } else if (event.getData().type() == NavigateEvent.NavigationType.REPLACE) {
      this.replace(event.getData().route(), event.getData().params());
    }
  }

  /**
   * Replaces the current screen with a new one based on the given route,
   * and navigates to the specified route with provided parameters.
   * Updates the navigation stack, switches the screen, and triggers lifecycle callbacks.
   *
   * @param route  The unique identifier (route) of the new screen to navigate to.
   *               Must be non-null and correspond to a known screen.
   * @param params A map of parameters to pass to the new screen. Can be empty or null
   *               if no parameters are required.
   */
  private void replace(String route, Map<String, Object> params) {
    log.info("Replacing screen: {}", route);
    this.navigationStack.pop();
    String previousScreen = this.navigationStack.peek();
    Parent screen = this.screens.get(previousScreen);
    this.currentScreenId.setValue(previousScreen);

    if (screen != null) {
      Parent currentScreen = this.getCurrentScreen();
      this.screenContainer.getChildren().setAll(screen);
      this._maybeCallNavigatedFrom(currentScreen);
      this._maybeCallNavigatedTo(screen);
    } else {
      this.handleBrokenBackReference(previousScreen);
    }
    this.navigate(route, params);
  }

  /**
   * Handles the event of closing a modal.
   *
   * @param event The {@link CloseModalEvent} containing details about the modal being closed.
   */
  private void handleCloseModalEvent(CloseModalEvent event) {
    this.closeModal(event.getModalId());
  }

  /**
   * Opens a modal window based on the provided {@link OpenModalEvent} object, ensuring that
   * the modal is allowed to be displayed under the current stage configuration.
   *
   * @param event An {@link OpenModalEvent} containing the modal's route name, parameters,
   *              and an optional callback ID.
   */
  private void handleOpenModalEvent(OpenModalEvent event) {
    boolean isMainStage = this.getStageController().isMainStage();
    String modalName = event.getData().route();
    String stageControllerName = this.getStageController().getName();
    boolean isDebugModal = modalName.equals("debugOverlay");
    boolean allowDebugModal = isDebugModal && stageControllerName.equals("debugWindow");
    boolean allowOthermodal = isMainStage && !isDebugModal;
    if (allowDebugModal || allowOthermodal) {
      this.openModal(event.getData().route(), event.getCallbackId(),
          event.getData().params());
    }
  }

  /**
   * Navigates to a specified screen by name. If the target screen is not registered, it navigates
   * to the "not-found" screen.
   *
   * @param name The name of the target screen. Must not be null. If the screen is not registered,
   *             navigation will fall back to the "not-found" screen.
   */
  private void navigate(String name) {
    log.info("Navigating to screen: {}", name);
    Parent screen = this.screens.get(name);

    if (screen == null) {
      log.warn("Attempted to navigate to non-existent screen: {}", name);
      this.navigate("not-found");
      return;
    }

    if (this.shouldSkipNavigation(name)) {
      return;
    }

    this.performNavigation(name, screen);
  }

  /**
   * Navigates to a specified screen and optionally provides context parameters to that screen.
   * The target screen is identified by its name, and any given parameters are passed to its
   * context.
   *
   * @param name       The unique name of the target screen to navigate to. Must not be null.
   * @param parameters A map containing key-value pairs representing parameters to be set in the
   *                   target screen's context. May be null if no parameters need to be passed.
   *                   Any parameter with a null value will overwrite existing values for the same
   *                   key.
   */
  private void navigate(String name, Map<String, Object> parameters) {
    log.info("Navigating to screen '{}' with parameters: {}", name, parameters);
    ScreenContext context = this.getContext(name);

    if (parameters != null) {
      parameters.forEach((key, value) -> {
        log.debug("Setting context parameter: {} = {}", key, value);
        context.setParameter(key, value);
      });
    }

    this.navigate(name);
  }

  /**
   * Determines whether navigation to the specified screen should be skipped.
   * Skipping occurs if the current screen matches the target screen or
   * if navigating to the previous screen in the stack.
   *
   * @param name The name of the target screen. Must not be null.
   * @return {@code true} if navigation should be skipped; {@code false} otherwise.
   */
  private boolean shouldSkipNavigation(String name) {
    // Skip if already on the given screen
    if (!this.navigationStack.isEmpty() && this.navigationStack.peek().equals(name)) {
      log.debug("Already on screen: {}", name);
      return true;
    }

    // Handle navigation to previous screen
    String previousScreen = this.navigationStack.peek(-1);
    if (!this.navigationStack.isEmpty() && previousScreen != null && previousScreen.equals(name)) {
      this.pop();
      return true;
    }

    return false;
  }

  /**
   * Performs a navigation operation by updating the current screen and maintaining
   * the navigation history stack.
   *
   * <p>This method changes the application's active view to the specified screen,
   * updates the navigation stack to track the new screen, and notifies the
   * current and new screens about the navigation event if applicable.
   * </p>
   *
   * @param name   The unique identifier for the screen being navigated to.
   *               Must not be null or empty.
   * @param screen The JavaFX {@code Parent} node representing the new screen
   *               to display. Must not be null.
   */
  private void performNavigation(String name, Parent screen) {
    this.navigationStack.push(name);
    this.currentScreenId.setValue(name);
    this.screenContainer.getChildren().setAll(screen);

    Parent currentScreen = this.getCurrentScreen();
    this._maybeCallNavigatedFrom(currentScreen);
    this._maybeCallNavigatedTo(screen);
  }

  /**
   * Navigates to a specified screen behind the current screen. This is useful for
   * preloading screens or preparing for background navigation.
   *
   * @param name The unique name of the target screen to navigate to. Must not be null or empty.
   *             If no screen with the given name is found in the map, no action is performed.
   */
  private void navigateInBackground(String name) {
    log.info("Navigating in background to screen: {}", name);
    Parent screen = this.screens.get(name);

    if (screen == null) {
      log.warn("Attempted to navigate in background to non-existent screen: {}", name);
      return;
    }

    if (this.screenContainer.getChildren().isEmpty()) {
      this.navigate(name);
      return;
    }

    this.performBackgroundNavigation(name, screen);
  }

  /**
   * Performs navigation to a new screen in the background while managing the navigation stack.
   *
   * <p>Removes the current screen from the container and replaces it with the new screen.
   * Additionally, updates the navigation stack to maintain a history of visited screens.
   * Calls a helper method to handle any post-navigation actions for the navigated-to screen.</p>
   *
   * @param name   The unique identifier for the new screen being navigated to.
   *               This is used to track the navigation history.
   * @param screen The {@link Parent} node representing the new screen to display.
   *               Must not be null; replacing the current screen requires a valid node.
   */
  private void performBackgroundNavigation(String name, Parent screen) {
    Node currentScreen = this.screenContainer.getChildren().getFirst();
    this.screenContainer.getChildren().clear();
    this.screenContainer.getChildren().addAll(screen, currentScreen);

    if (!this.navigationStack.isEmpty()) {
      String currentScreenName = this.navigationStack.pop();
      this.navigationStack.push(name);
      this.navigationStack.push(currentScreenName);
    } else {
      this.navigationStack.push(name);
    }

    this._maybeCallNavigatedTo(screen);
  }

  // Modal mgmt

  /**
   * Opens a modal dialog specified by its name, generates a unique identifier for the modal,
   * and initializes it with provided parameters.
   *
   * <p>If the specified modal is already open, nothing happens.</p>
   * If the modal is not found in the registered screens, a warning is logged.
   *
   * @param name       The unique name of the modal to be opened. Must be a valid key in the
   *                   registered  screens.
   * @param callbackId An identifier used to associate actions or events with the modal instance.
   *                   Can be null.
   * @param parameters A map containing additional data to pass into the modal for initialization
   *                   or interaction. Can be empty or null if no parameters are needed.
   */
  private void openModal(String name, String callbackId, Map<String, Object> parameters) {
    if (this.modalStack.contains(name)) {
      log.debug("Modal already open: {}", name);
      return;
    }

    String generatedId = name + "-" + System.currentTimeMillis();
    log.info("Opening modal: {} with id: {} and callbackId: {}", name, generatedId, callbackId);
    Parent modal = this.screens.get(name);

    if (modal == null) {
      log.warn("Attempted to open non-existent modal: {}", name);
      return;
    }

    this.setupAndShowModal(name, generatedId, callbackId, modal, parameters);
  }

  /**
   * Configures and displays a modal window in the application. This involves setting up its
   * context, updating the navigation stack, and enabling user interaction for the modal.
   *
   * @param name        The unique name of the modal being displayed. This is pushed onto the modal
   *                    stack to track navigation.
   * @param generatedId A unique identifier for the modal context, used to store and retrieve
   *                    parameters specific to the modal view.
   * @param callbackId  An identifier for callback processing, enabling tracking of events or
   *                    actions triggered by the modal.
   * @param modal       The {@code Parent} node representing the UI structure of the modal. This is
   *                    added to the application’s modal container for rendering.
   * @param parameters  A map of key-value pairs containing additional data passed to the modal.
   */
  private void setupAndShowModal(String name, String generatedId, String callbackId, Parent modal,
                                 Map<String, Object> parameters) {
    this.initializeModalResult(modal, generatedId, callbackId);

    // Set parameters for the modal
    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
      ScreenContextManager.getHolder(this.screenControllerId).getOrCreateContext(generatedId)
          .setParameter(entry.getKey(), entry.getValue());
    }

    Parent currentModal = this.getCurrentModal();
    if (!this.modalStack.isEmpty()) {
      this._maybeCallNavigatedFrom(currentModal);
    }
    this.modalStack.push(name);
    this.modalContainer.getChildren().add(modal);
    this._maybeCallNavigatedTo(modal);
    this.enableModalInteraction();
  }

  /**
   * Initializes the modal result by associating a pending {@code ModalResult} with the provided
   * modal's context within the {@code ScreenContextManager}.
   *
   * @param modal       The modal to initialize. Can be a {@code BaseModal} or {@code BaseScreen}.
   *                    If neither, no modal result is set.
   * @param generatedId A unique identifier generated for the modal, used to track its context.
   *                    Required for {@code BaseModal} instances.
   * @param callbackId  A string identifier used to link the modal result callback.
   *                    This value is stored in the context and tied to the {@code ModalResult}.
   */
  private void initializeModalResult(Parent modal, String generatedId, String callbackId) {
    if (modal instanceof BaseModal baseModal) {
      log.debug("Modal is being opened, preparing result");
      ScreenContextManager.getHolder(this.screenControllerId).getOrCreateContext(generatedId)
          .setParameter("modalResult", ModalResult.pending(callbackId));
      baseModal.setModalId(generatedId);
    } else if (modal instanceof BaseScreen baseScreen) {
      log.debug("Modal is a screen, setting up modal result");
      ScreenContextManager.getHolder(this.screenControllerId)
          .getOrCreateContext(baseScreen.getScreenId())
          .setParameter("modalResult", ModalResult.pending(callbackId));
    } else {
      log.debug("Modal is not a BaseModal or BaseScreen, not setting up modal result");
    }
  }

  /**
   * Enables interaction with the modal layer by configuring its ability to receive mouse events.
   */
  private void enableModalInteraction() {
    this.modalContainer.setPickOnBounds(true);
    this.modalContainer.setMouseTransparent(false);
  }

  /**
   * Disables interaction with the modal layer by preventing it from receiving mouse events.
   */
  private void disableModalInteraction() {
    this.modalContainer.setPickOnBounds(false);
    this.modalContainer.setMouseTransparent(true);
  }

  /**
   * Closes the currently active modal in the modal stack, if any, and handles
   * the transition to the previous modal or disables modal interaction if no
   * modals remain.
   */
  private void closeModal() {
    if (this.modalStack.isEmpty()) {
      log.debug("No modal to close");
      return;
    }

    String currentModalName = this.modalStack.pop();
    log.info("Closing modal: {}", currentModalName);

    this._maybeCallNavigatedFrom(this.getCurrentModal());
    if (!this.modalStack.isEmpty()) {
      this.modalContainer.getChildren().removeLast();
    }
    ScreenContextManager.getHolder(this.screenControllerId).clearContext(currentModalName);

    Node previousModal = this.getCurrentModal();
    this._maybeCallNavigatedTo(previousModal);

    if (previousModal == null) {
      this.disableModalInteraction();
    }
  }

  /**
   * Closes and removes the specified modal from the current screen, updating the modal stack
   * and clearing its associated context. If no modals remain after this operation, modal-related
   * interaction is disabled.
   *
   * @param modalId The unique identifier of the modal to close. If the modal does not exist in
   *                the stack, its context is still cleared, and no exception is thrown.
   */
  private void closeModal(String modalId) {
    if (this.modalStack.isEmpty()) {
      log.debug("[{}]: No modal to close", this.getStageController().getName());
      return;
    }

    log.info("Closing modal: {}", modalId);

    BaseModal modal = this.getModal(modalId);
    if (modal != null) {
      this._maybeCallNavigatedFrom(modal);
      this.modalContainer.getChildren().remove(modal);
      ScreenContextManager.getHolder(this.screenControllerId).clearContext(modalId);
      this.modalStack.remove(modal.getScreenId());
    } else {
      ScreenContextManager.getHolder(this.screenControllerId).clearContext(modalId);
      this.modalStack.remove(modalId);
    }
    Node previousModal = this.getCurrentModal();
    if (previousModal == null) {
      this.disableModalInteraction();
    }
  }

  /**
   * Closes all currently active modals in the application by iteratively removing them
   * from the modal stack.
   */
  private void closeAllModals() {
    log.info("Closing all modals");
    while (!this.modalStack.isEmpty()) {
      this.closeModal();
    }
  }


  // Screen Mgmt

  /**
   * Adds a screen to the collection, associating it with a unique name.
   * The screen can later be retrieved or manipulated by its name.
   *
   * @param name   A unique identifier for the screen. Must not be null or empty.
   *               If a screen with the same name already exists, it will be replaced.
   * @param screen The screen instance to add. Must not be null.
   */
  public void addScreen(String name, BaseScreen screen) {
    log.debug("Adding screen: {}", name);
    this.screens.put(name, screen);
  }

  /**
   * Removes a screen and its associated context.
   * This includes removing the screen from the screen registry, clearing its context,
   * and removing any references from navigation and modal stacks.
   *
   * @param name The unique identifier of the screen to be removed. Must not be null or empty.
   *             If the screen does not exist, no action will be performed.
   */
  public void removeScreen(String name) {
    log.info("Removing screen: {}", name);
    this._maybeCallCleanup(this.screens.get(name));
    this.screens.remove(name);
    ScreenContextManager.getHolder(this.screenControllerId).clearContext(name);
    this.navigationStack.removeElement(name);
    this.modalStack.removeElement(name);
    log.debug("Screen and context removed: {}", name);
  }

  /**
   * Removes the top screen context from the navigation stack and navigates back
   * to the previous screen if possible.
   *
   * @throws IllegalStateException If the back navigation fails for unexpected reasons.
   */
  private void pop() {
    log.info("Going back");
    if (this.navigationStack.size() <= 1) {
      log.debug("Cannot go back - at root screen");
      return;
    }

    this.handleBackNavigation();
  }

  /**
   * Removes a screen from the navigation stack by its unique identifier,
   * updates the current screen and handles navigation to the previous screen if present.
   *
   * @param screenId The unique identifier of the screen to be removed. Must not be null.
   *                 If the screen is not present in the stack, a warning is logged, and no changes
   *                 are made.
   */
  private void pop(String screenId) {
    log.info("Removing screen from stack: {}", screenId);
    if (!this.navigationStack.getList().contains(screenId)) {
      log.warn("Screen not in stack: {}", screenId);
      return;
    }

    this.navigationStack.removeElement(screenId);
    this.currentScreenId.setValue(this.navigationStack.peek());
    Parent screen = this.screens.get(this.navigationStack.peek());
    if (screen != null) {
      this.performBackNavigation(screen);
    } else {
      this.handleBrokenBackReference(this.navigationStack.peek());
    }
  }

  /**
   * Navigates back to the previous screen in the navigation stack.
   *
   * <p>If the previous screen is not found, it navigates to the "not-found" screen.</p>
   */
  private void handleBackNavigation() {
    this.navigationStack.pop();
    String previousScreen = this.navigationStack.peek();
    Parent screen = this.screens.get(previousScreen);
    this.currentScreenId.setValue(previousScreen);

    if (screen != null) {
      this.performBackNavigation(screen);
    } else {
      this.navigate("not-found");
    }
  }

  /**
   * Performs back navigation by replacing the current screen with the specified screen.
   *
   * @param screen The screen to navigate to. Must not be null.
   */
  private void performBackNavigation(Parent screen) {
    Parent currentScreen = this.getCurrentScreen();
    this.screenContainer.getChildren().setAll(screen);
    this._maybeCallNavigatedFrom(currentScreen);
    this._maybeCallNavigatedTo(screen);
  }

  /**
   * Handles a broken back reference by logging an error and removing the
   * non-existent screen from the navigation stack.
   *
   * <p>This is used when a screen is not found in the navigation stack,
   * indicating a potential issue with the navigation history.</p>
   *
   * @param previousScreen The name of the previous screen that could not be found.
   */
  private void handleBrokenBackReference(String previousScreen) {
    log.error("Previous screen not found: {}", previousScreen);
    log.debug("Broken reference '{}', removing from stack and calling back again",
        previousScreen);
    this.navigationStack.pop();
    this.currentScreenId.setValue(this.navigationStack.peek());

    if (!this.navigationStack.isEmpty()) {
      this.pop();
    }
  }

  /**
   * Completely resets the navigation states, by clearing all screens and modals.
   * Lifecycle hooks are called for each screen to ensure proper cleanup.
   */
  private void clear() {
    log.info("Clearing all screens and modals");
    this.closeAllModals();

    for (Node screen : this.screenContainer.getChildren()) {
      this._maybeCallNavigatedFrom(screen);
      this._maybeCallCleanup(screen);
    }
    this.screenContainer.getChildren().clear();
    this.navigationStack.clear();
    this.currentScreenId.setValue(null);
    log.debug("All screens and modals cleared");
  }

  // Lifecycle Hooks

  /**
   * Calls the {@link BaseScreen#handleNavigatedFrom()} method on the given screen
   * if it is an instance of {@link BaseScreen}. Otherwise, does nothing.
   *
   * @param screen The screen to call the lifecycle hook on.
   */
  private void _maybeCallNavigatedFrom(Node screen) {
    if (screen instanceof BaseScreen baseScreen) {
      log.debug("Calling handleNavigatedFrom on {}", baseScreen.getScreenId());
      try {
        baseScreen.handleNavigatedFrom();
      } catch (Exception e) {
        log.error("Error calling lifecycle hook", e);
      }
    }
  }

  /**
   * Calls the {@link BaseScreen#handleNavigatedTo()} method on the given screen
   * if it is an instance of {@link BaseScreen}. Otherwise, does nothing.
   *
   * @param screen The screen to call the lifecycle hook on.
   */
  private void _maybeCallNavigatedTo(Node screen) {
    if (screen instanceof BaseScreen baseScreen) {
      log.debug("Calling handleNavigatedTo on {}", baseScreen.getScreenId());
      try {
        baseScreen.handleNavigatedTo();
      } catch (Exception e) {
        log.error("Error calling lifecycle hook", e);
      }
    }
  }


  /**
   * Calls the {@link BaseScreen#handleCleanup()} method on the given screen
   * if it is an instance of {@link BaseScreen}. Otherwise, does nothing.
   *
   * @param screen The screen to call the lifecycle hook on.
   */
  private void _maybeCallCleanup(Node screen) {
    if (screen instanceof BaseScreen baseScreen) {
      log.debug("Calling handleCleanup on {}", baseScreen.getScreenId());
      try {
        baseScreen.handleCleanup();
      } catch (Exception e) {
        log.error("Error calling lifecycle hook", e);
      }
    }
  }

  // Ctx and screen state

  /**
   * Eases the process of obtaining a {@link ScreenContext} for a given screen ID.
   */
  public ScreenContext getContext(String screenId) {
    log.debug("Getting context for screen: {}", screenId);
    return ScreenContextManager.getHolder(this.screenControllerId).getOrCreateContext(screenId);
  }

  /**
   * Retrieves the currently active screen in the screen container.
   *
   * @return The currently active {@link Parent} screen, or {@code null} if no screens
   * are present in the container.
   */
  public Parent getCurrentScreen() {
    if (this.screenContainer.getChildren().isEmpty()) {
      log.debug("No current screen (container empty)");
      return null;
    }
    return (Parent) this.screenContainer.getChildren().getFirst();
  }

  /**
   * Retrieves the currently active modal in the modal container.
   *
   * @return The currently active {@link Parent} modal, or {@code null} if no modals
   * are present in the container.
   */
  private Parent getCurrentModal() {
    if (this.modalContainer.getChildren().isEmpty()) {
      return null;
    }
    return (Parent) this.modalContainer.getChildren().getLast();
  }

  /**
   * Retrieves a modal instance by its unique identifier.
   *
   * @param modalId The unique identifier of the modal to retrieve.
   * @return The {@link BaseModal} instance if found, or {@code null} if not found.
   */
  private BaseModal getModal(String modalId) {
    return this.modalContainer.getChildren().stream()
        .filter(node -> node instanceof BaseModal)
        .map(node -> (BaseModal) node)
        .filter(modal -> modal.getModalId().equals(modalId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Retrieves the current modal instance if it is of type {@link BaseModal}.
   *
   * @return The current {@link BaseModal} instance, or {@code null} if no modals are open
   * or the current modal is not a {@link BaseModal}.
   */
  public BaseModal getCurrentModalInstance() {
    Parent currentModal = this.getCurrentModal();
    if (currentModal instanceof BaseModal) {
      return (BaseModal) currentModal;
    }
    return null;
  }

  // Navstate

  /**
   * Checks if the navigation stack has more than one screen, indicating that
   * the user can navigate back to a previous screen.
   *
   * @return {@code true} if there is a previous screen to navigate back to;
   * {@code false} otherwise.
   */
  public boolean canGoBack() {
    return this.navigationStack.size() > 1;
  }

  /**
   * Checks if there are any open modals in the modal stack.
   *
   * @return {@code true} if there are open modals; {@code false} otherwise.
   */
  public boolean hasOpenModals() {
    return !this.modalStack.isEmpty();
  }

  /**
   * Retrieves the current screen ID as an observable string value.
   *
   * @return The current screen ID as an {@link ObservableStringValue}.
   */
  public ObservableStringValue getCurrentScreenId() {
    return this.currentScreenId;
  }

  /**
   * Retrieves the current modal ID from the modal stack.
   *
   * @return The current modal ID as a string, or {@code null} if no modals are open.
   */
  public String getCurrentModalId() {
    return this.modalStack.isEmpty() ? null : this.modalStack.peek();
  }

  /**
   * Sets the main container to display a utility node.
   *
   * <p>This method is used to display a utility node in the main container,
   * for utility stages, like the sidebar and debug windows.</p>
   *
   * @param node The node to be displayed in the main container.
   */
  public void utility(Node node) {
    this.mainContainer.getChildren().setAll(node);
    if (node instanceof BaseComponent baseComponent) {
      baseComponent.mount();
    }
  }

  /**
   * Cleans up resources used by the current instance.
   *
   * <p>This method removes any listeners and clears stored data to ensure the instance
   * is properly reset and does not hold onto unnecessary references.
   * </p>
   *
   * <ul>
   * <li>Calls {@code tearDownListeners()} to deregister any attached listeners.</li>
   * <li>Invokes {@code clear()} to remove any internal state or data.</li>
   * </ul>
   */
  public void close() {
    this.tearDownListeners();
    this.clear();
  }

  @Override
  public int hashCode() {
    return this.screenControllerId.hashCode();
  }
}
