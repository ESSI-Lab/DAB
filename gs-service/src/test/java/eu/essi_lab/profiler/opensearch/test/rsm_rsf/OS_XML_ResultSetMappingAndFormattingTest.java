package eu.essi_lab.profiler.opensearch.test.rsm_rsf;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.os.handler.discover.OS_XML_ResultSetFormatter;

public class OS_XML_ResultSetMappingAndFormattingTest extends JS_API_ResultSetMappingAndFormattingTest {

    @Test
    public void test() {

	// web request
	String value = "http://localhost/gs-service/services/essi/opensearch?si=10&ct=50&st=pippo&ts=1900&te=2000-01-01T00:00:00.000Z&bbox=0,0,0,0_1,1,1,1&outputFormat="
		+ NameSpace.GS_DATA_MODEL_XML_MEDIA_TYPE;
	WebRequest webRequest = WebRequest.createGET(value);

	// message
	DiscoveryMessage message = new DiscoveryMessage();
	message.setWebRequest(webRequest);
	message.setPage(new Page(10));

	// result set
	ResultSet<GSResource> resultSet = new ResultSet<GSResource>();

	try {
	    InputStream stream = OS_XML_ResultSetMappingAndFormattingTest.class.getClassLoader().getResourceAsStream("termFrequency.xml");
	    TermFrequencyMap tfMap = TermFrequencyMap.create(stream);
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 1;
		}

		public Optional<TermFrequencyMap> mergeTermFrequencyMaps(int maxItemsCount) {
		    return Optional.ofNullable(tfMap);
		}
	    };
	    resultSet.setCountResponse(countSet);

	} catch (JAXBException e1) {

	    fail("Exception thrown");
	}

	ArrayList<GSResource> list = new ArrayList<GSResource>();
	ResourceDescriptor descriptor = new ResourceDescriptor();
	descriptor.setAbstract(UUID.randomUUID().toString());
	descriptor.setTitle(UUID.randomUUID().toString());
	GeographicBoundingBox boundingBox = new GeographicBoundingBox();
	boundingBox.setEast(0.);
	boundingBox.setWest(0.);
	boundingBox.setNorth(0.);
	boundingBox.setSouth(0.);
	descriptor.setBbox(boundingBox);
	descriptor.setIdentitifer(UUID.randomUUID().toString());
	descriptor.setResourceTimeStamp(ISO8601DateTimeUtils.getISO8601DateTime());
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(ISO8601DateTimeUtils.getISO8601DateTime(2000, 5, 1));
	temporalExtent.setEndPosition(ISO8601DateTimeUtils.getISO8601DateTime(2010, 5, 1));
	descriptor.setTemporal(temporalExtent);

	list.add(createDataset(descriptor));
	resultSet.setResultsList(list);

	// result set mapper
	DiscoveryResultSetMapper<String> mapper = DiscoveryResultSetMapperFactory.loadMappers(//
		new ESSILabProvider(), //
		MappingSchema.GS_DATA_MODEL_MAPPING_SCHEMA, //
		String.class).get(0); //

	// result set formatter
	DiscoveryResultSetFormatter<String> formatter = DiscoveryResultSetFormatterFactory.loadFormatters(//
		new ESSILabProvider(), //
		OS_XML_ResultSetFormatter.OS_XML_FORMATTING_ENCODING, String.class).get(0);

	try {

	    // -----------------
	    //
	    // mapper test
	    //
	    //
	    ResultSet<String> mappedResultSet = mapper.map(message, resultSet);

	    List<String> resultsList = mappedResultSet.getResultsList();
	    Assert.assertTrue(resultsList.size() == 1);

	    for (String result : resultsList) {

		ByteArrayInputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
		Dataset dataset = Dataset.create(inputStream);

		CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
		Assert.assertEquals(coreMetadata.getTitle(), descriptor.getTitle());
	    }

	    // -----------------
	    //
	    // formatter test
	    //
	    //
	    Response response = formatter.format(message, mappedResultSet);

	    String entity = (String) response.getEntity();

	    Assert.assertNotNull(entity);

	    // System.out.println(entity);

	} catch (Exception e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}
    }
}
