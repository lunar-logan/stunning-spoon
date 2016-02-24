package sage;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.Triple;

import java.util.*;

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
                addTriple(
                        Arrays.asList(dependency.dep()),
                        Arrays.asList(dependency.gov()),
                        obj
                );
                /*
                triples.add(new Triple<>(
                                dependency.dep().word(),
                                dependency.gov().word(),
                                Sentence.listToString(obj)
                        )
                );*/
            }
        } else { // look for copular relation
            for (TypedDependency td : dependencies) {
                String shortName = td.reln().getShortName();
                if (td.gov().equals(dependency.gov())) {
                    if (shortName.equalsIgnoreCase("cop")) {
                        addTriple(
                                Arrays.asList(dependency.dep()),
                                Arrays.asList(td.dep()),
                                Arrays.asList(td.gov())
                        );
                        /*triples.add(
                                new Triple<>(
                                        dependency.dep().word(),
                                        td.dep().word(),
                                        td.gov().word()
                                )
                        );*/
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
                        addTriple(
                                Arrays.asList(subj),
                                Arrays.asList(pred),
                                obj
                        );
                        /*
                        triples.add(
                                new Triple<>(
                                        subj.word(),
                                        pred.word(),
                                        Sentence.listToString(obj)
                                )
                        );*/
                    }
                }
            }
        }
    }

    private void addTriple(List<IndexedWord> subject, List<IndexedWord> predicate, List<IndexedWord> object) {
        ArrayList<IndexedWord> subjectPhrase = new ArrayList<>();
        ArrayList<IndexedWord> predicatePhrase = new ArrayList<>();
        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();

        subject.forEach(sub -> {
            subjectPhrase.add(sub);
            subjectPhrase.addAll(findAttributes(sub));
        });

        predicate.forEach(pred -> {
            predicatePhrase.add(pred);
            predicatePhrase.addAll(findAttributes(pred));
        });

        object.forEach(obj -> {
            objectPhrase.add(obj);
            objectPhrase.addAll(findAttributes(obj));
        });

        Collections.sort(subjectPhrase);
        Collections.sort(predicatePhrase);
        Collections.sort(objectPhrase);

        String sub = Sentence.listToString(subjectPhrase);
        String pre = Sentence.listToString(predicatePhrase);
        String obj = Sentence.listToString(objectPhrase);

        triples.add(new Triple<>(sub, pre, obj));

    }

    private ArrayList<IndexedWord> findAttributes(IndexedWord word) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();
        if (word.tag().startsWith("NN")) { //
            getNounAttributes(word, attrs);
        }
        return attrs;
    }

    private void getNounAttributes(IndexedWord nounWord, List<IndexedWord> attributes) {
        for (TypedDependency td : dependencies) {
            String relationName = td.reln().getShortName();
            if (td.gov().equals(nounWord)) {
                if (relationName.equalsIgnoreCase("amod")
                        || relationName.equalsIgnoreCase("det")
                        || relationName.equalsIgnoreCase("neg")) {
                    attributes.add(td.dep());
                }
            }
        }
    }

    public ArrayList<Triple<String, String, String>> getTriples() {
        return triples;
    }
}
