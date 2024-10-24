package eu.essi_lab.access;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.data.DataDescriptor;

public class DataDownloaderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {

    }

    @Test
    public void testName() throws Exception {
	exception.expect(GSException.class);
	DataDownloader dd = new DataDownloader() {

	    @Override
	    public boolean canConnect() throws GSException {
		return true;
	    }
	    
	    @Override
	    public Provider getProvider() {
		return null;
	    }

	    @Override
	    public List<DataDescriptor> getRemoteDescriptors() {
		return null;
	    }

	    @Override
	    public File download(DataDescriptor descriptor) {
		return null;
	    }

	    @Override
	    public boolean canDownload() {
		return false;
	    }
	};
	dd.setOnlineResource(new Dataset(), "3432");
    }

}
