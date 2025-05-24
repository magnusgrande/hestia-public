package no.ntnu.principes.view;

import atlantafx.base.theme.Styles;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import no.ntnu.principes.components.primary.StyledText;
import no.ntnu.principes.components.primary.Text;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.service.NavigationService;
import no.ntnu.principes.util.styles.StyleManager;

public class SplashScreenView extends BaseScreen {
		public static boolean hasRun = false;
		private static final String LOGO_TEXT = "Principes";
		private static final String SUBTITLE_TEXT = "IDATA1005 - NTNU (2025)";
		private static final double LETTER_DELAY = 50;
		private static final double SUBTITLE_DELAY = 1000;
		private static final double MOVE_UP_DURATION = 600;
		private static final double SUBTITLE_FADE_DURATION = 800;
		private static final double FINAL_PAUSE = 3000;
		private static final double FADE_OUT_DELAY = 2000;
		private static final double FADE_OUT_DURATION = 2000;

		private static final Interpolator LETTER_FADE_IN = Interpolator.SPLINE(0.4, 0, 0.2, 1);
		private static final Interpolator MOVE_UP = Interpolator.SPLINE(0.4, 0, 0, 1);
		private static final Interpolator SUBTITLE_FADE = Interpolator.SPLINE(0.4, 0, 0.2, 1);
		private static final Interpolator FADE_OUT = Interpolator.SPLINE(0.8, 0, 0.2, 1);

		private final Group lettersGroup = new Group();
		private final List<Text> letters = new ArrayList<>();
		private final Text subtitle;
		private final StackPane container;
		private EventHandler<KeyEvent> keyEventHandler;

		public SplashScreenView(ScreenController controller, String screenId) {
				super(controller, screenId);

				this.container = new StackPane();
				this.subtitle = new Text(SUBTITLE_TEXT, StyledText.TextType.SUBHEADER);
				this.subtitle.setOpacity(0);
		}

		@Override
		protected void initializeScreen() {
				this.setupView();
				this.createLetters();
		}

		@Override
		protected void onNavigatedTo() {
				Timeline starter = new Timeline(new KeyFrame(Duration.millis(100), e -> startAnimation()));
				starter.play();
		}

		private void setupView() {
				container.setMinHeight(200);
				container.setMinWidth(400);
				container.getChildren().addAll(lettersGroup, subtitle);
				StyleManager.apply(container, Styles.BG_DEFAULT);
				container.setViewOrder(-1);
				StyleManager.growHorizontal(container);
				StyleManager.growVertical(container);
				subtitle.setTranslateY(40);

				getChildren().add(container);
		}

		private void createLetters() {
				double totalWidth = calculateTotalWidth();
				double startX = -totalWidth / 2;

				for (int i = 0; i < LOGO_TEXT.length(); i++) {
						Text letter = this.createLetter(LOGO_TEXT.charAt(i), startX, i);
						letters.add(letter);
						lettersGroup.getChildren().add(letter);

						if (i == 0) {
								startX += 22;
						} else {
								if (i + 1 < LOGO_TEXT.length() && LOGO_TEXT.charAt(i) == 'i') {
										startX += 14;
								} else if (i + 1 < LOGO_TEXT.length() && LOGO_TEXT.charAt(i + 1) == 'i') {
										startX += 18;
								} else {
										startX += 22;
								}
						}
				}
		}

		private double calculateTotalWidth() {
				double width = 0;
				for (char c : LOGO_TEXT.toCharArray()) {
						Text temp = new Text(String.valueOf(c), StyledText.TextType.PAGE_TITLE);
						StyleManager.apply(temp, StyleManager.Typography.PAGE_TITLE);
						width += temp.getBoundsInLocal().getWidth();
				}
				return width;
		}

		private Text createLetter(char letter, double x, int index) {
				Text text = new Text(String.valueOf(letter), StyledText.TextType.PAGE_TITLE);
				StyleManager.apply(text, StyleManager.Typography.PAGE_TITLE);
				text.setTranslateX(x);
				text.setOpacity(0);
				return text;
		}

		private void startAnimation() {
				if (hasRun) {
						return;
				}
				hasRun = true;
				Timeline timeline = new Timeline();
				double currentTime = 0;

				// Fade in letters one by one
				for (int i = 0; i < letters.size(); i++) {
						Text letter = letters.get(i);
						timeline.getKeyFrames().addAll(
								new KeyFrame(
										Duration.millis(currentTime),
										new KeyValue(letter.opacityProperty(), 0, LETTER_FADE_IN)
								),
								new KeyFrame(
										Duration.millis(currentTime + LETTER_DELAY),
										new KeyValue(letter.opacityProperty(), 1, LETTER_FADE_IN)
								)
						);
						currentTime += LETTER_DELAY;
				}

				currentTime += SUBTITLE_DELAY;

				// Move letters up
				for (Text letter : letters) {
						timeline.getKeyFrames().addAll(
								new KeyFrame(
										Duration.millis(currentTime),
										new KeyValue(letter.translateYProperty(), letter.getTranslateY(), MOVE_UP)
								),
								new KeyFrame(
										Duration.millis(currentTime + MOVE_UP_DURATION),
										new KeyValue(letter.translateYProperty(), letter.getTranslateY() - 40, MOVE_UP)
								)
						);
				}

				// Fade in subtitle
				timeline.getKeyFrames().addAll(
						new KeyFrame(
								Duration.millis(currentTime),
								new KeyValue(subtitle.opacityProperty(), 0, SUBTITLE_FADE)
						),
						new KeyFrame(
								Duration.millis(currentTime + SUBTITLE_FADE_DURATION),
								new KeyValue(subtitle.opacityProperty(), 1, SUBTITLE_FADE)
						)
				);

				// Calculate fade out
				double fadeOutStart = currentTime + FINAL_PAUSE - FADE_OUT_DELAY;
				timeline.getKeyFrames().addAll(
						new KeyFrame(
								Duration.millis(fadeOutStart),
								new KeyValue(this.container.opacityProperty(), 1, FADE_OUT)
						),
						new KeyFrame(
								Duration.millis(fadeOutStart + FADE_OUT_DURATION),
								new KeyValue(this.container.opacityProperty(), 0, FADE_OUT)
						)
				);

				// Final cleanup
				currentTime += FINAL_PAUSE;
				timeline.getKeyFrames().add(new KeyFrame(
						Duration.millis(currentTime),
						e -> NavigationService.closeScreen("splashScreen")
				));

				NavigationService.navigate((String) this.getContext().getParameter("nextScreen"));


				this.keyEventHandler = (e) -> {
						if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
								timeline.stop();
								NavigationService.closeScreen("splashScreen");
						}
				};

				this.controller.getStage().addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
				this.requestFocus();

				timeline.play();
		}

		@Override
		protected void onNavigatedFrom() {
				if (this.keyEventHandler != null) {
						this.controller.getStage()
								.removeEventHandler(KeyEvent.KEY_PRESSED, this.keyEventHandler);
				}
		}
}