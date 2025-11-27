package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.lib.geo.BBOXUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.messages.bond.spatial.WKT;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.model.*;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.RankingStrategy;
import org.locationtech.jts.io.ParseException;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabrizio
 */
public class DiscoveryMessage extends QueryInitializerMessage {

    /**
     * forceEiffelAPIDiscoveryOption=disabled eiffelUseFilterAPICache=true eiffelUseMergedIdsCache=true eiffelSortAndFilterAPI=FILTER
     * eiffelAPISearchMinScore=0.7 eiffelAPISearchQueryMethod=semantic eiffelAPISearchTermsSignificance=none eiffelAPIFilterTreshold=0.7
     * eiffelAPIMaxSortIdentifiers=10000 eiffelSortAndFilterPartitionSize=10000 eiffelFilterAndSortSplitTreshold=3
     *
     * @author Fabrizio
     */
    public enum EiffelAPIDiscoveryOption {

	/**
	 *
	 */
	SORT_AND_FILTER,
	/**
	 *
	 */
	FILTER_AND_SORT;

	/**
	 *
	 */
	public static final String EIFFEL_S3_VIEW_ID = "eiffels3";

	/**
	 * @param value
	 * @return
	 */
	public static Optional<EiffelAPIDiscoveryOption> fromValue(String value) {

	    if (value.equals(SORT_AND_FILTER.name())) {

		return Optional.of(SORT_AND_FILTER);
	    }

	    if (value.equals(FILTER_AND_SORT.name())) {

		return Optional.of(FILTER_AND_SORT);
	    }

	    return Optional.empty();
	}
    }

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -1949423411370425086L;

    private static final String RANKING = "ranking";
    private static final String INLCUDE_DELETED = "includeDeleted";
    private static final String MAX_TERM_FREQUENCY_MAP_ITEMS = "maxTfMapItems";
    private static final String RESOURCE_SELECTOR = "resourceSelector";
    private static final String PARENTS_GSRESOURCE = "PARENTS_GSRESOURCE";
    private static final String QUAKE_ML_EVENT_ORDER = "QUAKE_ML_EVENT_ORDER";
    private static final String DATA_FOLDER_CHECK = "DATA_FOLDER_CHECK";
    private static final String DISINCT_VALUES_ELEMENT = "DISINCT_VALUES_ELEMENT";
    private static final String QUERY_REGISTRATION = "QUERY_REGISTRATION";
    private static final String RESULTS_PRIORITY = "RESULTS_PRIORITY"; // UNSET, DATASET, COLLECTION,
    private static final String TF_TARGETS = "TF_TARGETS";
    private static final String EIFFEL_DISCOVERY_OPTION = "EIFFEL_DISCOVERY_OPTION";
    private static final String INLCUDE_COUNT_IN_RETRIEVAL = "INLCUDE_COUNT_IN_RETRIEVAL";
    private static final String RSM_THREADS_COUNT = "rsmThreadsCount";
    private static final String DATA_PROXY_SERVER = "dataProxyServer";

    /**
     *
     */
    public static final int DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS = 50;

    /**
     *
     */
    private static final int USER_SELECTION = 10;

    /**
     *
     */
    public DiscoveryMessage() {

	List<GSResource> parents = new ArrayList<>();

	setIncludeDeleted(false);
	setMaxFrequencyMapItems(DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS);
	setRankingStrategy(new RankingStrategy());
	setResourceSelector(new ResourceSelector());
	setQueryRegistrationEnabled(false);
	setTermFrequencyTargets(Arrays.asList());
	setIncludeCountInRetrieval(false);

	getPayload().add(new GSProperty<>(PARENTS_GSRESOURCE, parents));
    }

