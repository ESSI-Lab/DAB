package eu.essi_lab.profiler.opensearch.test.rsm_rsf;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.indexes.IndexedElementsWriter;
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
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatter;
import eu.essi_lab.pdk.rsf.DiscoveryResultSetFormatterFactory;
import eu.essi_lab.pdk.rsf.impl.json.jsapi._1_0.JS_API_ResultSetFormatter_1_0;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapperFactory;
import eu.essi_lab.pdk.rsm.impl.json.jsapi.JS_API_ResultSetMapper;

public class JS_API_ResultSetMappingAndFormattingTest {

    @Before
    public void init() {
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

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
	    InputStream stream = JS_API_ResultSetMappingAndFormattingTest.class.getClassLoader().getResourceAsStream("termFrequency.xml");
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
		JS_API_ResultSetMapper.JS_API_MAPPING_SCHEMA, String.class).get(0); //

	// result set formatter
	DiscoveryResultSetFormatter<String> formatter = DiscoveryResultSetFormatterFactory.loadFormatters(//
		new ESSILabProvider(), //
		JS_API_ResultSetFormatter_1_0.JS_API_FORMATTING_ENCODING, //
		String.class).get(0);

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

		JSONObject jsonObject = new JSONObject(result);
		// System.out.println(jsonObject);
		String string = (String) jsonObject.get("title");
		Assert.assertEquals(string, descriptor.getTitle());
	    }

	    // -----------------
	    //
	    // formatter test
	    //
	    //
	    Response response = formatter.format(message, mappedResultSet);

	    String entity = (String) response.getEntity();

	    Assert.assertNotNull(entity);

	    System.out.println(entity);

	    JSONObject jsonObject = new JSONObject(entity);
	    JSONArray jsonArray = jsonObject.getJSONArray("reports");
	    Assert.assertEquals(jsonArray.length(), 1);
	    Assert.assertEquals(((JSONObject) jsonArray.get(0)).get("title"), descriptor.getTitle());

	} catch (Exception e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}
    }

    protected Dataset createDataset(ResourceDescriptor rd) {

	Dataset dataset = new Dataset();
	dataset.setPrivateId(rd.getIdentitifer());
	dataset.setPublicId(rd.getIdentitifer());

	dataset.setOriginalId("originalId");

	GSSource gsSource = new GSSource();

	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	gsSource.setLabel(UUID.randomUUID().toString());
	gsSource.setUniqueIdentifier(UUID.randomUUID().toString());

	dataset.setSource(gsSource);

	// ----------------------------
	//
	// set the core metadatas
	//
	String title = rd.getTitle();
	if (title != null) {
	    dataset.getHarmonizedMetadata().getCoreMetadata().setTitle(title);
	}

	String abstract_ = rd.getAbstract_();
	if (abstract_ != null) {
	    dataset.getHarmonizedMetadata().getCoreMetadata().setAbstract(abstract_);
	}

	IndexedElementsWriter.write(dataset);

	return dataset;
    }

    protected class ResourceDescriptor {

	private String identitifer;
	private String title;
	private String abstract_;
	private GeographicBoundingBox bbox;
	private TemporalExtent temporal;
	private String resourceTimeStamp;

	public String getResourceTimeStamp() {
	    return resourceTimeStamp;
	}

	public void setResourceTimeStamp(String resourceTimeStamp) {
	    this.resourceTimeStamp = resourceTimeStamp;
	}

	public String getIdentitifer() {
	    return identitifer;
	}

	public void setIdentitifer(String identitifer) {
	    this.identitifer = identitifer;
	}

	public String getTitle() {
	    return title;
	}

	public void setTitle(String title) {
	    this.title = title;
	}

	public String getAbstract_() {
	    return abstract_;
	}

	public void setAbstract(String abstract_) {
	    this.abstract_ = abstract_;
	}

	public GeographicBoundingBox getBbox() {
	    return bbox;
	}

	public void setBbox(GeographicBoundingBox bbox) {
	    this.bbox = bbox;
	}

	public TemporalExtent getTemporal() {
	    return temporal;
	}

	public void setTemporal(TemporalExtent temporal) {
	    this.temporal = temporal;
	}
    }

}
