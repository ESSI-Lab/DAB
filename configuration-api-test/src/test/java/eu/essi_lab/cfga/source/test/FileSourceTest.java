package eu.essi_lab.cfga.source.test;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class FileSourceTest extends ConfigurationSourceTest {

    private File file;

    @Before
    public void before() {

	file = new File(//
		System.getProperty("java.io.tmpdir") + File.separator + "filesSourceTest");

	file.mkdirs();
	file = new File(file.getAbsolutePath() + File.separator + "temp.json");

	Arrays.asList(file.getParentFile().listFiles()).forEach(f -> f.delete());
    }

    @Test
    public void locationTest() throws Exception {

	FileSource fileSource = new FileSource(file);
	Assert.assertEquals(fileSource.getLocation(), fileSource.getSource().getAbsolutePath());
    }

    @Test
    public void listTest() throws Exception {

	super.listTest(new FileSource(file));
    }

    @Test
    public void lockTest() throws Exception {

	super.lockTest(new FileSource(file));
    }

    @Test
    public void backupTest() throws Exception {

	FileSource source = new FileSource(file);

	Setting setting1 = new Setting();
	Setting setting2 = new Setting();
	Setting setting3 = new Setting();

	source.flush(Arrays.asList(setting1, setting2, setting3));

	FileSource backup = source.backup();

	Assert.assertFalse(backup.isEmptyOrMissing());

	Assert.assertEquals(source.getSource().getParent(), backup.getSource().getParent());

	Assert.assertTrue(backup.getLocation().endsWith(".backup"));
    }
}
