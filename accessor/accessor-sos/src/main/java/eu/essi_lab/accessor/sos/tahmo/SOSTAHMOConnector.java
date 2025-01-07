package eu.essi_lab.accessor.sos.tahmo;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import dev.failsafe.FailsafeException;
import eu.essi_lab.accessor.sos.SOSConnector;
import eu.essi_lab.accessor.sos.SOSNamespaceContext;
import eu.essi_lab.accessor.sos.SOSRequestBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.sos._2_0.swes_2.AbstractOfferingType;
import eu.essi_lab.jaxb.sos.factory.JAXBSOS;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.protocols.NetProtocols;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SOSTAHMOConnector extends SOSConnector {

    /**
     * @author Fabrizio
     */
    public class SOSTTAHMORequestBuilder extends SOSRequestBuilder {

	/**
	 * @param serviceUrl
	 * @param version
	 */
	public SOSTTAHMORequestBuilder(String serviceUrl, String version) {
	    super(serviceUrl, version);
	    this.serviceUrl = addCredentialsInRequests(serviceUrl);
	}

	@Override
	public String createCapabilitiesRequest() {
	    String ret = super.createCapabilitiesRequest();
	    ret = getXMLOutputRequest(ret);
	    return ret;

	}

	@Override
	public String createDataRequest(String procedure, String featureIdentifier, String property, Date begin, Date end) {
	    String ret = "";
	    String baseEndpoint = createBaseRequest();
	    String temporalFilter = "";
	    if (begin != null && end != null) {
		String beginStr = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(begin);
		String filter = "om:phenomenonTime,";
		if (end.equals(begin)) {
		    Calendar c = Calendar.getInstance();
		    c.setTime(end);
		    c.add(Calendar.HOUR, 24);
		    end = c.getTime();
		    c.setTime(begin);
		    c.add(Calendar.HOUR, 12);
		    begin = c.getTime();
		    beginStr = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(begin);
		    filter += beginStr + "/" + ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(end); // an
													     // instant
													     // has been
													     // required
		} else {
		    String endStr = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(end);
		    filter += beginStr + "/" + endStr; // a period has been required
		}
		temporalFilter = "&temporalFilter=" + encode(filter);
	    }
	    ret = baseEndpoint + "&REQUEST=GetObservation&featureOfInterest=" + encode(procedure) + "&observedProperty=" + encode(property)
		    + "&procedure=" + encode(procedure) + temporalFilter;
	    ret = getXMLOutputRequest(ret);
	    return ret;
	}

	@Override

	public String createDataAvailabilityRequest(String procedure, String feature, String observedProperty) {
	    String ret = super.createDataAvailabilityRequest(procedure, feature, observedProperty);
	    ret = getXMLOutputRequest(ret);
	    return ret;
	}

	@Override
	public String createFeaturesRequest(String procedure) {
	    String ret = super.createFeaturesRequest(procedure);
	    ret = getXMLOutputRequest(ret);
	    return ret;
	}

	@Override
	public String createProcedureDescriptionRequest(String procedure, String procedureFormat) {
	    String ret = super.createProcedureDescriptionRequest(procedure, procedureFormat);
	    ret = getXMLOutputRequest(ret);
	    return ret;
	}

	//
	// @Override
	// public String addCredentialsInRequests(String url) {
	// return url.replace(HTTP, HTTP + TAHMO_CREDENTIALS + "@");
	// }

	//
	// @Override
	// public String removeCredentialsInRequests(String url) {
	// if (url.contains(TAHMO_CREDENTIALS)) {
	// String[] splittedString = url.split("@");
	// if (splittedString.length > 1) {
	// url = HTTP + splittedString[1];
	// }
	// }
	// return url;
	// }

    }

    /**
     * 
     */
    public static final String TYPE = "SOS TAHMO Connector";

    @Override
    public String getDownloadProtocol() {
	return NetProtocols.SOS_2_0_0_TAHMO.getCommonURN();
    }

    /**
     * TAHMO service replies with a capabilities indicating UTF-16 as the encoding. However it is not UTF-16, it is
     * UTF-8! The default unmarshaller isn't able to unmarshal it.
     */

    @Override
    public Object unmarshal(File tmpFile) throws Exception {
	try (FileInputStream stream = new FileInputStream(tmpFile)) {
	    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
	    Object ret = null;
	    ret = JAXBSOS.getInstance().unmarshal(reader);
	    return ret;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(SOSTAHMOConnector.class).error("Error unmarshalling file: {}", tmpFile.getPath());
	    throw e;
	}

    }

    /**
     * TAHMO service replies with XML documents indicating UTF-16 as the encoding. However it is not UTF-16, it is
     * UTF-8! The default unmarshaller isn't able to unmarshal it.
     */

    @Override
    public Object unmarshal(InputStream stream) throws Exception {
	InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
	Object ret = JAXBSOS.getInstance().unmarshal(reader);
	reader.close();
	stream.close();
	return ret;
    }

    @Override

    public void modifyCapabilitiesResponse(File tmpFile) throws Exception {

	try (FileInputStream stream = new FileInputStream(tmpFile)) {
	    XMLDocumentReader xmlReader = new XMLDocumentReader(stream);
	    xmlReader.setNamespaceContext(new SOSNamespaceContext());
	    XMLDocumentWriter writer = new XMLDocumentWriter(xmlReader);
	    writer.rename("//*:ObservationOfferingType", "sos:ObservationOffering");
	    try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
		ByteArrayInputStream input = xmlReader.asStream();
		IOUtils.copy(input, fos);
		input.close();
	    }
	}

    }

    @Override

    public InputStream modifyObservationResponse(InputStream stream) throws Exception {
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	reader.getDocument().getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi",
		"http://www.w3.org/2001/XMLSchema-instance");
	reader.setNamespaceContext(new SOSNamespaceContext());
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	Node[] nodes = reader.evaluateNodes("//*:procedure");
	for (Node node : nodes) {
	    String text = reader.evaluateString(node, ".");
	    writer.setText(node, ".", "");
	    XMLNodeReader nr = new XMLNodeReader(node);
	    XMLNodeWriter nw = new XMLNodeWriter(nr);
	    nw.addAttributesNS(".", CommonNameSpaceContext.XLINK_NS_URI, "xlink:title", text);
	}
	Node[] nodesResult = reader.evaluateNodes("//*:result");
	for (Node node : nodesResult) {
	    XMLNodeReader nr = new XMLNodeReader(node);
	    XMLNodeWriter nw = new XMLNodeWriter(nr);
	    nw.addAttributesNS(".", CommonNameSpaceContext.XSI_SCHEMA_INSTANCE_NS_URI, "xsi:type", "gml:MeasureType");
	    nw.addAttributes(".", "uom", "unkown");
	}
	return reader.asStream();
    }

    @Override
    public InputStream modifyFeatureResponse(InputStream stream) throws Exception {
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	reader.setNamespaceContext(new SOSNamespaceContext());
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:identifier", "gml:identifier");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:description", "gml:description");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:name", "gml:name");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:type", "sam:type");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:sampledFeature", "sam:sampledFeature");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape", "sams:shape");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape/*:Point", "gml:Point");
	writer.rename("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape/*:Point/*:pos", "gml:pos");
	String srs = reader
		.evaluateString("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape/*:Point/*:srsName");
	if (srs != null && !srs.isEmpty()) {
	    writer.addAttributes("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape/*:Point", "srsName",
		    srs);
	}
	writer.remove("/*:GetFeatureOfInterestResponse/*:featureMember/*:SF_SpatialSamplingFeature/*:shape/*:Point/*:srsName");
	stream.close();
	return reader.asStream();
    }

    /**
     * By default TAHMO service replies with a JSON based capabilities document. To avoid, this parameter must be
     * specified in each request
     */

    private String getXMLOutputRequest(String ret) {
	ret += "&ResponseFormat=application/xml";
	return ret;
    }

    /**
     * @return
     */
    public SOSRequestBuilder createRequestBuilder() {

	return new SOSTTAHMORequestBuilder(getSourceURL(), "2.0.0");
    }

    /**
     * No description format is shown in the offering. A fixed format is used
     */

    @Override
    public List<String> getProcedureDescriptionFormats(AbstractOfferingType abstractOffering) {
	List<String> ret = new ArrayList<>();
	ret.add("http://www.opengis.net/sensorml/2.0");
	return ret;
    }

    @Override
    protected boolean isLatLon() {
	return false;
    }

    /**
     * @param url
     * @return
     * @throws GSException
     */
    protected HttpResponse<InputStream> downloadResponseWithRetry(String url) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);
	downloader.setRetryPolicy(5, TimeUnit.SECONDS, 5);

	try {

	    return downloader.downloadResponse(HttpRequestUtils.build(//
		    MethodNoBody.GET, //
		    url.trim(), //
		    HttpHeaderUtils.build(//
			    "Authorization", //
			    "Bearer " + ConfigurationWrapper.getCredentialsSetting().getSOSTahmoToken().orElse(""))));

	} catch (FailsafeException | IOException | InterruptedException | URISyntaxException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_CONNECTOR_DOWNLOAD_ERROR);
	}
    }

    @Override
    public String getType() {

	return TYPE;
    }

    public static void main(String[] args) {

	String baseURL = "http://hnapi.hydronet.com/api/service/sos?";
	String xmlRequest = "&ResponseFormat=application/xml";
	String getCapabilitiesRequest = "Version=2&Request=GetCapabilities";
	String getFOIRequest = "Version=2&Request=GetFeatureOfInterest&procedure=Tahmo.Stations.Data.Distribution.Measurements_TA00241&version=2.0.0";
	String getObservationRequest = "service=SOS&request=getobservation&procedure=Tahmo.Stations.Data.Distribution.Measurements_TA00241&Version=2.0.0&ObservedProperty=P&temporalfilter=phenomenonTime,2018-10-05T09%3A08%3A27.000Z%2F2018-11-06T01%3A38%3A23.000Z&featureofinterest=Tahmo.Stations.Data.Distribution.Measurements_TA00241";
	String describeSensorRequest = "service=SOS&request=describesensor&procedure=Tahmo.Stations.Data.Distribution_TA00302&version=2.0.0";
	String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImIyY2I4NjU2NjNlY2RiYzEyMGZkOGViYzFkM2ExOGIwIiwidHlwIjoiSldUIn0.eyJuYmYiOjE1NzE4MzEzOTksImV4cCI6MTcyMzA2MjYyOSwiaXNzIjoiaHR0cHM6Ly9vaWRjLmh5ZHJvbmV0LmNvbSIsImF1ZCI6Imh0dHBzOi8vb2lkYy5oeWRyb25ldC5jb20vcmVzb3VyY2VzIiwiY2xpZW50X2lkIjoiaG40cy1wcm9kdWN0aW9uIiwiY2xpZW50X3Byb2ZpbGUiOiJwcm9maWxlIiwiY2xpZW50X2VtYWlsIjoiZW1haWwiLCJjbGllbnRfbmFtZSI6Im5hbWUiLCJjbGllbnRfc3RyaW5nIjoiaWRfdG9rZW4gdG9rZW4iLCJjbGllbnRfb2ZmbGluZV9hY2Nlc3MiOiJvZmZsaW5lX2FjY2VzcyIsInN1YiI6IjQzYmEwNjhjLTI3NzYtNDg1My1hYjc5LTIyMzIxMTE3NzZhMCIsImF1dGhfdGltZSI6MTU3MTgzMTM5OSwiaWRwIjoibG9jYWwiLCJqdGkiOiIyNTg1M2UwNjRjMGFhYjg2N2RmZTgyNjdjNzBmZTcyMCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJvZmZsaW5lX2FjY2VzcyJdLCJhbXIiOlsicHdkIl19.PhkJ3SVi2ZjBEan9OlV9qpFAV3fqjsPQqQv-gk_ZtWlIuwTvoPMt5whwAa07opUNV5tO_-Jk4B8R-W9x2znMmzA_d9Bjyiwrwto3GgaUBBryXR-GIs21Dy1Hj62qUvUDpI07EQhGeq7SpxYeO0WdK_t-5U-3w3y9WNihvmcyfMsJukw9AOsSObEPUY6YUiTv71vbqKPQc55pbuPHpaDRHZITH0Nps3E_jrnwn9Aepz7B7MDVXyM_vU1Vb-MzAVZq03XvCf_YdWWxQUqPhbBVHcJjfzSQGrFl-8pGMLkHvcx1PgEnrxN9y7JS6tP_D7rZMBwA4vaiHXX5Z1XdBsvwYg";

	try {
	    // getCapabilities
	    HashMap<String, String> headers = new HashMap<String, String>();
	    headers.put("Authorization", "Bearer " + token);

	    Optional<InputStream> response = new Downloader().downloadOptionalStream(//
		    baseURL + getCapabilitiesRequest + xmlRequest, //
		    HttpHeaderUtils.build(headers));

	    InputStream is = response.get();

	    XMLDocumentReader xdoc = new XMLDocumentReader(is);

	    String res = xdoc.asString();

	    System.out.println("GET CAPABILITIES");
	    System.out.println(res);

	    // getFOI
	    Optional<InputStream> foiResponse = new Downloader().downloadOptionalStream(//
		    baseURL + getFOIRequest + xmlRequest, //
		    HttpHeaderUtils.build(headers));

	    InputStream foiIs = foiResponse.get();

	    XMLDocumentReader foiXdoc = new XMLDocumentReader(foiIs);

	    String foiRes = foiXdoc.asString();
	    System.out.println("GET FOI");
	    System.out.println(foiRes);

	    // getObs
	    Optional<InputStream> obsResponse = new Downloader().downloadOptionalStream(//
		    baseURL + getObservationRequest + xmlRequest, //
		    HttpHeaderUtils.build(headers));

	    InputStream obsIs = obsResponse.get();

	    XMLDocumentReader obsXdoc = new XMLDocumentReader(obsIs);

	    String obsRes = obsXdoc.asString();

	    System.out.println("GET OBS");
	    System.out.println(obsRes);

	    // getDescribe

	    Optional<InputStream> desResponse = new Downloader().downloadOptionalStream(//
		    baseURL + describeSensorRequest + xmlRequest, //
		    HttpHeaderUtils.build(headers));

	    InputStream desIs = desResponse.get();

	    XMLDocumentReader desXdoc = new XMLDocumentReader(desIs);

	    String desRes = desXdoc.asString();
	    System.out.println("GET DESCRIPTION");
	    System.out.println(desRes);

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (TransformerException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
