package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.OrderingDirection;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
public class DiscoveryMessage extends QueryInitializerMessage {
    public static Double TOL = Math.pow(10, -8);

    @Override
    public String getBaseType() {
	return "discovery-message";
    }

    /**
     * 
     */
    private static final long serialVersionUID = -1949423411370425086L;

    private static final String RANKING = "ranking";
    private static final String INLCUDE_DELETED = "includeDeleted";
    private static final String MAX_TERM_FREQUENCY_MAP_ITEMS = "maxTfMapItems";
    private static final String RESOURCE_SELECTOR = "resourceSelector";
    private static final String PARENTS_GSRESOURCE = "PARENTS_GSRESOURCE";
    private static final String QUAKE_ML_EVENT_ORDER = "QUAKE_ML_EVENT_ORDER";
    private static final String DATA_FOLDER_CHECK = "DATA_FOLDER_CHECK";
    private static final String ORDERING_PROPERTY = "ORDERING_PROPERTY";
    private static final String ORDERING_DIRECTION = "ORDERING_DIRECTION";
    private static final String DISINCT_VALUES_ELEMENT = "DISINCT_VALUES_ELEMENT";
    private static final String QUERY_REGISTRATION = "QUERY_REGISTRATION";
    private static final String RESULTS_PRIORITY = "RESULTS_PRIORITY"; // UNSET, DATASET, COLLECTION,
    private static final String TF_TARGETS = "TF_TARGETS";

    /**
     * 
     */
    public static final int DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS = 50;

    private List<GSResource> parents = new ArrayList<>();

    public DiscoveryMessage() {

	setIncludeDeleted(false);
	setMaxFrequencyMapItems(DEFAULT_MAX_TERM_FREQUENCY_MAP_ITEMS);
	setRankingStrategy(new RankingStrategy());
	setResourceSelector(new ResourceSelector());
	setQueryRegistrationEnabled(false);
	setTermFrequencyTargets(Arrays.asList());

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

	setSharedRepositoryInfo(accessMessage.getSharedRepositoryInfo());
	setSources(accessMessage.getSources());
	setCurrentUser(accessMessage.getCurrentUser().orElse(null));
	accessMessage.getView().ifPresent(view -> setView(view));
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

	setSharedRepositoryInfo(statMessage.getSharedRepositoryInfo());
	setSources(statMessage.getSources());
	setCurrentUser(statMessage.getCurrentUser().orElse(null));
	statMessage.getView().ifPresent(view -> setView(view));
	setWebRequest(statMessage.getWebRequest());
	setDataBaseURI(statMessage.getDataBaseURI());
	setPage(statMessage.getPage());
	setPermittedBond(statMessage.getPermittedBond());
	statMessage.getUserBond().ifPresent(b -> setUserBond(b));
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

	StorageUri dataBaseURI = getDataBaseURI();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_STORAGE_URI.getName(), Arrays.asList(dataBaseURI.getUri()));

