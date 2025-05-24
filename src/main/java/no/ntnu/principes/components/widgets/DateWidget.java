package no.ntnu.principes.components.widgets;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.scene.paint.Paint;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.view.BaseScreen;

/**
 * A widget that displays the current date (day and month).
 *
 * <p>Text elements are styled according to predefined properties inherited from
 * {@link BaseWidget} and {@link Text}. The date and month sections are visually distinct.</p>
 */
public class DateWidget extends BaseWidget {
  // Cmponent state
  private final Text dateText;
  private final Text monthText;

  /**
   * Creates a DateWidget that displays the current day and month in a styled format.
   *
   * <p>The day is displayed as a two-digit number, and the month is shown as a full name.</p>
   *
   * @param screen the parent {@link BaseScreen} that this widget will be associated with.
   *               It utilizes the screen context and lifecycle for initialization and cleanup.
   */
  public DateWidget(BaseScreen screen) {
    super("DateWidget", "Todays date", screen);
    this.dateText = new Text("00", StyledText.TextType.PAGE_TITLE);
    this.monthText = new Text("January", StyledText.TextType.SUBHEADER);
  }

  @Override
  protected void initializeComponent() {
    dateText.setTextFill(Paint.valueOf("#000000"));
    dateText.setStyle("-fx-font-size: " + FONT_SIZE + "px;");
    monthText.setStyle(
        "-fx-text-fill: -color-warning-4;-fx-font-size: " + (FONT_SIZE * SUB_FONT_SIZE_SCALE)
            + "px;");
    this.contentContainer.getChildren().addAll(dateText, monthText);
    this.updateDate();
  }

  /**
   * Updates the displayed date (day and month) in the widget.
   *
   * <p>This method retrieves the current date and:
   * <ul>
   *     <li>Sets the day as a two-digit integer (e.g., "01" for the first of the month).</li>
   *     <li>Sets the month as a full name in English (e.g., "January").</li>
   * </ul>
   * </p>
   */
  private void updateDate() {
    LocalDateTime now = LocalDateTime.now();
    dateText.setText(String.format("%02d", now.getDayOfMonth()));
    monthText.setText(now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
  }

  @Override
  protected void onMount() {
    this.updateDate();

  }

  @Override
  protected void onUnmount() {
    //nnoopd
  }

  @Override
  protected void onDestroy() {
    this.getChildren().clear();
  }
}