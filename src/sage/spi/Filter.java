package sage.spi;

import java.util.function.Predicate;

/**
 * Represents a filter for the SPO triple
 * Created by Anurag Gautam on 25-02-2016.
 */
public interface Filter extends Predicate<Triplet> {
    boolean test(Triplet t);
}
