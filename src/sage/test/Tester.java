package sage.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import edu.stanford.nlp.ling.Sentence;
import org.bson.Document;
import sage.spi.Triplet;
import sage.util.CryptoUtil;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class Tester {

    private final MongoClient client;
    private static final Tester instance = new Tester();

    private static final String DATABASE_NAME = "triple_db";
    private static final String COLLECTION_NAME = "triplets";

    private int totalTested = 0;
    private int correct = 0;

    public static Tester getInstance() {
        return instance;
    }

    private Tester() {
        client = new MongoClient();
    }

    private String normalize(String text) {
        return text.toLowerCase().replaceAll("\\w+", "").trim();
    }

    public boolean insert(String origSentence, Triplet t) {
        String hash = CryptoUtil.md5(normalize(origSentence));
        MongoDatabase tripletDB = client.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = tripletDB.getCollection(COLLECTION_NAME);
        Document doc = new Document(hash,
                new Document()
                        .append("sub", Sentence.listToString(t.getSubject()))
                        .append("pre", Sentence.listToString(t.getPredicate()))
                        .append("obj", Sentence.listToString(t.getObject())));
        collection.insertOne(doc);
        return true;
    }

    public void test(String origSentence, Triplet t) {
        String hash = CryptoUtil.md5(normalize(origSentence));
        MongoDatabase tripletDB = client.getDatabase(DATABASE_NAME);
        MongoCollection<Document> collection = tripletDB.getCollection(COLLECTION_NAME);
        Document doc = collection.find(Filters.eq("hash", hash)).first();
        if (doc != null) {
            String sub = Sentence.listToString(t.getSubject());
            String pre = Sentence.listToString(t.getPredicate());
            String obj = Sentence.listToString(t.getObject());

            String subFound = (String) doc.get("sub");
            String preFound = (String) doc.get("pre");
            String objFound = (String) doc.get("obj");

            if ((sub.equalsIgnoreCase(subFound) || sub.contains(subFound))
                    && (pre.equalsIgnoreCase(preFound) || pre.contains(preFound))
                    && (obj.equalsIgnoreCase(objFound) || obj.contains(objFound))) {
                correct++;
            }
        }
        totalTested++;
    }

    public int getCorrect() {
        return correct;
    }

    public int getTotalTested() {
        return totalTested;
    }

    @Override
    public String toString() {
        return String.format("Score: %s/%d\nAccuracy: %.04f\n", correct, totalTested, ((correct / (float) totalTested) * 100.0));
    }
}
