package sage.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.stream.Stream;

/**
 * Created by Anurag Gautam on 22-01-2016.
 */
public class Util {
    private static final Logger logger = Logger.getLogger(Util.class.getName());

    static {
        logger.setUseParentHandlers(false);
        logger.addHandler(new StreamHandler() {

            @Override
            public void publish(LogRecord logRecord) {
                System.out.printf("%s/%s#%s: %s\n",
                        logRecord.getLevel(),
                        logRecord.getSourceClassName(),
                        logRecord.getSourceMethodName(),
                        logRecord.getMessage()
                );
            }
        });
    }

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

//        StringBuilder value = new StringBuilder();
        StringJoiner joiner = new StringJoiner(separator);
        if (decorator != null) {
//            stream.forEach(e -> value.append(decorator.apply(e.word())).append(separator));
            stream.forEach(w -> joiner.add(decorator.apply(w.word())));
        } else {
//            stream.forEach(e -> value.append(e.word()).append(separator));
            stream.forEach(w -> joiner.add(w.word()));
        }

//        return value.toString().trim();
        return joiner.toString();
    }

    public static String weld(Stream<String> stream, String sep) {
        StringJoiner joiner = new StringJoiner(sep);
        stream.forEach(joiner::add);
        return joiner.toString();
    }

    public static String join(Stream<? extends HasWord> stream, String separator) {
        Objects.requireNonNull(separator, "ComparableWord separator cannot be null");
        Objects.requireNonNull(stream, "ComparableWord stream cannot be null");

//        StringBuilder value = new StringBuilder();
//        stream.forEach(e -> value.append(e.word()).append(separator));
//
//        return value.toString().trim();
        return join(stream, separator, null);
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

    public static String read(URI uri) {
        String text = null;
        try {
            logger.info("Reading " + uri);
            Document doc = Jsoup.connect(uri.toString()).get();
            doc.getElementsByTag("sup").forEach(Node::remove);
            text = doc.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}
