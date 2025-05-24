package no.ntnu.principes.components.widgets;

import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Badge;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.AlertUtil;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.StringUtils;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/**
 * Displays a widget showing tasks that have been created but not yet assigned to any user.
 * If no unassigned tasks exist, the widget is hidden.
 */
@Slf4j
public class UnassignedTasksWidget extends BaseWidget {
  // Component state

  private static final String EMPTY_TASK_IMAGE = "/no/ntnu/principes/images/happycharacter.png";
  private static final String EMPTY_TASK_IMAGE_DARK =
      "/no/ntnu/principes/images/happycharacter-darkmode.png";

  private final TaskTemplateService taskTemplateService;
  private final TaskAssignmentService taskAssignmentService;
  private final ScrollPane scrollPane = new ScrollPane();
  private final VBox innerContent = new VBox();
  private boolean hasUnassignedTasks = false;

  /**
   * Creates an UnassignedTasksWidget component that displays tasks that are not yet assigned to any user.
   *
   * @param screen the {@link BaseScreen} instance that serves as the parent for this widget.
   */
  public UnassignedTasksWidget(BaseScreen screen) {
    super("UnassignedTasksWidget", "Unassigned Tasks", "Tasks waiting to be assigned", screen);
    this.taskTemplateService = new TaskTemplateService();
    this.taskAssignmentService = new TaskAssignmentService();
  }

  @Override
  protected void initializeComponent() {
    // Set up content area
    this.scrollPane.setContent(this.innerContent);
    this.scrollPane.setFitToWidth(true);
    this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    this.scrollPane.setPadding(InsetBuilder.symmetric(10, 0).bottom(20).build());
    this.contentContainer.setAlignment(Pos.TOP_LEFT);
    this.contentContainer.getChildren().add(this.scrollPane);
    this.contentContainer.setMaxHeight(300);
    this.textContainer.setPadding(InsetBuilder.symmetric(20, 0).top(20).build());
    StyleManager.grow(this.scrollPane);
    this.innerContent.setAlignment(Pos.TOP_LEFT);
    this.innerContent.setSpacing(10);
  }

  /**
   * Refreshes the list of unassigned tasks displayed in the widget.
   *
   * @return true if there are unassigned tasks, false otherwise
   */
  @Override
  public boolean refresh() {
    List<Task> tasks = taskTemplateService.getUnassignedTasks();
    this.hasUnassignedTasks = !tasks.isEmpty();
    Platform.runLater(() -> {
      this.innerContent.getChildren().clear();

      if (tasks.isEmpty()) {
        displayEmptyTasksView();
      } else {
        this.setDescription("There are " + tasks.size() + " tasks waiting to be assigned");
        displayTasks(tasks);
      }
    });
    return this.hasUnassignedTasks;
  }

