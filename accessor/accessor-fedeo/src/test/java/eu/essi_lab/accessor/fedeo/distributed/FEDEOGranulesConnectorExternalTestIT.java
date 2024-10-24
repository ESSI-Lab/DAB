package eu.essi_lab.accessor.fedeo.distributed;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */

public class FEDEOGranulesConnectorExternalTestIT {

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    @Test
    public void testRealCountMethod() throws IOException, GSException {

	FEDEOGranulesConnector connector = new FEDEOGranulesConnector();
	
	connector.setSourceURL("https://fedeo.ceos.org/opensearch/request?");

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	Page page = Mockito.mock(Page.class);

	int count = 0;
	int start = 1;

	Mockito.doReturn(start).when(page).getStart();

	Mockito.doReturn(count).when(page).getSize();

	String templateURL = "https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP%3ASTFC%3ACEDA-CCI&startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}&clientId=gs-service";

	HttpResponse<InputStream> resp = connector.retrieve(message, page, templateURL);

	int i = connector.count(resp);

	logger.debug("Count {}", i);

	Assert.assertEquals(i, 229);
	
	
    }

}