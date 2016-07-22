package sage.ui;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Paths;

/**
 * Created by Dell on 20-07-2016.
 */
public class EUIUtil {

    private EUIUtil() {
    }

    public static void fetchRemote(String url) {
        try {
            URI uri = new URI(url);
            URLConnection conn = uri.toURL().openConnection();

        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    public static String fetchLocal(String path) {
        StringBuilder out = new StringBuilder();
        File file = new File(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return out.toString();
    }

    public static void main(String[] args) throws URISyntaxException {
        URI uri = Paths.get(System.getProperty("user.dir"), "downloads").toUri();
        System.out.println(uri);

    }
}
