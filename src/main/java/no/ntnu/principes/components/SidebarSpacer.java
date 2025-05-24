package no.ntnu.principes.components;

import static no.ntnu.principes.components.Sidebar.SIDEBAR_WIDTH;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import no.ntnu.principes.view.BaseScreen;

/**
 * Creates a responsive spacer component that reserves space for the sidebar.
 * Dynamically adjusts its width based on whether the parent window is in fullscreen
 * or maximized state.
 */
public class SidebarSpacer extends BaseComponent {
  private final DoubleProperty width = new SimpleDoubleProperty(SIDEBAR_WIDTH - 40);
  private final ChangeListener<Boolean> fullScreenListener = (observable, oldValue, newValue) -> {
    if (newValue) {
      this.width.set(SIDEBAR_WIDTH);
    } else {
      this.width.set(SIDEBAR_WIDTH - 40);
    }
  };

  /**
   * Creates a new SidebarSpacer attached to the specified parent screen.
   *
   * @param parent The parent screen that contains this spacer
   */
  public SidebarSpacer(BaseScreen parent) {
    super("sidebar-spacer", parent);
  }

  @Override
  protected void initializeComponent() {
    this.setWidth(Double.MAX_VALUE);
    this.maxWidthProperty().bind(this.width);
    this.minWidthProperty().bind(this.width);
  }

  @Override
  protected void onMount() {
    this.parentScreen.getController().getStage().fullScreenProperty()
        .addListener(this.fullScreenListener);
    this.parentScreen.getController().getStage().maximizedProperty()
        .addListener(this.fullScreenListener);
    boolean currentlyFullOrMaximized =
        this.parentScreen.getController().getStage().isFullScreen()
            || this.parentScreen.getController().getStage().isMaximized();
    this.width.set(currentlyFullOrMaximized ? SIDEBAR_WIDTH : SIDEBAR_WIDTH - 40);
  }

  @Override
  protected void onUnmount() {
    this.parentScreen.getController().getStage().fullScreenProperty()
        .removeListener(this.fullScreenListener);
    this.parentScreen.getController().getStage().maximizedProperty()
        .removeListener(this.fullScreenListener);
  }

  @Override
  protected void onDestroy() {
  }
}