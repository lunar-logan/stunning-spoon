package sage.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anurag Gautam on 21-01-2016.
 */
public final class Values {
    private static final HashMap<String, String> VALUES = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> ARRAY_VALUES = new HashMap<>();

    public static void loadValues() {
        File valuesFile = Paths.get(System.getProperty("user.dir"), "values", "values.xml").toFile();
        try {
            Document values = Jsoup.parse(valuesFile, "UTF-8");
            values.select("string").forEach(item -> {
                VALUES.put(item.attr("name"), item.text());
            });

            values.select("string-array").forEach(array -> {
                ArrayList<String> arrayList = new ArrayList<String>();
                array.select("item").forEach(item -> arrayList.add(item.text()));
                ARRAY_VALUES.put(array.attr("name"), arrayList);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key, String defaultValue) {
        return VALUES.getOrDefault(key, defaultValue);
    }

    public static Path getTestDir() {
        return Paths.get(System.getProperty("user.dir"), VALUES.get("test_dir"));
    }

    public static Path getTestOutDir() {
        return Paths.get(System.getProperty("user.dir"), VALUES.get("test_out_dir"));
    }

    public static Path getVocabPath() {
        return Paths.get(System.getProperty("user.dir"), VALUES.get("vocab_dir"));
    }

    public static Path getParserModelPath() {
        return Paths.get(VALUES.get("parser_model_path"));
    }

    public static Path getTaggerModelPath() {
        return Paths.get(VALUES.get("tagger_model_path"));
    }

    public static Path getTestDirPath() {
        return Paths.get(System.getProperty("user.dir"), VALUES.get("test_data_dir"));
    }

    public static void main(String[] args) {
        loadValues();
        System.out.println(VALUES);
        System.out.println(ARRAY_VALUES);
        System.out.println(getTestOutDir());
    }
}
