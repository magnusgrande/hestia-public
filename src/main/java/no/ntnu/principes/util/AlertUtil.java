package no.ntnu.principes.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertUtil {
  private static boolean enabled = true;

  /**
   * Displays a success alert with the specified title and message.
   *
   * @param title   The title of the alert dialog
   * @param message The content message to display
   */
  public static void success(String title, String message) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    show(alert);
  }

  private static void show(Alert alert) {
    if (enabled) {
      Platform.runLater(alert::showAndWait);
    }
  }

  /**
   * Displays a success alert with a default title and the specified message.
   *
   * @param message The content message to display
   */
  public static void success(String message) {
    success("Success", message);
  }

  /**
   * Displays an error alert with the specified title and message.
   *
   * @param title   The title of the alert dialog
   * @param message The content message to display
   */
  public static void error(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    show(alert);
  }

  /**
   * Displays an error alert with a default title and the specified message.
   *
   * @param message The content message to display
   */
  public static void error(String message) {
    error("Error", message);
  }


  /**
   * Enables or disables the display of alert dialogs in the application.
   * When disabled, all alert dialogs will be suppressed and not shown to the user.
   *
   * @param enabled A boolean value indicating whether alerts should be enabled (true) or disabled (false)
   */
  public static void setEnabled(boolean enabled) {
    AlertUtil.enabled = enabled;
  }

  /**
   * Checks if alerts are currently enabled in the application.
   *
   * @return {@code true} if alerts are enabled and will be displayed; {@code false} otherwise
   */
  public static boolean isEnabled() {
    return enabled;
  }
}