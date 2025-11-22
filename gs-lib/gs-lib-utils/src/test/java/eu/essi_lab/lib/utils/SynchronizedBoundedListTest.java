/**
 * 
 */
package eu.essi_lab.lib.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class SynchronizedBoundedListTest {

    @Test
    public void test() {

	List<String> list = SynchronizedBoundedList.of(3, String.class);

	list.add("A");
	list.add("B");
	list.add("C");

	Assert.assertEquals("[A, B, C]", list.toString());

	list.add("D");
	Assert.assertEquals("[B, C, D]", list.toString());

	list.add("E");
	Assert.assertEquals("[C, D, E]", list.toString());

	list.add("F");
	Assert.assertEquals("[D, E, F]", list.toString());
    }
}
