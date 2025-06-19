package eu.essi_lab.gssrv.conf.task.bluecloud;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.math.BigDecimal;
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
import java.util.Optional;
import java.util.Set;

import org.w3c.dom.Node;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

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

	    ReportManager reportManager = new ReportManager();

	    String viewId = view.getId();
	    String label = view.getLabel();

	    DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());

	    DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
	    DiscoveryMessage message = new DiscoveryMessage();
	    Optional<View> v = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	    message.setView(v.get());
	    message.setUserBond(v.get().getBond());
	    message.setPermittedBond(v.get().getBond());
	    DiscoveryCountResponse total = finder.count(message);

	    ViewReport viewReport = new ViewReport();
	    viewReport.setCount(total.getCount());
	    viewReport.setReturned(total.getCount());
	    viewReport.setLabel(label);
	    viewReports.put(viewId, viewReport);

	    DocumentReport documentReport = new DocumentReport();

	    List<BlueCloudMetadataElement> toTest = new ArrayList<>();
	    toTest.addAll(Arrays.asList(getCoreMetadataElements()));
	    toTest.addAll(Arrays.asList(getOptionalMetadataElements()));

	    HashMap<BlueCloudMetadataElement, ReportResult> results = new HashMap<BlueCloudMetadataElement, ReportResult>();

	    Bond sourceBond = message.getUserBond().get().clone();
	    for (BlueCloudMetadataElement element : toTest) {
		ReportResult report = new ReportResult();
		report.setTotal(total.getCount());
		SimpleValueBond bond = BondFactory.createExistsSimpleValueBond(element.getQueryable());
		message.setUserBond(BondFactory.createAndBond(bond, sourceBond));
		message.setPermittedBond(BondFactory.createAndBond(bond, sourceBond));
		DiscoveryCountResponse c2 = finder.count(message);
		report.setCount(c2.getCount());
		ResultSet<TermFrequencyItem> valuesResponse = executor.getIndexValues(message, element.getQueryable(), 0, null);
		if (valuesResponse != null) {
		    List<TermFrequencyItem> frequencies = valuesResponse.getResultsList();
		    List<String> vvs = new ArrayList<String>();
		    for (TermFrequencyItem frequency : frequencies) {
			vvs.add(frequency.getTerm());
		    }
		    report.addValue(vvs);
		}
		results.put(element, report);
	    }

	    List<String[]> table = createTable(results);
	    tables.put(viewId, table);

	    String htmlTable = createHTMLTable(tables, getProjectName(), viewReports, getHostname(),null);
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

    public static String[] getRow(HashMap<BlueCloudMetadataElement, ReportResult> map, BlueCloudMetadataElement reportElement) {
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
	    BigDecimal percent = calculatePercentage(count, total);
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
		return new String[] { metadata, path, terms, vocabularies, percent.toString() };
	    } else {
		return new String[] { metadata, path, terms, "", percent.toString() };
	    }

	} else {
	    return new String[] { metadata, path, "", "", "0" };
	}
    }

    public static String checkVocabulary(String metadata, Set<String> values) {
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

    public static String createHTMLTable(HashMap<String, List<String[]>> tableMap, String projectName,
	    HashMap<String, ViewReport> viewReports, String hostname, String lastHarvesting) {

	StringBuilder builder = new StringBuilder();

	builder.append("<html>");
	builder.append("<head>");
	builder.append("<style>");
	builder.append("@media print {");
	builder.append("  body {");
	builder.append("    width: 100%;");
	builder.append("    margin: 0;");
	builder.append("    padding: 0;");
	builder.append("  }");
	builder.append("  #table {");
	builder.append("    width: 100% !important;");
	builder.append("    font-size: 10pt;");
	builder.append("    table-layout: fixed !important;");
	builder.append("  }");
	builder.append("  #table td, #table th {");
	builder.append("    padding: 4px;");
	builder.append("    font-size: 9pt;");
	builder.append("    overflow: visible !important;");
	builder.append("    white-space: normal !important;");
	builder.append("  }");
	builder.append("  #table td:nth-child(1), #table th:nth-child(1) {");
	builder.append("    width: 15% !important;");
	builder.append("  }");
	builder.append("  #table td:nth-child(2), #table th:nth-child(2) {");
	builder.append("    width: 25% !important;");
	builder.append("  }");
	builder.append("  #table td:nth-child(3), #table th:nth-child(3) {");
	builder.append("    width: 80px !important;");
	builder.append("    max-width: 80px !important;");
	builder.append("  }");
	builder.append("  #table td:nth-child(4), #table th:nth-child(4) {");
	builder.append("    width: 20% !important;");
	builder.append("  }");
	builder.append("  #table td:nth-child(5), #table th:nth-child(5) {");
	builder.append("    width: 10% !important;");
	builder.append("  }");
	builder.append("}");
	builder.append("#table {");
	builder.append("  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;");
	builder.append("  border-collapse: collapse;");
	builder.append("  table-layout: fixed;");
	builder.append("  width: 100%;");
	builder.append("}");
	builder.append("#table td, #table th {");
	builder.append("  border: 1px solid #ddd;");
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
	builder.append("#table td:nth-child(1), #table th:nth-child(1) {");
	builder.append("  width: 15%;");
	builder.append("}");
	builder.append("#table td:nth-child(2), #table th:nth-child(2) {");
	builder.append("  width: 25%;");
	builder.append("}");
	builder.append("#table td:nth-child(3), #table th:nth-child(3) {");
	builder.append("  width: 80px;");
	builder.append("  max-width: 80px;");
	builder.append("  overflow: hidden;");
	builder.append("  text-overflow: ellipsis;");
	builder.append("  white-space: nowrap;");
	builder.append("}");
	builder.append("#table td:nth-child(4), #table th:nth-child(4) {");
	builder.append("  width: 20%;");
	builder.append("}");
	builder.append("#table td:nth-child(5), #table th:nth-child(5) {");
	builder.append("  width: 10%;");
	builder.append("}");
	builder.append("#table td:nth-child(3) {");
	builder.append("  position: relative;");
	builder.append("}");
	builder.append("#table td:nth-child(3):hover {");
	builder.append("  overflow: visible;");
	builder.append("  white-space: normal;");
	builder.append("  z-index: 1;");
	builder.append("}");
	builder.append("</style>");
	builder.append("</head>");

	builder.append("<body>");

	builder.append("<h1>" + projectName + " metadata completeness report</h1><p>Generated on: " + ISO8601DateTimeUtils.getISO8601DateTime()
		+ "</p><p>Last metadat aharvesting of the source: " + lastHarvesting + "</p><p>The following tables show individual reports for each " + projectName
		+ " service harvested by the DAB (first level metadata only). The main metadata elements are reported, along with their completeness score.</p>");

	for (Entry<String, List<String[]>> entry : tableMap.entrySet()) {

	    String viewId = entry.getKey();

	    List<String[]> table = entry.getValue();

	    ViewReport viewReport = viewReports.get(viewId);
	    String label = viewReport.getLabel();
	    int count = viewReport.getCount();
	    int returned = viewReport.getReturned();
	    BigDecimal percent = calculatePercentage(returned, count);
	    DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
	    sym.setDecimalSeparator('.');
	    DecimalFormat df2 = new DecimalFormat("#.##");
	    df2.setRoundingMode(RoundingMode.UP);
	    df2.setDecimalFormatSymbols(sym);

	    int rows = table.size();
	    int cols = table.get(0).length;

	    String testPortal = "https://" + hostname + "/gs-service/search?view=" + viewId;
	    builder.append("<div><strong>" + label + "</strong> available service at: <strong>https://" + hostname
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

    public static BigDecimal calculatePercentage(int good, int total) {
	if (total == 0) {
	    return BigDecimal.ZERO;
	}

	BigDecimal goodDecimal = new BigDecimal(good);
	BigDecimal totalDecimal = new BigDecimal(total);

	return goodDecimal.divide(totalDecimal, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2,
		RoundingMode.HALF_UP);
    }

    private void uploadToS3(S3TransferWrapper manager, File f, String name) throws Exception {
	System.out.println("Uploading to S3");
	String bucketName = "dabreporting";
	try {
	    manager.setACLPublicRead(true);
	    manager.uploadFile(f.getAbsolutePath(), bucketName, getS3Folder() + "/" + name, "text/html");
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
