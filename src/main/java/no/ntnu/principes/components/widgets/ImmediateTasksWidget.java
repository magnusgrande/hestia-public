package no.ntnu.principes.components.widgets;

import atlantafx.base.theme.Styles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import no.ntnu.principes.components.primary.Badge;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.StringUtils;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/**
 * Displays a widget showing tasks due for a specific date for the logged-in user.
 * If no tasks are due, the widget presents a message with an image indicating the user is caught
 * up.
 *
 * <p>The widget includes functionality to list tasks, highlight their status,
 * and allow toggling a task's completion state. Navigation buttons allow viewing
 * tasks for previous and next days.</p>
 */
public class ImmediateTasksWidget extends BaseWidget {
  // Component state
  private static final String EMPTY_TASK_IMAGE = "/no/ntnu/principes/images/happycharacter.png";
  private static final String EMPTY_TASK_IMAGE_DARK =
      "/no/ntnu/principes/images/happycharacter-darkmode.png";

  private final TaskAssignmentService taskAssignmentService;
  private final ScrollPane scrollPane = new ScrollPane();
  private final VBox innerContent = new VBox();

  // Date navigation
  private LocalDate selectedDate = LocalDate.now();
  private final Button prevDateButton = new Button();
  private final Button nextDateButton = new Button();
  private final Text dateLabel = new Text("", StyledText.TextType.SUBHEADER);
  private final BooleanProperty isToday = new SimpleBooleanProperty(true);

  /**
   * Creates an ImmediateTasksWidget component that displays tasks that are due for the selected date.
   * The widget is initialized with a corresponding screen context and a predefined header.
   *
   * @param screen the {@link BaseScreen} instance that serves as the parent for this widget.
   */
  public ImmediateTasksWidget(BaseScreen screen) {
    super("ImmediateTasksWidget", "", "You have 0 tasks due today", screen);
    this.taskAssignmentService = new TaskAssignmentService();
  }

  @Override
  protected void initializeComponent() {
    // Initialize date navigation buttons
    setupDateNavigation();

    // Set up content area
    this.scrollPane.setContent(this.innerContent);
    this.scrollPane.setFitToWidth(true);
    this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    this.scrollPane.setPadding(InsetBuilder.symmetric(10, 0).bottom(20).build());
    this.contentContainer.setAlignment(Pos.TOP_LEFT);
    this.contentContainer.getChildren().add(this.scrollPane);
    this.contentContainer.setMaxHeight(400);
    this.textContainer.setPadding(InsetBuilder.symmetric(20, 0).build());
    StyleManager.grow(this.scrollPane);
    this.innerContent.setAlignment(Pos.TOP_LEFT);

    // Update the date display
    updateDateDisplay();
  }

  private void updateIsToday() {
    isToday.set(selectedDate.equals(LocalDate.now()));
  }

  /**
   * Sets up the date navigation controls with previous and next buttons.
   */
  private void setupDateNavigation() {
    // Create navigation icons
    FontIcon prevIcon = new FontIcon(Material2AL.CHEVRON_LEFT);
    FontIcon nextIcon = new FontIcon(Material2AL.CHEVRON_RIGHT);

    // Configure buttons
    prevDateButton.setGraphic(prevIcon);
    nextDateButton.setGraphic(nextIcon);
    prevDateButton.getStyleClass().add(Styles.BUTTON_ICON);
    nextDateButton.getStyleClass().add(Styles.BUTTON_ICON);

    // Set up button actions
    prevDateButton.setOnAction(e -> {
      selectedDate = selectedDate.minusDays(1);
      updateIsToday();
      updateDateDisplay();
      refreshTasks();
    });

    nextDateButton.setOnAction(e -> {
      selectedDate = selectedDate.plusDays(1);
      updateIsToday();
      updateDateDisplay();
      refreshTasks();
    });
    HBox dateBox = new HBox(10, dateLabel);
    dateBox.setAlignment(Pos.CENTER);
    StyleManager.growHorizontal(dateBox);

    HBox dateNavigation = new HBox(5, prevDateButton, dateBox, nextDateButton);
    dateNavigation.setPadding(InsetBuilder.symmetric(10, 0).top(5).build());
    dateNavigation.setAlignment(Pos.CENTER);

    Button toTodayButton = new Button();
    toTodayButton.setGraphic(
        new FontIcon(Material2AL.CALENDAR_TODAY)
    );
    toTodayButton.getStyleClass().add(Styles.BUTTON_ICON);
    toTodayButton.setOnAction(e -> {
      selectedDate = LocalDate.now();
      updateIsToday();
      updateDateDisplay();
      refreshTasks();
    });
    toTodayButton.visibleProperty().bind(isToday.not());
    toTodayButton.managedProperty().bind(isToday.not());

    dateBox.getChildren().addFirst(toTodayButton);

    contentContainer.getChildren().addFirst(dateNavigation);
  }

