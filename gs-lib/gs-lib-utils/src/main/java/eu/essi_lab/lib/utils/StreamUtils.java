package eu.essi_lab.lib.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Fabrizio
 */
public class StreamUtils {

    /**
     * @param iterator
     * @return
     */
    public static <T> Stream<T> iteratorToStream(final Iterator<T> iterator) {

	return iteratorToStream(iterator, false);
    }

    /**
     * @param iterator
     * @param parallell
     * @return
     */
    public static <T> Stream<T> iteratorToStream(final Iterator<T> iterator, final boolean parallell) {

	Iterable<T> iterable = () -> iterator;
	return StreamSupport.stream(iterable.spliterator(), parallell);
    }

    /**
     * E.g.: given a stream of Person:<br><br>
     * persons.stream().filter(distinctByKey(Person::getName)) <br>
     * persons.stream().filter(distinctByKey(p -> p.getName()))
     * 
     * @param keyExtractor
     * @return
     */
    public static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {

	Set<Object> seen = ConcurrentHashMap.newKeySet();
	return t -> seen.add(keyExtractor.apply(t));
    }
}
