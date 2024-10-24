//package eu.essi_lab.profiler.csw.test.ressetmapper;
//
//import static org.junit.Assert.fail;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//
//import javax.xml.bind.JAXBException;
//import javax.xml.bind.Marshaller;
//
//import org.junit.Test;
//
//import eu.essi_lab.jaxb.common.CommonContext;
//import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
//import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
//import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
//import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
//import eu.essi_lab.messages.DiscoveryMessage;
//import eu.essi_lab.messages.ResultSet;
//import eu.essi_lab.messages.web.WebRequest;
//import eu.essi_lab.model.CoreMetadata;
//import eu.essi_lab.model.Dataset;
//import eu.essi_lab.model.GSResource;
//import eu.essi_lab.model.HarmonizedMetadata;
//import eu.essi_lab.model.OriginalMetadata;
//import eu.essi_lab.profiler.csw.CSWISOProfiler;
//import eu.essi_lab.profiler.csw.CSWProfiler;
//
//public class CSWResultSetMapperTest {
//
//    @Test
//    public void GMDOutputSchemaTest() {
//
//	try {
//
//	    GetRecords getRecords = new GetRecords();
//	    getRecords.setResultType(ResultType.RESULTS);
//	    getRecords.setOutputSchema(CommonNameSpaceContext.GMD_NS_URI);
//
//	    InputStream inputStream = asInputStream(getRecords);
//
//	    WebRequest webRequest = WebRequest.create("http://localhost/cwiso", inputStream);
//
//	    ResultSet<GSResource> resultSet = new ResultSet<>();
//
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title1");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata("xxx");
//		originalMetadata.setSchemeURI("xxx");
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title2");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMDMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title3");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMIMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(webRequest);
//
//	    CSWProfiler mapper = new CSWISOProfiler();
//	    mapper.handle(webRequest);
//
//	} catch (Exception e) {
//
//	    e.printStackTrace();
//	    fail("Exception thrown");
//	}
//    }
//
//    @Test
//    public void GMIOutputSchemaTest() {
//
//	try {
//
//	    GetRecords getRecords = new GetRecords();
//	    getRecords.setResultType(ResultType.RESULTS);
//	    getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);
//
//	    InputStream inputStream = asInputStream(getRecords);
//
//	    WebRequest webRequest = WebRequest.create("http://localhost/cwiso", inputStream);
//
//	    ResultSet<GSResource> resultSet = new ResultSet<>();
//
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title1");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata("xxx");
//		originalMetadata.setSchemeURI("xxx");
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title2");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMDMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title3");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMIMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(webRequest);
//
//	    CSWProfiler mapper = new CSWISOProfiler();
//	    mapper.handle(webRequest);
//
//	} catch (Exception e) {
//
//	    e.printStackTrace();
//	    fail("Exception thrown");
//	}
//    }
//
//    @Test
//    public void HITSResultTypeTest() {
//
//	try {
//
//	    GetRecords getRecords = new GetRecords();
//	    getRecords.setResultType(ResultType.HITS);
//	    getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);
//
//	    InputStream inputStream = asInputStream(getRecords);
//
//	    WebRequest webRequest = WebRequest.create("http://localhost/cwiso", inputStream);
//
//	    ResultSet<GSResource> resultSet = new ResultSet<>();
//
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title1");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata("xxx");
//		originalMetadata.setSchemeURI("xxx");
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title2");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMDMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    {
//		Dataset dataset = new Dataset();
//
//		HarmonizedMetadata harmonizedMetadata = new HarmonizedMetadata();
//		CoreMetadata coreMetadata = new CoreMetadata();
//		coreMetadata.setTitle("Title3");
//		harmonizedMetadata.setCoreMetadata(coreMetadata);
//		dataset.setHarmonizedMetadata(harmonizedMetadata);
//
//		OriginalMetadata originalMetadata = new OriginalMetadata();
//		originalMetadata.setMetadata(coreMetadata.getMIMetadata().asString(true));
//		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMI_NS_URI);
//		dataset.setOriginalMetadata(originalMetadata);
//
//		resultSet.getResultsList().add(dataset);
//	    }
//	    // ----------------------------------------------
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(webRequest);
//
//	    CSWProfiler mapper = new CSWISOProfiler();
//	    mapper.handle(webRequest);
//
//	} catch (Exception e) {
//
//	    e.printStackTrace();
//	    fail("Exception thrown");
//	}
//    }
//
//    @Test
//    public void GetRecordByIdGETTest() {
//
//	try {
//
//	    WebRequest webRequest = WebRequest.create(
//		    "http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=X&outputFormat=text/xml&outputSchema=http://www.opengis.net/cat/csw/2.0.2&ElementSetName=full");
//
//	    ResultSet<GSResource> resultSet = new ResultSet<>();
//
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(webRequest);
//
//	    CSWProfiler mapper = new CSWISOProfiler();
////	    mapper.handle(webRequest);
//
//	} catch (Exception e) {
//
//	    e.printStackTrace();
//	    fail("Exception thrown");
//	}
//    }
//
//    @Test
//    public void GetRecordByIdPOSTTest() {
//
//	try {
//
//	    GetRecordById getRecordsById = new GetRecordById();
//	    InputStream inputStream = asInputStream(getRecordsById);
//
//	    WebRequest webRequest = WebRequest.create("http://localhost/cwiso", inputStream);
//	    ResultSet<GSResource> resultSet = new ResultSet<>();
//
//	    DiscoveryMessage message = new DiscoveryMessage();
//	    message.setWebRequest(webRequest);
//
//	    CSWProfiler mapper = new CSWISOProfiler();
//	    mapper.handle(webRequest);
//
//	} catch (Exception e) {
//
//	    e.printStackTrace();
//	    fail("Exception thrown");
//	}
//    }
//
//    private InputStream asInputStream(Object getRecords) throws JAXBException {
//
//	Marshaller marshaller = CommonContext.createMarshaller(true);
//	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//	marshaller.marshal(getRecords, outputStream);
//	return new ByteArrayInputStream(outputStream.toByteArray());
//    }
//
//}
