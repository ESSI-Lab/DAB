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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * Finds harvested or mixed sources in a {@link View} whose harvesting {@link Scheduling} is disabled, assigns each a
 * staggered start time from a base datetime, enables periodic harvesting at a fixed day interval, then persists the
 * configuration and registers jobs with the scheduler.
 * <p>
 * Task options: one {@code key: value} per line (keys are case-insensitive). Empty lines and lines starting with
 * {@code #} are ignored.
 * <ul>
 * <li>{@code view:} &mdash; view identifier (as stored in the database)</li>
 * <li>{@code start:} &mdash; first source start time, {@code yyyy-MM-dd'T'HH:mm:ss} in the scheduler user timezone</li>
 * <li>{@code staggerMinutes:} (alias {@code stagger:}) &mdash; minutes between each source's first run</li>
 * <li>{@code repeatDays:} (alias {@code repeat:}) &mdash; days between periodic harvest runs</li>
 * <li>{@code maximumResources:} (optional, alias {@code maxResources:}) &mdash; if set, only sources whose stored
 * {@link HarvestingProperties#getResourcesCount() resources count} is strictly less than this value are scheduled (unknown
 * or negative counts are skipped)</li>
 * </ul>
 * Example:
 *
 * <pre>
 * view: whos-view
 * start: 2026-04-16T02:00:00
 * staggerMinutes: 30
 * repeatDays: 14
 * maximumResources: 5000
 * </pre>
 *
 * <p>
 * Legacy positional format (four lines, no keys) is still accepted for compatibility: view id, start datetime, stagger
 * minutes, repeat days.
 *
 * @author boldrini
 */
public class ViewHarvestSchedulerTask extends AbstractCustomTask {

    private static final DateTimeFormatter START_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Recognized option keys (prefix before the first colon on each line).
     */
    private enum OptionKey {
	VIEW("view:"), //
	START("start:"), //
	STAGGER("staggerMinutes:", "stagger:"), //
	REPEAT("repeatDays:", "repeat:"), //
	MAX_RESOURCES("maximumResources:", "maxResources:"); //

	private static final EnumSet<OptionKey> REQUIRED_KEYS = EnumSet.of(VIEW, START, STAGGER, REPEAT);

	private final String[] prefixes;

	OptionKey(String... prefixes) {
	    this.prefixes = prefixes;
	}

	/**
	 * @return the key and value after the matched prefix, or {@code null} if the line does not match this key
	 */
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
	    return "view:  start:  staggerMinutes: (or stagger:)  repeatDays: (or repeat:)  [optional] maximumResources: (or maxResources:)";
	}
    }

    private static final class ParsedOptions {
	final String viewId;
	final String startDateTimeStr;
	final int staggerMinutes;
	final int repeatDays;
	/** If present, only sources with {@code resourcesCount < value} are scheduled. */
	final Optional<Integer> maximumResources;

	ParsedOptions(String viewId, String startDateTimeStr, int staggerMinutes, int repeatDays, Optional<Integer> maximumResources) {
	    this.viewId = viewId;
	    this.startDateTimeStr = startDateTimeStr;
	    this.staggerMinutes = staggerMinutes;
	    this.repeatDays = repeatDays;
	    this.maximumResources = maximumResources;
	}
    }

    /**
     * Parses {@code key: value} lines (preferred) or four legacy positional lines.
     */
    private Optional<ParsedOptions> parseTaskOptions(Optional<String> taskOptions) {

	if (taskOptions.isEmpty() || taskOptions.get() == null || taskOptions.get().isBlank()) {
	    GSLoggerFactory.getLogger(getClass()).error("Missing task options. Expected: {}", OptionKey.getExpectedKeysHelp());
	    return Optional.empty();
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
	    GSLoggerFactory.getLogger(getClass()).error("No task options after removing blanks and comments. Expected: {}",
		    OptionKey.getExpectedKeysHelp());
	    return Optional.empty();
	}

	boolean anyKeyed = lines.stream().anyMatch(l -> OptionKey.decodeLine(l) != null);
	if (anyKeyed) {
	    return parseKeyedLines(lines);
	}

	if (lines.size() == 4 && lines.stream().noneMatch(l -> l.contains(":"))) {
	    GSLoggerFactory.getLogger(getClass()).warn(
		    "Using legacy positional task options (four lines without keys); prefer key:value lines such as view: and start:");
	    try {
		return Optional.of(new ParsedOptions(lines.get(0), lines.get(1), Integer.parseInt(lines.get(2)), Integer.parseInt(lines.get(3)),
			Optional.empty()));
	    } catch (NumberFormatException e) {
		GSLoggerFactory.getLogger(getClass()).error("Invalid number in legacy positional options (lines 3–4 must be integers)", e);
		return Optional.empty();
	    }
	}

	GSLoggerFactory.getLogger(getClass()).error(
		"Could not parse task options. Use key:value lines (see {}), or four positional lines without ':' characters.",
		OptionKey.getExpectedKeysHelp());
	return Optional.empty();
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
	    if (decoded.getValue().isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Empty value for option {}", decoded.getKey());
		return Optional.empty();
	    }
	    map.put(decoded.getKey(), decoded.getValue());
	}

	for (OptionKey required : OptionKey.REQUIRED_KEYS) {
	    if (!map.containsKey(required)) {
		GSLoggerFactory.getLogger(getClass()).error("Missing required option {}. Expected: {}", required, OptionKey.getExpectedKeysHelp());
		return Optional.empty();
	    }
	}

	try {
	    int stagger = Integer.parseInt(map.get(OptionKey.STAGGER).trim());
	    int repeat = Integer.parseInt(map.get(OptionKey.REPEAT).trim());
	    Optional<Integer> maxRes = Optional.empty();
	    if (map.containsKey(OptionKey.MAX_RESOURCES)) {
		int cap = Integer.parseInt(map.get(OptionKey.MAX_RESOURCES).trim());
		if (cap < 1) {
		    GSLoggerFactory.getLogger(getClass()).error("maximumResources must be at least 1 when set");
		    return Optional.empty();
		}
		maxRes = Optional.of(cap);
	    }
	    return Optional.of(new ParsedOptions(map.get(OptionKey.VIEW).trim(), map.get(OptionKey.START).trim(), stagger, repeat, maxRes));
	} catch (NumberFormatException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Numeric options must be integers (staggerMinutes, repeatDays, maximumResources)", e);
	    return Optional.empty();
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	Optional<String> taskOptions = readTaskOptions(context);

	Optional<ParsedOptions> parsed = parseTaskOptions(taskOptions);
	if (parsed.isEmpty()) {
	    return;
	}

	String viewId = parsed.get().viewId;
	String startDateTimeStr = parsed.get().startDateTimeStr;
	int staggerMinutes = parsed.get().staggerMinutes;
	int repeatDays = parsed.get().repeatDays;
	Optional<Integer> maximumResources = parsed.get().maximumResources;

	if (repeatDays < 1) {
	    GSLoggerFactory.getLogger(getClass()).error("Repeat period in days must be at least 1");
	    return;
	}

	if (staggerMinutes < 0) {
	    GSLoggerFactory.getLogger(getClass()).error("Stagger interval in minutes must be non-negative");
	    return;
	}

	Optional<View> optView = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	if (optView.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("View '{}' not found", viewId);
	    return;
	}

	View view = optView.get();
	List<GSSource> viewSources = ConfigurationWrapper.getViewSources(view);

	Set<String> harvestableIds = ConfigurationWrapper.getHarvestedAndMixedSources().stream()
		.map(GSSource::getUniqueIdentifier)
		.collect(Collectors.toSet());

	List<GSSource> harvestedInView = viewSources.stream()
		.filter(s -> harvestableIds.contains(s.getUniqueIdentifier()))
		.collect(Collectors.toList());

	SourceStorage sourceStorage = null;
	if (maximumResources.isPresent()) {
	    sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getStorageInfo());
	}

	List<HarvestingSetting> unscheduled = new ArrayList<>();
	for (GSSource source : harvestedInView) {
	    Optional<HarvestingSetting> optSetting = ConfigurationWrapper.getHarvestingSettings(source.getUniqueIdentifier());
	    if (optSetting.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("No harvesting setting for source {}, skipping", source.getUniqueIdentifier());
		continue;
	    }
	    HarvestingSetting hs = optSetting.get();
	    if (!hs.getScheduling().isEnabled()) {
		if (maximumResources.isPresent()) {
		    int cap = maximumResources.get();
		    HarvestingProperties hp = sourceStorage.retrieveHarvestingProperties(source);
		    int count = hp.getResourcesCount();
		    if (count <= 0) {
			GSLoggerFactory.getLogger(getClass()).info(
				"Skipping unscheduled source in view '{}': {} ({}) — resources count unknown (not harvested yet); maximumResources:{} requires a known count",
				viewId,
				source.getLabel(),
				source.getUniqueIdentifier(),
				cap);
			continue;
		    }
		    if (count >= cap) {
			GSLoggerFactory.getLogger(getClass()).info(
				"Skipping unscheduled source in view '{}': {} ({}) — resourcesCount {} is not less than maximumResources {}",
				viewId,
				source.getLabel(),
				source.getUniqueIdentifier(),
				count,
				cap);
			continue;
		    }
		}
		unscheduled.add(hs);
		GSLoggerFactory.getLogger(getClass()).info("Unscheduled harvested source in view '{}': {} ({}){}",
			viewId,
			source.getLabel(),
			source.getUniqueIdentifier(),
			maximumResources.map(m -> " [resourcesCount < " + m + "]").orElse(""));
	    }
	}

	if (unscheduled.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("No unscheduled harvested sources in view '{}'{}", viewId,
		    maximumResources.map(m -> " eligible under maximumResources " + m).orElse(""));
	    return;
	}

	SchedulerViewSetting schedulerViewSetting = ConfigurationWrapper.getSchedulerSetting();
	ZoneId zoneId = ZoneId.of(schedulerViewSetting.getUserDateTimeZone().getID());

	LocalDateTime baseLocal = LocalDateTime.parse(startDateTimeStr, START_FORMAT);
	ZonedDateTime baseZoned = baseLocal.atZone(zoneId);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();
	ConfigurationSource configurationSource = configuration.getSource();
	configurationSource.backup();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerViewSetting);

	List<HarvestingSetting> forQuartz = new ArrayList<>();

	for (int i = 0; i < unscheduled.size(); i++) {

	    HarvestingSetting template = unscheduled.get(i);
	    String settingId = template.getIdentifier();

	    HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().stream()
		    .filter(s -> s.getIdentifier().equals(settingId))
		    .findFirst()
		    .get();

	    setting = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), HarvestingSettingLoader.load().getClass());

	    Scheduling scheduling = setting.getScheduling();
	    scheduling.setEnabled(true);
	    scheduling.setRunIndefinitely();
	    scheduling.setRepeatInterval(repeatDays, TimeUnit.DAYS);

	    SelectionUtils.deepClean(setting);

	    ZonedDateTime start = baseZoned.plusMinutes((long) i * staggerMinutes);
	    ZonedDateTime inUserZone = start.withZoneSameInstant(zoneId);
	    String formattedStart = inUserZone.format(START_FORMAT);
	    scheduling.setStartTime(formattedStart);

	    SelectionUtils.deepAfterClean(setting);

	    boolean replaced = configuration.replace(setting);
	    if (!replaced) {
		GSLoggerFactory.getLogger(getClass()).error("Unable to replace harvesting setting for source {}",
			setting.getSelectedAccessorSetting().getSource().getUniqueIdentifier());
	    } else {
		forQuartz.add(setting);
		GSLoggerFactory.getLogger(getClass()).info("Scheduled source {} first run at {} (repeat every {} days)",
			setting.getSelectedAccessorSetting().getSource().getUniqueIdentifier(),
			formattedStart,
			repeatDays);
	    }
	}

	configuration.flush();

	for (HarvestingSetting setting : forQuartz) {
	    String settingId = setting.getIdentifier();
	    boolean already = scheduler.listScheduledSettings().stream().map(Setting::getIdentifier).anyMatch(settingId::equals);
	    if (already) {
		scheduler.reschedule(setting);
	    } else {
		scheduler.schedule(setting);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("View harvest scheduler finished for view '{}': {} sources registered with the scheduler",
		viewId,
		forQuartz.size());
    }

    @Override
    public String getName() {
	return "View harvest scheduler task";
    }
}
