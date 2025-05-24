package no.ntnu.principes.event.navigation;

import java.util.Map;
import lombok.Getter;
import no.ntnu.principes.event.PrincipesEvent;

/**
 * Represents an event for opening a modal within the application.
 * This event encapsulates the callback identifier and payload information
 * required to display a modal with specific parameters.
 *
 * <p>The payload contains details like the target route or identifier and
 * optional parameters that provide additional context for the modal.</p>
 */
@Getter
public class OpenModalEvent extends PrincipesEvent<OpenModalEvent.ModalPayload> {
  private final String callbackId;

  /**
   * Constructs a new {@code OpenModalEvent} with a unique callback identifier and payload
   * containing modal configuration details.
   *
   * @param callbackId a unique string used to identify the modal's callback operation.
   *                   This ID triggers specific post-modal actions upon modal closure.
   * @param payload    encapsulates the modal target route and additional parameters
   *                   required to configure the modal display and behavior.
   */
  public OpenModalEvent(String callbackId, ModalPayload payload) {
    super(payload);
    this.callbackId = callbackId;
  }

  /**
   * Creates a new instance of {@code OpenModalEvent} with the specified path and callback
   * identifier.
   *
   * @param path       the target route or identifier of the modal to be opened.
   * @param callBackId the callback identifier associated with the modal event.
   * @return a new {@code OpenModalEvent} with the specified path and callback identifier.
   */
  public static OpenModalEvent of(String path, String callBackId) {
    return new OpenModalEvent(callBackId, new ModalPayload(path, Map.of()));
  }

  /**
   * Creates a new {@code OpenModalEvent} with the specified callback identifier and payload.
   * The payload includes the target path for the modal and any additional parameters.
   *
   * @param path       the target path or route identifier for the modal
   * @param callBackId the unique callback identifier associated with the modal event
   * @param params     a map of key-value pairs representing additional parameters for the modal
   *                   (can be empty)
   * @return a new {@code OpenModalEvent} containing the specified callback identifier and payload
   */
  public static OpenModalEvent of(String path, String callBackId, Map<String, Object> params) {
    return new OpenModalEvent(callBackId, new ModalPayload(path, params));
  }

  /**
   * Represents the payload data required to open a modal in the application.
   * This payload contains information necessary for the modal, including the
   * target route and additional parameters.
   *
   * @param route  the target route or identifier for the modal
   *               (e.g., the path to the modal component)
   * @param params a map of key-value pairs representing additional parameters for the modal
   *               (e.g., context data or configuration options)
   */
  public record ModalPayload(String route, Map<String, Object> params) {
  }
}
