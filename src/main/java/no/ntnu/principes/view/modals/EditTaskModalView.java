package no.ntnu.principes.view.modals;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Spacer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.synedra.validatorfx.Validator;
import no.ntnu.principes.components.primary.Badge;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.secondary.EnumCombobox;
import no.ntnu.principes.components.secondary.FormValidatorResultInfo;
import no.ntnu.principes.components.secondary.ProfileRowInCombobox;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TaskStatus;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.dto.ProfileDto;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseModal;

@Slf4j
public class EditTaskModalView extends BaseModal {
  private CustomTextField nameField;
  private CustomTextField descriptionField;
  private EnumCombobox<WorkWeight> workWeightSpinner;
  private EnumCombobox<TimeWeight> timeWeightSpinner;
  private DatePicker dueDatePicker;
  private ComboBox<String> dueDateTimeComboBox;
  private CheckBox recurringCheckbox;
  private Spinner<Integer> recurringDaysSpinner;
  private VBox recurringDaysBox;
  private Button updateButton;
  private Button cancelButton;
  private Text errorText;
  private final Validator validator = new Validator();

  // Task data
  private TaskDto task;
  private List<TaskAssignmentDto> assignments;

  // Multi-assignment support
  private VBox assigneeContainer;
  private ComboBox<Profile> assigneeComboBox;
  private List<Profile> availableProfiles;
  private final MemberRepository memberRepository;
  private final TaskTemplateService taskTemplateService;

