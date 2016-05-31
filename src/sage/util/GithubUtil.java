package sage.util;

import com.jcabi.github.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Base64;

/**
 * Created by Anurag Gautam on 31-05-2016.
 */
public class GithubUtil {

    public static void commitNew(JsonArray triples, String message) {
        Github hub = new RtGithub(Values.get("github_username", ""), Values.get("github_password", ""));
        Repo tripleRepo = hub.repos().get(new Coordinates.Simple("Agromax", "triples"));

        JsonObject spoTriples = Json.createObjectBuilder()
                .add("desc", message)
                .add("ts", System.currentTimeMillis())
                .add("triplets", triples).build();

        JsonObjectBuilder content = Json.createObjectBuilder()
                .add("path", "triples.json")
                .add("message", message)
                .add("content", Base64.getEncoder().encodeToString(spoTriples.toString().getBytes()));

        try {
            if (tripleRepo.contents().exists("triples.json", "master")) {
                Content oldFile = tripleRepo.contents().get("triples.json");
                content.add("sha", oldFile.json().get("sha"));
                tripleRepo.contents().update("triples.json", content.build());
            } else {
                tripleRepo.contents().create(content.build());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
