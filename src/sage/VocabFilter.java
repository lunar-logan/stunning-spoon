package sage;

import edu.stanford.nlp.ling.IndexedWord;
import sage.spi.Filter;
import sage.spi.Triplet;

/**
 * Created by Anurag Gautam on 25-02-2016.
 */
public class VocabFilter implements Filter {
    private final Vocabulary vocabulary;
    private static VocabFilter instance = null;

    private VocabFilter(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public static VocabFilter getInstance(Vocabulary vocabulary) {
        if (instance == null) {
            instance = new VocabFilter(vocabulary);
        }
        return instance;
    }

    private boolean apply(Triplet t) {
        boolean subFound = false;
        boolean objFound = false;

        for (IndexedWord subWord : t.getSubject()) {
            subFound = (subFound || vocabulary.contains(subWord.word()));
        }

        for (IndexedWord objWord : t.getObject()) {
            objFound = (objFound || vocabulary.contains(objWord.word()));
        }
        return subFound && objFound;
    }

    @Override
    public boolean test(Triplet t) {
        return apply(t);
    }
}
