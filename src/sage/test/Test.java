package sage.test;

import sage.Vocabulary;
import sage.util.Values;

import java.util.logging.Logger;

/**
 * Created by Anurag Gautam on 08-03-2016.
 */
public class Test {
    static final Logger L = Logger.getGlobal();


    public static void main(String[] args) {
        Values.loadValues();
        Vocabulary V = Vocabulary.getInstance();
        System.out.println(V.contains("solanum"));
        System.out.println(V.contains("lycopersicum"));
        System.out.println(V.contains("berry"));

    }
}
