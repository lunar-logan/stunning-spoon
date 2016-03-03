package sage.test.web;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.bson.Document;
import sage.test.Tester;
import sage.util.CryptoUtil;
import sage.util.MongoUtil;
import sage.util.URIUtil;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
            L.info("Document read from the URI " + uriParam);
            storeInDB(uriParam, text.toLowerCase());
        }
        return true;
    }

    private void storeInDB(String uri, String text) {
        MongoCollection<Document> collection = MongoUtil.getDocumentCollection();
        String uriHash = CryptoUtil.md5(uri);

        ArrayList<String> document = new ArrayList<>();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        tokenizer.forEach(sentence -> document.add(Sentence.listToString(sentence)));
        Document doc = new Document();
        doc.append("hash", uriHash).append("doc", document);
        try {
            collection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        L.info("Document has been inserted into the collection. URI-hash: " + uriHash);
    }

    private String makeResponse(CharSequence message, int code, Object payload) {
        Document res = new Document();
        res.append("code", code).append("message", message);
        if (payload != null) {
            res.append("payload", payload);
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
            String uriString = request.queryParams("uri");
            if (load(uriString)) {
                return success("Successfully loaded");
            }
            return failure("Could not read the URI");
        });

        get("/sentence", (request, response) -> {
            response.type("application/json");
            String uri = request.queryParams("uri");
            String index = request.queryParams("index");
            String sentence = getSentence(uri, index);
            if (sentence != null) {
                return makeResponse("Success", 200, new Document("sentence", sentence));
            }
            return failure("Could not fetch the sentence");
        });

        get("/sentences", (request, response) -> {
            response.type("application/json");
            String uri = request.queryParams("uri");
            String start = request.queryParams("start");
            String limit = request.queryParams("limit");
            Document resObj = getSentences(uri, start, limit);
            if (resObj != null) {
                return makeResponse("Success", 200, resObj);
            }
            return failure("Could not fetch the sentence");
        });

        post("/add", (request, response) -> {
            response.type("application/json");
            String spoJson = request.queryParams("spo");
            if (store(spoJson)) {
                return success("Stored in the triplet store");
            }
            return failure("Could not store into the database");
        });

    }

    private boolean store(String spoJson) {
        if (spoJson == null) return false;
        Document spo = Document.parse(spoJson);
        return spo != null && Tester.getInstance()
                .insert((String) spo.get("sen"),
                        (String) spo.get("sub"),
                        (String) spo.get("pre"),
                        (String) spo.get("obj"));
    }

    private Document getSentences(String uri, String start, String limit) {
        if (uri == null || start == null) return null;
        int s = 0, l = 10;
        try {
            s = Integer.parseInt(start);
            if (limit != null) l = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        MongoCollection<Document> documentCollection = MongoUtil.getDocumentCollection();
        Document doc = documentCollection.find(Filters.eq("hash", CryptoUtil.md5(uri))).first();
        if (doc != null) {
            ArrayList<String> sentences = (ArrayList<String>) doc.get("doc");
            if (sentences != null && s >= 0 && l >= 1 && sentences.size() >= (s + l)) {
                Document res = new Document("sentences", sentences.subList(s, s + l));
                try {
                    res.append("next", "http://localhost:4567/sentences?uri=" + URLEncoder.encode(uri, "UTF-8") + "&start=" + (s + l) + "&limit=10");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
                return res;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getSentence(String uri, String index) {
        if (uri == null || index == null) return null;
        int i = -1;
        try {
            i = Integer.parseInt(index);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        MongoCollection<Document> documentCollection = MongoUtil.getDocumentCollection();
        Document doc = documentCollection.find(Filters.eq("hash", CryptoUtil.md5(uri))).first();
        if (doc != null) {
            ArrayList<String> sentences = (ArrayList<String>) doc.get("doc");
            if (sentences != null && sentences.size() > i && i >= 0) {
                return sentences.get(i);
            }
        }
        return null;
    }

    @Override
    public void run() {
        setupRoutes();
    }

    @Override
    protected void finalize() throws Throwable {
        MongoUtil.getClient().close();
        super.finalize();
    }

    public static void main(String[] args) {
        new WebApp().run();
    }
}
