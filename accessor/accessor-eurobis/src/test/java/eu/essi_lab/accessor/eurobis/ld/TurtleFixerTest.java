package eu.essi_lab.accessor.eurobis.ld;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.Test;

import com.amazonaws.util.IOUtils;

public class TurtleFixerTest {

    @Test
    public void test() throws Exception {
	File inFile = File.createTempFile(TurtleFixerTest.class.getSimpleName(), ".ttl");	
	File outFile = File.createTempFile(TurtleFixerTest.class.getSimpleName(), ".ttl");
	InputStream stream = TurtleFixerTest.class.getClassLoader().getResourceAsStream("invalidturtle.ttl");
	FileOutputStream fos = new FileOutputStream(inFile);
	IOUtils.copy(stream, fos);
	stream.close();
	fos.close();
	TurtleFixer.fixFile(inFile, outFile);
	inFile.delete();
	outFile.delete();
    }

}
