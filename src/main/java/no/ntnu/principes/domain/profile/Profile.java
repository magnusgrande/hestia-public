package no.ntnu.principes.domain.profile;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a user profile with an identifier, name, avatar, and creation timestamp.
 * The class uses a unique {@code id} to determine equality and hash code.
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class Profile {
  private Long id;
  private String name;
  private String avatarHash;
  private LocalDateTime createdAt;
}
