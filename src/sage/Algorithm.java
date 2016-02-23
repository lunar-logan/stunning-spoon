package sage;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.Triple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by Anurag Gautam on 24-02-2016.
 */
public class Algorithm {
    private final Collection<TypedDependency> dependencies;
    private ArrayList<Triple<IndexedWord, IndexedWord, IndexedWord>> triples = new ArrayList<>();

    public Algorithm(Collection<TypedDependency> dependencies) {
        Objects.requireNonNull(dependencies);
        this.dependencies = dependencies;
        System.out.println(dependencies);
        process();
    }

    public void process() {
        // Look for nsubj with gov as verbs
        for (TypedDependency td : dependencies) {
            String reln = td.reln().getShortName();
            if (reln.equalsIgnoreCase("nsubj")) {
                handleNsubj(td);
            } else if (reln.equalsIgnoreCase("nsubjpass")) {

            }
        }
    }

    private void handleNsubj(TypedDependency dependency) {
        if (dependency.gov().tag().startsWith("VB")) {
            IndexedWord obj = findObject(dependency.gov());
            if (obj != null) {
                triples.add(new Triple<>(dependency.dep(), dependency.gov(), obj));
            }
        } else { // look for copular relation
            for (TypedDependency td : dependencies) {
                String shortName = td.reln().getShortName();
                if (td.gov().equals(dependency.gov())) {
                    if (shortName.equalsIgnoreCase("cop")) {
                        triples.add(
                                new Triple<>(
                                        dependency.dep(),
                                        td.dep(),
                                        td.gov()
                                )
                        );
                        handleConj(dependency.dep(), dependency.gov());
                        break;
                    }
                }
            }
        }
    }

    private IndexedWord findObject(IndexedWord predicate) {
        for (TypedDependency td : dependencies) {
            String reln = td.reln().getShortName();
            if (td.gov().equals(predicate)) {
                if (reln.equalsIgnoreCase("dobj") || reln.equalsIgnoreCase("iobj") || reln.equalsIgnoreCase("ccomp")) {
                    return td.dep();
                }
            }
        }
        return null;
    }

    private void handleConj(IndexedWord subj, IndexedWord conjunct) {
        for (TypedDependency td : dependencies) {
            String shortName = td.reln().getShortName();
            if (td.gov().equals(conjunct)) {
                if (shortName.equalsIgnoreCase("conj")) {
                    IndexedWord pred = td.dep();
                    IndexedWord obj = findObject(pred);
                    if (obj != null) {
                        triples.add(
                                new Triple<>(
                                        subj,
                                        pred,
                                        obj
                                )
                        );
                    }
                }
            }
        }
    }

    public List<Triple<IndexedWord, IndexedWord, IndexedWord>> getTriples() {
        return triples;
    }
}
