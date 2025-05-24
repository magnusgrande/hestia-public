package no.ntnu.principes.components.widgets;

import atlantafx.base.theme.Styles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

/**
 * A widget for displaying and updating task distribution among team members.
 *
 * <p>The widget shows the number of tasks assigned to each member and highlights unassigned tasks.
 * Icons and text are displayed if no tasks are distributed.
 * Members without assigned tasks are omitted.
 * </p>
 *
 * <p>Task rows include member names, task counts, and badges color-coded to indicate task quantity.
 * Unassigned tasks have a distinct badge color. The component updates dynamically when mounted.
 * </p>
 */
@Slf4j
public class TaskDistributionWidget extends BaseWidget {
  private static final String EMPTY_DIST_IMAGE = "/no/ntnu/principes/images/sadcharacter.png";
  private final VBox contentBox;
  private final TaskAssignmentService taskAssignmentService;
  private final MemberRepository memberRepository;

  /**
   * A widget for task distribution and interface layout setup within a parent screen.
   *
   * <p>This widget initializes a vertical layout container {@link VBox} for task distribution
   * and alignment, and integrates task assignment and member repository services for additional
   * functionality. It ensures the content area adapts to the widget's size.</p>
   *
   * @param screen the parent {@link BaseScreen} that owns this widget. .
   */
  public TaskDistributionWidget(BaseScreen screen) {
    super("TaskDistributionWidget", "Distribution", screen);
    this.taskAssignmentService = new TaskAssignmentService();
    this.memberRepository = DatabaseManager.getInstance().getRepository(MemberRepository.class);
    this.contentBox = new VBox(10);
    this.contentBox.setAlignment(Pos.CENTER_LEFT);
    this.contentBox.setPadding(InsetBuilder.uniform(10).build());
    this.fitContent();
  }

  @Override
  protected void initializeComponent() {
    this.contentContainer.getChildren().add(this.contentBox);
    StyleManager.padding(this.contentContainer, InsetBuilder.uniform(10).build());
    this.updateDistribution();
  }

  @Override
  protected void onMount() {
    super.onMount();
    this.updateDistribution();
    PrincipesEventBus.getInstance()
        .subscribe(TasksDistributedEvent.class, this::onTaskDistributedEvent);
  }

  @Override
  protected void onUnmount() {
    super.onUnmount();
    PrincipesEventBus.getInstance()
        .unsubscribe(TasksDistributedEvent.class, this::onTaskDistributedEvent);
  }

  private void onTaskDistributedEvent(TasksDistributedEvent event) {
    Platform.runLater(this::updateDistribution);
  }

  /**
   * Updates the distribution display by clearing current content and rebuilding it based on task
   * assignments and member profiles.
   *
   * <p>This method performs the following actions:</p>
   * <ul>
   *     <li>Clears any existing content in the container.</li>
   *     <li>Retrieves all task assignments and member profiles.</li>
   *     <li>If no tasks exist, displays an "empty state" message with a corresponding icon.</li>
   *     <li>Counts and associates tasks with members, dynamically creating rows for members
   *         with assigned tasks.</li>
   *     <li>Includes an additional row for unassigned tasks, if any exist.</li>
   * </ul>
   */
  public void updateDistribution() {
    this.contentBox.getChildren().clear();

    // Get all tasks
    List<TaskAssignmentDto> allAssignments = this.taskAssignmentService.getAllTasks()
        .stream().filter(TaskAssignmentDto::isPending).toList();

    if (allAssignments.isEmpty()) {
      // Show empty state
      HBox imageContainer = new HBox(this.createEmptyTasksIcon());
      StyleManager.growHorizontal(imageContainer);
      imageContainer.setAlignment(Pos.CENTER);
      imageContainer.setStyle("-fx-padding: 20px;");
      this.contentBox.getChildren().add(imageContainer);

      Label text = new Label("No tasks distributed yet.");
      HBox textContainer = new HBox(text);
      textContainer.setAlignment(Pos.CENTER);
      text.setTextAlignment(TextAlignment.CENTER);
      text.setAlignment(Pos.CENTER);
      StyleManager.growHorizontal(text, textContainer);
      this.contentBox.getChildren().add(textContainer);
      return;
    }

    List<Profile> allProfiles = this.memberRepository.findAll();

    // Count tasks per member
    Map<Long, Integer> taskCounts = new HashMap<>();

    // Count assigned tasks
    for (TaskAssignmentDto assignment : allAssignments) {
      if (assignment.getMember() != null) {
        Long memberId = assignment.getMember().getId();
        taskCounts.put(memberId, taskCounts.getOrDefault(memberId, 0) + 1);
      }
    }

    // Count unassigned tasks
    long unassignedCount = allAssignments.stream()
        .filter(a -> a.getMember() == null)
        .count();

    // Create member rows
    for (Profile profile : allProfiles) {
      int count = taskCounts.getOrDefault(profile.getId(), 0);
      if (count > 0) { // Only show members with tasks
        this.contentBox.getChildren().add(
            createMemberRow(profile.getName(), count,
                this.taskAssignmentService.calculateTotalWeightForUser(profile.getId()))
        );
      }
    }

    // Add unassigned tasks if any
    if (unassignedCount > 0) {
      this.contentBox.getChildren().add(
          createMemberRow("Unassigned tasks", (int) unassignedCount, 0)
      );
    }
  }

