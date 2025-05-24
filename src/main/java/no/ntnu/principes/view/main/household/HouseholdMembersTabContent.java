package no.ntnu.principes.view.main.household;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.components.BaseComponent;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.domain.profile.Profile;
import no.ntnu.principes.repository.MemberRepository;
import no.ntnu.principes.service.DatabaseManager;
import no.ntnu.principes.service.DeletionService;
import no.ntnu.principes.util.Auth;
import no.ntnu.principes.util.images.AvatarManager;
import no.ntnu.principes.util.styles.InsetBuilder;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

@Slf4j
public class HouseholdMembersTabContent extends BaseComponent {
  private final VBox content = new VBox();
  private final MemberRepository memberRepository;
  private final VBox memberContent = new VBox(10);

  public HouseholdMembersTabContent(BaseScreen parentScreen) {
    super("householdMembersTabContent", parentScreen);
    this.memberRepository = DatabaseManager.getInstance().getRepository(MemberRepository.class);
  }

  @Override
  protected void initializeComponent() {
    this.getChildren().add(content);

    this.content.setSpacing(20);
    StyleManager.growHorizontal(content);
    this.addMessage();
    this.content.getChildren().add(memberContent);
  }

  private void addMessage() {
    Message message = new Message(
        "How do I change the members?",
        "Just add or remove a name from the list below, and the members will be updated!",
        new FontIcon(Material2OutlinedAL.HELP_OUTLINE)
    );
    StyleManager.apply(message, Styles.ACCENT);
    StyleManager.growHorizontal(message);
    this.content.getChildren().add(message);
  }

  private void fetchMembers() {
    log.debug("Fetching members");
    List<Profile> members = memberRepository.findAll();
    log.debug("Found {} members", members.size());
    this.memberContent.getChildren().clear();
    if (members.isEmpty()) {
      this.memberContent.getChildren()
          .add(new Text("No members found", StyledText.TextType.BODY));
    } else {
      for (Profile member : members) {
        this.memberContent.getChildren().add(this.createMemberRow(member));
      }
    }
    Button addButton = new Button("Add Member", Button.ButtonType.OUTLINED);
    addButton.setGraphic(new FontIcon(Material2OutlinedAL.ADD));
    addButton.setOnAction(event -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Member Registration");
      dialog.setHeaderText("Add New Member");
      dialog.setContentText("Please enter member name:");


      Optional<String> result = dialog.showAndWait();

      // Process the result
      if (result.isPresent()) {
        String memberName = result.get().trim();

        // Basic validation
        if (memberName.isEmpty()) {
          System.out.println("Member name cannot be empty");
          return;
        }

        Profile newMember = Profile.builder()
            .name(memberName)
            .avatarHash(AvatarManager.getHashFor(memberName))
            .createdAt(LocalDateTime.now())
            .build();
        memberRepository.save(newMember);
        this.fetchMembers();
      }
    });
    StyleManager.apply(addButton, Styles.ACCENT);
    this.memberContent.getChildren().add(addButton);

  }

  private Node createMemberRow(Profile member) {
    HBox memberRow = new HBox(10);
    memberRow.setAlignment(Pos.CENTER_LEFT);
    memberRow.setSpacing(10);
    StyleManager.growHorizontal(memberRow);

    TextField nameField = new TextField(member.getName());
    nameField.setPromptText("Member Name");
    nameField.setAlignment(Pos.CENTER_LEFT);
    StyleManager.growHorizontal(nameField);
    nameField.setOnKeyReleased(event -> {
      member.setName(nameField.getText());
      memberRepository.save(member);
    });

    Button removeButton = new Button("Remove", Button.ButtonType.OUTLINED);
    removeButton.setGraphic(new FontIcon(Material2OutlinedAL.DELETE));
    removeButton.setDisable(Objects.equals(member.getId(), Auth.getInstance().getProfileId()));
    removeButton.setTooltip(
        new Tooltip(
            "You cannot remove yourself from the household."
        )
    );
    removeButton.setOnAction(event -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Dangerous operation");
      confirm.setHeaderText("Are you sure you want to remove " + member.getName() + "?");
      confirm.setContentText(
          "All task assignments, and stats will also be deleted for " + member.getName());
      confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          DeletionService.deleteMember(member.getId());
          this.fetchMembers();
        }
      });
    });
    StyleManager.apply(removeButton, Styles.DANGER);
    removeButton.setPadding(InsetBuilder.uniform(10).build());
    removeButton.setText("Remove");
    StyleManager.shrinkHorizontal(removeButton);
    memberRow.getChildren().addAll(nameField, removeButton);
    return memberRow;
  }

  @Override
  protected void onMount() {
    this.fetchMembers();
  }

  @Override
  protected void onUnmount() {

  }

  @Override
  protected void onDestroy() {

  }
}
