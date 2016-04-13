package sage.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.nio.file.Paths;
import java.util.Objects;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class URIUtil {
    private URIUtil() {
    }

    private static void endHeadersWithPeriod(Document htmlDoc, String headerTag) {
        htmlDoc.getElementsByTag(headerTag)
                .stream().filter(Element::hasText)
                .forEach(e -> {
                    String innerText = e.text();
                    if (!innerText.endsWith(".")) {
                        e.text(innerText + ".");
                    }
                });
    }

    private static String sanitizeDocument(Document htmlDoc) {
        htmlDoc.getElementsByTag("sup").forEach(Node::remove);
        endHeadersWithPeriod(htmlDoc, "h1");
        endHeadersWithPeriod(htmlDoc, "h2");
        endHeadersWithPeriod(htmlDoc, "h3");
        return htmlDoc.text();
    }

    private static String readFile(String file) {
        return Util.read(Values.getTestDirPath().resolve(Paths.get(file)));
    }

    public static String readFromURI(String uri) {
        Objects.requireNonNull(uri);
        System.err.println("Reading URI: " + uri);
        Document document = Jsoup.parse(readFile("potato.html")); //Jsoup.connect(uri).get();
        return sanitizeDocument(document);
    }
}
