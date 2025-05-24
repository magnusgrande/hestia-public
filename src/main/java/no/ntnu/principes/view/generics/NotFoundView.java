package no.ntnu.principes.view.generics;


import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import no.ntnu.principes.components.primary.Button;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.styles.StyleManager;
import no.ntnu.principes.view.BaseScreen;

public class NotFoundView extends BaseScreen {
		public static final String SCREEN_ID = "not-found";

		public NotFoundView(ScreenController controller, String screenId) {
				super(controller, screenId);
		}

		@Override
		protected void initializeScreen() {
				StyleManager.grow(this);
				Text title = new Text("Not Found", Text.TextType.PAGE_TITLE);
				Text subheader = new Text("The requested page was not found", Text.TextType.SUBHEADER);
				Button backButton = new Button("Back", Button.ButtonType.DEFAULT);
				backButton.setOnAction(e -> NavigationService.back());
				VBox content = new VBox(10, title, subheader, backButton);
				content.setAlignment(Pos.CENTER);
				this.setAlignment(Pos.CENTER);
				this.getChildren().add(content);
		}
}
