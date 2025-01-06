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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import eu.essi_lab.gssrv.conf.task.bluecloud.ReportManager;
import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

public class BLUECloudDownloadReport {

    private static ReportManager bmanager = new ReportManager();

    private static HashMap<String, String> countMap = new HashMap<>();

    private static HashMap<String, String> returnedMap = new HashMap<>();

    private static HashMap<String, String> labelMap = new HashMap<>();

    private static HashMap<String, DownloadInfo> protocolMap = new HashMap<>();

    public static enum BlueCLoudViews {
	// SEADATANET_OPEN("seadatanet-open", "SeaDataNet Open"), //
	SEADATANET_PRODUCTS("seadatanet-products", "SeaDataNet Products"), //
	EMODNET_CHEMISTRY("emodnet-chemistry", "EMODnet Chemistry"); //
	// ARGO("argo", "ARGO"), //
	// EUROBIS("eurobis", "EurOBIS"), //
	// ECOTAXA("ecotaxa", "EcoTaxa"), //
	// ELIXIR_ENA("elixir-ena", "ELIXIR-ENA"), //
	// WEKEO("wekeo", "WEKEO"), //
	// ICOS("icos-marine", "ICOS Marine"), //
	// ICOS_SOCAT("icos-socat", "ICOS SOCAT"), //
	// ICOS_DATA_PORTAL("icos-data-portal", "ICOS Data Portal");// ;

	private String label;

	public String getLabel() {
	    return label;
	}

	public String getView() {
	    return view;
	}

	private String view;

	BlueCLoudViews(String view, String label) {
	    this.label = label;
	    this.view = view;
	}

    }

    /*
     * DEFAULT POST REQUEST PARAMS IN blueCloudPostRequest.xml
     * startPosition = 1; requestSize = 100;
     */

