package no.ntnu.principes.view.dev;

import atlantafx.base.controls.CustomTextField;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.StageController;
import no.ntnu.principes.controller.StageManager;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.event.HouseholdNameChangedEvent;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.NavigateEvent;
import no.ntnu.principes.event.navigation.OpenModalEvent;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.DevTaskGenerator;
import no.ntnu.principes.util.ModalResult;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseModal;
import no.ntnu.principes.view.BaseScreen;

@Slf4j
public class DebugOverlayView extends BaseModal {
  private final ConfigValueRepository configValueRepository;
  private final CustomTextField textField = new CustomTextField();
  private final ScreenController mainController;
  private final boolean isMainStage;

  public DebugOverlayView(ScreenController controller,
                          String screenId) {
    super(controller, screenId);
    this.configValueRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
    this.isMainStage = this.controller.getStageController().isMainStage();
    this.mainController =
        StageManager.getInstance().getController("main").getScreenController();
  }

  @Override
  protected void initializeScreen() {

    this.getChildren().clear();
    Text title = new Text("Debugger", Text.TextType.PAGE_TITLE);

    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    StyleManager.growVertical(scrollPane);

    VBox content = new VBox(10);
    content.getChildren()
        .addAll(title, this.createNavigationPath(), this.createInnerContent(),
            this.createUpdateHouseholdName(),
            this.createSampleTaskGeneration(),
            this.moveSideBarByXY(),
            this.createStageTypeSelector());
    scrollPane.setContent(content);
    this.getModalContent().getChildren().add(scrollPane);
    this.getChildren().add(this.getModalContent());
  }

  private Node createSampleTaskGeneration() {
    VBox box = new VBox(10);
    box.setPadding(InsetBuilder.uniform(10).build());
    Text text = new Text("Sample Task Generation", Text.TextType.SUBHEADER);
    Button button = new Button("Generate Sample Tasks", Button.ButtonType.OUTLINED);
    button.setOnAction(e -> {
      DevTaskGenerator.generateTasks();
    });
    box.getChildren().addAll(text, button);
    return box;
  }

  private VBox createNavigationPath() {
    VBox box = new VBox(10);
    box.setPadding(InsetBuilder.uniform(10).build());
    Text text = new Text("Navigation Path", Text.TextType.SUBHEADER);
    Text currentPath = new Text("Unknown",
        Text.TextType.BODY);
    ObservableList<String> navigationPath = this.mainController.getNavigationStack().getList();
    currentPath.textProperty().bind(Bindings.createStringBinding(
        () -> navigationPath.isEmpty() ? "Unknown" : String.join(" > ", navigationPath),
        navigationPath));

    Button backButton = new Button("Back", Button.ButtonType.OUTLINED);
    backButton.setOnAction(e -> {
      NavigationService.back();
      if (this.isMainStage) {
        this.close();
      }
    });

    box.getChildren().addAll(backButton, text, currentPath);
    return box;
  }

