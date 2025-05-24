package no.ntnu.principes.components.secondary;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import no.ntnu.principes.components.BaseComponent;

/**
 * Extends the functionality of JavaFX's <code>TabPane</code> to manage lifecycle methods
 * (<code>mount</code> and <code>unmount</code>) of {@link BaseComponent instances} within its tabs.
 *
 * <p>When the selected tab changes, the previously visible tab's components are unmounted,
 * and the newly selected tab's components are mounted. This ensures proper initialization
 * and cleanup for components that extend <code>BaseComponent</code>.</p>
 *
 * <p>Uses recursive traversal to detect and execute mount/unmount actions on all
 * child components of type <code>BaseComponent</code>, if present. Tabs that do not
 * contain <code>BaseComponent</code> instances are ignored during this process.</p>
 */
public class TabPane extends javafx.scene.control.TabPane {
  /**
   * Constructs a custom TabPane that manages lifecycle methods (`mount` and `unmount`)
   * for {@link BaseComponent} instances within its tabs.
   *
   * <p>When the selected tab changes:</p>
   * <ul>
   *   <li>The components in the previously selected tab are unmounted.</li>
   *   <li>The components in the newly selected tab are mounted.</li>
   * </ul>
   */
  public TabPane() {
    super();
    this.getSelectionModel().selectedIndexProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue.intValue() == -1) {
            return;
          }
          // Get the selected tab
          Tab selectedTab = this.getTabs().get(newValue.intValue());
          Tab previousTab = this.getTabs().get(Math.max(oldValue.intValue(), 0));
          // Unmount the previous tab
          if (previousTab.getContent() instanceof Parent) {
            this.recurseUnmountBaseComponents((Parent) previousTab.getContent());
          }
          // Mount the new tab
          if (selectedTab.getContent() instanceof Parent) {
            this.recurseMountBaseComponents((Parent) selectedTab.getContent());
          }
        });
  }

  /**
   * Recursively mounts all {@link BaseComponent} instances within a given JavaFX
   * {@link Parent} node.
   *
   * <p>If the node itself is a {@link BaseComponent}, the {@link BaseComponent#mount} method
   * is invoked. Otherwise, the method traverses all {@link Node} children of the {@link Parent}
   * recursively, searching for and mounting child {@link BaseComponent} instances.</p>
   *
   * <p>Nodes that are not {@link Parent} types or {@link BaseComponent} instances
   * are ignored.</p>
   *
   * @param node A JavaFX {@link Parent} node to process. Can be a {@link BaseComponent},
   *             a container of nested {@link BaseComponent} instances, or {@code null}.
   */
  private void recurseMountBaseComponents(Parent node) {
    if (node instanceof BaseComponent) {
      ((BaseComponent) node).mount();
    } else if (node != null) {
      for (Node child : node.getChildrenUnmodifiable()) {
        if (child instanceof Parent) {
          this.recurseMountBaseComponents((Parent) child);
        }
      }
    }
  }

  /**
   * Recursively unmounts all {@link BaseComponent} instances within a given JavaFX {@link Parent}
   * node.
   *
   * <p>If the provided node is a {@link BaseComponent}, its {@link BaseComponent#unmount}
   * method is invoked.
   * Otherwise, the method traverses all child {@link Node} elements of the provided {@link Parent}.
   * If any child is also a {@link Parent}, the method recursively processes its descendants.
   * Non-{@link Parent} and non-{@link BaseComponent} nodes are ignored.</p>
   *
   * <p>If the input {@code node} is {@code null}, the method terminates immediately without action.
   * </p>
   *
   * @param node A JavaFX {@link Parent} node to process. Can be a {@link BaseComponent},
   *             a container of nested {@link BaseComponent} instances, or {@code null}.
   */
  public void recurseUnmountBaseComponents(Parent node) {
    if (node instanceof BaseComponent) {
      ((BaseComponent) node).unmount();
    } else if (node != null) {
      for (Node child : node.getChildrenUnmodifiable()) {
        if (child instanceof Parent) {
          this.recurseUnmountBaseComponents((Parent) child);
        }
      }
    }
  }
}
