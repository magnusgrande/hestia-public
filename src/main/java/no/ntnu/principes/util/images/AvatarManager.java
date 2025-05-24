package no.ntnu.principes.util.images;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages avatar images using the DiceBear API.
 * Provides caching and thread-safe access to avatar images.
 */
@Slf4j
public class AvatarManager {
  private static final String AVATAR_API_BASE = "https://api.dicebear.com/9.x/pixel-art/jpg";
  private static final int AVATAR_SIZE = 160;
  private static final int DISPLAY_SIZE = 40;
  private static final int HASH_CUTOF_LENGTH = 8;

  // ConcurrentHashMap is used to ensure thread safety, as the cache may be accessed by multiple
  // threads as this is a static utility class and not a scene or component.
  private static final Map<String, Image> avatarCache = new ConcurrentHashMap<>();

  /**
   * Gets an avatar image for a name, creating it if not already cached.
   * Hashes the name first for privacy and deterministic image generation.
   *
   * @param name The name to get an avatar for
   * @return The avatar image, either from cache or newly created
   */
  public static Image getAvatarForName(String name) {
    String hashedName = getHashFor(name);
    return avatarCache.computeIfAbsent(hashedName, AvatarManager::createAvatar);
  }

  /**
   * Gets a cached avatar for a name without creating a new one if not found.
   *
   * @param name The name to get a cached avatar for
   * @return An Optional containing the cached avatar, or empty if not cached
   */
  public static Optional<Image> getCachedAvatarForName(String name) {
    String hashedName = getHashFor(name);
    return Optional.ofNullable(avatarCache.get(hashedName));
  }

  /**
   * Gets an avatar for a pre-hashed name, creating it if not already cached.
   * Logs whether the avatar was fetched or retrieved from cache.
   *
   * @param hashedName The pre-hashed name to get an avatar for
   * @return The avatar image, either from cache or newly created
   */
  public static Image getAvatarForHashedName(String hashedName) {
    if (!avatarCache.containsKey(hashedName)) {
      logCacheResult(hashedName, false);
      Image avatar = createAvatar(hashedName);
      avatarCache.put(hashedName, avatar);
      return avatar;
    } else {
      logCacheResult(hashedName, true);
      return avatarCache.get(hashedName);
    }
  }

  /**
   * Preloads avatars for multiple hashed names.
   * Ensures avatars are available in the cache, loading any that aren't already cached.
   *
   * @param hashedNames Array of pre-hashed names to preload avatars for
   */
  public static void preloadAvatars(String... hashedNames) {
    log.debug("Preloading avatars for {} profiles", hashedNames.length);
    for (String hashedName : hashedNames) {
      if (!avatarCache.containsKey(hashedName)) {
        logCacheResult(hashedName, false);
        avatarCache.put(hashedName, createAvatar(hashedName));
      } else {
        logCacheResult(hashedName, true);
      }
    }
  }

  /**
   * Clears the avatar cache.
   * Removes all cached avatars, freeing memory.
   */
  public static void clearCache() {
    avatarCache.clear();
  }

  /**
   * Creates an avatar image for a hashed name.
   * Loads the image from the DiceBear API with appropriate display settings.
   *
   * @param hashedName The hashed name to create an avatar for
   * @return The created avatar image
   */
  private static Image createAvatar(String hashedName) {
    String url = buildAvatarUrl(hashedName);
    log.debug("Fetching avatar from: {}", url);
    return new Image(url,
        DISPLAY_SIZE,    // requestedWidth
        DISPLAY_SIZE,    // requestedHeight
        true,           // preserveRatio
        true,           // smooth
        true            // backgroundLoading
    );
  }

  /**
   * Builds a URL for fetching an avatar from the DiceBear API.
   *
   * @param hashedName The hashed name to use as the seed for image generation
   * @return The complete URL for fetching the avatar
   */
  private static String buildAvatarUrl(String hashedName) {
    return AVATAR_API_BASE + "?seed=" + hashedName + "&size=" + AVATAR_SIZE
        +
        "&backgroundColor=transparent&mouth=happy01,happy02,happy03,happy04,happy05,happy06,happy07,happy08,happy09,happy10,happy11,happy12,happy13";
  }

  /**
   * Logs whether an avatar was retrieved from cache or fetched from the API.
   * Uses only a prefix of the hashed name for brevity.
   *
   * @param hashedName The hashed name used for the avatar
   * @param isHit      Whether the avatar was found in the cache (hit) or not (miss)
   */
  private static void logCacheResult(String hashedName, boolean isHit) {
    String prefix = hashedName.substring(0, HASH_CUTOF_LENGTH);
    if (isHit) {
      log.debug("[ HIT ] {}", prefix);
    } else {
      log.debug("[FETCH] {}", prefix);
    }
  }

  /**
   * Gets an MD5 hash for a name.
   * Normalizes the name by trimming and converting to lowercase before hashing.
   *
   * @param name The name to hash
   * @return The hexadecimal string representation of the MD5 hash
   * @throws RuntimeException If the MD5 algorithm is not available
   */
  public static String getHashFor(String name) {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      byte[] hash = digest.digest(normalizeInput(name));
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to hash name", e);
    }
  }

  /**
   * Normalizes input for consistent hashing.
   * Converts to lowercase, trims whitespace, and encodes as UTF-8 bytes.
   *
   * @param input The input string to normalize
   * @return The normalized bytes
   */
  private static byte[] normalizeInput(String input) {
    return input.toLowerCase().trim().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Converts a byte array to a hexadecimal string.
   * Ensures each byte is represented by two hex digits.
   *
   * @param bytes The byte array to convert
   * @return The hexadecimal string representation
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}