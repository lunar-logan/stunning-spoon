package sage;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.Util;
import sage.util.Values;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anurag Gautam on 20-02-2016.
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Values.loadValues();
        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

        parseFile(tagger, dependencyParser, Values.getTestDirPath().resolve(Paths.get("test4.txt")));

    }

    private static void parseFile(MaxentTagger tagger, DependencyParser dependencyParser, Path testFilePath) throws FileNotFoundException {
        String text = Util.read(testFilePath);

        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

        PrintWriter pw = new PrintWriter("test.out.tsv");
        pw.println("Sentence\tSubject\tPredicate\tObject");
        pw.flush();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
            GrammaticalStructure gs = dependencyParser.predict(taggedSentence);
            Collection<TypedDependency> dependencies = gs.typedDependencies();
            Algorithm algo = new Algorithm(dependencies);
            algo.getTriples().forEach(triple -> {
                String tripleDump = String.format("%s\t%s\t%s\t%s",
                        Sentence.listToString(sentence),
                        triple.first,
                        triple.second,
                        triple.third
                );
                pw.println(tripleDump);
                pw.flush();
            });
        }
        pw.close();
    }
}