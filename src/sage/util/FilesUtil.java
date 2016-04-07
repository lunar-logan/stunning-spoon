package sage.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Anurag Gautam on 07-04-2016.
 */
public class FilesUtil {
    private FilesUtil() {
    }

    public static String getFilenameWithoutExtension(String path) {
        try {
            Path fileName = Paths.get(new URI(path).getPath()).getFileName();
            if (fileName != null) {
                String name = fileName.toString();
                int dotIndex = name.indexOf('.');
                if (dotIndex >= 0) {
                    return name.substring(0, dotIndex);
                } else {
                    return name;
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void main(String[] args) {
        System.out.println(getFilenameWithoutExtension("/hell/fo/bar/test.html"));
        System.out.println(getFilenameWithoutExtension("/hell/fo/bar/.gitignore"));
        System.out.println(getFilenameWithoutExtension("io.gitignore"));
        System.out.println(getFilenameWithoutExtension("http://git.io.gitignore/Procfile"));
    }
}
