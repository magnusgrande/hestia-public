package no.ntnu.principes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.ntnu.principes.util.ModalResult.ModalResultStatus;
import org.junit.jupiter.api.Test;

public class ModalResultTest {

  @Test
  public void testWithResultSuccess() {
    String callbackId = "test-callback";
    Object result = "test-result";
    boolean success = true;

    ModalResult modalResult = ModalResult.withResult(callbackId, result, success);

    assertEquals(callbackId, modalResult.getCallbackId());
    assertEquals(result, modalResult.getResult());
    assertTrue(modalResult.isSuccess());
    assertEquals(ModalResultStatus.SUCCESS, modalResult.getStatus());
  }

  @Test
  public void testPending() {
    String callbackId = "test-callback";

    ModalResult modalResult = ModalResult.pending(callbackId);

    assertEquals(callbackId, modalResult.getCallbackId());
    assertNull(modalResult.getResult());
    assertFalse(modalResult.isSuccess());
    assertEquals(ModalResultStatus.PENDING, modalResult.getStatus());
  }

  @Test
  public void testCanceled() {
    String callbackId = "test-callback";

    ModalResult modalResult = ModalResult.canceled(callbackId);

    assertEquals(callbackId, modalResult.getCallbackId());
    assertNull(modalResult.getResult());
    assertFalse(modalResult.isSuccess());
    assertEquals(ModalResultStatus.CANCEL, modalResult.getStatus());
  }
}