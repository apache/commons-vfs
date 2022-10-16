package org.apache.commons.vfs2.provider.ftp;

import java.io.Serializable;

/**
 * The FTP active port range.
 */
public final class FtpActivePortRange implements Serializable {

    /**
     * Serialization version.
     *
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Obtains FTP active port range with the specified minimum and maximum values (both inclusive).
     *
     * @param minimum the minimum port (inclusive)
     * @param maximum the maximum port (inclusive)
     * @throws IllegalArgumentException if minimum value is greater than maximum one
     */
    public static FtpActivePortRange of(final int minimum, final int maximum) {
        return new FtpActivePortRange(minimum, maximum);
    }

    /**
     * The minimum value in this range (inclusive).
     */
    private final int minimum;

    /**
     * The maximum value in this range (inclusive).
     */
    private final int maximum;

    /**
     * Creates an instance.
     *
     * @param minimum the minimum port (inclusive)
     * @param maximum the maximum port (inclusive)
     */
    private FtpActivePortRange(final int minimum, final int maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("The maximum value must be greater or equal minimum one: maximum=" +
                    maximum + ", minimum=" + minimum);
        }
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    /**
     * <p>Compares this range to another object to test if they are equal.</p>.
     *
     * <p>To be equal, the minimum and maximum values must be equal</p>
     *
     * @param obj the reference object with which to compare
     * @return true if this object is equal
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            final FtpActivePortRange range = (FtpActivePortRange) obj;
            return (minimum == range.minimum) && (maximum == range.maximum);
        }
    }

    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode;

    /**
     * <p>Gets a suitable hash code for the range.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (hashCode == 0) {
            result = 17;
            result = 37 * result + getClass().hashCode();
            result = 37 * result + minimum;
            result = 37 * result + maximum;
            hashCode = result;
        }
        return result;
    }

    /**
     * Cached output toString (class is immutable).
     */
    private transient String toString;

    /**
     * <p>Gets the range as a {@code String}.</p>
     *
     * <p>The format of the String is '[<i>min</i>..<i>max</i>]'.</p>
     *
     * @return the {@code String} representation of this range
     */
    @Override
    public String toString() {
        if (toString == null) {
            toString = "[" + minimum + ".." + maximum + "]";
        }
        return toString;
    }
}
