package no.ntnu.principes.util;

public class StringUtils {

  public static String titleCase(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }
}
