package no.ntnu.principes.event.navigation;

import lombok.Getter;
import no.ntnu.principes.event.PrincipesEvent;
import no.ntnu.principes.util.ModalResult;

/**
 * Represents an event for closing a modal within the application.
 * This event encapsulates the identifier of the modal being closed
 * and the result of the modal action in the form of a {@link ModalResult}.
 *
 * <p>The event is designed to notify listeners when a modal has been closed,
 * providing contextual information about the closure.
 * </p>
 */
@Getter
public class CloseModalEvent extends PrincipesEvent<ModalResult> {
  private final String modalId;

  /**
   * Constructs a {@code CloseModalEvent} with the specified modal identifier and payload.
   * This event signifies the closure of a modal and includes data relevant to the result
   * of the modal action.
   *
   * @param modalId the unique identifier of the modal being closed
   * @param payload the {@link ModalResult} object containing details about the closure result
   */
  public CloseModalEvent(String modalId, ModalResult payload) {
    super(payload);
    this.modalId = modalId;
  }

  /**
   * Creates a new {@code CloseModalEvent} instance with the specified modal identifier and result
   * payload.
   *
   * @param modalId the unique identifier of the modal being closed
   * @param payload contains the result of the modal's operation, encapsulated in a
   *                {@code ModalResult}
   * @return a new instance of {@code CloseModalEvent} with the provided modal identifier and result
   * payload
   */
  public static CloseModalEvent of(String modalId, ModalResult payload) {
    return new CloseModalEvent(modalId, payload);
  }
}