  /**
   * Creates the icon for the empty tasks state.
   *
   * @return The {@link ImageView} representing the empty tasks icon.
   */
  private ImageView createEmptyTasksIcon() {
    // Create an empty tasks icon
    ImageView icon = new ImageView(
        Objects.requireNonNull(
                getClass().getResource(EMPTY_DIST_IMAGE))
            .toExternalForm());
    icon.setFitHeight((this.getWidth() - 40) / 1.75);
    icon.setFitWidth(Math.min(300, this.getWidth() - 40));
    icon.setPreserveRatio(true);
    icon.setSmooth(true);
    icon.setCache(true);
    return icon;
  }

  /**
   * Creates a horizontally aligned row containing a styled name label, a spacer,
   * and a task count badge.
   *
   * <p>The method styles the task count badge using different colors based on
   * specific rules, including a special case for "Unassigned tasks" and varying thresholds
   * of the task count. The resulting HBox is set up with consistent spacing and alignment.</p>
   *
   * @param name  the name to be displayed in the row. If set to "Unassigned tasks", the badge
   *              will be styled with a distinct red color.
   * @param count the number of tasks to display in the badge. This determines the badge's color:
   *              <ul>
   *                <li>Red for "Unassigned tasks"</li>
   *                <li>Blue for counts greater than 6</li>
   *                <li>Green for counts between 5 and 6 (inclusive)</li>
   *                <li>Light blue for counts 4 or lower</li>
   *              </ul>
   * @return an {@link HBox} containing the name label, spacer, and task count badge.
   */
  private HBox createMemberRow(String name, int count, int weight) {
    HBox row = new HBox(10);
    row.setAlignment(Pos.CENTER_LEFT);

    Label nameText = new Label(name);
    StyleManager.apply(nameText, Styles.TEXT, Styles.TEXT_BOLD);
    nameText.setStyle("-fx-font-size: 18px;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Badge color
    String badgeColor;
    if (name.equals("Unassigned tasks")) {
      badgeColor = "#c42b1c"; // Red for unassigned
    } else if (count > 6) {
      badgeColor = "#0070c0"; // Blue for high count
    } else if (count > 4) {
      badgeColor = "#0f7b0f"; // Green for medium count
    } else {
      badgeColor = "#00b0f0"; // Light blue for low count
    }

    // Task count badge
    HBox badge = new HBox();
    badge.setAlignment(Pos.CENTER);
    badge.setMinSize(25, 25);
    badge.setPrefSize(25, 25);
    badge.setMaxSize(25, 25);
    badge.setStyle(
        "-fx-background-color: " + badgeColor + ";"
            + "-fx-background-radius: 12.5;"
            + "-fx-text-fill: white;"
    );

    Text countText = new Text(String.valueOf(count), StyledText.TextType.BODY);
    countText.setStyle("-fx-fill: white;-fx-text-fill: white");
    badge.getChildren().add(countText);

    if (weight != 0) {
      nameText.setTooltip(new Tooltip("Workload: " + weight));
    }

    // Add to row
    row.getChildren().addAll(nameText, spacer, badge);

    return row;
  }
}