package sage.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import org.apache.jena.rdf.model.*;
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
    private static final String NAMESPACE = "s:";

    public static String listToURIString(List<? extends HasWord> list) {
        StringJoiner joiner = new StringJoiner("_");
        list.forEach(e -> joiner.add(e.word()));
        return joiner.toString();
    }

    private static String asURI(List<IndexedWord> val) throws UnsupportedEncodingException, URISyntaxException {
        URI uri = new URI(String.format("%s%s", NAMESPACE, URLEncoder.encode(listToURIString(val), "UTF-8")));
        return uri.toString();
    }

    private static String asURI(String val) throws URISyntaxException, UnsupportedEncodingException {
        URI uri = new URI("p://u/" + URLEncoder.encode(val, "UTF-8"));
        return uri.toString();
    }

    public static String normalizePredicate(List<? extends HasWord> list) {
        StringBuilder out = new StringBuilder();
        if (list.size() > 0) {
            HasWord first = list.get(0);
            out.append(first.word().toLowerCase());
            for (int i = 1; i < list.size(); i++) {
                char[] word = list.get(i).word().toLowerCase().toCharArray();
                if (word.length > 0) {
                    word[0] = Character.toUpperCase(word[0]);
                    out.append(new String(word));
                }

            }
        }
        return out.toString();
    }


    public static void dumpAsRDF(List<Triplet> triplets, String dumpFileName) throws IOException {
        final Model agroModel = ModelFactory.createDefaultModel();
        triplets.forEach(triple -> {
            if (triple.hasSubject() && triple.hasPredicate() && triple.hasObject())
                try {
                    String subject = asURI(triple.getSubject());
                    String predicate = asURI(normalizePredicate(triple.getPredicate()));
                    String object = listToURIString(triple.getObject());

                    Resource newResource = agroModel.createResource(subject);
                    Property property = agroModel.createProperty(predicate);
                    Literal objectLiteral = agroModel.createLiteral(object);
                    newResource.addProperty(property, objectLiteral);

                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
        });
        FileOutputStream fout = new FileOutputStream(Values.getTestDirPath().resolve(Paths.get(dumpFileName)).toFile());
        agroModel.write(fout);
        fout.close();
    }
}