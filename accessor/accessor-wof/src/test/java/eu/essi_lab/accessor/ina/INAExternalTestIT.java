package eu.essi_lab.accessor.ina;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.wof.WML_1_1Mapper;
import eu.essi_lab.accessor.wof.client.datamodel.TimeSeriesResponseDocument;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

public class INAExternalTestIT {

    private INAConnector connector;
    private WML_1_1Mapper mapper;
    private String endpoint;

    private Logger logger = GSLoggerFactory.getLogger(INAExternalTestIT.class);

    @Before
    public void init() {
	this.endpoint = "https://alerta.ina.gob.ar/wml";
	this.connector = new INAConnector();
	connector.setSourceURL(endpoint);
	this.mapper = new WML_1_1Mapper();
    }

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
	    Assert.assertEquals(1, list.size());
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
	    token = response.getResumptionToken();
	    request.setResumptionToken(token);
	    response = connector.listRecords(request);
	}
	logger.debug("Retrieved " + count + " original metadata records.");
	Assert.assertTrue(count >= 14);
    }

    @Test
    public void getValuesTest() throws GSException, ParseException {

	// e.g.
	// "https://alerta.ina.gob.ar/wml/GetValues?site=1072&variable=4&startDate=2018-03-19T21:00&endDate=2018-03-21T08:00

	String siteCode = "1072";
	String networkName = null;
	String variableCode = "4";
	SimpleDateFormat iso8601OutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601OutputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	String start = "2018-03-19T21:00:00";
	String endTime = "2018-03-21T08:00:00";
	Date begin = iso8601OutputFormat.parse(start);
	Date end = iso8601OutputFormat.parse(endTime);
	TimeSeriesResponseDocument res = connector.getValues(networkName, siteCode, variableCode, null, null, null, begin, end);
	Assert.assertNotNull(res);
    }
}
