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

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor(r -> {

	Thread thread = new Thread(r);
	thread.setPriority(Thread.MIN_PRIORITY);
	thread.setName(thread.getName() + "_ES_HARVESTING_PUBLISHER");

	return thread;
    });

    /**
     * Asynchronously writes the given harvesting statistics document. The host name and the execution mode are added to
     * the document
     *
     * @param id the document identifier
     * @param document
     */
    public static void publish(String id, JSONObject document) {

	Optional<DatabaseSetting> setting;

	try {

	    setting = ConfigurationWrapper.getSystemSettings().getStatisticsSetting();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).error("Unable to read statistics setting: {}",
		    ex.getMessage(), ex);
	    return;
	}

	if (setting.isEmpty()) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class)
		    .debug("Statistics gathering disabled, harvesting statistics not stored");
	    return;
	}

	DatabaseSetting databaseSetting = setting.get();

	String endpoint = databaseSetting.getDatabaseUri();
	String dbName = databaseSetting.getDatabaseName();
	String user = databaseSetting.getDatabaseUser();
	String password = databaseSetting.getDatabasePassword();

	if (endpoint == null || dbName == null || user == null || password == null) {

	    GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class)
		    .warn("Statistics database options missing, harvesting statistics not stored");
	    return;
	}

	JSONObject copy = new JSONObject(document.toString());
	copy.put("hostName", HostNamePropertyUtils.getHostNameProperty());
	copy.put("executionMode", ExecutionMode.get().name());

	String docId = id != null ? id : UUID.randomUUID().toString();

	THREAD_POOL.execute(() -> {

	    ElasticsearchClient client = null;

	    try {

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).info("Storing harvesting statistics {} STARTED", docId);

		client = new ElasticsearchClient(endpoint, user, password);
		client.setDbName(dbName);
		client.init(HARVESTS_INDEX);

		HashMap<String, String> items = new HashMap<>();
		items.put(docId, copy.toString());

		client.write(HARVESTS_INDEX, items);

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).info("Storing harvesting statistics {} ENDED", docId);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).error("Unable to store harvesting statistics: {}",
			ex.getMessage(), ex);

	    } finally {

		if (client != null) {

		    try {
			client.close();
		    } catch (Exception ex) {
			GSLoggerFactory.getLogger(ElasticsearchHarvestingPublisher.class).warn("Unable to close client: {}",
				ex.getMessage());
		    }
		}
	    }
	});
    }
}
