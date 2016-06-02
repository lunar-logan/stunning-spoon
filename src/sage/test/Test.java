package sage.test;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.Vocabulary;
import sage.extraction.ExtractionFramework;
import sage.extraction.TNAUShuruaat;
import sage.spi.Triplet;
import sage.util.Values;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Anurag Gautam on 08-03-2016.
 */
public class Test {
    static final Logger L = Logger.getGlobal();
    private static final List<Triplet> triplets = new ArrayList<>();

    private static void genTriples(MaxentTagger tagger, DependencyParser dependencyParser, String topic, String description, Vocabulary vocabulary) {
        DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(description));
        for (List<HasWord> sentence : preprocessor) {
            List<TaggedWord> taggedWords = tagger.tagSentence(sentence);
            GrammaticalStructure grammaticalStructure = dependencyParser.predict(taggedWords);
            Collection<TypedDependency> typedDependencies = grammaticalStructure.typedDependencies();

            System.out.println("Sen: " + taggedWords);
            typedDependencies.forEach(System.out::println);
            System.out.println();

            ExtractionFramework transform = new ExtractionFramework(typedDependencies, vocabulary);
            transform.getTriples().forEach(triplets::add);
        }
    }


    public static void main(String[] args) throws URISyntaxException, IOException {
        Values.loadValues();
        Vocabulary instance = Vocabulary.getInstance();

        System.out.println(instance.contains("yield"));
        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

       /* String data = Util.read(Values.getTestDir().resolve("in1.txt"));
        genTriples(tagger, dependencyParser, "topic", data, instance);


        triplets.forEach(t -> System.out.println(t.getAsJsonObject()));
        RDFUtil.dumpAsRDF(triplets, "ef-res.xml");
*/
        TNAUShuruaat shuruaat = new TNAUShuruaat(tagger, dependencyParser, instance, new FileInputStream(Values.getTestDir().resolve("in0.txt").toFile()), "tomato");
        shuruaat.start();
//        String markup = URIUtil.readURI(new URI("http://agritech.tnau.ac.in/horticulture/horti_vegetables_bhendi_Varieties.html"));
//        org.jsoup.nodes.Document doc = Jsoup.parse(markup);
//        new TNAUMarkupParser(doc).parse();
    }
}
