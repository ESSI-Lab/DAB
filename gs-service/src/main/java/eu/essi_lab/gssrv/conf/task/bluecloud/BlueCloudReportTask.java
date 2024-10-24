package eu.essi_lab.gssrv.conf.task.bluecloud;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Map.Entry;
import java.util.Optional;

import org.quartz.JobExecutionContext;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * @author Fabrizio
 */
public class BlueCloudReportTask extends AbstractCustomTask {

	private ReportManager bmanager;
	private HashMap<String, String> countMap;
	private HashMap<String, String> returnedMap;
	private HashMap<String, String> labelMap;

	private static final String POST_REQUEST_FILE = "task/blueCloudPostRequest.xml";

	/**
	 * 
	 */
	public BlueCloudReportTask() {

		bmanager = new ReportManager();
		countMap = new HashMap<>();
		returnedMap = new HashMap<>();
		labelMap = new HashMap<>();

	}

	@Override
	public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

		log(status, "BlueCloud report task STARTED");

		CustomTaskSetting setting = retrieveSetting(context);

		Optional<String> taskOptions = setting.getTaskOptions();

		String s3SubFolder = "BlueCloud";
		Integer maxRecords = null;

		if (taskOptions.isPresent()) {

			try {

				String[] options = taskOptions.get().split("\n");

				s3SubFolder = options[0]; // s3 sub folder
				maxRecords = Integer.valueOf(options[1]); // max records
				if (maxRecords == -1) {
					maxRecords = null;
				}

			} catch (Exception ex) {
			}
		}

		doJob(maxRecords, s3SubFolder);

