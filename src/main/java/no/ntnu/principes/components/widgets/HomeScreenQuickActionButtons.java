package no.ntnu.principes.components.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.AlertUtil;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Widget that provides quick access to action buttons for managing tasks
 * and household-related functionalities on the home screen.
 */
public class HomeScreenQuickActionButtons extends BaseWidget {
  private static final Logger log = LoggerFactory.getLogger(HomeScreenQuickActionButtons.class);
  private static final int ICON_SIZE = 48;

  private final TaskTemplateService taskTemplateService = new TaskTemplateService();
  private final TaskAssignmentService taskAssignmentService = new TaskAssignmentService();
  private boolean listenerSubscribed = false;

  /**
   * Constructs an instance of the `HomeScreenQuickActionButtons` widget, providing quick
   * action buttons for the application's home screen.
   *
   * @param widgetHeader The text to display as the widget's header.
   * @param parentScreen The {@link BaseScreen} to which this widget is associated,
   *                     managing its lifecycle and context.
   */
  public HomeScreenQuickActionButtons(String widgetHeader,
                                      BaseScreen parentScreen) {
    super("homescreen-quick-actions", widgetHeader,
        "Manage tasks and household members", parentScreen);
  }

  @Override
  protected void initializeComponent() {

    // Create action grid and add it to the content container
    this.contentContainer.getChildren().add(createActionGrid());
    this.setMinWidth(350);
    this.setMaxWidth(350);
  }

  /**
   * Creates a grid of action buttons arranged in a 2x2 layout.
   *
   * @return A GridPane containing the action buttons
   */
  private GridPane createActionGrid() {
    this.contentContainer.setAlignment(Pos.TOP_LEFT);
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));

    // Create the four action buttons
    StackPane newTaskBtn = createActionButton("New Task", new FontIcon(Material2AL.ADD),
        e -> NavigationService.openModal("createTaskModal", "create-task-quick-action"));

    StackPane distributeTasksBtn =
        createActionButton("Distribute Tasks", new FontIcon(Material2MZ.SHARE),
            e -> {
              List<Task> tasks = taskTemplateService.getUnassignedTasks();

              if (tasks.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Tasks to Distribute");
                alert.setHeaderText("All tasks are already assigned");
                alert.showAndWait();
                return;
              }

              log.debug("Unassigned tasks: {}, distributing...", tasks.size());
              List<TaskAssignment> assignedTasks = new ArrayList<>();
              AlertUtil.setEnabled(false); // Prevent alerts for each task
              for (Task task : tasks) {
                assignedTasks.add(taskAssignmentService.autoAssignTask(task.getId(), false));
              }
              StringBuilder sb = new StringBuilder();
              // Grooup assignments by member, and build string to inform about counts assinged
              Map<Long, String> memberIdToName = DatabaseManager.getInstance().getRepository(
                  MemberRepository.class).findAll().stream().collect(
                  Collectors.toMap(Profile::getId, Profile::getName)
              );
              assignedTasks.stream().collect(
                  Collectors.groupingBy(TaskAssignment::getMemberId)
              ).forEach((memberId, assignments) -> {
                sb.append(assignments.size()).append(" tasks assigned to ")
                    .append(memberIdToName.get(memberId))
                    .append("\n");
              });
              AlertUtil.setEnabled(true);
              Alert alert = new Alert(Alert.AlertType.INFORMATION);
              alert.setTitle("Tasks Distributed");
              alert.setHeaderText("Tasks have been distributed to members");
              alert.setContentText(sb.toString());
              alert.showAndWait();
              log.debug("Tasks distributed");
              PrincipesEventBus.getInstance().publish(
                  TasksDistributedEvent.of(tasks)
              );
            });

    StackPane addMemberBtn = createActionButton("Add Member", new FontIcon(Material2MZ.PERSON_ADD),
        e -> {
          NavigationService.navigate("household", Map.of("tabIndex", 0));
        });
//
//    StackPane reassignTasksBtn =
//        createActionButton("Reassign Tasks", new FontIcon(Material2MZ.SWAP_HORIZ),
//            e -> {
//              // TODO: Implement reassign tasks action
//            });

    // Add buttons to the grid
    grid.add(newTaskBtn, 0, 0);
    grid.add(distributeTasksBtn, 1, 0);
    grid.add(addMemberBtn, 0, 1, 2, 1);
//    grid.add(reassignTasksBtn, 1, 1);

    // Make all cells equal size
    for (int i = 0; i < 2; i++) {
      GridPane.setHgrow(grid.getChildren().get(i), Priority.ALWAYS);
      GridPane.setVgrow(grid.getChildren().get(i), Priority.ALWAYS);
    }

    return grid;
  }

  /**
   * Creates a styled action button with centered icon and text.
   *
   * @param text   Label text for the button
   * @param icon   Icon to display above the text
   * @param action Event handler for button click
   * @return A StackPane containing the styled button
   */
  private StackPane createActionButton(String text, FontIcon icon,
                                       javafx.event.EventHandler<javafx.scene.input.MouseEvent> action) {
    // container for the button
    StackPane button = new StackPane();
    button.setStyle(
        "-fx-background-color: transparent; -fx-border-color: -color-border-default; -fx-border-radius: 4px;");
    button.setPadding(new Insets(15));
    button.setCursor(javafx.scene.Cursor.HAND);

    VBox content = new VBox(10);
    content.setAlignment(Pos.CENTER);
    // icon
    icon.setIconSize(ICON_SIZE);
    icon.setIconColor(Color.WHITE);

    Label label = new Label(text);
    label.setStyle("-fx-font-size: 14px;");
    label.setAlignment(Pos.CENTER);

    content.getChildren().addAll(icon, label);
    button.getChildren().add(content);

    // Add hover effect
    button.setOnMouseEntered(e -> {
      button.setStyle(
          "-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-radius: 4px;");
    });

    button.setOnMouseExited(e -> {
      button.setStyle(
          "-fx-background-color: transparent; -fx-border-color: -color-border-default; -fx-border-radius: 4px;");
    });

    // Click handler
    button.setOnMouseClicked(action);

    return button;
  }

  @Override
  protected void setupContainer() {
    super.setupContainer();
  }

  /**
   * Sets up event listeners for the widget.
   */
  private void setupListeners() {
    if (listenerSubscribed) {
      return;
    }
    PrincipesEventBus.getInstance()
        .subscribe(CloseModalEvent.class, this::onCreateTaskModalClose);
    listenerSubscribed = true;
  }

  /**
   * Removes event listeners for the widget.
   */
  private void tearDownListeners() {
    if (!listenerSubscribed) {
      return;
    }
    PrincipesEventBus.getInstance()
        .unsubscribe(CloseModalEvent.class, this::onCreateTaskModalClose);
    listenerSubscribed = false;
  }

  /**
   * Handles the event when the "create task modal" is closed.
   *
   * @param event The {@link CloseModalEvent} that occurred
   */
  private void onCreateTaskModalClose(CloseModalEvent event) {
    if (event.getData().getCallbackId().equals("create-task-quick-action")) {
      log.debug("Create task modal closed with status: {}, success: {} and data: {}",
          event.getData().getStatus(), event.getData().isSuccess(),
          event.getData().getResult());

      if (!event.getData().isSuccess() || event.getData().getResult() == null) {
        return;
      }

      TaskTemplateService service = new TaskTemplateService();
      service.createTask((CreateTaskRequest) event.getData().getResult(), List.of());
    }
  }

  @Override
  protected void onMount() {
    this.setupListeners();
  }

  @Override
  protected void onUnmount() {
    this.tearDownListeners();
  }
}