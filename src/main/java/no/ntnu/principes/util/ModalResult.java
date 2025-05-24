package no.ntnu.principes.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/**
 * Captures the outcome of a modal dialog operation.
 * Includes the operation status, callback identifier, and optional result data.
 */
@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class ModalResult {
  private String callbackId;
  private @Nullable Object result;
  private boolean success;
  private ModalResultStatus status;

  /**
   * Creates a ModalResult with success or failure status based on the success flag.
   *
   * @param callbackId Identifier for the modal operation
   * @param result     The data returned from the modal
   * @param success    Whether the operation completed successfully
   * @return A new ModalResult with appropriate status
   */
  public static ModalResult withResult(String callbackId, Object result, boolean success) {
    return new ModalResult(callbackId, result, success,
        success ? ModalResultStatus.SUCCESS : ModalResultStatus.FAILURE);
  }

  /**
   * Creates a ModalResult with pending status.
   *
   * @param callbackId Identifier for the modal operation
   * @return A new ModalResult with PENDING status
   */
  public static ModalResult pending(String callbackId) {
    return new ModalResult(callbackId, null, false, ModalResultStatus.PENDING);
  }

  /**
   * Creates a ModalResult for a canceled operation.
   *
   * @param callbackId Identifier for the modal operation
   * @return A new ModalResult with CANCEL status
   */
  public static ModalResult canceled(String callbackId) {
    return new ModalResult(callbackId, null, false, ModalResultStatus.CANCEL);
  }

  /**
   * Possible states for a modal operation result.
   */
  public enum ModalResultStatus {
    PENDING, SUCCESS, FAILURE, CANCEL
  }
}