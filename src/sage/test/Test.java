package sage.test;

import sage.Vocabulary;
import sage.util.URIUtil;
import sage.util.Values;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Created by Anurag Gautam on 08-03-2016.
 */
public class Test {
    static final Logger L = Logger.getGlobal();

    public static void main(String[] args) {
        Values.loadValues();
        Vocabulary v = Vocabulary.getInstance();
        TreeSet<String> unigrams = new TreeSet<>();
        TreeSet<String> bigrams = new TreeSet<>();

        String text = URIUtil.readFromURI("https://en.wikipedia.org/wiki/Rice");
        if (text != null) {
            String prevWord = null;
            BreakIterator wordBreaker = BreakIterator.getWordInstance(Locale.ENGLISH);
            wordBreaker.setText(text);
            int start = wordBreaker.first();
            for (int end = wordBreaker.next(); end != BreakIterator.DONE; start = end, end = wordBreaker.next()) {
                String curWord = text.substring(start, end).trim().toLowerCase();
                if (v.contains(curWord)) {
                    unigrams.add(curWord);
                }
                String bi = prevWord + " " + curWord;
                if (v.contains(bi)) {
                    bigrams.add(bi);
                }

                prevWord = curWord;
            }

            L.info("Found " + unigrams.size() + " unigrams vocabulary words");
            L.info("Found " + bigrams.size() + " bigrams");
            System.out.println(unigrams);
            System.out.println(bigrams);
        } else {
            L.warning("the text read from the url is null");
        }

    }
}
