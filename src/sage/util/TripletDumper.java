package sage.util;

import edu.stanford.nlp.ling.Sentence;
import j2html.tags.ContainerTag;
import j2html.tags.Tag;
import sage.spi.Triplet;

import java.util.ArrayList;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

/**
 *
 * Created by Anurag Gautam on 25-02-2016.
 */
public class TripletDumper {
    private final ArrayList<Tag> tripletsDump = new ArrayList<>();

    private ContainerTag getTripletDiv(Triplet triplet) {

        return div().with(
                div()
                        .withClass("sentence")
                        .withText(Sentence.listToString(triplet.getOriginalSentence())),
                div()
                        .withClass("subject")
                        .withText(Sentence.listToString(triplet.getSubject())),
                div()
                        .withClass("predicate")
                        .withText(Sentence.listToString(triplet.getPredicate())),
                div()
                        .withClass("object")
                        .withText(Sentence.listToString(triplet.getObject()))
        ).withClass("triple");
    }

    public void add(Stream<Triplet> tripletStream) {
        tripletStream.forEach(triplet -> {
            tripletsDump.add(getTripletDiv(triplet));
        });
    }

    public String getHTML() {

        return html().with(
                head().with(
                        title("SPO Triples"),
                        link()
                                .withRel("stylesheet")
                                .withHref("sagestyle.css")
                ),
                body().with(
                        tripletsDump
                )
        ).render();
    }
}
