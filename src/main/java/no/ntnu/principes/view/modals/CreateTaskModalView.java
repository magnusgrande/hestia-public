package no.ntnu.principes.view.modals;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.Spacer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.synedra.validatorfx.Validator;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.secondary.EnumCombobox;
import no.ntnu.principes.components.secondary.FormValidatorResultInfo;
import no.ntnu.principes.components.validations.FormDecoration;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseModal;

public class CreateTaskModalView extends BaseModal {
  private CustomTextField nameField;
  private CustomTextField descriptionField;
  private EnumCombobox<WorkWeight> workWeightSpinner;
  private EnumCombobox<TimeWeight> timeWeightSpinner;
  private DatePicker dueDatePicker;
  private ComboBox<String> dueDateTimeComboBox;
  private CheckBox recurringCheckbox;
  private Spinner<Integer> recurringDaysSpinner;
  private VBox recurringDaysBox;
  private Button createButton;
  private Button cancelButton;
  private Text errorText;
  private Validator validator = new Validator();
  private FormValidatorResultInfo validatorResultInfo;
  private ObservableMap<String, BooleanProperty> touchedMap = FXCollections.observableHashMap();

  public CreateTaskModalView(ScreenController controller, String screenId) {
    super(controller, screenId);
  }

  @Override
  protected void initializeScreen() {
    this.getChildren().clear();
    VBox content = new VBox(20);
    content.setPadding(InsetBuilder.uniform(20).build());
    content.setAlignment(Pos.TOP_LEFT);
    StyleManager.growHorizontal(content);

    Text title = new Text("Create New Task", Text.TextType.PAGE_TITLE);

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

    // Due date
    HBox dueDateBox = new HBox(10);
    dueDateBox.setAlignment(Pos.CENTER_LEFT);
    this.dueDatePicker = new DatePicker();
    this.dueDatePicker.setPromptText("Due date");
    this.dueDatePicker.applyCss();
    this.dueDatePicker.requestLayout();
    // Only allow setting editable when the name has a non blank value
    var nameBlankBinding = Bindings.createBooleanBinding(
        () -> !this.nameField.getText().isEmpty(),
        this.nameField.textProperty()
    );
    this.dueDatePicker.disableProperty().bind(nameBlankBinding.not());

    this.dueDateTimeComboBox = new ComboBox<>(FXCollections.observableArrayList(
        "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
        "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
        "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
    ));
    this.dueDateTimeComboBox.getSelectionModel().select(16);

    this.dueDateTimeComboBox.disableProperty().bind(nameBlankBinding.not());

    dueDateBox.getChildren().addAll(
        this.createSpinnerWithLabel("Due Date", this.dueDatePicker),
        this.createSpinnerWithLabel("Due Time", this.dueDateTimeComboBox)
    );
//    StyleManager.apply(this.dueDatePicker, StyleManager.InputStyle.INPUT);

    // Recurring task
    VBox recurringBox = new VBox(10);
    this.recurringCheckbox = new CheckBox("This is a recurring task");
    this.recurringDaysSpinner = new Spinner<>(1, 365, 7);
    this.recurringDaysSpinner.setEditable(true);
    this.recurringDaysSpinner.setDisable(true);

    recurringDaysBox =
        createSpinnerWithLabel("Repeat every X days", this.recurringDaysSpinner);
    recurringBox.getChildren().addAll(this.recurringCheckbox, recurringDaysBox);
    this.recurringDaysBox.setVisible(false);
    this.recurringDaysBox.setManaged(false);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);

    this.createButton = new Button("Create Task", Button.ButtonType.DEFAULT);
    this.cancelButton = new Button("Cancel", Button.ButtonType.FLAT);
    this.createButton.setDefaultButton(true);
    this.validator.containsErrorsProperty().addListener((obs, old, newVal) -> {
      this.createButton.setDisable(newVal);
    });


    buttonBox.getChildren().addAll(
        new Spacer(),
        this.cancelButton,
        this.createButton
    );

    VBox spacer = new VBox();
    StyleManager.growVertical(spacer);

    this.setupValidation();
    this.validatorResultInfo = new FormValidatorResultInfo(this, this.validator);

