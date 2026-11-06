package eu.essi_lab.gssrv.conf.task;

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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.quartz.JobExecutionContext;

import eu.essi_lab.accessor.hiscentral.deflusso.HISCentralRatingCurveAugmenter;
import eu.essi_lab.accessor.hiscentral.deflusso.HISCentralRatingCurvesMapper;
import eu.essi_lab.accessor.hiscentral.deflusso.RatingCurvesClient;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ratings.RatingCurves;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * Harvesting embedded task to attach to a HIS-Central regional source (e.g. ita-sir-val-d-aosta).<br>
 * At the end of each harvesting of the source, it reads the rating curves (scale di deflusso) from the SharePoint
 * sub-folder named after the source identifier, maps them to datasets, enriches them with the metadata of the
 * matching station already harvested in the same source (see {@link HISCentralRatingCurveAugmenter}) and stores
 * them in the source data folder. Rating curves of the source no longer available on SharePoint are removed.<br>
 * <br>
 * Since the records are stored in the source own data folder, they are dropped by every full harvesting of the
 * source and re-created by this task at its end.<br>
 * <br>
 * Task options:
 *
 * <pre>
 * endpoint=https://tenant-my.sharepoint.com/:f:/g/... (sharing link of the rating curves root folder)
 * </pre>
 *
 * @author boldrini
 */
public class HISCentralRatingCurvesTask extends AbstractEmbeddedTask {

    public enum HISCentralRatingCurvesTaskOptions implements OptionsKey {
	ENDPOINT;
    }

    private static final int PAGE_SIZE = 500;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "HIS-Central rating curves task STARTED");

	Optional<GSSource> optSource = getSource();
	if (optSource.isEmpty()) {
	    status.addWarningMessage("No source available, HIS-Central rating curves task must be embedded in a harvesting");
	    return;
	}

	GSSource source = optSource.get();
	String sourceId = source.getUniqueIdentifier();

	Optional<String> endpoint = readTaskOptions(context, HISCentralRatingCurvesTaskOptions.class)//
		.map(options -> options.get(HISCentralRatingCurvesTaskOptions.ENDPOINT))//
		.filter(e -> !e.isBlank());

	if (endpoint.isEmpty()) {
	    status.addWarningMessage("Missing 'endpoint' task option, HIS-Central rating curves task skipped");
	    return;
	}

	//
	// reads the rating curves of this source from SharePoint.
	// in case of errors an exception is thrown and the existing rating curves are preserved
	//

	List<RatingCurves> stations = new RatingCurvesClient(endpoint.get()).harvest(sourceId);

	log(status, "Found " + stations.size() + " rating curves for source " + sourceId);

	//
	// maps and augments
	//

	HISCentralRatingCurvesMapper mapper = new HISCentralRatingCurvesMapper();
	HISCentralRatingCurveAugmenter augmenter = new HISCentralRatingCurveAugmenter();

	List<GSResource> resources = new ArrayList<>();

	for (RatingCurves station : stations) {

	    try {

		OriginalMetadata originalMD = HISCentralRatingCurvesMapper.create(endpoint.get(), station);

		GSResource resource = mapper.map(originalMD, source);

		// same as the harvesting identifier decorator: private id from the (SHA-1) original id
		String originalId = resource.getOriginalId().orElseThrow(() -> new IllegalStateException("Missing original id"));
		resource.setPrivateId(originalId);
		resource.setPublicId(originalId);

		augmenter.augment(resource);

		IndexedElementsWriter.write(resource);

		resources.add(resource);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to create rating curve of station {}", station.getStationIdentifier(),
			ex);

		status.addWarningMessage("Unable to create rating curve of station " + station.getStationIdentifier() + ": " + ex.getMessage());
	    }
	}

	//
	// stores in the source data folder
	//

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());
	SourceStorage storage = database.getStorage(sourceId);
	DatabaseFolder folder = database.findWritingFolder(storage);

	GSLoggerFactory.getLogger(getClass()).debug("Target data folder: {}", folder.getName());

	Set<String> storedIds = new HashSet<>();

	for (GSResource resource : resources) {

	    folder.upsert(resource.getPrivateId(), FolderEntry.of(resource.asDocument(true)), EntryType.GS_RESOURCE);

	    storedIds.add(resource.getPrivateId());
	}

	log(status, "Stored " + storedIds.size() + " rating curves in folder " + folder.getName());

	//
	// removes the rating curves no longer available
	//

	int removed = 0;

	for (String privateId : findRatingCurvesIds(sourceId)) {

	    if (!storedIds.contains(privateId) && folder.exists(privateId)) {

		folder.remove(privateId);
		removed++;
	    }
	}

	log(status, "Removed " + removed + " rating curves no longer available");

	log(status, "HIS-Central rating curves task ENDED");
    }

    /**
     * @param sourceId
     * @return the private ids of the rating curves currently stored for the given source
     * @throws Exception
     */
    private List<String> findRatingCurvesIds(String sourceId) throws Exception {

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

	LogicalBond bond = BondFactory.createAndBond(//
		BondFactory.createSourceIdentifierBond(sourceId), //
		BondFactory.createIsRatingCurveBond(true));

	DiscoveryMessage message = new DiscoveryMessage();
	message.setSources(List.of(ConfigurationWrapper.getSource(sourceId)));
	message.setPermittedBond(bond);
	message.setNormalizedBond(bond);
	message.setUserBond(bond);
	message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	message.setExcludeResourceBinary(true);

	ResourceSelector selector = new ResourceSelector();
	selector.addIndex(ResourceProperty.PRIVATE_ID);
	message.setResourceSelector(selector);

	List<String> ids = new ArrayList<>();

	int start = 1;

	while (true) {

	    message.setPage(new Page(start, PAGE_SIZE));

	    ResultSet<GSResource> resultSet = finder.discover(message);
	    List<GSResource> results = resultSet.getResultsList();

	    results.forEach(r -> ids.add(r.getPrivateId()));

	    if (results.size() < PAGE_SIZE) {
		break;
	    }

	    start += PAGE_SIZE;
	}

	return ids;
    }

    @Override
    public String getName() {

	return "HIS-Central rating curves task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }
}
