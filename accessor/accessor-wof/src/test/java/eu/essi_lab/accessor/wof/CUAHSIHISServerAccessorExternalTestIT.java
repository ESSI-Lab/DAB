package eu.essi_lab.accessor.wof;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class CUAHSIHISServerAccessorExternalTestIT {

    @SuppressWarnings("rawtypes")
    public CUAHSIHISServerConnector connector;
    protected WML_1_1Mapper mapper;
    protected String endpoint;

    private Logger logger = GSLoggerFactory.getLogger(CUAHSIHISServerAccessorExternalTestIT.class);

    @SuppressWarnings("rawtypes")
    @Before
    public void init() {
	this.endpoint = CUAHSIHISServerConnectorExternalTestIT.ENDPOINT1;
	this.connector = new CUAHSIHISServerConnector();
	connector.setSourceURL(endpoint);
	this.mapper = new WML_1_1Mapper();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws GSException {
	ListRecordsRequest request = new ListRecordsRequest();
	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);
	String token = response.getResumptionToken();
	int count = 0;
	GSSource source = new GSSource();
	source.setEndpoint(endpoint);
	HashSet<String> resourceIdentifiers = new HashSet<>();
	while (token != null && count < 14) {
	    logger.debug("Token: {}", token);
	    Iterator<OriginalMetadata> iterator = response.getRecords();
	    List<OriginalMetadata> list = Lists.newArrayList(iterator);
	    // Assert.assertEquals(1, list.size());
	    if (!list.isEmpty()) {
		OriginalMetadata metadata = list.get(0);
		GSResource res = mapper.map(metadata, source);
		String id = res.getHarmonizedMetadata().getCoreMetadata().getDataIdentification().getResourceIdentifier();
		logger.debug("Resource id: {}", id);
		if (!resourceIdentifiers.contains(id)) {
		    resourceIdentifiers.add(id);
		} else {
		    String msg = "Found a duplicate identifier: " + id;
		    logger.error(msg);
		    Assert.fail(msg);
		}

		count++;
	    }
	    token = response.getResumptionToken();
	    request.setResumptionToken(token);
	    response = connector.listRecords(request);
	}
	logger.debug("Retrieved " + count + " original metadata records.");
	Assert.assertTrue(count >= 14);
    }
}
