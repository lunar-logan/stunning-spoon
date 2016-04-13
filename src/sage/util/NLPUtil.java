package sage.util;

import edu.stanford.nlp.trees.TypedDependency;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anurag Gautam on 12-04-2016.
 */
public class NLPUtil {
    public static ArrayList<TypedDependency> getRelation(Collection<TypedDependency> dependencyCollection, String relation) {
        ArrayList<TypedDependency> relations = new ArrayList<>();

        for (TypedDependency dep : dependencyCollection) {
            if (dep.reln().getShortName().startsWith(relation)) {
                relations.add(dep);
            }
        }

        return relations;
    }
}
