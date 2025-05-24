package no.ntnu.principes.view;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.controller.screen.ScreenContext;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.util.ModalResult;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * Base class for modal dialogs with animated transitions and result handling.
 * Provides overlay background, content container, and standardized close behaviors.
 * Supports result callback pattern for returning data from modal dialogs.
 */
@Setter
@Getter
@Slf4j
public abstract class BaseModal extends BaseScreen {
  private String modalId;
  private VBox modalContent;
  private final EventHandler<KeyEvent> keyEventHandler;
  private boolean isClosing = false;

  /**
   * Creates a new modal with the specified controller and screen ID.
   * Sets up the modal layout and registers ESC key handler.
   *
   * @param controller The screen controller
   * @param screenId   The unique screen identifier
   */
  public BaseModal(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.keyEventHandler = keyEvent -> {
      System.out.println("Key pressed: " + keyEvent.getCode());
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        this.initiateClose(ModalResult.ModalResultStatus.CANCEL, null);
      }
    };
    this.setupModal();
  }

  /**
   * Sets up the modal's visual structure with overlay and content container.
   * Configures styling, animations, and event handling for the modal dialog.
   */
  private void setupModal() {
    // Basic idea is, the outer HBox (this) will be the overlay with a opacity of 0.5 and black
    // background.
    // The inner vbox will be the content, like a card that fits 90% of the screen width and height
    this.modalContent = new VBox();
    this.getChildren().add(this.modalContent);
    StyleManager.grow(this);
    this.setAlignment(Pos.CENTER);
    this.setOnMouseClicked(e -> this.animateOut());
    this.setOpacity(0);

    // Set the content
    this.modalContent = new VBox();
    StyleManager.grow(this.modalContent);

    // Consume click events on the content card so that the overlay doesn't close when clicking on
    // the card
    this.modalContent.setOnMouseClicked(Event::consume);

    StyleManager.apply(this.modalContent, Styles.BG_DEFAULT, Styles.ROUNDED,
        StyleManager.Overlay.CARD);
    boolean isMainStage = this.getController().getStageController().isMainStage();
    if (isMainStage) {
      // Set the overlay background
      StyleManager.apply(this, StyleManager.Overlay.OVERLAY);
      VBox.setMargin(this.modalContent, InsetBuilder.uniform(40).build());
      this.modalContent.setTranslateY(80);
      this.modalContent.maxHeightProperty().bind(this.heightProperty().multiply(0.9));
      this.modalContent.maxWidthProperty().bind(this.widthProperty().multiply(0.9));
      this.modalContent.setPadding(InsetBuilder.uniform(20).build());
    } else {
      this.modalContent.setPadding(InsetBuilder.custom(40, 20, 20, 20).build());
    }
  }

  /**
   * Initiates the modal closing process with a specific status and result.
   * Prevents multiple close attempts and prepares result before animation.
   *
   * @param status The status to set for the modal result
   * @param result The data result to return, if any
   */
  private void initiateClose(ModalResult.ModalResultStatus status, Object result) {
    if (this.isClosing) {
      return;
    }
    this.isClosing = true;

    // Prepare the result before animation
    if (result != null) {
      this.setResult(result);
    } else {
      ModalResult modalResult = this.getModalResult();
      if (modalResult != null) {
        modalResult.setStatus(status);
        modalResult.setSuccess(status == ModalResult.ModalResultStatus.SUCCESS);
      }
    }

    // Always animate out, then perform actual close
    this.animateOut();
  }

  /**
   * Completes the modal closing process after animation finishes.
   * Publishes a close event for the controller to process.
   */
  private void finalizeClose() {
    log.debug("Finalizing close for modal: {}", this.getScreenId());

    // Publish the close event
    PrincipesEventBus.getInstance().publish(
        CloseModalEvent.of(this.modalId, this.getModalResult())
    );
  }

  /**
   * Gets the screen context for this modal.
   * Retrieves context parameters including the modal result object.
   *
   * @return The screen context for this modal
   */
  @Override
  protected ScreenContext getContext() {
    log.debug("Getting context for modal: {}", this.getScreenId());
    ScreenContext context = this.controller.getContext(this.modalId);
    log.debug("Context parameters for {}: {}", this.getScreenId(), context.toString());
    return context;
  }

  /**
   * Closes the modal with a success status.
   */
  public void close() {
    log.debug("Closing modal: {}", this.getScreenId());
    this.initiateClose(ModalResult.ModalResultStatus.SUCCESS, null);
  }

  /**
   * Closes the modal with the specified status.
   *
   * @param status The status to set for the modal result
   */
  public void close(ModalResult.ModalResultStatus status) {
    log.debug("Closing modal: {} with status: {}", this.getScreenId(), status);
    this.initiateClose(status, null);
  }

  /**
   * Closes the modal with a success status and the specified result data.
   *
   * @param result The data to return from the modal
   */
  public void closeWithResult(Object result) {
    log.debug("Closing modal: {} with result: {}", this.getScreenId(), result);
    this.initiateClose(ModalResult.ModalResultStatus.SUCCESS, result);
  }

  /**
   * Sets the modal result status to cancelled and closes the modal.
   */
  protected void setModalCancelled() {
    log.debug("Setting modal cancelled: {}", this.getScreenId());
    this.initiateClose(ModalResult.ModalResultStatus.CANCEL, null);
  }

  /**
   * Sets the modal result status to failed and closes the modal.
   */
  protected void setModalFailed() {
    log.debug("Setting modal failed: {}", this.getScreenId());
    this.initiateClose(ModalResult.ModalResultStatus.FAILURE, null);
  }

  /**
   * Sets the result data in the modal result object.
   * Also sets the status to success.
   *
   * @param result The data to set as the result
   */
  private void setResult(Object result) {
    log.debug("Setting result for modal: {} with result: {}", this.getScreenId(), result);
    ModalResult resultObject = this.getModalResult();
    if (resultObject != null) {
      resultObject.setResult(result);
      resultObject.setSuccess(true);
      resultObject.setStatus(ModalResult.ModalResultStatus.SUCCESS);
    }
  }

  /**
   * Gets the modal result object from the screen context.
   *
   * @return The modal result object or null if not found
   */
  public ModalResult getModalResult() {
    return this.getContext().getParameter("modalResult");
  }

  /**
   * Animates the modal into view with fade and slide transitions.
   */
  private void animateIn() {
    Platform.runLater(() -> {
      FadeTransition ft = new FadeTransition();
      ft.setNode(this);
      ft.setDuration(Duration.millis(200));
      ft.setFromValue(0);
      ft.setToValue(1);

      TranslateTransition tt = new TranslateTransition();
      tt.setNode(this.modalContent);
      tt.setFromY(80);
      tt.setToY(0);
      tt.setDuration(Duration.millis(400));
      ParallelTransition pt = new ParallelTransition(ft, tt);
      pt.setOnFinished(e -> this.isClosing = false);
      pt.play();
    });
  }

  /**
   * Animates the modal out of view with fade and slide transitions.
   * Calls finalizeClose when animation completes.
   */
  private void animateOut() {
    Platform.runLater(() -> {
      FadeTransition ft = new FadeTransition();
      ft.setNode(this);
      ft.setDuration(Duration.millis(200));
      ft.setFromValue(1);
      ft.setToValue(0);


      TranslateTransition tt = new TranslateTransition();
      tt.setNode(this.modalContent);
      tt.setFromY(0);
      tt.setToY(80);
      tt.setDuration(Duration.millis(400));
      ParallelTransition pt = new ParallelTransition(ft, tt);
      pt.setOnFinished(e -> this.finalizeClose());
      pt.play();
    });
  }

  /**
   * Handles navigation to this modal.
   * Registers key event handler and animates the modal in.
   */
  @Override
  protected void onNavigatedTo() {
    // Reset the form when navigating to this screen
    this.controller.getStage().addEventHandler(KeyEvent.KEY_PRESSED, this.keyEventHandler);
    this.animateIn();
  }

  /**
   * Handles navigation away from this modal.
   * Removes the key event handler.
   */
  @Override
  protected void onNavigatedFrom() {
    this.controller.getStage().removeEventHandler(KeyEvent.KEY_PRESSED, this.keyEventHandler);
  }
}