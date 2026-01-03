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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SynchronizedBoundedList<E> extends LinkedList<E> {

    private final int maxSize;

    /**
     * @param <E>
     * @param maxSize
     * @param clazz
     * @return
     */
    public static <E> List<E> of(int maxSize, Class<E> clazz) {

	SynchronizedBoundedList<E> out = new SynchronizedBoundedList<E>(maxSize);

	return Collections.synchronizedList(out);
    }

    /**
     * @param condition
     * @return
     */
    public Optional<E> find(Predicate<E> condition) {

	return stream().filter(condition).findFirst();
    }

    @Override
    public boolean add(E e) {

	synchronized (this) {

	    while (size() >= maxSize) {

		removeFirst();
	    }

	    return super.add(e);
	}
    }

    /**
     * @param maxSize
     */
    private SynchronizedBoundedList(int maxSize) {

	this.maxSize = maxSize;
    }
}
