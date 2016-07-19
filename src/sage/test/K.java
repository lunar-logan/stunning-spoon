package sage.test;

import sage.util.Values;

import java.net.URISyntaxException;

/**
 * Created by Dell on 03-06-2016.
 */
public class K {
    public static void main(String[] args) throws URISyntaxException {
        Values.loadValues();
        System.out.println(Values.getVocabPath());

    }
}
