package no.ntnu.principes.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.domain.task.Task;
import no.ntnu.principes.domain.task.TaskAssignment;
import no.ntnu.principes.dto.ProfileDto;
import no.ntnu.principes.dto.TaskAssignmentDto;
import no.ntnu.principes.dto.TaskDto;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.repository.TaskRepository;
import no.ntnu.principes.service.DatabaseManager;

/**
 * Maps TaskAssignment domain objects to TaskAssignmentDto objects.
 * Provides optimized mapping for both single assignments and collections.
 * Uses batch loading for related entities when mapping multiple assignments.
 */
@Slf4j
public class TaskAssigmentMapper {
  private static final MemberRepository memberRepository =
      DatabaseManager.getInstance().getRepository(MemberRepository.class);
  private static final TaskRepository taskRepository =
      DatabaseManager.getInstance().getRepository(TaskRepository.class);

  /**
   * Maps a single TaskAssignment to its DTO representation.
   * Fetches related task and member information to complete the mapping.
   *
   * @param assignment The task assignment to be mapped
   * @return The mapped DTO, or null if the related task cannot be found
   */
  public static TaskAssignmentDto mapAssignment(TaskAssignment assignment) {
    Task task = taskRepository.findById(assignment.getTaskId()).orElse(null);
    if (task == null) {
      return null;
    }
    ProfileDto assignedTo = memberRepository.findById(assignment.getMemberId())
        .map(p -> ProfileDto.builder()
            .id(p.getId())
            .name(p.getName())
            .avatarHash(p.getAvatarHash())
            .createdAt(p.getCreatedAt())
            .build()
        ).orElse(null);
    return TaskAssignmentDto.builder()
        .id(assignment.getId())
        .task(TaskMapper.mapTask(task))
        .member(assignedTo)
        .assignedAt(assignment.getAssignedAt())
        .dueAt(assignment.getDueAt())
        .completedAt(assignment.getCompletedAt())
        .status(assignment.getStatus())
        .build();
  }

  /**
   * Maps a list of TaskAssignments to their DTO representations with optimized data loading.
   * Prefetches all related members and tasks in two batch operations to avoid N+1 query issues.
   *
   * @param assignments The list of task assignments to be mapped
   * @return A list of mapped DTOs with complete task and member information
   */
  public static List<TaskAssignmentDto> mapAssignments(List<TaskAssignment> assignments) {
//    log.debug("Mapping {} task assignments", assignments.size());
    Map<Long, ProfileDto> profileMap =
        memberRepository.findAllById(assignments.stream()
                .map(TaskAssignment::getMemberId)
                .distinct()
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
//    log.debug("Prefetched {} members", profileMap.size());


    Map<Long, TaskDto> taskMap = TaskMapper.mapTasks(
            taskRepository.findByIds(
                assignments.stream().map(TaskAssignment::getTaskId).distinct().toList()
            )
        )
        .stream().collect(Collectors.toMap(TaskDto::getId, t -> t));
    log.debug("Prefetched {} tasks", taskMap.size());

    return assignments.stream()
        .map(assignment -> TaskAssignmentDto.builder()
            .id(assignment.getId())
            .task(taskMap.get(assignment.getTaskId()))
            .member(profileMap.get(assignment.getMemberId()))
            .assignedAt(assignment.getAssignedAt())
            .dueAt(assignment.getDueAt())
            .completedAt(assignment.getCompletedAt())
            .status(assignment.getStatus())
            .build()
        )
        .toList();
  }
}