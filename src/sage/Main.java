package sage;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.TripletDumper;
import sage.util.Util;
import sage.util.Values;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * TODO: Add named entity recognition (NER)
 * TODO: Anaphora resolution
 * Created by Anurag Gautam on 20-02-2016.
 */
public class Main {
    private static Vocabulary vocab;

    public static void main(String[] args) throws FileNotFoundException {
        Values.loadValues();
        vocab = Vocabulary.getInstance(); // load the vocabulary

        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

        parseFile(tagger, dependencyParser, "test0.txt");

    }

    private static String getText(String path) {
        String text = null;
        if (path.startsWith("http") || path.startsWith("ftp")) {
            try {
                URI uri = new URI(path);
                text = Util.read(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            text = Util.read(Values.getTestDirPath().resolve(Paths.get(path)));
        }
        return text;
    }

    private static void parseFile(MaxentTagger tagger, DependencyParser dependencyParser, String testFilePath) throws FileNotFoundException {
        String text = getText(testFilePath);
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

        PrintWriter pw = new PrintWriter("test0Out.html");
//        pw.println("Sentence,Subject,Predicate,Object");
//        pw.flush();

        TripletDumper tripletDumper = new TripletDumper();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
            GrammaticalStructure gs = dependencyParser.predict(taggedSentence);
            Collection<TypedDependency> dependencies = gs.typedDependencies();
            SentenceTransform transform = new SentenceTransform(dependencies, sentence);

            tripletDumper.add(
                    transform.getTriples()
                            .stream()
                            .filter(t -> t.hasSubject() && t.hasPredicate() && t.hasObject())
                            .filter(VocabFilter.getInstance(vocab))
            );
        }
        pw.println(tripletDumper.getHTML());
        pw.close();
    }
}