package sage.extraction;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import sage.Vocabulary;
import sage.util.LogUtil;
import sage.util.MemCache;
import sage.util.SPOTriplet;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Anurag Gautam on 18-04-2016.
 */
public class ExtractionFramework {

    private LogUtil L = new LogUtil();

    private final Collection<TypedDependency> dependencies;
    private final Vocabulary vocab;

    private IndexedWord subject = null;
    private IndexedWord predicate = null;

    private ArrayList<IndexedWord> objects = new ArrayList<>();
    private Set<IndexedWord> predicates = new TreeSet<>();

    private IndexedWord lastSubject = null;

    public ExtractionFramework(Collection<TypedDependency> dependencies, Vocabulary vocabulary) {
        this.dependencies = dependencies;
        this.vocab = vocabulary;

        System.err.println(dependencies);

        // Lets get the last subject
        IndexedWord ls = (IndexedWord) MemCache.get("lastSubject");
        if (ls != null) {
            lastSubject = ls;
        }
    }


    public ArrayList<SPOTriplet> getTriples() {
        ArrayList<SPOTriplet> triples = new ArrayList<>();

        filterByRelation(dependencies.stream(), (td) -> {
            handleNominalSubject(td);

            ArrayList<IndexedWord> subjectPhrase = new ArrayList<>();
            subjectPhrase.add(handleCoref(subject));
            subjectPhrase.addAll(getSubjectAttributes(subject));

            ArrayList<IndexedWord> predicatePhrase = new ArrayList<>();
            predicatePhrase.addAll(predicates);
            predicates.forEach(p -> predicatePhrase.addAll(getPredicateAttributes(p)));

            Collections.sort(subjectPhrase);
            Collections.sort(predicatePhrase);


            objects.forEach(obj -> {
                ArrayList<IndexedWord> objectPhrase = new ArrayList<>();
                objectPhrase.add(obj);
                objectPhrase.addAll(findAttributes(obj));
                Collections.sort(objectPhrase);

                triples.add(new SPOTriplet(Arrays.asList(), subjectPhrase, predicatePhrase, objectPhrase));
            });


        }, "nsubj", "nsubjpass");

        return triples;
    }

    private IndexedWord handleCoref(IndexedWord prp) {
        IndexedWord sub = null;
        if ((prp.tag().toLowerCase().startsWith("prp") /*&& prp.word().toLowerCase().startsWith("it")*/) ||
                prp.tag().toLowerCase().startsWith("w")) {
            if (lastSubject != null) {
                sub = lastSubject.makeCopy();
            }
        } else {
            sub = prp;
        }
        lastSubject = sub;
        MemCache.put("lastSubject", lastSubject);
        return lastSubject;
    }

    /**
     * Extracts the attributes of a subject phrase
     *
     * @param subj
     * @return
     */
    private ArrayList<IndexedWord> getSubjectAttributes(IndexedWord subj) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();
        dependencies.stream()
                .filter(td -> td.gov().equals(subj))
                .forEach(td -> attrs.add(td.dep()));
        return attrs;
    }

    private void handleNominalSubject(TypedDependency td) {
        predicates.clear();
        objects.clear();

        IndexedWord gov = td.gov();                             // Get the governor
        IndexedWord dep = td.dep();                             // Get the dependent
        IndexedWord base = null;
        boolean iscop = false;

        subject = dep;                                          // Subject in all cases  is the dependent
        if (gov.tag().startsWith("VB")) {                       // If the governor is a verb then we've found our predicate
            predicates.add(gov);
            base = gov;
        } else {
            TypedDependency cop = getRelation("cop", gov);
            if (cop != null) {
                base = handleCopRelation(cop);
                iscop = true;
            }
        }
        findObject(base, iscop);
    }

    /**
     * Handles copular relations
     *
     * @param cop
     * @return
     */
    private IndexedWord handleCopRelation(TypedDependency cop) {
        IndexedWord dep = cop.dep();
        if (dep.tag().startsWith("VB")) {                    // Dependent of cop is a verb, probably the predicate
            predicates.add(dep);
//            predicates.add(cop.gov());

            TypedDependency acl = getRelation("acl", cop.gov());
            if (acl != null) {
                L.i("Found acl relation for " + cop.gov());
                predicates.add(acl.dep());
                return acl.dep();
            }

            return cop.gov();
        }
        return null;
    }

    /**
     * Extracts the object phrase form the sentence (typed dependencies of it)
     */
    private void findObject(IndexedWord base, boolean isCop) {
        IndexedWord object = null;
        if (base == null) {
            L.w("base word is null, cannot find object");
        } else {
//            if(isCop){
//                object = base;
//            } else {
            TypedDependency dobj = getRelation("dobj", base);
            if (dobj != null) {
                object = dobj.dep();
            } else {
                L.w("Could not find `dobj` relation, looking for nmod");  //
                TypedDependency nmod = getRelation("nmod", base);
                if (nmod != null) {
                    predicates.add(base);
                    object = nmod.dep();
                    addCase(object);
                } else {
                    L.w("nmod relation could not be located. Taking gov of cop to be object");
                    object = base;
                    predicates.remove(base);
                }
//                }
            }
        }

        if (object != null) {
            objects.add(object);
            addConjunct(object);
        }
    }

    private void addConjunct(IndexedWord object) {
        TypedDependency cc = getRelation("cc", object);
        if (cc != null) {
            String ccWord = cc.dep().word();
            if (ccWord.startsWith("or") || ccWord.startsWith("and")) {
                filterByRelation(dependencies.stream(), td -> {
                    if (td.gov().equals(object)) {
                        objects.add(td.dep());
                    }
                }, "conj");
            } else {
                L.i("Unknown ccWord: " + ccWord + " found, ignoring");
            }
        } else {
            L.i("No cc relation for " + object);
        }
    }

    private void addCase(IndexedWord nmodGov) {
        TypedDependency aCase = getRelation("case", nmodGov);
        if (aCase == null) {
            L.i("No case relation for " + nmodGov + " found");
        } else {
            predicates.add(aCase.dep());
        }
    }

    /**
     * Find and returns the auxiliary of a verb. See <b>aux</b> universal dependency
     * for more information.
     *
     * @param verb
     * @return
     */
    private ArrayList<IndexedWord> getPredicateAttributes(IndexedWord verb) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();
        for (TypedDependency dependency : dependencies) {
            if (dependency.gov().equals(verb)) {
                if (dependency.reln().getShortName().startsWith("aux")) {
                    attrs.add(dependency.dep());
                }
            }
        }
        attrs.addAll(getCompoundAndAMod(verb));
        return attrs;
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


    private Stream<TypedDependency> getRelations(String relnName) {
        return dependencies
                .stream()
                .filter(td -> td.reln().getShortName().equalsIgnoreCase(relnName));
    }

    private TypedDependency getRelation(String relnName, IndexedWord governor) {
        relnName = relnName.toLowerCase();

        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().toLowerCase().startsWith(relnName) &&
                    dependency.gov().equals(governor)) {
                return dependency;
            }
        }
        return null;
    }

    private void filterByRelation(Stream<TypedDependency> stream, Consumer<TypedDependency> callback, String... relns) {
        stream.filter(td -> {
            String shortName = td.reln().getShortName();
            for (String r : relns) {
                if (shortName.startsWith(r)) return true;
            }
            return false;
        }).forEach(callback);                                   // All the filtered relations consumed by the callback
    }
}
