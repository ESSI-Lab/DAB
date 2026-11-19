package eu.essi_lab.gssrv.conf.task.collection;

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

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.whos.WIS2HydrologyTopic;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Sends the WIS2 deletion notification message of one or more WCMP2 discovery metadata records, given their URLs
 * (e.g. the record in the WIS2 Global Discovery Catalogue:
 * https://wmo-resource-catalogue.wis2dev.io/collections/wis2-discovery-metadata/items/urn:wmo:md:it-cnr-itiam-whos-dab:can-swe:10).
 * <br>
 * For each record the notification has:
 * <ul>
 * <li>data_id: the record identifier</li>
 * <li>a "deletion" link to the canonical link of the record (the URL where it was originally published, the same used
 * by the {@link CollectionCreatorTask} notifications); if the record has no canonical link, the given URL</li>
 * <li>the geometry of the record</li>
 * </ul>
 * It is published on the metadata topic of the record centre (origin/a/wis2/{centre-id}/metadata), unless a topic is
 * given in the options. Only the notification is sent: the record is not removed from the DAB database.<br>
 * Options (new line separated, key=value):
 * <ul>
 * <li>RECORD_URLS: the record URLs, separated by ';' (required)</li>
 * <li>TOPIC: the topic where to publish (optional)</li>
 * <li>DRY_RUN: if true, the messages are logged but not published (optional, default false)</li>
 * </ul>
 *
 * @author boldrini
 */
public class WISRecordDeletionTask extends AbstractEmbeddedTask {

    public enum WISRecordDeletionTaskOptions implements OptionsKey {
	RECORD_URLS, //
	TOPIC, //
	DRY_RUN;
    }

    private static final Pattern CENTRE_ID_PATTERN = Pattern.compile("^urn:wmo:md:([^:]+):.*$");

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "WIS record deletion task STARTED");

	Optional<EnumMap<WISRecordDeletionTaskOptions, String>> options = readTaskOptions(context, WISRecordDeletionTaskOptions.class);

	String urls = options.map(o -> o.get(WISRecordDeletionTaskOptions.RECORD_URLS)).orElse(null);
	if (urls == null || urls.isBlank()) {
	    status.addWarningMessage("Missing RECORD_URLS option, WIS record deletion task skipped");
	    return;
	}

	Optional<String> topic = options.map(o -> o.get(WISRecordDeletionTaskOptions.TOPIC)).filter(t -> !t.isBlank()).map(String::trim);
	boolean dryRun = options.map(o -> Boolean.parseBoolean(o.get(WISRecordDeletionTaskOptions.DRY_RUN))).orElse(false);
	if (dryRun) {
	    log(status, "Dry run: the deletion messages will not be published");
	}

	String generatedBy = WISUtils.getGeneratedBy();

	for (String url : urls.split(";")) {

	    url = url.trim();
	    if (url.isEmpty()) {
		continue;
	    }

	    try {

		Optional<String> response = new Downloader().downloadOptionalString(getJSONURL(url));
		if (response.isEmpty()) {
		    status.addWarningMessage("Unable to download record " + url);
		    continue;
		}

		JSONObject record = new JSONObject(response.get());

		String recordTopic = topic.orElseGet(() -> getTopic(record));
		WISNotificationMessage message = createDeletionMessage(record, url, generatedBy);

		GSLoggerFactory.getLogger(getClass()).info("Deletion message of record {} on topic {}:\n{}", getRecordId(record), recordTopic,
			message.getJSONObject().toString(3));

		if (dryRun) {
		    log(status, "Dry run: deletion message of record " + getRecordId(record) + " not published on topic " + recordTopic);
		} else if (WIS2Publisher.publish(recordTopic, message)) {
		    log(status, "Deletion message of record " + getRecordId(record) + " published on topic " + recordTopic);
		} else {
		    status.addWarningMessage("Unable to publish the deletion message of record " + getRecordId(record));
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to send the deletion message of record {}", url, e);
		status.addWarningMessage("Unable to send the deletion message of record " + url + ": " + e.getMessage());
	    }
	}

	log(status, "WIS record deletion task ENDED");
    }

    /**
     * @param record a WCMP2 record (GeoJSON feature)
     * @param recordURL the URL where the record has been downloaded
     * @param generatedBy
     * @return the deletion notification message of the record
     */
    static WISNotificationMessage createDeletionMessage(JSONObject record, String recordURL, String generatedBy) {

	String recordId = getRecordId(record);

	Link link = new Link("deletion", "application/geo+json", getCanonicalURL(record).orElse(stripQuery(recordURL)));

	WISNotificationMessage message = new WISNotificationMessage(UUID.randomUUID().toString(), null, recordId, link, generatedBy);
	// the deleted resource is a WCMP2 record, so its metadata identifier is the record identifier
	message.setMetadataId(recordId);

	Object geometry = record.opt("geometry");
	message.getJSONObject().put("geometry", geometry == null ? JSONObject.NULL : geometry);

	return message;
    }

    /**
     * @param record
     * @return the record identifier
     */
    static String getRecordId(JSONObject record) {

	String id = record.optString("id", "");
	if (id.isEmpty()) {
	    JSONObject properties = record.optJSONObject("properties");
	    id = properties == null ? "" : properties.optString("id", "");
	}
	if (id.isEmpty()) {
	    throw new IllegalArgumentException("Record identifier not found");
	}
	return id;
    }

    /**
     * @param record
     * @return the metadata topic of the record centre
     */
    static String getTopic(JSONObject record) {

	JSONObject properties = record.optJSONObject("properties");
	String centreId = properties == null ? "" : properties.optString("centre-id", "");

	if (centreId.isEmpty()) {
	    Matcher matcher = CENTRE_ID_PATTERN.matcher(getRecordId(record));
	    if (matcher.matches()) {
		centreId = matcher.group(1);
	    }
	}

	if (centreId.isEmpty()) {
	    centreId = WISUtils.getCentreId();
	}

	return WIS2HydrologyTopic.getMetadataTopic(centreId);
    }

    /**
     * @param record
     * @return the href of the canonical link of the record
     */
    static Optional<String> getCanonicalURL(JSONObject record) {

	JSONArray links = record.optJSONArray("links");
	if (links == null) {
	    return Optional.empty();
	}

	for (int i = 0; i < links.length(); i++) {
	    JSONObject link = links.optJSONObject(i);
	    if (link != null && "canonical".equals(link.optString("rel")) && !link.optString("href").isEmpty()) {
		return Optional.of(link.getString("href"));
	    }
	}

	return Optional.empty();
    }

    /**
     * @param url
     * @return the URL requesting the JSON encoding (OGC API f=json parameter)
     */
    static String getJSONURL(String url) {

	if (url.matches(".*[?&]f=[^&]*.*")) {
	    return url;
	}
	return url + (url.contains("?") ? "&" : "?") + "f=json";
    }

    private static String stripQuery(String url) {

	int index = url.indexOf('?');
	return index < 0 ? url : url.substring(0, index);
    }

    @Override
    public String getName() {

	return "WIS record deletion task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }
}
