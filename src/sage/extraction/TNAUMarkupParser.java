package sage.extraction;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

/**
 * Created by Anurag Gautam on 13-04-2016.
 */
public class TNAUMarkupParser {

    private final Document markup;
    private final HashMap<String, String> knowledge = new HashMap<>();

    public TNAUMarkupParser(Document markup) {
        this.markup = markup;
    }

    private void parseColumn(Element td) {
        Elements u = td.select("u");
        if (!u.isEmpty()) {
            String topic = u.text();
            u.remove();

            // Get the text
            String description = td.text();

            if (topic != null && topic.length() > 0 && !topic.matches("[ \n\t]+")) {
                knowledge.put(topic, description);
            }
        }
    }

    private void parseRow(Element tr) {
        Elements tds = tr.getElementsByTag("td");
        tds.forEach(this::parseColumn);
    }

    public HashMap<String, String> parse() {

        Elements tables = markup.getElementsByTag("table");
        int numTables = tables.size();

        if (numTables > 0) {
            Element lastTable = tables.get(numTables - 1);
            Elements trs = lastTable.getElementsByTag("tr");
            trs.forEach(this::parseRow);
        }

//        for (int i = 1; i < tables.size(); i++) {
//            Elements trs = tables.get(i).getElementsByTag("tr");
//            trs.forEach(this::parseRow);
//        }
//        Element table3 = tables.get(2);
//        Elements trs = table3.getElementsByTag("tr");
//        trs.forEach(this::parseRow);

//        knowledge.forEach((k, v) -> System.out.println(k + " ==> " + v));
        return knowledge;
    }
}
