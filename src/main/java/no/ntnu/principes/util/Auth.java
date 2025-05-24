package no.ntnu.principes.util;

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;

/**
 * Manages authentication state throughout the application using the singleton pattern.
 * Stores the currently authenticated profile and provides access to authentication status.
 */
public class Auth {
  private static volatile Auth instance;

  private final MemberRepository memberRepository;
  private final BooleanProperty isAuthenticated = new SimpleBooleanProperty(false);
  @Getter
  @Setter
  private Long profileId;
  private final ObjectProperty<Profile> profile = new SimpleObjectProperty<>(null);

  /**
   * Creates a new Auth instance with the member repository from the database manager.
   */
  public Auth() {
    this.memberRepository = DatabaseManager.getInstance().getRepository(MemberRepository.class);
  }

  /**
   * Gets the singleton instance of Auth, creating it if necessary.
   *
   * @return The single Auth instance for the application
   */
  public static Auth getInstance() {
    if (instance == null) {
      synchronized (Auth.class) {
        if (instance == null) {
          instance = new Auth();
        }
      }
    }
    return instance;
  }

  /**
   * Authenticates a user with the specified profile.
   *
   * @param profile The profile to authenticate with
   */
  public void authenticate(Profile profile) {
    this.profile.setValue(profile);
    this.profileId = profile.getId();
    this.isAuthenticated.set(true);
  }

  /**
   * Authenticates a user with the specified profile ID.
   *
   * @param profileId The ID of the profile to authenticate with
   * @throws IllegalArgumentException If no profile exists with the given ID
   */
  public void authenticate(Long profileId) {
    Optional<Profile> profile = this.memberRepository.findById(profileId);
    if (profile.isEmpty()) {
      throw new IllegalArgumentException("Profile not found");
    }
    this.authenticate(profile.get());
  }

  /**
   * Logs out the current user by clearing profile data and authentication state.
   */
  public void deauthenticate() {
    this.profile.setValue(null);
    this.profileId = null;
    this.isAuthenticated.set(false);
  }

  /**
   * Gets the observable authentication state property.
   *
   * @return Property indicating whether a user is authenticated
   */
  public BooleanProperty isAuthenticatedProperty() {
    return this.isAuthenticated;
  }

  /**
   * Checks if a user is currently authenticated.
   *
   * @return True if a user is authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    return this.isAuthenticated.get();
  }

  /**
   * Gets the observable profile property.
   *
   * @return Property containing the currently authenticated profile
   */
  public ObjectProperty<Profile> profileProperty() {
    return this.profile;
  }

  /**
   * Gets the currently authenticated profile.
   *
   * @return The authenticated profile or null if no user is authenticated
   */
  public Profile getProfile() {
    return this.profile.get();
  }
}