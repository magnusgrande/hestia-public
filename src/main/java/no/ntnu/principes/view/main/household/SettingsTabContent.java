package no.ntnu.principes.view.main.household;

import atlantafx.base.controls.Message;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.ConfigValue;
import no.ntnu.principes.repository.ConfigValueRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

@Slf4j
public class SettingsTabContent extends BaseComponent {
  private final VBox content = new VBox();
  private final ConfigValueRepository configRepository;
  private BooleanProperty isAdmin = new SimpleBooleanProperty(false);

  public SettingsTabContent(BaseScreen parentScreen) {
    super("settingsTabContent", parentScreen);
    this.configRepository =
        DatabaseManager.getInstance().getRepository(ConfigValueRepository.class);
  }

  @Override
  protected void initializeComponent() {
    this.getChildren().add(content);
    this.content.setSpacing(30);
    StyleManager.growHorizontal(content);
    this.content.setPadding(InsetBuilder.uniform(20).build());

    // Add settings groups
    this.addAccessibilitySettings();

    // Only show admin settings to admins
    this.isAdmin.addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        addAdminSettings();
      } else {
        removeAdminSettings();
      }
    });
  }

  private boolean isUserAdmin(Long profileId) {
    // No roles yet, so just the first registered user.
    log.debug("Checking if user is admin: {}, {}", profileId, profileId == 1L);
    return profileId == 1L;
  }

  private void addAccessibilitySettings() {
    VBox group = createSettingsGroup("Accessibility",
        "These are options to provide helpful changes to the application for your current profile");

    addToggleSetting(group, "settings.colorblind_mode", "Colorblind mode (coming soon)", false,
        true);
    addToggleSetting(group, "settings.larger_text", "Larger text (coming soon)", false, true);
    addToggleSetting(group, "settings.screenreader", "Screenreader (coming soon)", false, true);
    addToggleSetting(group, "settings.darkmode", "Darkmode", false);

    this.content.getChildren().add(group);
  }

  private void addAdminSettings() {
    VBox group = createSettingsGroup("Admin",
        "These are options that only the admin may change.");
    group.setId("adminSettingsGroup");

    addToggleSetting(group, "settings.allow_registration",
        "Allow new registrations from login page",
        true);
    addToggleSetting(group, "settings.allow_reassign",
        "Allow users to reassign tasks to someone else",
        false);
    addToggleSetting(group, "settings.allow_delete", "Allow anyone to delete tasks", false);
    addToggleSetting(group, "settings.allow_create", "Allow anyone to create new tasks", true);

    Message message = new Message(
        "Admin privileges",
        "These settings affect all users in the household. Use with caution.",
        new FontIcon(Material2OutlinedAL.INFO)
    );
    StyleManager.apply(message, Styles.ACCENT);
    StyleManager.margin(message, InsetBuilder.create().top(10).build());
    group.getChildren().add(message);

    this.content.getChildren().add(group);
  }

  private void removeAdminSettings() {
    this.content.getChildren()
        .removeIf(node -> node.getId() != null && node.getId().equals("adminSettingsGroup"));
  }

  private VBox createSettingsGroup(String title, String description) {
    VBox group = new VBox();
    group.setPadding(new Insets(0, 0, 10, 0));

    Text titleText = new Text(title, StyledText.TextType.SECTION_HEADER);
    Text descriptionText = new Text(description, StyledText.TextType.HELPER);

    group.getChildren().addAll(titleText, descriptionText);
    return group;
  }

  private void addToggleSetting(VBox group, String key, String label, boolean defaultValue) {
    addToggleSetting(group, key, label, defaultValue, null, false);
  }

  private void addToggleSetting(VBox group, String key, String label, boolean defaultValue,
                                boolean disabled) {
    addToggleSetting(group, key, label, defaultValue, null, disabled);
  }


  private void addToggleSetting(VBox group, String key, String label, boolean defaultValue,
                                Consumer<Boolean> changeHandler, boolean disabled) {
    HBox setting = new HBox(10);
    setting.setAlignment(Pos.CENTER_LEFT);
    setting.setSpacing(15);
    setting.setPadding(new Insets(5, 0, 5, 0));
    StyleManager.growHorizontal(setting);

    ToggleSwitch toggle = new ToggleSwitch();

    // Set initial state from config repository
    boolean initialState = false;
    ObservableObjectValue<ConfigValue> configValue =
        configRepository.getValueOrDefault(key, defaultValue);
    try {
      initialState = configValue.get().getBooleanValue();
    } catch (Exception e) {
      log.error("Error loading setting {}: {}", key, e.getMessage());
      initialState = defaultValue;
    }
    toggle.setSelected(initialState);

    // Create label for the setting
    Text nameLabel = new Text(label, StyledText.TextType.BODY);
    StyleManager.apply(nameLabel, StyleManager.Typography.BODY);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setMaxWidth(Double.MAX_VALUE);

    // Add change listener
    toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
      try {
        log.debug("Setting {} changed to {}", key, newVal);
        configRepository.setConfigValue(key, newVal);

        // Call the custom handler if provided
        if (changeHandler != null) {
          changeHandler.accept(newVal);
        }
      } catch (Exception e) {
        log.error("Error saving setting {}: {}", key, e.getMessage());
        // Revert the toggle if saving fails
        toggle.setSelected(oldVal);
      }
    });
    configValue.addListener((obs, oldVal, newVal) -> {
      log.debug("Setting {} changed to {}", key, newVal);
      if (newVal != null) {
        toggle.setSelected(newVal.getBooleanValue());
      }
    });
    toggle.setDisable(disabled);
    nameLabel.setTextFill(disabled ? Color.GRAY : Color.BLACK);

    setting.getChildren().addAll(toggle, nameLabel);
    group.getChildren().add(setting);
  }


  @Override
  protected void onMount() {
    this.isAdmin.set(Auth.getInstance().isAuthenticated() &&
        Auth.getInstance().getProfile() != null &&
        isUserAdmin(Auth.getInstance().getProfileId()));
  }

  @Override
  protected void onUnmount() {
  }

  @Override
  protected void onDestroy() {
  }
}