package no.ntnu.principes.util;

import static org.reflections.scanners.Scanners.SubTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import no.ntnu.principes.controller.screen.ScreenController;
import no.ntnu.principes.view.BaseScreen;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Handles automatic registration of application screens.
 * Uses reflection to locate and register all screen classes in the view package.
 */
@Slf4j
public class ScreenRegistry {
  private static final String VIEW_PACKAGE = "no.ntnu.principes.view";

  /**
   * Automatically discovers and registers all screen classes with the controller.
   * Scans for classes extending BaseScreen and instantiates them.
   *
   * @param controller The controller to register screens with
   * @throws RuntimeException If registration fails due to reflection or instantiation errors
   */
  public static void registerScreens(ScreenController controller) {
    try {
      log.info("Starting automatic screen registration");

      // Configure reflection to scan the view package
      Reflections reflections = new Reflections(new ConfigurationBuilder()
          .setUrls(ClasspathHelper.forPackage(VIEW_PACKAGE))
          .setScanners(SubTypes.filterResultsBy(s -> true)));

      // Find all classes that extend BaseScreen
      Set<Class<? extends BaseScreen>> screenClasses = reflections.getSubTypesOf(BaseScreen.class);

      log.debug("Found {} screen classes", screenClasses.size());

      // Register each screen
      for (Class<? extends BaseScreen> screenClass : screenClasses) {
        registerScreen(controller, screenClass);
      }

      log.info("Completed automatic screen registration");
    } catch (Exception e) {
      log.error("Error during automatic screen registration", e);
      throw new RuntimeException("Failed to register screens automatically", e);
    }
  }

  /**
   * Registers a single screen class with the controller.
   * Creates an instance using the constructor that takes controller and ID.
   *
   * @param controller  The controller to register the screen with
   * @param screenClass The screen class to register
   * @throws RuntimeException If registration fails due to instantiation errors
   */
  private static void registerScreen(ScreenController controller,
                                     Class<? extends BaseScreen> screenClass) {
    try {
      // Skip abstract classes
      if (Modifier.isAbstract(screenClass.getModifiers())) {
        log.debug("Skipping abstract class: {}", screenClass.getName());
        return;
      }

      // Generate screenId from class name
      String screenId = generateScreenId(screenClass);

      log.debug("Registering screen: {} with id: {}", screenClass.getName(), screenId);

      // Create instance using constructor that takes ScreenController
      Constructor<? extends BaseScreen> constructor =
          screenClass.getConstructor(ScreenController.class, String.class);
      BaseScreen screenInstance = constructor.newInstance(controller, screenId);

      // Register with controller
      controller.addScreen(screenId, screenInstance);

      log.debug("Successfully registered screen: {}", screenId);
    } catch (Exception e) {
      log.error("Failed to register screen: " + screenClass.getName(), e);
      throw new RuntimeException("Failed to register screen: " + screenClass.getName(), e);
    }
  }

  /**
   * Generates a screen ID from the class name.
   * First tries to use a SCREEN_ID static field if available,
   * otherwise converts the class name to camelCase.
   *
   * @param screenClass The screen class to generate an ID for
   * @return The generated screen ID
   */
  private static String generateScreenId(Class<?> screenClass) {
    try {
      String definedId = screenClass.getDeclaredField("SCREEN_ID").get(null).toString();
      if (!definedId.isEmpty()) {
        return definedId;
      }
    } catch (Exception e) {
      // Will generate ID from class name
    }
    String className = screenClass.getSimpleName();
    // Remove "View" suffix if present
    if (className.endsWith("View")) {
      className = className.substring(0, className.length() - 4);
    }
    // Convert to camelCase if not already
    return className.substring(0, 1).toLowerCase() + className.substring(1);
  }
}