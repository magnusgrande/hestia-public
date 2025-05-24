package no.ntnu.principes.view.onboarding;

import atlantafx.base.theme.Styles;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.onboarding.OnboardingDetails;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

public class OnboardingSuccessView extends BaseScreen {
  private static final double LOADING_DURATION = 500;
  private static final double CHECK_ANIMATION_DURATION = 500;
  private static final double WELCOME_FADE_DURATION = 500;
  private static final double FINAL_PAUSE = 1500;

  private final ProgressIndicator loader;
  private final FontIcon checkmark;
  private final Text welcomeText;
  private final StackPane container;
  private final DatabaseManager databaseManager;
  private final ConfigValueRepository configValueRepository;
  private final MemberRepository memberRepository;

  private static final Interpolator CHECK_DRAW = Interpolator.SPLINE(0.4, 0, 0.2, 1);
  private static final Interpolator TEXT_FADE = Interpolator.SPLINE(0.4, 0, 0.2, 1);

  public OnboardingSuccessView(ScreenController controller, String screenId) {
    super(controller, screenId);

    this.container = new StackPane();
    this.loader = new ProgressIndicator();
    this.checkmark = new FontIcon(Material2AL.CHECK);
    this.welcomeText = new Text("Welcome!", StyledText.TextType.PAGE_TITLE);
    this.container.setAlignment(Pos.CENTER);
    this.databaseManager = DatabaseManager.getInstance();
    this.configValueRepository =
        this.databaseManager.getRepository(ConfigValueRepository.class);
    this.memberRepository = this.databaseManager.getRepository(MemberRepository.class);
    StyleManager.grow(this.container);
  }

  @Override
  protected void initializeScreen() {
    // Set up loader
    this.loader.setMaxSize(50, 50);

    // Set up checkmark
    this.checkmark.setVisible(false);
    this.checkmark.setOpacity(0);
    this.checkmark.setIconSize(128);
    this.checkmark.setScaleX(10);
    this.checkmark.setScaleY(10);
    StyleManager.apply(this.checkmark, Styles.SUCCESS);

    // Set up welcome text
    this.welcomeText.setStyle("-fx-font-size: 56px;");
    this.welcomeText.setVisible(false);
    this.welcomeText.setTranslateY(-140);

    // Stack everything
    this.container.getChildren().addAll(this.loader, this.checkmark, this.welcomeText);
    this.getChildren().add(container);
  }

  private void saveOnboardingDetails() {
    OnboardingDetails onboardingDetails = this.getContext().getParameter("onboardingDetails");
    this.configValueRepository.setConfigValue("onboardingComplete", true);
    this.configValueRepository.setConfigValue("householdName",
        onboardingDetails.householdName());

    for (String memberName : onboardingDetails.members()) {
      Profile profile = Profile.builder().
          name(memberName)
          .avatarHash(AvatarManager.getHashFor(memberName))
          .build();
      this.memberRepository.save(profile);
    }
    this.startAnimation();
  }

  @Override
  protected void onNavigatedTo() {
    this.saveOnboardingDetails();
  }

  private void startAnimation() {
    Timeline timeline = new Timeline();

    // Phase 1: Loading
    timeline.getKeyFrames().add(new KeyFrame(
        Duration.millis(LOADING_DURATION),
        e -> {
          this.loader.setVisible(false);
          this.checkmark.setVisible(true);
          this.fillSettingsConfig();
        }
    ));

    // Phase 2: Draw checkmark
    timeline.getKeyFrames().addAll(
        new KeyFrame(
            Duration.millis(LOADING_DURATION),
            new KeyValue(this.checkmark.opacityProperty(), 0)
        ),
        new KeyFrame(
            Duration.millis(LOADING_DURATION + CHECK_ANIMATION_DURATION),
            new KeyValue(this.checkmark.opacityProperty(), 1)
        )
    );

    // Phase 3: Fade in welcome text
    double textStart = LOADING_DURATION + CHECK_ANIMATION_DURATION;
    timeline.getKeyFrames().addAll(
        new KeyFrame(
            Duration.millis(textStart),
            e -> this.welcomeText.setVisible(true),
            new KeyValue(this.welcomeText.opacityProperty(), 0, TEXT_FADE)
        ),
        new KeyFrame(
            Duration.millis(textStart + WELCOME_FADE_DURATION),
            new KeyValue(this.welcomeText.opacityProperty(), 1, TEXT_FADE)
        )
    );

    // Final navigation
    double totalDuration = textStart + WELCOME_FADE_DURATION + FINAL_PAUSE;
    timeline.getKeyFrames().add(new KeyFrame(
        Duration.millis(totalDuration),
        e -> NavigationService.navigate("selectProfile")
    ));

    timeline.play();
  }

  private void fillSettingsConfig() {
    this.configValueRepository.setConfigValue("settings.colorblind_mode", false);
    this.configValueRepository.setConfigValue("settings.larger_text", false);
    this.configValueRepository.setConfigValue("settings.screenreader", false);
    this.configValueRepository.setConfigValue("settings.allow_registration", true);
    this.configValueRepository.setConfigValue("settings.allow_reassign", false);
    this.configValueRepository.setConfigValue("settings.allow_delete", false);
    this.configValueRepository.setConfigValue("settings.allow_create", true);
    this.configValueRepository.setConfigValue("settings.darkmode", false);
  }
}