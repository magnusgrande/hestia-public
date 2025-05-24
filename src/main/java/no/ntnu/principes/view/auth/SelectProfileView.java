package no.ntnu.principes.view.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.components.secondary.ProfileRowInCombobox;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.event.HouseholdNameChangedEvent;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.ConfigValueBinder;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

@Slf4j
public class SelectProfileView extends BaseScreen {
  private static final double CONTENT_WIDTH = 468;
  private static final double RIGHT_BAR_WIDTH = 500;

  private final StackPane contentContainer;
  private ComboBox<Profile> profileComboBox;
  private Button signInButton;
  private final ConfigValueRepository configValueRepository;
  private final MemberRepository memberRepository;
  private StringProperty householdName = new SimpleStringProperty("Not Set");
  private List<Profile> members;
  private Integer prevX = 1;

  public SelectProfileView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.contentContainer = new StackPane();
    DatabaseManager databaseManager = DatabaseManager.getInstance();
    this.configValueRepository = databaseManager.getRepository(ConfigValueRepository.class);
    this.memberRepository = databaseManager.getRepository(MemberRepository.class);
  }

  @Override
  protected void initializeScreen() {
    log.debug("Initializing SelectProfile screen");
    this.setSpacing(0);
    this.setMaxWidth(Double.MAX_VALUE);
    this.getChildren().addAll(this.createMainContent(), this.createRightBar());
    this.requestFocus();
    if (!this.isInitialized()) {
      this.householdName.addListener(
          (obs, oldVal, newVal) -> {
            PrincipesEventBus.getInstance().publish(
                HouseholdNameChangedEvent.to(newVal)
            );
          });
    }
  }

  @Override
  protected void onNavigatedTo() {
    // Get onboarding details from context when navigating to this screen
    this.members = this.memberRepository.findAll();
    AvatarManager.preloadAvatars(
        this.members.stream().map(Profile::getAvatarHash).toArray(String[]::new));
    this.updateProfileList();
    this.requestFocus();
  }

  private void updateProfileList() {
    log.debug("Updating profile list");
    List<Profile> profiles = new ArrayList<>();
    profiles.add(Profile.builder().name("Select your profile").id(-1L).build());
    if (this.members != null) {
      profiles.addAll(this.members);
    }
    this.profileComboBox.setItems(profiles.stream()
        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    Long selectedProfileId = getContext().getParameter("selectedProfileId");
    if (selectedProfileId != null) {
      this.profileComboBox.getSelectionModel().select(profiles.stream().filter(
          p -> p.getId().equals(selectedProfileId)).findFirst().orElse(null));
    } else {
      this.profileComboBox.getSelectionModel().selectFirst();
    }
  }

  private VBox createMainContent() {
    log.debug("Creating main content for SelectProfile screen");
    VBox content = new VBox(5);
    content.setPadding(InsetBuilder.uniform(20).build());
    content.setAlignment(Pos.CENTER);
    setHgrow(content, Priority.ALWAYS);

    VBox centerContent = this.createCenterContent();
    StyleManager.growVertical(centerContent);

    // Text
    Text header =
        new Text(this.householdName.getValue(), Text.TextType.PAGE_TITLE);
    new ConfigValueBinder(this.configValueRepository).bindString("householdName", "Not set",
        header.textProperty());

    Text subtitle =
        new Text("Select your profile", Text.TextType.SUBHEADER);
    subtitle.setWrapText(true);
    subtitle.setTextAlignment(TextAlignment.CENTER);
    subtitle.setLineSpacing(-8);
    VBox.setMargin(subtitle, InsetBuilder.create().top(-5).bottom(30).build());

    // Combobox setup
    this.profileComboBox = new ComboBox<>();
    this.profileComboBox.setMaxHeight(100);
    StyleManager.growHorizontal(this.profileComboBox, Priority.ALWAYS);
    this.profileComboBox.setButtonCell(new ProfileRowInCombobox());
    this.profileComboBox.setCellFactory(listView -> new ProfileRowInCombobox());
    this.profileComboBox.setAccessibleText("Select your profile");
    this.profileComboBox.setAccessibleRole(AccessibleRole.COMBO_BOX);

    this.updateProfileList();

    this.setupComboBoxListeners();
    VBox.setMargin(this.profileComboBox, InsetBuilder.create().bottom(20).build());

    // Buttons
    this.signInButton = new Button("Let's Start!", Button.ButtonType.DEFAULT);
    StyleManager.growHorizontal(this.signInButton);
    this.signInButton.setOnAction(e -> this.handleSignIn());

    Button signUpButton = new Button("My profile is not listed", Button.ButtonType.FLAT);
    signUpButton.setOnAction(e -> handleSignUp());
    VBox.setMargin(signUpButton, InsetBuilder.create().top(10).build());
    new ConfigValueBinder(this.configValueRepository).bindBoolean("settings.allow_registration",
        true,
        signUpButton.visibleProperty());

    centerContent.getChildren()
        .addAll(header, subtitle, this.profileComboBox, this.signInButton, signUpButton);

    content.getChildren().add(centerContent);
    return content;
  }

  private void setupComboBoxListeners() {
    log.debug("Setting up combobox listeners");
    this.profileComboBox.onKeyPressedProperty().set(e -> {
      if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
        if (this.profileComboBox.isShowing()) {
          this.profileComboBox.hide();
        } else {
          this.profileComboBox.show();
        }
      }
    });

    this.profileComboBox.setOnAction(e -> this.signInButton.setDisable(
        this.profileComboBox.getSelectionModel().getSelectedIndex() <= 0));

    Platform.runLater(() -> this.signInButton.setDisable(
        this.profileComboBox.getSelectionModel().getSelectedIndex() <= 0));
  }

  private void handleSignIn() {
    Profile selectedProfile = this.profileComboBox.getSelectionModel().getSelectedItem();
    if (selectedProfile != null && selectedProfile.getId() > -1L) {
      // Store selected profile in context and navigate to main
      this.getContext().setParameter("selectedProfileId",
          this.profileComboBox.getSelectionModel().getSelectedItem().getId());
      Auth.getInstance().authenticate(selectedProfile);
      NavigationService.navigate("main", getContext().getParameters());
    }
  }

  private void handleSignUp() {
    NavigationService.navigate("addProfile", getContext().getParameters());
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

