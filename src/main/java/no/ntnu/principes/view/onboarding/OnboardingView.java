package no.ntnu.principes.view.onboarding;

import static no.ntnu.principes.Launcher.APP_NAME;

import atlantafx.base.controls.CustomTextField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.secondary.SelectableGroup;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.household.HouseholdType;
import no.ntnu.principes.domain.onboarding.OnboardingDetails;
import no.ntnu.principes.domain.onboarding.OnboardingStep;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.Throttle;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

public class OnboardingView extends BaseScreen {
  public static final String SCREEN_ID = "onboarding";
  private static final int MIN_TEAM_NAME_LENGTH = 3;
  private static final int MIN_MEMBER_NAME_LENGTH = 2;
  private static final Duration TRANSITION_DURATION = Duration.millis(200);
  private double prevX = 1;

  private List<OnboardingStep> steps;
  private int currentStepIndex = 0;
  private StackPane contentContainer;
  private Button nextButton;
  private Button prevButton;

  private ScrollPane scrollPane;

  private OnboardingDetails form;
  private final ConfigValueRepository configValueRepository;

  public OnboardingView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.initializeLayout();
    this.configValueRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
  }

  @Override
  protected void initializeScreen() {
    this.form = new OnboardingDetails();
    this.steps = this.createSteps();
    this.contentContainer = new StackPane();
    this.initializeLayout();
    this.getChildren().addAll(this.createMainContent(), this.createRightBar());
    this.showCurrentStep(true);
    this.registerListeners();
    this.requestFocus();
  }

  private List<OnboardingStep> createSteps() {
    ArrayList<OnboardingStep> stepsList = new ArrayList<>();
    if (!this.configValueRepository.getValueOrDefault("onboardingComplete", false).get()
        .getBooleanValue()) {

      // Initial information about the application.
      stepsList.add(new OnboardingStep(
          "Welcome to " + APP_NAME,
          "",
          this.createInformationalContent(),
          () -> true
      ));

      // Initial setup with household type
      stepsList.add(new OnboardingStep(
          "Let's get set up",
          String.format("Who will be using %s?", APP_NAME),
          this.createUserTypeSelection(),
          () -> this.form.householdType() != null
      ));

      // Naming the household / griyup
      stepsList.add(new OnboardingStep(
          "Give yourselves a name!",
          "Your last name, a fun group name, company- / workplace name, etc.",
          this.createEnterGroupNameForm(),
          () -> {
            String name = this.form.householdName();
            return name != null && !name.isBlank() && name.length() >= MIN_TEAM_NAME_LENGTH;
          }
      ));

      // Add members
      stepsList.add(new OnboardingStep(
          "Add members",
          String.format(
              "Set up your household by adding the names of the people who will be using %s",
              APP_NAME),
          this.createAddMembersForm(),
          // At least one member with name
          () -> this.form.members().stream()
              .anyMatch(name -> name != null && !name.isBlank() &&
                  name.length() >= MIN_MEMBER_NAME_LENGTH)
      ));
    } else {
      stepsList.add(new OnboardingStep(
          "Welcome back!",
          String.format("You've already set up %s. Please continue to login.", APP_NAME),
          new VBox(),
          () -> true
      ));
    }

    return stepsList;
  }

  private VBox createInformationalContent() {
    VBox content = new VBox(10);
    content.setAlignment(Pos.CENTER_LEFT);

    Text text = new Text(
        String.format(
            "%s is a household management application that helps you organize tasks and manage your"
                + " home.",
            APP_NAME),
        StyledText.TextType.FORM_LABEL);
    text.setWrapText(true);
    text.setTextAlignment(TextAlignment.LEFT);

    Text text2 = new Text(
        "You can create tasks and assign them to your household members, and follow through on"
            + " their completion.",
        StyledText.TextType.FORM_LABEL);

    text2.setWrapText(true);
    text2.setTextAlignment(TextAlignment.LEFT);

    text.setStyle("-fx-font-size: 1.5em;");
    text2.setStyle("-fx-font-size: 1.5em;");

    content.getChildren().addAll(text, text2);
    return content;
  }

  private Node createAddMembersForm() {
    VBox form = new VBox(10);
    form.setAlignment(Pos.CENTER);

    this.scrollPane = new ScrollPane();
    this.scrollPane.setFitToWidth(true);
    this.scrollPane.setFitToHeight(true);
    this.scrollPane.setPadding(InsetBuilder.create().right(20).build());
    this.scrollPane.setMaxHeight(300);
    this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    VBox membersContainer = new VBox(10);
    membersContainer.setAlignment(Pos.CENTER);
    this.scrollPane.setContent(membersContainer);

    this.updateMembersForm(membersContainer);

    Button addMemberButton = new Button("Add member", Button.ButtonType.OUTLINED);
    addMemberButton.setOnAction(e -> {
      this.form.newMember();
      this.updateMembersForm(membersContainer);

      CustomTextField nextField =
          (CustomTextField) membersContainer.lookup(
              "#member-" + (this.form.members().size() - 1));
      if (nextField != null) {
        nextField.requestFocus();
      } else {
        this.nextButton.fire();
      }
    });

    form.getChildren().addAll(this.scrollPane, this.createShortCutHelpText(), addMemberButton);

    return form;
  }

  private void updateMembersForm(VBox membersContainer) {
    if (membersContainer.getChildren().size() < this.form.members().size()) {
      // Handle addition case
      for (int i = membersContainer.getChildren().size(); i < this.form.members().size(); i++) {
        membersContainer.getChildren().add(this.createMemberRow(i, membersContainer));
      }
    } else if (membersContainer.getChildren().size() > this.form.members().size()) {
      // Handle removal case
      membersContainer.getChildren().clear();
      for (int i = 0; i < this.form.members().size(); i++) {
        membersContainer.getChildren().add(this.createMemberRow(i, membersContainer));
      }
    }
    Throttle.debounce("update-members-form", () -> {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          scrollPane.setVvalue(1.0);

          // Enable / disable the first row delete button basedo nn the number of members
          Button removeButton =
              (Button) ((HBox) membersContainer.getChildren().getFirst()).getChildren().get(2);
          removeButton.setDisable(form.members().size() <= 1);
        }
      });
    }, 100);
  }

  private void updateSingleMemberData(int index, VBox membersContainer) {
    Optional<Image> cachedImage =
        AvatarManager.getCachedAvatarForName(this.form.members().get(index));
    if (cachedImage.isPresent()) {
      // Update the image immediately
      HBox row = (HBox) membersContainer.getChildren().get(index);
      ImageView imageView = (ImageView) row.getChildren().get(0);
      imageView.setImage(cachedImage.get());
      return;
    }

    // Debounce the update to prevent spamming the API
    Throttle.debounce("update-member-" + index, () -> {
      HBox row = (HBox) membersContainer.getChildren().get(index);
      ImageView imageView = (ImageView) row.getChildren().get(0);

      // Use AvatarManager to get cached image
      Image image = AvatarManager.getAvatarForName(this.form.members().get(index));
      imageView.setImage(image);
    }, 500);
  }

  private HBox createMemberRow(int index, VBox membersContainer) {
    HBox row = new HBox(10);

    // Create avatar
    ImageView imageView = new ImageView();
    imageView.setFitWidth(40);
    imageView.setFitHeight(40);
    imageView.setPreserveRatio(true);
    String name = this.form.members().get(index);
    Image image = AvatarManager.getAvatarForName(name.isBlank() ? ("member-" + index) : name);
    imageView.setImage(image);
    // Transparent background
    imageView.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

    // Create and configure text field
    CustomTextField textField = new CustomTextField(this.form.members().get(index));
    textField.setPromptText("Name");
    textField.setId("member-" + index);
    textField.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        if (index < this.form.members().size() - 1) {
          // Focus the next row's text field
          CustomTextField nextField =
              (CustomTextField) membersContainer.lookup("#member-" + (index + 1));
          if (nextField != null) {
            nextField.requestFocus();
          } else {
            this.nextButton.fire();
          }
        } else if (index == this.form.members().size() - 1) {
          // Add a new member
          this.form.newMember();
          this.updateMembersForm(membersContainer);
          // Focus the new row's text field
          CustomTextField nextField =
              (CustomTextField) membersContainer.lookup("#member-" + (index + 1));
          if (nextField != null) {
            nextField.requestFocus();
          }
        }
      } else if (e.getCode().isArrowKey()) {
        if (e.getCode() == KeyCode.DOWN) {
          // Focus the next row's text field
          CustomTextField nextField =
              (CustomTextField) membersContainer.lookup("#member-" + (index + 1));
          if (nextField != null) {
            nextField.requestFocus();
          }
        } else if (e.getCode() == KeyCode.UP) {
          // Focus the previous row's text field
          CustomTextField prevField =
              (CustomTextField) membersContainer.lookup("#member-" + (index - 1));
          if (prevField != null) {
            prevField.requestFocus();
          }
        }
      }
    });
    textField.setOnKeyTyped(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        textField.getParent().requestFocus();
        return;
      }
      e.consume();
      String text = textField.getText();

      this.form.setMember(index, text);
      this.updateNavigationButtons();
      this.updateSingleMemberData(index, membersContainer);
    });

    StyleManager.apply(textField, StyleManager.InputStyle.INPUT);
    StyleManager.growHorizontal(textField, row);

    // Create remove button
    Button removeButton = new Button("Remove", Button.ButtonType.OUTLINED);
    removeButton.setPadding(InsetBuilder.symmetric(15, 10).build());
    removeButton.setGraphic(new FontIcon(Material2OutlinedAL.DELETE));
    removeButton.setOnAction(e -> {
      if (textField.getText().isBlank()) {
        this.form.removeMember(index);
        this.updateMembersForm(membersContainer);
        this.updateNavigationButtons();
        return;
      }
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Remove member");
      confirm.setHeaderText("Are you sure you want to remove this member?");
      confirm.setContentText("Press OK if you meant to do this.");
      confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          this.form.removeMember(index);
          this.updateMembersForm(membersContainer);
          this.updateNavigationButtons();
        }
      });
    });
    removeButton.setDisable(this.form.members().size() <= 1);
    imageView.setStyle("-fx-border-radius: 50%;");

    row.getChildren().addAll(imageView, textField, removeButton);
    return row;
  }

  private VBox createEnterGroupNameForm() {
    VBox form = new VBox(10);
    form.setAlignment(Pos.CENTER);

    CustomTextField textField = new CustomTextField(this.form.householdName() == null ? "" :
        this.form.householdName());
    textField.setPromptText("");
    textField.setId("household-name");
    textField.textProperty()
        .addListener((obs, oldVal, newVal) -> {
          this.form.setHouseholdName(newVal);
          this.updateNavigationButtons();
        });
    // On enter, move to next step if possible
    textField.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER && !this.nextButton.isDisable()) {
        this.nextButton.fire();
      }
    });
    StyleManager.apply(textField, StyleManager.InputStyle.INPUT);

    form.getChildren().add(textField);
    return form;
  }

  private void initializeLayout() {
    this.setSpacing(0);
    this.setMaxWidth(Double.MAX_VALUE);
    this.controller.getStage().setTitle("Onboarding");
  }

  private VBox createMainContent() {
    VBox content = new VBox(5);
    content.setPadding(InsetBuilder.uniform(20).build());
    content.setAlignment(Pos.CENTER);
    HBox.setHgrow(content, Priority.ALWAYS);

    VBox centerContent = this.createCenterContent();
    StyleManager.growVertical(centerContent);

    // Bottom navigation
    VBox navigationContent = new VBox(5);
    StyleManager.growHorizontal(navigationContent);
    navigationContent.setMaxWidth(CONTENT_WIDTH + (2 * 20));

    navigationContent.setPadding(InsetBuilder.uniform(20).build());
    navigationContent.setAlignment(Pos.BOTTOM_LEFT);
    navigationContent.getChildren().add(this.createNavigationButtons());


    content.getChildren().addAll(centerContent, navigationContent);

    return content;
  }

  private VBox createShortCutHelpText() {
    VBox shortcutBox = new VBox(10);
    shortcutBox.setAlignment(Pos.CENTER);
    shortcutBox.setPadding(InsetBuilder.create().top(20).build());

    Text enterShortcutText =
        new Text("ENTER - Focus next name",
            StyledText.TextType.FORM_LABEL);
    enterShortcutText.setTextAlignment(TextAlignment.CENTER);
    enterShortcutText.setWrapText(true);

    Text tabShortcutText =
        new Text("TAB - Switch focus",
            StyledText.TextType.FORM_LABEL);
    tabShortcutText.setTextAlignment(TextAlignment.CENTER);
    tabShortcutText.setWrapText(true);

    shortcutBox.getChildren().addAll(enterShortcutText, tabShortcutText);
    return shortcutBox;
  }

  private VBox createCenterContent() {
    VBox centerContent = new VBox(0);
    centerContent.setAlignment(Pos.CENTER);
    centerContent.setMaxWidth(CONTENT_WIDTH);
    centerContent.setMinWidth(CONTENT_WIDTH);
    centerContent.setPrefWidth(CONTENT_WIDTH);

    this.contentContainer.setMaxWidth(CONTENT_WIDTH);

    centerContent.getChildren().add(
        this.contentContainer
    );

    return centerContent;
  }

  private HBox createNavigationButtons() {
    HBox navigationBox = new HBox(10);
    navigationBox.setAlignment(Pos.CENTER);
    navigationBox.setPadding(InsetBuilder.create().top(20).build());

    this.prevButton = new Button("Previous", Button.ButtonType.FLAT);
    this.prevButton.setOnAction(e -> this.navigateToPrevious());
    this.prevButton.setVisible(false);
    this.prevButton.setManaged(false);

    this.nextButton = new Button("Next", Button.ButtonType.DEFAULT);
    this.nextButton.setOnAction(e -> this.navigateToNext());
    this.nextButton.setDisable(!this.steps.get(this.currentStepIndex).validate().get());
    StyleManager.growHorizontal(this.nextButton, navigationBox);

    navigationBox.getChildren().addAll(this.prevButton, this.nextButton);
    return navigationBox;
  }

  private SelectableGroup createUserTypeSelection() {
    List<SelectableGroup.SelectableGroupItem> items = List.of(
        new SelectableGroup.SelectableGroupItem(HouseholdType.FAMILY.name(), "Family", false),
        new SelectableGroup.SelectableGroupItem(HouseholdType.SHARED.name(), "Shared Living",
            false),
        new SelectableGroup.SelectableGroupItem(HouseholdType.WORKPLACE.name(), "Workplace",
            false)
    );

    return new SelectableGroup(items, this::handleSelection);
  }

  private void handleSelection(String id) {
    if (id != null) {
      this.form.setHouseholdType(HouseholdType.valueOf(id));
    } else {
      this.form.setHouseholdType(null);
    }
    this.updateNavigationButtons();
  }

  private void navigateToNext() {
    if (this.currentStepIndex < this.steps.size() - 1) {
      this.currentStepIndex++;
      this.showCurrentStep(true);
      this.updateNavigationButtons();
//      if (this.currentStepIndex == this.steps.size() - 1) {
//        this._animateArneIn();
//      }
    } else {
      try {
        if (this.configValueRepository.getValueOrDefault("onboardingComplete", false).get()
            .getBooleanValue()) {
          NavigationService.navigate("selectProfile");
          return;
        }
        NavigationService.navigate("onboardingSuccess", new HashMap<>() {{
          put("onboardingDetails", form.clean());
        }});
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void navigateToPrevious() {
    if (this.currentStepIndex > 0) {
      this.currentStepIndex--;
      this.showCurrentStep(false);
      this.updateNavigationButtons();
    }
  }

  private void updateNavigationButtons() {
    this.prevButton.setVisible(this.currentStepIndex > 0);
    this.prevButton.setManaged(this.currentStepIndex > 0);
    this.nextButton.setText(
        this.currentStepIndex == this.steps.size() - 1 ? "Complete" : "Next");
    this.nextButton.setDisable(!this.steps.get(this.currentStepIndex).validate().get());
    if (!this.nextButton.isDisable() && this.currentStepIndex == 0) {
      this.nextButton.requestFocus();
    }
  }

  private void showCurrentStep(boolean forward) {
    OnboardingStep step = this.steps.get(this.currentStepIndex);
    OnboardingStep otherStep =
        forward ? this.currentStepIndex > 0 ? this.steps.get(this.currentStepIndex - 1) : null :
            this.currentStepIndex < this.steps.size() - 1 ?
                this.steps.get(this.currentStepIndex + 1) : null;
    VBox stepContent = this.createStepContent(step);
    VBox otherStepContent = otherStep != null ? this.createStepContent(otherStep) : null;

    // new step
    TranslateTransition translate = new TranslateTransition(TRANSITION_DURATION, stepContent);
    FadeTransition fade = new FadeTransition(TRANSITION_DURATION, stepContent);

    // previous/next step
    TranslateTransition otherTranslate = otherStepContent != null ?
        new TranslateTransition(TRANSITION_DURATION, otherStepContent) : null;
    FadeTransition otherFade = otherStepContent != null ?
        new FadeTransition(TRANSITION_DURATION, otherStepContent) : null;

    // initial state for new step
    stepContent.setTranslateX(forward ? CONTENT_WIDTH : -CONTENT_WIDTH);
    stepContent.setOpacity(0);

    // initial state for prev/next step
    if (otherStepContent != null) {
      otherStepContent.setTranslateX(0);
      otherStepContent.setOpacity(1);
    }

    // Ofinal state for new step
    translate.setToX(0);
    fade.setToValue(1);

    // final state for prev/next step
    if (otherStepContent != null) {
      otherTranslate.setToX(forward ? -CONTENT_WIDTH : CONTENT_WIDTH);
      otherFade.setToValue(0);
    }

    // content container
    this.contentContainer.getChildren().clear();
    if (otherStepContent != null) {
      this.contentContainer.getChildren().add(otherStepContent);
    }
    this.contentContainer.getChildren().add(stepContent);

    // transitions for both steps
    ParallelTransition newStepTransition = new ParallelTransition(translate, fade);
    ParallelTransition oldStepTransition = otherStepContent != null ?
        new ParallelTransition(otherTranslate, otherFade) : null;

    //  all transitions
    ParallelTransition sequence = new ParallelTransition();
    if (oldStepTransition != null) {
      sequence.getChildren().addAll(oldStepTransition, newStepTransition);
    } else {
      sequence.getChildren().add(newStepTransition);
    }

    // Clean up
    sequence.setOnFinished(event -> {
      this.contentContainer.getChildren().clear();
      this.contentContainer.getChildren().add(stepContent);
      if (this.currentStepIndex == 2) {
        try {
          stepContent.lookup("#household-name").requestFocus();
        } catch (Exception ignored) {
        }
      } else if (this.currentStepIndex == 3) {
        try {
          stepContent.lookup("#member-0").requestFocus();
        } catch (Exception ignored) {
        }
      }
    });

    sequence.play();
  }

  private VBox createStepContent(OnboardingStep step) {
    VBox content = new VBox(5);
    content.setAlignment(Pos.CENTER);

    Text header = new Text(step.title(), Text.TextType.PAGE_TITLE);

    Text subtitle = new Text(step.subtitle(), Text.TextType.SUBHEADER);
    subtitle.setWrapText(true);
    subtitle.setTextAlignment(TextAlignment.CENTER);
    subtitle.setLineSpacing(-8);
    VBox.setMargin(subtitle, InsetBuilder.create().top(-5).bottom(30).build());

    content.getChildren().addAll(header, subtitle, step.content());
    return content;
  }

  private VBox createRightBar() {
    VBox rightBar = new VBox(20);
    rightBar.setStyle("-fx-background-color: #e1c89f;");
    rightBar.setPrefWidth(RIGHT_BAR_WIDTH);
    rightBar.setMaxWidth(RIGHT_BAR_WIDTH);
    rightBar.setMinWidth(RIGHT_BAR_WIDTH);
    rightBar.setAlignment(Pos.CENTER);
    Image hestia =
        new Image(
            getClass().getResource("/no/ntnu/principes/images/hestia.png").toExternalForm());
    ImageView hestiaView = new ImageView(hestia);
    hestiaView.fitWidthProperty().bind(rightBar.widthProperty().multiply(0.95));
    hestiaView.setPreserveRatio(true);
    hestiaView.setFitHeight(RIGHT_BAR_WIDTH);

    VBox imageContainer = new VBox();
    imageContainer.setAlignment(Pos.CENTER);

    imageContainer.getChildren().addAll(hestiaView);
    rightBar.getChildren().add(imageContainer);

    HBox.setHgrow(rightBar, Priority.SOMETIMES);
    return rightBar;
  }

  private void registerListeners() {
    this.setOnKeyPressed(e -> {
      switch (e.getCode()) {
        case ESCAPE, LEFT -> this.prevButton.fire();
        case RIGHT -> this.nextButton.fire();
        default -> {
        }
      }
    });
  }
}

