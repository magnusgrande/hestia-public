package no.ntnu.principes.components.secondary;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import javafx.beans.value.ObservableValue;
import net.synedra.validatorfx.Validator;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

/**
 * Displays validation errors on a form and dynamically updates its state based on the bound
 * {@link Validator}.
 * This component monitors the validation status of the associated form and shows an error message
 * when validation fails.
 * The error message is automatically updated with the details provided by the validator.
 *
 * <p>This component uses a message panel styled as a danger alert, and it is displayed only when
 * validation errors are present. If the form becomes valid, the message panel is removed.</p>
 *
 * <p>Maximum height is constrained to 200 pixels. The width is set to fill the available space by
 * default.</p>
 *
 * <p><b>Lifecycle:</b></p>
 * <ul>
 *   <li>On mounting: Starts listening to validation state changes and initiates validation.</li>
 *   <li>On unmounting: Stops listening to validation changes and clears the validator.</li>
 *   <li>On destruction: The validator is cleared, and bindings for dynamic message updates are
 *   removed.</li>
 * </ul>
 */
public class FormValidatorResultInfo extends BaseComponent {
  private final Validator validator;
  private Message message;

  /**
   * Constructs a new {@code FormValidatorResultInfo} component that displays and dynamically
   * updates a form's validation errors based on the provided {@link Validator}.
   * This component is mounted to the specified parent screen and initializes with its default
   * state.
   *
   * @param parentScreen the {@link BaseScreen} to which this component is attached.
   *                     It provides the component's lifecycle management and context.
   * @param validator    the {@link Validator} responsible for validating the form's data and
   *                     providing detailed error messages. This must not be {@code null}.
   *                     If the validator detects errors, they are displayed dynamically within
   *                     this component.
   */
  public FormValidatorResultInfo(BaseScreen parentScreen, Validator validator) {
    super("form-validator", parentScreen);
    this.validator = validator;
  }

  /**
   * Initializes and configures the {@code FormValidatorResultInfo} component to display form
   * validation errors. Dynamically binds validation error messages to the component's state and
   * configures its appearance and behavior.
   *
   * <p>Adds an error message UI element if validation errors are detected during initialization.
   * </p>
   *
   * <ul>
   * <li>Binds the error message's description to the {@link Validator}'s dynamic error state.
   * </li>
   * <li>Applies danger styling to highlight errors prominently.</li>
   * <li>Sets a maximum height of 200 units for the component and its error message.</li>
   * <li>Maximizes the component's width to occupy available horizontal space.</li>
   * </ul>
   *
   * @throws IllegalStateException if the {@code validator} is improperly initialized or null when
   *                               this method is invoked.
   */
  @Override
  protected void initializeComponent() {
    this.message = new Message("Invalid or missing data", "Please check the form for errors",
        new FontIcon(Material2AL.ERROR));
    this.message.descriptionProperty().bind(validator.createStringBinding());
    this.message.getStyleClass().add(Styles.DANGER);
    this.setMaxHeight(200);
    this.message.setMaxHeight(200);
    this.setPrefWidth(Double.MAX_VALUE);
    StyleManager.growHorizontal(this, this.message);
    if (this.validator.containsErrors()) {
      this.getChildren().add(this.message);
    }
  }

  /**
   * Updates the visibility of a validation error message based on the validation state.
   *
   * <p>If a validation error is detected (i.e., {@code newValue} is {@code true}),
   * the validation error message is added to the component's children if it is not already present.
   * If the validation state changes to no error (i.e., {@code newValue} is {@code false}),
   * the message is removed from the component's children if it exists.
   * </p>
   *
   * @param obs      the {@link ObservableValue} representing the validation state.
   *                 This is typically bound to the result of validation logic.
   * @param oldValue the previous validation state.
   * @param newValue the current validation state, where {@code true} indicates an error
   *                 and {@code false} indicates no errors.
   */
  private void onValidationError(ObservableValue<? extends Boolean> obs, Boolean oldValue,
                                 Boolean newValue) {
    if (newValue) {
      if (this.getChildren().isEmpty()) {
        this.getChildren().add(this.message);
      }
    } else {
      this.getChildren().remove(this.message);
    }
  }

  /**
   * Sets up listeners and triggers validation logic when the component is mounted.
   *
   * <p>This method ensures the validation state is actively monitored by adding a property
   * change listener to the {@code containsErrorsProperty()} of the associated {@code Validator}.
   * It listens for changes in validation errors and invokes {@link #onValidationError}
   * to handle UI updates based on the validation state.
   * </p>
   *
   * <p>Immediately triggers an initial validation on the mounted state to ensure any existing
   * errors are accounted for and reflected in the UI at the moment of mounting.
   * </p>
   *
   * @throws IllegalStateException if the associated {@code Validator} is not properly initialized
   *                               or is null.
   */
  @Override
  protected void onMount() {
    this.validator.containsErrorsProperty().addListener(this::onValidationError);
    this.validator.validate();
  }

  /**
   * Cleans up state and listeners associated with form validation when the component is removed
   * from its parent or lifecycle.
   */
  @Override
  protected void onUnmount() {
    this.validator.containsErrorsProperty().removeListener(this::onValidationError);
    this.validator.clear();
  }

  /**
   * Cleans up resources and unbinds properties associated with the validation mechanism.
   */
  @Override
  protected void onDestroy() {
    this.validator.clear();
    this.message.descriptionProperty().unbind();
  }
}
