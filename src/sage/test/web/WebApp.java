package sage.test.web;

import sage.util.URIUtil;

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

    private void load(String uriParam) {
        if (uriParam == null) {
            L.severe("uri is null");
            return;
        }
        String text = URIUtil.readFromURI(uriParam);
        if (text == null) {
            L.warning("Could not read from the uri " + uriParam);
        } else {
            storeInDB(text);
        }
    }

    private void storeInDB(String text) {
        

    }

    private void setupRoutes() {
        get("/", (req, res) -> "This is the index route!");

        post("/load", (request, response) -> {
            String uriString = request.params("uri");
            if (uriString != null) {

            }
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