	Optional<Queryable> element = getDistinctValuesElement();
	element.ifPresent(e -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_DISTINCT_QUERY_ELEMENT.getName(), Arrays.asList(e.getName())));

	int maxFrequencyMapItems = getMaxFrequencyMapItems();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_MAX_FREQUENCY_MAP_ITEMS.getName(),
		Arrays.asList(String.valueOf(maxFrequencyMapItems)));

	Optional<OrderingDirection> orderingDirection = getOrderingDirection();
	orderingDirection
		.ifPresent(d -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_ORDERING_DIRECTION.getName(), Arrays.asList(d.getName())));

	Optional<Queryable> orderingProperty = getOrderingProperty();
	orderingProperty
		.ifPresent(p -> map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_ORDERING_PROPERTY.getName(), Arrays.asList(p.getName())));

	int size = getPage().getSize();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_SIZE.getName(), Arrays.asList(String.valueOf(size)));

	int start = getPage().getStart();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_PAGE_START.getName(), Arrays.asList(String.valueOf(start)));

	Boolean scheduled = getScheduled();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SCHEDULED.getName(),
		scheduled == null ? Arrays.asList("false") : Arrays.asList(String.valueOf(scheduled)));

	List<GSSource> sources = getSources();
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_ID.getName(),
		sources.stream().map(s -> s.getUniqueIdentifier()).collect(Collectors.toList()));
	map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_SOURCE_LABEL.getName(),
		sources.stream().map(s -> s.getLabel()).collect(Collectors.toList()));

	Optional<GSUser> user = getCurrentUser();
	if (user.isPresent()) {

	    if (Objects.nonNull(user.get().getAuthProvider())) {
		map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_GS_USER_AUTH_PROVIDER.getName(), Arrays.asList(user.get().getAuthProvider()));
	    }

	    if (Objects.nonNull(user.get().getIdentifier())) {
		map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_GS_USER_EMAIL.getName(), Arrays.asList(user.get().getIdentifier()));
	    }
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

		if (bond instanceof QueryableBond) {

		    QueryableBond<?> b = (QueryableBond<?>) bond;

		    if (b.getProperty() == MetadataElement.BOUNDING_BOX) {

			SpatialExtent extent = (SpatialExtent) b.getPropertyValue();

			double e = extent.getEast();
			double w = extent.getWest();
			double n = extent.getNorth();
			double s = extent.getSouth();

			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_EAST.getName(), Arrays.asList(String.valueOf(extent.getEast())));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_WEST.getName(), Arrays.asList(String.valueOf(extent.getWest())));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SOUTH.getName(),
				Arrays.asList(String.valueOf(extent.getSouth())));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NORTH.getName(),
				Arrays.asList(String.valueOf(extent.getNorth())));

			String se = extent.getSouth() + " " + extent.getEast();
			String sw = extent.getSouth() + " " + extent.getWest();
			String ne = extent.getNorth() + " " + extent.getEast();
			String nw = extent.getNorth() + " " + extent.getWest();

			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SW.getName(), Arrays.asList(sw));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_SE.getName(), Arrays.asList(se));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NE.getName(), Arrays.asList(ne));
			map.put(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX_NW.getName(), Arrays.asList(nw));

			String shape = getShape("" + s, "" + e, "" + w, "" + n);
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
		}

		else if (bond instanceof QueryableBond<?>) {

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
     * @param enabled
     */
    public void setQueryRegistrationEnabled(boolean enabled) {

	getHeader().add(new GSProperty<Boolean>(QUERY_REGISTRATION, enabled));
    }

    /**
     * @return
     */
    public boolean isQueryRegistrationEnabled() {

	return getHeader().get(QUERY_REGISTRATION, Boolean.class);
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

	getHeader().add(new GSProperty<Integer>(MAX_TERM_FREQUENCY_MAP_ITEMS, max));
    }

    public Optional<String> getQuakeMLEventOrder() {

	return Optional.ofNullable(getHeader().get(QUAKE_ML_EVENT_ORDER, String.class));
    }

    public void setQuakeMLEventOrder(String order) {

	getHeader().add(new GSProperty<String>(QUAKE_ML_EVENT_ORDER, order));
    }

    /**
     * Default value: false
     *
     * @return
     */
    public boolean isDeletedIncluded() {

	return getHeader().get(INLCUDE_DELETED, Boolean.class);
    }

    public void setIncludeDeleted(boolean include) {

	getHeader().add(new GSProperty<Boolean>(INLCUDE_DELETED, include));
    }

    public void setRankingStrategy(RankingStrategy strategy) {

	getHeader().add(new GSProperty<RankingStrategy>(RANKING, strategy));
    }

    public static String getShape(String sString, String eString, String wString, String nString) {
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
			String shape = "POINT (" + ws + ")";
			return shape;
		    } else if (!snEqual && !weEqual) {
			String shape = "POLYGON ((" + ws + "," + wn + "," + en + "," + es + "," + ws + "))";
			return shape;
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

    /**
     * Default value: a non null {@link RankingStrategy}
     *
     * @param strategy
     */
    public RankingStrategy getRankingStrategy() {

	return getHeader().get(RANKING, RankingStrategy.class);
    }

    public void setResourceSelector(ResourceSelector selector) {

	getHeader().add(new GSProperty<ResourceSelector>(RESOURCE_SELECTOR, selector));
    }

    /**
     * @return
     */
    public Optional<OrderingDirection> getOrderingDirection() {

	return Optional.ofNullable(getHeader().get(ORDERING_DIRECTION, OrderingDirection.class));
    }

    /**
     * @param direction
     */
    public void setOrderingDirection(OrderingDirection direction) {

	getHeader().add(new GSProperty<OrderingDirection>(ORDERING_DIRECTION, direction));
    }

    /**
     * Used to indicate the type of results expected (e.g. datasets only, collection only or datasets and collection).
     * This is useful for the case of mixed sources, to indicate the desired results
     * 
     * @return
     */
    public Optional<ResultsPriority> getResultsPriority() {

	return Optional.ofNullable(getHeader().get(RESULTS_PRIORITY, ResultsPriority.class));
    }

    /**
     * Used to indicate the type of results expected (e.g. datasets only, collection only or datasets and collection).
     * This is useful for the case of mixed sources, to indicate the desired results
     * 
     * @param results priority
     */
    public void setResultsPriority(ResultsPriority priority) {

	getHeader().add(new GSProperty<ResultsPriority>(RESULTS_PRIORITY, priority));
    }

    /**
     * @param element
     */
    public void setDistinctValuesElement(Queryable element) {

	getHeader().add(new GSProperty<Queryable>(DISINCT_VALUES_ELEMENT, element));
    }

    /**
     * @return
     */
    public Optional<Queryable> getDistinctValuesElement() {

	return Optional.ofNullable(getHeader().get(DISINCT_VALUES_ELEMENT, Queryable.class));
    }

    /**
     * @param element
     */
    public void setOrderingProperty(Queryable element) {

	getHeader().add(new GSProperty<Queryable>(ORDERING_PROPERTY, element));
    }

    /**
     * @return
     */
    public Optional<Queryable> getOrderingProperty() {

	return Optional.ofNullable(getHeader().get(ORDERING_PROPERTY, Queryable.class));
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
    public boolean isDataFolderCheckEnabled() {

	Boolean enabled = getHeader().get(DATA_FOLDER_CHECK, Boolean.class);
	if (enabled == null) {
	    return true;
	}

	return false;
    }

    /**
     * 
     */
    public void disableDataFolderCheck() {

	getHeader().add(new GSProperty<Boolean>(DATA_FOLDER_CHECK, true));
    }

}
