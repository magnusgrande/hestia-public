package no.ntnu.principes.domain.onboarding;

import java.util.function.Supplier;
import javafx.scene.Node;

/**
 * Represents a single step in the onboarding process with UI elements and validation.
 * Each step contains content to display and optional validation logic to determine
 * if the user can proceed to the next step.
 */
public record OnboardingStep(String title, String subtitle, Node content,
                             Supplier<Boolean> validate) {
  /**
   * Creates an onboarding step with automatic validation pass.
   *
   * @param title    The main heading displayed for this step
   * @param subtitle The secondary text providing additional context
   * @param content  The JavaFX Node containing the UI elements for this step
   */
  public OnboardingStep(String title, String subtitle, Node content) {
    this(title, subtitle, content, () -> true);
  }
}