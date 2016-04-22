package sage.extraction;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import sage.Vocabulary;
import sage.util.SPOTriplet;
import sage.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by Anurag Gautam on 18-04-2016.
 */
public class Xtract {
    private final Collection<TypedDependency> dependencies;
    private final Vocabulary vocab;
    private IndexedWord subject = null;
    private IndexedWord predicate = null;
    private ArrayList<IndexedWord> objects = new ArrayList<>();
    private IndexedWord lastSubject = null;

    public Xtract(Collection<TypedDependency> dependencies, Vocabulary vocabulary) {
        this.dependencies = dependencies;
        this.vocab = vocabulary;
    }

    public ArrayList<SPOTriplet> getTriples() {
        ArrayList<SPOTriplet> triples = new ArrayList<>();

        dependencies.stream()
                .filter(td -> td.reln().getShortName().equalsIgnoreCase("nsubj") || td.reln().getShortName().equalsIgnoreCase("nsubjpass"))
                .forEach(td -> {
                    handleNominalSubject(td);
                    ArrayList<IndexedWord> subjectPhrase = new ArrayList<>();
                    subjectPhrase.addAll(getSubjectAttributes(subject));
                    subjectPhrase.add(handleCoref(subject));

                    ArrayList<IndexedWord> predicatePhrase = new ArrayList<>();
                    predicatePhrase.add(predicate);
                    Util.addIfNotNull(predicatePhrase, getPredicateAttributes(predicate));

                    Collections.sort(subjectPhrase);
                    Collections.sort(predicatePhrase);

                    objects.forEach(obj -> {
                        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();
                        objectPhrase.add(obj);
                        objectPhrase.addAll(findAttributes(obj));
                        Collections.sort(objectPhrase);
                        triples.add(new SPOTriplet(Arrays.asList(), subjectPhrase, predicatePhrase, objectPhrase));
                    });
                });

        return triples;
    }

    private IndexedWord handleCoref(IndexedWord prp) {
        if (prp.tag().toLowerCase().startsWith("prp") && prp.word().toLowerCase().startsWith("it")) {
            if (lastSubject != null) {
                return lastSubject.makeCopy();
            }
        } else {
            lastSubject = prp;
        }
        return prp;
    }

    private ArrayList<IndexedWord> getSubjectAttributes(IndexedWord subj) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();
        dependencies.stream()
                .filter(td -> td.gov().equals(subj))
                .forEach(td -> attrs.add(td.dep()));
        return attrs;
    }

    private void handleNominalSubject(TypedDependency td) {
        IndexedWord gov = td.gov();
        IndexedWord dep = td.dep();

        // If the governor is a verb then we've found our predicate
        if (gov.tag().startsWith("VB")) {
            subject = dep;
            predicate = gov;
        } else {
            subject = dep;
            TypedDependency cop = getRelation("cop", gov);
            if (cop != null) {
                if (cop.dep().tag().startsWith("VB")) { // found our predicate
                    predicate = cop.dep();
                    objects.add(cop.gov());
                }
            }
        }

        if (predicate != null && subject != null)
            findObjects();
    }

    private void findObjects() {
        getRelations("dobj")
                .filter(d -> d.gov().equals(predicate))
                .forEach(d -> objects.add(d.dep()));

        dependencies.stream()
                .filter(td -> {
                    return objects.contains(td.gov()) && td.reln().getShortName().equalsIgnoreCase("conj");
                })
                .forEach(td -> objects.add(td.dep()));

    }


    private Stream<TypedDependency> getRelations(String relnName) {
        return dependencies
                .stream()
                .filter(td -> td.reln().getShortName().equalsIgnoreCase(relnName));
    }

    private TypedDependency getRelation(String relnName, IndexedWord governor) {
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equalsIgnoreCase(relnName) &&
                    dependency.gov().equals(governor)) {
                return dependency;
            }
        }
        return null;
    }

    /**
     * Find and returns the auxiliary of a verb. See <b>aux</b> universal dependency
     * for more information.
     *
     * @param verb
     * @return
     */
    private IndexedWord getPredicateAttributes(IndexedWord verb) {
        IndexedWord aux = null;
        for (TypedDependency dependency : dependencies) {
            if (dependency.gov().equals(verb)) {
                if (dependency.reln().getShortName().startsWith("aux")) {
                    aux = dependency.dep();
                }
            }
        }
        return aux;
    }


    private ArrayList<IndexedWord> getCompoundAndAMod(IndexedWord ofWord) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();

        dependencies.stream()
                .filter(td -> td.gov().equals(ofWord))
                .filter(td -> {
                    String relationName = td.reln().getShortName();
                    return relationName.equalsIgnoreCase("compound") || relationName.equalsIgnoreCase("amod");
                })
                .forEach(td -> attrs.add(td.dep()));
        return attrs;
    }


    /**
     * TODO: Fix this bro
     *
     * @param word
     * @return
     */
    private ArrayList<IndexedWord> findAttributes(IndexedWord word) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();

        // Add amod and compound modifiers
        attrs.addAll(getCompoundAndAMod(word));

        dependencies.stream()
                .filter(td -> td.gov().equals(word))
                .filter(td -> td.reln().getShortName().equalsIgnoreCase("nummod") || td.reln().getShortName().equalsIgnoreCase("acl"))
                .forEach(td -> {
                    attrs.add(td.dep());
                    attrs.addAll(getCompoundAndAMod(td.dep()));
                });

        if (attrs.isEmpty()) {

            // Handle nmod relations
            dependencies.stream()
                    .filter(td -> td.reln().getShortName().equalsIgnoreCase("nmod"))
                    .filter(td -> td.gov().equals(word))
                    .forEach(nmod -> {
                        attrs.add(nmod.dep());
                        attrs.addAll(getCompoundAndAMod(nmod.dep()));
                        dependencies.stream()
                                .filter(ds -> ds.reln().getShortName().equalsIgnoreCase("case") && ds.gov().equals(nmod.dep()))
                                .findFirst().ifPresent(d -> {
                            attrs.add(d.dep());
                            if (d.dep().word().equalsIgnoreCase("between")) {
                                dependencies.stream()
                                        .filter(ds -> ds.reln().getShortName().equalsIgnoreCase("conj") && ds.gov().equals(d.gov()))
                                        .findFirst().ifPresent(e -> attrs.add(e.dep()));
                            }
                        });
                    });
        }
        return attrs;
    }
}
