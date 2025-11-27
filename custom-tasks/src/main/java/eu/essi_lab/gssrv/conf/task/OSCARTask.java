package eu.essi_lab.gssrv.conf.task;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;

import org.json.JSONObject;

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

import org.quartz.JobExecutionContext;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.oaipmh.profile.mapper.wigos.WIGOS_MAPPER;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class OSCARTask extends AbstractCustomTask {

    public OSCARTask() {
    }
    // source_id=argentina-ina
    // view_id=whos

    public static final String OSCAR_ENDPOINT = "https://oscardepl.wmo.int/surface/rest/api/wmd/upload";

    public enum OSCARTaskOptions implements OptionsKey {
	TOKEN, OSCAR_ENDPOINT, SOURCE, BBOX, MAX_RECORD;
    }

    @Override
    public String getName() {
	return "OSCAR report task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	// TODO Auto-generated method stub
	// Optional<String> taskOptions = readTaskOptions(context);
	Optional<EnumMap<OSCARTaskOptions, String>> taskOptions = readTaskOptions(context, OSCARTaskOptions.class);

	if (taskOptions.isEmpty() || taskOptions.get().isEmpty()) {
	    GSLoggerFactory.getLogger(getClass())
		    .error("No options specified. Options should be new line separated and in the form key=value");
	    return;
	}

	String hostname = taskOptions.get().get(OSCARTaskOptions.OSCAR_ENDPOINT);
	if (hostname == null) {
	    GSLoggerFactory.getLogger(getClass()).info("No hostname option specified, using default");
	    hostname = "https://whos.geodab.eu";
	}

	String sourceId = taskOptions.get().get(OSCARTaskOptions.SOURCE);
	if (sourceId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No source id option specified");
	    return;
	}

	String tokenValue = taskOptions.get().get(OSCARTaskOptions.TOKEN);
	if (tokenValue == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No token option specified");
	    return;
	}

	String bbox = taskOptions.get().get(OSCARTaskOptions.BBOX);

	String tokenName = "X-WMO-WMDR-Token";

	String maxRecords = taskOptions.get().get(OSCARTaskOptions.MAX_RECORD);

	Integer limit = null;
	if (maxRecords != null) {
	    limit = Integer.parseInt(maxRecords);
	}
	// String tokenValue = null;
	// String endpoint = null;
	// String sourceId = null;
	// String bbox = null;
	// boolean isGRDC = false;
	// if (taskOptions.isPresent()) {
	// String options = taskOptions.get();
	// if (options != null) {
	// if (options.contains("\n")) {
	// String[] split = options.split("\n");
	// if (split.length < 3) {
	// GSLoggerFactory.getLogger(getClass()).error("Missing options for this task");
	// return;
	// }
	//
	// String[] token = split[0].trim().split(":");
	// tokenName = token[0].trim();
	// tokenValue = token[1].trim();
	// String[] splittedEndpoint = split[1].trim().split(":");
	// endpoint = splittedEndpoint[1].trim() + ":" + splittedEndpoint[2].trim();
	// sourceId = split[2].trim().split(":")[1].trim();
	// bbox = split[3].trim().split(":")[1].trim();
	//
	// }
	// }
	// }
	// if (tokenName == null || tokenValue == null || endpoint == null || sourceId == null) {
	// GSLoggerFactory.getLogger(getClass()).error("Missing options for this task");
	// return;
	// }

	String finalEndpoint = OSCAR_ENDPOINT;

	String[] splits = sourceId.split(";");

	for (String split : splits) {

	    WIGOS_MAPPER wigosMapper = new WIGOS_MAPPER();
	    // isGRDC = sourceId.contains("grdc");
	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    discoveryMessage.setRequestId("oscar-task-" + split + "-" + UUID.randomUUID());
	    discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	    discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	    discoveryMessage.setExcludeResourceBinary(false);
	    discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	    discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	    // ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond(sourceId);
	    Bond bond;
	    if (bbox != null) {
		bond = BondFactory.createSourceIdentifierBond(split);
		String[] splittedBox = bbox.split(",");
		SpatialExtent saExtent = new SpatialExtent(); // bbox=11.558,-38.098,39.868,-23.743
		saExtent.setEast(Double.valueOf(splittedBox[2])); // 'ZA': ('South Africa', (16.3449768409,
								  // -34.8191663551,
								  // // 32.830120477, -22.0913127581)),
		saExtent.setNorth(Double.valueOf(splittedBox[3]));
		saExtent.setSouth(Double.valueOf(splittedBox[1]));
		saExtent.setWest(Double.valueOf(splittedBox[0]));
		bond = BondFactory.createAndBond(bond, BondFactory.createSpatialEntityBond(BondOperator.INTERSECTS, saExtent));
		// bond = BondFactory.createAndBond(bond, BondFactory.createSimpleValueBond(BondOperator.EQUAL,
		// MetadataElement.COUNTRY, "South Africa")); //
	    } else {
		bond = BondFactory.createSourceIdentifierBond(split);
	    }

	    // ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond(sourceId);

	    discoveryMessage.setPermittedBond(bond);
	    discoveryMessage.setUserBond(bond);
	    discoveryMessage.setNormalizedBond(bond);
	    discoveryMessage.setSortedFields(
		    new SortedFields(Arrays.asList(new SimpleEntry(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, SortOrder.ASCENDING),
			    new SimpleEntry(MetadataElement.ONLINE_ID, SortOrder.ASCENDING))));

	    SearchAfter searchAfter = null;
	    int i = 0;
	    int start = 1;
	    int pageSize = 50;
	    Downloader downloader = new Downloader();
	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put(tokenName, tokenValue);
	    String platformIdentifier = null;
	    Map<String, List<GSResource>> oscarMap = new LinkedHashMap<String, List<GSResource>>();
	    List<GSResource> resList = new ArrayList<GSResource>();
	    main: while (true) {

		GSLoggerFactory.getLogger(getClass()).info("OSCAR task {} at record {}", split, start);
		discoveryMessage.setPage(new Page(start, pageSize));
		start = start + pageSize;

		if (searchAfter != null) {

		    if (searchAfter.toString().equals("empty")) {
			break main;
		    }
		    discoveryMessage.setSearchAfter(searchAfter);
		}

		ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
		IDiscoveryExecutor executor = loader.iterator().next();
		ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
		if (resultSet.getSearchAfter().isPresent()) {
		    searchAfter = resultSet.getSearchAfter().get();
		}
		List<GSResource> resources = resultSet.getResultsList();

		for (GSResource resource : resources) {
		    i++;
		    MIPlatform platform = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform();
		    if (platform != null) {
			String code = platform.getMDIdentifierCode();
			if (platformIdentifier == null) {
			    platformIdentifier = code;
			}
			if (!code.equals(platformIdentifier)) {
			    oscarMap.put(platformIdentifier, new ArrayList<>(resList));

			    resList.clear();
			    platformIdentifier = code;
			}

			resList.add(resource);
		    }
		}

		if ((limit != null && oscarMap.size() > limit) || resources.isEmpty()) {
		    break main;
		}

	    }

	    if (!resList.isEmpty()) {
		oscarMap.put(platformIdentifier, new ArrayList<>(resList));
	    }
	    GSLoggerFactory.getLogger(getClass()).info("OSCAR MAP SIZE" + ": " + oscarMap.size());

	    int count = 0;
	    for (Map.Entry<String, List<GSResource>> entry : oscarMap.entrySet()) {
		if (limit != null && count >= limit) {
		    break;
		}

		String key = entry.getKey();
		List<GSResource> gsresources = entry.getValue();

		try {

		    String doc = wigosMapper.mapStations(gsresources, key);
		    doc = doc.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		    HttpRequest postRequest = HttpRequestUtils.build(//
			    MethodWithBody.POST, //
			    finalEndpoint, doc, params);

		    HttpResponse<InputStream> response = downloader.downloadResponse(postRequest);

		    InputStream content = response.body();

		    JSONObject jsonObject = JSONUtils.fromStream(content);

		    String xmlStatus = jsonObject.optString("xmlStatus");
		    String logs = jsonObject.optString("logs");
		    String idResponse = jsonObject.optString("id");

		    GSLoggerFactory.getLogger(getClass()).info("ID_RESPONSE:" + idResponse + "-" + xmlStatus + ": " + logs);
		    count++;

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
		}

	    }
	    // oscarMap.forEach((key, gsresource) -> {
	    // try {
	    // String doc = wigosMapper.mapStations(gsresource, key);
	    // doc = doc.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
	    // HttpRequest postRequest = HttpRequestUtils.build(//
	    // MethodWithBody.POST, //
	    // finalEndpoint, doc, params);
	    //
	    // HttpResponse<InputStream> response = downloader.downloadResponse(postRequest);
	    //
	    // InputStream content = response.body();
	    //
	    // JSONObject jsonObject = JSONUtils.fromStream(content);
	    //
	    // String xmlStatus = jsonObject.optString("xmlStatus");
	    // String logs = jsonObject.optString("logs");
	    // String idResponse = jsonObject.optString("id");
	    //
	    // GSLoggerFactory.getLogger(getClass()).info("ID_RESPONSE:" + idResponse + "-" + xmlStatus + ": " + logs);
	    //
	    //
	    // } catch (Exception e) {
	    // GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	    // }
	    // });
	    // }
	}

    }

    public static void main(String[] args) throws Exception {

	InputStream stream = OSCARTask.class.getClassLoader().getResourceAsStream("doc4_SouthAfrica.xml");

	ClonableInputStream cis = new ClonableInputStream(stream);

	XMLDocumentReader xmlRequest = new XMLDocumentReader(cis.clone());

	InputStream stream2 = OSCARTask.class.getClassLoader().getResourceAsStream("doc5_SouthAfrica.xml");

	ClonableInputStream cis2 = new ClonableInputStream(stream2);

	XMLDocumentReader xmlRequest2 = new XMLDocumentReader(cis2.clone());

	XMLDocumentWriter writer = new XMLDocumentWriter(xmlRequest);

	Node node = xmlRequest2.evaluateNode("//*:ObservingFacility/*:observation");
	writer.setText("//*:ResponsibleParty/*:validPeriod/*:TimePeriod/*:beginPosition", "1900-11-30T22:00:00.000Z");
	writer.setText("//*:GeospatialLocation/*:validPeriod/*:TimePeriod/*:beginPosition", "1900-11-30T22:00:00.000Z");
	writer.setText("//*:Territory/*:validPeriod/*:TimePeriod/*:beginPosition", "1900-11-30T22:00:00.000Z");
	writer.setText("//*:GeospatialLocation/*:validPeriod/*:TimePeriod/*:endPosition", "2000-11-30T22:00:00.000Z");
	writer.setText("//*:Territory/*:validPeriod/*:TimePeriod/*:endPosition", "2000-11-30T22:00:00.000Z");

	String xpath = "//*:ObservingFacility";
	writer.addNode(xpath, node);

	System.out.println(xmlRequest.asString());

	// WIGOS_MAPPER wigosMapper = new WIGOS_MAPPER();
	//
	// String request =
	// "http://localhost:9090/gs-service/services/essi/token/whos-d40a452b-b865-4fbe-8165-43a96ebf1b3d/view/gs-view-source(argentina-ina)/oaipmh?verb=ListRecords&metadataPrefix=WIGOS-1.0&resumptionToken=restoken/101/null/null/null/WIGOS-1.0/1729189908361";
	//
	// Downloader d = new Downloader();
	// Optional<String> resp = d.downloadOptionalString(request);
	//
	// if (resp.isPresent()) {
	// XMLDocumentReader xdoc = new XMLDocumentReader(resp.get());
	//
	// Node[] nodes = xdoc.evaluateNodes("//*:WIGOSMetadataRecord");
	//
	// HashMap<String, String> params = new HashMap<String, String>();
	// params.put("X-WMO-WMDR-Token", System.getProperty("WMO_TOKEN"));
	//
	// Downloader downloader = new Downloader();
	//
	// for (Node node : nodes) {
	// String doc = XMLDocumentReader.asString(node);
	// doc = doc.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
	// HttpRequest postRequest = HttpRequestUtils.build(//
	// MethodWithBody.POST, //
	// System.getProperty("WMO_ENDPOINT"), doc, params);
	//
	// HttpResponse<InputStream> response = downloader.downloadResponse(postRequest);
	//
	// InputStream content = response.body();
	//
	// JSONObject jsonObject = JSONUtils.fromStream(content);
	//
	// String xmlStatus = jsonObject.optString("xmlStatus");
	// String logs = jsonObject.optString("logs");
	// String idResponse = jsonObject.optString("id");
	//
	// System.out.println(xmlStatus + ": " + logs);
	//
	// }
	// System.out.println(xdoc.asString());
	//
	// }

    }

}
