package sage.extraction;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.Vocabulary;
import sage.util.SPOTriplet;
import sage.util.Util;
import sage.util.Values;

import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

/**
 * @author Anurag Gautam
 */
public class Framework implements Runnable {
    private final List<String> docFiles;
    private HashSet<String> vocabulary = new HashSet<>();
    private Function<HashMap<String, ArrayList<SPOTriplet>>, Void> callback;

    public Framework(String vocabPath, List<String> docFiles) {
        Values.loadValues();

        loadVocab(vocabPath);
        this.docFiles = docFiles;
    }

    private String sanitize(String t) {
        return t.replaceAll("[ \t]+", " ").trim().toLowerCase();
    }

    private void loadVocab(String path) {
        Objects.requireNonNull(path);

        String vocabText = Util.read(Paths.get(path));
        String[] tokens = vocabText.split("[\n\r]+");

        for (String token : tokens) {
            String t = sanitize(token);
            if (!t.isEmpty()) {
                vocabulary.add(t);
            }
        }
    }

    @Override
    public void run() {
        HashMap<String, ArrayList<SPOTriplet>> triplesMap = new HashMap<>();

        if (docFiles != null) {
            for (String doc : docFiles) {
                triplesMap.put(doc, handleDocument(doc));
            }
        }

        if (callback != null) {
            callback.apply(triplesMap);
        }
    }

    public void setOnPostComplete(Function<HashMap<String, ArrayList<SPOTriplet>>, Void> callback) {
        this.callback = callback;
    }

    /**
     * Path is assumed to be local
     *
     * @param path
     */
    private ArrayList<SPOTriplet> handleDocument(String path) {
        // Step 1 : Read the document
        String doc = Util.read(Paths.get(path));

        // Step 2: Initialize the tagger and the parser
        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

        // Step 3: Generate the triplets
        return genTriples(tagger, dependencyParser, doc, null);
    }

    private static ArrayList<SPOTriplet> genTriples(
            MaxentTagger tagger,
            DependencyParser dependencyParser,
            String description,
            Vocabulary vocabulary) {

        ArrayList<SPOTriplet> triplets = new ArrayList<>();
        DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(description));
        for (List<HasWord> sentence : preprocessor) {
            List<TaggedWord> taggedWords = tagger.tagSentence(sentence);
            GrammaticalStructure grammaticalStructure = dependencyParser.predict(taggedWords);
            Collection<TypedDependency> typedDependencies = grammaticalStructure.typedDependencies();

            ExtractionFramework transform = new ExtractionFramework(typedDependencies, vocabulary);
            triplets.addAll(transform.getTriples());
        }
        return triplets;
    }

}
