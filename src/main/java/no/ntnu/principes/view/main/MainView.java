package no.ntnu.principes.view.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.impl.WelcomeBannerComponent;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.widgets.BaseWidget;
import no.ntnu.principes.components.widgets.CompletionRateWidget;
import no.ntnu.principes.components.widgets.HomeScreenQuickActionButtons;
import no.ntnu.principes.components.widgets.ImmediateTasksWidget;
import no.ntnu.principes.components.widgets.TaskDistributionWidget;
import no.ntnu.principes.components.widgets.UnassignedTasksWidget;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TaskCompletionUpdatedEvent;
import no.ntnu.principes.event.task.TaskCreatedEvent;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

@Slf4j
public class MainView extends DashboardScreen {
  private BaseWidget immediateTasksWidget;
  private final BaseWidget completionRateWidget;
  private final BaseWidget unassignedTasksWidget;
  private final BaseWidget taskDistributionWidget;
  private final Text todaysDateText = new Text("--", StyledText.TextType.SUBHEADER);
  private VBox scrollContent;

  public MainView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.completionRateWidget = new CompletionRateWidget(this);
    this.unassignedTasksWidget = new UnassignedTasksWidget(this);
    this.taskDistributionWidget = new TaskDistributionWidget(this);
  }

  @Override
  protected void initializeScreen() {
    super.initializeScreen();

    HBox topWidgets = new HBox(20);

    this.immediateTasksWidget = new ImmediateTasksWidget(this);
    HomeScreenQuickActionButtons quickActionButtons = new HomeScreenQuickActionButtons(
        "Quick Actions", this);

    topWidgets.getChildren().addAll(this.immediateTasksWidget, quickActionButtons);

    WelcomeBannerComponent welcomeBanner = new WelcomeBannerComponent(this);

    // Create a content VBox to hold all scrollable content
    this.scrollContent = new VBox(20);
    this.scrollContent.setPadding(InsetBuilder.uniform(20).build());
    this.scrollContent.getChildren().addAll(
        this.createHeader(),
        topWidgets
    );

    // Create a ScrollPane and add the content to it
    ScrollPane scrollPane = new ScrollPane(scrollContent);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    scrollPane.setStyle("-fx-background-color: transparent;");
    VBox bannerContainer = new VBox(welcomeBanner);
    bannerContainer.setPadding(InsetBuilder.symmetric(20, 0).build());

    this.content.getChildren().addAll(
        bannerContainer,
        scrollPane
    );

    StyleManager.growHorizontal(topWidgets, scrollContent, this, this.immediateTasksWidget,
        quickActionButtons, this.unassignedTasksWidget, this.taskDistributionWidget);

    this.subscribeToEvents();

    // This will add the widgets in the correct order based on if there are unassigned tasks
    this.updateWidgetsDisplay();
  }

  private VBox createHeader() {
    Text header = new Text("Dashboard", StyledText.TextType.PAGE_TITLE);

    VBox headerContainer = new VBox();
    headerContainer.getChildren()
        .setAll(header, this.todaysDateText);

    return headerContainer;
  }

  private void onTaskCompletionUpdated(TaskCompletionUpdatedEvent event) {
    log.debug("Task completion updated: {}", event.getData());
    this.refreshStats();
  }

  private void onTaskCreated(TaskCreatedEvent event) {
    log.debug("Task created: {}", event.getData());
    this.refreshStats();
  }

  private void onTasksDistributed(TasksDistributedEvent event) {
    log.debug("Tasks distributed: {}", event.getData());
    this.refreshStats();
  }

  private void refreshStats() {
    this.completionRateWidget.refresh();
    this.immediateTasksWidget.refresh();
    this.taskDistributionWidget.refresh();
    this.updateWidgetsDisplay();
  }

  /**
   * Updates the display of widgets, only showing the unassigned tasks widget
   * if there are unassigned tasks.
   */
  private void updateWidgetsDisplay() {
    // Remove all three widgets first (to maintain proper order)
    this.scrollContent.getChildren().remove(this.unassignedTasksWidget);
    this.scrollContent.getChildren().remove(this.completionRateWidget);
    this.scrollContent.getChildren().remove(this.taskDistributionWidget);

    // Refresh unassigned tasks widget and check if it has tasks
    boolean hasUnassignedTasks = this.unassignedTasksWidget.refresh();

    // Add widgets in the correct order
    if (hasUnassignedTasks) {
      this.scrollContent.getChildren().add(this.unassignedTasksWidget);
    }

    // Always add the completion rate and distribution widgets
    this.scrollContent.getChildren().addAll(this.completionRateWidget, this.taskDistributionWidget);
  }

  private void subscribeToEvents() {
    PrincipesEventBus.getInstance().subscribe(
        TaskCompletionUpdatedEvent.class,
        this::onTaskCompletionUpdated
    );
    PrincipesEventBus.getInstance().subscribe(
        TaskCreatedEvent.class,
        this::onTaskCreated
    );
    PrincipesEventBus.getInstance().subscribe(
        TasksDistributedEvent.class,
        this::onTasksDistributed
    );
  }

  private void unsubscribeFromEvents() {
    PrincipesEventBus.getInstance().unsubscribe(
        TaskCompletionUpdatedEvent.class,
        this::onTaskCompletionUpdated
    );
    PrincipesEventBus.getInstance().unsubscribe(
        TaskCreatedEvent.class,
        this::onTaskCreated
    );
    PrincipesEventBus.getInstance().unsubscribe(
        TasksDistributedEvent.class,
        this::onTasksDistributed
    );
  }

  private void refreshDate() {
    this.todaysDateText.setText(LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")
    ));
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();
    this.subscribeToEvents();
    this.refreshDate();
    this.refreshStats();
  }

  @Override
  protected void onNavigatedFrom() {
    super.onNavigatedFrom();
    this.unsubscribeFromEvents();
  }
}