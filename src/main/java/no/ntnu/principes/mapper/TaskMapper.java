package no.ntnu.principes.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.dto.ProfileDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;

/**
 * Maps Task domain objects to TaskDto objects for use in the presentation layer.
 * Provides methods for mapping individual tasks and collections of tasks.
 * Uses batch loading for related entities when mapping multiple tasks.
 */
public class TaskMapper {
  private static final MemberRepository memberRepository =
      DatabaseManager.getInstance().getRepository(MemberRepository.class);

  /**
   * Maps a single Task to its DTO representation.
   * Fetches the creator's profile information and initializes assignments as empty.
   *
   * @param task The task domain object to be mapped
   * @return The mapped TaskDto with creator information
   */
  public static TaskDto mapTask(Task task) {
    return TaskDto.builder()
        .id(task.getId())
        .name(task.getName())
        .description(task.getDescription())
        .workWeight(task.getWorkWeight())
        .timeWeight(task.getTimeWeight())
        .createdBy(
            memberRepository.findById(task.getCreatedById())
                .map(p -> ProfileDto.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .avatarHash(p.getAvatarHash())
                    .createdAt(p.getCreatedAt())
                    .build()
                ).orElse(null)
        )
        .createdAt(task.getCreatedAt())
        .assignments(Collections.emptyList())
        .isRecurring(task.isRecurring())
        .recurrenceIntervalDays(task.getRecurrenceIntervalDays())
        .build();
  }

  /**
   * Maps a list of Tasks to their DTO representations with optimized data loading.
   * Prefetches all creator profiles in a single batch operation to avoid multiple database queries.
   * Initializes assignment lists as empty for each task.
   *
   * @param tasks The list of task domain objects to be mapped
   * @return A list of mapped TaskDto objects with creator information
   */
  public static List<TaskDto> mapTasks(List<Task> tasks) {
    Map<Long, ProfileDto> profileMap =
        memberRepository.findAllById(tasks.stream()
                .map(Task::getCreatedById)
                .toList())
            .stream()
            .map(p -> ProfileDto.builder()
                .id(p.getId())
                .name(p.getName())
                .avatarHash(p.getAvatarHash())
                .createdAt(p.getCreatedAt())
                .build()
            )
            .collect(Collectors.toMap(ProfileDto::getId, p -> p));

    return tasks.stream()
        .map(task -> TaskDto.builder()
            .id(task.getId())
            .name(task.getName())
            .description(task.getDescription())
            .workWeight(task.getWorkWeight())
            .timeWeight(task.getTimeWeight())
            .createdBy(profileMap.get(task.getCreatedById()))
            .createdAt(task.getCreatedAt())
            .assignments(Collections.emptyList())
            .isRecurring(task.isRecurring())
            .recurrenceIntervalDays(task.getRecurrenceIntervalDays())
            .build()
        )
        .toList();
  }
}