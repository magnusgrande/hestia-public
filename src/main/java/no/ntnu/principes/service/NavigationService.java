package no.ntnu.principes.service;

import java.util.Map;
import java.util.UUID;
import no.ntnu.principes.event.PrincipesEventBus;
import no.ntnu.principes.event.navigation.CloseModalEvent;
import no.ntnu.principes.event.navigation.NavigateEvent;
import no.ntnu.principes.event.navigation.NavigationClearStackEvent;
import no.ntnu.principes.event.navigation.OpenModalEvent;
import no.ntnu.principes.util.ModalResult;

/**
 * Provides application-wide navigation capabilities using an event-based approach.
 * Centralizes screen transitions, modal dialogs, and navigation history management
 * without requiring direct references to UI controllers.
 */
public class NavigationService {

  private NavigationService() {
  } // Private constructor to prevent instantiation

  /**
   * Navigates to a specified route.
   * Adds the route to the navigation stack.
   *
   * @param route The destination route name
   */
  public static void navigate(String route) {
    PrincipesEventBus.getInstance().publish(NavigateEvent.push(route));
  }

  /**
   * Navigates to a route with additional parameters.
   * Adds the route to the navigation stack and passes the parameters to the destination screen.
   *
   * @param route  The destination route name
   * @param params Map of parameters to pass to the destination
   */
  public static void navigate(String route, Map<String, Object> params) {
    PrincipesEventBus.getInstance().publish(
        new NavigateEvent(
            new NavigateEvent.NavigationPayload(route, NavigateEvent.NavigationType.PUSH,
                params)
        )
    );
  }

  /**
   * Performs navigation using a pre-configured navigation payload.
   * Allows for complete control over navigation type and parameters.
   *
   * @param payload The pre-configured navigation payload
   */
  public static void navigate(NavigateEvent.NavigationPayload payload) {
    PrincipesEventBus.getInstance().publish(
        new NavigateEvent(payload)
    );
  }

  /**
   * Closes a specific screen by its route name.
   * Removes the screen from the navigation stack.
   *
   * @param route The route name of the screen to close
   */
  public static void closeScreen(String route) {
    PrincipesEventBus.getInstance().publish(NavigateEvent.pop(route));
  }

  /**
   * Opens a modal dialog over the current screen.
   *
   * @param route      The route name of the modal to open
   * @param callBackId An identifier to associate with modal results
   */
  public static void openModal(String route, String callBackId) {
    PrincipesEventBus.getInstance().publish(OpenModalEvent.of(route, callBackId));
  }

  /**
   * Closes a modal dialog with a result.
   * The result can be processed by screens that are waiting for modal feedback.
   *
   * @param modalId The ID of the modal to close
   * @param result  The result to return from the modal
   */
  public static void closeModal(String modalId, ModalResult result) {
    PrincipesEventBus.getInstance().publish(CloseModalEvent.of(modalId, result));
  }

  /**
   * Clears the entire navigation stack for a stage.
   * Removes all screens and returns to the initial state.
   *
   * @param stageId The ID of the stage to clear
   */
  public static void clear(UUID stageId) {
    PrincipesEventBus.getInstance().publish(new NavigationClearStackEvent(stageId));
  }

  /**
   * Navigates back to the previous screen.
   * Removes the current screen from the navigation stack.
   */
  public static void back() {
    PrincipesEventBus.getInstance().publish(NavigateEvent.pop());
  }
}