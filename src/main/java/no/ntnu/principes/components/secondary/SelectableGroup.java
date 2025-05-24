package no.ntnu.principes.components.secondary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import no.ntnu.principes.components.primary.Selectable;

/**
 * A specialized horizontal container for managing and rendering a group of selectable components.
 *
 * <p>The SelectableGroup creates and manages an interactive collection of {@code Selectable}
 * elements. Each element is associated with a unique identifier, label text, and an initial
 * selection state. The group enforces selection behavior, allowing for dynamic updates to the
 * visual and logical state of the elements. When a selection is made, a callback
 * function is executed with the associated identifier of the selected item or {@code null} if
 * no item is selected.</p>
 */
public class SelectableGroup extends HBox {
  private final Consumer<String> onSelect;
  private final Map<String, Selectable> selectableMap = new HashMap<>();

  /**
   * Creates a horizontal container for managing a group of selectable components, allowing
   * interaction and updating their selection states dynamically.
   *
   * <p>Each item in the group is represented as a {@code Selectable} component with a label,
   * an initial selection state, and a unique identifier. When an item is selected, the specified
   * callback function is executed with the identifier of the selected item, or {@code null}
   * if no item is selected.</p>
   *
   * @param items    A list of {@code SelectableGroupItem} objects, each containing a unique
   *                 identifier, label text, and an initial selected state. If the list is empty,
   *                 the group will render no selectable components.
   * @param onSelect A {@code Consumer<String>} callback triggered when an item's selection state
   *                 changes. The callback receives the identifier of the selected item, or
   *                 {@code null} if no item is selected. Cannot be null.
   */
  public SelectableGroup(List<SelectableGroupItem> items, Consumer<String> onSelect) {
    // Setup self
    HBox.setHgrow(this, Priority.ALWAYS);

    this.onSelect = onSelect;

    this.setSpacing(10);
    for (SelectableGroupItem item : items) {
      Selectable selectable = new Selectable(item.text(), item.selected());
      selectable.setOnAction(e -> this.handleSelect(item.id()));
      this.selectableMap.put(item.id(), selectable);
      getChildren().add(selectable);
    }
  }

  /**
   * Updates the selection state of a specific {@code Selectable} component in the group.
   *
   * <p>The method checks if the provided {@code id} matches a selectable component in the group.
   * If it exists and is not already selected, it deselects all other components and marks the
   * matching component as selected. If the {@code id} is already selected, no change.</p>
   *
   * <p>After updating the selection state, the specified callback function ({@code onSelect})
   * is invoked with the {@code id} of the selected component or {@code null} if nothing is
   * selected.</p>
   *
   * @param id The identifier of the {@code Selectable} component to toggle selection for.
   *           If {@code null} or invalid, no changes occur.
   */
  private void handleSelect(String id) {
    boolean selected = false;
    for (Map.Entry<String, Selectable> entry : this.selectableMap.entrySet()) {
      boolean isSelected = entry.getKey().equals(id) && !entry.getValue().isSelected();
      selected = selected || isSelected;
      entry.getValue().setSelected(isSelected);
    }

    this.onSelect.accept(selected ? id : null);
  }

  /**
   * Represents an item in a {@code SelectableGroup}, containing its unique identifier,
   * displayed label, and current selection state.
   *
   * <p>This object is immutable and used to initialize or update selectable components within a
   * group.</p>
   *
   * @param id       The unique identifier for the item. Must not be null or empty.
   * @param text     The display text for the item. Intended for use as a label.
   * @param selected The initial selection state of the item. {@code true} if selected,
   *                 {@code false} otherwise.
   */
  public record SelectableGroupItem(String id, String text, boolean selected) {
  }
}