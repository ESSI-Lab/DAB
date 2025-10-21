/**
 * 
 */
package eu.essi_lab.lib.utils;

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
