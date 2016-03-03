package sage.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class MongoUtil {

    private static final String DATABASE_NAME = "triple_db";
    private static final String COLLECTION_NAME = "triplets";

    private static final MongoClient client = new MongoClient("localhost");

    private MongoUtil() {
    }

    public static MongoClient getClient() {
        return client;
    }

    public static MongoDatabase getDefaultDatabase() {
        return client.getDatabase(DATABASE_NAME);
    }

    public static MongoCollection<Document> getDefaultCollection() {
        return getDefaultDatabase().getCollection(COLLECTION_NAME);
    }
}
