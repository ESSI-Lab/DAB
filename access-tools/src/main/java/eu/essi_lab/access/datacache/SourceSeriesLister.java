package eu.essi_lab.access.datacache;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.index.jaxb.IndexesMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.DiscoveryExecutor;

/**
 * Lists, in pages, the time series (online identifiers) of the not deleted metadata records of a source, reading only the
 * indexes
 *
 * @author boldrini
 */
public class SourceSeriesLister {

    /**
     * The metadata of a time series is considered current if its temporal extent ends now or in this period
     */
    public static final long CURRENT_PERIOD_MS = TimeUnit.DAYS.toMillis(365);

    private static final int PAGE_SIZE = 1000;

    /**
     * The metadata of a time series
     */
    public static class SeriesMetadata {

	private final String onlineId;
	private final List<String> observedPropertyURIs = new ArrayList<>();
	private String observedProperty;
	private String title;
	private String platform;
	private boolean current;

	private SeriesMetadata(String onlineId) {
	    this.onlineId = onlineId;
	}

	public String getOnlineId() {
	    return onlineId;
	}

	public List<String> getObservedPropertyURIs() {
	    return observedPropertyURIs;
	}

	public Optional<String> getObservedPropertyURI() {
	    return observedPropertyURIs.stream().findFirst();
	}

	public String getObservedProperty() {
	    return observedProperty;
	}

	public String getTitle() {
	    return title;
	}

	public String getPlatform() {
	    return platform;
	}

	/**
	 * @return true if the temporal extent ends now or in the last {@link SourceSeriesLister#CURRENT_PERIOD_MS}
	 */
	public boolean isCurrent() {
	    return current;
	}
    }

    /**
     * Iterates the time series of the source, in pages of at most {@link #PAGE_SIZE} records. A time series whose online
     * identifier is shared by more records can be returned in more pages
     *
     * @param source
     * @param view optional view, whose bond restricts the listed records
     * @param consumer receives the time series of each page, by online identifier
     * @throws Exception
     */
    public void forEachPage(GSSource source, Optional<View> view, DataCacheConnector.PageConsumer<Map<String, SeriesMetadata>> consumer)
	    throws Exception {

	ServiceLoader<DiscoveryExecutor> loader = ServiceLoader.load(DiscoveryExecutor.class);
	DiscoveryExecutor executor = loader.iterator().next();

	Bond bond = BondFactory.createAndBond( //
		BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier()), //
		BondFactory.createIsDeletedBond(false));

	if (view.isPresent() && view.get().getBond() != null) {
	    bond = BondFactory.createAndBond(bond, view.get().getBond());
	}

	long currentLimit = System.currentTimeMillis() - CURRENT_PERIOD_MS;

	SearchAfter searchAfter = null;

	do {
	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setRequestId("source-series-lister-" + source.getUniqueIdentifier() + "-" + UUID.randomUUID());
	    message.setExcludeResourceBinary(true);

	    ResourceSelector selector = new ResourceSelector();
	    selector.addIndex(MetadataElement.ONLINE_ID);
	    selector.addIndex(MetadataElement.TEMP_EXTENT_END);
	    selector.addIndex(MetadataElement.TEMP_EXTENT_END_NOW);
	    selector.addIndex(MetadataElement.OBSERVED_PROPERTY_URI);
	    selector.addIndex(MetadataElement.ATTRIBUTE_TITLE);
	    selector.addIndex(MetadataElement.TITLE);
	    selector.addIndex(MetadataElement.PLATFORM_TITLE);
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

	    Map<String, SeriesMetadata> page = new HashMap<>();

	    for (GSResource resource : resultSet.getResultsList()) {

		IndexesMetadata indexes = resource.getIndexesMetadata();

		boolean current = isCurrent(indexes, currentLimit);
		List<String> uris = indexes.read(MetadataElement.OBSERVED_PROPERTY_URI);

		for (String onlineId : indexes.read(MetadataElement.ONLINE_ID)) {

		    if (onlineId == null || onlineId.isBlank()) {
			continue;
		    }

		    SeriesMetadata series = page.computeIfAbsent(onlineId, SeriesMetadata::new);
		    series.current |= current;
		    for (String uri : uris) {
			if (uri != null && !uri.isBlank() && !series.observedPropertyURIs.contains(uri.trim())) {
			    series.observedPropertyURIs.add(uri.trim());
			}
		    }
		    if (series.observedProperty == null) {
			series.observedProperty = first(indexes, MetadataElement.ATTRIBUTE_TITLE);
		    }
		    if (series.title == null) {
			series.title = first(indexes, MetadataElement.TITLE);
		    }
		    if (series.platform == null) {
			series.platform = first(indexes, MetadataElement.PLATFORM_TITLE);
		    }
		}
	    }

	    if (!page.isEmpty() && !consumer.accept(page)) {
		return;
	    }

	    searchAfter = resultSet.getResultsList().isEmpty() ? null : resultSet.getSearchAfter().orElse(null);

	} while (searchAfter != null);
    }

    private static String first(IndexesMetadata indexes, MetadataElement element) {

	return indexes.read(element).stream().filter(v -> v != null && !v.isBlank()).findFirst().orElse(null);
    }

    /**
     * @return true if the temporal extent ends now or after the given limit
     */
    private static boolean isCurrent(IndexesMetadata indexes, long limit) {

	if (!indexes.read(MetadataElement.TEMP_EXTENT_END_NOW).isEmpty()) {
	    return true;
	}

	for (String end : indexes.read(MetadataElement.TEMP_EXTENT_END)) {
	    Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(end);
	    if (date.isPresent() && date.get().getTime() >= limit) {
		return true;
	    }
	}

	return false;
    }
}
