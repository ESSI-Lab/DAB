package eu.essi_lab.gssrv.conf.task;

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

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.ResponseException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkRequest.Builder;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.UpdateOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author boldrini
 */
public class DeploymentUpdaterTask extends AbstractCustomTask {
    private static final double MEGABYTE = 1024L * 1024L;
    private static final int MAX_PAYLOAD_SIZE = 50000000;// 900 MB

    // One usages is expected, with the aim of updating record source deployment
    //
    // source: s1;s2;s3
    // deployment: whos;whos-test

    public enum DeploymentUpdaterTaskKey {
	SOURCE_KEY("source:"), //
	DEPLOYMENT_KEY("deployment:"), //

	;

	private String id;

	DeploymentUpdaterTaskKey(String id) {
	    this.id = id;
	}

	@Override
	public String toString() {
	    return id;
	}

	public static String getExpectedKeys() {
	    String ret = "";
	    for (DeploymentUpdaterTaskKey key : values()) {
		ret += key.toString() + " ";
	    }
	    return ret;
	}

	public static SimpleEntry<DeploymentUpdaterTaskKey, String> decodeLine(String line) {
	    for (DeploymentUpdaterTaskKey key : values()) {
		if (line.toLowerCase().startsWith(key.toString().toLowerCase())) {
		    SimpleEntry<DeploymentUpdaterTaskKey, String> ret = new SimpleEntry<>(key,
			    line.substring(key.toString().length()).trim());
		    return ret;
		}
	    }
	    return null;
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("Deployment udpate task STARTED");
	log(status, "Deployment udpate task STARTED");

	// SETTINGS RETRIEVAL
	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String settings = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		settings = options;
	    }
	}
	if (settings == null) {
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for updater task");
	    return;
	}
	String[] lines = settings.split("\n");
	String source = null;
	String deployment = null;
	for (String line : lines) {
	    SimpleEntry<DeploymentUpdaterTaskKey, String> decoded = DeploymentUpdaterTaskKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass())
			.error("unexpected settings for updater task. Expected: " + DeploymentUpdaterTaskKey.getExpectedKeys());
		return;
	    }
	    switch (decoded.getKey()) {
	    case SOURCE_KEY:
		source = decoded.getValue();
		break;
	    case DEPLOYMENT_KEY:
		deployment = decoded.getValue();
		break;
	    default:
		break;
	    }
	}
	if (source == null || deployment == null) {
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for updater task");
	    return;
	}

	OpenSearchDatabase database = new OpenSearchDatabase();

	database.initialize(ConfigurationWrapper.getStorageInfo());
	setDeploymentsToSources(database, source.trim().split(";"), deployment.trim().split(";"));
    }

    public static void setDeploymentsToSources(OpenSearchDatabase db, String[] sources, String[] deployments) throws Exception {

	String index = DataFolderMapping.get().getIndex();

	OpenSearchClient client = db.getClient();

	OpenSearchWrapper wrapper = new OpenSearchWrapper(db);

	for (String sourceId : sources) {

	    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Source id: {}", sourceId);

	    String dataFolderTag = db.getMetaFolders().stream().map(dir -> (OpenSearchFolder) dir)
		    .filter(dir -> sourceId.equals(DatabaseFolder.computeSourceId("production", dir.getName()))).//
		    map(dir -> {
			try {
			    return dir.getSourceWrapper(DatabaseFolder.computeSourceId("production", dir.getName()) + "_dataFolder")
				    .getDataFolder().get();
			} catch (Exception e) {

			    e.printStackTrace();
			}

			return null;
		    }).findFirst().get();

	    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Data folder tag: {}", dataFolderTag);

	    OpenSearchFolder dataFolder = new OpenSearchFolder(db, "production_" + sourceId + "-" + dataFolderTag);

	    int size = dataFolder.size();

	    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Data folder size: {}", StringUtils.format(size));

	    //
	    //
	    //

	    int processedRecords = 0;

	    Query searchQuery = OpenSearchQueryBuilder.buildFolderEntriesQuery(dataFolder);

	    int requestSize = 100;

	    Optional<SearchAfter> searchAfter = Optional.empty();

	    while (processedRecords < size) {

		GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Processing records [{}/{}-{}]  STARTED",

			StringUtils.format(processedRecords), //
			StringUtils.format(processedRecords + requestSize), //
			StringUtils.format(size)//
		);//

		SearchResponse<Object> response = wrapper.search(

			index, //
			searchQuery, //
			Arrays.asList(), // fields
			0, // start index is ignored when using search after, no need to update it
			requestSize, //
			Optional.of(SortedFields.of(ResourceProperty.RESOURCE_TIME_STAMP, SortOrder.ASCENDING)), // sortOrder
			searchAfter, // search after
			false, // request cache
			false);// exclude res binary

		//
		//
		//

		List<JSONObject> jsonSources = OpenSearchUtils.toJSONSourcesList(response);

		JSONArray array = new JSONArray();
		for (String deployment : deployments) {
		    array.put(deployment);
		}

		jsonSources.forEach(source -> {

		    source.put(ResourceProperty.SOURCE_DEPLOYMENT.getName(), array);
		    source.put(IndexMapping.toKeywordField(ResourceProperty.SOURCE_DEPLOYMENT.getName()), array);
		});

		update(db, dataFolder, jsonSources);

		//
		//
		//

		searchAfter = getSearchAfter(response);

		GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Processing records [{}/{}-{}]  ENDED",

			StringUtils.format(processedRecords), //
			StringUtils.format(processedRecords + requestSize), //
			StringUtils.format(size)//
		);//

		processedRecords += requestSize;
	    }

	}
    }

    /**
     * @param response
     */
    private static Optional<SearchAfter> getSearchAfter(SearchResponse<Object> response) {

	List<Hit<Object>> hits = response.hits().hits();
	int size = hits.size();
	if (size > 0) {

	    Hit<Object> hit = hits.get(size - 1);
	    List<FieldValue> sortVals = hit.sortVals();

	    if (!sortVals.isEmpty()) {

		FieldValue fieldValue = sortVals.get(0);

		if (fieldValue.isString()) {

		    return Optional.of(SearchAfter.of(fieldValue.stringValue()));

		} else if (fieldValue.isDouble()) {

		    return Optional.of(SearchAfter.of(fieldValue.doubleValue()));

		} else if (fieldValue.isLong()) {

		    return Optional.of(SearchAfter.of(fieldValue.longValue()));
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * @param dataDirFiles
     * @param openSearchDb
     * @param dataFolder
     * @param mlDbId
     * @throws OpenSearchException
     * @throws IOException
     * @throws JAXBException
     */
    private static void update(OpenSearchDatabase openSearchDb, OpenSearchFolder folder, List<JSONObject> sources) {

	Builder bulkBuilder = new BulkRequest.Builder().index(DataFolderMapping.get().getIndex());

	int payloadSize = 0;
	List<BulkOperation> opList = new ArrayList<>();
	int storedResources = 0;
	int processedResources = 0;

	boolean writingFolderTagstored = false;
	int skipped = 0;

	for (JSONObject source : sources) {

	    JSONArray array = source.optJSONArray(ResourceProperty.PRIVATE_ID.getName());
	    
	    if (array==null) {
		System.err.println("error for record in folder: "+folder.getName());
		continue;
	    }

	    String first = array.getString(0).toString();

	    String id = OpenSearchFolder.getEntryId(folder, first);

	    payloadSize += source.toString().length();

	    Map<String, Object> data = source.toMap();

	    UpdateOperation<Map<String, Object>> updateOperation = new UpdateOperation.Builder<Map<String, Object>>().//
		    document(data).//
		    id(id).//
		    index(DataFolderMapping.get().getIndex()).//
		    build();

	    BulkOperation bulkOperation = new BulkOperation.Builder().update(updateOperation).build();
	    opList.add(bulkOperation);

	    processedResources++;

	    if (payloadSize > MAX_PAYLOAD_SIZE || processedResources == sources.size()) {

		GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info(//
			"Payload size bytes/MB: {}/{} ", //
			StringUtils.format(payloadSize), //
			StringUtils.format(bytesToMeg(payloadSize)));

		if (payloadSize > MAX_PAYLOAD_SIZE) {

		    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Max payload size reached");

		} else {

		    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("All sources processed");
		}

		GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Executing bulk operation STARTED");

		try {

		    BulkRequest bulkRequest = bulkBuilder.operations(opList).build();

		    BulkResponse bulkResponse = openSearchDb.getClient().bulk(bulkRequest);

		    int errors = 0;

		    if (bulkResponse.errors()) {

			List<JSONObject> items = bulkResponse.items().//
				stream().//
				filter(item -> item.error() != null).//
				map(item -> new JSONObject(item.toJsonString())).//
				map(item -> item.put("dataDir", Thread.currentThread().getName())).//
				map(item -> item.put("type", "index error")).//
				collect(Collectors.toList());

			errors = items.size();

		    }

		} catch (ResponseException ex) {

		    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).error(ex);

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).error(ex);

		} finally {

		    GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Executing bulk operation ENDED");
		}

		bulkBuilder = new BulkRequest.Builder().index(DataFolderMapping.get().getIndex());

		payloadSize = 0;
		opList.clear();
	    }
	}

	GSLoggerFactory.getLogger(DeploymentUpdaterTask.class).info("Skipped files: {}", skipped);
    }

    /**
     * @param bytes
     * @return
     */
    public static double bytesToMeg(double bytes) {

	return bytes / MEGABYTE;
    }

    @Override
    public String getName() {

	return "Deployment updater task";
    }
}
