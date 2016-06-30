package sage.util;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Dell on 03-06-2016.
 */
public class PDFUtil {
    public static void main(String[] args) throws IOException {
        Values.loadValues();
        PDFTextStripper stripper = new PDFTextStripper();
        File pdfFile = new File("E:\\Books\\b0.pdf");
        PDFParser parser = new PDFParser(new RandomAccessBufferedFileInputStream(pdfFile));
        parser.parse();
        COSDocument doc = parser.getDocument();
        PDDocument pdDocument = new PDDocument(doc);
        stripper.setStartPage(1);
        stripper.setEndPage(pdDocument.getNumberOfPages() - 1);
        String text = stripper.getText(pdDocument);
        Path filePath = Values.getTestDir().resolve("book_pdf.txt");
        FileOutputStream f = new FileOutputStream(filePath.toFile());
        f.write(text.getBytes());
        f.close();
    }
}
