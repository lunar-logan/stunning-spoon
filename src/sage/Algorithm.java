package sage;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.Triple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Anurag Gautam on 24-02-2016.
 */
public class Algorithm {
    private final Collection<TypedDependency> dependencies;
    private ArrayList<Triple<String, String, String>> triples = new ArrayList<>();

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
                handleNominalSubject(td);
            } else if (reln.equalsIgnoreCase("nsubjpass")) {
                handleNominalSubject(td);
            }
        }
    }

    private void handleNominalSubject(TypedDependency dependency) {
        if (dependency.gov().tag().startsWith("VB")) {
            ArrayList<IndexedWord> obj = findObject(dependency.gov());
            if (obj != null && !obj.isEmpty()) {
                triples.add(new Triple<>(
                                dependency.dep().word(),
                                dependency.gov().word(),
                                Sentence.listToString(obj)
                        )
                );
            }
        } else { // look for copular relation
            for (TypedDependency td : dependencies) {
                String shortName = td.reln().getShortName();
                if (td.gov().equals(dependency.gov())) {
                    if (shortName.equalsIgnoreCase("cop")) {
                        triples.add(
                                new Triple<>(
                                        dependency.dep().word(),
                                        td.dep().word(),
                                        td.gov().word()
                                )
                        );
                        handleConjunct(dependency.dep(), dependency.gov());
                        break;
                    }
                }
            }
        }
    }

    private ArrayList<IndexedWord> findObject(IndexedWord predicate) {
        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();

        for (TypedDependency td : dependencies) {
            String reln = td.reln().getShortName();
            if (td.gov().equals(predicate)) {
                if (reln.equalsIgnoreCase("dobj") || reln.equalsIgnoreCase("iobj") || reln.equalsIgnoreCase("ccomp")) {
                    objectPhrase.add(td.dep());
                } else if (reln.equalsIgnoreCase("nmod")) {
                    objectPhrase.add(td.dep());

                    // Look for the case relations
                    for (TypedDependency td1 : dependencies) {
                        String relnName = td1.reln().getShortName();
                        if (td1.gov().equals(td.dep())) {
                            if (relnName.equalsIgnoreCase("case")) {
                                objectPhrase.add(td1.dep());
                            }
                        }
                    }
                }
            }
        }
        Collections.sort(objectPhrase);
        return objectPhrase;
    }

    private void handleConjunct(IndexedWord subj, IndexedWord conjunct) {
        for (TypedDependency td : dependencies) {
            String shortName = td.reln().getShortName();
            if (td.gov().equals(conjunct)) {
                if (shortName.equalsIgnoreCase("conj")) {
                    IndexedWord pred = td.dep();
                    ArrayList<IndexedWord> obj = findObject(pred);
                    if (obj != null && !obj.isEmpty()) {
                        triples.add(
                                new Triple<>(
                                        subj.word(),
                                        pred.word(),
                                        Sentence.listToString(obj)
                                )
                        );
                    }
                }
            }
        }
    }

    public ArrayList<Triple<String, String, String>> getTriples() {
        return triples;
    }
}
