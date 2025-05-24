package no.ntnu.principes.view.main.household;

import java.util.Objects;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.secondary.TabPane;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.event.HouseholdNameChangedEvent;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.PrincipesEventListener;
import no.ntnu.principes.event.navigation.NavigateEvent;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.main.DashboardScreen;

@Slf4j
public class HouseholdView extends DashboardScreen {
  private final ConfigValueRepository configValueRepository;
  private TextField householdNameField;
  private Region spacer = new Region();
  private final TabPane tabPane = new TabPane();
  private final PrincipesEventListener<HouseholdNameChangedEvent> householdNameChangedListener =
      event -> {
        if (this.householdNameField != null) {
          int oldPos = this.householdNameField.getCaretPosition();
          this.householdNameField.setText(event.getData());
          this.householdNameField.positionCaret(oldPos);
        }
      };

  public HouseholdView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.configValueRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
  }

  @Override
  protected void initializeScreen() {
    super.initializeScreen();
    VBox innerContent = new VBox();
    StyleManager.growHorizontal(innerContent);
    innerContent.setMinWidth(800);
    innerContent.setPadding(InsetBuilder.create().right(20).bottom(40).build());
    innerContent.getChildren().add(this.createTabBar());

    ScrollPane outerContent = new ScrollPane(innerContent);
    outerContent.setFitToWidth(true);
    outerContent.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    StyleManager.growVertical(outerContent);
    this.content.getChildren().add(outerContent);
//				this.getChildren().add(new DashboardStatsComponent(this));
    this.setStyle("-fx-background-color: -color-bg-default;");

  }

  private VBox createGeneralForm() {
    VBox form = new VBox();
    form.setSpacing(5);
    Label label = new Label("Household name");
    this.householdNameField = new TextField();
    this.householdNameField.setPromptText("Enter household name");
    this.householdNameField.setText(
        this.configValueRepository.getValueOrDefault("householdName", "Not set").get()
            .getValue());
    this.householdNameField.setOnKeyTyped((event) -> {
      this.configValueRepository.setConfigValue("householdName",
          this.householdNameField.getText());
      PrincipesEventBus.getInstance().publish(
          HouseholdNameChangedEvent.to(this.householdNameField.getText())
      );
    });

    // Fuses the  Members content to the General tab
    Label membersLabel = new Label("Household members");

    form.getChildren()
        .addAll(label, this.householdNameField, spacer, membersLabel, createMembersForm());
    return form;
  }

  private TabPane createTabBar() {
    StyleManager.growHorizontal(tabPane);
    tabPane.getTabs().setAll(
        this.createTab("General", this.createGeneralForm()),
        // this.createTab("Household members", this.createMembersForm()),
        this.createTab("Settings", this.createSettingsForm())
    );
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-background-color: -color-bg-default;");
    return tabPane;
  }

  private VBox createSettingsForm() {
    VBox content = new VBox();
    SettingsTabContent tabContent = new SettingsTabContent(this);
    StyleManager.growHorizontal(content, tabContent);
    content.getChildren().add(tabContent);
    return content;
  }

  private VBox createMembersForm() {
    VBox content = new VBox();
    HouseholdMembersTabContent tabContent = new HouseholdMembersTabContent(this);
    StyleManager.growHorizontal(content, tabContent);
    content.getChildren().add(tabContent);
    return content;
  }

  private Tab createTab(String tabName, Node content) {
    Tab tab = new Tab(tabName, content);
    tab.setText(null); // Clear the default text to avoid duplication
    Label tabLabel = new Label(tabName);
    tabLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    tab.setGraphic(tabLabel);
    content.setStyle("-fx-padding: 20px 0 0 0;");
    tab.setContent(content);
    return tab;
  }

  private void tearDownListeners() {
    PrincipesEventBus.getInstance()
        .unsubscribe(HouseholdNameChangedEvent.class, this.householdNameChangedListener);
    PrincipesEventBus.getInstance()
        .unsubscribe(NavigateEvent.class, this::onNavigateToSettingsListener);
  }

  private void setupListeners() {
    PrincipesEventBus.getInstance()
        .subscribe(HouseholdNameChangedEvent.class, this.householdNameChangedListener);
    PrincipesEventBus.getInstance()
        .subscribe(NavigateEvent.class, this::onNavigateToSettingsListener);
  }

  private void onNavigateToSettingsListener(NavigateEvent event) {
    if (Objects.equals(event.getData().route(), this.getScreenId())) {
      Optional<Integer> tabIndex = event.getData().getParam("tabIndex", Integer.class);
      this.tabPane.getSelectionModel().select(tabIndex.orElse(0));// Default to first tab
    }
  }

  @Override
  protected void onNavigatedFrom() {
    super.onNavigatedFrom();
    this.tearDownListeners();
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();
    Integer tabIndex = this.getContext().getParameter("tabIndex");
    if (tabIndex != null) {
      this.tabPane.getSelectionModel().select(Math.max(0, tabIndex));
    } else {
      this.tabPane.getSelectionModel().select(0);
    }
    if (this.householdNameField != null) {
      this.householdNameField.setText(
          this.configValueRepository.getValueOrDefault("householdName", "Not set").get()
              .getValue());
    }
    this.setupListeners();
  }
}
