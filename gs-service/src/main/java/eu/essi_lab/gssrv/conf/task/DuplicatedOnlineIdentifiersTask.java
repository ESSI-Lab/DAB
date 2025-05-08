package eu.essi_lab.gssrv.conf.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.ommdk.GMIResourceMapper;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

public class DuplicatedOnlineIdentifiersTask extends AbstractCustomTask {

    public DuplicatedOnlineIdentifiersTask() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getName() {
	return "Duplicated Online Identifiers task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("Duplicated Online Identifiers STARTED");
	log(status, "Duplicated Online Identifiers STARTED");
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
	boolean test = true;
	String tmpSourcedir = Files.createTempDirectory("duplicated-task-" + sourceId).toFile().getAbsolutePath();
	GSLoggerFactory.getLogger(getClass()).info("Created duplicated dir {}", tmpSourcedir);
	downloadData(sourceId, tmpSourcedir, test);

    }

    private void downloadData(String sourceId, String tmpSourcedir, boolean test) throws Exception {
	int pageSize = 250;
	File sourceDir = new File(tmpSourcedir);
	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setRequestId("duplicated-online-id-task-" + sourceId + "-" + UUID.randomUUID());
	discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	discoveryMessage.getResourceSelector().setSubset(ResourceSubset.FULL);
	discoveryMessage.setExcludeResourceBinary(false);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	ResourcePropertyBond bond = BondFactory.createSourceIdentifierBond(sourceId);
	discoveryMessage.setPermittedBond(bond);
	discoveryMessage.setUserBond(bond);
	discoveryMessage.setNormalizedBond(bond);
	GMIResourceMapper mapper = new GMIResourceMapper();

	int start = 1;
	int file = 0;

	discoveryMessage
		.setSortedFields(new SortedFields(Arrays.asList(new SimpleEntry(ResourceProperty.PRIVATE_ID,SortOrder.ASCENDING))));

	SearchAfter searchAfter = null;
	int i = 0;
	Set<String> setIds = new HashSet<String>();
	Map<String, List<String>> idMap = new HashMap<String, List<String>>();
	main: while (true) {

	    // CHECKING CANCELED JOB

	    // if (ConfigurationWrapper.isJobCanceled(context)) {
	    // GSLoggerFactory.getLogger(getClass()).info("Turtle task CANCELED");
	    // log(status, "Turtle task CANCELED");
	    // status.setPhase(JobPhase.CANCELED);
	    // return;
	    // }

	    GSLoggerFactory.getLogger(getClass()).info("Duplicated Online identifier task {} at record {}", sourceId, start);
	    discoveryMessage.setPage(new Page(start, pageSize));
	    start = start + pageSize;

	    if (searchAfter != null) {
		discoveryMessage.setSearchAfter(searchAfter);
	    }
	    ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	    if (resultSet.getSearchAfter().isPresent()) {
		searchAfter = resultSet.getSearchAfter().get();
	    }
	    List<GSResource> resources = resultSet.getResultsList();

	    for (GSResource resource : resources) {
		i++;
		// if (test && i > 2000) {
		// break main;
		// }

		CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();
		if (coreMetadata != null) {
		    String fileIdentifier = coreMetadata.getIdentifier();
		    Online online = coreMetadata.getOnline();
		    String publicationDate = coreMetadata.getDataIdentification().getCitationPublicationDate();
		    if (online == null) {
			continue;
		    }
		    String onlineId = online.getIdentifier();

		    if (idMap.containsKey(onlineId)) {
			List<String> currentList = idMap.get(onlineId);
			currentList.add(fileIdentifier);
			idMap.put(onlineId, currentList);
			File temp = new File(sourceDir, onlineId + ".txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
			String text = "ONLINE_ID:" + onlineId + " - FILE_IDENTIFIER:" + String.join(";", currentList)
				+ " - PUBLICATION_DATE: " + publicationDate;
			GSLoggerFactory.getLogger(getClass()).info(text);
			writer.write(text);
			writer.close();
		    } else {
			List<String> newList = new ArrayList<String>();
			newList.add(fileIdentifier);
			idMap.put(onlineId, newList);
			// setIds.add(onlineId);
		    }
		}

	    }

	    if (resources.isEmpty()) {
		break main;
	    }
	}

	for (Map.Entry<String, List<String>> entry : idMap.entrySet()) {

	    String key = entry.getKey();
	    List<String> listIds = entry.getValue();
	    if (listIds.size() > 1) {
		GSLoggerFactory.getLogger(getClass()).info("ONLINE_ID:" + key + " - FILE_IDENTIFIERS:" + String.join(";", listIds));
	    }

	}

    }

}
