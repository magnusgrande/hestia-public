package no.ntnu.principes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EmptyStackException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObservableStackTest {
  private static ObservableStack<String> stack;

  @BeforeAll
  public static void setUp() {
    stack = new ObservableStack<>();
  }

  @BeforeEach
  public void resetStack() {
    stack.clear();
  }

  @Test
  public void testPushAndPop() {
    String item = "test item";

    stack.push(item);
    String popped = stack.pop();

    assertEquals(item, popped);
    assertTrue(stack.isEmpty());
  }

  @Test
  public void testPeek() {
    String item = "test item";

    stack.push(item);
    String peeked = stack.peek();

    assertEquals(item, peeked);
    assertFalse(stack.isEmpty());
    assertEquals(1, stack.size());
  }

  @Test
  public void testPeekWithIndex() {
    stack.push("item1");
    stack.push("item2");
    stack.push("item3");

    assertEquals("item3", stack.peek(0));
    assertEquals("item2", stack.peek(-1));
    assertEquals("item1", stack.peek(-2));
    assertNull(stack.peek(-3));
    assertNull(stack.peek(1));
  }

  @Test
  public void testPopEmptyStack() {
    assertThrows(EmptyStackException.class, () -> stack.pop());
  }
}