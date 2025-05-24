package no.ntnu.principes.components.widgets;

import java.time.LocalDate;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.secondary.SelectableGroup;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.view.BaseScreen;

/**
 * Displays task completion rates and related statistics with the ability to toggle
 * between household and personal views.
 */
@Slf4j
public class CompletionRateWidget extends BaseWidget {
  private final VBox contentBox;
  private final TaskAssignmentService taskAssignmentService;
  private boolean isPersonalMode = false;

  /**
   * A widget that displays the completion rate of tasks with toggling between
   * household and personal views.
   *
   * @param screen the {@link BaseScreen} instance that this widget belongs to.
   */
  public CompletionRateWidget(BaseScreen screen) {
    super("CompletionRateWidget", "Task Completion", screen);
    this.taskAssignmentService = new TaskAssignmentService();
    this.contentBox = new VBox(20); // Increased spacing between elements
    this.contentBox.setAlignment(Pos.TOP_LEFT);
    this.contentBox.setPadding(InsetBuilder.uniform(15).build());
    this.fitContent();
  }

  @Override
  protected void initializeComponent() {

    // Add the toggle group
    HBox toggleContainer = new HBox();
    toggleContainer.setAlignment(Pos.CENTER_LEFT);
    toggleContainer.setPadding(InsetBuilder.symmetric(20, 0).build());

    SelectableGroup viewToggle = createViewToggle();
    toggleContainer.getChildren().add(viewToggle);

    this.contentContainer.getChildren().addAll(toggleContainer, this.contentBox);
  }

  /**
   * Creates a toggle control to switch between Household and Personal views
   */
  private SelectableGroup createViewToggle() {
    List<SelectableGroup.SelectableGroupItem> items = List.of(
        new SelectableGroup.SelectableGroupItem("household", "Household", !isPersonalMode),
        new SelectableGroup.SelectableGroupItem("personal", "Personal", isPersonalMode)
    );

    return new SelectableGroup(items, this::handleViewToggle);
  }

  /**
   * Handles the view toggle selection
   */
  private void handleViewToggle(String id) {
    if (id == null) {
      return;
    }

    boolean wasPersonal = this.isPersonalMode;
    this.isPersonalMode = id.equals("personal");

    // Only update if the mode actually changed
    if (wasPersonal != this.isPersonalMode) {
      this.refresh();
    }
  }

  @Override
  protected void onMount() {
    super.onMount();
    this.refresh(); // Only refresh here during initialization
  }

  /**
   * Updates the display of completion rates based on the selected view mode
   */

  @Override
  public boolean refresh() {
    this.contentBox.getChildren().clear();

    // Today's stats
    LocalDate today = LocalDate.now();
    CompletionStats todayStats = this.getCompletionStatsForDate(today);

    // Yesterday's stats
    LocalDate yesterday = today.minusDays(1);
    CompletionStats yesterdayStats = this.getCompletionStatsForDate(yesterday);

    // Add progress sections - only add each one once
    this.contentBox.getChildren().add(createProgressSection("Today's progress", todayStats));
    this.contentBox.getChildren()
        .add(createProgressSection("Yesterday's progress", yesterdayStats));

    // If it's personal mode, add task counts as well
    if (this.isPersonalMode) {
      this.addTaskCounts();
    }
    return true;
  }

  /**
   * Represents completion statistics for a specific date
   */
  private static class CompletionStats {
    int completed;
    int total;
    double rate;

    CompletionStats(int completed, int total) {
      this.completed = completed;
      this.total = total;
      this.rate = total > 0 ? (double) completed / total : 0.0;
    }
  }

  /**
   * Gets completion statistics for a specific date
   */
  private CompletionStats getCompletionStatsForDate(LocalDate date) {
    List<TaskAssignmentDto> assignments;

    if (this.isPersonalMode && Auth.getInstance().getProfileId() != null) {
      // Get assignments for the specific user
      assignments =
          this.taskAssignmentService.getTasksForMemberDueAt(Auth.getInstance().getProfileId(),
              date);
    } else {
      assignments = this.taskAssignmentService.getAllTasks().stream()
          .filter(ass -> ass.getMember() != null)
          .filter(ass -> {
            // Check if this task is due on the specified date
            return this.taskAssignmentService.getTasksForMemberDueAt(
                ass.getMember().getId(), date).contains(ass);
          })
          .toList();
    }

    int totalTasks = assignments.size();
    int completedTasks = (int) assignments.stream()
        .filter(TaskAssignmentDto::isCompleted)
        .count();

    return new CompletionStats(completedTasks, totalTasks);
  }

