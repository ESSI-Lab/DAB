package eu.essi_lab.lib.utils;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class ComparableEntryTest {

    @Test
    public void test() {
	ComparableEntry<String, String> entryAB = new ComparableEntry<>("a", "b");
	ComparableEntry<String, String> entryAC = new ComparableEntry<>("a", "c");
	ComparableEntry<String, String> entryCA = new ComparableEntry<>("c", "a");
	Assert.assertTrue(entryAB.compareTo(entryAC) < 0);
	Assert.assertTrue(entryAB.compareTo(entryCA) < 0);
	Assert.assertTrue(entryAC.compareTo(entryCA) < 0);
	Set<ComparableEntry<String, String>> set = new TreeSet<>();
	set.add(entryAC);
	set.add(entryCA);
	set.add(entryAB);
	for (ComparableEntry<String, String> entry : set) {
	    System.out.println(entry.getKey() + " " + entry.getValue());
	}
    }
}
