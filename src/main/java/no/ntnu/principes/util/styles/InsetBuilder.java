package no.ntnu.principes.util.styles;

import javafx.geometry.Insets;

/**
 * Fluent builder for creating JavaFX Insets objects.
 * Provides multiple factory methods and a chained API for creating
 * insets with different configurations.
 */
public class InsetBuilder {
  private final double top;
  private final double right;
  private final double bottom;
  private final double left;

  /**
   * Creates an InsetBuilder from a builder instance.
   *
   * @param builder The builder containing the inset values
   */
  private InsetBuilder(Builder builder) {
    this.top = builder.top;
    this.right = builder.right;
    this.bottom = builder.bottom;
    this.left = builder.left;
  }

  /**
   * Creates a builder with uniform insets on all sides.
   *
   * @param inset The value to use for all sides
   * @return A builder configured with uniform insets
   */
  public static Builder uniform(double inset) {
    return new Builder(inset);
  }

  /**
   * Creates an empty builder with zero insets.
   *
   * @return A builder with zero insets
   */
  public static Builder create() {
    return new Builder();
  }

  /**
   * Creates a builder with symmetric insets.
   * Uses x for horizontal (left and right) and y for vertical (top and bottom) insets.
   *
   * @param x The horizontal inset value
   * @param y The vertical inset value
   * @return A builder configured with symmetric insets
   */
  public static Builder symmetric(double x, double y) {
    return new Builder()
        .top(y)
        .right(x)
        .bottom(y)
        .left(x);
  }

  /**
   * Creates a builder with custom insets for each side.
   *
   * @param top    The top inset value
   * @param right  The right inset value
   * @param bottom The bottom inset value
   * @param left   The left inset value
   * @return A builder configured with the specified insets
   */
  public static Builder custom(double top, double right, double bottom, double left) {
    return new Builder()
        .top(top)
        .right(right)
        .bottom(bottom)
        .left(left);
  }

  /**
   * Builds the Insets object with the configured values.
   *
   * @return A new Insets object
   */
  public Insets build() {
    return new Insets(top, right, bottom, left);
  }

  /**
   * Builder class for creating InsetBuilder instances.
   * Supports fluent API for setting individual inset values.
   */
  public static class Builder {
    private double top = 0;
    private double right = 0;
    private double bottom = 0;
    private double left = 0;

    /**
     * Creates an empty builder with zero insets.
     */
    private Builder() {
    }

    /**
     * Creates a builder with uniform insets.
     *
     * @param inset The value to use for all sides
     */
    private Builder(double inset) {
      this.top = inset;
      this.right = inset;
      this.bottom = inset;
      this.left = inset;
    }

    /**
     * Sets the top inset value.
     *
     * @param top The top inset value
     * @return This builder for method chaining
     */
    public Builder top(double top) {
      this.top = top;
      return this;
    }

    /**
     * Sets the right inset value.
     *
     * @param right The right inset value
     * @return This builder for method chaining
     */
    public Builder right(double right) {
      this.right = right;
      return this;
    }

    /**
     * Sets the bottom inset value.
     *
     * @param bottom The bottom inset value
     * @return This builder for method chaining
     */
    public Builder bottom(double bottom) {
      this.bottom = bottom;
      return this;
    }

    /**
     * Sets the left inset value.
     *
     * @param left The left inset value
     * @return This builder for method chaining
     */
    public Builder left(double left) {
      this.left = left;
      return this;
    }

    /**
     * Builds the final Insets object.
     * Creates an InsetBuilder and delegates to its build method.
     *
     * @return A new Insets object with the configured values
     */
    public Insets build() {
      return new InsetBuilder(this).build();
    }
  }
}