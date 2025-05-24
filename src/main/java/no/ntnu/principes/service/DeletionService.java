package no.ntnu.principes.service;

import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.repository.PointsRepository;
import no.ntnu.principes.repository.TaskAssignmentRepository;

/**
 * Service for safely deleting entities while maintaining database integrity.
 * Handles proper deletion order for related records to prevent referential integrity issues.
 * Implemented as utility methods with no instance state.
 */
@Slf4j
public class DeletionService {
  /**
   * Deletes a member and all associated data in the correct order.
   * First removes points and task assignments linked to the member,
   * then deletes the member record itself.
   *
   * @param memberId The ID of the member to delete
   */
  public static void deleteMember(Long memberId) {
    log.info("Deleting member with ID: {}", memberId);
    MemberRepository memberRepository =
        DatabaseManager.getInstance().getRepository(MemberRepository.class);
    TaskAssignmentRepository taskAssignmentRepository =
        DatabaseManager.getInstance().getRepository(TaskAssignmentRepository.class);
    PointsRepository pointsRepository =
        DatabaseManager.getInstance().getRepository(PointsRepository.class);
    // Delete related data
    int deletedPoints = pointsRepository.deleteByMemberId(memberId);
    int deletedAssignments = taskAssignmentRepository.deleteByMemberId(memberId);
    // Delete the member
    memberRepository.deleteById(memberId);
    log.info("Deleted member with ID: {}. Deleted {} points and {} assignments.", memberId,
        deletedPoints, deletedAssignments);
  }

  /**
   * Deletes a task and all associated data in the correct order.
   * First removes points earned for the task's assignments,
   * then deletes the task assignments, and finally the task itself.
   *
   * @param taskId The ID of the task to delete
   */
  public static void deleteTask(Long taskId) {
    log.info("Deleting task with ID: {}", taskId);
    TaskAssignmentRepository taskAssignmentRepository =
        DatabaseManager.getInstance().getRepository(TaskAssignmentRepository.class);
    PointsRepository pointsRepository =
        DatabaseManager.getInstance().getRepository(PointsRepository.class);
    // Delete related assignments and points
    int deletedPoints = pointsRepository.deleteByTaskId(taskId);
    int deletedAssignments = taskAssignmentRepository.deleteByTaskId(taskId);
    // Delete the task
    taskAssignmentRepository.deleteByTaskId(taskId);
    log.info("Deleted task with ID: {}. Deleted {} point entries and {} assignments.", taskId,
        deletedPoints, deletedAssignments);
  }
}