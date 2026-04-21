package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * Tests source connectivity by executing a listRecords request using each
 * source harvested accessor.
 * <p>
 * Task options:
 * <ul>
 * <li><b>Legacy</b> — a single value (optionally multiple lines of plain text): the view identifier. When blank or
 * omitted, all configured sources are considered.</li>
 * <li><b>Keyed (KVP)</b> — one {@code key: value} per line (keys are case-insensitive). Empty lines and lines starting
 * with {@code #} are ignored. Recognized keys:
 * <ul>
 * <li>{@code view:} — optional view identifier; when omitted, all sources are considered (same as legacy empty).</li>
 * <li>{@code source:} (alias {@code sourceId:}) — optional source unique identifier; when set, only that source is
 * tested (it must appear in the resolved source list from the view or from all sources).</li>
 * </ul>
 * <p>
 * Example:
 *
 * <pre>
 * view: whos-view
 * source: my-source-uid
 * </pre>
 *
 * If any line uses a recognized key prefix, every non-comment line must be a recognized key; otherwise the task is
 * canceled. A line is treated as legacy only when no line starts with {@code view:} or {@code source:} (or their
 * aliases).
 * </li>
 * </ul>
 */
public class SourceConnectivityTestTask extends AbstractCustomTask {

    private enum OptionKey {
	VIEW("view:"), //
	SOURCE("source:", "sourceId:"); //

	private final String[] prefixes;

	OptionKey(String... prefixes) {
	    this.prefixes = prefixes;
	}

	static SimpleEntry<OptionKey, String> decodeLine(String line) {
	    String t = line.trim();
	    for (OptionKey key : values()) {
		for (String prefix : key.prefixes) {
		    if (t.length() >= prefix.length() && t.regionMatches(true, 0, prefix, 0, prefix.length())) {
			return new SimpleEntry<>(key, t.substring(prefix.length()).trim());
		    }
		}
	    }
	    return null;
	}

	static String getExpectedKeysHelp() {
	    return "view:  source: (or sourceId:)";
	}
    }

    private static final class ParsedOptions {
	final String viewId;
	final String sourceId;

	ParsedOptions(String viewId, String sourceId) {
	    this.viewId = viewId;
	    this.sourceId = sourceId;
	}
    }

    /**
     * @return empty if keyed options are invalid; otherwise the resolved view and source filters
     */
    private Optional<ParsedOptions> parseTaskOptions(Optional<String> taskOptions) {

	if (taskOptions.isEmpty() || taskOptions.get() == null || taskOptions.get().isBlank()) {
	    return Optional.of(new ParsedOptions(null, null));
	}

	String raw = taskOptions.get().replace("\r\n", "\n");
	List<String> lines = new ArrayList<>();
	for (String line : raw.split("\n", -1)) {
	    String t = line.trim();
	    if (t.isEmpty() || t.startsWith("#")) {
		continue;
	    }
	    lines.add(t);
	}

	if (lines.isEmpty()) {
	    return Optional.of(new ParsedOptions(null, null));
	}

	boolean anyKeyed = lines.stream().anyMatch(l -> OptionKey.decodeLine(l) != null);
	if (anyKeyed) {
	    return parseKeyedLines(lines);
	}

	String legacyView = raw.trim();
	return Optional.of(new ParsedOptions(legacyView.isEmpty() ? null : legacyView, null));
    }

    private Optional<ParsedOptions> parseKeyedLines(List<String> lines) {

	Map<OptionKey, String> map = new EnumMap<>(OptionKey.class);
	for (String line : lines) {
	    SimpleEntry<OptionKey, String> decoded = OptionKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass()).error("Unrecognized task option line: [{}]. Expected: {}", line,
			OptionKey.getExpectedKeysHelp());
		return Optional.empty();
	    }
	    if (map.containsKey(decoded.getKey())) {
		GSLoggerFactory.getLogger(getClass()).error("Duplicate option: {}", decoded.getKey());
		return Optional.empty();
	    }
	    String v = decoded.getValue();
	    if (!v.isEmpty()) {
		map.put(decoded.getKey(), v);
	    }
	}

	String viewId = map.get(OptionKey.VIEW);
	String sourceId = map.get(OptionKey.SOURCE);

	return Optional.of(new ParsedOptions(viewId, sourceId));
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Source connectivity test task STARTED");

	Optional<ParsedOptions> parsedOpts = parseTaskOptions(readTaskOptions(context));
	if (parsedOpts.isEmpty()) {
	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	String viewId = parsedOpts.get().viewId;
	String sourceId = parsedOpts.get().sourceId;

	List<GSSource> viewSources = new ArrayList<>();

	if (viewId == null) {
	    GSLoggerFactory.getLogger(getClass()).info("View not set, checking all sources");
	    viewSources = ConfigurationWrapper.getAllSources();
	} else {
	    Optional<View> optView = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	    if (optView.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("View '{}' not found, exiting", viewId);
		status.setPhase(JobPhase.CANCELED);
		return;
	    } else {
		viewSources = ConfigurationWrapper.getViewSources(optView.get());
	    }
	}

	if (sourceId != null) {
	    List<GSSource> only = new ArrayList<>();
	    for (GSSource source : viewSources) {
		if (sourceId.equals(source.getUniqueIdentifier())) {
		    only.add(source);
		}
	    }
	    if (only.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("Source '{}' not found in the resolved source list (view '{}'), exiting", sourceId,
			viewId);
		status.setPhase(JobPhase.CANCELED);
		return;
	    }
	    viewSources = only;
	}

	SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getStorageInfo());

	GSLoggerFactory.getLogger(getClass()).info("Testing connectivity of {} sources for view {} (source filter: {})", viewSources.size(),
		viewId, sourceId != null ? sourceId : "none");



	int s = 0;

	for (GSSource source : viewSources) {
		GSLoggerFactory.getLogger(getClass()).info("Testing connectivity for source '{}', {}", source.getLabel(),++s+"/"+viewSources.size());

	    HarvestingProperties properties = sourceStorage.retrieveHarvestingProperties(source);

	    int harvestingCount = properties.getHarvestingCount();

	    if(harvestingCount == 0){

		GSLoggerFactory.getLogger(getClass()).debug("Skipping test of not harvested source");

		continue;
	    }

	    boolean sourceUp = false;
	    long testStartMs = System.currentTimeMillis();

	    try {
		Optional<HarvestingSetting> optHarvestingSetting = ConfigurationWrapper.getHarvestingSettings(source.getUniqueIdentifier());
		if (optHarvestingSetting.isPresent()) {
		    AccessorSetting accessorSetting = optHarvestingSetting.get().getSelectedAccessorSetting();
		    AccessorSetting clonedSetting = SettingUtils.downCast(accessorSetting, AccessorSetting.class, true);
		    if (clonedSetting != null) {
			HarvestedConnectorSetting connectorSetting = clonedSetting.getHarvestedConnectorSetting();
			if (connectorSetting != null) {
			    connectorSetting.setMaxRecords(1);
			    connectorSetting.setPageSize(1);
			    accessorSetting = clonedSetting;
			}
		    }
		    @SuppressWarnings("rawtypes")
		    IHarvestedAccessor accessor = AccessorFactory.getConfiguredHarvestedAccessor(accessorSetting);

		    ListRecordsRequest request = new ListRecordsRequest();
		    request.setHarvestingProperties(sourceStorage.retrieveHarvestingProperties(source));


		    ListRecordsResponse<?> response = accessor.getConnector().listRecords(request);
		    if (response==null){
			GSLoggerFactory.getLogger(getClass()).info("Null response");
		    }else{
			if (response.getRecordsAsList().size()==0){
			    GSLoggerFactory.getLogger(getClass()).info("Zero resources returned");
			}
		    }
		    sourceUp = response != null && !response.getRecordsAsList().isEmpty();
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn(
			    "No harvesting setting found for source '{}' ({}), storing sourceUp=0",
			    source.getLabel(),
			    source.getUniqueIdentifier());
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(
			"Connectivity test failed for source '{}' ({})",
			source.getLabel(),
			source.getUniqueIdentifier(),
			e);
	    }

	    long connectivityTestDurationMs = System.currentTimeMillis() - testStartMs;
	    long connectivityTestUnixTimestampMs = System.currentTimeMillis();

	    properties.setSourceUp(sourceUp);
	    properties.setConnectivityTestDurationMs(connectivityTestDurationMs);
	    properties.setConnectivityTestUnixTimestampMs(connectivityTestUnixTimestampMs);
	    if (sourceUp) {
		properties.setLastSourceUpUnixTimestampMs(System.currentTimeMillis());
	    }
	    sourceStorage.storeHarvestingProperties(source, properties);

	    GSLoggerFactory.getLogger(getClass()).info("Source '{}' ({}) connectivity status: {}",
		    source.getLabel(),
		    source.getUniqueIdentifier(),
		    sourceUp ? 1 : 0);

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Source connectivity test task CANCELED view id {} source filter {}", viewId,
			sourceId);
		status.setPhase(JobPhase.CANCELED);
		return;
	    }
	}

	log(status, "Source connectivity test task ENDED");
    }

    @Override
    public String getName() {
	return "Source connectivity test task";
    }
}
