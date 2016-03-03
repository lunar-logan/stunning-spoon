package sage.util;

import com.github.jsonldjava.core.RDFDataset;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import sage.spi.Triplet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

/**
 * This is a temporary class, to be removed when the integration completes
 * Created by Anurag Gautam on 25-10-2015.
 */
public class RDFUtil {
    private static final String NAMESPACE = ":";

    public static String listToURIString(List<? extends HasWord> list) {
        StringJoiner joiner = new StringJoiner("_");
        list.forEach(e -> joiner.add(e.word()));
        return joiner.toString();
    }

    private static String asURI(List<IndexedWord> val) throws UnsupportedEncodingException, URISyntaxException {
        URI uri = new URI(String.format("%s%s", NAMESPACE, URLEncoder.encode(listToURIString(val), "UTF-8")));
        return uri.toString();
    }

    public static void dumpAsRDF(List<Triplet> triplets) throws IOException {
        final Model agroModel = ModelFactory.createDefaultModel();
        triplets.forEach(triple -> {
            try {
                String subject = asURI(triple.getSubject());
                String predicate = asURI(triple.getPredicate());
                String object = asURI(triple.getObject());



                Resource newResource = agroModel.createResource(subject);
                Property property = agroModel.createProperty(predicate);
                newResource.addProperty(property, object);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        FileOutputStream fout = new FileOutputStream(Values.getTestDirPath().resolve(Paths.get("rdfDump.xml")).toFile());
        agroModel.write(fout);
        fout.close();
    }
}