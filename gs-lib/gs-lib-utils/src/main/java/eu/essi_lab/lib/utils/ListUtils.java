/**
 * 
 */
package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
