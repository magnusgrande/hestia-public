package no.ntnu.principes.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * A Data Transfer Object (DTO) representing profile information for a user or member.
 * Used for associating task-related data with its creator or assigned member.
 */
@Data
@Builder
public class ProfileDto {
  private Long id;
  private String name;
  private String avatarHash;
  private LocalDateTime createdAt;
}
