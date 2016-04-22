package sage.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class MongoUtil {

    public static final String DATABASE_NAME = "triple_store";
    public static final String CACHE_COLLECTION_NAME = "url_cache";
    public static final String TRIPLETS_COLLECTION_NAME = "triplets";
    public static final String DOCUMENT_COLLECTION_NAME = "documents";

    private static final MongoClient client = new MongoClient();

    private MongoUtil() {
    }

    public static MongoClient getClient() {
        return client;
    }

    public static MongoDatabase getDefaultDatabase() {
        return getClient().getDatabase(DATABASE_NAME);
    }

    public static MongoCollection<Document> getTripletsCollection() {
        return getDefaultDatabase().getCollection(TRIPLETS_COLLECTION_NAME);
    }

    public static MongoCollection<Document> getDocumentCollection() {
        return getDefaultDatabase().getCollection(DOCUMENT_COLLECTION_NAME);
    }

    public static MongoCollection<Document> getUrlCacheCollection() {
        return getDefaultDatabase().getCollection(CACHE_COLLECTION_NAME);
    }
}
