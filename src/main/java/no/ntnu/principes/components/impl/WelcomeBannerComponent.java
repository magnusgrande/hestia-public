package no.ntnu.principes.components.impl;

import static no.ntnu.principes.Launcher.APP_NAME;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TaskCreatedEvent;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

public class WelcomeBannerComponent extends BaseComponent {
  public WelcomeBannerComponent(BaseScreen parentScreen) {
    super("welcome-banner", parentScreen);
  }

  @Override
  protected void initializeComponent() {
    Message message = new Message();
    message.setTitle("Welcome to " + APP_NAME + "!");
    message.setDescription(APP_NAME +
        " is a household management application that helps you organize tasks and manage your home.\n" +
        "Get started by creating tasks and assigning them to your household members.");
    message.getStyleClass().add(Styles.SUCCESS);
    StyleManager.growHorizontal(this, message);
    StyleManager.shrinkVertical(this, message);

    TaskTemplateService taskTemplateService = new TaskTemplateService();
    if (taskTemplateService.hasCreatedTasks()) {
      this.getChildren().clear();
    } else {
      this.getChildren().setAll(message);
    }
  }

  @Override
  protected void setupEventHandlers() {
    PrincipesEventBus.getInstance().subscribe(TaskCreatedEvent.class, this::onTaskCreatedEvent);
  }

  private void onTaskCreatedEvent(TaskCreatedEvent event) {
    this.initializeComponent();
  }

  private void tearDownListeners() {
    PrincipesEventBus.getInstance().unsubscribe(TaskCreatedEvent.class, this::onTaskCreatedEvent);
  }

  @Override
  protected void onMount() {
    this.initializeComponent();
    this.setupEventHandlers();
  }

  @Override
  protected void onUnmount() {
    this.tearDownListeners();
  }

  @Override
  protected void onDestroy() {

  }
}
