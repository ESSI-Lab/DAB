package eu.essi_lab.accessor.wof.stress;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class HydroStressTest {

    public static void main(String[] args) throws Exception {
	int maxStations = 10;

	String[] sourceCodes = new String[] { "brazil-inmet" };
	List<SourceReport> reports = getSourceReports(maxStations, sourceCodes);
	// List<SourceReport> reports = getSourceReports(maxStations,null);
	for (SourceReport report : reports) {
	    System.out.println(report.getSourceName());
	    System.out.println(report.getSourceURL());
	    System.out.println(report.getMeanTime());
	    for (StationReport stationReport : report.getStationReports()) {
		System.out.println("*" + stationReport.getName() + " " + stationReport.getMeanTime());
		List<VariableReport> variableReports = stationReport.getVariableReports();
		for (VariableReport variableReport : variableReports) {
		    System.out.println(variableReport.getName() + "," + variableReport.isSucceeded() + "," + variableReport.getTime());
		}
	    }

	}

    }

    private static List<SourceReport> getSourceReports(int maxStations, String[] sourceCodes) throws Exception {
	String url = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-plata/hiscentral.asmx/GetWaterOneFlowServiceInfo";

	Downloader wofInfoExecutor = new Downloader();
	HttpResponse<InputStream> wofInfoResponse = wofInfoExecutor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	XMLDocumentReader wofInfoReader = new XMLDocumentReader(wofInfoResponse.body());

	Node[] serviceNodes = wofInfoReader.evaluateNodes("//*:ServiceInfo");
	List<SourceReport> reports = new ArrayList<>();
	service: for (Node serviceNode : serviceNodes) {
	    String serviceURL = wofInfoReader.evaluateString(serviceNode, "*:servURL");
	    String id = serviceURL.substring(serviceURL.indexOf("gs-view-source"));
	    id = id.substring(id.indexOf("(") + 1, id.indexOf(")"));
	    if (sourceCodes != null) {
		boolean found = false;
		for (String sourceCode : sourceCodes) {
		    if (id.equals(sourceCode)) {
			found = true;
		    }
		}
		if (!found) {
		    continue service;
		}
	    }
	    serviceURL = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/gs-view-source(" + id + ")/cuahsi_1_1.asmx";
	    String serviceTitle = wofInfoReader.evaluateString(serviceNode, "*:Title");
	    SourceReport report = new SourceReport();
	    report.setSourceURL(serviceURL);
	    report.setSourceName(serviceTitle);
	    List<StationReport> stationReports = getStationReports(serviceURL, maxStations);
	    Long meanTime = 0l;
	    Long stations = 0l;
	    for (StationReport stationReport : stationReports) {
		Long tmpTime = stationReport.getMeanTime();
		if (tmpTime != 0) {
		    stations++;
		    meanTime += tmpTime;
		}
	    }
	    if (stations != 0) {
		meanTime = meanTime / stations;
	    }
	    report.setMeanTime(meanTime);
	    report.getStationReports().addAll(stationReports);

	    reports.add(report);
	}
	return reports;
    }

    private static List<StationReport> getStationReports(String url, int i) throws Exception {

	Downloader wofInfoExecutor = new Downloader();
	HttpResponse<InputStream> wofInfoResponse = wofInfoExecutor
		.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url + "?request=GetSitesObject&count=" + i));

	XMLDocumentReader wofInfoReader = new XMLDocumentReader(wofInfoResponse.body());

	Node[] stationNodes = wofInfoReader.evaluateNodes("//*:siteInfo");
	List<StationReport> reports = new ArrayList<>();
	for (Node stationNode : stationNodes) {
	    String stationName = wofInfoReader.evaluateString(stationNode, "*:siteName");
	    String stationCode = wofInfoReader.evaluateString(stationNode, "*:siteCode");
	    StationReport report = new StationReport();
	    report.setName(stationName);
	    report.setCode(stationCode);

	    List<VariableReport> variableReports = getVariableReports(url, stationCode);
	    Long meanTime = 0l;
	    Long variables = 0l;
	    for (VariableReport variableReport : variableReports) {
		if (variableReport.isSucceeded()) {
		    variables++;
		    meanTime += variableReport.getTime();
		}
	    }
	    if (variables != 0) {
		meanTime = meanTime / variables;
	    }
	    report.setMeanTime(meanTime);
	    report.getVariableReports().addAll(variableReports);

	    reports.add(report);
	}
	return reports;
    }

    private static List<VariableReport> getVariableReports(String serviceURL, String stationCode) throws Exception {
	String url = serviceURL + "?request=GetSiteInfoObject&site=" + stationCode;

	Downloader wofInfoExecutor = new Downloader();
	HttpResponse<InputStream> wofInfoResponse = wofInfoExecutor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	XMLDocumentReader wofInfoReader = new XMLDocumentReader(wofInfoResponse.body());
	Node[] variableNodes = wofInfoReader.evaluateNodes("//*:series");
	List<VariableReport> variableReports = new ArrayList<>();
	for (Node variableNode : variableNodes) {
	    String variableCode = wofInfoReader.evaluateString(variableNode, "*:variable/*:variableCode");
	    String variableName = wofInfoReader.evaluateString(variableNode, "*:variable/*:variableName");
	    String begin = wofInfoReader.evaluateString(variableNode, "*:variableTimeInterval/*:beginDateTimeUTC");
	    Date beginDate = ISO8601DateTimeUtils.parseISO8601ToDate(begin).get();
	    String end = wofInfoReader.evaluateString(variableNode, "*:variableTimeInterval/*:endDateTimeUTC");
	    Date endDate = ISO8601DateTimeUtils.parseISO8601ToDate(end).get();
	    VariableReport variableReport = new VariableReport();

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(endDate);
	    cal.add(Calendar.DATE, -5);
	    Date actualBegin = cal.getTime();
	    Date actualEnd = endDate;

	    String time1 = ISO8601DateTimeUtils.getISO8601DateTime(actualBegin);
	    String time2 = ISO8601DateTimeUtils.getISO8601DateTime(actualEnd);

	    String valueURL = serviceURL + "?request=GetValuesObject&site=" + stationCode + "&variable=" + variableCode + "&beginDate="
		    + time1 + "&endDate=" + time2;

	    long start = System.currentTimeMillis();
	    try {
		Downloader wofValueExecutor = new Downloader();
		HttpResponse<InputStream> wofValueResponse = wofValueExecutor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, valueURL));

		XMLDocumentReader wofValueReader = new XMLDocumentReader(wofValueResponse.body());
		Node[] nodes = wofValueReader.evaluateNodes("//*:value");
		if (nodes.length > 0) {
		    variableReport.setSucceeded(true);
		    long time = System.currentTimeMillis() - start;
		    variableReport.setTime(time);
		    System.out.println("Good response after ms: " + time);
		}

	    } catch (Exception e) {
		// TODO: handle exception
		System.err.println("Error occurred: " + e.getMessage());
	    }

	    variableReport.setName(variableName);
	    variableReport.setCode(variableCode);
	    variableReport.setBegin(beginDate);
	    variableReport.setEnd(endDate);
	    variableReports.add(variableReport);

	}
	return variableReports;
    }

    private static List<String> getStringsResponse(String url, String xpath) throws Exception {

	Downloader wofInfoExecutor = new Downloader();
	HttpResponse<InputStream> wofInfoResponse = wofInfoExecutor.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url));

	XMLDocumentReader wofInfoReader = new XMLDocumentReader(wofInfoResponse.body());
	Node[] urlNodes = wofInfoReader.evaluateNodes(xpath);
	List<String> urls = new ArrayList<>();
	for (Node urlNode : urlNodes) {
	    urls.add(wofInfoReader.evaluateString(urlNode, "."));
	}
	return urls;
    }
}
