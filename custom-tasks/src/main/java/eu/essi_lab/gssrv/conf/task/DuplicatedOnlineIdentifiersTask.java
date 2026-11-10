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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.UUID;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.gs.task.OptionsParser;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.DiscoveryExecutor;
import eu.essi_lab.request.executor.StatisticsExecutor;

/**
 * Finds the online identifiers shared by more than one record of a source.<br>
 * The duplicated online identifiers are found by the database with a single term frequency query (the terms with
 * frequency greater than one), then the identifiers and publication dates of the records sharing them are retrieved
 * with index-only discovery queries, so that the cost depends on the number of duplicates and not on the size of the
 * source.<br>
 * The source is given with the <code>sourceId=...</code> option (the former <code>sourceId ...</code> form is also
 * supported). The duplicates are reported in the task status and in a report file in a temporary folder of the node
 * running the task. Nothing is modified
 *
 * @author roncella
 * @author boldrini
 */
public class DuplicatedOnlineIdentifiersTask extends AbstractCustomTask {

    public enum DuplicatedOnlineIdentifiersTaskOptions implements OptionsKey {
	SOURCE_ID;
    }

    /**
     * Initial number of most frequent online identifiers requested, doubled until a non duplicated one is found
     */
    private static final int INITIAL_FREQUENCY_ITEMS = 1000;

    /**
     * Maximum number of most frequent online identifiers requested (below the OpenSearch default max buckets)
     */
    private static final int MAX_FREQUENCY_ITEMS = 64000;

    /**
     * Number of online identifiers per discovery query
     */
    private static final int ONLINE_IDS_PER_QUERY = 200;

    private static final int PAGE_SIZE = 1000;

    /**
     * Maximum number of duplicates reported in the task status (all of them are in the report file and in the log)
     */
    private static final int MAX_STATUS_DUPLICATES = 50;

    @Override
    public String getName() {
	return "Duplicated Online Identifiers task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Duplicated Online Identifiers STARTED");

	Optional<String> sourceId = readSourceId(context);
	if (sourceId.isEmpty()) {
	    status.setPhase(JobPhase.ERROR);
	    log(status, "Missing source id: set the sourceId=<source identifier> option");
	    return;
	}

	Optional<GSSource> source = ConfigurationWrapper.getAllSources().stream()
		.filter(s -> s.getUniqueIdentifier().equals(sourceId.get())).findFirst();
	if (source.isEmpty()) {
	    status.setPhase(JobPhase.ERROR);
	    log(status, "Source " + sourceId.get() + " not found in the configuration");
	    return;
	}

	//
	// 1) duplicated online identifiers, with the number of records sharing each one
	//

	Map<String, Integer> duplicates = findDuplicatedOnlineIds(source.get());

	log(status, "Found " + duplicates.size() + " online identifiers shared by more than one record of source "
		+ source.get().getLabel() + " (" + sourceId.get() + ")");

	if (duplicates.isEmpty()) {
	    log(status, "Duplicated Online Identifiers ENDED");
	    return;
	}

	//
	// 2) records sharing them
	//

	Map<String, List<String>> records = new TreeMap<>();

	List<String> onlineIds = new ArrayList<>(duplicates.keySet());
	for (int i = 0; i < onlineIds.size(); i += ONLINE_IDS_PER_QUERY) {

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		status.setPhase(JobPhase.CANCELED);
		log(status, "Duplicated Online Identifiers CANCELED");
		return;
	    }

