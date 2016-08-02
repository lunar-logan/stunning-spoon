package sage.test;

import sage.util.PDFUtil;
import sage.util.Values;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Created by Dell on 03-06-2016.
 */
public class K {
    public static void main(String[] args) throws URISyntaxException, IOException {
        Values.loadValues();
        System.out.println(Values.getVocabPath());
        String text = PDFUtil.getText(Paths.get("E:\\Thesis Data\\presentation.pdf"));
        System.out.println(text);

    }
}
