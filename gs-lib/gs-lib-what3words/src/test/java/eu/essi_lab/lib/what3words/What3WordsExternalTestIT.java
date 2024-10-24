package eu.essi_lab.lib.what3words;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class What3WordsExternalTestIT {

    @Test
    public void testWordsToPosition() throws Exception {
	What3Words w3w = new What3Words("firm", "airless", "camp");
	assertEquals("43.819251", w3w.getLatitude());
	assertEquals("11.200691", w3w.getLongitude());
	assertEquals("https://api.what3words.com/v2", w3w.getEndpoint());
    }

    @Test
    public void testPositionToWords() throws Exception {
	What3Words w3w = new What3Words("43.819251", "11.200691");
	assertEquals("firm", w3w.getWord1());
	assertEquals("airless", w3w.getWord2());
	assertEquals("camp", w3w.getWord3());
	assertEquals("https://api.what3words.com/v2", w3w.getEndpoint());
    }

    @Test
    public void testTimeout1() {
	long start = System.currentTimeMillis();
	What3Words w3w = null;
	try {
	    w3w = new What3Words("firm", "airless", "camp", 1000);
	} catch (Exception e) {
	}
	if (w3w != null) {
	    assertEquals("43.819251", w3w.getLatitude());
	    assertEquals("11.200691", w3w.getLongitude());
	}
	long end = System.currentTimeMillis();
	assertTrue(end - start < 4000);
    }

    @Test
    public void testTimeout2() {
	long start = System.currentTimeMillis();
	What3Words w3w = null;
	try {
	    w3w = new What3Words("43.819251", "11.200691", 1000);
	} catch (Exception e) {
	}
	if (w3w != null) {
	    assertEquals("firm", w3w.getWord1());
	    assertEquals("airless", w3w.getWord2());
	    assertEquals("camp", w3w.getWord3());
	}
	long end = System.currentTimeMillis();
	assertTrue(end - start < 4000);
    }
}
