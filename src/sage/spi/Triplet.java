package sage.spi;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;

import java.util.List;

/**
 * Created by Anurag Gautam on 25-02-2016.
 */
public interface Triplet {
    List<IndexedWord> getSubject();

    List<IndexedWord> getPredicate();

    List<IndexedWord> getObject();

    List<? extends HasWord> getOriginalSentence();

    boolean hasSubject();

    boolean hasObject();

    boolean hasPredicate();

    String getAsCSV();

    String getAsTSV();
}
