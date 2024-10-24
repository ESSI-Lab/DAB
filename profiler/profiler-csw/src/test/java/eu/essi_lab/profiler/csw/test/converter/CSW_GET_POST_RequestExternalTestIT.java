/**
 * 
 */
package eu.essi_lab.profiler.csw.test.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecord;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecordResponse;
import eu.essi_lab.jaxb.csw._2_0_2.GetCapabilities;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordByIdResponse;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordsResponse;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionReport;
import eu.essi_lab.jaxb.ows._1_0_0.ExceptionType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.CSWRequestConverter;

/**
 * @author Fabrizio
 */
public class CSW_GET_POST_RequestExternalTestIT {

    private static final String TARGET = "https://gs-service-production.geodab.eu/gs-service/services/csw?";
    // private static final String TARGET = "http://localhost:9090/gs-service/services/csw?";

    @SuppressWarnings("unchecked")
    @Test
    public void getRecordsTest() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetRecords&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "startPosition=1&";

	getRequest += "maxRecords=1&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "resultType=results&";

	getRequest += "typeNames=gmd:MD_Metadata&";

	getRequest += "elementSetName=brief&";

	getRequest += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();
	GetRecordsResponse getRecordsResponseFromGET = CommonContext.unmarshal(bodyFromGET, GetRecordsResponse.class);

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(getRequest);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ByteArrayInputStream getRecordsStream = CommonContext.asInputStream(getRecords, false);

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, TARGET, getRecordsStream);

	HttpResponse<InputStream> responseFromPOST = downloader.downloadResponse(postRequest);
	InputStream bodyFromPOST = responseFromPOST.body();

	GetRecordsResponse getRecordsResponseFromPOST = CommonContext.unmarshal(bodyFromPOST, GetRecordsResponse.class);

	//
	//
	//

	List<Object> aniesFromGET = getRecordsResponseFromGET.getSearchResults().getAnies();
	assertEquals(1, aniesFromGET.size());

	List<Object> aniesFromPOST = getRecordsResponseFromPOST.getSearchResults().getAnies();
	assertEquals(1, aniesFromPOST.size());

	//
	//
	//

	JAXBElement<MIMetadataType> elFromGET = (JAXBElement<MIMetadataType>) aniesFromGET.get(0);
	JAXBElement<MIMetadataType> elFromPOST = (JAXBElement<MIMetadataType>) aniesFromPOST.get(0);

	String miFromGETString = CommonContext.asString(elFromGET, false);
	String miFromPOSTString = CommonContext.asString(elFromPOST, false);

	assertEquals(miFromGETString, miFromPOSTString);
    }

    @Test
    public void getRecordsTest_1() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetRecords&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "startPosition=1&";

	getRequest += "maxRecords=1&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "resultType=results&";

	getRequest += "typeNames=gmd:MD_Metadata&";

	getRequest += "elementSetName=none&";

	getRequest += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	ExceptionReport exceptionReport = CommonContext.unmarshal(bodyFromGET, ExceptionReport.class);

	List<ExceptionType> exception = exceptionReport.getException();

	assertEquals(1, exception.size());

	List<String> exceptionText = exception.get(0).getExceptionText();

	assertEquals(1, exceptionText.size());

	assertTrue(exceptionText.get(0).contains("Unrecognize set name: none"));
    }

    @Test
    public void getRecordsTest_2() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetRecords&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "startPosition=1&";

	getRequest += "maxRecords=1&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "resultType=results&";

	getRequest += "typeNames=gmd:MD_Metadata&";

	getRequest += "elementSetName=brief&";

	getRequest += "sortBy=xxx&";

	getRequest += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	ExceptionReport exceptionReport = CommonContext.unmarshal(bodyFromGET, ExceptionReport.class);

	List<ExceptionType> exception = exceptionReport.getException();

	assertEquals(1, exception.size());

	List<String> exceptionText = exception.get(0).getExceptionText();

	assertEquals(1, exceptionText.size());

	assertTrue(exceptionText.get(0).contains("SortBy element not valid"));
    }

    @Test
    public void getRecordsTest_3() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetRecords&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "startPosition=1&";

	getRequest += "maxRecords=1&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "resultType=results&";

	getRequest += "typeNames=gmd:MD_Metadata&";

	getRequest += "elementSetName=brief&";

	getRequest += "sortBy=title:x&";

	getRequest += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	ExceptionReport exceptionReport = CommonContext.unmarshal(bodyFromGET, ExceptionReport.class);

	List<ExceptionType> exception = exceptionReport.getException();

	assertEquals(1, exception.size());

	List<String> exceptionText = exception.get(0).getExceptionText();

	assertEquals(1, exceptionText.size());

	assertTrue(exceptionText.get(0).contains("SortOrderType not valid"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getRecordsByIdTest() throws Exception {

	Downloader downloader = new Downloader();

	String id = "chinageoss";

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetRecordById&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "id=" + id + "&";

	getRequest += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	GetRecordByIdResponse getRecordByIdResponseFromGET = CommonContext.unmarshal(//
		bodyFromGET, //
		GetRecordByIdResponse.class);

	//
	//
	//

	GetRecordById getRecordById = new GetRecordById();
	getRecordById.setOutputFormat("text/xml");
	getRecordById.setOutputSchema("http://www.isotc211.org/2005/gmd");
	getRecordById.getIds().add(id);

	ByteArrayInputStream getRecordsStream = CommonContext.asInputStream(getRecordById, false);

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, TARGET, getRecordsStream);

	HttpResponse<InputStream> responseFromPOST = downloader.downloadResponse(postRequest);
	InputStream bodyFromPOST = responseFromPOST.body();

	GetRecordByIdResponse getRecordByResponseFromPOST = CommonContext.unmarshal(//
		bodyFromPOST, //
		GetRecordByIdResponse.class);

	//
	//
	//

	JAXBElement<MIMetadataType> elFromGET = (JAXBElement<MIMetadataType>) getRecordByIdResponseFromGET.getAnies().get(0);
	JAXBElement<MIMetadataType> elFromPOST = (JAXBElement<MIMetadataType>) getRecordByResponseFromPOST.getAnies().get(0);

	String miFromGETString = CommonContext.asString(elFromGET, false);
	String miFromPOSTString = CommonContext.asString(elFromPOST, false);

	assertEquals(miFromGETString, miFromPOSTString);
    }

    @Test
    public void describeRecordTest() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=DescribeRecord&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2&";

	getRequest += "outputFormat=text/xml&";

	getRequest += "typeName=gmd:MD_Metadata";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	DescribeRecordResponse descResponseResponseFromGET = CommonContext.unmarshal(//
		bodyFromGET, //
		DescribeRecordResponse.class);

	//
	//
	//

	//
	//
	//

	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("text/xml");
	describeRecord.getTypeNames().add(new QName(//
		CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	ByteArrayInputStream descRecordStream = CommonContext.asInputStream(describeRecord, false);

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, TARGET, descRecordStream);

	HttpResponse<InputStream> responseFromPOST = downloader.downloadResponse(postRequest);
	InputStream bodyFromPOST = responseFromPOST.body();

	DescribeRecordResponse descRecordResponseFromPOST = CommonContext.unmarshal(//
		bodyFromPOST, //
		DescribeRecordResponse.class);

	//
	//
	//

	String descResponseFromGETString = CommonContext.asString(descResponseResponseFromGET, false);
	String descResponseFromPOSTString = CommonContext.asString(descRecordResponseFromPOST, false);

	assertEquals(descResponseFromGETString, descResponseFromPOSTString);
    }

    @Test
    public void getCapabilitiesTest() throws Exception {

	Downloader downloader = new Downloader();

	//
	//
	//

	String getRequest = TARGET;

	getRequest += "request=GetCapabilities&";

	getRequest += "service=CSW&";

	getRequest += "version=2.0.2";

	//
	//
	//

	HttpResponse<InputStream> responseFromGET = downloader.downloadResponse(getRequest);

	InputStream bodyFromGET = responseFromGET.body();

	Capabilities getCapResponseFromGET = CommonContext.unmarshal(//
		bodyFromGET, //
		Capabilities.class);

	//
	//
	//

	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("csw");

	ByteArrayInputStream getCapStream = CommonContext.asInputStream(getCapabilities, false);

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, TARGET, getCapStream);

	HttpResponse<InputStream> responseFromPOST = downloader.downloadResponse(postRequest);
	InputStream bodyFromPOST = responseFromPOST.body();

	Capabilities getCapResponseFromFromPOST = CommonContext.unmarshal(//
		bodyFromPOST, //
		Capabilities.class);

	//
	//
	//

	String getCapResponseFromGETString = CommonContext.asString(getCapResponseFromGET, false);
	String getCapResponseFromPOSTString = CommonContext.asString(getCapResponseFromFromPOST, false);

	assertEquals(getCapResponseFromGETString, getCapResponseFromPOSTString);
    }

}
