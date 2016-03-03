package sage.test.web;

import java.net.URI;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class WebApp implements Runnable {

    private void load(String uriParam) {
        if (uriParam == null) return;
        URI uri = new URI()
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