		log(status, "BlueCloud report task ENDED");
	}

	/**
	 * @param maxRecords
	 * @throws Exception
	 */
	private void doJob(Integer maxRecords, String s3SubFolder) throws Exception {

		Optional<S3TransferWrapper> manager = getS3TransferManager();

		BlueCLoudViews[] views = BlueCLoudViews.values(); // ALL

		HashMap<String, String[][]> tables = new HashMap<String, String[][]>();

		String host = "https://blue-cloud.geodab.eu";

		//
		//
		//

		for (BlueCLoudViews view : views) {

			String viewId = view.getView();
			String label = view.getLabel();

			String postUrl = host + "/gs-service/services/essi/view/" + viewId + "/csw";

			InputStream stream = getClass().getClassLoader().getResourceAsStream(POST_REQUEST_FILE);

			ClonableInputStream cis = new ClonableInputStream(stream);

			XMLDocumentReader xmlRequest = new XMLDocumentReader(cis.clone());

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
				// countString =
				// xdoc.evaluateString("//*:SearchResults/@numberOfRecordsMatched");
				returnedString = xdoc.evaluateString("//*:SearchResults/@numberOfRecordsReturned");
				nextIndexString = xdoc.evaluateString("//*:SearchResults/@nextRecord");
				metadatas = xdoc.evaluateNodes("//*:MI_Metadata");
				returned = returned + Integer.parseInt(returnedString);
				nextIndex = Integer.parseInt(nextIndexString);

				returnedMap.put(viewId, String.valueOf(returned));

				tmpList = Arrays.asList(metadatas);
				recordsList.addAll(tmpList);

				if (stream != null) {
					stream.close();
				}
			}

			if (stream != null) {
				stream.close();
			}

			for (Node n : recordsList) {

				XMLDocumentReader metadata = new XMLDocumentReader(XMLDocumentReader.asString(n));
				metadata.setNamespaceContext(new ISO2014NameSpaceContext());

				DocumentReport bcm = new DocumentReport(metadata);

				for (MetadataElement bce : MetadataElement.values()) {

					String[] paths = bce.getPaths();
					List<String> values = new ArrayList<String>();
					boolean atLeastOneGood = false;
					for (String path : paths) {
						String value = metadata.evaluateString(path);
						if (value != null && !value.isEmpty()) {
							atLeastOneGood = true;
							values.add(value);
						}
					}
					if (atLeastOneGood) {
						bcm.addMetadata(bce, values);
					}
				}

				String id = metadata.evaluateString("//gmd:fileIdentifier/gco:CharacterString");
				bmanager.addDocumentReport(id, bcm);

			}

			bmanager.printStatistics();

			HashMap<MetadataElement, ReportResult> map = bmanager.getReportResults();

			String[][] table = createTable(map);
			tables.put(viewId, table);

			bmanager.reset();
		}

		//
		//
		//

		String htmlTable = createHTMLPage(tables);

		File tmpFile = File.createTempFile(getClass().getSimpleName(), "report.html");

		FileOutputStream outputStream = new FileOutputStream(tmpFile);
		outputStream.write(htmlTable.getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		outputStream.close();

		if (manager.isPresent()) {

			manager.get().setACLPublicRead(true);

			manager.get().uploadFile(tmpFile.getAbsolutePath(), "dabreporting",
					s3SubFolder + "/BlueCloudReport_brief.html");
			manager.get().uploadFile(tmpFile.getAbsolutePath(), "dabreporting",
					s3SubFolder + "/BlueCloudReport_full.html");

			String date = ISO8601DateTimeUtils.getISO8601Date().replace("-", "");

			manager.get().uploadFile(tmpFile.getAbsolutePath(), "dabreporting",
					s3SubFolder + "/" + date + "/BlueCloudReport_brief.html");
			manager.get().uploadFile(tmpFile.getAbsolutePath(), "dabreporting",
					s3SubFolder + "/" + date + "/BlueCloudReport_full.html");
		}

		tmpFile.delete();

		//
		//
		//
	}

	@Override
	public String getName() {

		return "BlueCloud report task";
	}

	/**
	 * @author Fabrizio
	 */
	private enum BlueCLoudViews {
		SEADATANET_OPEN("seadatanet-open", "SeaDataNet Open"), //
		SEADATANET_PRODUCTS("seadatanet-products", "SeaDataNet Products"), //
		EMODNET_CHEMISTRY("emodnet-chemistry", "EMODnet Chemistry"), //
		ARGO("argo", "ARGO"), //
		EUROBIS("eurobis", "EurOBIS"), //
		ELIXIR_ENA("elixir-ena", "ELIXIR-ENA"), //
		WEKEO("wekeo", "WEKEO"), //
		// ICOS("icos-marine", "ICOS Marine"), //

		ECOTAXA("ecotaxa", "EcoTaxa"), //
		ICOS_SOCAT("icos-socat", "ICOS SOCAT"), //
		ICOS_DATA_PORTAL("icos-data-portal", "ICOS Data Portal");// ;

		private String label;

		/**
		 * @return
		 */
		private String getLabel() {
			return label;
		}

		/**
		 * @return
		 */
		private String getView() {
			return view;
		}

		/**
		 * 
		 */
		private String view;

		/**
		 * @param view
		 * @param label
		 */
		private BlueCLoudViews(String view, String label) {
			this.label = label;
			this.view = view;
		}
	}

	/**
	 * @param tableMap
	 * @return
	 */
	private String createHTMLPage(HashMap<String, String[][]> tableMap) {

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
				"<h1>Blue-Cloud metadata completeness report</h1><p>The following tables show individual reports for each Blue-Cloud service harvested by the DAB (first level metadata only). The main metadata elements are reported, along with their completeness score.</p>");
		// post request?
		// builder.append("<h1>BlueCloud Report Mapping for main metadata fields</h1>");

		for (Entry<String, String[][]> entry : tableMap.entrySet()) {

			String viewId = entry.getKey();
			String[][] table = entry.getValue();
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
					+ "</strong> available service at: <strong>https://blue-cloud.geodab.eu/gs-service/services/essi/view/"
					+ viewId + "/csw</strong><br>" + "<strong>" + label
					+ "</strong> available test portal at: <a href=\"" + testPortal + "\">" + testPortal + "</a><br>"
					+ "Total number of records: <strong>" + count + "</strong> Number of records analyzed: <strong>"
					+ returned + "</strong> " + "Percentage of records analyzed: <strong>" + df2.format(percent)
					+ "%</strong></td></tr>");

			int rows = table.length;
			int cols = table[0].length;

			for (int row = 0; row < rows; row++) {

				builder.append("<tr>");
				String style = "";
				if (row > 0) {
					String value = table[row][cols - 1];
					try {
						double d = Double.parseDouble(value);
						if (d < 99.0) {
							style = " style='background-color:yellow' ";
						}
						if (d < 10.0) {
							style = " style='background-color:red' ";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				for (int col = 0; col < cols; col++) {

					String value = table[row][col];

					if (row > 0 && col == cols - 1) {
						value = value + "%";
					}

					if (row == 0) {
						builder.append("<th>");
						// if (col >= 3) {
						// builder.append("<th" + style + "><button style='width: 100%;'
						// onclick='sortTable(" + col +
						// ")'>Sort</button><br>");
						// } else {
						// builder.append("<th" + style + ">");
						// }
					} else {
						if (col == 0 || col == cols - 1) {
							builder.append("<td" + style + ">");
						} else {
							builder.append("<td>");
						}
					}

					// builder.append(row == 0 ? "<th" + style + ">" : "<td" + style + ">");
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

	/**
	 * @param map
	 * @return
	 */
	private String[][] createTable(HashMap<MetadataElement, ReportResult> map) {

		String[][] table = new String[13][3];

		table[0][0] = "Metadata element";
		table[0][1] = "Path";
		table[0][2] = "Completeness";

		MetadataElement[] bcElements = MetadataElement.values();

		int j = 0;
		for (MetadataElement bcElement : bcElements) {
			// geographic_description field empty
			String metadata = bcElement.toString();
			String path = bcElement.getPathHtml();
			if (map.containsKey(bcElement)) {
				table[j + 1][0] = metadata;
				table[j + 1][1] = path;
				table[j + 1][2] = Integer.toString(map.get(bcElement).getCount());
			} else {
				table[j + 1][0] = metadata;
				table[j + 1][1] = path;
				table[j + 1][2] = "0";
			}
			j++;
		}

		return table;
	}

	/**
	 * @param postUrl
	 * @param stream
	 * @return
	 * @throws Exception
	 */
	private XMLDocumentReader executeRequestPOST(String postUrl, InputStream stream) throws Exception {

		XMLDocumentReader res = null;

		HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, postUrl, stream);

		Downloader executor = new Downloader();

		HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

		InputStream content = response.body();

		res = new XMLDocumentReader(content);

		if (content != null) {
			content.close();
		}

		return res;
	}
}