    public static void main(String[] args) throws Exception {

	/////////////////////////
	//// USER ARGUMENTS
	/////////////////////////

	BlueCLoudViews[] views = BlueCLoudViews.values(); // ALL

	// selected ones
	// views = new BlueCLoudViews[] { BlueCLoudViews.ICOS_SOCAT };

	HashMap<String, ArrayList<Object>> tables = new HashMap<String, ArrayList<Object>>();

	String host = "https://blue-cloud.geodab.eu";
	// String host = "http://localhost:9090";

	boolean uploadToS3 = false;

	Integer maxRecords = null; // null if all

	/////////////////////////////

	if (host.contains("localhost")) {
	    uploadToS3 = false;
	}

	for (BlueCLoudViews view : views) {
	    String viewId = view.getView();
	    String label = view.getLabel();

	    String postUrl = host + "/gs-service/services/essi/view/" + viewId + "/csw";

	    InputStream stream = BLUECloudDownloadReport.class.getClassLoader().getResourceAsStream("blueCloudPostRequest.xml");

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
	    countMap.put(viewId, countString);
	    returnedMap.put(viewId, returnedString);
	    labelMap.put(viewId, label);
	    metadatas = xdoc.evaluateNodes("//*:MI_Metadata");

	    List<Node> tmpList = Arrays.asList(metadatas);
	    recordsList.addAll(tmpList);
	    while (nextIndex > 0 && (maxRecords == null || recordsList.size() < maxRecords)) {// while nextIndex > 0

		writer.setText("//*:GetRecords/@startPosition", nextIndexString);
		xdoc = executeRequestPOST(postUrl, xmlRequest.asStream());
		// countString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsMatched");
		returnedString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsReturned");
		nextIndexString = xdoc.evaluateString("//*:SearchResults/@nextRecord");
		metadatas = xdoc.evaluateNodes("//*:MI_Metadata");
		returned = returned + Integer.parseInt(returnedString);
		nextIndex = Integer.parseInt(nextIndexString);
		returnedMap.put(viewId, String.valueOf(returned));
		tmpList = Arrays.asList(metadatas);
		recordsList.addAll(tmpList);
		if (stream != null)
		    stream.close();
	    }

	    // List<Node> fullMetadata = Arrays.asList(metadatas);
	    // fullMetadata.addAll(fullMetadata);
	    // fullMetadata.addAll(metadatas.);
	    if (stream != null)
		stream.close();

	    int countEmpty = 0;
	    int countOnline = 0;
	    int countNoProtocol = 0;
	    int countNoLinkage = 0;
	    int countNoName = 0;

	    for (Node n : recordsList) {

		XMLDocumentReader metadata = new XMLDocumentReader(XMLDocumentReader.asString(n));
		metadata.setNamespaceContext(new ISO2014NameSpaceContext());
		List<Node> transferOptions = metadata.evaluateOriginalNodesList("//*:distributionInfo/*/*:transferOptions");
		boolean hasOnline = false;

		for (Node transfer : transferOptions) {
		    XMLDocumentReader tOpt = new XMLDocumentReader(XMLDocumentReader.asString(transfer));
		    Node[] onlineNodes = tOpt.evaluateNodes("//*:onLine");

		    if (onlineNodes != null) {
			for (int k = 0; k < onlineNodes.length; k++) {
			    XMLDocumentReader oNode = new XMLDocumentReader(XMLDocumentReader.asString(onlineNodes[k]));
			    if (oNode != null) {
				hasOnline = true;
				String protocol = oNode.evaluateString("//*:protocol/*:CharacterString");
				String name = oNode.evaluateString("//*:name/*:CharacterString");
				String onlineLinkage = oNode.evaluateString("//*:linkage/*:URL");
				if (onlineLinkage == null || onlineLinkage.isEmpty()) {
				    onlineLinkage = "NO LINKAGE AVAILABLE";
				    countNoLinkage++;
				}
				if (name == null || name.isEmpty()) {
				    Node nodeName = oNode.evaluateNode("//*:name");
				    if (nodeName == null) {
					countNoName++;
					name = "NO NAME AVAILABLE";
				    } else {

					NodeList childNodes = nodeName.getChildNodes();
					for (int i = 0; i < childNodes.getLength(); i++) {

					    Node item = childNodes.item(i);
					    String itemName = item.getLocalName();

					    if (itemName != null) {
						name = oNode.evaluateString("//*:name/*:" + itemName);
						if (name == null || name.isEmpty()) {
						    countNoName++;
						    name = "NO NAME AVAILABLE";
						}
						break;
					    }

					}
				    }
				}

				if (protocol != null && !protocol.isEmpty()) {
				    if (protocolMap.containsKey(protocol)) {
					DownloadInfo dInfo = protocolMap.get(protocol);
					// Integer actualCount = protocolMap.get(protocol);
					int currentCount = dInfo.getProtocolCount().intValue() + 1;
					dInfo.setProtocolCount(currentCount);
					protocolMap.put(protocol, dInfo);
				    } else {
					// protocolMap.put(protocol, 1);
					String id = metadata.evaluateString("//*:MI_Metadata/*:fileIdentifier/*:CharacterString");
					String getRecordByid = host + "/gs-service/services/essi/view/" + viewId
						+ "/csw?service=CSW&version=2.0.2&request=GetRecordById&id=" + id
						+ "&outputschema=https://www.blue-cloud.org/&elementSetName=full";
					DownloadInfo dInfo = new DownloadInfo(1, onlineLinkage, getRecordByid, name);
					protocolMap.put(protocol, dInfo);
				    }
				} else {

				    countNoProtocol++;
				}
				countOnline++;
			    }
			}
		    }

		}
		if (!hasOnline) {
		    countEmpty++;
		}

	    }

	    // BlueCloudMetadataManager.getInstance().printStatistics();
	    String[][] table = createTable(protocolMap);
	    ArrayList<Object> listElement = new ArrayList<Object>();
	    listElement.add(table);
	    listElement.add(countNoName);
	    listElement.add(countNoLinkage);
	    listElement.add(countNoProtocol);
	    listElement.add(countEmpty);
	    listElement.add(countOnline);

	    tables.put(viewId, listElement);

	    protocolMap.clear();

	}

	String htmlTable = createHTMLTable(tables);
	// String tmpDir = System.getProperty("java.io.tmpdir");
	// String fileName = tmpDir + File.separator + System.currentTimeMillis() + "_bluecloudReport.html";
	String pathName = maxRecords == null ? "BlueCloudDownloadReport_full" : "BlueCloudDownloadReport_brief";
	File tmpFile = File.createTempFile(BLUECloudDownloadReport.class.getClass().getSimpleName(), pathName + ".html");
	tmpFile.deleteOnExit();
	System.out.println("Writing table to: " + tmpFile.getAbsolutePath());
	FileOutputStream outputStream = new FileOutputStream(tmpFile);
	outputStream.write(htmlTable.getBytes(StandardCharsets.UTF_8));
	outputStream.flush();
	outputStream.close();

	if (uploadToS3) {
	    uploadToS3(tmpFile, pathName + ".html");
	}

    }

