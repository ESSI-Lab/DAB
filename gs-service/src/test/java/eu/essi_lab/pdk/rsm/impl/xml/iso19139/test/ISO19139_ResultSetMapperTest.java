package eu.essi_lab.pdk.rsm.impl.xml.iso19139.test;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper.MappingStrategy;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMD_ResultSetMapper;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMI_ResultSetMapper;

public class ISO19139_ResultSetMapperTest {

    static {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    private DiscoveryMessage message;
    private ResultSet<GSResource> resultSet;

    @Before
    public void init() throws UnsupportedEncodingException, JAXBException {

	WebRequest webRequest = WebRequest.createGET("http://localhost");
	message = new DiscoveryMessage();
	message.setWebRequest(webRequest);

	resultSet = new ResultSet<>();
	CountSet countSet = new CountSet() {
	    public int getCount() {
		return 3;
	    }

	};
	resultSet.setCountResponse(countSet);

	ArrayList<GSResource> arrayList = new ArrayList<>();
	{
	    // -------------------------------------------------
	    //
	    // first dataset with ISO19139 GMD original metadata
	    //
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset1");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    // set isDeleted to true
	    dataset.getIndexesMetadata().write(new IndexedResourceProperty(ResourceProperty.IS_DELETED, "true"));

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
	    // second dataset with core metadata
	    //
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset2");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    // set isDeleted to false
	    dataset.getIndexesMetadata().write(new IndexedResourceProperty(ResourceProperty.IS_DELETED, "false"));

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
	    // third dataset with ISO19139 GMI original metadata
	    //
	    //
	    Dataset dataset = new Dataset();

	    // set the private id, used to create the header id
	    dataset.setPrivateId("dataset3");

	    // resource time stamp
	    dataset.getIndexesMetadata()
		    .write(new IndexedResourceProperty(ResourceProperty.RESOURCE_TIME_STAMP, ISO8601DateTimeUtils.getISO8601DateTime()));

	    // set isDeleted to true
	    dataset.getIndexesMetadata().write(new IndexedResourceProperty(ResourceProperty.IS_DELETED, "true"));

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

	resultSet.setResultsList(arrayList);
    }

    @Test
    public void testGMDMapperCOREPriority() {

	try {

	    // ---------------------------------------------------------------------------------------------------

	    GMD_ResultSetMapper mapper = new GMD_ResultSetMapper();

	    ResultSet<Element> mappedResultSet = mapper.map(message, resultSet);
	    List<Element> resultsList = mappedResultSet.getResultsList();

	    // ---------------------------------------
	    //
	    // GMD original metadata, mapper set with the CORE priority
	    // the expected metadata is the CORE - GMD
	    //
	    {
		Element el = resultsList.get(0);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("core_MI1", mdMetadata.getFileIdentifier());
	    }
	    // ---------------------------------------
	    //
	    // original metadata is not GMD nor GMI
	    // the expected metadata is the CORE - GMD
	    //
	    {
		Element el = resultsList.get(1);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("core_MI2", mdMetadata.getFileIdentifier());
	    }

	    // ---------------------------------------
	    //
	    // GMI original metadata, mapper set with the CORE priority
	    // the expected metadata is the CORE - GMD
	    //
	    {
		Element el = resultsList.get(2);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("core_MI3", mdMetadata.getFileIdentifier());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGMIMapperCOREPriority() {

	try {

	    // ---------------------------------------------------------------------------------------------------

	    GMI_ResultSetMapper mapper = new GMI_ResultSetMapper();

	    ResultSet<Element> mappedResultSet = mapper.map(message, resultSet);
	    List<Element> resultsList = mappedResultSet.getResultsList();

	    // ---------------------------------------
	    //
	    // GMD original metadata, mapper set with the CORE priority
	    // the expected metadata is the CORE - GMI
	    //
	    {
		Element el = resultsList.get(0);
		MIMetadata miMetadata = new MIMetadata(el);
		Assert.assertEquals("core_MI1", miMetadata.getFileIdentifier());
	    }
	    // ---------------------------------------
	    //
	    // original metadata is not GMD nor GMI
	    // the expected metadata is the CORE - GMI
	    //
	    {
		Element el = resultsList.get(1);
		MIMetadata miMetadata = new MIMetadata(el);
		Assert.assertEquals("core_MI2", miMetadata.getFileIdentifier());
	    }

	    // ---------------------------------------
	    //
	    // GMI original metadata, mapper set with the CORE priority
	    // the expected metadata is the CORE - GMI
	    //
	    {
		Element el = resultsList.get(2);
		MIMetadata miMetadata = new MIMetadata(el);
		Assert.assertEquals("core_MI3", miMetadata.getFileIdentifier());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGMDMapperORIGINALPriority() {

	try {

	    // ---------------------------------------------------------------------------------------------------

	    GMD_ResultSetMapper mapper = new GMD_ResultSetMapper();
	    mapper.setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);

	    ResultSet<Element> mappedResultSet = mapper.map(message, resultSet);
	    List<Element> resultsList = mappedResultSet.getResultsList();

	    // ---------------------------------------
	    //
	    // GMD ORIGINAL metadata, mapper set with the ORIGINAL priority
	    // the expected metadata is the ORIGINAL - GMD
	    //
	    {
		Element el = resultsList.get(0);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("original_MD", mdMetadata.getFileIdentifier());
	    }
	    // ---------------------------------------
	    //
	    // original metadata is not GMD nor GMI
	    // the expected metadata is the CORE - GMD
	    //
	    {
		Element el = resultsList.get(1);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("core_MI2", mdMetadata.getFileIdentifier());
	    }

	    // ---------------------------------------
	    //
	    // mapper set with the ORIGINAL priority but the ORIGINAL is GMI
	    // the expected metadata is the CORE - GMD
	    //
	    {
		Element el = resultsList.get(2);
		MDMetadata mdMetadata = new MDMetadata(el);
		Assert.assertEquals("core_MI3", mdMetadata.getFileIdentifier());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGMIMapperORIGINALPriority() {

	try {

	    // ---------------------------------------------------------------------------------------------------

	    GMI_ResultSetMapper mapper = new GMI_ResultSetMapper();
	    mapper.setMappingStrategy(MappingStrategy.PRIORITY_TO_ORIGINAL_METADATA);

	    ResultSet<Element> mappedResultSet = mapper.map(message, resultSet);
	    List<Element> resultsList = mappedResultSet.getResultsList();

	    // ---------------------------------------
	    //
	    // mapper set with the ORIGINAL priority but the ORIGINAL is GMD
	    // the expected metadata is the CORE - GMI
	    //
	    {
		Element el = resultsList.get(0);
		MIMetadata mdMetadata = new MIMetadata(el);
		Assert.assertEquals("core_MI1", mdMetadata.getFileIdentifier());
	    }
	    // ---------------------------------------
	    //
	    // original metadata is not GMD nor GMI
	    // the expected metadata is the CORE - GMI
	    //
	    {
		Element el = resultsList.get(1);
		MIMetadata mdMetadata = new MIMetadata(el);
		Assert.assertEquals("core_MI2", mdMetadata.getFileIdentifier());
	    }

	    // ---------------------------------------
	    //
	    // GMI original metadata, mapper set with the ORIGINAL priority
	    // the expected metadata is the ORIGINAL - GMI
	    //
	    {
		Element el = resultsList.get(2);
		MIMetadata mdMetadata = new MIMetadata(el);
		Assert.assertEquals("original_MI", mdMetadata.getFileIdentifier());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
