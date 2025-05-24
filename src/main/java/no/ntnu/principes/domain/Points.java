package no.ntnu.principes.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a points record that associates a value with a member and task assignment.
 * The class includes fields for a unique identifier, the points value, creation timestamp,
 * member ID, and task assignment ID.
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class Points {
  private Long id;
  private int value;
  private LocalDateTime createdAt;
  private Long memberId;
  private Long taskAssignmentId;
}