    private static String[][] createTable(HashMap<String, DownloadInfo> map) {
	int mapSize = map.size() + 1;
	String[][] table = new String[mapSize][5];
	table[0][0] = "Online Protocol";
	table[0][1] = "Number of occurences";
	table[0][2] = "Download Link example";
	table[0][3] = "Online Name example";
	table[0][4] = "Metadata record";

	int j = 0;
	for (Map.Entry<String, DownloadInfo> entry : map.entrySet()) {
	    String protocol = entry.getKey();
	    DownloadInfo dInfo = entry.getValue();
	    table[j + 1][0] = protocol;
	    table[j + 1][1] = Integer.toString(dInfo.getProtocolCount());
	    table[j + 1][2] = dInfo.getOnlineLinkage();
	    table[j + 1][3] = dInfo.getOnlineName();
	    table[j + 1][4] = dInfo.getGetRecordById();
	    j++;
	}

	return table;
    }

    private static XMLDocumentReader executeRequestPOST(String postUrl, InputStream stream) throws Exception {

	XMLDocumentReader res = null;

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, postUrl, stream);

	// httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

	Downloader executor = new Downloader();

	HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	InputStream content = response.body();

	// GetRecordsResponse grResponse = (GetRecordsResponse) CommonContext.createUnmarshaller().unmarshal(content);

	// grResponse.getSearchResults().

	res = new XMLDocumentReader(content);

	if (content != null)
	    content.close();

