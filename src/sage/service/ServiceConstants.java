package sage.service;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Dell on 30-06-2016.
 */
public class ServiceConstants {
    public static final String STATIC_FILES_DIR = "www";


    public static Path resolve(String path) {
        return Paths.get(System.getProperty("user.dir")).resolve(path);
    }

    public static String getStaticFilesDir() {
        return resolve(STATIC_FILES_DIR).toString();
    }
}
