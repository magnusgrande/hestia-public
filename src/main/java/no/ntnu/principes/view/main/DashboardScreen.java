package no.ntnu.principes.view.main;

import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import no.ntnu.principes.components.Sidebar;
import no.ntnu.principes.components.SidebarSpacer;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

/**
 * Abstract class representing a dashboard screen in the application.
 * It extends the BaseScreen class and provides the basic structure
 * and functionality for dashboard screens, with the sidebar and handling of its visibility.
 */
public abstract class DashboardScreen extends BaseScreen {
  private Sidebar sidebar;
  protected final VBox content;

  public DashboardScreen(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.content = new VBox();
  }

  @Override
  protected void initializeScreen() {
    this.sidebar = Sidebar.init(this);
    this.getChildren().add(new SidebarSpacer(this));
    StyleManager.grow(this.content);
    this.content.setStyle("-fx-background-color: -color-bg-default;");
    this.content.setPadding(InsetBuilder.create().top(40).left(20).build());

    this.getChildren().add(this.content);
  }

  private boolean nextTargetIsDashboardView() {
    Parent target = this.controller.getCurrentScreen();
    return target instanceof DashboardScreen;
  }

  @Override
  protected void onNavigatedFrom() {
    if (this.sidebar != null && !this.nextTargetIsDashboardView()) {
      this.sidebar.unmount();
    }
  }

  @Override
  protected void onNavigatedTo() {
    if (this.sidebar != null) {
      this.sidebar.mount();
    }
  }
}