	    List<String> batch = onlineIds.subList(i, Math.min(i + ONLINE_IDS_PER_QUERY, onlineIds.size()));
	    retrieveRecords(source.get(), batch, records);
	}

	//
	// 3) report
	//

	List<String> lines = new ArrayList<>();
	for (String onlineId : onlineIds) {
	    List<String> ids = records.getOrDefault(onlineId, List.of());
	    lines.add("ONLINE_ID: " + onlineId + " - RECORDS: " + duplicates.get(onlineId) + " - FILE_IDENTIFIERS (PUBLICATION_DATE): "
		    + String.join("; ", ids));
	}

	lines.forEach(line -> GSLoggerFactory.getLogger(getClass()).info(line));
	lines.stream().limit(MAX_STATUS_DUPLICATES).forEach(line -> log(status, line, false));
	if (lines.size() > MAX_STATUS_DUPLICATES) {
	    log(status, "... and " + (lines.size() - MAX_STATUS_DUPLICATES) + " more, see the report file", false);
	}

	File report = Files.createTempFile("duplicated-online-ids-" + sourceId.get() + "-", ".txt").toFile();
	Files.write(report.toPath(), lines, StandardCharsets.UTF_8);
	log(status, "Report file: " + report.getAbsolutePath() + " (on the node running this task)");

	log(status, "Duplicated Online Identifiers ENDED");
    }

    /**
     * @param context
     * @return the source identifier, from the <code>sourceId=...</code> option or the former <code>sourceId ...</code>
     *         form
     */
    private Optional<String> readSourceId(JobExecutionContext context) {

	Optional<String> options = readTaskOptions(context);
	if (options.isEmpty() || options.get().isBlank()) {
	    return Optional.empty();
	}

	String raw = options.get().trim();
	if (raw.contains("=")) {
	    EnumMap<DuplicatedOnlineIdentifiersTaskOptions, String> parsed = OptionsParser.parseOptions(raw,
		    DuplicatedOnlineIdentifiersTaskOptions.class);
	    return Optional.ofNullable(parsed.get(DuplicatedOnlineIdentifiersTaskOptions.SOURCE_ID)).filter(s -> !s.isBlank());
	}

	String[] split = raw.split("\\s+");
	return Optional.of(split.length > 1 ? split[1] : split[0]);
    }

    /**
     * Requests the most frequent online identifiers of the source, increasing the number of requested items until the
     * least frequent one is not duplicated
     *
     * @param source
     * @return the duplicated online identifiers, with the number of records sharing each one, most frequent first
     * @throws Exception
     */
    private Map<String, Integer> findDuplicatedOnlineIds(GSSource source) throws Exception {

	ServiceLoader<StatisticsExecutor> loader = ServiceLoader.load(StatisticsExecutor.class);
	StatisticsExecutor executor = loader.iterator().next();

	int maxItems = INITIAL_FREQUENCY_ITEMS;

	while (true) {

	    StatisticsMessage message = new StatisticsMessage();
	    message.setRequestId("duplicated-online-id-task-" + source.getUniqueIdentifier() + "-" + UUID.randomUUID());
	    message.setSources(List.of(source));
	    message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	    message.setUserBond(BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier()));
	    Page page = new Page();
	    page.setStart(1);
	    page.setSize(1);
	    message.setPage(page);
	    message.computeFrequency(Arrays.asList(MetadataElement.ONLINE_ID), maxItems);

	    StatisticsResponse response = executor.compute(message);
	    if (response == null) {
		throw new Exception("Statistics query failed, see the log for details");
	    }

	    List<TermFrequencyItem> items = new ArrayList<>();
	    for (ResponseItem item : response.getItems()) {
		Optional<ComputationResult> frequency = item.getFrequency(MetadataElement.ONLINE_ID);
		if (frequency.isPresent() && frequency.get().getValue() != null) {
		    items.addAll(frequency.get().getFrequencyItems());
		}
	    }

	    boolean complete = items.size() < maxItems || items.getLast().getFreq() < 2 || maxItems >= MAX_FREQUENCY_ITEMS;

	    if (complete) {
		if (items.size() == maxItems && items.getLast().getFreq() > 1) {
		    GSLoggerFactory.getLogger(getClass()).warn("More than {} duplicated online identifiers, only the first are reported",
			    maxItems);
		}

		Map<String, Integer> ret = new LinkedHashMap<>();
		items.stream().filter(item -> item.getFreq() > 1).forEach(item -> ret.put(item.getTerm(), item.getFreq()));
		return ret;
	    }

	    maxItems = Math.min(maxItems * 2, MAX_FREQUENCY_ITEMS);
	}
    }

    /**
     * Retrieves the identifier and the publication date of the records of the source with the given online identifiers,
     * reading only the indexes
     *
     * @param source
     * @param onlineIds
     * @param records filled with the "identifier (publication date)" of the records, per online identifier
     * @throws Exception
     */
    private void retrieveRecords(GSSource source, List<String> onlineIds, Map<String, List<String>> records) throws Exception {

	ServiceLoader<DiscoveryExecutor> loader = ServiceLoader.load(DiscoveryExecutor.class);
	DiscoveryExecutor executor = loader.iterator().next();

	List<Bond> onlineIdBonds = new ArrayList<>();
	for (String onlineId : onlineIds) {
	    onlineIdBonds.add(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ONLINE_ID, onlineId));
	}
	Bond onlineIdBond = onlineIdBonds.size() == 1 ? onlineIdBonds.getFirst() : BondFactory.createOrBond(onlineIdBonds);
	ResourcePropertyBond sourceBond = BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier());
	Bond bond = BondFactory.createAndBond(sourceBond, onlineIdBond);

	SearchAfter searchAfter = null;

	do {
	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setRequestId("duplicated-online-id-task-" + source.getUniqueIdentifier() + "-" + UUID.randomUUID());
	    message.setExcludeResourceBinary(true);

	    ResourceSelector selector = new ResourceSelector();
	    selector.addIndex(MetadataElement.IDENTIFIER);
	    selector.addIndex(MetadataElement.ONLINE_ID);
	    selector.addIndex(MetadataElement.PUBLICATION_DATE);
	    message.setResourceSelector(selector);

	    message.setSources(List.of(source));
	    message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	    message.setPermittedBond(bond);
	    message.setUserBond(bond);
	    message.setNormalizedBond(bond);
	    message.setSortedFields(SortedFields.of(ResourceProperty.PRIVATE_ID, SortOrder.ASCENDING));
	    message.setPage(new Page(1, PAGE_SIZE));
	    if (searchAfter != null) {
		message.setSearchAfter(searchAfter);
	    }

	    ResultSet<GSResource> resultSet = executor.retrieve(message);

	    for (GSResource resource : resultSet.getResultsList()) {
		List<String> identifiers = resource.getIndexesMetadata().read(MetadataElement.IDENTIFIER);
		List<String> dates = resource.getIndexesMetadata().read(MetadataElement.PUBLICATION_DATE);
		String record = (identifiers.isEmpty() ? "-" : identifiers.getFirst()) + " (" + (dates.isEmpty() ? "-" : dates.getFirst()) + ")";

		for (String onlineId : resource.getIndexesMetadata().read(MetadataElement.ONLINE_ID)) {
		    if (onlineIds.contains(onlineId)) {
			records.computeIfAbsent(onlineId, k -> new ArrayList<>()).add(record);
		    }
		}
	    }

	    searchAfter = resultSet.getResultsList().isEmpty() ? null : resultSet.getSearchAfter().orElse(null);

	} while (searchAfter != null);
    }
}
