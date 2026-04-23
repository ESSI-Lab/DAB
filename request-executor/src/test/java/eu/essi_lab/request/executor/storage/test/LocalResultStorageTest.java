package eu.essi_lab.request.executor.storage.test;

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.request.executor.storage.*;
import org.apache.commons.io.*;
import org.junit.*;

import java.io.*;

/**
 * @author Fabrizio
 */
public class LocalResultStorageTest {

    @Test
    public void test() throws IOException {

	DownloadSetting setting = new DownloadSetting();
	setting.setDownloadStorage(DownloadSetting.DownloadStorage.LOCAL_DOWNLOAD_STORAGE);

	LocalResultStorage storage = new LocalResultStorage(setting);

	String location = storage.getStorageLocation("test");

	try (InputStream stream = LocalResultStorageTest.class.getClassLoader().getResourceAsStream("test-storage.txt")) {

	    File tmpFile = File.createTempFile("test", ".tmp");
	    tmpFile.deleteOnExit();

	    FileOutputStream fos = new FileOutputStream(tmpFile);

	    IOUtils.copy(stream, fos);
	    fos.close();

	    storage.store("test", tmpFile);

	    tmpFile.delete();

	} catch (Exception e) {

	    throw new RuntimeException(e);
	}

	Assert.assertTrue(new File(location).exists());

    }
}