  private VBox moveSideBarByXY() {
    VBox box = new VBox(10);
    box.setPadding(InsetBuilder.uniform(10).build());
    Text text = new Text("Move Sidebar by X and Y", Text.TextType.SUBHEADER);
    CustomTextField xField = new CustomTextField();
    xField.setPromptText("X");
    CustomTextField yField = new CustomTextField();
    yField.setPromptText("Y");
    Button button = new Button("Move", Button.ButtonType.OUTLINED);
    button.setOnAction(e -> {
      if (!StageManager.getInstance().hasController("sidebar")) {
        return;
      }
      double x = 0;
      double y = 0;
      if (!xField.getText().isEmpty()) {
        try {
          x = Double.parseDouble(xField.getText());
        } catch (NumberFormatException ex) {
          xField.clear();
        }
      }
      if (!yField.getText().isEmpty()) {
        try {
          y = Double.parseDouble(yField.getText());
        } catch (NumberFormatException ex) {
          yField.clear();
        }
      }
      StageController stageController = StageManager.getInstance().getController("sidebar");
      Stage stage = stageController.getStage();

      Timeline timeline = new Timeline();

      // Store initial position
      double startX = stage.getX();
      double startY = stage.getY();
      double endX = startX + x;
      double endY = startY + y;

      // Create animation frames
      timeline.getKeyFrames().addAll(
          new KeyFrame(Duration.ZERO, // Starting state
              e1 -> {
                stage.setX(startX);
                stage.setY(startY);
              }),
          new KeyFrame(Duration.millis(400), // End state
              e1 -> {
                stage.setX(endX);
                stage.setY(endY);
              })
      );

      for (int i = 1; i < 60; i++) { // 60 frames
        double fraction = i / 60.0;
        double interpolatedX = startX + (x * fraction);
        double interpolatedY = startY + (y * fraction);

        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(400 * fraction),
                e1 -> {
                  stage.setX(interpolatedX);
                  stage.setY(interpolatedY);
                })
        );
      }

      timeline.play();
    });
    box.getChildren().addAll(text, xField, yField, button);
    return box;
  }

  private VBox createStageTypeSelector() {
    VBox box = new VBox(10);
    box.setPadding(InsetBuilder.uniform(10).build());
    Text text = new Text("Select Stage Type", Text.TextType.SUBHEADER);

    // Create a button for each StageStyle
    Button decoratedButton = createStageTypeButton("Decorated", StageStyle.DECORATED);
    Button transparentButton = createStageTypeButton("Transparent", StageStyle.TRANSPARENT);
    Button undefocatedButton = createStageTypeButton("Undecorated", StageStyle.UNDECORATED);
    Button utilityButton = createStageTypeButton("Utility", StageStyle.UTILITY);
    Button uniconifiedButton = createStageTypeButton("Unified", StageStyle.UNIFIED);

    box.getChildren().addAll(
        text,
        decoratedButton,
        transparentButton,
        undefocatedButton,
        utilityButton,
        uniconifiedButton
    );
    return box;
  }

  private Button createStageTypeButton(String label, StageStyle style) {
    Button button = new Button(label, Button.ButtonType.OUTLINED);
    button.setMaxWidth(Double.MAX_VALUE); // Make buttons fill width

    button.setOnAction(e -> {
      Stage mainStage = this.mainController.getStageController().getStage();

      // Don't recreate if it's already the selected style
      if (mainStage.getStyle() == style) {
        return;
      }

      // Create new stage with selected style
      Stage newStage = new Stage();
      newStage.initStyle(style);
      newStage.setScene(mainStage.getScene());

      // Copy stage properties
      this.mainController.getStageController().setStage(newStage);
      this.copyStageProperties(mainStage, newStage);

      // Clear and close old stage
      this.clearStageEvents(mainStage);
      mainStage.close();

      // Show new stage
      newStage.show();
    });

    return button;
  }

  private void copyStageProperties(Stage source, Stage target) {
    target.setOnCloseRequest(source.getOnCloseRequest());
    target.setOnHiding(source.getOnHiding());
    target.setOnHidden(source.getOnHidden());
    target.setOnShowing(source.getOnShowing());
    target.setOnShown(source.getOnShown());
    target.setX(source.getX());
    target.setY(source.getY());
  }

  private void clearStageEvents(Stage stage) {
    stage.setOnCloseRequest(null);
    stage.setOnHiding(null);
    stage.setOnHidden(null);
    stage.setOnShowing(null);
    stage.setOnShown(null);
  }

  @Override
  protected void onNavigatedTo() {
    this.textField.setText(
        this.configValueRepository.getValueOrDefault("householdName", "Not Set").get()
            .getValue());
    this.controller.getStage().setTitle("Debugger");
    super.onNavigatedTo();
  }

  private VBox createUpdateHouseholdName() {
    VBox box = new VBox(10);
    box.setPadding(InsetBuilder.uniform(10).build());
    Text text = new Text("Update Household Name", Text.TextType.SUBHEADER);
    Button button = new Button("Update", Button.ButtonType.OUTLINED);
    button.setOnAction(e -> {
      this.configValueRepository.setConfigValue("householdName", this.textField.getText());
      PrincipesEventBus.getInstance().publish(
          HouseholdNameChangedEvent.to(this.textField.getText())
      );
    });
    PrincipesEventBus.getInstance().subscribe(HouseholdNameChangedEvent.class, (event) -> {
      this.textField.setText(event.getData());
    });
    box.getChildren().addAll(text, textField, button);
    return box;
  }

  private HBox createInnerContent() {
    HBox box = new HBox(20);
    Map<String, Parent> parents = this.mainController.getScreens();
    /*
    if (modal.getScreenId().equals(this.getScreenId()) && this.isMainStage) {
        continue;
      }
     */
    List<BaseModal> modals = parents.values().stream()
        .filter(screen -> screen instanceof BaseModal &&
            !(((BaseModal) screen).getScreenId().equals(this.getScreenId()) &&
                this.isMainStage))
        .map(screen -> (BaseModal) screen)
        .toList();
    List<BaseScreen> screens = parents.values().stream()
        .filter(screen -> screen instanceof BaseScreen && !(screen instanceof BaseModal))
        .map(screen -> (BaseScreen) screen)
        .toList();

    VBox modalContent = new VBox(10);
    modalContent.setPadding(InsetBuilder.uniform(10).build());
    modalContent.getChildren().add(
        new Text("Modals", Text.TextType.SUBHEADER)
    );
    for (BaseModal modal : modals) {
      Button button = new Button(modal.getScreenId(), Button.ButtonType.OUTLINED);
      StyleManager.growHorizontal(button);
      button.setOnAction(e -> {
        if (Objects.equals(this.mainController.getCurrentModalId(), modal.getScreenId())) {
          modal.close(ModalResult.ModalResultStatus.CANCEL);
        } else {
          PrincipesEventBus.getInstance().publish(
              OpenModalEvent.of(modal.getScreenId(), "debugCallback")
          );
        }
      });
      modalContent.getChildren().add(button);
    }
    ScrollPane modalScrollPane = new ScrollPane();
    modalScrollPane.setContent(modalContent);
    modalScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    VBox screenContent = new VBox(10);
    screenContent.setPadding(InsetBuilder.uniform(10).build());
    screenContent.getChildren().add(
        new Text("Screens", Text.TextType.SUBHEADER)
    );
    for (BaseScreen screen : screens) {
      Button button = new Button(screen.getScreenId(), Button.ButtonType.OUTLINED);
      StyleManager.growHorizontal(button);
      button.setOnAction(e -> {
        PrincipesEventBus.getInstance().publish(
            NavigateEvent.push(screen.getScreenId())
        );
        if (this.isMainStage) {
          this.close();
        }
      });
      screenContent.getChildren().add(button);
    }
    ScrollPane screenScrollPane = new ScrollPane();
    screenScrollPane.setContent(screenContent);
    screenScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    if (!modals.isEmpty()) {
      box.getChildren().add(modalScrollPane);
    }
    if (!screens.isEmpty()) {
      box.getChildren().add(screenScrollPane);
    }
    return box;
  }
}