	return res;

    }

    private static String createHTMLTable(HashMap<String, ArrayList<Object>> tableMap) {

	StringBuilder builder = new StringBuilder();

	builder.append("<html>");
	builder.append("<head>");
	builder.append("<style>");
	builder.append("#table {");
	builder.append("  font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif;");
	builder.append("  border-collapse: collapse;");
	builder.append("  width: 100%;");
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

	builder.append(
		"<h1>Blue-Cloud Dowload protocol report</h1><p>The following tables show Download reports for each Blue-Cloud service harvested by the DAB (first level metadata only).</p>");
	// post request?
	// builder.append("<h1>BlueCloud Report Mapping for main metadata fields</h1>");

	for (Entry<String, ArrayList<Object>> entry : tableMap.entrySet()) {

	    String viewId = entry.getKey();
	    int countNoName = 0;
	    int countNoLinkage = 0;
	    int countNoProtocol = 0;
	    int countNoOnline = 0;
	    int countOnline = 0;
	    ArrayList<Object> listObject = entry.getValue();
	    int k = 0;
	    String[][] table = null;
	    for (Object o : listObject) {
		switch (k) {
		case 0:
		    table = (String[][]) o;
		    break;
		case 1:
		    countNoName = (int) o;
		    break;

		case 2:
		    countNoLinkage = (int) o;
		    break;
		case 3:
		    countNoProtocol = (int) o;
		    break;
		case 4:
		    countNoOnline = (int) o;
		    break;
		case 5:
		    countOnline = (int) o;
		    break;

		}
		k++;

	    }

	    builder.append("<table id='table' style='width:100%'>");

	    String label = labelMap.get(viewId);
	    String count = countMap.get(viewId);
	    String returned = returnedMap.get(viewId);
	    int c = Integer.parseInt(count);
	    int r = Integer.parseInt(returned);
	    double percent = (((double) r / (double) c) * 100.00);
	    DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
	    sym.setDecimalSeparator('.');
	    DecimalFormat df2 = new DecimalFormat("#.##");
	    df2.setRoundingMode(RoundingMode.UP);
	    df2.setDecimalFormatSymbols(sym);
	    String testPortal = "https://blue-cloud.geodab.eu/gs-service/search?view=" + viewId;
	    builder.append("<tr><td colspan='12'><br/><strong>" + label
		    + "</strong> available service at: <strong>https://blue-cloud.geodab.eu/gs-service/services/essi/view/" + viewId
		    + "/csw</strong><br>" + "<strong>" + label + "</strong> available test portal at: <a href=\"" + testPortal + "\">"
		    + testPortal + "</a><br>" + "Total number of records: <strong>" + count
		    + "</strong> Number of records analyzed: <strong>" + returned + "</strong> "
		    + "Percentage of records analyzed: <strong>" + df2.format(percent) + "%</strong><br>"
		    + "Total Number of Online resources: <strong>" + countOnline
		    + "</strong> Number of Online resources withouth a protocol: <strong>" + countNoProtocol + "</strong> "
		    + "Number of Online resources withouth a linkage: <strong>" + countNoLinkage
		    + "</strong> Number of Online resource withouth a name: <strong>" + countNoName + "</strong> " + "</td></tr>");

	    int rows = table.length;
	    int cols = table[0].length;
	    for (int row = 0; row < rows; row++) {

		builder.append("<tr>");
		String style = "";

		for (int col = 0; col < cols; col++) {

		    String value = table[row][col];

		    if (row == 0) {
			builder.append("<th>");

		    } else {

			if (col == cols - 2) {
			    // Online name case
			    String nameVal = table[row][cols - 2];
			    if (nameVal.equals("NO NAME AVAILABLE")) {
				style = " style='background-color:red' ";
				builder.append("<td" + style + ">");
			    } else {
				builder.append("<td>");
			    }
			    // Online linkage case
			} else if (col == cols - 3) {
			    String onlineVal = table[row][cols - 3];
			    if (onlineVal == null || onlineVal.isEmpty() || onlineVal.equals("NO LINKAGE AVAILABLE")) {
				style = " style='background-color:red' ";
				builder.append("<td" + style + ">");
			    } else {
				builder.append("<td>");
			    }
			    // GetRecordByid case
			} else if (col == cols - 1) {
			    String getRecordById = table[row][cols - 1];
			    // builder.append("<td" + style + ">");
			    builder.append("<td><a target=\"_blank\" href=\"" + getRecordById + "\"</a>");
			} else {
			    builder.append("<td>");
			}

		    }

		    builder.append(value);
		    builder.append(row == 0 ? "</th>" : "</td>");
		}

		builder.append("</tr>");
	    }

	    builder.append("</table>");
	}

	builder.append("</table></body></html>");

	return builder.toString();

    }

    private static void uploadToS3(File f, String name) throws Exception {
	System.out.println("Uploading to S3");
	Regions clientRegion = Regions.US_EAST_1;
	String bucketName = "dabreporting";

	try {
	    // This code expects that you have AWS credentials set up per:
	    // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html Expects
	    // (~/.aws/credentials)
	    AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();

	    // Upload a file as a new object with ContentType and title specified.
	    PutObjectRequest request = new PutObjectRequest(bucketName, "BlueCloud/" + name, f)
		    .withCannedAcl(CannedAccessControlList.PublicRead);
	    ObjectMetadata metadata = new ObjectMetadata();
	    metadata.setContentType("text/html");
	    metadata.addUserMetadata("title", "BlueCloud Report");
	    request.setMetadata(metadata);
	    s3Client.putObject(request);
	    // s3Client.putObject(bucketName, "BlueCloud/" + name, f);
	    System.out.println("Uploaded");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("Error during S3 upload!");
	}
    }

}
