package eu.essi_lab.gssrv.conf.task;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISUtils;
import eu.essi_lab.request.executor.IStatisticsExecutor;

/**
 * @author boldrini
 */
public class WISMetadataTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "WIS metadata task STARTED");

	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String sourceId = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    sourceId = options.trim();
	}
	if (sourceId == null) {

	    log(status, "Source id setting missing, unable to perform task");

	    return;
	}

	run(sourceId);

	log(status, "WIS Metadata task ENDED");

    }

    /**
     * @param targetSourceIdentifier
     */
    private void run(String targetSource) throws Exception {
	// StorageUri databaseURI = ConfigurationWrapper.getDatabaseURI();
	//
	// DatabaseReader dbReader = DatabaseConsumerFactory.createDataBaseReader(databaseURI);

	List<GSSource> allSources = ConfigurationWrapper.getAllSources();
	StatisticsMessage statisticsMessage = new StatisticsMessage();
	// set the required properties
	statisticsMessage.setSources(allSources);
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
	// statisticsMessage.setSharedRepositoryInfo(ConfigurationUtils.getSharedRepositoryInfo());
	WebRequestTransformer.setView(//
		"gs-view-source(" + targetSource + ")", //
		statisticsMessage.getDataBaseURI(), //
		statisticsMessage);

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	// pagination works with grouped results. in this case there is one result item for each
	// source.
	Page page = new Page();
	page.setStart(1);
	page.setSize(1);

	statisticsMessage.setPage(page);

	// computes union of bboxes
	statisticsMessage.computeBboxUnion();

	statisticsMessage.computeTempExtentUnion();

	// computes count distinct of 2 queryables
	statisticsMessage.countDistinct(//
		Arrays.asList(//
			MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER, //
			MetadataElement.UNIQUE_PLATFORM_IDENTIFIER));

	statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);

	StatisticsResponse response = executor.compute(statisticsMessage);

	List<ResponseItem> items = response.getItems();

	Double w = null;
	Double e = null;
	Double s = null;
	Double n = null;
	ResponseItem responseItem = items.get(0);

	JSONObject accessCollection = new JSONObject();
	JSONObject discoveryCollection = new JSONObject();

	String sourceId = responseItem.getGroupedBy().get();
	GSSource source = ConfigurationWrapper.getSource(sourceId);

	String accessId = sourceId;
	String discoveryId = WISRequest.DATASET_DISCOVERY + sourceId;

	accessCollection.put("id", accessId);
	discoveryCollection.put("id", discoveryId);

	Optional<CardinalValues> cardinal = responseItem.getBBoxUnion().getCardinalValues();
	if (cardinal.isPresent()) {
	    w = Double.parseDouble(cardinal.get().getWest());
	    e = Double.parseDouble(cardinal.get().getEast());
	    s = Double.parseDouble(cardinal.get().getSouth());
	    n = Double.parseDouble(cardinal.get().getNorth());
	}

	ComputationResult union = responseItem.getTempExtentUnion();
	System.out.println();

	WISUtils.addGeometry(accessCollection, w, e, s, n);
	WISUtils.addGeometry(discoveryCollection, w, e, s, n);

	JSONObject properties = new JSONObject();

	JSONArray keywords = new JSONArray();
	keywords.put("wis2");
	keywords.put("default");
	keywords.put("wmo");
	keywords.put("whos");
	keywords.put("dab");

	// access collection
	accessCollection.put("id", accessId);
	accessCollection.put("title", "Observations from " + source.getLabel());
	accessCollection.put("description", "Observations from " + accessId);
	accessCollection.put("keywords", keywords);

	// discovery collection
	discoveryCollection.put("id", discoveryId);
	discoveryCollection.put("title", "Discovery dataset metadata from " + source.getLabel());
	discoveryCollection.put("description", "Discovery dataset metadata from " + source.getLabel());
	discoveryCollection.put("keywords", keywords);

	// access collection
	JSONArray accessLinks = new JSONArray();
	// WISUtils.addLink(accessLinks, "OAFeat", "collection", accessId, url + "/collections/" + accessId);
	// WISUtils.addLink(accessLinks, "OARec", "canonical", accessId, url + "/collections/discovery-metadata/items/"
	// + accessId);
	// WISUtils.addLink(accessLinks, "application/json", "root", "The landing page of this server as JSON", url +
	// "?f=json");
	// WISUtils.addLink(accessLinks, "text/html", "root", "The landing page of this server as HTML", url +
	// "?f=html");
	// WISUtils.addLink(accessLinks, "application/json", "self", "This document as JSON", url + "/collections/" +
	// accessId + "?f=json");
	// WISUtils.addLink(accessLinks, "text/html", "alternate", "This document as HTML", url + "/collections/" +
	// accessId + "?f=html");
	// WISUtils.addLink(accessLinks, "application/geo+json", "items", "items as JSON", url + "/collections/" +
	// accessId + "/items?f=json");
	// WISUtils.addLink(accessLinks, "text/html", "items", "items as HTML", url + "/collections/" + accessId +
	// "/items?f=html");
	accessCollection.put("links", accessLinks);
	WISUtils.addExtent(accessCollection, w, e, s, n);
	accessCollection.put("itemType", "feature");

	// discovery collection
	JSONArray discoveryLinks = new JSONArray();
	// WISUtils.addLink(discoveryLinks, "OAFeat", "collection", accessId, url + "/collections/" + accessId);
	// WISUtils.addLink(discoveryLinks, "text/html", "canonical", accessId, url +
	// "/collections/discovery-metadata/items/" + accessId);
	// WISUtils.addLink(discoveryLinks, "application/json", "root", "The landing page of this server as JSON", url +
	// "?f=json");
	// WISUtils.addLink(discoveryLinks, "text/html", "root", "The landing page of this server as HTML", url +
	// "?f=html");
	// WISUtils.addLink(discoveryLinks, "application/json", "self", "This document as JSON",
	// url + "/collections/" + discoveryId + "?f=json");
	// WISUtils.addLink(discoveryLinks, "text/html", "alternate", "This document as HTML", url + "/collections/"
	// + discoveryId + "?f=html");
	// WISUtils.addLink(discoveryLinks, "application/geo+json", "items", "items as JSON",
	// url + "/collections/" + discoveryId + "/items?f=json");
	// WISUtils.addLink(discoveryLinks, "text/html", "items", "items as HTML", url + "/collections/" +
	// discoveryId + "/items?f=html");
	discoveryCollection.put("links", discoveryLinks);
	WISUtils.addExtent(discoveryCollection, w, e, s, n);
	discoveryCollection.put("itemType", "record");

	System.out.println(discoveryCollection.toString());

    }

    @Override
    public String getName() {

	return "WIS metadata task";
    }

}
