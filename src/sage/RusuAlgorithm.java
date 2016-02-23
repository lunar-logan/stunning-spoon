package sage;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.*;
import java.util.function.Function;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Created by Anurag Gautam on 21-02-2016.
 */
public class RusuAlgorithm {
    private static final Logger logger = Logger.getLogger(RusuAlgorithm.class.getName());

    static {
        logger.setUseParentHandlers(false);
        logger.addHandler(new StreamHandler() {
            public void publish(LogRecord logRecord) {
                System.out.printf(
                        "%s/%s#%s: %s\n",
                        logRecord.getLevel().getName().charAt(0),
                        logRecord.getSourceClassName(),
                        logRecord.getSourceMethodName(),
                        logRecord.getMessage());
            }
        });
    }

    private Tree predicateRoot;

//    private final Collection<TypedDependency> dependencies;
//    private final TreeMap<IndexedWord, TreeSet<IndexedWord>> relationMap;
//    private final ArrayList<String> sentence;


    public RusuAlgorithm(Collection<TypedDependency> dependencies, ArrayList<String> sentence) {
//        this.dependencies = dependencies;
//        this.relationMap = relationGraph(dependencies);
//        relationMap.forEach((k, v) -> {
//            System.out.println(k + "\t => \t" + v);
//        });
//        this.sentence = sentence;
    }

    public String extractSubject(Tree root) {
        Tree npSubtree = getSubtree(root, node -> node.label().value().equalsIgnoreCase("NP"));
        if (npSubtree != null) {
            Tree nn = getSubtree(npSubtree, node -> node.label().value().startsWith("NN") && node.firstChild().isLeaf());
            if (nn != null) {
                return nn.firstChild().label().value();
            } else {
                logger.info("No NN node found in the NP subtree");
            }
        } else {
            logger.info("No NP subtree found");
        }
        return null;
    }


    public Tree extractPredicate(Tree root) {
        Tree vp = getSubtree(root, node -> node.label().value().equalsIgnoreCase("vp"));
        predicateRoot = vp;
        Tree result = null;
        if (vp != null) {
            result = deepestVerb(vp);
        } else {
            logger.info("No VP subtree found");
        }
        return result;
    }

    public Tree deepestVerb(Tree root) {
        if (!root.label().value().equalsIgnoreCase("vp")) {
            throw new IllegalArgumentException("tree must be vp");
        }
        TreeMap<Integer, ArrayList<Tree>> result = new TreeMap<>(Collections.reverseOrder());
        lookDeep(root, tag -> tag.startsWith("VB"), 0, result);
        System.out.println(result);
        Map.Entry<Integer, ArrayList<Tree>> entry = result.firstEntry();
        if (entry != null) {
            return entry.getValue().get(0);
        }
        return null;
    }


    private void lookDeep(Tree t,
                          Function<String, Boolean> acceptable,
                          int currentDepth,
                          TreeMap<Integer, ArrayList<Tree>> map) {
        String rootLabel = t.label().value();
        if (acceptable.apply(rootLabel)) {
            ArrayList<Tree> adj = map.get(currentDepth);
            if (adj == null) {
                adj = new ArrayList<>();
                map.put(currentDepth, adj);
            }
            adj.add(t);
        }
        for (Tree child : t.children()) {
            lookDeep(child, acceptable, currentDepth + 1, map);
        }
    }


    public String extractObject(Tree root, Tree predicateSubtree) {
        if (predicateSubtree == null) {
            logger.info("Predicate subtree is null, exiting");
            return null;
        }
        Tree parent = getSubtree(predicateRoot, node ->
                node.label().value().startsWith("NP") ||
                        node.label().value().startsWith("PP") ||
                        node.label().value().startsWith("ADJP"));
        if (parent != null) {
            String parentLabel = parent.label().value();
            if (parentLabel.startsWith("NP")) {
                Tree nn = getSubtree(parent, node -> node.label().value().startsWith("NN"));
                if (nn != null) {
                    return nn.firstChild().label().value();
                }
            } else if (parentLabel.startsWith("PP")) {
                Tree nn = getSubtree(parent, node -> node.label().value().startsWith("NN"));
                if (nn != null) {
                    return nn.firstChild().label().value();
                }
            } else if (parentLabel.startsWith("ADJP")) {
                Tree jj = getSubtree(parent, node -> node.label().value().startsWith("JJ"));
                if (jj != null) {
                    return jj.firstChild().label().value();
                }
            }
        }
        return null;
    }


    /**
     * Extracts the attribute for the given word
     *
     * @param root
     * @param subtree
     * @return
     */
    private ArrayList<Tree> extractAttributes(Tree root, Tree subtree) {
        String label = subtree.label().value().toLowerCase();
        ArrayList<Tree> attributes = new ArrayList<>();
        Tree parent = subtree.ancestor(1, root);

        if (subtree.label().value().startsWith("JJ")) { // Its an adjective, get all the RB
            for (Tree child : parent.children()) {
                if (child != subtree) {
                    if (child.label().value().startsWith("JJ")
                            || child.label().value().startsWith("RB")) {
                        attributes.addAll(child.postOrderNodeList());
                    }
                }
            }
        } else if (label.startsWith("NN")) { // Its a noun, add all DT, PRP, POS, JJ, CD, ADJP, QP, NP siblings
            for (Tree child : parent.children()) {
                if (child != subtree) {
                    String childLabel = child.label().value().toLowerCase();
                    if (childLabel.startsWith("DT")
                            || childLabel.startsWith("PRP")
                            || childLabel.startsWith("JJ")
                            || childLabel.startsWith("CD")
                            || childLabel.startsWith("NP")
                            || childLabel.startsWith("ADJP")) {
                        attributes.addAll(child.preOrderNodeList());
                    }

                }
            }
        }
        return attributes;
    }

    /**
     * Returns the subtree satisfying the predicate
     *
     * @param root
     * @param predicate
     * @return
     */
    private Tree getSubtree(Tree root, Function<Tree, Boolean> predicate) {
        Deque<Tree> q = new LinkedList<>();
        Tree subtree = null;

        q.offer(root);

        while (!q.isEmpty()) {
            Tree u = q.poll();
            if (predicate.apply(u)) {
                subtree = u;
                break;
            }
            for (Tree v : u.children()) {
                q.offer(v);
            }
        }
        return subtree;
    }

}
