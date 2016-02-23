package sage;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import sage.util.Util;

import java.util.*;

/**
 * Created by Anurag Gautam on 20-02-2016.
 */
public class ExtractionAlgorithm {
    public static TreeMap<IndexedWord, TreeSet<IndexedWord>> relationGraph(Collection<TypedDependency> dependencies) {
        final TreeMap<IndexedWord, TreeSet<IndexedWord>> g = new TreeMap<>((o1, o2) -> {
            return Integer.compare(o1.index(), o2.index());
        });
        for (TypedDependency dependency : dependencies) {
            IndexedWord gov = dependency.gov();
            IndexedWord dep = dependency.dep();
            TreeSet<IndexedWord> adjList = g.get(gov);
            if (adjList == null) {
                adjList = new TreeSet<>();
                g.put(gov, adjList);
            }
            adjList.add(dep);               // Add the dependent to the adjacency list
        }
        return g;
    }

    /**
     * @param tree
     * @param map
     */
    public static void extractSubject(
            Tree tree,
            Map<IndexedWord, TreeSet<IndexedWord>> map,
            ArrayList<String> sentence) {

        // Find the NP subtree
        getSubtree(tree, "NP");                                 // Get the NP subtree, store in result
        TreeSet<IndexedWord> subjectPhrase = new TreeSet<>();   // Holds the subject phrase

        System.out.println("Current result: " + result);        // Ignore this line

        result.forEach(subtree -> {
            Tree nn = getByTag(subtree, "NN");                  /// Get the first Noun
            if (nn != null) {
                IndexedWord noun = new IndexedWord(nn.value(), -1, sentence.indexOf(nn.label().value()) + 1);
                noun.setWord(nn.label().value());
                subjectPhrase.add(noun);
                subjectPhrase.addAll(extractAttributes(noun, map));
            }
        });
        System.out.println(Util.join(subjectPhrase.stream(), " "));
    }


    /**
     * Extracts the predicate from the sentence.
     * It performs level order traversal
     *
     * @param tree
     * @param map
     * @param sentence
     */
    public static void extractPredicate(
            Tree tree,
            Map<IndexedWord, TreeSet<IndexedWord>> map,
            ArrayList<String> sentence) {

        result.clear();
        getSubtree(tree, "VP");

        TreeSet<IndexedWord> predicatePhrase = new TreeSet<>();

        for (Tree subtree : result) {
            subtree.pennPrint();
            System.out.println("\n----==\n");
            /*for (Tree child : subtree.children()) {
                if (child.label().value().startsWith("VB")) {

                }
            }
            Tree vb = getByTag(subtree, "VB");
            if (vb != null) {
                IndexedWord verb = new IndexedWord(vb.value(), -1, sentence.indexOf(vb.value()));
                verb.setWord(vb.value());
                ArrayList<IndexedWord> attributes = extractAttributes(verb, map);
                predicatePhrase.add(verb);
                predicatePhrase.addAll(attributes);
                break;
            }*/
        }

        System.out.println("Predicate: " + Util.join(predicatePhrase.stream(), " "));
    }

    private static int optDepth = -1;
    private static Tree node = null;

    private static void getDeepest(Tree subtree, String tag, int currentDepth) {
        if (subtree.value().startsWith(tag)) {
            if (optDepth < currentDepth) {
                optDepth = currentDepth;
                node = subtree;
            }
        }
        for (Tree child : subtree.children()) {
            getDeepest(child, tag, currentDepth + 1);
        }
    }


    static ArrayList<Tree> result = new ArrayList<>();

    /**
     * Performs a breadth first search for the attributes in the relationship map of the sentence
     *
     * @param word
     * @param map
     * @return
     */
    private static ArrayList<IndexedWord> extractAttributes(IndexedWord word, Map<IndexedWord, TreeSet<IndexedWord>> map) {
        Deque<IndexedWord> queue = new LinkedList<>();
        TreeSet<IndexedWord> seen = new TreeSet<>();
        ArrayList<IndexedWord> attributes = new ArrayList<>();

        queue.add(word);

        while (!queue.isEmpty()) {
            IndexedWord u = queue.poll();
            seen.add(u);
            if (map.containsKey(u)) {
                for (IndexedWord v : map.get(u)) {
                    if (!seen.contains(v)) {
                        queue.offer(v);
                        attributes.add(v);
                    }
                }
            }
        }
        return attributes;
    }

    private static void getSubtree(Tree t, String label) {
        if (t.label().value().equalsIgnoreCase(label)) {
            result.add(t);
        }
        for (Tree child : t.children())
            getSubtree(child, label);
    }

    private static Tree getByTag(Tree subtreeRoot, String tag) {
        if (subtreeRoot.label().value().startsWith(tag)) {
            return subtreeRoot.firstChild();
        }
        Tree res = null;
        for (Tree child : subtreeRoot.children()) {
            res = getByTag(child, tag);
        }
        return res;
    }
}