  public EditTaskModalView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.memberRepository = DatabaseManager.getInstance().getRepository(MemberRepository.class);
    this.taskTemplateService = new TaskTemplateService();
  }

  @Override
  protected void initializeScreen() {
    this.getChildren().clear();

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    StyleManager.growHorizontal(scrollPane);
    StyleManager.growVertical(scrollPane);

    VBox content = new VBox(20);
    content.setPadding(InsetBuilder.uniform(20).build());
    content.setAlignment(Pos.TOP_LEFT);
    StyleManager.growHorizontal(content);

    Text title = new Text("Edit Task", Text.TextType.PAGE_TITLE);

    // Error text
    this.errorText = new Text("", StyledText.TextType.ERROR_MESSAGE);
    this.errorText.setVisible(false);
    this.errorText.setManaged(false);

    // Form fields
    this.nameField = new CustomTextField();
    this.nameField.setPromptText("Task name");
    StyleManager.apply(this.nameField, StyleManager.InputStyle.INPUT);

    this.descriptionField = new CustomTextField();
    this.descriptionField.setPromptText("Description");
    StyleManager.apply(this.descriptionField, StyleManager.InputStyle.INPUT);

    HBox weightBox = new HBox(10);
    this.workWeightSpinner = this.createWeightSpinner(WorkWeight.class);
    this.timeWeightSpinner = this.createWeightSpinner(TimeWeight.class);
    weightBox.getChildren().addAll(
        this.createSpinnerWithLabel("Work Difficulty", this.workWeightSpinner),
        this.createSpinnerWithLabel("Time Complexity", this.timeWeightSpinner)
    );

    VBox dueDateSection = new VBox(10);
    Text dueDateSectionTitle = new Text("Due Date", Text.TextType.SUBHEADER);
    Text dueDateNote =
        new Text("This date will apply to all task assignments", Text.TextType.HELPER);

    // Due date
    HBox dueDateBox = new HBox(10);
    dueDateBox.setAlignment(Pos.CENTER_LEFT);
    this.dueDatePicker = new DatePicker();
    this.dueDatePicker.setPromptText("Due date");

    this.dueDateTimeComboBox = new ComboBox<>(FXCollections.observableArrayList(
        "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
        "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
        "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    ));
    this.dueDateTimeComboBox.getSelectionModel().select(16); // Default to 16:00

    dueDateBox.getChildren().addAll(
        this.createSpinnerWithLabel("Date", this.dueDatePicker),
        this.createSpinnerWithLabel("Time", this.dueDateTimeComboBox)
    );

    dueDateSection.getChildren().addAll(dueDateSectionTitle, dueDateNote, dueDateBox);

    // Recurring task
    VBox recurringBox = new VBox(10);
    this.recurringCheckbox = new CheckBox("This is a recurring task");
    this.recurringDaysSpinner = new Spinner<>(1, 365, 7);
    this.recurringDaysSpinner.setEditable(true);

    recurringDaysBox = createSpinnerWithLabel("Repeat every X days", this.recurringDaysSpinner);
    recurringBox.getChildren().addAll(this.recurringCheckbox, recurringDaysBox);

    // Assignee section
    VBox assigneeSection = new VBox(10);
    Text assigneeTitle = new Text("Assign To", Text.TextType.SUBHEADER);
    this.assigneeContainer = new VBox(10);

    // Profile selection
    this.assigneeComboBox = new ComboBox<>();
    this.assigneeComboBox.setCellFactory(listView -> new ProfileRowInCombobox());
    this.assigneeComboBox.setButtonCell(new ProfileRowInCombobox());
    this.assigneeComboBox.setPromptText("Select a profile");
    StyleManager.apply(this.assigneeComboBox, StyleManager.InputStyle.INPUT);

    Button addAssigneeButton = new Button("Add Assignee", Button.ButtonType.OUTLINED);
    addAssigneeButton.setOnAction(e -> this.addAssignee());

    assigneeSection.getChildren()
        .addAll(assigneeTitle, this.assigneeContainer, this.assigneeComboBox,
            addAssigneeButton);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);

    this.updateButton = new Button("Update Task", Button.ButtonType.DEFAULT);
    this.cancelButton = new Button("Cancel", Button.ButtonType.FLAT);
    this.updateButton.setDefaultButton(true);
    this.validator.containsErrorsProperty().addListener((obs, old, newVal) -> {
      this.updateButton.setDisable(newVal);
    });

    buttonBox.getChildren().addAll(
        new Spacer(),
        this.cancelButton,
        this.updateButton
    );

    // Add a spacer to push buttons to bottom
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    this.setupValidation();
    FormValidatorResultInfo validatorResultInfo = new FormValidatorResultInfo(this, this.validator);

    content.getChildren().addAll(
        title,
        this.errorText,
        this.nameField,
        this.descriptionField,
        weightBox,
        dueDateSection,
        recurringBox,
        assigneeSection,
        validatorResultInfo,
        spacer
    );

    scrollPane.setContent(content);
    this.setupEventHandlers();

    this.getModalContent().getChildren().addAll(scrollPane, buttonBox);
    this.getModalContent().setPrefWidth(600);
    this.getModalContent().setPrefHeight(500);
    this.getChildren().add(this.getModalContent());
  }

  private void setupValidation() {
    validator.createCheck()
        .dependsOn("name", this.nameField.textProperty())
        .withMethod(c -> {
          String name = c.get("name");
          if (name == null || name.trim().isEmpty()) {
            c.error("Task name cannot be empty");
          }
        })
        .decorates(this.nameField)
        .immediate();

    validator.createCheck()
        .dependsOn("recurring", this.recurringCheckbox.selectedProperty())
        .dependsOn("recurringDays", this.recurringDaysSpinner.getValueFactory().valueProperty())
        .withMethod(c -> {
          Boolean isRecurring = c.get("recurring");
          Integer recurringDays = c.get("recurringDays");
          if (isRecurring == null || !isRecurring) {
            return;
          }
          if (recurringDays == null) {
            c.error("Recurrence interval must be selected");
          } else if (recurringDays <= 0) {
            c.error("Recurrence interval must be greater than 0");
          } else if (recurringDays > 365) {
            c.error("Recurrence interval must be less than or equal to 365");
          }
        })
        .decorates(this.recurringDaysSpinner)
        .immediate();

    validator.createCheck()
        .dependsOn("dueDate", this.dueDatePicker.valueProperty())
        .dependsOn("dueDateTime",
            this.dueDateTimeComboBox.getSelectionModel().selectedItemProperty())
        .withMethod(c -> {
          LocalDate dueDate = c.get("dueDate");
          if (dueDate == null) {
            return; // Due date is optional
          }

          String selectedTimeString = c.get("dueDateTime");
          if (selectedTimeString == null) {
            return;
          }

          LocalDateTime now = LocalDateTime.now();
          LocalDateTime dueDateTime = dueDate.atTime(
              Integer.parseInt(selectedTimeString.split(":")[0]),
              Integer.parseInt(selectedTimeString.split(":")[1])
          );

          if (dueDateTime.isBefore(now)) {
            c.error("Due date cannot be in the past");
          }
        })
        .decorates(this.dueDatePicker)
        .immediate();

    validator.createCheck()
        .dependsOn("workWeight",
            this.workWeightSpinner.getSelectionModel().selectedItemProperty())
        .withMethod(c -> {
          WorkWeight workWeight = c.get("workWeight");
          if (workWeight == null) {
            c.error("Work weight must be selected");
          }
        })
        .decorates(this.workWeightSpinner)
        .immediate();

    validator.createCheck()
        .dependsOn("timeWeight",
            this.timeWeightSpinner.getSelectionModel().selectedItemProperty())
        .withMethod(c -> {
          TimeWeight timeWeight = c.get("timeWeight");
          if (timeWeight == null) {
            c.error("Time weight must be selected");
          }
        })
        .decorates(this.timeWeightSpinner)
        .immediate();
  }

  private <T> EnumCombobox<T> createWeightSpinner(Class<T> enumClass) {
    return EnumCombobox.of(enumClass);
  }

  private VBox createSpinnerWithLabel(String label, Node spinner) {
    VBox box = new VBox(10);
    Text labelText = new Text(label, Text.TextType.FORM_LABEL);
    box.getChildren().addAll(labelText, spinner);
    return box;
  }

  private void setupEventHandlers() {
    this.recurringCheckbox.selectedProperty().addListener((obs, old, newVal) -> {
      this.recurringDaysSpinner.setDisable(!newVal);
      this.recurringDaysBox.setVisible(newVal);
      this.recurringDaysBox.setManaged(newVal);
    });

    this.updateButton.setOnAction(e -> this.handleUpdate());
    this.cancelButton.setOnAction(e -> this.setModalCancelled());
  }

  private void loadTaskData() {
    // Get task data from context
    this.task = this.getContext().getParameter("task");
    this.assignments = this.getContext().getParameter("assignments");

    if (this.task == null) {
      this.showError("No task data provided");
      return;
    }

    // Populate form fields
    this.nameField.setText(this.task.getName());
    this.descriptionField.setText(this.task.getDescription());
    this.workWeightSpinner.getSelectionModel().select(this.task.getWorkWeight());
    this.timeWeightSpinner.getSelectionModel().select(this.task.getTimeWeight());

    // Set recurring info
    this.recurringCheckbox.setSelected(this.task.isRecurring());
    this.recurringDaysSpinner.getValueFactory().setValue(this.task.getRecurrenceIntervalDays());
    this.recurringDaysBox.setVisible(this.task.isRecurring());
    this.recurringDaysBox.setManaged(this.task.isRecurring());
    this.recurringDaysSpinner.setDisable(!this.task.isRecurring());

    // Set due date from the first assignment with a due date (if available)
    Optional<TaskAssignmentDto> assignmentWithDueDate = this.assignments.stream()
        .filter(a -> a.getDueAt() != null)
        .findFirst();

    if (assignmentWithDueDate.isPresent()) {
      LocalDateTime dueDateTime = assignmentWithDueDate.get().getDueAt();
      this.dueDatePicker.setValue(dueDateTime.toLocalDate());

      // Format time for combobox
      String timeStr = String.format("%02d:00", dueDateTime.getHour());
      this.dueDateTimeComboBox.getSelectionModel().select(timeStr);
    }

    // Load existing assignees
    this.loadAssignees();
  }

  private void loadAssignees() {
    // Clear existing assignees
    this.assigneeContainer.getChildren().clear();

    // Load all members for the assignee dropdown
    List<Profile> allProfiles = this.memberRepository.findAll();

    // Create a list of profiles already assigned
    List<Long> assignedProfileIds = this.assignments.stream()
        .filter(a -> a.getMember() != null)
        .map(a -> a.getMember().getId())
        .collect(Collectors.toList());

    // Filter out already assigned profiles for the dropdown
    this.availableProfiles = allProfiles.stream()
        .filter(p -> !assignedProfileIds.contains(p.getId()))
        .collect(Collectors.toList());

    // Update assignee combobox
    this.assigneeComboBox.setItems(
        FXCollections.observableArrayList(this.availableProfiles)
    );

    // Add existing assignees to the container
    for (TaskAssignmentDto assignment : this.assignments) {
      if (assignment.getMember() != null) {
        this.addAssigneeRow(assignment);
      }
    }
  }

  private void addAssigneeRow(TaskAssignmentDto assignment) {
    HBox row = new HBox(10);
    row.setAlignment(Pos.CENTER_LEFT);

    ProfileDto member = assignment.getMember();
    Text profileName = new Text(member.getName(), Text.TextType.BODY);

    // Status badge
    Badge.Variant statusVariant = assignment.getStatus() == TaskStatus.DONE
        ? Badge.Variant.SUCCESS
        : Badge.Variant.WARNING;

    no.ntnu.principes.components.primary.Badge statusBadge =
        new no.ntnu.principes.components.primary.Badge(
            assignment.getStatus().toString(),
            statusVariant
        );

    // Due date display
    Text dueDate = new Text(
        assignment.getDueAt() != null
            ? "Due: " + formatDateTime(assignment.getDueAt())
            : "No due date",
        Text.TextType.HELPER
    );

    // Remove button
    Button removeButton = new Button("Remove", Button.ButtonType.OUTLINED);
    removeButton.setOnAction(e -> this.removeAssignee(assignment.getId(), member.getId()));

    VBox profileInfo = new VBox(5);
    profileInfo.getChildren().addAll(profileName, dueDate);

    HBox.setHgrow(profileInfo, Priority.ALWAYS);
    row.getChildren().addAll(profileInfo, statusBadge, removeButton);

    this.assigneeContainer.getChildren().add(row);
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "N/A";
    }

    DateTimeFormatter formatter;
    if (dateTime.getYear() != LocalDateTime.now().getYear()) {
      formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    } else {
      formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm");
    }

    return dateTime.format(formatter);
  }

  private void addAssignee() {
    Profile selectedProfile = this.assigneeComboBox.getValue();
    if (selectedProfile == null) {
      return;
    }

    // Create new assignment DTO manually (will be saved during update)
    TaskAssignmentDto newAssignment = TaskAssignmentDto.builder()
        .task(this.task)
        .member(ProfileDto.builder()
            .id(selectedProfile.getId())
            .name(selectedProfile.getName())
            .avatarHash(selectedProfile.getAvatarHash())
            .createdAt(selectedProfile.getCreatedAt())
            .build())
        .status(TaskStatus.TODO)
        .build();

    // Add to current assignments list
    this.assignments.add(newAssignment);

    // Add to UI
    this.addAssigneeRow(newAssignment);

    // Remove from available profiles
    this.availableProfiles.remove(selectedProfile);
    this.assigneeComboBox.setItems(
        FXCollections.observableArrayList(this.availableProfiles)
    );
    this.assigneeComboBox.setValue(null);
  }

  private void removeAssignee(Long assignmentId, Long profileId) {
    // If it has an ID, mark for deletion
    if (assignmentId != null) {
      // Actually remove the assignment on save
      this.assignments.removeIf(a ->
          a.getId() != null && a.getId().equals(assignmentId));
    } else {
      // Just remove from our temporary list
      this.assignments.removeIf(a ->
          a.getMember() != null && a.getMember().getId().equals(profileId));
    }

    // Add profile back to available profiles
    Profile removedProfile = this.memberRepository.findById(profileId).orElse(null);
    if (removedProfile != null) {
      this.availableProfiles.add(removedProfile);
      this.assigneeComboBox.setItems(
          FXCollections.observableArrayList(this.availableProfiles)
      );
    }

    // Reload assignee list
    this.loadAssignees();
  }

  private void handleUpdate() {
    if (this.task == null) {
      this.showError("No task to update");
      return;
    }

    // Validate form
    if (this.validator.containsErrors()) {
      return;
    }

    // Prepare due date
    LocalDateTime dueDateTime = null;
    if (this.dueDatePicker.getValue() != null) {
      String timeString = this.dueDateTimeComboBox.getValue();
      LocalTime time = LocalTime.of(
          Integer.parseInt(timeString.split(":")[0]),
          0
      );
      dueDateTime = LocalDateTime.of(this.dueDatePicker.getValue(), time);
    }

    // Create update request
    CreateTaskRequest updateRequest = CreateTaskRequest.builder()
        .name(this.nameField.getText().trim())
        .description(this.descriptionField.getText().trim())
        .workWeight(this.workWeightSpinner.getValue())
        .timeWeight(this.timeWeightSpinner.getValue())
        .dueAt(dueDateTime)
        .isRecurring(this.recurringCheckbox.isSelected())
        .recurrenceIntervalDays(
            this.recurringCheckbox.isSelected()
                ? this.recurringDaysSpinner.getValue()
                : 0
        )
        .createdById(this.task.getCreatedBy().getId())
        .build();

    try {
      // Update task
      Task updatedTask = this.taskTemplateService.updateTask(
          this.task.getId(),
          updateRequest,
          this.getAssigneeIds()
      );

      // Close modal with success
      this.closeWithResult(updatedTask);
    } catch (Exception e) {
      log.error("Error updating task", e);
      this.showError("Error updating task: " + e.getMessage());
    }
  }

  private List<Long> getAssigneeIds() {
    return this.assignments.stream()
        .filter(a -> a.getMember() != null)
        .map(a -> a.getMember().getId())
        .toList();
  }

  private void showError(String message) {
    this.errorText.setText(message);
    this.errorText.setVisible(true);
    this.errorText.setManaged(true);
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();
    this.loadTaskData();

    // Trigger initial validation to set button state
    Platform.runLater(() -> {
      this.validator.validate();
      this.updateButton.setDisable(this.validator.containsErrors());
    });
  }
}