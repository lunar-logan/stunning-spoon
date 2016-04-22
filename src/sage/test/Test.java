package sage.test;

import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import sage.Vocabulary;
import sage.extraction.TNAUShuruaat;
import sage.util.Values;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Created by Anurag Gautam on 08-03-2016.
 */
public class Test {
    static final Logger L = Logger.getGlobal();


    public static void main(String[] args) throws URISyntaxException, IOException {
        Values.loadValues();
        Vocabulary instance = Vocabulary.getInstance();

        System.out.println(instance.contains("yield"));
        MaxentTagger tagger = new MaxentTagger(Values.getTaggerModelPath().toString());
        DependencyParser dependencyParser = DependencyParser.loadFromModelFile(Values.getParserModelPath().toString());

        TNAUShuruaat shuruaat = new TNAUShuruaat(tagger, dependencyParser, instance, new FileInputStream(Values.getTestDir().resolve("in0.txt").toFile()), "tomato");
        shuruaat.start();
//        String markup = URIUtil.readURI(new URI("http://agritech.tnau.ac.in/horticulture/horti_vegetables_bhendi_Varieties.html"));
//        org.jsoup.nodes.Document doc = Jsoup.parse(markup);
//        new TNAUMarkupParser(doc).parse();
    }
}
