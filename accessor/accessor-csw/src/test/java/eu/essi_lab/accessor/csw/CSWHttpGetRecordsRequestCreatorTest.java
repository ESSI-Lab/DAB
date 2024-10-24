package eu.essi_lab.accessor.csw;

import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

 
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.jaxb.csw._2_0_2.AbstractQueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class CSWHttpGetRecordsRequestCreatorTest {

    @Test
    public void testPost() throws GSException {

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	String postUrl = "http://example.com/Post";
	connector.getRecordsURLPOSTXML = postUrl;

	GetRecords getRecords = Mockito.mock(GetRecords.class);

	CSWHttpGetRecordsRequestCreator creator = Mockito
		.spy(new CSWHttpGetRecordsRequestCreator(CSWConnector.Binding.POST_XML, connector, getRecords));

	Assert.assertEquals(postUrl, creator.getGetRecordsUrl());

	Assert.assertNotNull(creator.getHttpRequest());

	Assert.assertTrue(creator.getHttpRequest() instanceof HttpRequest);

    }

    @Test
    public void testPostPort80() throws GSException {

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	String postUrl = "http://example.com/Post";
	String postUrlPort = "http://example.com:80/Post";

	connector.getRecordsURLPOSTXML = postUrlPort;

	GetRecords getRecords = Mockito.mock(GetRecords.class);

	CSWHttpGetRecordsRequestCreator creator = Mockito
		.spy(new CSWHttpGetRecordsRequestCreator(CSWConnector.Binding.POST_XML, connector, getRecords));

	Assert.assertEquals(postUrlPort, creator.getGetRecordsUrl());

	Assert.assertEquals(postUrl, creator.getHttpRequest().uri().toString());

	Assert.assertNotNull(creator.getHttpRequest());

	Assert.assertTrue(creator.getHttpRequest() instanceof HttpRequest);

    }

    @Test
    public void testGetPort80() throws GSException {

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doCallRealMethod().when(connector).normalizeURL(Mockito.any());

	Mockito.doCallRealMethod().when(connector).getConstraintLanguageParameter();

	String getUrl = "http://example.com/Get";

	String getUrlPort = "http://example.com:80/Get";

	connector.getRecordsURLGET = getUrlPort;

	GetRecords getRecords = Mockito.mock(GetRecords.class);

	Integer maxRecords = 50;

	Mockito.doReturn(new BigInteger("" + maxRecords)).when(getRecords).getMaxRecords();

	Integer start = 30;

	Mockito.doReturn(new BigInteger("" + start)).when(getRecords).getStartPosition();

	String outputformat = "application/xml";
	Mockito.doReturn(outputformat).when(getRecords).getOutputFormat();

	JAXBElement<? extends AbstractQueryType> jaxb = Mockito.mock(JAXBElement.class);
	QueryType abstractQuery = Mockito.mock(QueryType.class);

	ElementSetName esn = Mockito.mock(ElementSetName.class);

	String elementSetName = "full";

	ElementSetType value = ElementSetType.fromValue(elementSetName);

	Mockito.doReturn(value).when(esn).getValue();

	Mockito.doReturn(esn).when(abstractQuery).getElementSetName();

	List<QName> qNameList = new ArrayList<>();

	String uri = "uri";
	String local = "local";
	String prefix = "prefix";
	qNameList.add(new QName(uri, local, prefix));

	Mockito.doReturn(qNameList).when(abstractQuery).getTypeNames();

	Mockito.doReturn(abstractQuery).when(jaxb).getValue();

	Mockito.doReturn(jaxb).when(getRecords).getAbstractQuery();

	String schema = "schema";
	Mockito.doReturn(schema).when(getRecords).getOutputSchema();

	ResultType resultType = ResultType.RESULTS;
	Mockito.doReturn(resultType).when(getRecords).getResultType();

	CSWHttpGetRecordsRequestCreator creator = Mockito
		.spy(new CSWHttpGetRecordsRequestCreator(CSWConnector.Binding.GET, connector, getRecords));

	Assert.assertEquals(getUrlPort, creator.getGetRecordsUrl());

	Assert.assertNotNull(creator.getHttpRequest());

	Assert.assertTrue(creator.getHttpRequest() instanceof HttpRequest);

	String expectedRequest = getUrl + "?service=CSW&request=GetRecords&version=2.0" + ".2&outputFormat=" + outputformat
		+ "&outputSchema=" + schema + "&ElementSetName=" + elementSetName + "&resultType=" + resultType.value() + "&typeNames" + "="
		+ prefix + ":" + local + "&CONSTRAINTLANGUAGE=CQL_TEXT" + "&startPosition=" + start + "&maxRecords=" + maxRecords;

	Assert.assertEquals(expectedRequest, creator.getHttpRequest().uri().toString());

    }

    @Test
    public void testGet() throws GSException {

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doCallRealMethod().when(connector).normalizeURL(Mockito.any());

	Mockito.doCallRealMethod().when(connector).getConstraintLanguageParameter();

	String getUrl = "http://example.com/Get";
	connector.getRecordsURLGET = getUrl;

	GetRecords getRecords = Mockito.mock(GetRecords.class);

	Integer maxRecords = 50;

	Mockito.doReturn(new BigInteger("" + maxRecords)).when(getRecords).getMaxRecords();

	Integer start = 30;

	Mockito.doReturn(new BigInteger("" + start)).when(getRecords).getStartPosition();

	String outputformat = "application/xml";
	Mockito.doReturn(outputformat).when(getRecords).getOutputFormat();

	JAXBElement<? extends AbstractQueryType> jaxb = Mockito.mock(JAXBElement.class);
	QueryType abstractQuery = Mockito.mock(QueryType.class);

	ElementSetName esn = Mockito.mock(ElementSetName.class);

	String elementSetName = "full";

	ElementSetType value = ElementSetType.fromValue(elementSetName);

	Mockito.doReturn(value).when(esn).getValue();

	Mockito.doReturn(esn).when(abstractQuery).getElementSetName();

	List<QName> qNameList = new ArrayList<>();

	String uri = "uri";
	String local = "local";
	String prefix = "prefix";
	qNameList.add(new QName(uri, local, prefix));

	Mockito.doReturn(qNameList).when(abstractQuery).getTypeNames();

	Mockito.doReturn(abstractQuery).when(jaxb).getValue();

	Mockito.doReturn(jaxb).when(getRecords).getAbstractQuery();

	String schema = "schema";
	Mockito.doReturn(schema).when(getRecords).getOutputSchema();

	ResultType resultType = ResultType.RESULTS;
	Mockito.doReturn(resultType).when(getRecords).getResultType();

	CSWHttpGetRecordsRequestCreator creator = Mockito
		.spy(new CSWHttpGetRecordsRequestCreator(CSWConnector.Binding.GET, connector, getRecords));

	Assert.assertEquals(getUrl, creator.getGetRecordsUrl());

	Assert.assertNotNull(creator.getHttpRequest());

	Assert.assertTrue(creator.getHttpRequest() instanceof HttpRequest);

	String expectedRequest = getUrl + "?service=CSW&request=GetRecords&version=2.0" + ".2&outputFormat=" + outputformat
		+ "&outputSchema=" + schema + "&ElementSetName=" + elementSetName + "&resultType=" + resultType.value() + "&typeNames" + "="
		+ prefix + ":" + local + "&CONSTRAINTLANGUAGE=CQL_TEXT" + "&startPosition=" + start + "&maxRecords=" + maxRecords;

	Assert.assertEquals(expectedRequest, creator.getHttpRequest().uri().toString());

    }

}