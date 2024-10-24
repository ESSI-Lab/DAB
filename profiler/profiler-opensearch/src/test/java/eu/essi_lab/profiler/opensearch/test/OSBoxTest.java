package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import eu.essi_lab.profiler.os.OSBox;

public class OSBoxTest {

    @Test
    public void parseExceptionTest1() {

	String box1 = "a,b,c,d";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest2() {

	String box1 = "a";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest3() {

	String box1 = "0,0,0,";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest4() {

	String box1 = "00,0,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest5() {

	String box1 = "0,0,a,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest6() {

	String box1 = "-181,0,0,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest7() {

	String box1 = "181,0,0,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest8() {

	String box1 = "0,-91,0,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest9() {

	String box1 = "0,91,0,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest10() {

	String box1 = "0,0,-181,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest11() {

	String box1 = "0,0,181,0";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest12() {

	String box1 = "0,0,0,-91";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void parseExceptionTest13() {

	String box1 = "0,0,0,91";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");

	} catch (IllegalArgumentException ex) {
	    // OK
	}
    }

    @Test
    public void allZeroValues() {

	String box1 = "0,0,0,0";
	try {
	    new OSBox(box1);
	    // OK
	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void westGreaterThanEastTest() {

	String box1 = "70,0,-70,0";
	try {
	    new OSBox(box1);
	    // OK
	} catch (IllegalArgumentException ex) {
	    fail("Exception thrown");
	}
    }

    @Test
    public void southGreaterThanNorthTest() {

	String box1 = "0,70,0,-70";
	try {
	    new OSBox(box1);
	    fail("Exception not thrown");
	} catch (IllegalArgumentException ex) {
	    // OK 
	}
    }
}
