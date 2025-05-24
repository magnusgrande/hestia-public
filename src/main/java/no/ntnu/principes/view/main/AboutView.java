package no.ntnu.principes.view.main;

import static no.ntnu.principes.Launcher.APP_NAME;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;

/**
 * AboutView displays information about the Hestia application, including its purpose,
 * features, development team, and version.
 */
@Slf4j
public class AboutView extends DashboardScreen {
  private static final String APP_VERSION = "1.0";
  private static final String LOGO_PATH = "/no/ntnu/principes/images/hestia.png";
  private static final String GITHUB_URL =
      "https://github.com/ntnu-ie-iir/prosjekt-idata1005-2025-principes";
  private static final String JAVADOC_URL = "https://docs.principes.no/index.html";

  /**
   * Creates a new AboutView instance.
   *
   * @param controller the screen controller
   * @param screenId   the screen identifier
   */
  public AboutView(ScreenController controller, String screenId) {
    super(controller, screenId);
  }

  @Override
  protected void initializeScreen() {
    super.initializeScreen();

    // Create main content container
    VBox contentBox = new VBox(20);
    contentBox.setPadding(InsetBuilder.uniform(30).build());
    StyleManager.growHorizontal(contentBox);

    // Add header with app logo and title
    contentBox.getChildren().add(createHeader());

    // Add separator
    contentBox.getChildren().add(new Separator());

    // Add about section
    contentBox.getChildren().add(createAboutSection());

    // Add separator
    contentBox.getChildren().add(new Separator());

    // Add team section
    contentBox.getChildren().add(createTeamSection());

    // Add separator
    contentBox.getChildren().add(new Separator());

    // Add footer with version and links
    contentBox.getChildren().add(createFooter());

    // Create scroll pane to handle overflow
    ScrollPane scrollPane = new ScrollPane(contentBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setStyle("-fx-background-color: transparent;");
    StyleManager.grow(scrollPane);

    // Add scroll pane to content
    this.content.getChildren().add(scrollPane);
  }

  /**
   * Creates the header section with logo and title.
   *
   * @return VBox containing the header elements
   */
  private HBox createHeader() {
    HBox header = new HBox(20);
    header.setAlignment(Pos.CENTER_LEFT);

    // Try to load logo if available
    try {
      ImageView logoView = new ImageView(new Image(
          Objects.requireNonNull(getClass().getResourceAsStream(LOGO_PATH))));
      logoView.setFitHeight(100);
      logoView.setPreserveRatio(true);
      Circle logoCircle = new Circle(50, 50, 50);
      logoView.setClip(logoCircle);
      header.getChildren().add(logoView);
    } catch (Exception e) {
      log.warn("Could not load logo image", e);
    }
    Text logoText = new Text(APP_NAME, StyledText.TextType.PAGE_TITLE);
    logoText.setStyle("-fx-font-size: 48px;");
    header.getChildren().add(logoText);

    return header;
  }

  /**
   * Creates the about section with general information.
   *
   * @return VBox containing the about content
   */
  private VBox createAboutSection() {
    VBox section = new VBox(15);

    Text sectionTitle = new Text("About " + APP_NAME, StyledText.TextType.SECTION_HEADER);

    javafx.scene.text.Text descriptionText = new javafx.scene.text.Text(
        APP_NAME +
            " is a JavaFX desktop application designed to help households, shared living spaces, " +
            "and small offices manage and distribute tasks fairly. The application enables users to create, " +
            "assign, and track the completion of various household chores and responsibilities.\n\n" +
            "Named after the Greek goddess of the hearth, home, and family, " + APP_NAME +
            " aims to " +
            "transform household task management into an engaging and fair experience for everyone involved.");
    descriptionText.getStyleClass().add("text-body");
    descriptionText.setWrappingWidth(800);
    descriptionText.setTextAlignment(TextAlignment.LEFT);

    section.getChildren().addAll(sectionTitle, descriptionText);

    return section;
  }

  /**
   * Creates the team section with developer information.
   *
   * @return VBox containing the team content
   */
  private VBox createTeamSection() {
    VBox section = new VBox(15);

    Text sectionTitle = new Text("Development Team", StyledText.TextType.SECTION_HEADER);

    javafx.scene.text.Text teamInfo = new javafx.scene.text.Text(
        APP_NAME +
            " was developed by Team Principes as part of the IDATA1005 Software Engineering course at the Norwegian University of Science and Technology (NTNU).");
    teamInfo.getStyleClass().add("text-body");
    teamInfo.setWrappingWidth(800);

    section.getChildren().addAll(sectionTitle, teamInfo);

    return section;
  }

  /**
   * Creates the footer section with version and links.
   *
   * @return HBox containing the footer content
   */
  private HBox createFooter() {
    HBox footer = new HBox(30);
    footer.setAlignment(Pos.CENTER);

    Text versionText = new Text("Version " + APP_VERSION, StyledText.TextType.BODY);
    versionText.setStyle("-fx-font-style: italic;");

    Hyperlink githubLink = createHyperlink("GitHub Repository", GITHUB_URL);
    Hyperlink javadocLink = createHyperlink("JavaDoc Documentation", JAVADOC_URL);

    footer.getChildren().addAll(versionText, githubLink, javadocLink);

    return footer;
  }

  /**
   * Creates a hyperlink that opens in the default browser.
   *
   * @param text the hyperlink text
   * @param url  the URL to open
   * @return Hyperlink component
   */
  private Hyperlink createHyperlink(String text, String url) {
    Hyperlink link = new Hyperlink(text);
    link.setOnAction(e -> {
      try {
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(new URI(url));
        }
      } catch (IOException | URISyntaxException ex) {
        log.error("Failed to open URL: " + url, ex);
      }
    });
    return link;
  }

  @Override
  protected void onNavigatedTo() {
    super.onNavigatedTo();
    this.controller.getStage().setTitle("About " + APP_NAME);
  }

  @Override
  protected void onNavigatedFrom() {
    super.onNavigatedFrom();
  }
}