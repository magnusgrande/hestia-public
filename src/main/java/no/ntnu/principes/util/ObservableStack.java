package no.ntnu.principes.util;

import java.util.EmptyStackException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

/**
 * Stack implementation backed by an observable JavaFX list.
 * Provides standard stack operations with observable elements for UI binding.
 *
 * @param <T> Type of elements stored in the stack
 */
@Getter
public class ObservableStack<T> {
  private final ObservableList<T> list = FXCollections.observableArrayList();

  /**
   * Pushes an item onto the top of the stack.
   *
   * @param item The item to add to the stack
   */
  public void push(T item) {
    this.list.add(item);
  }

  /**
   * Removes and returns the item at the top of the stack.
   *
   * @return The item at the top of the stack
   * @throws EmptyStackException If the stack is empty
   */
  public T pop() {
    if (this.list.isEmpty()) {
      throw new EmptyStackException();
    }
    return this.list.removeLast();
  }

  /**
   * Returns the item at the top of the stack without removing it.
   *
   * @return The item at the top of the stack
   * @throws EmptyStackException If the stack is empty
   */
  public T peek() {
    if (this.list.isEmpty()) {
      throw new EmptyStackException();
    }
    return this.list.getLast();
  }

  /**
   * Returns the item at a specified position relative to the top of the stack.
   * Index 0 is the top of the stack, -1 is one below the top, etc.
   *
   * @param peekIndex Relative index from the top (0 = top, negative values = below top)
   * @return The item at the specified position, or null if index is out of bounds
   */
  public T peek(int peekIndex) {
    // Allow peeking below the stack, just return null
    if (this.list.isEmpty()) {
      return null;
    }
    int indexToPeek = this.list.size() + peekIndex - 1;
    if (indexToPeek < 0 || indexToPeek >= this.list.size()) {
      return null;
    }
    return this.list.get(indexToPeek);
  }

  /**
   * Checks if the stack is empty.
   *
   * @return True if the stack contains no elements, false otherwise
   */
  public boolean isEmpty() {
    return this.list.isEmpty();
  }

  /**
   * Removes the first occurrence of the specified element from the stack.
   *
   * @param item The element to remove
   * @return True if the element was removed, false if not found
   */
  public boolean removeElement(T item) {
    return this.list.remove(item);
  }

  /**
   * Returns the number of elements in the stack.
   *
   * @return The number of elements in the stack
   */
  public int size() {
    return this.list.size();
  }

  /**
   * Removes all elements from the stack.
   */
  public void clear() {
    this.list.clear();
  }
}