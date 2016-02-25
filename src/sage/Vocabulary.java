package sage;

import sage.util.Util;
import sage.util.Values;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * @author Anurag Gautam
 * @version revision history: 220116, 100216, 250216
 */
public class Vocabulary {
    private static final Logger logger = Logger.getLogger(Vocabulary.class.getName());

    static {
        logger.setUseParentHandlers(false);
        logger.addHandler(new StreamHandler() {
            @Override
            public void publish(LogRecord logRecord) {
                System.out.printf(
                        "%s/%s#%s: %s\n",
                        logRecord.getLevel().getName().charAt(0),
                        logRecord.getSourceClassName(),
                        logRecord.getSourceMethodName(),
                        logRecord.getMessage()
                );
            }
        });
    }

    private HashSet<String> words = new HashSet<>();

    private Vocabulary() {
        try {
            Files.newDirectoryStream(Values.getVocabPath()).forEach(f -> {
                File vocabFile = f.toFile();
                String fileName = vocabFile.getName();
                if (vocabFile.isFile() && fileName.endsWith("txt")) {
                    logger.info("Loading vocabulary from: " + vocabFile);
                    parseFile(vocabFile);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFile(File vocabFile) {
        try (BufferedReader br = Util.getBufferedReader(vocabFile)) {
            br.lines().forEach(line -> {
                line = line.trim().replaceAll(" +", " ").toLowerCase();
                int fsp = line.indexOf(' ');
                String word = line.substring(fsp + 1);
                words.add(word);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Vocabulary instance = new Vocabulary();

    public static Vocabulary getInstance() {
        return instance;
    }

    public int size() {
        return words.size();
    }

    public HashSet<String> get() {
        return words;
    }

    /**
     * Counts the number of <b>terms</b> in the vocabulary having the <b>given no. of words</b>.
     * <p>For example the term <b>{@code Oriza Sativa}</b> has <b>2</b> words in it <b>{@code Oriza}</b> and <b>{@code Sativa}</b></p>
     *
     * @param n the word count
     * @return
     */
    public long countN(int n) {
        return words
                .stream()
                .filter(w -> !w.isEmpty() && w.split(" +").length == n)
                .count();
    }

    public boolean contains(String word) {
        return words.contains(word.toLowerCase());
    }
}
