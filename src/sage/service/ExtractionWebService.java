package sage.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import sage.util.Util;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by Dell on 30-06-2016.
 */
public class ExtractionWebService {

    public static void main(String[] args) {

        // setup static files location
        staticFileLocation(ServiceConstants.getStaticFilesDir());


        get("/", (request, response) -> {
            response.type("text/html");
            return Util.read(ServiceConstants.resolve("www/service.html"));
        });

        post("/extract", (request, response) -> {
            final String uploadPath = Paths.get(System.getProperty("user.dir")).resolve("upload").toString();
            final File uploadDir = new File(uploadPath);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new RuntimeException("Failed to create the directory " + uploadDir.getAbsolutePath());
            }

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(uploadDir);
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            List<FileItem> fileItemList = fileUpload.parseRequest(request.raw());

            fileItemList.stream()
                    .filter(item -> item.getFieldName().equals("doc_file") || item.getFieldName().equals("vocab_file"))
                    .forEach(item -> {
                        String fileName = item.getName();
                        try {
                            item.write(new File(uploadDir, fileName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            return "Check the upload directory";
        });

    }
}
