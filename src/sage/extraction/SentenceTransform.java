package sage.extraction;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import sage.spi.Triplet;
import sage.util.SPOTriplet;

import java.util.*;

/**
 * Created by Anurag Gautam on 02-03-2016.
 */
public class SentenceTransform {
    private final List<HasWord> originalSentence;
    private final Collection<TypedDependency> dependencies;

    private final ArrayList<IndexedWord> subject;
    private final ArrayList<IndexedWord> predicate;
    private final ArrayList<IndexedWord> object;

    private ArrayList<Triplet> triples = new ArrayList<>();

    public SentenceTransform(Collection<TypedDependency> dependencies, List<HasWord> origSent) {
        this.dependencies = dependencies;
        this.originalSentence = origSent;
        this.subject = new ArrayList<>();
        this.predicate = new ArrayList<>();
        this.object = new ArrayList<>();
        transform();
    }

    private void transform() {
        // Look for nsubj or nsubjpass with governor as verb
        // TODO: why not combine the two if else into one if?
        for (TypedDependency td : dependencies) {
            String relationName = td.reln().getShortName();
            if (relationName.equalsIgnoreCase("nsubj")) {
                handleNominalSubject(td);
            } else if (relationName.equalsIgnoreCase("nsubjpass")) {
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
            findObject(dependency.gov());
            subject.add(dependency.dep());
            predicate.add(dependency.gov());
            addTriple();
        } else {                                                            // If gov not a VB then look for copular relation
            for (TypedDependency td : dependencies) {
                if (td.gov().equals(dependency.gov())) {
                    if (td.reln().getShortName().equalsIgnoreCase("cop")) {
                        subject.add(dependency.dep());
                        predicate.add(td.dep());
                        object.add(td.gov());
                        addTriple();
                        break;
                    }
                }
            }
        }
        handleConjunct(dependency.dep(), dependency.gov());
    }

    private void handleConjunct(IndexedWord subj, IndexedWord conjunct) {
        for (TypedDependency td : dependencies) {
            String shortName = td.reln().getShortName();
            if (td.gov().equals(conjunct)) {
                if (shortName.equalsIgnoreCase("conj")) {
                    IndexedWord pred = td.dep();
                    findObject(pred);
                    subject.add(subj);
                    predicate.add(pred);
                    addTriple();
                }
            }
        }
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
            return Integer.compare(pp, qp);
        });

        for (TypedDependency dependency : dependencies) {
            if (dependency.gov().equals(gov) && priorityMap.containsKey(dependency.reln().getShortName())) {
                pq.add(dependency);
            }
        }
        return pq;
    }

    private void findObject(IndexedWord predicate) {
        PriorityQueue<TypedDependency> candidates = get(predicate, "dobj", "iobj", "nmod");
        TypedDependency head = candidates.peek();
        if (head != null) {
            String relation = head.reln().getShortName();
            if (relation.equalsIgnoreCase("dobj") || relation.equalsIgnoreCase("iobj")) {
                object.add(head.dep());
            } else if (relation.equalsIgnoreCase("nmod")) {
                handleNominalModifier(head);
            }
        }
    }


    /**
     * Handles nmod dependency for predicate/object computation
     *
     * @param dependency
     */
    private void handleNominalModifier(TypedDependency dependency) {
        if (dependency.gov().tag().startsWith("VB")) { // If governor is a verb
            TypedDependency td = getRelationHavingGovernor(dependency.dep(), "case");
            object.add(dependency.dep());
            if (td != null) {
                predicate.add(td.dep());
            }
        }
    }


    private TypedDependency getRelationHavingGovernor(IndexedWord gov, String relationName) {
        for (TypedDependency td : dependencies) {
            if (td.gov().equals(gov)) {
                if (td.reln().getShortName().equalsIgnoreCase(relationName)) {
                    return td;
                }
            }
        }
        return null;
    }

    private void addTriple() {
        ArrayList<IndexedWord> subjectPhrase = new ArrayList<>();
        ArrayList<IndexedWord> predicatePhrase = new ArrayList<>();
        ArrayList<IndexedWord> objectPhrase = new ArrayList<>();

        subject.forEach(sub -> {
//            subjectPhrase.add(sub.makeCopy());
            subjectPhrase.addAll(findSubjectAttributes(sub));
        });

        predicate.forEach(pre -> {
            predicatePhrase.add(pre.makeCopy());
            IndexedWord auxiliary = getPredicateAttributes(pre);
            if (auxiliary != null) {
                predicatePhrase.add(auxiliary);
            }
        });

        object.forEach(obj -> {
            objectPhrase.add(obj.makeCopy());
            objectPhrase.addAll(findAttributes(obj));
        });

        Collections.sort(subjectPhrase);
        Collections.sort(predicatePhrase);
        Collections.sort(objectPhrase);

        triples.add(new SPOTriplet(originalSentence, subjectPhrase, predicatePhrase, objectPhrase));

        subject.clear();
        predicate.clear();
        object.clear();
    }

    private Collection<? extends IndexedWord> findSubjectAttributes(IndexedWord sub) {
        ArrayList<IndexedWord> attributes = new ArrayList<>();
        attributes.add(sub.makeCopy());

        PriorityQueue<TypedDependency> relations = get(sub, "amod", "compound");
        relations.forEach(reln -> {
            IndexedWord dep = reln.dep();
            Collection<? extends IndexedWord> attrs = findSubjectAttributes(dep);
            attributes.addAll(attrs);
        });

        return attributes;
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
