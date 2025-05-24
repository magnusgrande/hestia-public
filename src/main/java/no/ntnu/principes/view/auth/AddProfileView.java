package no.ntnu.principes.view.auth;

import atlantafx.base.controls.CustomTextField;
import java.util.Objects;
import java.util.Optional;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
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
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.onboarding.OnboardingDetails;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

public class AddProfileView extends BaseScreen {
  private static final double CONTENT_WIDTH = 468;
  private static final double RIGHT_BAR_WIDTH = 500;

  private final StackPane contentContainer;
  private Button createProfileButton;
  private StringProperty householdName = new SimpleStringProperty("");
  private CustomTextField nameField;
  private Text nameError;
  private final MemberRepository memberRepository;
  private final ConfigValueRepository configValueRepository;
  private double prevX = 1;

  public AddProfileView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.contentContainer = new StackPane();
    this.memberRepository = DatabaseManager.getInstance().getRepository(MemberRepository.class);
    this.configValueRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
  }

  @Override
  protected void initializeScreen() {
    this.setSpacing(0);
    this.setMaxWidth(Double.MAX_VALUE);
    new ConfigValueBinder(this.configValueRepository).bindString("householdName", "Not set",
        this.householdName);
    this.getChildren().addAll(createMainContent(), createRightBar());
    this.requestFocus();
  }

  @Override
  protected void onNavigatedTo() {
    // Reset the form when navigating to this screen
    if (this.nameField != null) {
      this.nameField.setText("");
    }
    this.requestFocus();
  }

  private VBox createMainContent() {
    VBox content = new VBox(5);
    content.setPadding(InsetBuilder.uniform(20).build());
    content.setAlignment(Pos.CENTER);
    setHgrow(content, Priority.ALWAYS);

    VBox centerContent = createCenterContent();
    StyleManager.growVertical(centerContent);

    // Get form data from context
    OnboardingDetails form = getContext().getParameter("onboardingDetails");

    // Text
    Text header = new Text("",
        Text.TextType.PAGE_TITLE);
    header.textProperty().bind(this.householdName);

    Text subtitle = new Text("What is your name?", Text.TextType.SUBHEADER);
    subtitle.setWrapText(true);
    subtitle.setTextAlignment(TextAlignment.CENTER);
    subtitle.setLineSpacing(-8);
    VBox.setMargin(subtitle, InsetBuilder.create().top(-5).bottom(30).build());

    // Name field
    this.nameField = new CustomTextField("");
    StyleManager.apply(this.nameField, StyleManager.InputStyle.INPUT);
    StyleManager.growHorizontal(this.nameField);
    VBox.setMargin(this.nameField, InsetBuilder.create().bottom(20).build());

    this.setupNameFieldListeners();
    this.nameError = new Text("", StyledText.TextType.FORM_LABEL, "error");
    this.nameError.setVisible(false);
    this.nameError.setManaged(false);
    VBox.setMargin(this.nameError, InsetBuilder.create().bottom(10).build());

    // Buttons
    this.createProfileButton = new Button("Let's Start!", Button.ButtonType.DEFAULT);
    this.createProfileButton.setDisable(true);
    StyleManager.growHorizontal(this.createProfileButton);
    this.createProfileButton.setOnAction(e -> this.handleCreateProfile());

    Button cancelButton = new Button("I already have a profile", Button.ButtonType.FLAT);
    cancelButton.setOnAction(e -> handleCancel());
    VBox.setMargin(cancelButton, InsetBuilder.create().top(10).build());

    centerContent.getChildren()
        .addAll(header, subtitle, this.nameError, this.nameField, this.createProfileButton,
            cancelButton);

    content.getChildren().add(centerContent);
    return content;
  }

  private void setupNameFieldListeners() {
    this.nameField.textProperty().addListener((obs, oldVal, newVal) -> {
      this.createProfileButton.setDisable(newVal.isBlank() || newVal.length() < 2);
      if (this.nameError.isVisible()) {
        this.nameError.setVisible(false);
        this.nameError.setManaged(false);
        StyleManager.unapply(this.nameField, StyleManager.InputStyle.PseudoClass.ERROR);
      }
    });

    this.nameField.setOnKeyPressed(e -> {
      if (e.getCode().equals(KeyCode.ENTER)) {
        e.consume();
        this.handleCreateProfile();
      }
    });
  }

  private void handleCreateProfile() {
    if (this.nameField.getText().isBlank() || this.nameField.getText().length() < 2) {
      return;
    }

    // Create the new user if not exists.
    Optional<Profile> existingProfile =
        this.memberRepository.findByName(this.nameField.getText());
    if (existingProfile.isPresent()) {
      System.out.println("Profile already exists");
      StyleManager.apply(this.nameField, StyleManager.InputStyle.PseudoClass.ERROR);
      this.nameError.setText("Profile already exists");
      this.nameError.setVisible(true);
      this.nameError.setManaged(true);
      return;
    }
    Profile profile = this.memberRepository.save(Profile.builder()
        .name(this.nameField.getText())
        .avatarHash(AvatarManager.getHashFor(this.nameField.getText()))
        .build());

    // Store updated details and selected index
    this.getContext().setParameter("selectedProfileId", profile.getId());

    // Navigate back to select profile
    NavigationService.navigate("selectProfile", this.getContext().getParameters());
  }

  private void handleCancel() {
    NavigationService.navigate("selectProfile");
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

  private VBox createRightBar() {
    VBox rightBar = new VBox(20);
    rightBar.setStyle("-fx-background-color: #e1c89f;");
    rightBar.setPrefWidth(RIGHT_BAR_WIDTH);
    rightBar.setMaxWidth(RIGHT_BAR_WIDTH);
    rightBar.setMinWidth(RIGHT_BAR_WIDTH);
    rightBar.setAlignment(Pos.BOTTOM_CENTER);
    Image hestia =
        new Image(
            Objects.requireNonNull(getClass().getResource("/no/ntnu/principes/images/hestia.png"))
                .toExternalForm());
    ImageView hestiaView = new ImageView(hestia);
    // Flip the image horizontally
    hestiaView.setScaleX(prevX);
    hestiaView.fitWidthProperty().bind(rightBar.widthProperty().multiply(0.95));
    hestiaView.setPreserveRatio(true);
    hestiaView.setFitHeight(RIGHT_BAR_WIDTH);

    VBox imageContainer = new VBox();
    imageContainer.setAlignment(Pos.BOTTOM_CENTER);
    imageContainer.setPadding(InsetBuilder.create().bottom(40).build());
    HBox sparkleContainer = new HBox();
    Image sparkle1 =
        new Image(
            Objects.requireNonNull(
                    getClass().getResource("/no/ntnu/principes/images/sparkle.png"))
                .toExternalForm());
    StyleManager.growHorizontal(imageContainer, sparkleContainer);
    ImageView sparkleView1 = new ImageView(sparkle1);
    sparkleView1.setFitWidth(150);
    sparkleView1.setPreserveRatio(true);
    ImageView sparkleView2 = new ImageView(sparkle1);
    sparkleView2.setFitWidth(150);
    sparkleView2.setPreserveRatio(true);

    HBox sparkleWrapper1 = new HBox();
    HBox sparkleWrapper2 = new HBox();
    sparkleWrapper2.setAlignment(Pos.CENTER_RIGHT);
    sparkleWrapper1.getChildren().add(sparkleView1);
    sparkleWrapper2.getChildren().add(sparkleView2);
    StyleManager.growHorizontal(sparkleWrapper1, sparkleWrapper2);
    sparkleWrapper2.setTranslateY(-50);
    sparkleContainer.getChildren().addAll(sparkleWrapper1, sparkleWrapper2);

    hestiaView.setOnMousePressed(e -> {
      ScaleTransition press = new ScaleTransition(Duration.millis(100), hestiaView);
      prevX = prevX * -1;
      press.setToX(prevX < 0 ? -0.95 : 0.95);
      press.setToY(0.95);
      press.play();
    });

    hestiaView.setOnMouseReleased(e -> {
      ScaleTransition release = new ScaleTransition(Duration.millis(100), hestiaView);
      release.setToX(prevX);
      release.setToY(1.0);
      release.play();
    });


    imageContainer.getChildren().addAll(sparkleContainer, hestiaView);
    rightBar.getChildren().add(imageContainer);

    HBox.setHgrow(rightBar, Priority.SOMETIMES);
    return rightBar;
  }
}

