package sage.util;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

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

    public static String readFromURI(String uri) throws IOException {
        Objects.requireNonNull(uri);
        System.err.println("Reading URI: " + uri);
        Document document = Jsoup.connect(uri).get();
        return sanitizeDocument(document);
    }

    private static String checkInCache(String key) {
        MongoCollection<org.bson.Document> urlCacheCollection = MongoUtil.getUrlCacheCollection();
        FindIterable<org.bson.Document> results = urlCacheCollection.find(eq("url-hash", key));
        if (results.first() != null) {
            org.bson.Document firstDocument = results.first();
            if (firstDocument.containsKey("markup")) {
                return (String) firstDocument.get("markup");
            }
        }
        return null;
    }

    private static void addToCache(String key, String markup) {
        MongoCollection<org.bson.Document> urlCacheCollection = MongoUtil.getUrlCacheCollection();
        org.bson.Document document = new org.bson.Document()
                .append("url-hash", key)
                .append("markup", markup);
        urlCacheCollection.insertOne(document);
    }

    public static String readURI(URI uri) throws IOException {
        Objects.requireNonNull(uri, "uri cannot be null");
        System.out.println("Loading URI: " + uri);
        String urlHash = CryptoUtil.sha1(uri.toString());
        String cacheValue = checkInCache(urlHash);
        if (cacheValue != null) {
            System.out.println("URL: " + uri + " found in cache with key: " + urlHash);
            return cacheValue;
        }
        URLConnection connection = uri.toURL().openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String input = null;
        while ((input = br.readLine()) != null) {
            response.append(input);
        }
        String markup = response.toString();
        addToCache(urlHash, markup);
        return markup;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        String val = readURI(new URI("http://agritech.tnau.ac.in/horticulture/horti_vegetables_tomato_varieties.html"));
        System.out.println(val.length());
        System.out.println(CryptoUtil.sha256(val));
    }
}
