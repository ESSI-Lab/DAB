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

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class StreamUtils {

    /**
     * Blocking method that asynchronously executes the mapping of the list <code>source</code> of type <code>I</code> and returns the
     * resulting list of type <code>O</code>. The given <code>executor</code> is shutdown before exit
     *
     * @param source the source list of type <code>I</code>
     * @param asynchMapper a function that asynchronously maps type <code>I</code> items from <code>source</code> to type
     * <code>O</code> items
     * @param executor executes asynchronously <code>asynchFunction</code> on each item of the list <code>source</code>
     * @param <I> the input type
     * @param <O> the output type
     * @return
     */
    public static <I, O> List<O> asynchMap(List<I> source, Function<I, O> asynchMapper, ExecutorService executor) {

	try {

	    List<CompletableFuture<O>> futures = source.//
		    stream().//
		    map(res -> CompletableFuture.supplyAsync(() -> asynchMapper.apply(res), executor)).//
		    toList();

	    return futures.//
		    stream().//
		    map(CompletableFuture::join).//
		    collect(Collectors.toList()); //

	} finally {

	    executor.shutdown();
	}
    }

    /**
     * Blocking method that asynchronously consumes with <code>asynchConsumer</code> the given
     * list <code>source</code> of type <code>I</code>. The given <code>executor</code> is shutdown before exit
     *
     * @param source the source list of type <code>I</code>
     * @param asynchConsumer a consumer that asynchronously consume the items of the list <code>source</code>
     * @param executor executes asynchronously <code>asynchConsumer</code> on each item of the list <code>source</code>
     * @param <I> the input type
     * @return
     */
    public static <I> void asynchConsume(List<I> source, Consumer<I> asynchConsumer, ExecutorService executor) {

	try {

	    List<CompletableFuture<Object>> futures = source.//
		    stream().//

		    map(res -> CompletableFuture.supplyAsync(() -> {

		asynchConsumer.accept(res);

		return null;

	    }, executor)).//
		    toList();

	    futures.//
		    stream().//
		    map(CompletableFuture::join).//
		    toList();

	} finally {

	    executor.shutdown();
	}
    }

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
     * E.g.: given a stream of Person:<br><br> persons.stream().filter(distinctByKey(Person::getName)) <br>
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
