package no.ntnu.principes.components;

import atlantafx.base.theme.Styles;
import java.util.function.UnaryOperator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/**
 * A user interface component providing task-related quick actions, including task creation
 * and status filtering options.
 *
 * <p>Includes a "New Task" button and toggle buttons to filter tasks by their status
 * ("All", "Todo", "Done").</p>
 */
@Slf4j
public class QuickActions extends BaseComponent {
  private final BooleanProperty allowCreateProperty = new SimpleBooleanProperty(true);
  private final TaskTemplateService taskTemplateService = new TaskTemplateService();
  private final TaskAssignmentService taskAssignmentService = new TaskAssignmentService();
  private Button createTaskButton;
  // Setters for callbacks
  @Setter
  private UnaryOperator<TaskStatus> onSelectChange;

  /**
   * Initializes the QuickActions class for managing quick action components on the specified
   * parent screen.
   *
   * @param parentScreen The BaseScreen instance that this QuickActions object is linked to;
   *                     must not be null.
   */
  public QuickActions(BaseScreen parentScreen) {
    super("quick-actions", parentScreen);
  }

  @Override
  protected void initializeComponent() {
    this.setPadding(InsetBuilder.create().right(20).bottom(10).build());
    StyleManager.growHorizontal(this);

    this.initializeLayout();
    this.setStyle("-fx-background-color: -color-bg-default;");
  }

  /**
   * Initializes the toolbar layout with new task button and toggle group for task filtering.
   */
  private void initializeLayout() {
    // Left button group

    createTaskButton = new Button("New Task", Button.ButtonType.DEFAULT);
    createTaskButton.setIcon(new FontIcon(Material2MZ.PLUS));
    createTaskButton.setPadding(InsetBuilder.symmetric(15, 5).build());
    StyleManager.apply(createTaskButton, Styles.SMALL);
    new ConfigValueBinder(
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class)).bindBoolean(
        "settings.allow_create", true, this.allowCreateProperty
    );
    this.createTaskButton.disableProperty()
        .bind(this.allowCreateProperty.not().and(Bindings.createBooleanBinding(() -> {
          // TODO: should we maybe have an observable boolean in Auth, representing the current
          //  users Admin status?
          return false;
        }, this.allowCreateProperty)));

    ToolBar group = new ToolBar();
    group.setStyle("-fx-background-color: -color-bg-default;");
    group.getItems().addAll(createTaskButton, new Separator(Orientation.VERTICAL));


    ToggleButton allButton = new ToggleButton("All");
    ToggleButton todoButton = new ToggleButton("Todo");
    ToggleButton doneButton = new ToggleButton("Done");
    allButton.setGraphic(new FontIcon(Material2MZ.VIEW_LIST));
    todoButton.setGraphic(new FontIcon(Material2AL.HOURGLASS_EMPTY));
    doneButton.setGraphic(new FontIcon(Material2AL.CHECK));
    allButton.setTooltip(new Tooltip("View all tasks"));
    doneButton.setTooltip(new Tooltip("View all pending tasks"));
    doneButton.setTooltip(new Tooltip("View all completed tasks"));
    allButton.setUserData(null);
    todoButton.setUserData(TaskStatus.TODO);
    doneButton.setUserData(TaskStatus.DONE);

    ToggleGroup toggleGroup = new ToggleGroup();
    toggleGroup.getToggles().addAll(allButton, todoButton, doneButton);
    toggleGroup.selectToggle(allButton);
    toggleGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
      log.debug("Selected: {}", newVal);
      if (newVal == null) {
        return;
      }
      if (this.onSelectChange != null) {
        this.onSelectChange.apply((TaskStatus) newVal.getUserData());
      }
    });
    HBox toggleGroupBox = new HBox(allButton, todoButton, doneButton);
    toggleGroupBox.setSpacing(5);
    group.getItems().add(toggleGroupBox);
    StyleManager.growHorizontal(group);

    this.getChildren().add(group);
  }

  @Override
  protected void setupEventHandlers() {
    this.createTaskButton.setOnAction(e -> this.handleCreateTask());
  }

  @Override
  protected void onMount() {

  }

  @Override
  protected void onUnmount() {

  }

  @Override
  protected void onDestroy() {
    // Clear callbacks
    this.onSelectChange = null;
  }

  /**
   * Opens a modal window for creating a new task, by sending a navigation event through the
   * NavigationService.
   */
  private void handleCreateTask() {
    NavigationService.openModal("createTaskModal", "qa-create-task");
  }
}