package no.ntnu.principes.view.main;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.QuickActions;
import no.ntnu.principes.components.secondary.NewTaskList;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.event.task.TaskCreatedEvent;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import no.ntnu.principes.util.ModalResult;

@Slf4j
public class TasksView extends DashboardScreen {

  private final TaskTemplateService taskTemplateService;
  private final TaskAssignmentService taskAssignmentService = new TaskAssignmentService();
  private final NewTaskList taskList;

  public TasksView(ScreenController controller, String screenId) {
    super(controller, screenId);
    this.taskTemplateService = new TaskTemplateService();
    this.taskList = new NewTaskList("taskList", this, this::getFilteredTasks);
  }

  private List<TaskAssignmentDto> getFilteredTasks() {
    return this.taskAssignmentService.getAllTasks();
  }

  @Override
  protected void initializeScreen() {
    super.initializeScreen();
    QuickActions quickActions = new QuickActions(this);
    this.content.getChildren().add(quickActions);
    PrincipesEventBus.getInstance()
        .subscribe(CloseModalEvent.class, this::onCreateTaskModalResult);
    this.content.getChildren().add(taskList);

    quickActions.setOnSelectChange(status -> {
      log.info("Selected status: {}", status);
      this.taskList.setTaskStatus(status);
      taskList.refresh();
      return status;
    });
    this.initializeListeners();
  }

  private void initializeListeners() {
    PrincipesEventBus.getInstance()
        .subscribe(TaskCreatedEvent.class, this::onCreateTaskEvent);
    PrincipesEventBus.getInstance()
        .subscribe(TasksDistributedEvent.class, this::onTasksDistributedEvent);
  }

  private void onTasksDistributedEvent(TasksDistributedEvent tasksDistributedEvent) {
    log.info("Tasks distributed: {}", tasksDistributedEvent.getData());
    this.taskList.refresh();
  }

  private void onCreateTaskEvent(TaskCreatedEvent event) {
    Task task = event.getData();
    log.info("Task created: {}", task);
    this.taskList.refresh();
  }

  private void onCreateTaskModalResult(CloseModalEvent event) {
    if (event.getData().getCallbackId().equals("qa-create-task")) {
      ModalResult result = event.getData();
      if (result.isSuccess()) {
        CreateTaskRequest newTask = (CreateTaskRequest) result.getResult();
        if (newTask == null) {
          log.warn("No task created");
          return;
        }
        log.info("New task created: {}", newTask);
        Task task = this.taskTemplateService.createTask(newTask, List.of());
      }
    }
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();
  }
}
