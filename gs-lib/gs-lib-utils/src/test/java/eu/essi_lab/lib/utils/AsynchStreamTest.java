package eu.essi_lab.lib.utils;

import org.junit.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class AsynchStreamTest {

    @Test
    public void asynchMapperTest() {

	List<Long> source = Stream.iterate(0L, n -> n + 1).limit(100).toList();

	Function<Long, String> asynchMapper = String::valueOf;

	ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	List<String> result = StreamUtils.asynchMap(source, asynchMapper, executor);

	result.forEach(s -> Assert.assertEquals(String.valueOf(s), s));
    }

    @Test
    public void asynchConsumerTest() {

	List<String> list = Collections.synchronizedList(new ArrayList<>());

	List<Long> source = Stream.iterate(0L, n -> n + 1).limit(100).toList();

	Consumer<Long> asynchConsumer = (value) -> list.add(String.valueOf(value));

	ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	StreamUtils.asynchConsume(source, asynchConsumer, executor);

	list.forEach(s -> Assert.assertEquals(String.valueOf(s), s));
    }
}
