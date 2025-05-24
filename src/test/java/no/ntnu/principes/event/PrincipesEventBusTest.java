package no.ntnu.principes.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PrincipesEventBusTest {
  private static PrincipesEventBus eventBus;

  private static class TestEvent extends PrincipesEvent<String> {
    public TestEvent(String data) {
      super(data);
    }
  }

  @BeforeAll
  public static void setUp() {
    eventBus = PrincipesEventBus.getInstance();
  }

  @Test
  public void testSubscribeAndPublish() {
    AtomicBoolean listenerCalled = new AtomicBoolean(false);
    String testData = "test data";

    PrincipesEventListener<TestEvent> listener = event -> {
      assertEquals(testData, event.getData());
      listenerCalled.set(true);
    };

    eventBus.subscribe(TestEvent.class, listener);
    eventBus.publish(new TestEvent(testData));
    // Clean up
    eventBus.unsubscribe(TestEvent.class, listener);

    assertTrue(listenerCalled.get());
  }

  @Test
  public void testUnsubscribe() {
    AtomicBoolean listenerCalled = new AtomicBoolean(false);

    PrincipesEventListener<TestEvent> listener = event -> {
      listenerCalled.set(true);
    };

    eventBus.subscribe(TestEvent.class, listener);
    boolean unsubscribeResult = eventBus.unsubscribe(TestEvent.class, listener);
    eventBus.publish(new TestEvent("test"));

    assertTrue(unsubscribeResult);
    assertFalse(listenerCalled.get());
  }
}