package sage.test;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import sage.spi.Triplet;

import java.util.List;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class Tester {

    private final MongoClient client;
    private static final Tester instance = new Tester();

    private static final String DATABASE_NAME = "triple_db";
    private static final String COLLECTION_NAME = "triplets";

    public static Tester getInstance() {
        return instance;
    }

    private Tester() {
        client = new MongoClient();
    }

    private String sha1(String text) {

    }

    public void add(Triplet t) {
        MongoDatabase tripletDB = client.getDatabase(DATABASE_NAME);

    }

    public void add(List<Triplet> tripletList) {

    }
}
