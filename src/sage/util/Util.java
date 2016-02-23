package sage.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by Anurag Gautam on 22-01-2016.
 */
public class Util {
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static final int BUFFER_SIZE = 2048; // In bytes

    public static IndexedWord newIndexWord(String word, int wordIndex) {
        IndexedWord indexedWord = new IndexedWord(word, -1, wordIndex);
        indexedWord.setWord(word);
        return indexedWord;
    }

    public static BufferedReader getBufferedReader(File file) throws FileNotFoundException {
        Objects.requireNonNull(file);
        return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }

    public static BufferedReader getBufferedReader(String filePath) throws FileNotFoundException {
        Objects.requireNonNull(filePath);
        return new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
    }

    public static BufferedReader getBufferedReader(Path path) throws FileNotFoundException {
        Objects.requireNonNull(path);
        return new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile())));
    }

    public static String join(Stream<? extends HasWord> stream, String separator, Function<String, String> decorator) {
        Objects.requireNonNull(separator, "ComparableWord separator cannot be null");
        Objects.requireNonNull(stream, "ComparableWord stream cannot be null");

        StringBuilder value = new StringBuilder();
        if (decorator != null) {
            stream.forEach(e -> value.append(decorator.apply(e.word())).append(separator));
        } else {
            stream.forEach(e -> value.append(e.word()).append(separator));
        }

        return value.toString().trim();
    }

    public static String weld(Stream<String> stream, String sep) {
        StringJoiner joiner = new StringJoiner(sep);
        stream.forEach(joiner::add);
        return joiner.toString();
    }

    public static String join(Stream<? extends HasWord> stream, String separator) {
        Objects.requireNonNull(separator, "ComparableWord separator cannot be null");
        Objects.requireNonNull(stream, "ComparableWord stream cannot be null");

        StringBuilder value = new StringBuilder();
        stream.forEach(e -> value.append(e.word()).append(separator));

        return value.toString().trim();
    }

    public static boolean inOpenRange(int a, int x, int b) {
        return x > a && x < b;
    }

    public static boolean inClosedRange(int a, int x, int b) {
        return x >= a && x <= b;
    }

    public static boolean inOpenRange(long a, long x, long b) {
        return x > a && x < b;
    }

    public static boolean inClosedRange(long a, long x, long b) {
        return x >= a && x <= b;
    }

    /**
     * Reads a text file completely
     *
     * @param path path of the text file
     * @return
     */
    public static String read(Path path) {
        Objects.requireNonNull(path, "File path must not be null");

        logger.info("Trying to read the following file: " + path);

        StringBuilder text = new StringBuilder();

        try (FileReader reader = new FileReader(path.toFile())) {
            char[] buf = new char[BUFFER_SIZE];
            while (true) {
                int bytesRead = reader.read(buf);
                if (bytesRead <= -1) break;
                text.append(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }
}
