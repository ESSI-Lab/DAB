package eu.essi_lab.accessor;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import eu.essi_lab.accessor.imo.IMOClient;
import eu.essi_lab.accessor.imo.IMOConnector;
import eu.essi_lab.accessor.imo.ZRXPDocument;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class IMOConnectorExternalTestIT {

    @Test
    public void test() throws GSException, IOException {
	IMOConnector connector = new IMOConnector();
	IMOClient.setGiProxyEndpoint(System.getProperty("giProxyEndpoint"));
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	Iterator<OriginalMetadata> iterator = response.getRecords();
	while (iterator.hasNext()) {
	    OriginalMetadata originalMetadata = (OriginalMetadata) iterator.next();
	    String md = originalMetadata.getMetadata();
	    File file = File.createTempFile(getClass().getSimpleName(), ".zrxp");
	    ByteArrayInputStream bais = new ByteArrayInputStream(md.getBytes());
	    FileOutputStream fos = new FileOutputStream(file);
	    IOUtils.copy(bais, fos);
	    fos.close();
	    bais.close();
	    ZRXPDocument doc = new ZRXPDocument(file);
	    assertTrue(!doc.getBlocks().isEmpty());
	    System.out.println(doc.getBlocks().size());
	    file.delete();
	}
    }

}
