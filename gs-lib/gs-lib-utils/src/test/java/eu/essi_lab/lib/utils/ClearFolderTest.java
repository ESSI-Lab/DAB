/**
 * 
 */
package eu.essi_lab.lib.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class ClearFolderTest {

    @Test
    public void test1() throws IOException, URISyntaxException {

	File tempDir = FileUtils.createTempDir(UUID.randomUUID().toString(), true);

	Assert.assertTrue(tempDir.exists());

	File file = new File(getClass().getClassLoader().getResource("testDoc.xml").toURI());

	org.apache.commons.io.FileUtils.copyFileToDirectory(file, tempDir);

	Assert.assertEquals(1, tempDir.listFiles().length);

	//
	//
	//

	FileUtils.clearFolder(tempDir, false);

	//
	//
	//

	Assert.assertTrue(tempDir.exists());

	Assert.assertEquals(0, tempDir.listFiles().length);
    }
    
    @Test
    public void test2() throws IOException, URISyntaxException {

	File tempDir = FileUtils.createTempDir(UUID.randomUUID().toString(), true);

	Assert.assertTrue(tempDir.exists());

	File file = new File(getClass().getClassLoader().getResource("testDoc.xml").toURI());

	org.apache.commons.io.FileUtils.copyFileToDirectory(file, tempDir);

	Assert.assertEquals(1, tempDir.listFiles().length);

	//
	//
	//

	FileUtils.clearFolder(tempDir, true);

	//
	//
	//

	Assert.assertFalse(tempDir.exists());
    }
}