    content.getChildren().addAll(
        title,
        this.errorText,
        this.nameField,
        this.descriptionField,
        weightBox,
        dueDateBox,
        recurringBox,
        this.validatorResultInfo,
        spacer,
        buttonBox
    );
    StyleManager.growVertical(content, this.getModalContent());

    this.setupEventHandlers();

    //
    this.getModalContent().getChildren().add(content);
    this.getModalContent().setPrefWidth(500);
    this.getChildren().add(this.getModalContent());
  }

  private void addTouchedListener(String fieldName, ReadOnlyBooleanProperty focusedProperty,
                                  boolean onBlur) {
    BooleanProperty touchedProperty = this.touchedMap.get(fieldName);
    if (touchedProperty != null) {
      focusedProperty.addListener((obs, oldVal, newVal) -> {
        if (fieldName.equals("dueDate")) {
          System.out.println("Old: " + oldVal + ", New: " + newVal);
        }
        if ((onBlur && oldVal && !newVal) | (!onBlur && newVal)) {
          touchedProperty.set(true);
        }
      });
    }
  }

  private void setupValidation() {
    this.touchedMap.put("name", new SimpleBooleanProperty(false));
    this.touchedMap.put("description", new SimpleBooleanProperty(false));
    this.touchedMap.put("workWeight", new SimpleBooleanProperty(false));
    this.touchedMap.put("timeWeight", new SimpleBooleanProperty(false));
    this.touchedMap.put("dueDate", new SimpleBooleanProperty(false));
    this.touchedMap.put("dueDateTime", new SimpleBooleanProperty(false));
    this.touchedMap.put("recurring", new SimpleBooleanProperty(false));
    this.touchedMap.put("recurringDays", new SimpleBooleanProperty(false));
    this.addTouchedListener("name", this.nameField.focusedProperty(), true);
    this.addTouchedListener("description", this.descriptionField.focusedProperty(), true);
    this.addTouchedListener("workWeight", this.workWeightSpinner.focusedProperty(), true);
    this.addTouchedListener("timeWeight", this.timeWeightSpinner.focusedProperty(), true);
    this.addTouchedListener("dueDate", this.dueDatePicker.focusedProperty(), false);
    this.addTouchedListener("dueDateTime", this.dueDateTimeComboBox.focusedProperty(), false);
    this.addTouchedListener("recurring", this.recurringCheckbox.selectedProperty(), true);
    this.addTouchedListener("recurringDays", this.recurringDaysSpinner.focusedProperty(), true);
    validator.createCheck()
        .dependsOn("name", this.nameField.textProperty())
        .dependsOn("touched", this.touchedMap.get("name"))
        .withMethod(c -> {
          if (((String) c.get("name")).isEmpty()) {
            if ((Boolean) c.get("touched")) {
              c.error("Task name cannot be empty");
            }
          }
        })
        .decorates(this.nameField)
        .decoratingWith(FormDecoration.getFactory())
        .immediate();
    validator.createCheck()
        .dependsOn("recurring", this.recurringCheckbox.selectedProperty())
        .dependsOn("recurringDays", this.recurringDaysSpinner.getValueFactory().valueProperty())
        .dependsOn("touched", this.touchedMap.get("recurringDays"))
        .withMethod(c -> {
          Boolean isRecurring = c.get("recurring");
          Integer recurringDays = c.get("recurringDays");
          boolean touched = c.get("touched");
          if (!touched) {
            return;
          }
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
        .decoratingWith(FormDecoration.getFactory())
        .immediate();
    validator.createCheck()
        .dependsOn("dueDate", this.dueDatePicker.valueProperty())
        .dependsOn("dueDateTime",
            this.dueDateTimeComboBox.getSelectionModel().selectedItemProperty())
        .dependsOn("touched", this.touchedMap.get("dueDate"))
        .withMethod(c -> {
          LocalDateTime now = LocalDateTime.now();
          LocalDate dueDate = c.get("dueDate");
          String selectedTimeString = c.get("dueDateTime");
          boolean touched = c.get("touched");
          if (!touched) {
            return;
          }
          if (dueDate == null) {
            return;
          }
          LocalDateTime dueDateTime = dueDate.atTime(
              Integer.parseInt(selectedTimeString.split(":")[0]),
              Integer.parseInt(selectedTimeString.split(":")[1])
          );
          if (dueDateTime.isBefore(now)) {
            c.error("Due date cannot be in the past");
          }

        })
        .decorates(this.dueDatePicker)
        .decoratingWith(FormDecoration.getFactory())
        .immediate();
    validator.createCheck()
        .dependsOn("workWeight",
            this.workWeightSpinner.getSelectionModel().selectedItemProperty())
        .dependsOn("touched", this.touchedMap.get("workWeight"))
        .withMethod(c -> {
          WorkWeight workWeight = c.get("workWeight");
          boolean touched = c.get("touched");
          if (!touched) {
            return;
          }
          if (workWeight == null) {
            c.error("Work weight must be selected");
          }
        })
        .decorates(this.workWeightSpinner)
        .decoratingWith(FormDecoration.getFactory())
        .immediate();
    validator.createCheck()
        .dependsOn("timeWeight",
            this.timeWeightSpinner.getSelectionModel().selectedItemProperty())
        .dependsOn("touched", this.touchedMap.get("timeWeight"))
        .withMethod(c -> {
          TimeWeight timeWeight = c.get("timeWeight");
          boolean touched = c.get("touched");
          if (!touched) {
            return;
          }
          if (timeWeight == null) {
            c.error("Time weight must be selected");
          }
        })
        .decorates(this.timeWeightSpinner)
        .decoratingWith(FormDecoration.getFactory())
        .immediate();
  }

  private VBox createSpinnerWithLabel(String label, Node spinner) {
    VBox box = new VBox(10);
    Text labelText = new Text(label, Text.TextType.FORM_LABEL);
    box.getChildren().addAll(labelText, spinner);
    return box;
  }

  private <T> EnumCombobox<T> createWeightSpinner(Class<T> enumClass) {
    return EnumCombobox.of(enumClass);
  }

  private void setupEventHandlers() {
    this.recurringCheckbox.selectedProperty().addListener((obs, old, newVal) -> {
      this.recurringDaysSpinner.setDisable(!newVal);
      this.recurringDaysBox.setVisible(newVal);
      this.recurringDaysBox.setManaged(newVal);
    });

    this.createButton.setOnAction(e -> this.handleCreate());
    this.cancelButton.setOnAction(e -> {
      this.setModalCancelled();
    });
  }


  private void handleCreate() {
    if (this.nameField.getText().trim().isEmpty()) {
      return;
    }

    if (!Auth.getInstance().isAuthenticated()) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText("You are not logged in");
      alert.setContentText("Please log in to create a task");
      alert.show();
      return;
    }
    LocalDateTime dueDate = this.dueDatePicker.getValue() != null ?
        this.dueDatePicker.getValue().atTime(
            Integer.parseInt(this.dueDateTimeComboBox.getSelectionModel()
                .getSelectedItem().split(":")[0]),
            Integer.parseInt(this.dueDateTimeComboBox.getSelectionModel()
                .getSelectedItem().split(":")[1])
        ) :
        null;
    CreateTaskRequest task = CreateTaskRequest.builder()
        .name(this.nameField.getText().trim())
        .description(this.descriptionField.getText().trim())
        .workWeight(this.workWeightSpinner.getValue())
        .timeWeight(this.timeWeightSpinner.getValue())
        .dueAt(dueDate)
        .isRecurring(this.recurringCheckbox.isSelected())
        .recurrenceIntervalDays(this.recurringCheckbox.isSelected() ?
            this.recurringDaysSpinner.getValue() : 0)
        .createdById(Auth.getInstance().getProfileId())
        .build();

    this.closeWithResult(task);
  }

  private void resetForm() {
    for (String fieldName : this.touchedMap.keySet()) {
      this.touchedMap.get(fieldName).set(false);
    }
    this.nameField.clear();
    this.descriptionField.clear();
    this.workWeightSpinner.getSelectionModel().select(WorkWeight.EASY);
    this.timeWeightSpinner.getSelectionModel().select(TimeWeight.SHORT);
    this.dueDatePicker.setValue(null);
    this.recurringCheckbox.setSelected(false);
    this.recurringDaysSpinner.getValueFactory().setValue(7);
  }

  @Override
  protected void onNavigatedTo() {
    this.resetForm();
    this.nameField.requestFocus();
    super.onNavigatedTo();
  }
}