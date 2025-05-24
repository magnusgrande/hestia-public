package no.ntnu.principes.components.secondary;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * Customizes the appearance of cells in a {@link javafx.scene.control.ComboBox} containing
 * {@link Profile} objects. Displays the profile's name as text and their associated avatar as a
 * graphic.
 *
 * <p>Each cell fetches the avatar image via the {@link AvatarManager} using the profile's name.
 * The avatar is displayed in a circular clipped {@link ImageView} with a fixed size of 18 pixels.
 * The text styling is applied as per {@link StyleManager.Typography#SUBHEADER}.
 *
 * <p>If the cell is empty or the associated profile is null, both text and graphic are cleared.
 */
public class ProfileRowInCombobox extends ListCell<Profile> {

  /**
   * Updates the content and appearance of a cell in the {@link javafx.scene.control.ComboBox}
   * displaying {@link Profile} objects.
   *
   * <p>When the cell is not empty and the provided item is a valid {@link Profile}, the cell
   * displays the profile's name as text and loads their associated avatar via
   * {@link AvatarManager#getAvatarForName(String)}. The avatar is displayed in a circular
   * clipped {@link ImageView} with a diameter of 18 pixels. Text styling is applied
   * using {@link StyleManager.Typography#SUBHEADER}.
   *
   * <p>If the cell is empty or the profile is null, the text and graphic are cleared.
   *
   * @param item  the {@link Profile} object associated with the current cell, or {@code null}
   *              if no item is assigned
   * @param empty {@code true} if the cell is empty (i.e., not displaying any content);
   *              {@code false} otherwise
   */
  @Override
  protected void updateItem(Profile item, boolean empty) {
    super.updateItem(item, empty);
    if (item == null || empty) {
      this.setText(null);
      this.setGraphic(null);
    } else {
      this.setText(item.getName());
      if (item.getAvatarHash() != null) {
        Image avatar = AvatarManager.getAvatarForHashedName(item.getAvatarHash());
        ImageView imageView = new ImageView(avatar);
        int size = 18;
        Circle clip = new Circle(size, size, size);
        imageView.setClip(clip);
        this.setGraphic(imageView);
      } else {
        this.setGraphic(null);
      }
      StyleManager.apply(this.lookup(".list-cell"), StyleManager.Typography.SUBHEADER);
    }
  }
}
