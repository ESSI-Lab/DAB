package eu.essi_lab.shared.driver.es.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;

/**
 * Stores harvesting statistics in the {@code {dbName}-harvests} index of the database configured in the "DAB statistics
 * gathering" setting (the same database of the requests statistics, stored in the {@code {dbName}-request} index).<br>
 * Documents are written asynchronously, and nothing is written if the statistics gathering is disabled
 *
 * @author boldrini
 */
public class ElasticsearchHarvestingPublisher {

    /**
     *
     */
    public static final String HARVESTS_INDEX = "harvests";

    private static Optional<String> awsTaskId;

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor(r -> {

	Thread thread = new Thread(r);
	thread.setPriority(Thread.MIN_PRIORITY);
	thread.setName(thread.getName() + "_ES_HARVESTING_PUBLISHER");

	return thread;
    });

    /**
     * Asynchronously writes (creates or replaces) the given harvesting statistics document. The host name and the
     * execution mode are added to the document
     *
     * @param id the document identifier
     * @param document
     */
    public static void publish(String id, JSONObject document) {

	JSONObject copy = new JSONObject(document.toString());
	copy.put("hostName", HostNamePropertyUtils.getHostNameProperty());
	copy.put("executionMode", ExecutionMode.get().name());
	getAwsTaskId().ifPresent(taskId -> copy.put("awsTaskId", taskId));

	String docId = id != null ? id : UUID.randomUUID().toString();

	execute("Storing harvesting statistics " + docId, client -> {

	    HashMap<String, String> items = new HashMap<>();
	    items.put(docId, copy.toString());

	    client.write(HARVESTS_INDEX, items);
	});
    }

    /**
     * Asynchronously adds or replaces the given fields of the harvesting statistics document with the given id, only if
     * its <code>result</code> field has the given value
     *
     * @param id the document identifier
     * @param fields
     * @param expectedResult
     */
    public static void update(String id, JSONObject fields, String expectedResult) {

	execute("Updating harvesting statistics " + id, client -> {

	    boolean updated = client.conditionalUpdate(HARVESTS_INDEX, id, fields, "result", expectedResult);

	    if (!updated) {

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class)
			.info("Harvesting statistics {} not updated (missing or with result other than {})", id, expectedResult);
	    }
	});
    }

    /**
     * Executes the given action in the publisher thread, which is single, so actions are executed in the submission
     * order
     *
     * @param description
     * @param action
     */
    private static void execute(String description, Consumer<ElasticsearchClient> action) {

	Optional<StatisticsDatabase> database = getStatisticsDatabase();

	if (database.isEmpty()) {

	    return;
	}

	THREAD_POOL.execute(() -> {

	    ElasticsearchClient client = null;

	    try {

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).info("{} STARTED", description);

		client = database.get().createClient();
		client.init(HARVESTS_INDEX);

		action.accept(client);

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).info("{} ENDED", description);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).error("{} failed: {}", description, ex.getMessage(),
			ex);

	    } finally {

		close(client);
	    }
	});
    }

    /**
     * Creates a client to the "DAB statistics gathering" database, if configured. The caller must close the client
     *
     * @return
     */
    public static Optional<ElasticsearchClient> createClient() {

	return getStatisticsDatabase().map(StatisticsDatabase::createClient);
    }

    /**
     * @param client
     */
    public static void close(ElasticsearchClient client) {

	if (client != null) {

	    try {
		client.close();
	    } catch (Exception ex) {
		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).warn("Unable to close client: {}", ex.getMessage());
	    }
	}
    }

    /**
     * @return the ECS task id of this node, if any. The id is retrieved only once, since it requires an HTTP request
     */
    private static synchronized Optional<String> getAwsTaskId() {

	if (awsTaskId == null) {

	    awsTaskId = HostNamePropertyUtils.getAWSTaskId();
	}

	return awsTaskId;
    }

    /**
     * @param endpoint
     * @param dbName
     * @param user
     * @param password
     */
    private record StatisticsDatabase(String endpoint, String dbName, String user, String password) {

	/**
	 * @return
	 */
	ElasticsearchClient createClient() {

	    ElasticsearchClient client = new ElasticsearchClient(endpoint, user, password);
	    client.setDbName(dbName);

	    return client;
	}
    }

    /**
     * @return
     */
    private static Optional<StatisticsDatabase> getStatisticsDatabase() {

	Optional<DatabaseSetting> setting;

	try {

	    setting = ConfigurationWrapper.getSystemSettings().getStatisticsSetting();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).error("Unable to read statistics setting: {}",
		    ex.getMessage(), ex);
	    return Optional.empty();
	}

	if (setting.isEmpty()) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class)
		    .debug("Statistics gathering disabled, harvesting statistics not available");
	    return Optional.empty();
	}

	DatabaseSetting databaseSetting = setting.get();

	String endpoint = databaseSetting.getDatabaseUri();
	String dbName = databaseSetting.getDatabaseName();
	String user = databaseSetting.getDatabaseUser();
	String password = databaseSetting.getDatabasePassword();

	if (endpoint == null || dbName == null || user == null || password == null) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class)
		    .warn("Statistics database options missing, harvesting statistics not stored");
	    return Optional.empty();
	}

	return Optional.of(new StatisticsDatabase(endpoint, dbName, user, password));
    }
}
