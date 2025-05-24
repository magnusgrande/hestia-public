package no.ntnu.principes.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TimeWeight;
import no.ntnu.principes.domain.task.WorkWeight;
import no.ntnu.principes.dto.CreateTaskRequest;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.task.TasksCreatedEvent;
import no.ntnu.principes.event.task.TasksDistributedEvent;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.TaskAssignmentService;
import no.ntnu.principes.service.TaskTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevTaskGenerator {
  private static final Logger log = LoggerFactory.getLogger(DevTaskGenerator.class);

  private static final List<String> taskNames = Arrays.asList(
      "Vacuum the living room",
      "Clean the kitchen",
      "Take out the trash",
      "Do the dishes",
      "Mow the lawn",
      "Clean the bathroom",
      "Do the laundry",
      "Buy groceries",
      "Pay bills",
      "Dust the furniture",
      "Clean windows",
      "Empty dishwasher",
      "Water plants",
      "Clean refrigerator",
      "Organize garage",
      "Feed pets",
      "Change bed sheets",
      "Sweep the porch",
      "Shovel snow",
      "Clean gutters",
      "Trim hedges",
      "Wash the car",
      "Clean the oven",
      "Organize closet",
      "Mop the floors"
  );

  private static final List<String> taskDescriptions = Arrays.asList(
      "Be thorough and don't forget the corners",
      "Make sure all counters and appliances are wiped down",
      "Remember to replace the trash bag",
      "Use hot water and plenty of soap",
      "Set the height to medium",
      "Don't forget to clean under the sink",
      "Separate whites and colors",
      "Check the shopping list on the fridge",
      "Due on the 15th of this month",
      "Use the microfiber cloth",
      "Use the glass cleaner in the cabinet",
      "Put everything in its proper place",
      "Each plant needs different amounts of water",
      "Throw away expired items",
      "Find proper storage for all tools",
      "Use the special food in the pantry",
      "Fresh linens are in the hall closet",
      "Use the broom in the utility closet",
      "The shovel is in the garage",
      "Use the ladder with caution",
      "The hedge trimmer is in the shed",
      "Don't forget to wax",
      "Use the special oven cleaner",
      "Donate items that haven't been worn in a year",
      "Use the special floor cleaner under the sink"
  );

  public static List<Task> generateTasks() {
    MemberRepository memberRepository =
        DatabaseManager.getInstance().getRepository(MemberRepository.class);
    TaskTemplateService taskTemplateService = new TaskTemplateService();
    TaskAssignmentService taskAssignmentService = new TaskAssignmentService();
    Random random = new Random();
    log.info("Generating 25 sample tasks");
    List<Task> generatedTasks = new ArrayList<>();

    AlertUtil.setEnabled(false);

    List<Profile> members = memberRepository.findAll();
    if (members.isEmpty()) {
      log.warn("No members found. Cannot generate sample tasks.");
      return generatedTasks;
    }

    LocalDate today = LocalDate.now();

    for (int i = 0; i < 25; i++) {
      List<Long> assignedMemberIds = new ArrayList<>();
      int memberCount = random.nextInt(members.size()) + 1;
      for (int j = 0; j < memberCount && j < members.size(); j++) {
        Profile member = members.get(random.nextInt(members.size()));
        if (!assignedMemberIds.contains(member.getId())) {
          assignedMemberIds.add(member.getId());
        }
      }
      if (assignedMemberIds.isEmpty()) {
        assignedMemberIds.add(members.get(random.nextInt(members.size())).getId());
      }

      int dayOffset = -1 + (i * 4 / 25);
      LocalDate dueDate = today.plusDays(dayOffset);

      int hour = random.nextInt(12) + 8;
      LocalDateTime dueDateTime = LocalDateTime.of(dueDate, LocalTime.of(hour, 0));

      boolean isRecurring = random.nextBoolean();
      int recurrenceInterval = isRecurring ? (random.nextInt(14) + 1) : 0;

      CreateTaskRequest taskRequest = CreateTaskRequest.builder()
          .name(taskNames.get(i % taskNames.size()))
          .description(taskDescriptions.get(i % taskDescriptions.size()))
          .workWeight(WorkWeight.values()[random.nextInt(WorkWeight.values().length)])
          .timeWeight(TimeWeight.values()[random.nextInt(TimeWeight.values().length)])
          .createdById(members.getFirst().getId())
          .dueAt(dueDateTime)
          .isRecurring(isRecurring)
          .recurrenceIntervalDays(recurrenceInterval)
          .build();
      Task newTask = taskTemplateService.createTask(taskRequest, List.of(), false);
      generatedTasks.add(newTask);

      taskAssignmentService.autoAssignTask(newTask.getId(), false);

      // Mark completion on some
//      for (TaskAssignment assignment : this.) {
//        if (random.nextBoolean()) {
//          assignment.setCompleted(true);
//        }
//      }

      log.debug("Created task '{}' due on {} assigned to {} members",
          newTask.getName(), dueDateTime, assignedMemberIds.size());
    }
    AlertUtil.setEnabled(true);
    // Publish only one event to trigger updates.
    PrincipesEventBus.getInstance().publish(
        TasksCreatedEvent.of(generatedTasks)
    );
    PrincipesEventBus.getInstance().publish(
        TasksDistributedEvent.of(generatedTasks)
    );

    log.info("Successfully generated {} sample tasks", generatedTasks.size());
    return generatedTasks;
  }
}
