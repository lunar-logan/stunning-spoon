package sage;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import sage.spi.Triplet;
import sage.util.SPOTriplet;

import java.util.*;

/**
 * Created by Anurag Gautam on 24-02-2016.
 */
public class Algorithm {
    private final Collection<TypedDependency> dependencies;
    private final List<HasWord> origSent;
    private ArrayList<Triplet> triples = new ArrayList<>();

    public Algorithm(Collection<TypedDependency> dependencies, List<HasWord> origSent) {
        Objects.requireNonNull(dependencies);
        this.dependencies = dependencies;
        this.origSent = origSent;
        System.out.println(dependencies);
        process();
    }

    public void process() {

        // Look for nsubj or nsubjpass with governor as verb
        // TODO: why not combine the two if else into one if?
        for (TypedDependency td : dependencies) {
            String reln = td.reln().getShortName();
            if (reln.equalsIgnoreCase("nsubj")) {
                handleNominalSubject(td);
            } else if (reln.equalsIgnoreCase("nsubjpass")) {
                handleNominalSubject(td);
            }
        }
    }

    /**
     * Handles nsubj and nsubjpass dependencies
     *
     * @param dependency
     */
    private void handleNominalSubject(TypedDependency dependency) {
        if (dependency.gov().tag().startsWith("VB")) {                      // If gov is a verb, which is our predicate
            ArrayList<IndexedWord> obj = findObject(dependency.gov());
            if (obj != null && !obj.isEmpty()) {
                addTriple(
                        Arrays.asList(dependency.dep()),
                        Arrays.asList(dependency.gov()),
                        obj
                );
            }
        } else {                                                            // If gov not a VB then look for copular relation
            for (TypedDependency td : dependencies) {
                if (td.gov().equals(dependency.gov())) {
                    if (td.reln().getShortName().equalsIgnoreCase("cop")) {
                        addTriple(
                                Arrays.asList(dependency.dep()),
                                Arrays.asList(td.dep()),
                                Arrays.asList(td.gov())
                        );
                        break;
                    }
                }
            }
        }
        handleConjunct(dependency.dep(), dependency.gov());
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
                if (dependency.reln().getShortName().startsWith("aux")
                        /*|| dependency.reln().getShortName().startsWith("advcl")*/) {
                    aux = dependency.dep();
                }
            }
        }
        return aux;
    }

    /**
     * Takes a governor and a set of relations as a variable argument based on their priority order.
     * It returns the set of matched dependencies in the order of the given priority of relations.
     * <h3>Example:</h3>
     * Consider a set of typed dependencies:
     * <code>
     * nsubj(grows, Mango), nmod(grows, bla), dobj(grows, foo)
     * </code>
     * Suppose as per requirement the {@code dobj} relations has higher priority for the governor <i>grows</i>. You
     * can call {@code get(<grows as indexed word>, "dobj", "nmod")}. In the output you will get both relations with
     * {@code dobj} dominating the priority order.
     *
     * @param gov       the governor of the relations to be considered
     * @param relations the set of relations you want to extract in priority order
     * @return a priority queue of typed dependencies
     * <p>
     * TODO: After reading this... Wait what? This documentation is a stub
     */
    private PriorityQueue<TypedDependency> get(IndexedWord gov, String... relations) {
        HashMap<String, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < relations.length; i++) {
            priorityMap.put(relations[i], i);
        }
        PriorityQueue<TypedDependency> pq = new PriorityQueue<>((p, q) -> {
            Integer pp = priorityMap.get(p.reln().getShortName());
            Integer qp = priorityMap.get(q.reln().getShortName());
//            if (pp == null) {
//                pp = Integer.MAX_VALUE;
//            }
//            if (qp == null) {
//                qp = Integer.MAX_VALUE;
//            }
            return Integer.compare(pp, qp);
        });

        for (TypedDependency dependency : dependencies) {
            if (dependency.gov().equals(gov) && priorityMap.containsKey(dependency.reln().getShortName())) {
                pq.add(dependency);
            }
        }
        return pq;
    }

    private ArrayList<IndexedWord> findObject(IndexedWord predicate) {
        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();

        PriorityQueue<TypedDependency> candidates = get(predicate, "dobj", "iobj", "nmod");
        candidates.forEach(td -> {
            if (td.reln().getShortName().equalsIgnoreCase("nmod")) {
                handleNominalModifier(td, objectPhrase);
            } else {
                objectPhrase.add(td.dep());
            }
        });

       /* for (TypedDependency td : this.dependencies) {
            if (td.gov().equals(predicate)) {
                String reln = td.reln().getShortName();
                if (reln.equalsIgnoreCase("dobj") || reln.equalsIgnoreCase("iobj") || reln.equalsIgnoreCase("ccomp")) {
                    objectPhrase.add(td.dep());
                    break;
                }else if (reln.equalsIgnoreCase("nmod")) {
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
        }*/
        return objectPhrase;
    }


    private void handleNominalModifier(TypedDependency dependency, List<IndexedWord> objectPhrase) {
        if (dependency.gov().tag().startsWith("VB")) { // If governor is a verb
            TypedDependency td = getRelation(dependency.dep(), "case");
            if (td != null) {
                objectPhrase.add(dependency.dep());
                objectPhrase.add(td.dep());
            }
        }
    }

    private TypedDependency getRelation(IndexedWord gov, String relationName) {
        for (TypedDependency td : dependencies) {
            if (td.gov().equals(gov)) {
                if (td.reln().getShortName().equalsIgnoreCase(relationName)) {
                    return td;
                }
            }
        }
        return null;
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
                    }
                }
            }
        }
    }

    private void addTriple(List<IndexedWord> subject, List<IndexedWord> predicate, List<IndexedWord> object) {
//        System.out.println("Before pre-processing");
//        System.out.println(subject);
//        System.out.println(predicate);
//        System.out.println(object);
        ArrayList<IndexedWord> subjectPhrase = new ArrayList<>();
        ArrayList<IndexedWord> predicatePhrase = new ArrayList<>();
        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();

        subject.forEach(sub -> {
            subjectPhrase.add(sub);
            subjectPhrase.addAll(findAttributes(sub));
        });

        predicate.forEach(pre -> {
            predicatePhrase.add(pre);
            IndexedWord auxiliary = getPredicateAttributes(pre);
            if (auxiliary != null) {
                predicatePhrase.add(auxiliary);
            }
        });

        object.forEach(obj -> {
            objectPhrase.add(obj);
            objectPhrase.addAll(findAttributes(obj));
        });

        Collections.sort(subjectPhrase);
        Collections.sort(predicatePhrase);
        Collections.sort(objectPhrase);

        triples.add(new SPOTriplet(origSent, subjectPhrase, predicatePhrase, objectPhrase));
    }

    private ArrayList<IndexedWord> findAttributes(IndexedWord word) {
        ArrayList<IndexedWord> attrs = new ArrayList<>();
        for (TypedDependency td : dependencies) {
            String relationName = td.reln().getShortName();
            if (td.gov().equals(word)) {
                if (relationName.equalsIgnoreCase("amod")
                        || relationName.equalsIgnoreCase("neg")
                        || relationName.equalsIgnoreCase("compound")
                        || relationName.equalsIgnoreCase("nummod")
                        || relationName.equalsIgnoreCase("dep")
                        || relationName.equalsIgnoreCase("mark")
                        || relationName.equalsIgnoreCase("advmod")
                        || relationName.equalsIgnoreCase("det")
                        || relationName.startsWith("acl")) {
                    attrs.add(td.dep());
                    ArrayList<IndexedWord> attributes = findAttributes(td.dep());
                    if (!attributes.isEmpty()) {
                        attrs.addAll(attributes);
                    }
                } else if (relationName.equalsIgnoreCase("conj")) {
                    if (td.dep().tag().startsWith("NN")) {
                        attrs.add(td.dep());
                        ArrayList<IndexedWord> attributes = findAttributes(td.dep());
                        if (!attributes.isEmpty()) {
                            attrs.addAll(attributes);
                        }
                    }
                } else if (relationName.equalsIgnoreCase("nmod")) {
                    // Look for case relation
                    for (TypedDependency td1 : dependencies) {
                        String relnName = td1.reln().getShortName();
                        if (td1.gov().equals(td.dep())) {
                            if (relnName.equalsIgnoreCase("case")) {
                                attrs.add(td1.dep());
                                break;
                            }
                        }
                    }
                    ArrayList<IndexedWord> nmodAttrs = findAttributes(td.dep());
                    attrs.add(td.dep());
                    if (!nmodAttrs.isEmpty()) {
                        attrs.addAll(nmodAttrs);
                    }
                }
            }
        }
        return attrs;
    }

    public ArrayList<Triplet> getTriples() {
        return triples;
    }
}