    /**
     * Creates a new instance of {@link DiscoveryMessage} sharing the following properties with the supplied
     * <code>accessMessage</code>:
     * <ul>
     * <li>{@link AccessMessage#getSharedRepositoryInfo()}</li>
     * <li>{@link AccessMessage#getSources()}</li>
     * <li>{@link AccessMessage#getCurrentUser()}</li>
     * <li>{@link AccessMessage#getViewId()}</li>
     * <li>{@link AccessMessage#getWebRequest()}</li>
     * <li>{@link AccessMessage#getDataBaseURI()}</li>
     * </ul>
     * The following properties are set:
     * <ul>
     * <li>{@link DiscoveryMessage#getPage()} -> 10</li>
     * <li>{@link DiscoveryMessage#isDeletedIncluded()} -> false</li>
     * <li>{@link DiscoveryMessage#getMaxFrequencyMapItems()} -> 50</li>
     * <li>{@link DiscoveryMessage#isOutputSources()} -> false</li>
     * <li>{@link DiscoveryMessage#getRankingStrategy()} -> new RankingStrategy()</li>
     * </ul>
     * A {@link SimpleValueBond} from the {@link MetadataElement#ONLINE_ID} with {@link AccessMessage#getOnlineId()} is
     * created and with all
     * the setXXXBond methods.<br>
     * <br>
     * A new {@link ResourceSelector} is created and set with the following properties:
     * <ul>
     * <li>{@link ResourceSelector#getIndexesPolicy()} -> {@link IndexesPolicy#NONE}</li>
     * <li>{@link ResourceSelector#getSubset()} -> {@link ResourceSubset#CORE_EXTENDED}</li>
     * </ul>
     *
     * @param accessMessage
     */
    public DiscoveryMessage(AccessMessage accessMessage) {

	setSources(accessMessage.getSources());
	setCurrentUser(accessMessage.getCurrentUser().orElse(null));
	accessMessage.getView().ifPresent(this::setView);
	setWebRequest(accessMessage.getWebRequest());
	setDataBaseURI(accessMessage.getDataBaseURI());
	setQueryRegistrationEnabled(false);

	setPage(new Page(10));
	setIncludeDeleted(false);
	setMaxFrequencyMapItems(DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS);
	setRankingStrategy(new RankingStrategy());
	setOutputSources(false);

	String onlineId = accessMessage.getOnlineId();
	SimpleValueBond bond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.ONLINE_ID, //
		onlineId);

	setPermittedBond(bond);
	setUserBond(bond);
	setNormalizedBond(bond);

	setResourceSelector(new ResourceSelector());
	getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	getResourceSelector().setSubset(ResourceSubset.FULL);

	setResultsPriority(ResultsPriority.ALL);