  /**
   * Creates a section showing progress information including text and progress bar
   * Styled to match the image design with right-aligned percentages
   */
  private VBox createProgressSection(String label, CompletionStats stats) {
    VBox section = new VBox(8);
    section.setAlignment(Pos.TOP_LEFT);

    // Create the label
    Text labelText = new Text(label, StyledText.TextType.BODY);
    labelText.setStyle("-fx-fill: white; -fx-font-weight: normal;");

    // Create the "X of Y completed" text
    String completionText = stats.completed + " of " + stats.total + " tasks completed";
    Text completionDetailsText = new Text(completionText, StyledText.TextType.HELPER);
    completionDetailsText.setStyle("-fx-fill: -color-fg-subtle;");

    // Create progress bar row with percentage
    HBox progressRow = new HBox();
    progressRow.setAlignment(Pos.CENTER);

    // Format percentage text (right-aligned)
    String percentage = String.format("%.1f%%", stats.rate * 100);
    Text percentageText = new Text(percentage, StyledText.TextType.BODY);
    percentageText.setStyle("-fx-fill: white;");

    // Create progress bar container with the percentage on the right
    HBox progressContainer = new HBox();
    progressContainer.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(progressContainer, Priority.ALWAYS);

    // Create custom progress bar with the proper styling
    ProgressBar progressBar = new ProgressBar(stats.rate);
    progressBar.setPrefHeight(8);
    progressBar.setMinHeight(8);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(progressBar, Priority.ALWAYS);

    progressContainer.getChildren().add(progressBar);

    // Add percentage text on the right
    HBox percentageContainer = new HBox();
    percentageContainer.setAlignment(Pos.CENTER_RIGHT);
    percentageContainer.setPadding(new Insets(0, 0, 0, 10));
    percentageContainer.getChildren().add(percentageText);

    // Add everything to the progress row
    progressRow.getChildren().addAll(progressContainer, percentageContainer);

    // Add all components to section
    section.getChildren().addAll(labelText, completionDetailsText, progressRow);

    return section;
  }

  /**
   * Adds task count indicators to the widget
   */
  private void addTaskCounts() {
    if (Auth.getInstance().getProfileId() == null) {
      return;
    }

    List<TaskAssignmentDto> userAssignments =
        this.taskAssignmentService.getTasksForMember(Auth.getInstance().getProfileId());

    int incompleteCount = (int) userAssignments.stream()
        .filter(TaskAssignmentDto::isPending)
        .count();

    int completedCount = (int) userAssignments.stream()
        .filter(TaskAssignmentDto::isCompleted)
        .count();

    VBox taskCountsBox = new VBox(10);
    taskCountsBox.setAlignment(Pos.TOP_LEFT);
    taskCountsBox.setPadding(new Insets(15, 0, 0, 0));

    HBox incompleteBox = this.createCountDisplay(
        "Incomplete tasks",
        incompleteCount,
        Color.valueOf("#c42b1c")
    );

    HBox completedBox = this.createCountDisplay(
        "Completed tasks",
        completedCount,
        Color.valueOf("#0f7b0f")
    );

    taskCountsBox.getChildren().addAll(incompleteBox, completedBox);
    this.contentBox.getChildren().add(taskCountsBox);
  }

  /**
   * Creates a count display with circular indicator and label
   */
  private HBox createCountDisplay(String label, int count, Color color) {
    HBox countBox = new HBox(10);
    countBox.setAlignment(Pos.CENTER_LEFT);

    // Create circle indicator
    Circle circle = new Circle(12);
    circle.setFill(color);

    // Create count text
    Text countText = new Text(String.valueOf(count), StyledText.TextType.BODY);
    countText.setStyle("-fx-fill: white;");

    // Create label
    Text labelText = new Text(label, StyledText.TextType.BODY);
    labelText.setStyle("-fx-fill: -color-fg-default;");

    countBox.getChildren().addAll(circle, countText, labelText);
    return countBox;
  }

  @Override
  protected void setupContainer() {
    super.setupContainer();
  }
}