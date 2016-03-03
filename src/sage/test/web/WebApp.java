package sage.test.web;

import com.mongodb.client.MongoCollection;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.bson.Document;
import sage.util.CryptoUtil;
import sage.util.MongoUtil;
import sage.util.URIUtil;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class WebApp implements Runnable {

    private static final java.util.logging.Logger L = Logger.getLogger(WebApp.class.getName());

    static {
        L.setUseParentHandlers(false);
        L.addHandler(new StreamHandler() {
            @Override
            public void publish(LogRecord logRecord) {
                System.out.printf("%s/%s#%s: %s\n",
                        logRecord.getLevel().getName(),
                        logRecord.getSourceClassName(),
                        logRecord.getSourceMethodName(),
                        logRecord.getMessage());
            }
        });
    }

    private boolean load(String uriParam) {
        if (uriParam == null) {
            L.severe("uri is null");
            return false;
        }
        String text = URIUtil.readFromURI(uriParam);
        if (text == null) {
            L.warning("Could not read from the uri " + uriParam);
            return false;
        } else {
            storeInDB(uriParam, text);
        }
        return true;
    }

    private void storeInDB(String uri, String text) {
        MongoCollection<Document> collection = MongoUtil.getDocumentCollection();
        String uriHash = CryptoUtil.md5(uri);

        ArrayList<List<HasWord>> document = new ArrayList<>();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        tokenizer.forEach(document::add);

        Document doc = new Document();
        doc.append("hash", uriHash).append("doc", document);
        collection.insertOne(doc);
        L.info("Document has been inserted into the collection. URI-hash: " + uriHash);
    }

    private String makeResponse(CharSequence message, int code, Object payload) {
        Document res = new Document();
        res.append("code", code).append("message", message);
        if (payload != null) {
            res.append("result", payload);
        }
        return res.toJson();
    }

    private String success(String message) {
        return makeResponse(message, 200, null);
    }

    private String failure(String message) {
        return makeResponse(message, 500, null);
    }

    private void setupRoutes() {
        get("/", (req, res) -> "This is the index route!");

        post("/load", (request, response) -> {
            response.type("application/json");
            String uriString = request.params("uri");
            if (load(uriString)) {
                return success("Successfully loaded");
            }
            return failure("Could not read the URI");
        });
    }

    @Override
    public void run() {
        setupRoutes();
    }

    public static void main(String[] args) {
        new WebApp().run();
    }
}
