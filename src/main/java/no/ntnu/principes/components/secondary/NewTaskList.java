package no.ntnu.principes.components.secondary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.components.primary.Badge;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.DeletionService;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.jetbrains.annotations.NotNull;

/**
 * A JavaFX component for displaying and managing a task list, where tasks can be assigned,
 * marked as done, and information such as difficulty, estimated time, and due dates is shown.
 *
 * <p>This class extends {@link BaseComponent} and provides a table-like user interface for
 * visualizing tasks that can dynamically update through actions like assignment or completion.
 * It relies on services to fetch and manipulate task data.</p>
 */
@Slf4j
public class NewTaskList extends BaseComponent {
  private final ObservableList<TaskDto> tasks = FXCollections.observableArrayList();
  private final ObservableList<TaskDto> tasksFiltered = FXCollections.observableArrayList();
  private final Supplier<List<TaskAssignmentDto>> taskSupplier;
  private final TaskAssignmentService taskAssigmentService = new TaskAssignmentService();
  private final StringProperty query = new SimpleStringProperty("");
  @Getter
  @Setter
  private TaskStatus taskStatus;

  /**
   * Initializes a new task list component with a specific identifier, a parent screen,
   * and a supplier for dynamically fetching task data.
   *
   * <p>This component is used to display a list of task assignments on a
   * given user interface screen. The {@code taskSupplier} parameter provides a way
   * to retrieve task data on demand, allowing the task list to reflect the most up-to-date
   * data whenever necessary.</p>
   *
   * @param componentId  A unique identifier for the task list component. It must be a non-null
   *                     string  that uniquely identifies this instance among other UI components.
   * @param parentScreen The parent screen or container to which this task list belongs.
   *                     This parameter must be non-null and indicates the hierarchical
   *                     relationship in the UI.
   * @param taskSupplier A supplier function that returns a {@code List} of
   *                     {@code TaskAssignmentDTO}, representing the tasks to be displayed in the
   *                     task list. This parameter must be non-null and should provide
   *                     meaningful data; otherwise, the task list may be empty or invalid.
   */
  public NewTaskList(String componentId, BaseScreen parentScreen,
                     Supplier<List<TaskAssignmentDto>> taskSupplier) {
    super(componentId, parentScreen);
    this.taskSupplier = taskSupplier;
  }

  @NotNull
  private static TableColumn<TaskDto, String> getAssigneesCol() {
    TableColumn<TaskDto, String> assigneesCol = new TableColumn<>("Assignees");
    assigneesCol.setCellValueFactory(
        c -> {
          if (!c.getValue().getAssignments().isEmpty()) {
            return new SimpleStringProperty(String.join(", ",
                c.getValue().getAssignments()
                    .stream()
                    .filter(a -> a.getMember() != null)
                    .map(a -> a.getMember().getName())
                    .distinct()
                    .toList()));
          }
          return new SimpleStringProperty("Unassigned");
        }
    );
    return assigneesCol;
  }

