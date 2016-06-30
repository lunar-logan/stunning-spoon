package sage.service;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import sage.extraction.Framework;
import sage.util.Util;
import sage.util.Values;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
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

            String vocabPath = null;
            List<String> docsPath = new ArrayList<>();

            for (FileItem item : fileItemList) {
                String storagePath = uploadDir.toPath().resolve(item.getName()).toString();
                System.out.println("Storage Path: " + storagePath);
                if (item.getFieldName().equals("vocab_file")) {
                    vocabPath = storagePath;
                } else {
                    docsPath.add(storagePath);
                    try {
                        item.write(new File(storagePath));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            StringBuilder res = new StringBuilder();

            if (vocabPath == null) {
                vocabPath = Values.getVocabPath().toString();
            }
            Framework exF = new Framework(vocabPath, docsPath);
            exF.setOnPostComplete((ts) -> {
                res.append(ts.toString());
                return null;
            });
            exF.run();

            return res.toString();
        });

    }
}
