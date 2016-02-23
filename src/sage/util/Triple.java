package sage.util;

/**
 * Represents a generic triple
 *
 * @author Anurag Gautam
 * @version revision-history: 200216
 * @since Agromax 1.0
 */
public class Triple<P, Q, R> {
    public final P first;
    public final Q second;
    public final R third;

    public Triple(P first, Q second, R third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", String.valueOf(first), String.valueOf(second), String.valueOf(third));
    }
}