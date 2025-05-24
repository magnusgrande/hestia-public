package no.ntnu.principes.view.main;

import atlantafx.base.theme.Styles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Badge;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.event.navigation.OpenModalEvent;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.DeletionService;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.util.AlertUtil;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

@Slf4j
public class TaskDetailsView extends DashboardScreen {

  private final TaskAssignmentService taskAssignmentService;
  private TaskDto task;
  private List<TaskAssignmentDto> assignments;
  private VBox contentContainer;
  private final BooleanProperty deletionEnabledConfigProperty = new SimpleBooleanProperty(false);
  private final BooleanProperty userCreatedTaskProperty = new SimpleBooleanProperty(false);
  private final BooleanProperty taskDeletableStatus = new SimpleBooleanProperty(false);

  public TaskDetailsView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.taskAssignmentService = new TaskAssignmentService();
  }

  @Override
  protected void initializeScreen() {
    super.initializeScreen();

    // Create content container with increased spacing for better visual separation
    this.contentContainer = new VBox(30);
    StyleManager.padding(this.contentContainer, InsetBuilder.uniform(30).build());
    StyleManager.growHorizontal(this.contentContainer);

    // Add scrollpane to handle overflow content with improved styling
    ScrollPane scrollPane = new ScrollPane(this.contentContainer);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    StyleManager.growHorizontal(scrollPane);
    StyleManager.growVertical(scrollPane);

    // Focus traversal setup for keyboard navigation
    scrollPane.setFocusTraversable(true);

    this.content.getChildren().add(scrollPane);
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();

    // Set up event listeners
    PrincipesEventBus.getInstance()
        .subscribe(CloseModalEvent.class, this::onEditTaskModalResult);

    // Get task ID from context
    Long taskId = this.getContext().getParameter("taskId");
    if (taskId == null) {
      log.error("No task ID provided");
      AlertUtil.error("Error: No task ID was provided");
      NavigationService.back();
      return;
    }

    this.loadTaskDetails(taskId);
  }

  @Override
  protected void onNavigatedFrom() {
    super.onNavigatedFrom();

    // Clean up event listeners
    PrincipesEventBus.getInstance()
        .unsubscribe(CloseModalEvent.class, this::onEditTaskModalResult);
  }


  private void loadTaskDetails(Long taskId) {
    // Show loading indicator
    showLoadingState(true);

    try {
      // Get all tasks
      List<TaskAssignmentDto> allAssignments = this.taskAssignmentService.getAllTasks();

      // Filter assignments for this task
      this.assignments = allAssignments.stream()
          .filter(a -> a.getTask().getId().equals(taskId))
          .collect(Collectors.toList());

      if (this.assignments.isEmpty()) {
        log.error("Task not found: {}", taskId);
        AlertUtil.error("Task not found");
        NavigationService.back();
        return;
      }

      this.task = this.assignments.getFirst().getTask();

      // Update window title with page context for better accessibility
      this.controller.getStage().setTitle("Task Details: " + this.task.getName());

      // properties
      this.userCreatedTaskProperty.setValue(
          this.task.getCreatedBy().getId().equals(Auth.getInstance().getProfileId()));
      this.taskDeletableStatus.setValue(
          this.assignments.stream().noneMatch(t -> t.getStatus() == TaskStatus.DONE));

      // Clear and rebuild UI
      this.contentContainer.getChildren().clear();
      this.buildTaskDetailUI();
    } catch (Exception e) {
      log.error("Error loading task details", e);
      AlertUtil.error("Error loading task details: " + e.getMessage());
    } finally {
      showLoadingState(false);
    }
  }

  private void showLoadingState(boolean isLoading) {
    if (isLoading) {
      // Create and show loading indicator
      VBox loadingBox = new VBox(10);
      loadingBox.setAlignment(Pos.CENTER);
      loadingBox.setPadding(new Insets(30));

      Text loadingText = new Text("Loading task details...", Text.TextType.BODY);
      loadingBox.getChildren().add(loadingText);

      this.contentContainer.getChildren().clear();
      this.contentContainer.getChildren().add(loadingBox);
    }
  }

  private void buildTaskDetailUI() {
    // Add back button at the top with clear labeling
    Button backButton = new Button("Back to Tasks", Button.ButtonType.OUTLINED);
    backButton.setOnAction(e -> NavigationService.back());
    backButton.setFocusTraversable(true);

    // Add icon or visual indicator for back action
    backButton.setIcon(
        new FontIcon(Material2AL.ARROW_BACK)
    );
    backButton.setAccessibleText("Return to task list");

    this.contentContainer.getChildren().add(backButton);

    // Task header section
    this.createTaskHeader();

    // Add separator with semantic meaning
    Separator headerSeparator = createAccessibleSeparator("Task details section begins");
    this.contentContainer.getChildren().add(headerSeparator);

    // Task attributes section
    this.createTaskAttributes();

    // Add separator
    Separator assignmentSeparator = createAccessibleSeparator("Assignments section begins");
    this.contentContainer.getChildren().add(assignmentSeparator);

    // Assignment section
    this.createAssignmentSection();

    // Action buttons
    this.createActionButtons();
  }

  /**
   * Creates a separator with accessibility attributes
   */
  private Separator createAccessibleSeparator(String description) {
    Separator separator = new Separator();
    separator.getStyleClass().add("section-separator");
    return separator;
  }

  private void createTaskHeader() {
    VBox headerSection = new VBox(15);
    headerSection.setPadding(new Insets(15, 0, 25, 0));
    headerSection.getStyleClass().add("task-header-section");

    // Create heading for better semantic structure
    Text taskTitle = new Text(this.task.getName(), Text.TextType.PAGE_TITLE);

    // Add visual indication of importance
    Rectangle titleHighlight = new Rectangle(4, 30, Color.valueOf("#3b82f6"));
    titleHighlight.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 2px;");

    HBox titleContainer = new HBox(10);
    titleContainer.setAlignment(Pos.CENTER_LEFT);
    titleContainer.getChildren().addAll(titleHighlight, taskTitle);

    headerSection.getChildren().add(titleContainer);

    // Only add description if it's not empty
    if (this.task.getDescription() != null && !this.task.getDescription().trim().isEmpty()) {
      Text taskDescription = new Text(this.task.getDescription(), Text.TextType.BODY);
      taskDescription.setWrapText(true);

      // Create description container with proper indentation
      VBox descriptionContainer = new VBox(taskDescription);
      descriptionContainer.setPadding(new Insets(0, 0, 0, 14));

      headerSection.getChildren().add(descriptionContainer);
    }

    this.contentContainer.getChildren().add(headerSection);
  }

  private String titleCase(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

  private Badge.Variant weightToVariant(int weight) {
    if (weight <= 3) {
      return Badge.Variant.SUCCESS;
    } else if (weight == 4) {
      return Badge.Variant.WARNING;
    } else {
      return Badge.Variant.DANGER;
    }
  }

  private void createTaskAttributes() {
    VBox attributesSection = new VBox(20);
    attributesSection.setPadding(new Insets(15, 0, 15, 0));
    attributesSection.getStyleClass().add("task-attributes-section");

    // Create semantically meaningful section header
    Text sectionTitle = new Text("Task Details", Text.TextType.SECTION_HEADER);
    attributesSection.getChildren().add(sectionTitle);

    // Create a container for the task properties with visual grouping
    VBox propertiesContainer = new VBox(15);
    propertiesContainer.setPadding(new Insets(15));
    propertiesContainer.setStyle(
        "-fx-background-color: #f9fafb;" +
            "-fx-border-color: #e5e7eb;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
    );

    // Create rows for different attributes with improved layout
    HBox difficultyRow = createPropertyRow("Difficulty:",
        this.task.getWorkWeight().toString(),
        this.weightToVariant(this.task.getWorkWeight().getValue()));

    HBox timeRow = createPropertyRow("Estimated Time:",
        this.task.getTimeWeight().toString(),
        this.weightToVariant(this.task.getTimeWeight().getValue()));

    // Creation info with better formatting
    HBox creationRow = createInfoRow("Created By:",
        this.task.getCreatedBy() != null ? this.task.getCreatedBy().getName() : "Unknown");

    HBox createdAtRow = createInfoRow("Created At:",
        this.formatDateTime(this.task.getCreatedAt()));

    // Add recurring info if applicable
    if (this.task.isRecurring()) {
      HBox recurringRow = createInfoRow("Recurrence:",
          "Every " + this.task.getRecurrenceIntervalDays() + " days");
      propertiesContainer.getChildren().add(recurringRow);
    }

    // Add all rows to container
    propertiesContainer.getChildren().addAll(
        difficultyRow,
        timeRow,
        new Separator(),
        creationRow,
        createdAtRow
    );

    attributesSection.getChildren().add(propertiesContainer);
    this.contentContainer.getChildren().add(attributesSection);
  }

  /**
   * Creates a property row with a badge indicator
   */
  private HBox createPropertyRow(String labelText, String valueText, Badge.Variant variant) {
    HBox row = new HBox(15);
    row.setAlignment(Pos.CENTER_LEFT);

    Text label = new Text(labelText, Text.TextType.FORM_LABEL);
    label.setStyle("-fx-font-weight: 600;");

    Badge badge = new Badge(titleCase(valueText), variant);

    // Enhanced styling for better visibility
    badge.getStyleClass().add("enhanced-badge");

    row.getChildren().addAll(label, badge);
    return row;
  }

  /**
   * Creates an information row with label and value
   */
  private HBox createInfoRow(String labelText, String valueText) {
    HBox row = new HBox(15);
    row.setAlignment(Pos.CENTER_LEFT);

    Text label = new Text(labelText, Text.TextType.FORM_LABEL);
    label.setStyle("-fx-font-weight: 600;");

    Text value = new Text(valueText, Text.TextType.BODY);

    row.getChildren().addAll(label, value);
    return row;
  }

  private void createAssignmentSection() {
    VBox assignmentSection = new VBox(20);
    assignmentSection.setPadding(new Insets(20, 0, 20, 0));
    assignmentSection.getStyleClass().add("assignment-section");

    // Create semantically meaningful section header
    Text sectionTitle = new Text("Assignments", Text.TextType.SECTION_HEADER);
    assignmentSection.getChildren().add(sectionTitle);

    // Create a container for assignments
    VBox assignmentsContainer = new VBox(15);

    // Add description if there are multiple assignments
    if (this.assignments.size() > 1) {
      Text description = new Text("This task has " + this.assignments.size() + " assignments.",
          Text.TextType.BODY);
      assignmentSection.getChildren().add(description);
    }

    // Create a row for each assignment with enhanced visual design
    for (TaskAssignmentDto assignment : this.assignments) {
      VBox assignmentBox = new VBox(15);
      assignmentBox.setPadding(new Insets(20));
      assignmentBox.setStyle(
          "-fx-background-color: #ffffff;" +
              "-fx-border-color: #e5e7eb;" +
              "-fx-border-width: 1px;" +
              "-fx-border-radius: 8px;" +
              "-fx-background-radius: 8px;" +
              "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 4, 0, 0, 1);"
      );

      // Add a status indicator at the top of the card for quick visual reference
      HBox statusIndicator = new HBox();
      statusIndicator.setPrefHeight(4);
      statusIndicator.setMaxWidth(Double.MAX_VALUE);
      String statusColor = getStatusColor(assignment.getStatus());
      statusIndicator.setStyle("-fx-background-color: " + statusColor + ";" +
          "-fx-background-radius: 4px 4px 0 0;");

      // Make indicator semantically meaningful for screen readers
      statusIndicator.setAccessibleText("Assignment status: " + assignment.getStatus().toString());

      // Assignee and status in one row with enhanced layout
      HBox topRow = new HBox(20);
      topRow.setAlignment(Pos.CENTER_LEFT);

      // Assignee name with icon
      VBox assigneeInfo = new VBox(8);
      Text assigneeLabel = new Text("Assigned To:", Text.TextType.FORM_LABEL);
      assigneeLabel.setStyle("-fx-font-weight: 600;");

      HBox assigneeRow = new HBox(10);
      assigneeRow.setAlignment(Pos.CENTER_LEFT);

      // Add person icon for visual enhancement
      FontIcon personIcon = new FontIcon(Material2MZ.PERSON);
      Text assigneeName = new Text(
          assignment.getMember() != null ?
              assignment.getMember().getName() : "Unassigned",
          Text.TextType.BODY
      );
      assigneeRow.getChildren().addAll(personIcon, assigneeName);

      assigneeInfo.getChildren().addAll(assigneeLabel, assigneeRow);

      // Status with enhanced badge
      VBox statusInfo = new VBox(8);
      Text statusLabel = new Text("Status:", Text.TextType.FORM_LABEL);
      statusLabel.setStyle("-fx-font-weight: 600;");

      Badge statusBadge = new Badge(
          assignment.getStatus().toString(),
          this.statusToVariant(assignment.getStatus())
      );
      // Enhance badge visibility
      statusBadge.setStyle("-fx-font-weight: 600; -fx-padding: 5 10;");

      statusInfo.getChildren().addAll(statusLabel, statusBadge);

      topRow.getChildren().addAll(assigneeInfo, statusInfo);
      HBox.setHgrow(assigneeInfo, Priority.ALWAYS);

      // Dates in second row with improved layout
      HBox dateRow = new HBox(20);
      dateRow.setAlignment(Pos.CENTER_LEFT);
      dateRow.setPadding(new Insets(10, 0, 0, 0));

      // Due date with calendar icon
      VBox dueInfo = createDateInfoBox("Due Date:",
          assignment.getDueAt() != null ? this.formatDateTime(assignment.getDueAt()) :
              "No due date",
          Material2AL.CALENDAR_TODAY);

      // Assignment date
      VBox assignedInfo = createDateInfoBox("Assigned At:",
          assignment.getAssignedAt() != null ? this.formatDateTime(assignment.getAssignedAt()) :
              "N/A",
          Material2AL.HOURGLASS_BOTTOM);

      dateRow.getChildren().addAll(dueInfo, assignedInfo);
      HBox.setHgrow(dueInfo, Priority.ALWAYS);

      // Completion info if completed with check icon
      if (assignment.getStatus() == TaskStatus.DONE) {
        VBox completionInfo = createDateInfoBox("Completed At:",
            this.formatDateTime(assignment.getCompletedAt()),
            Material2AL.CHECK_CIRCLE);

        dateRow.getChildren().add(completionInfo);
        HBox.setHgrow(completionInfo, Priority.ALWAYS);
      }

      // Add rows to assignment box
      assignmentBox.getChildren().addAll(statusIndicator, topRow, dateRow);

      // Add the assignment box to the assignments container
      assignmentsContainer.getChildren().add(assignmentBox);
    }

    // Add assignments container to section
    assignmentSection.getChildren().add(assignmentsContainer);
    this.contentContainer.getChildren().add(assignmentSection);
  }

  /**
   * Creates a date information box with icon
   */
  private VBox createDateInfoBox(String labelText, String dateText, Ikon icon) {
    VBox infoBox = new VBox(8);

    Text label = new Text(labelText, Text.TextType.FORM_LABEL);
    label.setStyle("-fx-font-weight: 600;");

    HBox valueRow = new HBox(10);
    valueRow.setAlignment(Pos.CENTER_LEFT);

    Node iconNode = new FontIcon(icon);
    Text value = new Text(dateText, Text.TextType.BODY);

    valueRow.getChildren().addAll(iconNode, value);
    infoBox.getChildren().addAll(label, valueRow);

    return infoBox;
  }

  /**
   * Returns a color string for a task status
   */
  private String getStatusColor(TaskStatus status) {
    return switch (status) {
      case DONE -> "#16a34a";      // Green
      case CANCELLED -> "#dc2626"; // Red
      case TODO -> "#f59e0b"; // Amber
      default -> "#3b82f6";        // Blue
    };
  }

  private Badge.Variant statusToVariant(TaskStatus status) {
    return switch (status) {
      case DONE -> Badge.Variant.SUCCESS;
      case CANCELLED -> Badge.Variant.DANGER;
      case TODO -> Badge.Variant.WARNING;
      default -> Badge.Variant.INFO;
    };
  }

  private void createActionButtons() {
    VBox actionSection = new VBox(15);
    actionSection.setId("action-buttons");
    actionSection.setPadding(new Insets(20, 0, 30, 0));

    // Add heading for better semantics
    Text actionTitle = new Text("Actions", Text.TextType.SECTION_HEADER);
    actionTitle.setAccessibleRole(AccessibleRole.TEXT);

    // Container for buttons with visual separation
    HBox buttonContainer = new HBox(15);
    buttonContainer.setAlignment(Pos.CENTER_RIGHT);
    buttonContainer.setPadding(new Insets(15));
    buttonContainer.setStyle(
        "-fx-background-color: #f9fafb;" +
            "-fx-border-color: #e5e7eb;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
    );

    // Delete button with improved visual warning
    Button deleteButton = new Button("Delete Task", Button.ButtonType.FLAT);
    deleteButton.setIcon(
        new FontIcon(Material2AL.DELETE)
    );
    deleteButton.setOnAction(e -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Delete Task");
      confirm.setHeaderText(
          "Are you sure you want to delete the '" + this.task.getName() + "' task?");
      confirm.setContentText(
          "This action cannot be undone. All task assignments and awarded points will also be deleted.");

      // Make the dialog more accessible
      Label warningLabel = new Label("⚠️ Warning: This is a permanent action");
      warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #b91c1c;");
      confirm.getDialogPane().setExpandableContent(warningLabel);
      confirm.getDialogPane().setExpanded(true);

      confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          this.deleteTask(this.task.getId());
        }
      });
    });
    StyleManager.apply(deleteButton, Styles.DANGER);

    // Bind to config
    new ConfigValueBinder(
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class))
        .bindBoolean("settings.allow_delete", false, this.deletionEnabledConfigProperty);

    // Bind deletion button to deletionEnabledForAllProperty
    BooleanBinding deletable = this.deletionEnabledConfigProperty.or(
        userCreatedTaskProperty.and(taskDeletableStatus)
    );
    deleteButton.disableProperty().bind(deletable.not());

    // Enhanced tooltip with clearer messaging
    Tooltip deleteTooltip = new Tooltip();
    deleteTooltip.textProperty().bind(Bindings.createStringBinding(() -> {
      if (deletable.get()) {
        return "Delete this task permanently";
      } else if (userCreatedTaskProperty.get()) {
        return "You cannot delete this task because it has been completed";
      } else {
        return "Only the task creator can delete this task";
      }
    }, deletable, userCreatedTaskProperty, taskDeletableStatus));
    deleteTooltip.setStyle("-fx-font-size: 12px; -fx-padding: 8px;");
    Tooltip.install(deleteButton, deleteTooltip);

    // Set ARIA attributes for accessibility
    deleteButton.setAccessibleText("Delete task permanently");

    buttonContainer.getChildren().add(deleteButton);

    // Edit button with icon
    Button editButton = new Button("Edit Task", Button.ButtonType.OUTLINED);
    editButton.setIcon(
        new FontIcon(Material2AL.EDIT)
    );
    editButton.setOnAction(e -> this.openEditTaskModal());
    editButton.setAccessibleText("Edit task details");
    buttonContainer.getChildren().add(editButton);

    // Determine current user's assignment, if any
    Optional<TaskAssignmentDto> userAssignment = this.assignments.stream()
        .filter(a -> a.getMember() != null &&
            Objects.equals(a.getMember().getId(), Auth.getInstance().getProfileId()))
        .findFirst();

    // Create action buttons based on user's role and task status with clearer visual hierarchy
    if (userAssignment.isPresent()) {
      TaskAssignmentDto assignment = userAssignment.get();

      if (assignment.getStatus() == TaskStatus.TODO) {
        // Complete button with icon
        Button completeButton = new Button("Mark as Done", Button.ButtonType.DEFAULT);
        completeButton.setIcon(
            new FontIcon(Material2AL.CHECK)
        );
        completeButton.setOnAction(e -> this.completeTask(assignment.getId()));

        // Enhance button appearance to make it stand out as primary action
        completeButton.getStyleClass().add("primary-action-button");
        completeButton.setStyle("-fx-font-weight: bold;");

        completeButton.setAccessibleText("Mark task as completed");
        buttonContainer.getChildren().add(completeButton);
      } else if (assignment.getStatus() == TaskStatus.DONE) {
        // Uncomplete button with icon
        Button uncompleteButton = new Button("Mark as Todo", Button.ButtonType.OUTLINED);
        uncompleteButton.setIcon(
            new FontIcon(Material2MZ.UNDO)
        );
        uncompleteButton.setOnAction(e -> this.uncompleteTask(assignment.getId()));
        uncompleteButton.setAccessibleText("Mark task as not completed");
        buttonContainer.getChildren().add(uncompleteButton);
      }
    } else {
      // Assign to me button with icon
      Button assignButton = new Button("Assign to Me", Button.ButtonType.DEFAULT);
      assignButton.setIcon(
          new FontIcon(Material2MZ.PLUS)
      );
      assignButton.setOnAction(e -> this.assignTask(this.task.getId()));

      // Enhance button appearance to make it stand out as primary action
      assignButton.getStyleClass().add("primary-action-button");
      assignButton.setStyle("-fx-font-weight: bold;");

      assignButton.setAccessibleText("Assign this task to yourself");
      buttonContainer.getChildren().add(assignButton);
    }

    actionSection.getChildren().addAll(actionTitle, buttonContainer);
    this.content.getChildren().remove(this.content.lookup("#action-buttons"));
    this.content.getChildren().add(actionSection);
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "N/A";
    }

    DateTimeFormatter formatter;
    if (dateTime.getYear() != LocalDateTime.now().getYear()) {
      formatter = DateTimeFormatter.ofPattern("E dd MMM yyyy HH:mm");
    } else {
      formatter = DateTimeFormatter.ofPattern("E dd MMM HH:mm");
    }

    return dateTime.format(formatter);
  }

  private void completeTask(Long assignmentId) {
    try {
      this.taskAssignmentService.completeTask(assignmentId);
      AlertUtil.success("Task marked as completed successfully");
      this.loadTaskDetails(this.task.getId());

    } catch (Exception e) {
      log.error("Error completing task", e);
      AlertUtil.error("Error: Could not complete task");
    }
  }

  private void uncompleteTask(Long assignmentId) {
    try {
      this.taskAssignmentService.uncompleteTask(assignmentId);
      AlertUtil.success("Task marked as todo");
      this.loadTaskDetails(this.task.getId());

    } catch (Exception e) {
      log.error("Error uncompleting task", e);
      AlertUtil.error("Error: Could not update task status");
    }
  }

  private void assignTask(Long taskId) {
    try {
      this.taskAssignmentService.assignTask(
          taskId,
          Auth.getInstance().getProfileId()
      );
      AlertUtil.success("Task assigned to you successfully");
      this.loadTaskDetails(taskId);

    } catch (Exception e) {
      log.error("Error assigning task", e);
      AlertUtil.error("Error: Could not assign task");
    }
  }

  private void openEditTaskModal() {
    // Open edit task modal with current task data
    PrincipesEventBus.getInstance().publish(
        OpenModalEvent.of("editTaskModal", "editTask", Map.of(
            "taskId", this.task.getId(),
            "task", this.task,
            "assignments", this.assignments
        ))
    );
  }

  private void deleteTask(Long taskId) {
    try {
      DeletionService.deleteTask(taskId);
      AlertUtil.success("Task deleted successfully");
      NavigationService.back();

    } catch (Exception e) {
      log.error("Error deleting task", e);
      AlertUtil.error("Error: Could not delete task");
    }
  }

  private void onEditTaskModalResult(CloseModalEvent event) {
    if (event.getData().getCallbackId().equals("editTask") && event.getData().isSuccess()) {
      // Reload task details after successful edit
      AlertUtil.success("Task updated successfully");
      this.loadTaskDetails(this.task.getId());

    }
  }
}