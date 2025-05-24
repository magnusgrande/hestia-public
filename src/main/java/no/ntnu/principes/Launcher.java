package no.ntnu.principes;

import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.config.SQLiteDatabaseConfig;
import no.ntnu.principes.controller.StageController;
import no.ntnu.principes.controller.StageManager;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;

@Slf4j
public class Launcher extends Application {
  public static final String APP_NAME = "Hestia";
  private StageController stageController;

  @Override
  public void start(Stage primaryStage) {
    this.initializePersistance();
    this.stageController = StageManager.getInstance().getMainController(primaryStage);
    this.playStartup();
  }


  private void initializePersistance() {
    DatabaseManager.getInstance().useConfig(new SQLiteDatabaseConfig());
    log.info("Database connection established");
  }

  private void playStartup() {
    this.stageController.getStage().show();
    // Set initial navigation parameters
    ConfigValueRepository configValueRepository = DatabaseManager.getInstance()
        .getRepository(ConfigValueRepository.class);
    boolean onboardingComplete =
        configValueRepository.getValueOrDefault("onboardingComplete", false).get()
            .getBooleanValue();
    this.stageController.getScreenController().getContext("splashScreen")
        .setParameter("nextScreen", onboardingComplete ? "selectProfile" : "onboarding");

    NavigationService.navigate("splashScreen");

    Media sound = new Media(getClass().getResource("audio/startup.mp3").toExternalForm());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);
    mediaPlayer.play();
  }

  public static void main(String[] args) {
    launch();
  }
}