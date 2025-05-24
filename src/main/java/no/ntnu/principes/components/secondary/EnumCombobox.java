package no.ntnu.principes.components.secondary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * A specialized JavaFX ComboBox for enum types, allowing selection from the enum constants with
 * customizable inclusion and exclusion of specific values.
 *
 * @param <T> The enum type used by this combo box.
 */
public class EnumCombobox<T> extends ComboBox<T> {
  private final ObservableList<T> excludedItems = FXCollections.observableArrayList();

  /**
   * Constructs a ComboBox pre-populated with the constants of the provided enum type.
   * Items in the ComboBox can later be dynamically excluded or re-included if needed.
   *
   * @param enumClass The class of the enum whose constants will populate this ComboBox.
   *                  Must be a valid enum class. If null, no items will be added.
   */
  public EnumCombobox(Class<T> enumClass) {
    super();
    this.updateItems(enumClass);
    this.setCellFactory(param -> new EnumComboboxElement<>());
  }

  /**
   * Creates an instance of {@code EnumCombobox} pre-populated with the constants
   * of the specified enum type.
   *
   * <p>This is a helper method for constructing {@code EnumCombobox} objects.
   * It simplifies instantiation by automatically passing the enum class to the
   * constructor. The combobox will display all constants of the given enum unless
   * they are dynamically excluded after creation.</p>
   *
   * @param <T>       The type of the enum.
   * @param enumClass The class of the enum whose constants will populate the ComboBox.
   *                  Must be a valid, non-null enum type. If {@code null}, the resulting
   *                  {@code EnumCombobox} will contain no items.
   * @return A new {@code EnumCombobox} instance populated with the enum constants.
   */
  public static <T> EnumCombobox<T> of(Class<T> enumClass) {
    return new EnumCombobox<>(enumClass);
  }

  /**
   * Removes the specified item from the ComboBox's list of selectable items and marks it as
   * excluded.
   *
   * <p>This method adds the item to the internal exclusion list and immediately removes it from
   * the underlying items displayed in the ComboBox. It has no effect if the item is already
   * excluded or not present in the ComboBox's list of items.</p>
   *
   * @param item The item to exclude. Must not be null and must be a valid element of the
   *             ComboBox. If null or not present in the ComboBox, no action is taken.
   */
  public void excludeItem(T item) {
    this.excludedItems.add(item);
    this.getItems().remove(item);
  }

  /**
   * Adds the specified item back to the ComboBox's list of selectable items by removing it from
   * the internal exclusion list and appending it to the underlying item list.
   *
   * <p>If the item is not currently excluded, it is still added to the ComboBox's list of items.
   * Duplicate entries in the ComboBox are not prevented, so ensure that the specified item is
   * notalready present to maintain consistent behavior.</p>
   *
   * @param item The item to include in the ComboBox. Must not be null.
   *             If null, no action is taken.
   */
  public void includeItem(T item) {
    this.excludedItems.remove(item);
    this.getItems().add(item);
  }

  /**
   * Sets multiple items to be excluded from the ComboBox's list of selectable items.
   *
   * <p>This method clears the current list of excluded items, adds the provided items
   * to the exclusion list, and removes them from the ComboBox's displayed items.
   * If any of the provided items are not present in the ComboBox's list, they
   * are simply ignored without causing an error.</p>
   *
   * @param items The items to exclude from the ComboBox. These should be valid elements
   *              of the ComboBox. Passing <code>null</code> or an empty array has no effect.
   */
  @SafeVarargs
  public final void setExcludedItems(T... items) {
    this.excludedItems.clear();
    this.excludedItems.addAll(items);
    this.getItems().removeAll(items);
  }

  /**
   * Updates the list of items displayed in the ComboBox with the constants of the specified enum,
   * excluding items that are marked in the internal exclusion list.
   *
   * <p>This method first clears the current items in the ComboBox, retrieves all constants
   * from the given enum class, and adds only those not present in the exclusion list.
   * If the enum class is null or contains no constants, the ComboBox will remain empty.</p>
   *
   * @param enumClass The class of the enum whose constants will populate the ComboBox.
   *                  Must be a valid, non-null enum class.
   *                  If null, the ComboBox will not be updated.
   */
  private void updateItems(Class<T> enumClass) {
    this.getItems().clear();
    for (T item : enumClass.getEnumConstants()) {
      if (!this.excludedItems.contains(item)) {
        this.getItems().add(item);
      }
    }
  }

  /**
   * A custom ListCell implementation designed to display elements in a ComboBox populated with
   * enum constants.
   *
   * <p>This class updates the visual representation of a ComboBox item by setting its text to the
   * string returned by the item's {@code toString()} method.
   * When no item is present or the cell is empty, it clears the displayed text.
   * </p>
   *
   * @param <T> The type of the item, an enum type.
   */
  public static class EnumComboboxElement<T> extends ListCell<T> {
    @Override
    protected void updateItem(T item, boolean empty) {
      super.updateItem(item, empty);
      if (item == null || empty) {
        this.setText(null);
      } else {
        this.setText(item.toString());
      }
    }
  }
}
