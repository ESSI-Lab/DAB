package eu.essi_lab.gssrv.conf.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import org.json.JSONObject;

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

import org.quartz.JobExecutionContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.oaipmh.profile.mapper.wigos.WIGOS_MAPPER;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import tech.units.indriya.AbstractSystemOfUnits;

public class OSCARTask extends AbstractCustomTask {

    public OSCARTask() {
    }

    @Override
    public String getName() {
	return "OSCAR  report task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	// TODO Auto-generated method stub
	Optional<String> taskOptions = readTaskOptions(context);

	String settings = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		settings = options;
	    }
	}
	String sourceId;
	if (settings != null) {
	    sourceId = settings.split(" ")[1];
	} else {
	    GSLoggerFactory.getLogger(getClass()).error("missing source id for Duplicated Online Identifiers task");
	    return;
	}

	// String[] lines = settings.split("\n");
	// String secret = null;
	// String access = null;
	// boolean aggregateMode = false;
	// String aggregatedTarget = null;
	// String dataDir = null;
	// boolean test = false;
	// List<String> sources = new ArrayList<>();
	// for (String line : lines) {
	//
	// }

	WIGOS_MAPPER wigosMapper = new WIGOS_MAPPER();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId("oscar-task-" + sourceId + "-" + UUID.randomUUID());
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	discoveryMessage.setExcludeResourceBinary(false);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond("argentina-ina");
	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);

	discoveryMessage.setSortOrder(SortOrder.ASCENDING);
	discoveryMessage.setSortProperty(ResourceProperty.PRIVATE_ID);
	SearchAfter searchAfter = null;
	int i = 0;
	int start = 0;
	int pageSize = 50;
	Downloader downloader = new Downloader();
	HashMap<String, String> params = new HashMap<String, String>();
	params.put("X-WMO-WMDR-Token", System.getProperty("WMO_TOKEN"));
	main: while (true) {

	    GSLoggerFactory.getLogger(getClass()).info("OSCAR task {} at record {}", sourceId, start);
	    discoveryMessage.setPage(new Page(start, pageSize));
	    start = start + pageSize;

	    discoveryMessage.setPage(new Page(start, pageSize));
	    start = start + pageSize;

	    if (searchAfter != null) {
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
		if (i > 100) {
		    break main;
		}
		Element res = wigosMapper.map(discoveryMessage, resource);
		String doc = XMLDocumentReader.asString(res);
		doc = doc.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		HttpRequest postRequest = HttpRequestUtils.build(//
			MethodWithBody.POST, //
			System.getProperty("WMO_ENDPOINT"), doc, params);

		HttpResponse<InputStream> response = downloader.downloadResponse(postRequest);

		InputStream content = response.body();

		JSONObject jsonObject = JSONUtils.fromStream(content);

		String xmlStatus = jsonObject.optString("xmlStatus");
		String logs = jsonObject.optString("logs");
		String idResponse = jsonObject.optString("id");

		System.out.println(xmlStatus + ": " + logs);

	    }

	    if (resources.isEmpty()) {
		break;
	    }

	}


    }

    public static void main(String[] args) throws Exception {

	WIGOS_MAPPER wigosMapper = new WIGOS_MAPPER();

	String request = "http://localhost:9090/gs-service/services/essi/token/whos-d40a452b-b865-4fbe-8165-43a96ebf1b3d/view/gs-view-source(argentina-ina)/oaipmh?verb=ListRecords&metadataPrefix=WIGOS-1.0&resumptionToken=restoken/101/null/null/null/WIGOS-1.0/1729189908361";

	Downloader d = new Downloader();
	Optional<String> resp = d.downloadOptionalString(request);

	if (resp.isPresent()) {
	    XMLDocumentReader xdoc = new XMLDocumentReader(resp.get());

	    Node[] nodes = xdoc.evaluateNodes("//*:WIGOSMetadataRecord");

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("X-WMO-WMDR-Token", System.getProperty("WMO_TOKEN"));

	    Downloader downloader = new Downloader();

	    for (Node node : nodes) {
		String doc = XMLDocumentReader.asString(node);
		doc = doc.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		HttpRequest postRequest = HttpRequestUtils.build(//
			MethodWithBody.POST, //
			System.getProperty("WMO_ENDPOINT"), doc, params);

		HttpResponse<InputStream> response = downloader.downloadResponse(postRequest);

		InputStream content = response.body();

		JSONObject jsonObject = JSONUtils.fromStream(content);

		String xmlStatus = jsonObject.optString("xmlStatus");
		String logs = jsonObject.optString("logs");
		String idResponse = jsonObject.optString("id");

		System.out.println(xmlStatus + ": " + logs);

	    }
	    System.out.println(xdoc.asString());

	}

    }

}
