package sage;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.extraction.SentenceTransform;
import sage.spi.Triplet;
import sage.util.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Add named entity recognition (NER)
 * TODO: Anaphora resolution
 * Created by Anurag Gautam on 20-02-2016.
 */
public class Main {
    private static Vocabulary vocab;

    public static void main(String[] args) throws IOException {
        Values.loadValues();
        vocab = Vocabulary.getInstance(); // load the vocabulary

        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

//        parseFile(tagger, dependencyParser, "https://en.wikipedia.org/wiki/Rice");
//        parseFileAndRDFDump(tagger, dependencyParser, "https://en.wikipedia.org/wiki/Tomato");
        parseFileAndRDFDump(tagger, dependencyParser, "test3.txt");
//        parseFileAndTSVDump(tagger, dependencyParser, "https://en.wikipedia.org/wiki/Tomato");

    }

    private static String getText(String path) {
        String text = null;
        if (path.startsWith("http") || path.startsWith("ftp")) {
            text = URIUtil.readFromURI(path);
        } else {
            text = Util.read(Values.getTestDirPath().resolve(Paths.get(path)));
        }
        return text;
    }

    private static void parseFile(MaxentTagger tagger, DependencyParser dependencyParser, String testFilePath) throws IOException {
        String text = getText(testFilePath);
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

        PrintWriter pw = new PrintWriter("wheatOut.html");

        TripletDumper tripletDumper = new TripletDumper();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
            GrammaticalStructure gs = dependencyParser.predict(taggedSentence);
            Collection<TypedDependency> dependencies = gs.typedDependencies();
            SentenceTransform transform = new SentenceTransform(dependencies, sentence, vocab);

            tripletDumper.add(
                    transform.getTriples()
                            .stream()
                            .filter(t -> t.hasSubject() && t.hasPredicate() && t.hasObject())
                            .filter(VocabFilter.getInstance(vocab))
//                    .forEach(triplets::add);
            );
        }
        pw.println(tripletDumper.getHTML());
        pw.close();
    }

    private static void parseFileAndRDFDump(MaxentTagger tagger, DependencyParser dependencyParser, String testFilePath) throws IOException {
        String text = getText(testFilePath).toLowerCase();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

        ArrayList<Triplet> triplets = new ArrayList<>();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
            GrammaticalStructure gs = dependencyParser.predict(taggedSentence);
            Collection<TypedDependency> dependencies = gs.typedDependencies();
            SentenceTransform transform = new SentenceTransform(dependencies, sentence, vocab);

            transform.getTriples()
                    .stream()
                    .filter(t -> t.hasSubject() && t.hasPredicate() && t.hasObject())
//                    .filter(VocabFilter.getInstance(vocab))
                    .forEach(triplets::add);
        }
        RDFUtil.dumpAsRDF(triplets, FilesUtil.getFilenameWithoutExtension(testFilePath) + ".xml");
    }

    private static String dumpAsTSV(List<Triplet> triplets) {
        StringBuilder sbr = new StringBuilder();
        triplets.forEach(triple -> {
            sbr.append(triple.getAsTSV()).append("\n");
        });
        return sbr.toString();
    }


    private static void parseFileAndTSVDump(MaxentTagger tagger, DependencyParser dependencyParser, String testFilePath) throws IOException {
        String docText = getText(testFilePath);
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(docText));
        ArrayList<Triplet> triplets = new ArrayList<>();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedWordList = tagger.tagSentence(sentence);
            GrammaticalStructure gs = dependencyParser.predict(taggedWordList);
            Collection<TypedDependency> dependencies = gs.typedDependencies();
            SentenceTransform transform = new SentenceTransform(dependencies, sentence, vocab);
            transform.getTriples()
                    .stream()
                    .filter(t -> t.hasSubject() && t.hasPredicate() && t.hasObject())
                    .filter(VocabFilter.getInstance(vocab))
                    .forEach(triplets::add);
        }
        Collections.sort(triplets, (o1, o2) ->
                Sentence.listToString(o1.getSubject()).compareToIgnoreCase(Sentence.listToString(o2.getSubject())));
        FileOutputStream fout = new FileOutputStream(
                Paths.get(Values.getTestDirPath().toString(),
                        FilesUtil.getFilenameWithoutExtension(testFilePath) + ".tsv").toFile());
        fout.write(dumpAsTSV(triplets).getBytes());
        fout.close();
    }


}