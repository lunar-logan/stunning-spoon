package sage.extraction;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sage.Vocabulary;
import sage.spi.Triplet;
import sage.util.RDFUtil;
import sage.util.Util;
import sage.util.Values;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Anurag Gautam on 13-04-2016.
 */
public class TNAUShuruaat {


    private final String concept;
    private final MaxentTagger tagger;
    private final DependencyParser dependencyParser;
    private final Vocabulary vocabulary;
    private final List<Triplet> triplets = new ArrayList<>();
    private final InputStream in;


    public TNAUShuruaat(MaxentTagger tagger, DependencyParser dependencyParser, Vocabulary vocabulary, InputStream in, String baseConcept) {
        this.in = in;
        this.concept = baseConcept;
        this.tagger = tagger;
        this.dependencyParser = dependencyParser;
        this.vocabulary = vocabulary;
    }

    private void genTriples(String topic, String description) {
        DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(description));
        for (List<HasWord> sentence : preprocessor) {
            List<TaggedWord> taggedWords = tagger.tagSentence(sentence);
            GrammaticalStructure grammaticalStructure = dependencyParser.predict(taggedWords);
            Collection<TypedDependency> typedDependencies = grammaticalStructure.typedDependencies();
            Xtract transform = new Xtract(typedDependencies, vocabulary);
            transform.getTriples().forEach(triplets::add);
        }
    }

    public void start() {
        System.out.println("TNAU se data extraction ki shuruaat ho chuki hai");
        try {
            // Read the data from the input stream
            String text = Util.readFromStream(in).toLowerCase();
//            TNAUMarkupParser tnauMarkupParser = new TNAUMarkupParser(Jsoup.parse(markup));
//            HashMap<String, String> knowledge = tnauMarkupParser.parse();
//            knowledge.forEach((topic, desc) -> genTriples(topic, desc));
            genTriples(concept, text);
            System.out.println(triplets.size() + " triplets generate hue hai");
            RDFUtil.dumpAsRDF(triplets, Values.getTestOutDir().resolve("in1.out.xml").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
