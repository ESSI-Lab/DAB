/**
 * 
 */
package eu.essi_lab.cfga.source.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.cfg.DatabaseSource;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class DatabaseSourceTest extends ConfigurationSourceTest {

    protected StorageInfo storageInfo;

    /**
     * @param info
     */
    public DatabaseSourceTest(StorageInfo info) {

	this.storageInfo = info;
    }

    @Test
    public void locationTest() throws Exception {

	DatabaseSource source = create();
	Assert.assertEquals("configtest\\test-config.json", source.getLocation());
    }

    @Test
    public void listTest() throws Exception {

	super.listTest(create());
    }

    @Test
    public void lockTest() throws Exception {

	super.lockTest(create());
    }

    /**
     * @return
     * @throws GSException
     */
    protected abstract DatabaseSource create() throws Exception;

    @Test
    public void backupTest() throws Exception {

	DatabaseSource source = create();

	Setting setting1 = new Setting();
	Setting setting2 = new Setting();
	Setting setting3 = new Setting();

	source.flush(Arrays.asList(setting1, setting2, setting3));

	DatabaseSource backup = source.backup();

	Assert.assertFalse(backup.isEmptyOrMissing());

	// Assert.assertEquals(source.getSource().getParent(), backup.getSource().getParent());

	Assert.assertTrue(backup.getLocation().endsWith(".backup"));
    }

}
