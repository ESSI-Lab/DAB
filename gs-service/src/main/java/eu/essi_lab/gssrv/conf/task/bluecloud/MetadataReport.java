package eu.essi_lab.gssrv.conf.task.bluecloud;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public abstract class MetadataReport {

    private HashMap<String, ViewReport> viewReports = new HashMap<>();

    public MetadataReport(S3TransferWrapper manager, boolean uploadToS3, Integer maxRecords, String[] viewIdentifiers) throws Exception {
	/////////////////////////
	//// USER ARGUMENTS
	/////////////////////////

	List<ReportViews> views = getViews(); // ALL

	List<ReportViews> toRemove = new ArrayList<ReportViews>();

	for (ReportViews view : views) {
	    String id = view.getId();
	    boolean found = false;
	    for (int i = 0; i < viewIdentifiers.length; i++) {
		String interestingID = viewIdentifiers[i];
		if (id.equals(interestingID)) {
		    found = true;
		}
	    }
	    if (!found) {
		toRemove.add(view);
	    }
	}
	views.removeAll(toRemove);

	for (ReportViews view : views) {

	    HashMap<String, List<String[]>> tables = new HashMap<>();

	    String host = getProtocol() + "://" + getHostname();

	    ReportManager reportManager = new ReportManager();

	    String viewId = view.getId();
	    String label = view.getLabel();

	    String postUrl = host + "/gs-service/services/essi/view/" + viewId + "/csw";

	    InputStream stream = MetadataReport.class.getClassLoader().getResourceAsStream(getPostRequest());

	    ClonableInputStream cis = new ClonableInputStream(stream);

	    XMLDocumentReader xmlRequest = new XMLDocumentReader(cis.clone());
	    System.out.println("POST REQUEST: " + xmlRequest.asString());
	    XMLDocumentWriter writer = new XMLDocumentWriter(xmlRequest);

	    List<Node> recordsList = new ArrayList<>();
	    Node[] metadatas = new Node[] {};
	    XMLDocumentReader xdoc = executeRequestPOST(postUrl, cis.clone());
	    String countString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsMatched");
	    String returnedString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsReturned");
	    String nextIndexString = xdoc.evaluateString("//*:SearchResults/@nextRecord");
	    int count = Integer.parseInt(countString);
	    int returned = Integer.parseInt(returnedString);
	    int nextIndex = Integer.parseInt(nextIndexString);
	    ViewReport viewReport = new ViewReport();
	    viewReport.setCount(count);
	    viewReport.setReturned(returned);
	    viewReport.setLabel(label);
	    viewReports.put(viewId, viewReport);
	    metadatas = xdoc.evaluateNodes("//*:MI_Metadata");

	    List<Node> tmpList = Arrays.asList(metadatas);
	    recordsList.addAll(tmpList);
	    while (nextIndex > 0 && (maxRecords == null || recordsList.size() < maxRecords)) {// while nextIndex > 0

		writer.setText("//*:GetRecords/@startPosition", nextIndexString);
		xdoc = executeRequestPOST(postUrl, xmlRequest.asStream());
		returnedString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsReturned");
		nextIndexString = xdoc.evaluateString("//*:SearchResults/@nextRecord");
		metadatas = xdoc.evaluateNodes("//*:MI_Metadata");
		returned = returned + Integer.parseInt(returnedString);
		nextIndex = Integer.parseInt(nextIndexString);
		viewReports.get(viewId).setReturned(returned);
		tmpList = Arrays.asList(metadatas);
		recordsList.addAll(tmpList);
		if (stream != null)
		    stream.close();
	    }

	    if (stream != null)
		stream.close();
	    for (Node n : recordsList) {

		XMLDocumentReader document = new XMLDocumentReader(XMLDocumentReader.asString(n));
		document.setNamespaceContext(new ISO2014NameSpaceContext());
		DocumentReport documentReport = new DocumentReport(document);

		List<BlueCloudMetadataElement> toTest = new ArrayList<>();
		toTest.addAll(Arrays.asList(getCoreMetadataElements()));
		toTest.addAll(Arrays.asList(getOptionalMetadataElements()));
		for (BlueCloudMetadataElement element : toTest) {
		    documentReport.addValues(element);
		}

		String id = document.evaluateString("//gmd:fileIdentifier/gco:CharacterString");
		reportManager.addDocumentReport(id, documentReport);

	    }

	    reportManager.printStatistics();
	    HashMap<BlueCloudMetadataElement, ReportResult> results = reportManager.getReportResults();
	    List<String[]> table = createTable(results);
	    tables.put(viewId, table);

	    String htmlTable = createHTMLTable(tables);
	    String pathName = view.getId();
	    File tmpFile = File.createTempFile(MetadataReport.class.getClass().getSimpleName(), pathName + ".html");
	    System.out.println("Writing table to: " + tmpFile.getAbsolutePath());
	    FileOutputStream outputStream = new FileOutputStream(tmpFile);
	    outputStream.write(htmlTable.getBytes(StandardCharsets.UTF_8));
	    outputStream.flush();
	    outputStream.close();

	    if (uploadToS3) {
		uploadToS3(manager, tmpFile, pathName + ".html");
		tmpFile.delete();
	    }

	}

    }

    private void checkSDNIdentifiers(ReportManager reportManager) throws Exception {
	Set<String> ids = reportManager.getMetadatas().keySet();
	XMLDocumentReader reader = new XMLDocumentReader(new File("/home/boldrini/test/sdn.xml"));
	Node[] nodes = reader.evaluateNodes("//*:cdiUrl");
	for (Node node : nodes) {
	    String trueId = reader.evaluateString(node, ".");
	    trueId = trueId.replace("https://cdi.seadatanet.org/report/aggregation/", "sdn-open:urn:SDN:CDI:LOCAL:").replace("/", "-")
		    .replace("-open-xml", "");
	    String last = trueId.substring(trueId.lastIndexOf("-"));
	    String tmp = trueId.substring(0, trueId.lastIndexOf("-"));
	    String lastLast = tmp.substring(tmp.lastIndexOf("-"));
	    tmp = tmp.substring(0, tmp.lastIndexOf("-"));
	    trueId = tmp + last + lastLast;

	    if (!ids.contains(trueId)) {
		System.out.println(trueId);
	    }
	}

    }

    protected String getProtocol() {
	return "https";
    }

    protected abstract String getPostRequest();

    private List<String[]> createTable(HashMap<BlueCloudMetadataElement, ReportResult> map) {
	List<String[]> table = new ArrayList<>();
	table.add(new String[] { "Core metadata element", "Path", "Samples", "Vocabularies", "Occurrence (%)" });

	for (BlueCloudMetadataElement element : getCoreMetadataElements()) {
	    table.add(getRow(map, element));
	}

	table.add(new String[] { "Optional metadata element", "Path", "Samples", "Vocabularies", "Occurrence (%)" });

	for (BlueCloudMetadataElement element : getOptionalMetadataElements()) {
	    table.add(getRow(map, element));
	}

	return table;
    }

    private String[] getRow(HashMap<BlueCloudMetadataElement, ReportResult> map, BlueCloudMetadataElement reportElement) {
	String metadata = reportElement.toString();
	String path = reportElement.getPathHtml();
	boolean isVocabulary = false;
	if (metadata.contains("_URI")) {
	    isVocabulary = true;
	}

	if (map.containsKey(reportElement)) {
	    ReportResult result = map.get(reportElement);
	    int count = result.getCount();
	    int total = result.getTotal();
	    int percent = (int) (((double) count / (double) total) * 100.0);
	    Set<String> values = result.getCache().keySet();
	    String terms = "";

	    if (values.size() >= 30 && !isVocabulary) {
		int i = 3;
		terms = "Many terms. Examples:<br/>";
		for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
		    String term = (String) iterator.next();
		    terms += term + "<br/>";
		    if (i-- < 0) {
			break;
		    }
		}
	    } else {
		for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
		    String term = (String) iterator.next();
		    terms += term + "<br/>";
		}
	    }
	    if (isVocabulary) {
		String vocabularies = checkVocabulary(metadata, values);
		return new String[] { metadata, path, terms, vocabularies, Integer.toString(percent) };
	    } else {
		return new String[] { metadata, path, terms, "", Integer.toString(percent) };
	    }

	} else {
	    return new String[] { metadata, path, "", "", "0" };
	}
    }

    private String checkVocabulary(String metadata, Set<String> values) {
	String vocs = "Vocabularies found:<br/>";
	Set<String> set = new HashSet<>();
	int vocNumber = 0;
	for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
	    String term = (String) iterator.next();
	    vocNumber++;
	    if (term.contains("http://vocab.nerc.ac.uk/collection/") || term.contains("https://vocab.nerc.ac.uk/collection/")) {
		String[] splittedVoc = term.split("http://vocab.nerc.ac.uk/collection/");
		if (splittedVoc.length > 1) {
		    set.add(splittedVoc[1].split("/")[0]);
		} else {
		    splittedVoc = term.split("https://vocab.nerc.ac.uk/collection/");
		    set.add(splittedVoc[1].split("/")[0]);
		}
	    } else if (term.contains("https://vocab.ifremer.fr/scheme/SXT/")) {
		String[] splittedVoc = term.split("https://vocab.ifremer.fr/scheme/SXT/");
		if (splittedVoc.length > 1) {
		    set.add("https://vocab.ifremer.fr/scheme/SXT/" + splittedVoc[1].split("/")[0] + "/");
		}
	    } else if (term.contains("https://edmo.seadatanet.org/") || term.contains("http://seadatanet.maris2.nl/v_edmo/")) {
		set.add("EDMO");
	    } else if (term.contains("https://csr.seadatanet.org/")) {
		set.add("CSR");
	    } else if (term.contains("https://edmerp.seadatanet.org/")) {
		set.add("EDMERP");
	    } else if (term.contains("http://inspire.ec.europa.eu")) {
		set.add("INSPIRE Themes");
	    } else if (term.contains("http://meta.icos-cp.eu/")) {
		set.add("ICOS Project");
	    } else if (term.contains("https://cdi.seadatanet.org/")) {
		set.add(term + " (to check)");
	    } else if (term.contains("inspiretheme") || term.contains("inspire-theme")) {
		set.add(term + " (to check)");
	    } else if (term.contains("http://purl.org")) {
		String[] splittedVoc = term.split("#");
		set.add(splittedVoc[0]);
	    } else if (term.contains("https://www.marinespecies.org/aphia.php?")) {
		String[] splittedVoc = term.split("\\?");
		set.add(splittedVoc[0]);
	    } else if (term.contains("https://marineinfo.org/id/institute/")) {
		set.add("https://marineinfo.org/id/institute/");
	    }

	}
	if (set.isEmpty()) {
	    vocs = "No vocabularies found<br/>";
	}
	System.out.println("Number of VOC terms for " + metadata + " : " + vocNumber);
	for (String s : set) {
	    vocs += s + "<br/>";
	}
	return vocs;
    }

    private XMLDocumentReader executeRequestPOST(String postUrl, InputStream stream) throws Exception {

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, postUrl, stream);

	Downloader executor = new Downloader();

	HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	InputStream content = response.body();

	XMLDocumentReader res = null;

	res = new XMLDocumentReader(content);

	if (content != null)
	    content.close();

	return res;

    }

    public abstract BlueCloudMetadataElement[] getCoreMetadataElements();

    public abstract BlueCloudMetadataElement[] getOptionalMetadataElements();

    private String createHTMLTable(HashMap<String, List<String[]>> tableMap) {

	StringBuilder builder = new StringBuilder();

	builder.append("<html>");
	builder.append("<head>");
	builder.append("<style>");
	builder.append("#table {");
	builder.append("  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;");
	builder.append("  border-collapse: collapse;");
	builder.append("}");
	builder.append("#table td, #table th {");
	builder.append(" border: 1px solid #ddd;");
	builder.append("  padding: 8px;");
	builder.append("}");
	builder.append("#table tr:nth-child(even){background-color: #f2f2f2;}");
	builder.append("#table tr:hover {background-color: #ddd;}");
	builder.append("#table th {");
	builder.append("  padding-top: 12px;");
	builder.append("  padding-bottom: 12px;");
	builder.append("  text-align: left;");
	builder.append("  background-color: #007bff;");
	builder.append("  color: white;");
	builder.append("}");
	builder.append("</style>");
	builder.append("</head>");

	builder.append("<body>");

	builder.append("<h1>" + getProjectName() + " metadata completeness report (last update on: "
		+ ISO8601DateTimeUtils.getISO8601DateTime() + ")</h1><p>The following tables show individual reports for each "
		+ getProjectName()
		+ " service harvested by the DAB (first level metadata only). The main metadata elements are reported, along with their completeness score.</p>");

	for (Entry<String, List<String[]>> entry : tableMap.entrySet()) {

	    String viewId = entry.getKey();

	    List<String[]> table = entry.getValue();

	    ViewReport viewReport = viewReports.get(viewId);
	    String label = viewReport.getLabel();
	    int count = viewReport.getCount();
	    int returned = viewReport.getReturned();
	    double percent = (((double) returned / (double) count) * 100.00);
	    DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
	    sym.setDecimalSeparator('.');
	    DecimalFormat df2 = new DecimalFormat("#.##");
	    df2.setRoundingMode(RoundingMode.UP);
	    df2.setDecimalFormatSymbols(sym);

	    int rows = table.size();
	    int cols = table.get(0).length;

	    String testPortal = "https://" + getHostname() + "/gs-service/search?view=" + viewId;
	    builder.append("<div><strong>" + label + "</strong> available service at: <strong>https://" + getHostname()
		    + "/gs-service/services/essi/view/" + viewId + "/csw</strong><br/>" + "<strong>" + label
		    + "</strong> available test portal at: <a href=\"" + testPortal + "\">" + testPortal + "</a><br/>"
		    + "Total number of records: <strong>" + count + "</strong> Number of records analyzed: <strong>" + returned
		    + "</strong> " + "Percentage of records analyzed: <strong>" + df2.format(percent) + "%</strong></div>");

	    builder.append("<table id='table' style='width:100%'>");

	    for (int row = 0; row < rows; row++) {

		boolean headerRow = false;
		String columnTag = "td";
		if (table.get(row)[0].toLowerCase().contains("metadata element")) {
		    headerRow = true;
		    columnTag = "th";
		}

		builder.append("<tr>");
		String style = "";
		if (!headerRow) {
		    String value = table.get(row)[cols - 1];
		    try {
			double d = Double.parseDouble(value);
			if (d < 99.0) {
			    style = " style='background-color:yellow' ";
			}
			if (d < 10.0) {
			    style = " style='background-color:orange' ";
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}

		for (int col = 0; col < cols; col++) {

		    String value = table.get(row)[col];

		    if (headerRow) {
			builder.append("<" + columnTag + ">");
		    } else {
			if (col == 1) {
			    value = "<div title=\"" + value.trim() + "\">hover to display path [...]</div>";
			}
			if (col == cols - 1) {
			    value = value + "%";
			}
			builder.append("<" + columnTag + style + ">");
		    }

		    builder.append(value);

		    builder.append("</" + columnTag + ">");
		}

		builder.append("</tr>\n");
	    }

	    builder.append("</table><br/>\n");

	}

	builder.append("</body></html>");

	return builder.toString();

    }

    private void uploadToS3(S3TransferWrapper manager, File f, String name) throws Exception {
	System.out.println("Uploading to S3");
	String bucketName = "dabreporting";
	try {
	    manager.setACLPublicRead(true);
	    manager.uploadFile(f.getAbsolutePath(), bucketName, getS3Folder() + "/" + name);
	    System.out.println("Uploaded");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("Error during S3 upload!");
	}
    }

    public abstract String getS3Folder();

    public abstract String getHostname();

    public abstract String getProjectName();

    public abstract String getReportFilename();

    public abstract List<ReportViews> getViews();

}