  /**
   * Converts a given string to title case, capitalizing the first character and converting
   * the rest to lowercase.
   *
   * @param str The input string to be converted. Must not be null or empty.
   *            If null, a {@code NullPointerException} is thrown.
   *            If empty, the method may throw an exception or return undefined behavior.
   * @return The converted string in title case format. This will always have an uppercase
   * first character and the rest in lowercase.
   */
  private String titleCase(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

  /**
   * Formats a {@link LocalDateTime} object into a string based on its year in relation to the
   * current year.
   *
   * <p>If the input datetime is from a different year than the current year, the output format
   * includes the year.
   * Otherwise, the year is omitted from the format to simplify the representation.
   * </p>
   *
   * @param datetime The {@link LocalDateTime} object to be formatted. Must not be null.
   * @return A formatted string representation of the given {@link LocalDateTime}.
   * The format is {@code "E dd MMM yyyy HH:mm"} when the year differs from the current year.
   * It is {@code "E dd MMM HH:mm"} when the year matches the current year.
   */
  private String formatDate(LocalDateTime datetime) {
    if (datetime.getYear() != LocalDateTime.now().getYear()) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd MMM yyyy HH:mm");
      return datetime.format(formatter);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd MMM HH:mm");
    return datetime.format(formatter);
  }

  /**
   * Maps an integer weight to a corresponding badge variant.
   *
   * <p>This method determines the badge variant based on the input value:
   * <ul>
   *   <li>Returns {@code Badge.Variant.SUCCESS} if the weight is less than or equal to 3.</li>
   *   <li>Returns {@code Badge.Variant.WARNING} if the weight equals 4.</li>
   *   <li>Returns {@code Badge.Variant.DANGER} for all other weight values.</li>
   * </ul>
   * </p>
   *
   * @param weight An integer representing the weight to evaluate.
   * @return The badge variant corresponding to the given weight
   */
  private Badge.Variant weightToVariant(int weight) {
    if (weight <= 3) {
      return Badge.Variant.SUCCESS;
    } else if (weight == 4) {
      return Badge.Variant.WARNING;
    } else {
      return Badge.Variant.DANGER;
    }
  }

  /**
   * Initializes and configures a {@link TableView} component to display and interact with tasks.
   * The table includes columns for task details such as name, status, difficulty, estimated time,
   * due date, completion date, assignees, and available actions
   * (e.g., assigning or marking tasks as done).
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void initializeComponent() {
    TableColumn<TaskDto, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getName())
    );

    TableColumn<TaskDto, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        c -> {
          if (c.getValue().getAssignments().stream()
              .anyMatch(a -> a.getStatus() == TaskStatus.DONE)) {
            return new SimpleStringProperty("Done");
          }
          return new SimpleStringProperty("To-do");
        }
    );

    TableColumn<TaskDto, Node> difficultyCol =
        getDifficultyCol();

    TableColumn<TaskDto, Node> timeCol =
        getTimeEstimateCol();

    TableColumn<TaskDto, String> dueDateCol = new TableColumn<>("Due Date");
    dueDateCol.setCellValueFactory(
        c -> {
          if (c.getValue().getAssignments().getFirst().getDueAt() == null) {
            return new SimpleStringProperty("No due date");
          }
          return new SimpleStringProperty(
              formatDate(c.getValue().getAssignments().getFirst().getDueAt()));
        }
    );

    TableColumn<TaskDto, String> completedAtCol =
        getCompletedAtCol();

    TableColumn<TaskDto, String> assigneesCol =
        getAssigneesCol();
    TableColumn<TaskDto, Node> actionsCol =
        getActionsCol();


    TableView<TaskDto> table = new TableView<>(this.tasksFiltered);
    table.getColumns()
        .setAll(nameCol, difficultyCol, timeCol, dueDateCol, statusCol, completedAtCol,
            assigneesCol,
            actionsCol);
    for (TableColumn<TaskDto, ?> col : table.getColumns()) {
      col.setReorderable(false);
    }
    table.setRowFactory(tableView -> {
      TableRow<TaskDto> row = new TableRow<>() {
        private final ContextMenu contextMenu = new ContextMenu();
        private boolean menuInitialized = false;

        // Keep references to menu items to update their state
        private MenuItem autoAssignItem;
        private MenuItem assignToMeItem;
        private MenuItem assignToItem;
        private MenuItem deleteItem;
        private MenuItem viewDetailsItem;

        @Override
        protected void updateItem(TaskDto task, boolean empty) {
          super.updateItem(task, empty);

          if (empty || task == null) {
            setContextMenu(null);
            return;
          }

          if (!menuInitialized) {
            initializeContextMenu();
            menuInitialized = true;
          }

          // Update menu item states based on current task state
          updateContextMenuItems(task);
          setContextMenu(contextMenu);
        }

        private void initializeContextMenu() {
          // Clear any existing items
          contextMenu.getItems().clear();

          autoAssignItem = new MenuItem("Auto-assign");
          autoAssignItem.setId("autoAssign");

          assignToMeItem = new MenuItem("Assign to me");
          assignToMeItem.setId("assignToMe");

          assignToItem = new MenuItem("Assign to...");
          assignToItem.setId("assignTo");

          deleteItem = new MenuItem("Delete");
          deleteItem.setId("delete");

          viewDetailsItem = new MenuItem("View Details");
          viewDetailsItem.setId("viewDetails");

          contextMenu.getItems().addAll(
              viewDetailsItem,
              new SeparatorMenuItem(),
              autoAssignItem,
              assignToMeItem,
              assignToItem,
              new SeparatorMenuItem(),
              deleteItem
          );

          // action handlers
          autoAssignItem.setOnAction(e -> {
            TaskDto task = getItem();
            if (task != null) {
              taskAssigmentService.autoAssignTask(task.getId());
              refresh();
            }
          });

          assignToMeItem.setOnAction(e -> {
            TaskDto task = getItem();
            if (task != null) {
              taskAssigmentService.assignTask(task.getId(), Auth.getInstance().getProfileId());
              refresh();
            }
          });

          assignToItem.setOnAction(e -> {
            TaskDto task = getItem();
            if (task != null) {
              showAssignDialog(task);
            }
          });

          deleteItem.setOnAction(e -> {
            TaskDto task = getItem();
            if (task != null) {
              Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
              confirm.setTitle("Confirm Deletion");
              confirm.setHeaderText("Delete Task");
              confirm.setContentText(
                  "Are you sure you want to delete this task? All assignments and points related to this task will also be deleted.");

              confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                  DeletionService.deleteTask(task.getId());
                  refresh();
                }
              });
            }
          });

          viewDetailsItem.setOnAction(e -> {
            TaskDto task = getItem();
            if (task != null) {
              NavigationService.navigate("taskDetails", Map.of(
                  "taskId", task.getId()
              ));
            }
          });
        }

        private void updateContextMenuItems(TaskDto task) {
          Long currentUserId = Auth.getInstance().getProfileId();

          // Check if task is assigned to current user
          boolean isAssignedToCurrentUser = task.getAssignments().stream()
              .anyMatch(a -> a.getMember() != null &&
                  Objects.equals(a.getMember().getId(), currentUserId));

          // Check if task is completed by anyone
          boolean isTaskCompleted = task.getAssignments().stream()
              .anyMatch(a -> a.getStatus() == TaskStatus.DONE);

          // Check if current user created the task
          boolean isCreatedByCurrentUser = task.getCreatedBy() != null &&
              Objects.equals(task.getCreatedBy().getId(), currentUserId);

          ConfigValueRepository configRepo =
              DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
          boolean allowDelete =
              configRepo.getValueOrDefault("settings.allow_delete", false).get().getBooleanValue();

          // Update menu item states
          assignToMeItem.setDisable(isAssignedToCurrentUser || isTaskCompleted);
          autoAssignItem.setDisable(isTaskCompleted);
          assignToItem.setDisable(isTaskCompleted);

          // Allow delete if user created the task or has admin rights (later maybe..)
          boolean canDelete = allowDelete || isCreatedByCurrentUser;
          deleteItem.setDisable(!canDelete);
        }
      };
      row.setOnMouseClicked(me -> {
        if (me.getButton() == MouseButton.PRIMARY && me.getClickCount() == 2) {
          TaskDto task = row.getItem();
          if (task != null) {
            NavigationService.navigate("taskDetails", Map.of(
                "taskId", task.getId()
            ));
          }
        }
      });

      return row;
    });
    table.setColumnResizePolicy(
        TableView.CONSTRAINED_RESIZE_POLICY_NEXT_COLUMN
    );
    Text placeholder = new Text("No tasks match the filter", StyledText.TextType.SUBHEADER);
    table.setPlaceholder(placeholder);
    StyleManager.growHorizontal(table);
    StyleManager.margin(table, InsetBuilder.create().bottom(20).build())
        .growVertical(Priority.ALWAYS);
    StyleManager.padding(this, InsetBuilder.create().bottom(20).build())
        .growVertical(Priority.ALWAYS);
    StyleManager.padding(this, InsetBuilder.create().right(20).build());
    TextField searchField = new TextField();
    searchField.setPromptText("Search tasks");
    searchField.textProperty().bindBidirectional(this.query);
    searchField.textProperty().addListener((obs, old, newVal) -> {
      this.updateFilteredTasks();
    });
    VBox content = new VBox(10, searchField, table);
    StyleManager.grow(content);
    this.getChildren().addAll(content);
  }

  private void showAssignDialog(TaskDto task) {
    Dialog<Pair<Long, LocalDateTime>> dialog = new Dialog<>();
    dialog.setTitle("Assign Task");
    dialog.setHeaderText("Assign task to user");

    ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    ComboBox<Profile> userCombo = new ComboBox<>();
    userCombo.setCellFactory(listView -> new ProfileRowInCombobox());
    userCombo.setButtonCell(new ProfileRowInCombobox());

    // Load all profiles
    MemberRepository memberRepository =
        DatabaseManager.getInstance().getRepository(MemberRepository.class);
    List<Profile> profiles = memberRepository.findAll();
    userCombo.setItems(FXCollections.observableArrayList(profiles));

    DatePicker datePicker = new DatePicker(LocalDate.now());

    ComboBox<String> timeCombo = new ComboBox<>(FXCollections.observableArrayList(
        "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
        "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
        "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    ));
    timeCombo.getSelectionModel().select("16:00"); // Default to 4 PM

    // Add fields to layout
    grid.add(new Label("Assign to:"), 0, 0);
    grid.add(userCombo, 1, 0);
    grid.add(new Label("Due date:"), 0, 1);
    grid.add(datePicker, 1, 1);
    grid.add(new Label("Due time:"), 0, 2);
    grid.add(timeCombo, 1, 2);

    dialog.getDialogPane().setContent(grid);

    // Focus user combo box when dialog opens
    Platform.runLater(userCombo::requestFocus);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == assignButtonType) {
        Profile selectedUser = userCombo.getValue();

        if (selectedUser == null) {
          return null;
        }

        LocalDateTime dueDateTime = null;
        if (datePicker.getValue() != null) {
          String timeStr = timeCombo.getValue();
          LocalTime time = LocalTime.of(
              Integer.parseInt(timeStr.split(":")[0]),
              0
          );
          dueDateTime = LocalDateTime.of(datePicker.getValue(), time);
        }

        return new Pair<>(selectedUser.getId(), dueDateTime);
      }
      return null;
    });

    // Show dialog and process result
    Optional<Pair<Long, LocalDateTime>> result = dialog.showAndWait();

    result.ifPresent(pair -> {
      Long userId = pair.getKey();
      LocalDateTime dueDateTime = pair.getValue();

      TaskTemplateService taskTemplateService = new TaskTemplateService();
      CreateTaskRequest request = CreateTaskRequest.builder()
          .name(task.getName())
          .description(task.getDescription())
          .workWeight(task.getWorkWeight())
          .timeWeight(task.getTimeWeight())
          .dueAt(dueDateTime)
          .isRecurring(task.isRecurring())
          .recurrenceIntervalDays(task.getRecurrenceIntervalDays())
          .createdById(task.getCreatedBy().getId())
          .build();

      taskTemplateService.updateTask(task.getId(), request, List.of(userId));

      // Refresh table
      this.refresh();
    });
  }

  @NotNull
  private TableColumn<TaskDto, Node> getActionsCol() {
    TableColumn<TaskDto, Node> actionsCol = new TableColumn<>("Actions");
    actionsCol.setCellValueFactory(
        c -> {
          // Check if any assignments match the current user
          Optional<TaskAssignmentDto> assignment = c.getValue().getAssignments().stream()
              .filter(a -> a.getMember() != null
                  && Objects.equals(a.getMember().getId(), Auth.getInstance().getProfileId()))
              .findFirst();

          // Check if any assignment is done
          boolean isTaskDone = c.getValue().getAssignments().stream()
              .anyMatch(a -> a.getStatus() == TaskStatus.DONE);
          boolean isTaskDoneByCurrentUser = c.getValue().getAssignments().stream()
              .anyMatch(a -> a.getStatus() == TaskStatus.DONE
                  && Objects.equals(a.getMember().getId(), Auth.getInstance().getProfileId()));

          if (assignment.isPresent()) {
            if (isTaskDone && isTaskDoneByCurrentUser) {
              return new SimpleObjectProperty<>();
            }

            // Task assigned to current user but not done
            Button button = new Button("Mark Done", Button.ButtonType.OUTLINED);
            button.setPadding(InsetBuilder.symmetric(10, 0).build());
            button.setOnAction(e -> {
              log.debug("View task: {}", c.getValue().getName());
              taskAssigmentService.completeTask(assignment.get().getId());
              this.refresh();
            });
            return new SimpleObjectProperty<>(button);
          } else {
            if (isTaskDone) {
              // Task is done but not assigned to current user
              return new SimpleObjectProperty<>();
            }
            Button button = new Button("Assign to me", Button.ButtonType.OUTLINED);
            button.setPadding(InsetBuilder.symmetric(10, 0).build());
            button.setOnAction(e -> {
              log.debug("Claim task: {}", c.getValue().getName());
              taskAssigmentService.assignTask(c.getValue().getId(),
                  Auth.getInstance().getProfileId());
              this.refresh();
            });
            return new SimpleObjectProperty<>(button);
          }
        }
    );
    return actionsCol;
  }

  @NotNull
  private TableColumn<TaskDto, String> getCompletedAtCol() {
    TableColumn<TaskDto, String> completedAtCol = new TableColumn<>("Completed At");
    completedAtCol.setCellValueFactory(
        c -> {
          if (c.getValue().getAssignments().stream()
              .anyMatch(a -> a.getStatus() == TaskStatus.DONE)) {
            return new SimpleStringProperty(formatDate(c.getValue().getAssignments()
                .stream()
                .filter(a -> a.getStatus() == TaskStatus.DONE)
                .findFirst()
                .orElseThrow()
                .getCompletedAt()));
          }
          return new SimpleStringProperty("Not completed");
        }
    );
    return completedAtCol;
  }

  @NotNull
  private TableColumn<TaskDto, Node> getTimeEstimateCol() {
    TableColumn<TaskDto, Node> timeCol = new TableColumn<>("Estimated time");
    timeCol.setCellValueFactory(
        c -> {
          Badge badge =
              new Badge(titleCase(c.getValue().getTimeWeight().toString()),
                  weightToVariant(c.getValue().getTimeWeight().getValue()));
          VBox.setMargin(badge, InsetBuilder.symmetric(0, 5).build());
          badge.setStyle(
              "-fx-start-margin: 5px;-fx-end-margin: 5px;-fx-padding: 5px 0px 5px 0px;");
          return new SimpleObjectProperty<>(badge);
        }
    );
    return timeCol;
  }

  @NotNull
  private TableColumn<TaskDto, Node> getDifficultyCol() {
    TableColumn<TaskDto, Node> difficultyCol = new TableColumn<>("Difficulty");
    difficultyCol.setCellValueFactory(
        c -> {
          Badge badge =
              new Badge(titleCase(c.getValue().getWorkWeight().toString()),
                  weightToVariant(c.getValue().getWorkWeight().getValue()));
          VBox.setMargin(badge, InsetBuilder.symmetric(0, 5).build());
          badge.setStyle(
              "-fx-start-margin: 5px;-fx-end-margin: 5px;-fx-padding: 5px 0px 5px 0px;");
          return new SimpleObjectProperty<>(badge);
        }
    );
    return difficultyCol;
  }

  /**
   * Fetches tasks from the task supplier and updates the task list when the parent screen
   * mounts this component.
   */
  @Override
  protected void onMount() {
    this.fetchTasks();
  }

  /**
   * Converts a list of {@code TaskAssignmentDTO} to a list of {@code TaskDto}, grouping the
   * assignments by their respective tasks.
   *
   * @param assignmentDtos A list of {@code TaskAssignmentDTO} to group.
   *                       Each element represents an assignment linked to a task.
   *                       Must not be null. Can be empty.
   * @return A list of {@code TaskDto}, where each {@code TaskDto} represents a unique task and
   * includes all associated assignments grouped together. If no assignments match the
   * filtering criteria (when {@code taskStatus} is set), the resulting list may be empty.
   */
  private List<TaskDto> groupAssignmentsByTaskToDto(List<TaskAssignmentDto> assignmentDtos) {
    // Group assignments by both task ID and due date
    Map<String, List<TaskAssignmentDto>> groupedByTaskAndDueDate = new HashMap<>();

    for (TaskAssignmentDto assignment : assignmentDtos) {
      TaskDto task = assignment.getTask();
      // Create a unique key combining task ID and due date
      String key = task.getId() + "_" + assignment.getDueAt();

      // Add assignment to the appropriate group
      if (!groupedByTaskAndDueDate.containsKey(key)) {
        groupedByTaskAndDueDate.put(key, new ArrayList<>());
      }
      groupedByTaskAndDueDate.get(key).add(assignment);
    }

    // Create a TaskDto for each task+due date combination
    return groupedByTaskAndDueDate.values().stream()
        .map(assignments -> {
          TaskDto task = assignments.getFirst().getTask();
          return TaskDto.builder()
              .id(task.getId())
              .name(task.getName())
              .description(task.getDescription())
              .workWeight(task.getWorkWeight())
              .timeWeight(task.getTimeWeight())
              .createdBy(task.getCreatedBy())
              .createdAt(task.getCreatedAt())
              .isRecurring(task.isRecurring())
              .recurrenceIntervalDays(task.getRecurrenceIntervalDays())
              .assignments(assignments) // Only includes assignments with the same due date
              .build();
        })
        .filter(task -> {
          if (this.taskStatus == null) {
            return true;
          }
          return task.getAssignments().stream()
              .anyMatch(a -> a.getStatus() == this.taskStatus);
        })
        .toList();
  }

  /**
   * Updates the current list of tasks in the application by replacing the existing tasks
   * with a transformed version of the fetched tasks.
   *
   * <p>The method retrieves tasks using the {@code taskSupplier}, groups and transforms them to
   * a specific data transfer object (DTO) format via the internal method
   * {@code groupAssignmentsByTaskToDto()}, and finally updates the observable tasks list
   * with the new results.</p>
   *
   * <p>The existing tasks are completely replaced with the newly fetched tasks;
   * no existing task data is preserved.</p>
   *
   * <p>If {@code taskSupplier.get()} returns an empty or null list, the local tasks list
   * will become empty but remain valid.</p>
   */
  private void fetchTasks() {
    this.tasks.setAll(this.groupAssignmentsByTaskToDto(this.taskSupplier.get()));
    this.updateFilteredTasks();
  }

  private void updateFilteredTasks() {
    if (this.query.get().isEmpty()) {
      this.tasksFiltered.setAll(this.tasks);
      return;
    }
    this.tasksFiltered.setAll(this.tasks.filtered(
        task -> task.getName().toLowerCase().contains(this.query.get().toLowerCase())));
  }

  /**
   * Public method to refresh the task list by re-fetching the tasks from the supplier.
   */
  public void refresh() {
    this.fetchTasks();
  }

  @Override
  protected void onUnmount() {

  }

  @Override
  protected void onDestroy() {

  }
}
