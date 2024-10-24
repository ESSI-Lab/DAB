package eu.essi_lab.lib.utils;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ReplacingInputStreamTest {
    @Test
    public void testName() throws Exception {

	String input = "hello xyz world.";

	byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

	ReplacingInputStream ris = new ReplacingInputStream(bis);

	ris.addSearchString("hello", "bye");

	ris.addSearchString("xyz", "abc");

	ris.addSearchString("world", "moon");
	
	ris.addSearchString(".", "!");

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	int b;
	while (-1 != (b = ris.read()))
	    bos.write(b);

	ris.close();

	String output = new String(bos.toByteArray());

	System.out.println(output);

	assertEquals("bye abc moon!", output);

    }
    
    @Test
    public void testEmpty() throws Exception {

	String input = "hello xyz world.";

	byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

	ReplacingInputStream ris = new ReplacingInputStream(bis);

	ris.addSearchString("hello", "");

	ris.addSearchString("xyz", "");

	ris.addSearchString("world", "");
	
	ris.addSearchString(".", "");

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	int b;
	while (-1 != (b = ris.read()))
	    bos.write(b);

	ris.close();

	String output = new String(bos.toByteArray());

	System.out.println(output);

	assertEquals("  ", output);

    }
    
    @Test
    public void testEmptyEmpty() throws Exception {

	String input = "helloxyzworld.";

	byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

	ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

	ReplacingInputStream ris = new ReplacingInputStream(bis);

	ris.addSearchString("hello", "");

	ris.addSearchString("xyz", "");

	ris.addSearchString("world", "");
	
	ris.addSearchString(".", "");

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	int b;
	while (-1 != (b = ris.read()))
	    bos.write(b);

	ris.close();

	String output = new String(bos.toByteArray());

	System.out.println(output);

	assertEquals("", output);

    }
}
