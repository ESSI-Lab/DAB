package eu.essi_lab.profiler.oaipmh.test;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.jaxb.oaipmh.RecordType;
import eu.essi_lab.jaxb.oaipmh.StatusType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper.MappingStrategy;
import eu.essi_lab.profiler.oaipmh.handler.discover.OAIPMHResultSetMapper;

public class OAIPMHResultSetMapperTest {

    static {

	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());
    }

    protected OAIPMHResultSetMapper mapper;
    protected DiscoveryMessage message;
    protected ResultSet<GSResource> resultSet;

    public OAIPMHResultSetMapperTest() {

    }

    public void init(String queryString, int totalResults) throws UnsupportedEncodingException, JAXBException {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	WebRequest webRequest = WebRequest.createGET(queryString);
	message = new DiscoveryMessage();
	message.setWebRequest(webRequest);

	mapper = new OAIPMHResultSetMapper();

	resultSet = new ResultSet<>();
	CountSet countSet = new CountSet() {
	    public int getCount() {
		return totalResults;
	    }

	};
	resultSet.setCountResponse(countSet);

	ArrayList<GSResource> arrayList = new ArrayList<>();
	{
	    // -------------------------------------------------
	    //
	    // first dataset
	    // original: GMD
	    // core : GMI
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset1");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier("source1");
	    dataset.setSource(gsSource);

	    // set the original metadata
	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
	    MDMetadata mdMetadata = new MDMetadata();
	    mdMetadata.setFileIdentifier("original_MD");
	    originalMetadata.setMetadata(mdMetadata.asString(true));

	    // set the core metadata
	    MIMetadata miMetadata = new MIMetadata();
	    miMetadata.setFileIdentifier("core_MI1");
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    dataset.setOriginalMetadata(originalMetadata);
	    arrayList.add(dataset);
	}

	{
	    // ----------------------------------
	    //
	    // second dataset
	    // original: GMD
	    // core : GMI
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset2");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier("source2");
	    dataset.setSource(gsSource);

	    // set the original metadata
	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI("unknown");
	    originalMetadata.setMetadata("none");

	    // set the core metadata
	    MIMetadata miMetadata = new MIMetadata();
	    miMetadata.setFileIdentifier("core_MI2");
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(miMetadata);

	    dataset.setOriginalMetadata(originalMetadata);
	    arrayList.add(dataset);
	}

	{
	    // -------------------------------------------------
	    //
	    // third dataset
	    // original: GMI
	    // core : GMI
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset3");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier("source3");
	    dataset.setSource(gsSource);

	    // set the original metadata
	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
	    MIMetadata miMetadata = new MIMetadata();
	    miMetadata.setFileIdentifier("original_MI");
	    originalMetadata.setMetadata(miMetadata.asString(true));

	    // set the core metadata
	    MIMetadata coreMiMetadata = new MIMetadata();
	    coreMiMetadata.setFileIdentifier("core_MI3");
	    dataset.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(coreMiMetadata);

	    dataset.setOriginalMetadata(originalMetadata);
	    arrayList.add(dataset);
	}

	{
	    // -------------------------------------------------
	    //
	    // fourth dataset, deleted
	    //
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset4");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    GSSource gsSource = new GSSource();
	    gsSource.setUniqueIdentifier("source4");
	    dataset.setSource(gsSource);

	    dataset.getIndexesMetadata().write(new IndexedResourceProperty(ResourceProperty.IS_DELETED, "true"));

	    arrayList.add(dataset);
	}

	// ---------------------------------------------------------------------------------------------------

	resultSet.setResultsList(arrayList);
    }

    @Test
    public void noResumptionTokenTest() {
	try {

	    init("http://profiler-oai-pmh?verb=ListRecords&metadataPrefix=ISO19139-2006", 3);

	    ResultSet<String> map = getResultSet();

	    List<String> resultsList = map.getResultsList();

	    // ---------------------

	    String recordString0 = resultsList.get(0);
	    RecordType recordType = CommonContext.unmarshal(recordString0, RecordType.class);
	    Assert.assertEquals(recordType.getHeader().getSetSpec().get(0), "source1");

	    // ---------------------

	    String recordString1 = resultsList.get(1);
	    recordType = CommonContext.unmarshal(recordString1, RecordType.class);
	    Assert.assertEquals(recordType.getHeader().getSetSpec().get(0), "source2");

	    // ---------------------

	    String recordString2 = resultsList.get(2);
	    recordType = CommonContext.unmarshal(recordString2, RecordType.class);
	    Assert.assertEquals(recordType.getHeader().getSetSpec().get(0), "source3");

	    // ---------------------

	    String recordString3 = resultsList.get(3);
	    recordType = CommonContext.unmarshal(recordString3, RecordType.class);
	    Assert.assertNull(recordType.getMetadata());
	    Assert.assertEquals(recordType.getHeader().getSetSpec().get(0), "source4");
	    Assert.assertEquals(recordType.getHeader().getStatus().name(), StatusType.DELETED.name());

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void resourceMappingCOREPolicyTest() {
	try {

	    init("http://profiler-oai-pmh?verb=ListRecords&metadataPrefix=ISO19139-2006", 3);

	    ResultSet<String> map = getResultSet();

	    List<String> resultsList = map.getResultsList();

	    doTest(resultsList.get(0), "core_MI1");

	    doTest(resultsList.get(1), "core_MI2");

	    doTest(resultsList.get(2), "core_MI3");

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void resourceMappingORIGINALPolicyTest() {
        try {
    
            init("http://profiler-oai-pmh?verb=ListRecords&metadataPrefix=ISO19139-2006-GMI", 3);
    
            mapper.setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);
            ResultSet<String> map = getResultSet();
    
            List<String> resultsList = map.getResultsList();
    
            doTest(resultsList.get(0), "core_MI1");
    
            doTest(resultsList.get(1), "core_MI2");
    
            doTest(resultsList.get(2), "original_MI");
    
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown");
        }
    }

    private void doTest(String record, String metadataValue) throws Exception {
	RecordType recordType = CommonContext.unmarshal(record, RecordType.class);
	Object any = recordType.getMetadata().getAny();
	if (any instanceof Element) {
	    Element anyElement = (Element) any;
	    Assert.assertTrue(anyElement.getLocalName().equals("MI_Metadata"));
	    any = CommonContext.unmarshal(anyElement, JAXBElement.class);
	}

	if (any instanceof JAXBElement<?>) {
	    JAXBElement<?> jaxb = (JAXBElement<?>) any;
	    Object value = jaxb.getValue();

	    if (value instanceof MIMetadataType) {
		MIMetadataType miMetadata = (MIMetadataType) value;
		Assert.assertEquals(metadataValue, ISOMetadata.getStringFromCharacterString(miMetadata.getFileIdentifier()));
	    } else {
		Assert.fail();
	    }
	} else {
	    Assert.fail();
	}

    }

    protected ResultSet<String> getResultSet() throws GSException {

	return mapper.map(message, resultSet);
    }
}
