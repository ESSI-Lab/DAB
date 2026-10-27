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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Online;
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
 * Lists, in pages, the time series (online identifiers) of the not deleted metadata records of a source, reading the
 * indexes. The online resources of a record without the "download" function (e.g. links to site information pages) are
 * not time series: since the indexes don't report the function, the full records having more online identifiers are read
 * to select the download ones
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
     * Max records read in full by a single query (one private identifier clause each)
     */
    private static final int FULL_RECORDS_PAGE_SIZE = 200;

    private static final String DOWNLOAD_FUNCTION = "download";

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
	private Date begin;
	private Date end;

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

	/**
	 * @return the earliest begin of the temporal extent, if known
	 */
	public Optional<Date> getBegin() {
	    return Optional.ofNullable(begin);
	}

	/**
	 * @return the latest end of the temporal extent (the listing time, if it ends now), if known
	 */
	public Optional<Date> getEnd() {
	    return Optional.ofNullable(end);
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
	    selector.addIndex(ResourceProperty.PRIVATE_ID);
	    selector.addIndex(MetadataElement.ONLINE_ID);
	    selector.addIndex(MetadataElement.TEMP_EXTENT_BEGIN);
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

	    // records with more online identifiers: their download online identifiers are read from the full records
	    List<String> multipleOnlines = new ArrayList<>();
	    for (GSResource resource : resultSet.getResultsList()) {
		IndexesMetadata indexes = resource.getIndexesMetadata();
		Optional<String> privateId = indexes.read(ResourceProperty.PRIVATE_ID);
		if (privateId.isPresent() && indexes.read(MetadataElement.ONLINE_ID).stream().filter(id -> id != null && !id.isBlank())
			.count() > 1) {
		    multipleOnlines.add(privateId.get());
		}
	    }
	    Map<String, List<String>> downloadOnlineIds = getDownloadOnlineIds(executor, source, bond, multipleOnlines);

	    Map<String, SeriesMetadata> page = new HashMap<>();

	    for (GSResource resource : resultSet.getResultsList()) {

		IndexesMetadata indexes = resource.getIndexesMetadata();

		List<String> onlineIds = indexes.read(ResourceProperty.PRIVATE_ID).map(downloadOnlineIds::get)
			.orElse(indexes.read(MetadataElement.ONLINE_ID));

		boolean current = isCurrent(indexes, currentLimit);
		Optional<Date> begin = earliest(indexes.read(MetadataElement.TEMP_EXTENT_BEGIN));
		Optional<Date> end = indexes.read(MetadataElement.TEMP_EXTENT_END_NOW).isEmpty()
			? latest(indexes.read(MetadataElement.TEMP_EXTENT_END))
			: Optional.of(new Date());
		List<String> uris = indexes.read(MetadataElement.OBSERVED_PROPERTY_URI);

		for (String onlineId : onlineIds) {

		    if (onlineId == null || onlineId.isBlank()) {
			continue;
		    }

		    SeriesMetadata series = page.computeIfAbsent(onlineId, SeriesMetadata::new);
		    series.current |= current;
		    if (begin.isPresent() && (series.begin == null || begin.get().before(series.begin))) {
			series.begin = begin.get();
		    }
		    if (end.isPresent() && (series.end == null || end.get().after(series.end))) {
			series.end = end.get();
		    }
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

    /**
     * Reads the given full records and returns, by private identifier, the identifiers of their online resources having
     * the "download" function. Records without download online identifiers are not returned (all their online
     * identifiers are listed)
     *
     * @param executor
     * @param source
     * @param bond the bond of the listing
     * @param privateIds
     * @return
     * @throws Exception
     */
    private Map<String, List<String>> getDownloadOnlineIds(DiscoveryExecutor executor, GSSource source, Bond bond,
	    List<String> privateIds) throws Exception {

	Map<String, List<String>> ret = new HashMap<>();

	for (int i = 0; i < privateIds.size(); i += FULL_RECORDS_PAGE_SIZE) {

	    List<String> chunk = privateIds.subList(i, Math.min(i + FULL_RECORDS_PAGE_SIZE, privateIds.size()));

	    List<Bond> idBonds = chunk.stream().map(id -> (Bond) BondFactory.createPrivateIdentifierBond(id)).toList();
	    Bond chunkBond = BondFactory.createAndBond(bond, idBonds.size() == 1 ? idBonds.get(0) : BondFactory.createOrBond(idBonds));

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setRequestId("source-series-lister-onlines-" + source.getUniqueIdentifier() + "-" + UUID.randomUUID());
	    message.setExcludeResourceBinary(false);
	    message.setSources(List.of(source));
	    message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	    message.setPermittedBond(chunkBond);
	    message.setUserBond(chunkBond);
	    message.setNormalizedBond(chunkBond);
	    message.setPage(new Page(1, chunk.size()));

	    for (GSResource resource : executor.retrieve(message).getResultsList()) {

		Distribution distribution = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution();
		if (distribution == null || resource.getPrivateId() == null) {
		    continue;
		}

		List<String> ids = new ArrayList<>();
		Iterator<Online> onlines = distribution.getDistributionOnlines();
		while (onlines.hasNext()) {
		    Online online = onlines.next();
		    String id = online.getIdentifier();
		    if (id != null && !id.isBlank() && DOWNLOAD_FUNCTION.equalsIgnoreCase(online.getFunctionCode())) {
			ids.add(id);
		    }
		}

		if (!ids.isEmpty()) {
		    ret.put(resource.getPrivateId(), ids);
		}
	    }
	}

	return ret;
    }

    private static String first(IndexesMetadata indexes, MetadataElement element) {

	return indexes.read(element).stream().filter(v -> v != null && !v.isBlank()).findFirst().orElse(null);
    }

    private static Optional<Date> earliest(List<String> dates) {

	return dates.stream().map(ISO8601DateTimeUtils::parseISO8601ToDate).flatMap(Optional::stream).min(Date::compareTo);
    }

    private static Optional<Date> latest(List<String> dates) {

	return dates.stream().map(ISO8601DateTimeUtils::parseISO8601ToDate).flatMap(Optional::stream).max(Date::compareTo);
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
