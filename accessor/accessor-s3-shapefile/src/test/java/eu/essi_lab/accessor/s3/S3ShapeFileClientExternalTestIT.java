package eu.essi_lab.accessor.s3;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

public class S3ShapeFileClientExternalTestIT {

    @Test
    public void test() throws Exception {
	S3ShapeFileClient client = new S3ShapeFileClient("https://his-central.s3.amazonaws.com/polygons/");
	File s3Dir = Files.createTempDirectory("s3-test").toFile();
	client.downloadTo(s3Dir);
	File[] files = s3Dir.listFiles();
	for (File file : files) {
	    System.out.println(file.getAbsolutePath());
	}
	assertTrue(s3Dir.listFiles().length == 2);
	
	
	
//	for (File file : files) {
//	    file.delete();
//	}
//	s3Dir.delete();
    }

}