  /**
   * Displays a placeholder view indicating there are no unassigned tasks available.
   */
  private void displayEmptyTasksView() {
    VBox noTasksContainer = new VBox();
    noTasksContainer.setSpacing(10);
    noTasksContainer.setPadding(new Insets(20));
    noTasksContainer.setAlignment(Pos.CENTER);
    StyleManager.growHorizontal(noTasksContainer);

    Text noTasksText = new Text("All tasks are assigned", StyledText.TextType.BODY);
    HBox.setMargin(noTasksText, InsetBuilder.create().left(10).build());
    noTasksText.setStyle("-fx-font-size: 24px;");
    noTasksText.setTextAlignment(TextAlignment.CENTER);
    noTasksText.setPrefWidth(this.getWidth() - 40);
    noTasksText.setAlignment(Pos.CENTER);

    ImageView iconLight = createEmptyTasksIcon(false);
    ImageView iconDark = createEmptyTasksIcon(true);
    boolean isDarkMode = StyleManager.getThemeProvider().getTheme().equals("dark");
    iconLight.setVisible(isDarkMode);
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
   * Creates an icon to be displayed when there are no unassigned tasks available.
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
   * @param tasks the list of {@link Task} objects to display.
   */
  private void displayTasks(List<Task> tasks) {
    for (Task task : tasks) {
      this.innerContent.getChildren().add(createTaskRow(task));
    }
  }

  /**
   * Creates a styled row in an {@link HBox} representing a task, including its name and properties.
   *
   * @param task the {@link Task} containing the task details. Must not be {@code null}.
   * @return an {@link HBox} containing the visual representation of the task.
   */
  private HBox createTaskRow(Task task) {
    HBox taskRow = new HBox();
    taskRow.setAlignment(Pos.CENTER_LEFT);
    taskRow.setSpacing(10);
    taskRow.setPadding(new Insets(15));
    taskRow.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 8px;");

    VBox contentContainer = new VBox(5);
    contentContainer.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(contentContainer, Priority.ALWAYS);

    // Task name at the top
    Label taskNameLabel = new Label(task.getName());
    taskNameLabel.getStyleClass().addAll("text", "text-bold");
    taskNameLabel.setStyle("-fx-font-size: 16px;");

    // Add task icon
    FontIcon taskIcon = new FontIcon(Material2AL.ASSIGNMENT);
    taskIcon.setIconSize(20);
    HBox taskHeader = new HBox(8, taskIcon, taskNameLabel);
    taskHeader.setAlignment(Pos.CENTER_LEFT);

    // Description if available
    if (task.getDescription() != null && !task.getDescription().isEmpty()) {
      Label descriptionLabel = new Label(task.getDescription());
      descriptionLabel.getStyleClass().add("text-muted");
      descriptionLabel.setWrapText(true);
      contentContainer.getChildren().add(descriptionLabel);
    }

    // Status indicators row
    HBox statusRow = new HBox(10);
    statusRow.setAlignment(Pos.CENTER_LEFT);
    statusRow.setSpacing(15);

    // Difficulty badge
    Badge difficultyBadge = new Badge(
        StringUtils.titleCase(task.getWorkWeight().toString()),
        Badge.Variant.getWeightVariant(task.getWorkWeight().getValue())
    );

    // Time weight indicator
    HBox timeContainer = new HBox(5);
    timeContainer.setAlignment(Pos.CENTER_LEFT);
    FontIcon clockIcon = new FontIcon(Material2MZ.SCHEDULE);
    clockIcon.getStyleClass().add("text-muted");
    Label timeLabel = new Label(task.getTimeWeight().toString());
    timeLabel.getStyleClass().add("text-muted");
    timeContainer.getChildren().addAll(clockIcon, timeLabel);

    // Recurring indicator if applicable
    if (task.isRecurring()) {
      HBox recurringContainer = new HBox(5);
      recurringContainer.setAlignment(Pos.CENTER_LEFT);
      FontIcon repeatIcon = new FontIcon(Material2AL.AUTORENEW);
      repeatIcon.getStyleClass().add("text-muted");
      Label recurringLabel = new Label("Recurring");
      recurringLabel.getStyleClass().add("text-muted");
      recurringContainer.getChildren().addAll(repeatIcon, recurringLabel);
      statusRow.getChildren().add(recurringContainer);
    }

    // Add Assign buttons
    Button assignToMeButton = new Button("Assign to me", Button.ButtonType.OUTLINED);
    assignToMeButton.setCursor(Cursor.HAND);
    assignToMeButton.setOnAction(event -> {
      try {
        taskAssignmentService.assignTask(task.getId(), Auth.getInstance().getProfileId());
        refresh();
      } catch (Exception e) {
        log.error("Failed to assign task: {}", e.getMessage(), e);
      }
    });

    Button assignButton = new Button("Auto-distribute", Button.ButtonType.OUTLINED);
    assignButton.setCursor(Cursor.HAND);
    assignButton.setOnAction(event -> {
      try {
        AlertUtil.setEnabled(true);
        taskAssignmentService.autoAssignTask(task.getId());
        refresh();
      } catch (Exception e) {
        log.error("Failed to assign task: {}", e.getMessage(), e);
      }
    });

    // Add components to containers
    statusRow.getChildren().addAll(difficultyBadge, timeContainer);
    contentContainer.getChildren().addAll(taskHeader, statusRow);
    taskRow.getChildren().addAll(contentContainer, assignToMeButton, assignButton);

    // Add hover effect
    taskRow.setOnMouseEntered(event -> {
      taskRow.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 8px;");
    });
    taskRow.setOnMouseExited(event -> {
      taskRow.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 8px;");
    });

    return taskRow;
  }

  /**
   * Checks if the widget has unassigned tasks.
   *
   * @return true if there are unassigned tasks, false otherwise
   */
  public boolean hasUnassignedTasks() {
    return this.hasUnassignedTasks;
  }

  @Override
  protected void onMount() {
    this.refresh();
  }

  @Override
  protected void onUnmount() {
    // No operation needed
  }

  @Override
  protected void onDestroy() {
    this.getChildren().clear();
  }
}