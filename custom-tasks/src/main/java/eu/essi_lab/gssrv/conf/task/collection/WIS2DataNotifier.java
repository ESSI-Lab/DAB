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

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.LatitudeLongitude;
import eu.essi_lab.access.datacache.WIS2DataItem;
import eu.essi_lab.cfga.gs.setting.WIS2Setting.DataNotificationsMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.whos.WIS2DataPolicy;
import eu.essi_lab.lib.whos.WIS2HydrologyTopic;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * Publishes a WIS2 data notification message for each new value of the series of a source. Values are notified only
 * if the source is a core source (see {@link WISUtils#isCoreDataPublished(String)}), according to the data
 * notifications mode of the WIS2 settings:
 * <ul>
 * <li>dry run: the messages are logged, not published</li>
 * <li>private: the messages are published under the private topic prefix, not relayed by the WIS2 Global Brokers</li>
 * <li>experimental and operational: the messages are published only under the data topics advertised by the published
 * WCMP2 record of the source (see {@link CollectionCreatorTask}), so that a topic is never used before a record
 * advertises it</li>
 * </ul>
 * Each message provides the value inline, and links the same content served by the WIS profiler (the values are read
 * back from the data cache, so that the notified content matches the served one)
 *
 * @author boldrini
 */
public class WIS2DataNotifier {

    /**
     * Min time between two readings of the topics advertised by the published record of the source
     */
    private static final long TOPICS_REFRESH_MS = TimeUnit.MINUTES.toMillis(10);

    private final String sourceId;
    private Set<String> topics;
    private boolean enabled;
    private long topicsRead;

    /**
     * @param sourceId
     */
    public WIS2DataNotifier(String sourceId) {

	this.sourceId = sourceId;
	this.topics = new HashSet<>();
    }

    /**
     * Reads the data topics advertised by the published WCMP2 record of the source, if not read recently
     */
    private void refreshTopics() {

	long now = System.currentTimeMillis();
	if (topicsRead != 0 && now - topicsRead < TOPICS_REFRESH_MS) {
	    return;
	}
	topicsRead = now;

	Set<String> ret = new HashSet<>();

	Optional<String> viewId = WISUtils.getViewId();

	if (!WISUtils.isCoreDataPublished(sourceId)) {

	    GSLoggerFactory.getLogger(getClass()).info("[WIS2] Source {} not a core source or data notifications disabled", sourceId);

	} else if (viewId.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).error("[WIS2] View id not configured in the WIS2 settings, no data notifications");

	} else if (WISUtils.getDataNotificationsMode().isAdvertised()) {

	    try {

		ResultSet<GSResource> resultSet = WISUtils.getMetadataItems(null, viewId, Optional.of(sourceId));
		for (GSResource resource : resultSet.getResultsList()) {
		    ret.addAll(resource.getExtensionHandler().getWISDataTopics());
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("[WIS2] Unable to read the published record of source {}: {}", sourceId,
			e.getMessage(), e);
		// the previous topics are kept
		return;
	    }
	}

	if (!ret.equals(topics)) {
	    GSLoggerFactory.getLogger(getClass()).info("[WIS2] Source {} advertised data topics: {}", sourceId, ret);
	}

	topics = ret;
	enabled = WISUtils.isCoreDataPublished(sourceId) && viewId.isPresent();
    }

    /**
     * @param records the new values written to the data cache, already searchable
     * @param dataCacheConnector
     * @return the number of published notifications
     */
    public int notify(Collection<DataRecord> records, DataCacheConnector dataCacheConnector) {

	refreshTopics();

	if (!enabled || records.isEmpty()) {
	    return 0;
	}

	DataNotificationsMode mode = WISUtils.getDataNotificationsMode();

	// the period of the new values of each series
	Map<String, Date[]> periods = new LinkedHashMap<>();
	for (DataRecord record : records) {
	    if (record.getValue() == null || record.getDate() == null) {
		continue;
	    }
	    Date[] period = periods.computeIfAbsent(record.getDataIdentifier(), id -> new Date[] { record.getDate(), record.getDate() });
	    if (record.getDate().before(period[0])) {
		period[0] = record.getDate();
	    }
	    if (record.getDate().after(period[1])) {
		period[1] = record.getDate();
	    }
	}

	String viewId = WISUtils.getViewId().get();
	String metadataId = WISUtils.getMetadataIdentifier(sourceId);
	String centreId = WISUtils.getCentreId();
	String generatedBy = WISUtils.getGeneratedBy();

	int published = 0;

	for (Map.Entry<String, Date[]> entry : periods.entrySet()) {

	    List<DataRecord> cached;
	    try {
		cached = dataCacheConnector.getRecords(entry.getValue()[0], entry.getValue()[1], entry.getKey());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("[WIS2] Unable to read the new values of {}: {}", entry.getKey(), e.getMessage(),
			e);
		continue;
	    }

	    for (DataRecord record : cached) {

		if (record.getValue() == null) {
		    continue;
		}

		Optional<WIS2HydrologyTopic> topic = WIS2HydrologyTopic.decode(record.getObservedPropertyURI());
		if (topic.isEmpty()) {
		    continue;
		}

		Optional<String> channel = WISUtils.getPublicationTopic(topic.get());
		if (channel.isEmpty() || (mode.isAdvertised() && !topics.contains(channel.get()))) {
		    continue;
		}

		// the data id is based on the advertised topic or, if not advertised, on the official one
		String dataTopic = WISUtils.getAdvertisedTopic(topic.get()).orElse(topic.get().getTopic(centreId, WIS2DataPolicy.CORE));

		WISNotificationMessage message = createMessage(record, dataTopic, viewId, metadataId, generatedBy);

		if (mode == DataNotificationsMode.DRY_RUN) {

		    GSLoggerFactory.getLogger(getClass()).info("[WIS2] Dry run, not published to {}: {}", channel.get(),
			    message.getJSONObject().toString());
		    published++;

		} else if (WIS2Publisher.publish(channel.get(), message)) {

		    published++;
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("[WIS2] Source {} {} data notifications ({})", sourceId, published, mode);

	return published;
    }

    /**
     * @param record
     * @param topic the topic on which the data identifier is based (starting with
     *        {@link WIS2HydrologyTopic#ORIGIN_PREFIX})
     * @param viewId
     * @param metadataId
     * @param generatedBy
     * @return
     */
    static WISNotificationMessage createMessage(DataRecord record, String topic, String viewId, String metadataId, String generatedBy) {

	String itemId = WIS2DataItem.getItemId(record);
	String content = WIS2DataItem.encode(record);

	// the data id is the topic, without the channel prefix, followed by the item id
	String dataId = WIS2HydrologyTopic.withoutPrefix(topic) + "/" + itemId;

	Link link = new Link("canonical", "application/json", WISUtils.getDataItemUrl(viewId, itemId));
	link.setLength(content.getBytes(StandardCharsets.UTF_8).length);

	WISNotificationMessage message = new WISNotificationMessage(UUID.randomUUID().toString(), null, dataId, link, generatedBy);
	message.setMetadataId(metadataId);
	message.setDatetime(record.getDate());

	LatitudeLongitude location = record.getLatitudeLongitude();
	if (location != null && location.getLatitude() != null && location.getLongitude() != null) {
	    message.setPoint(location.getLongitude().doubleValue(), location.getLatitude().doubleValue());
	}

	message.setIntegrity(content);
	message.setContent(content);

	return message;
    }
}