	setTermFrequencyTargets(Arrays.asList());
    }

    /**
     * @param statMessage
     */
    public DiscoveryMessage(StatisticsMessage statMessage) {

	setSources(statMessage.getSources());
	setCurrentUser(statMessage.getCurrentUser().orElse(null));
	statMessage.getView().ifPresent(this::setView);
	setWebRequest(statMessage.getWebRequest());
	setDataBaseURI(statMessage.getDataBaseURI());
	setPage(statMessage.getPage());
	setPermittedBond(statMessage.getPermittedBond());
	statMessage.getUserBond().ifPresent(this::setUserBond);
	setNormalizedBond(statMessage.getNormalizedBond());

	setQueryRegistrationEnabled(false);
	setIncludeDeleted(false);
	setMaxFrequencyMapItems(DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS);
	setRankingStrategy(new RankingStrategy());
	setOutputSources(false);
    }

    @Override
    public HashMap<String, List<String>> provideInfo() {

	final HashMap<String, List<String>> map = super.provideInfo();

	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP.getName(),
		Arrays.asList(ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds()));
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_TIME_STAMP_MILLIS.getName(),
		Arrays.asList(String.valueOf(System.currentTimeMillis())));

	getView().ifPresent(v -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_VIEW_ID.getName(), Arrays.asList(v.getId())));

	StorageInfo dataBaseURI = getDataBaseURI();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_STORAGE_URI.getName(), Arrays.asList(dataBaseURI.getUri()));

	Optional<Queryable> element = getDistinctValuesElement();
	element.ifPresent(e -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_DISTINCT_QUERY_ELEMENT.getName(), Arrays.asList(e.getName())));

	int maxFrequencyMapItems = getMaxFrequencyMapItems();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_MAX_FREQUENCY_MAP_ITEMS.getName(),
		Arrays.asList(String.valueOf(maxFrequencyMapItems)));

	Optional<SortedFields> sortedFields = getSortedFields();
	sortedFields.ifPresent(d -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_ORDERING_DIRECTION.getName(),
		Arrays.asList(d.getFields().getFirst().getValue().getLabel())));
	sortedFields.ifPresent(d -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_ORDERING_PROPERTY.getName(),
		Arrays.asList(d.getFields().getFirst().getKey().getName())));

	int size = getPage().getSize();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_SIZE.getName(), Arrays.asList(String.valueOf(size)));

	int start = getPage().getStart();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_START.getName(), Arrays.asList(String.valueOf(start)));

	Boolean scheduled = getScheduled();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SCHEDULED.getName(),
		scheduled == null ? Arrays.asList("false") : Arrays.asList(String.valueOf(scheduled)));

	List<GSSource> sources = getSources();
	if (sources.size() <= USER_SELECTION) {
	    map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_ID.getName(),
		    sources.stream().map(GSSource::getUniqueIdentifier).collect(Collectors.toList()));
	    map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL.getName(),
		    sources.stream().map(GSSource::getLabel).collect(Collectors.toList()));
	}

	Optional<Bond> userBond = getUserBond();
	userBond.ifPresent(b -> provideUserBondInfo(map));

	List<Queryable> termFrequencyTargets = getTermFrequencyTargets();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_TERM_FREQUENCY_BOND.getName(), Arrays.asList(termFrequencyTargets.toString()));

	return map;
    }

    @Override
    public String getName() {

	return "DISCOVERY_MESSAGE";
    }

    /**
     * @param enabled
     */
    @Deprecated
    public void setQueryRegistrationEnabled(boolean enabled) {

	getHeader().add(new GSProperty<>(QUERY_REGISTRATION, enabled));
    }

    /**
     * @return
     */
    @Deprecated
    public boolean isQueryRegistrationEnabled() {

	return getHeader().get(QUERY_REGISTRATION, Boolean.class);
    }

    /**
     * @return
     */
    public Optional<String> getDataProxyServer() {

	return Optional.ofNullable(getHeader().get(DATA_PROXY_SERVER, String.class));
    }

    /**
     * @param count
     */
    public void setDataProxyServer(String server) {

	getHeader().add(new GSProperty<>(DATA_PROXY_SERVER, server));
    }

    /**
     * @return
     */
    public Optional<Integer> getResultSetMapperThreadsCount() {

	return Optional.ofNullable(getHeader().get(RSM_THREADS_COUNT, Integer.class));
    }

    /**
     * @param count
     */
    public void setResultSetMapperThreadsCount(int count) {

	getHeader().add(new GSProperty<>(RSM_THREADS_COUNT, count));
    }

    /**
     * Default value: 50
     *
     * @return
     */
    public int getMaxFrequencyMapItems() {

	return getHeader().get(MAX_TERM_FREQUENCY_MAP_ITEMS, Integer.class);
    }

    public void setMaxFrequencyMapItems(int max) {

	getHeader().add(new GSProperty<>(MAX_TERM_FREQUENCY_MAP_ITEMS, max));
    }

    public Optional<String> getQuakeMLEventOrder() {

	return Optional.ofNullable(getHeader().get(QUAKE_ML_EVENT_ORDER, String.class));
    }

    public void setQuakeMLEventOrder(String order) {

	getHeader().add(new GSProperty<>(QUAKE_ML_EVENT_ORDER, order));
    }

    /**
     * Default value: false
     *
     * @return
     */
    public boolean isDeletedIncluded() {

	return getHeader().get(INLCUDE_DELETED, Boolean.class);
    }

    /**
     * @param include
     */
    public void setIncludeDeleted(boolean include) {

	getHeader().add(new GSProperty<>(INLCUDE_DELETED, include));
    }

    /**
     * @param strategy
     */
    public void setRankingStrategy(RankingStrategy strategy) {

	getHeader().add(new GSProperty<>(RANKING, strategy));
    }

    /**
     * Default value: a non null {@link RankingStrategy}
     *
     * @param strategy
     */
    public RankingStrategy getRankingStrategy() {

	return getHeader().get(RANKING, RankingStrategy.class);
    }

    public void setResourceSelector(ResourceSelector selector) {

	getHeader().add(new GSProperty<>(RESOURCE_SELECTOR, selector));
    }

    /**
     * Used to indicate the type of results expected (e.g. datasets only, collection only or datasets and collection). This is useful for
     * the case of mixed sources, to indicate the desired results
     *
     * @return
     */
    public Optional<ResultsPriority> getResultsPriority() {

	return Optional.ofNullable(getHeader().get(RESULTS_PRIORITY, ResultsPriority.class));
    }

    /**
     * Used to indicate the type of results expected (e.g. datasets only, collection only or datasets and collection). This is useful for
     * the case of mixed sources, to indicate the desired results
     *
     * @param results priority
     */
    public void setResultsPriority(ResultsPriority priority) {

	getHeader().add(new GSProperty<>(RESULTS_PRIORITY, priority));
    }

    /**
     * @param element
     */
    public void setDistinctValuesElement(Queryable element) {

	getHeader().add(new GSProperty<>(DISINCT_VALUES_ELEMENT, element));
    }

    /**
     * @return
     */
    public Optional<Queryable> getDistinctValuesElement() {

	return Optional.ofNullable(getHeader().get(DISINCT_VALUES_ELEMENT, Queryable.class));
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public void setTermFrequencyTargets(List<Queryable> queryables) {

	getHeader().add(new GSProperty<List>(TF_TARGETS, queryables));
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Queryable> getTermFrequencyTargets() {

	return getHeader().get(TF_TARGETS, List.class);
    }

    /**
     * Default value: false
     *
     * @return
     */
    public boolean isCountInRetrievalIncluded() {

	return getHeader().get(INLCUDE_COUNT_IN_RETRIEVAL, Boolean.class);
    }

    /**
     * @param include
     */
    public void setIncludeCountInRetrieval(boolean include) {

	getHeader().add(new GSProperty<>(INLCUDE_COUNT_IN_RETRIEVAL, include));
    }

    // ----------------------------------------
    //
    // Payload properties
    //
    //

    public ResourceSelector getResourceSelector() {

	return getHeader().get(RESOURCE_SELECTOR, ResourceSelector.class);
    }

    @SuppressWarnings("unchecked")
    public void addParentGSResource(GSResource gsResource) {

	getPayload().get(PARENTS_GSRESOURCE, List.class).add(gsResource);

    }

    @SuppressWarnings("unchecked")
    public Optional<GSResource> getParentGSResource(String id) {

	return getPayload().//
		get(PARENTS_GSRESOURCE, List.class).//
		stream().//
		filter(res -> ((GSResource) res).getPublicId().equals(id)).//
		findFirst();
    }

    /**
     * @return
     */
    @Deprecated
    public boolean isDataFolderCheckEnabled() {

	Boolean enabled = getHeader().get(DATA_FOLDER_CHECK, Boolean.class);
	return enabled == null;
    }

    /**
     *
     */
    @Deprecated
    public void disableDataFolderCheck() {

	getHeader().add(new GSProperty<>(DATA_FOLDER_CHECK, true));
    }

    /**
     * @param option
     */
    public void enableEiffelAPIDiscoveryOption(EiffelAPIDiscoveryOption option) {

	getHeader().add(new GSProperty<>(EIFFEL_DISCOVERY_OPTION, option));
    }

    /**
     * @param
     */
    public Optional<EiffelAPIDiscoveryOption> getEiffelAPIDiscoveryOption() {

	return Optional.ofNullable(getHeader().get(EIFFEL_DISCOVERY_OPTION, EiffelAPIDiscoveryOption.class));
    }

    private void provideUserBondInfo(HashMap<String, List<String>> map) {

	DiscoveryBondHandler handler = new DiscoveryBondHandler() {

	    @Override
	    public void startLogicalBond(LogicalBond bond) {
	    }

	    @Override
	    public void separator() {
	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {

		if (bond instanceof QueryableBond<?> b) {

		    if (b.getProperty() == MetadataElement.BOUNDING_BOX) {

			double east = 0;
			double west = 0;
			double north = 0;
			double south = 0;

			switch (b.getPropertyValue()) {
			case SpatialExtent bbox -> {

			    east = bbox.getEast();
			    west = bbox.getWest();
			    north = bbox.getNorth();
			    south = bbox.getSouth();
			}

			case WKT wkt -> {

			    try {

				Map<String, Double> envelope = BBOXUtils.getEnvelope(wkt.getValue());

				east = envelope.get("east");
				west = envelope.get("west");
				north = envelope.get("north");
				south = envelope.get("south");

			    } catch (ParseException e) {

				GSLoggerFactory.getLogger(getClass()).error(e);
			    }
			}
			default -> GSLoggerFactory.getLogger(getClass()).error("Unsupported spatial entity: " + b.getPropertyValue());
			}

			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_EAST.getName(), Arrays.asList(String.valueOf(east)));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_WEST.getName(), Arrays.asList(String.valueOf(west)));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SOUTH.getName(), Arrays.asList(String.valueOf(south)));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NORTH.getName(), Arrays.asList(String.valueOf(north)));

			String se = south + " " + east;
			String sw = south + " " + west;
			String ne = north + " " + east;
			String nw = north + " " + west;

			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SW.getName(), Arrays.asList(sw));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SE.getName(), Arrays.asList(se));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NE.getName(), Arrays.asList(ne));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NW.getName(), Arrays.asList(nw));

			String shape = getShape("" + south, "" + east, "" + west, "" + north);
			if (shape != null) {
			    map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SHAPE.getName(), Arrays.asList(shape));
			}

		    } else {
			//
			//
			// these properties have at the moment no related statistical element
			//
			//
			String name = b.getProperty().getName();

			List<String> list = null;

			if (map.get(name) == null) {
			    list = new ArrayList<>();
			} else {
			    list = map.get(name);
			}

			list.add(b.getPropertyValue().toString());

			map.put(getName() + RuntimeInfoElement.NAME_SEPARATOR + b.getProperty().getName(), list);
		    }
		} else if (bond instanceof QueryableBond<?>) {

		} else if (bond instanceof ViewBond) {

		}
	    }

	    @Override
	    public void endLogicalBond(LogicalBond bond) {
	    }

	    @Override
	    public void viewBond(ViewBond bond) {
	    }

	    @Override
	    public void spatialBond(SpatialBond bond) {
	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond bond) {
	    }

	    @Override
	    public void resourcePropertyBond(ResourcePropertyBond bond) {
	    }

	    @Override
	    public void customBond(QueryableBond<String> bond) {
	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    }
	};

	DiscoveryBondParser parser = new DiscoveryBondParser(getUserBond().get());
	parser.parse(handler);
    }

    /**
     * @param sString
     * @param eString
     * @param wString
     * @param nString
     * @return
     */
    public static String getShape(String sString, String eString, String wString, String nString) {

	final Double TOL = Math.pow(10, -8);

	if (wString != null && sString != null && nString != null && eString != null) {

	    double e;
	    double w;
	    double s;
	    double n;
	    try {
		e = Double.parseDouble(eString);
		w = Double.parseDouble(wString);
		s = Double.parseDouble(sString);
		n = Double.parseDouble(nString);
		if (!Double.isFinite(e)) {
		    return null;
		}
		if (!Double.isFinite(w)) {
		    return null;
		}
		if (!Double.isFinite(s)) {
		    return null;
		}
		if (!Double.isFinite(n)) {
		    return null;
		}
	    } catch (Exception ex) {
		return null;
	    }

	    String es = e + " " + s;
	    String ws = w + " " + s;
	    String en = e + " " + n;
	    String wn = w + " " + n;

	    if (n <= 90 && s >= -90 && e >= -180 && e <= 180 && w <= 180 && w >= -180) {

		if (n >= s) {

		    boolean snEqual = (Math.abs(n - s) < TOL);
		    boolean weEqual = (Math.abs(w - e) < TOL);

		    if (snEqual && weEqual) {
			return "POINT (" + ws + ")";
		    } else if (!snEqual && !weEqual) {
			return "POLYGON ((" + ws + "," + wn + "," + en + "," + es + "," + ws + "))";
		    } else {
			GSLoggerFactory.getLogger(DiscoveryMessage.class).warn("Not valid bbox {} {} {} {}", w, s, e, n);
		    }
		} else {
		    GSLoggerFactory.getLogger(DiscoveryMessage.class).warn("Not valid bbox {} {} {} {}", w, s, e, n);
		}
	    } else {
		GSLoggerFactory.getLogger(DiscoveryMessage.class).warn("Not valid bbox {} {} {} {}", w, s, e, n);
	    }
	}

	return null;
    }
}
