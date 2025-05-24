package no.ntnu.principes.util.images;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AvatarManagerTest {

  @BeforeEach
  public void setUp() {
    // Clear the cache
    AvatarManager.clearCache();
  }

  @Test
  public void testGetHashFor() {
    String name = "John Doe";

    String hash = AvatarManager.getHashFor(name);

    assertNotNull(hash);
    assertFalse(hash.isEmpty());
    assertEquals(32, hash.length());
  }

  @Test
  public void testGetHashForNormalization() {
    String name1 = "John Doe";
    String name2 = "john doe";
    String name3 = " John Doe ";

    String hash1 = AvatarManager.getHashFor(name1);
    String hash2 = AvatarManager.getHashFor(name2);
    String hash3 = AvatarManager.getHashFor(name3);

    assertEquals(hash1, hash2);
    assertEquals(hash1, hash3);
  }
}