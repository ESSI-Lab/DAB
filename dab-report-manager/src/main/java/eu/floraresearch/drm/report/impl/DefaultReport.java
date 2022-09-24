package eu.floraresearch.drm.report.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.net.utils.HttpRequestExecutor;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.floraresearch.drm.ConfigReader;
import eu.floraresearch.drm.report.AbstractReport;

public class DefaultReport extends AbstractReport {

    private int totalRecords;
    private int geossDataCoreRecords;

    private int newRecords;
    private int deletedRecords;
    private int notValidated;

    public DefaultReport() {

	totalRecords = -1;
	geossDataCoreRecords = -1;

	newRecords = -1;
	deletedRecords = -1;
	notValidated = -1;
    }

    @Override
    public String getShortName() {

	return "";
    }

    @Override
    public int getCategory_1_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(1)) {
	    return 0;
	}

	if (totalRecords == -1) {

	    // String query = allRecordsTemplate.replace("IDENTIFIER", catalog.getId());

	    totalRecords = getRecordsCount(false);
	    // totalRecords = execPostQuery(query);
	}

	return totalRecords;
    }

    @Override
    public int getCategory_2_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(2)) {
	    return 0;
	}

	if (geossDataCoreRecords == -1) {

	    geossDataCoreRecords = getRecordsCount(true);
	}

	return geossDataCoreRecords;
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(3)) {
	    return 0;
	}

	return getCategory_1_Value();
    }

    @Override
    public int getCategory_4_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(4)) {
	    return 0;
	}

	return getCategory_2_Value();
    }

    @Override
    public int getCategory_6_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(6)) {
	    return 0;
	}

	return getValidCount(true, true);
    }

    @Override
    public int getCategory_7_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(7)) {
	    return 0;
	}

	return getValidCount(true, false);
    }

    @Override
    public int getCategory_8_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(8)) {
	    return 0;
	}

	return getValidCount(false, true);
    }

    @Override
    public int getCategory_9_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(9)) {
	    return 0;
	}

	return getValidCount(false, false);
    }

    @Override
    public int getCategory_10_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(10)) {
	    return 0;
	}

	if (newRecords > -1) {
	    return newRecords;
	}

	String query = newRecordsTemplate.replace("IDENTIFIER", source.getUniqueIdentifier());

	String fromDate = ConfigReader.getInstance().readNewFromDate();
	long time = ISO8601DateTimeUtils.parseISO8601(fromDate).getTime();

	query = query.replace("DATESTAMP", String.valueOf(time));

	return newRecords = execPostQuery(query);
    }

    @Override
    public int getCategory_11_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(11)) {
	    return 0;
	}

	if (deletedRecords > -1) {
	    return deletedRecords;
	}

	String query = deletedRecordsTemplate.replace("IDENTIFIER", source.getUniqueIdentifier());
	return deletedRecords = execPostQuery(query);
    }

    @Override
    public int getCategory_12_Value() throws Exception {

	if (!ConfigReader.getInstance().readShowCategory(12)) {
	    return 0;
	}

	if (notValidated > -1) {
	    return notValidated;
	}

	String query = notValidatedRecordsTemplate.replace("IDENTIFIER", source.getUniqueIdentifier());
	return notValidated = execPostQuery(query);
    }

    @Override
    public String getComments() {

	return "Number of records and granules harvested by GEODAB";
    }

    protected int getRecordsCount(boolean isGdc) throws Exception {

	String gdc = isGdc ? "&gdc=true" : "";

	String reqID = UUID.randomUUID().toString();

	String dabEndpoint = ConfigReader.getInstance().readDABEndpoint();

	String id = source.getUniqueIdentifier();

	dabEndpoint = createQuery(dabEndpoint, reqID, id, gdc);

	Downloader request = new Downloader();
	InputStream is = request.downloadStream(dabEndpoint).get();

	try {

	    XMLDocumentReader doc = new XMLDocumentReader(is);
	    String res = doc.evaluateString("//*:totalResults");
	    return Integer.valueOf(res);

	} catch (Exception ex) {

	    ex.printStackTrace();
	}

	return 0;
    }

    protected int getValidCount(boolean valid, boolean yes) throws Exception {

	String query = new String(valid ? validRecordsTemplate : repairedRecordsTemplate);

	query = query.replace("IDENTIFIER", source.getUniqueIdentifier());

	query = query.replace("VALUE", yes ? "true" : "false");

	return execPostQuery(query);

	// String value = yes ? "true" : "false";
	// String param = valid ? "&vld=" : "&rep=";
	//
	// String reqID = UUID.randomUUID().toString();
	//
	// String dabEndpoint = cleanDABEndpoint();
	//
	// String id = catalog.isHarvested() ? catalog.getSourceID() : catalog.getSourceID();
	//
	// dabEndpoint = createValidQuery(dabEndpoint, reqID, id, param, value);
	//
	// GetRequest request = new GetRequest(dabEndpoint);
	//
	// CommonLogger.getInstance().main(this, "Performing request: " + dabEndpoint);
	//
	// Response resp = request.execRequest();
	// InputStream is = resp.getResponseBodyAsStream();
	//
	// XMLDocument doc = new XMLDocument(is);
	// String res = doc.evaluateXPath("//*:totalResults").asString();
	//
	// CommonLogger.getInstance().main(this, "Request result: " + res);
	//
	// return Integer.valueOf(res);
    }

    protected String createQuery(String dabEndpoint, String reqID, String id, String gdc) {
	
//	http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?reqID=expand_svrw3ivlcw9&si=1&ct=500&parents=ROOT

	return dabEndpoint + "services/essi/view/geoss/opensearch/query?&reqID=" + reqID + "&sources=" + id + gdc + "&ct=1"
		+ "&tf=keyword,format,protocol,providerID,organisationName";
    }

    protected String createValidQuery(String dabEndpoint, String reqID, String id, String param, String value) {

	return dabEndpoint + "services/essi/view/geoss/opensearch/query?&reqID=" + reqID + "&sources=" + id + param + value
		+ "&ct=1&outputFormat=application%2Fatom%2Bxml";
    }

    protected int execPostQuery(String query) throws Exception {

	String dabEndpoint = ConfigReader.getInstance().readDABEndpoint() + "services/essi/cswiso";

	HttpRequestExecutor executor = new HttpRequestExecutor();

	HttpPost httpPost = new HttpPost(dabEndpoint);

	InputStream stream = new ByteArrayInputStream(query.getBytes("UTF-8"));

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	IOUtils.copy(stream, baos);

	ByteArrayEntity inputStreamEntity = new ByteArrayEntity(baos.toByteArray());

	httpPost.setEntity(inputStreamEntity);

	httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

	HttpResponse response = executor.execute(httpPost);

	XMLDocumentReader reader = new XMLDocumentReader(response.getEntity().getContent());

	String matched = reader.evaluateString("//@numberOfRecordsMatched");

	return Integer.valueOf(matched);
    }
    
    /**
     * 
     * @return
     */
    protected Downloader createDownloader() {
	
	Downloader downloader = new Downloader();
	downloader.setTimeout(10000);
    
	return downloader;
    }

    public static void main(String[] args) throws ParseException {

	// WIS: 2014-12-23T17:19:06Z
	// IRIS STATION: 2015-01-10
	// PANGAEA: 2015-01-29

	Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.ITALY);
	long time = ISO8601DateTimeUtils.parseISO8601("2015-02-01").getTime();
	System.out.println(time);

	// System.out.println(new Date(1422549801000l));

    }

    @Override
    protected void cleanCache() {
	totalRecords = -1;
	geossDataCoreRecords = -1;

	newRecords = -1;
	deletedRecords = -1;
	notValidated = -1;

    }

}
