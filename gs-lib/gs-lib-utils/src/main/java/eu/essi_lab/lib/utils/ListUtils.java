/**
 * 
 */
package eu.essi_lab.lib.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabrizio
 */
public class ListUtils {

    /**
     * @param <T>
     * @param list
     * @return
     */
    public static <T> List<List<T>> permutations(List<T> list) {

	if (list.size() <= 1) {
	    return List.of(list);
	}

	List<List<T>> result = new ArrayList<>();

	for (int i = 0; i < list.size(); i++) {

	    T elem = list.get(i);
	    List<T> rest = new ArrayList<>(list);
	    rest.remove(i);

	    for (List<T> perm : permutations(rest)) {
		List<T> newPerm = new ArrayList<>();
		newPerm.add(elem);
		newPerm.addAll(perm);
		result.add(newPerm);
	    }
	}

	return result;
    }
}
