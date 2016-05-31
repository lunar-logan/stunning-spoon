package sage.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import sage.spi.Triplet;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.List;

/**
 * Represents a (Subject, Predicate, Object) triple
 *
 * @author Anurag Gautam
 * @version revision-history: 200216, 250216
 * @since Agromax 1.0
 */
public class SPOTriplet implements Triplet {
    private final List<IndexedWord> sub;
    private final List<IndexedWord> pre;
    private final List<IndexedWord> obj;
    private final List<HasWord> sen;

    public SPOTriplet(List<HasWord> sen, List<IndexedWord> sub, List<IndexedWord> pre, List<IndexedWord> obj) {
        this.sen = sen;
        this.sub = sub;
        this.pre = pre;
        this.obj = obj;
    }

    @Override
    public String getAsCSV() {
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\"",
                Sentence.listToString(sen),
                Sentence.listToString(sub),
                Sentence.listToString(pre),
                Sentence.listToString(obj)
        );
    }

    @Override
    public String getAsTSV() {
        return String.format(
                "%s\t%s\t%s",
                Util.join(sub.stream(), "_"),
                Util.join(pre.stream(), "", (w) -> Character.toUpperCase(w.charAt(0)) + w.substring(1)),
                Util.join(obj.stream(), "_")
        );
    }

    @Override
    public JsonObject getAsJsonObject() {
        return Json.createObjectBuilder()
                .add("sub", Sentence.listToString(sub))
                .add("pre", Sentence.listToString(pre))
                .add("obj", Sentence.listToString(obj)).build();
    }

    @Override
    public String toString() {
        return getAsCSV();
    }

    @Override
    public List<IndexedWord> getSubject() {
        return sub;
    }

    @Override
    public List<IndexedWord> getPredicate() {
        return pre;
    }

    @Override
    public List<IndexedWord> getObject() {
        return obj;
    }

    @Override
    public List<HasWord> getOriginalSentence() {
        return sen;
    }

    @Override
    public boolean hasSubject() {
        return sub != null && !sub.isEmpty();
    }

    @Override
    public boolean hasObject() {
        return obj != null && !obj.isEmpty();
    }

    @Override
    public boolean hasPredicate() {
        return pre != null && !pre.isEmpty();
    }
}