  /**
   * Updates the header text and date label based on the selected date.
   */
  private void updateDateDisplay() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d");
    String dateText = selectedDate.format(formatter);
    dateLabel.setText(dateText);
  }

  /**
   * Refreshes the list of tasks displayed in the widget by querying and filtering tasks
   * assigned to the currently authenticated member for the selected date.
   *
   * <p>If no tasks are found, a placeholder view indicating no tasks is shown. Otherwise,
   * the tasks are rendered in the widget.</p>
   */
  private void refreshTasks() {
    List<TaskAssignmentDto> tasks = taskAssignmentService
        .getTasksForMemberDueAt(Auth.getInstance().getProfileId(), selectedDate);

    this.innerContent.getChildren().clear();
    long completeCount = tasks.stream().filter(TaskAssignmentDto::isCompleted).count();
    if (completeCount == 0) {
      this.setDescription("You have " + tasks.size() + " tasks due on this day");
    } else {
      this.setDescription(
          "You have " + tasks.size() + " tasks due on this day (" + completeCount + " completed)");
    }

    if (tasks.isEmpty()) {
      displayEmptyTasksView();
    } else {
      displayTasks(tasks);
    }
  }


  /**
   * Displays a placeholder view indicating there are no immediate tasks available.
   */
  private void displayEmptyTasksView() {
    VBox noTasksContainer = new VBox();
    noTasksContainer.setSpacing(10);
    noTasksContainer.setPadding(new Insets(20));
    noTasksContainer.setAlignment(Pos.CENTER);
    StyleManager.growHorizontal(noTasksContainer);


    Text noTasksText = new Text("You're all caught up", StyledText.TextType.BODY);
    HBox.setMargin(noTasksText, InsetBuilder.create().left(10).build());
    noTasksText.setStyle("-fx-font-size: 24px;");
    noTasksText.setTextAlignment(TextAlignment.CENTER);
    noTasksText.setPrefWidth(this.getWidth() - 40);
    noTasksText.setAlignment(Pos.CENTER);

    ImageView iconLight = createEmptyTasksIcon(false);
    ImageView iconDark = createEmptyTasksIcon(true);
    boolean isDarkMode = StyleManager.getThemeProvider().getTheme().equals("dark");
    iconLight.setVisible(!isDarkMode);
    StyleManager.getThemeProvider().themeProperty()
        .addListener((observable, oldValue, newValue) -> {
          iconLight.setVisible(newValue.equals("dark"));
          iconDark.setVisible(newValue.equals("light"));
        });
    StackPane iconPanel = new StackPane(iconLight, iconDark);
    iconPanel.setAlignment(Pos.CENTER);
    iconPanel.setPadding(InsetBuilder.create().top(10).bottom(10).build());
    iconPanel.setMaxHeight(200);
    iconPanel.setMaxWidth(200);
    noTasksContainer.getChildren().addAll(iconPanel, noTasksText);
    this.innerContent.getChildren().add(noTasksContainer);
  }

  /**
   * Creates an icon to be displayed when there are no immediate tasks available.
   *
   * @return an {@link ImageView} instance containing the styled "empty tasks" icon.
   */
  private ImageView createEmptyTasksIcon(boolean darkMode) {
    ImageView icon = new ImageView(
        Objects.requireNonNull(
                getClass().getResource(darkMode ? EMPTY_TASK_IMAGE_DARK : EMPTY_TASK_IMAGE))
            .toExternalForm());
    icon.setFitHeight(180);
    icon.setFitWidth(200);
    icon.setPreserveRatio(true);
    icon.setSmooth(true);
    icon.setCache(true);
    icon.setBlendMode(BlendMode.MULTIPLY);
    return icon;
  }

  /**
   * Populates the widget's content area with task rows generated from the provided list of tasks.
   *
   * <p>Each task in the list is converted into a visual row using {@link #createTaskRow} and added
   * to the {@code innerContent} container.</p>
   *
   * <p>If the task list is empty or null, this method performs no action. Existing content in the
   * {@code innerContent} container is preserved.</p>
   *
   * @param tasks the list of {@link TaskAssignmentDto} objects to display. Each object represents
   *              a task assignment with associated details such as due date and completion status.
   */
  private void displayTasks(List<TaskAssignmentDto> tasks) {
    Iterator<TaskAssignmentDto> iterator = tasks.iterator();
    while (iterator.hasNext()) {
      TaskAssignmentDto assignment = iterator.next();
      this.innerContent.getChildren().add(createTaskRow(assignment));
      if (iterator.hasNext()) {
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(InsetBuilder.symmetric(0, 10).build());
        this.innerContent.getChildren().add(separator);
      }
    }
  }

  /**
   * Generates an icon representing the status of a task based on its due date and current status.
   *
   * <p>If the task has no due date, a default "in progress" icon is returned. For tasks
   * with a due date, the icon varies depending on whether the task is overdue or its current
   * status:
   * <ul>
   *   <li>TODO: Returns a "warning" icon if overdue; otherwise, returns "in progress" icon.</li>
   *   <li>DONE: Returns a "completed" icon.</li>
   *   <li>CANCELLED: Returns a "canceled" icon.</li>
   *   <li>Unknown or unsupported statuses: Returns "in progress" icon.</li>
   * </ul>
   *
   * @param assignment the {@link TaskAssignmentDto} containing the task's current status
   *                   and due date.
   * @return the {@link Ikon} representing the task's status icon. Defaults to "in progress"
   * for unrecognized statuses or missing data.
   */
  private Ikon getIconForTaskStatus(TaskAssignmentDto assignment) {
    if (assignment == null || assignment.getDueAt() == null) {
      return Material2AL.HOURGLASS_TOP;
    }

    boolean isOverdue = LocalDateTime.now().isAfter(assignment.getDueAt());

    return switch (assignment.getStatus()) {
      case TaskStatus.TODO -> isOverdue ? Material2MZ.WARNING : Material2AL.HOURGLASS_TOP;
      case TaskStatus.DONE -> Material2AL.CHECK_CIRCLE;
      case TaskStatus.CANCELLED -> Material2AL.CLOSE;
      default -> Material2AL.HOURGLASS_TOP;
    };
  }

  /**
   * Creates a styled row in an {@link HBox} representing a task assignment, including its icon,
   * name, due date, and a checkbox to toggle completion status.
   *
   * <p>The method formats the display of the task name and due date based on the task's current
   * status. Completed tasks appear strikethrough, while others are styled differently based on
   * their proximity to the due date.
   * It also allows toggling the completion status of the task via a checkbox, which triggers
   * appropriate API calls and UI refresh.</p>
   *
   * @param assignment the {@link TaskAssignmentDto} containing the task details, such as name,
   *                   status, and due date. Must not be {@code null}.
   * @return an {@link HBox} containing the visual representation of the task assignment,
   * ready to be rendered in the UI.
   */
  private HBox createTaskRow(TaskAssignmentDto assignment) {
    HBox taskRow = new HBox();
    taskRow.setAlignment(Pos.CENTER_LEFT);
    taskRow.setSpacing(10);
    taskRow.setPadding(new Insets(15));

    taskRow.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 8px;");

    VBox contentContainer = new VBox(5);
    contentContainer.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(contentContainer, Priority.ALWAYS);

    // Task name at the top
    Label taskNameLabel = new Label(assignment.getTask().getName());
    taskNameLabel.getStyleClass().addAll(Styles.TEXT, Styles.TEXT_BOLD);
    taskNameLabel.setStyle("-fx-font-size: 16px;");

    // Status indicators row
    HBox statusRow = new HBox(10);
    statusRow.setAlignment(Pos.CENTER_LEFT);

    // Difficulty badge (Easy, Hard, Very hard)
    Badge difficultyBadge = new Badge(
        StringUtils.titleCase(assignment.getTask().getWorkWeight().toString()),
        Badge.Variant.getWeightVariant(assignment.getTask().getWorkWeight().getValue())
    );

    // Time estimate badge
    HBox timeContainer = new HBox(5);
    timeContainer.setAlignment(Pos.CENTER_LEFT);
    FontIcon clockIcon = new FontIcon(Material2MZ.SCHEDULE);
    clockIcon.getStyleClass().add(Styles.TEXT_MUTED);
    Label timeLabel = new Label(assignment.getTask().getTimeWeight().toString());
    timeLabel.getStyleClass().add(Styles.TEXT_MUTED);
    timeContainer.getChildren().addAll(clockIcon, timeLabel);

    // Due time container
    HBox dueContainer = new HBox(5);
    dueContainer.setAlignment(Pos.CENTER_LEFT);
    FontIcon calendarIcon = new FontIcon(Material2AL.EVENT);

    // Style the due date based on status
    String statusStyleClass = getDueDateStyleClass(assignment);
    calendarIcon.getStyleClass().add(statusStyleClass);

    // Format due date/time
    String dueDateText = formatDueDate(assignment.getDueAt());
    Label dueLabel = new Label(dueDateText);
    dueLabel.getStyleClass().add(statusStyleClass);

    dueContainer.getChildren().addAll(calendarIcon, dueLabel);

    // status indicators
    statusRow.getChildren().addAll(difficultyBadge, timeContainer, dueContainer);

    // Action button on the right side
    CheckBox completeTask = new CheckBox();
    completeTask.setSelected(assignment.getStatus() == TaskStatus.DONE);
    completeTask.onActionProperty().set(event -> {
      if (completeTask.isSelected()) {
        taskAssignmentService.completeTask(assignment.getId());
      } else {
        taskAssignmentService.uncompleteTask(assignment.getId());
      }
      Platform.runLater(this::refreshTasks);
    });

    // Assemble the layout
    FontIcon statusIcon = new FontIcon(this.getIconForTaskStatus(assignment));
    statusIcon.getStyleClass().add(getDueDateStyleClass(assignment));
    HBox taskLabelContianer =
        new HBox(5, statusIcon, taskNameLabel);
    taskLabelContianer.setAlignment(Pos.CENTER_LEFT);
    contentContainer.getChildren().add(taskLabelContianer);
    // If the task is overdue or due soon and not done, show an additional warning
    if (assignment.getStatus() != TaskStatus.DONE) {
      if (getDueDateStyleClass(assignment).equals(Styles.WARNING)) {
        Label warningLabel = new Label("Due soon!");
        warningLabel.getStyleClass().add(Styles.WARNING);
        contentContainer.getChildren().add(warningLabel);
      } else if (getDueDateStyleClass(assignment).equals(Styles.DANGER)) {
        Label overdueLabel = new Label("Overdue!");
        overdueLabel.getStyleClass().add(Styles.DANGER);
        contentContainer.getChildren().add(overdueLabel);
      }
    }
    contentContainer.getChildren().addAll(statusRow);
    taskRow.getChildren().addAll(contentContainer, completeTask);

    // Click handler for the entire row
    taskRow.setCursor(Cursor.HAND);
    taskRow.setOnMouseClicked(event -> {
      NavigationService.navigate("taskDetails", Map.of(
          "taskId", assignment.getTask().getId()
      ));
    });

    // Hover effect
    taskRow.setOnMouseEntered(event -> {
      taskRow.setStyle("-fx-background-color: -color-accent-1; -fx-background-radius: 8px;");
    });
    taskRow.setOnMouseExited(event -> {
      taskRow.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 8px;");
    });

    return taskRow;
  }

  /**
   * Determines the CSS style class to apply to the due date icon based on the task's status and
   * due date.
   *
   * <p>If the task is marked as DONE, a success style is applied. If the task is overdue, a danger
   * style is applied. If the task is due soon (within 24 hours), a warning style is applied.
   * Otherwise, a muted text style is applied.</p>
   *
   * @param assignment the {@link TaskAssignmentDto} containing the task's status and due date.
   * @return a {@link String} representing the CSS style class for the due date icon.
   */
  private String getDueDateStyleClass(TaskAssignmentDto assignment) {
    if (assignment.getStatus() == TaskStatus.DONE) {
      return Styles.SUCCESS;
    }

    LocalDateTime dueDate = assignment.getDueAt();
    if (dueDate == null) {
      return Styles.TEXT_MUTED;
    }

    LocalDateTime now = LocalDateTime.now();
    if (dueDate.isBefore(now)) {
      return Styles.DANGER;  // Overdue
    } else if (dueDate.isBefore(now.plusHours(24))) {
      return Styles.WARNING; // Due soon (within 24 hours)
    } else {
      return Styles.TEXT_MUTED;   // Not urgent
    }
  }

  /**
   * Formats the provided due date into a human-readable string based on its proximity to the
   * current date.
   *
   * <p>If the due date is today, only the time is returned, formatted as "HH:mm".
   * If the due date is tomorrow, the time is returned followed by the word "tomorrow".
   * For all other dates, the day and month are returned, * formatted as "dd.MM".
   * If the input is {@code null}, an empty string is returned.</p>
   *
   * @param dueDate the {@link LocalDateTime} to format. May be {@code null},
   *                in which case an empty string is returned.
   * @return a {@link String} representing the formatted due date,depending on its relation to the
   * current date.
   */
  private String formatDueDate(LocalDateTime dueDate) {
    if (dueDate == null) {
      return "";
    }

    LocalDate today = LocalDate.now();
    LocalDate tomorrow = today.plusDays(1);
    LocalDate dueDateDay = dueDate.toLocalDate();

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");

    if (dueDateDay.isEqual(today)) {
      return dueDate.format(timeFormatter);
    } else if (dueDateDay.isEqual(tomorrow)) {
      return dueDate.format(timeFormatter) + " tomorrow";
    } else {
      return dueDate.format(dateFormatter);
    }
  }

  @Override
  protected void onMount() {
    this.refreshTasks();
  }

  @Override
  protected void onUnmount() {
    // noop
  }

  @Override
  protected void onDestroy() {
    this.getChildren().clear();
  }

  /**
   * Refreshes the widget's task list based on the currently selected date.
   */
  @Override
  public boolean refresh() {
    this.refreshTasks();
    return true;
  }
}