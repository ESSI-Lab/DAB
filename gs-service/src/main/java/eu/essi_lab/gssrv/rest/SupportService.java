package eu.essi_lab.gssrv.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;

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

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.SourceCacheStats;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.authorization.userfinder.UserFinder;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.gssrv.portal.PortalTranslator;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.OutputFormat;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Profile;
import eu.essi_lab.lib.odip.ODIPVocabularyHandler.Target;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.semantic.Stats;
import eu.essi_lab.request.executor.IAccessExecutor;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.IStatisticsExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.owasp.encoder.*;

@WebService
@Path("/")
/**
 * @author Fabrizio
 */
public class SupportService {

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/odip")
    public Response odip(//
	    @QueryParam("target") String target, //
	    @QueryParam("term") String term, //
	    @QueryParam("profile") String profile, //
	    @QueryParam("label") String label, //
	    @QueryParam("suggestion") String suggestion, //
	    @QueryParam("callback") String callback) { //

	ODIPVocabularyHandler handler = new ODIPVocabularyHandler();
	handler.setOutputFormat(OutputFormat.JSON);

	String output = null;
	try {
	    if (label != null) {
		output = handler.getTerm(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target), label);
	    } else if (term != null) {
		output = handler.getLabel(term);
	    } else {
		output = handler.listLabels(Profile.valueOf(Profile.class, profile), Target.valueOf(Target.class, target), suggestion);
	    }
	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    JSONObject object = new JSONObject();
	    object.put("error", ex.getMessage());
	    output = object.toString();
	}

	String safeCallback = Encode.forHtml(callback);
	output = safeCallback + "(" + output + ")";

	return Response.ok(output, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/metadata-stats")
    public Response metadataStats(//
	    @QueryParam("view") String viewId, @QueryParam("source") String sourceId, @QueryParam("token") String token,
	    @QueryParam("language") String language) { //
	JSONObject output = new JSONObject();
	JSONArray statsArray = new JSONArray();
	output.put("stats", statsArray);
	PortalTranslator translator = new PortalTranslator(language);
	StatisticsMessage statisticsMessage = new StatisticsMessage();
	View view = null;
	try {
	    view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId).get();
	} catch (GSException e) {
	    e.printStackTrace();
	}

	List<GSSource> sources = ConfigurationWrapper.getViewSources(view);

	// set the required properties
	statisticsMessage.setSources(sources);
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	// set the view

	try {
	    WebRequestTransformer.setView(//
		    viewId, //
		    statisticsMessage.getDataBaseURI(), //
		    statisticsMessage);
	} catch (GSException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	// set the user bond
	if (sourceId != null && !sourceId.isEmpty()) {
	    statisticsMessage.setUserBond(BondFactory.createSourceIdentifierBond(sourceId));
	}

	// groups by source id
	statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);

	// pagination works with grouped results. in this case there is one result item for each source.
	// in order to be sure to get all the items in the same statistics response,
	// we set the count equals to number of sources

	statisticsMessage.setPage(new Page(1, 100));

	// computes union of bboxes
	statisticsMessage.computeBboxUnion();
	List<Queryable> minArray = new ArrayList<>();
	minArray.add(MetadataElement.ELEVATION_MIN);
	statisticsMessage.computeMin(minArray);
	List<Queryable> maxArray = new ArrayList<>();
	maxArray.add(MetadataElement.ELEVATION_MAX);
	statisticsMessage.computeMax(maxArray);
	statisticsMessage.computeTempExtentUnion();
	List<Queryable> freqs = new ArrayList<>();
	freqs.add(MetadataElement.ORGANIZATION);
	freqs.add(MetadataElement.ATTRIBUTE_TITLE);
	statisticsMessage.computeFrequency(freqs, 1000);
	List<Queryable> distArray = new ArrayList<>();
	distArray.add(MetadataElement.ATTRIBUTE_TITLE);
	distArray.add(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER);
	distArray.add(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER);
	distArray.add(MetadataElement.IDENTIFIER);
	// computes count distinct of 2 queryables
	statisticsMessage.countDistinct(distArray);

	// statisticsMessage.computeSum(Arrays.asList(MetadataElement.DATA_SIZE));

	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	StatisticsResponse statResponse = null;
	try {
	    statResponse = executor.compute(statisticsMessage);
	} catch (GSException e) {
	    e.printStackTrace();
	    return Response.serverError().entity("").build();
	}
	List<ResponseItem> items = statResponse.getItems();
	HashMap<String, Stats> smap = new HashMap<>();
	for (ResponseItem responseItem : items) {
	    String id = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
	    GSSource source = ConfigurationWrapper.getSource(id);
	    JSONObject stat = new JSONObject();
	    stat.put("source", id);
	    stat.put("source-label", source.getLabel());
	    // stat.put(", false)

	    JSONArray orgs = toJSON(responseItem.getFrequency(MetadataElement.ORGANIZATION));
	    stat.put("organization-stats", orgs);

	    JSONArray frequencies = toJSON(responseItem.getFrequency(MetadataElement.ATTRIBUTE_TITLE));
	    stat.put("attribute-stats", frequencies);

	    stat.put("site-count", responseItem.getCountDistinct(MetadataElement.UNIQUE_PLATFORM_IDENTIFIER).get().getValue());
	    stat.put("unique-attribute-count", responseItem.getCountDistinct(MetadataElement.UNIQUE_ATTRIBUTE_IDENTIFIER).get().getValue());
	    stat.put("attribute-count", responseItem.getCountDistinct(MetadataElement.ATTRIBUTE_TITLE).get().getValue());
	    stat.put("timeseries-count", responseItem.getCountDistinct(MetadataElement.IDENTIFIER).get().getValue());
	    Optional<CardinalValues> cardinalValues = responseItem.getBBoxUnion().getCardinalValues();
	    String union = responseItem.getTempExtentUnion().getValue();
	    String begin = union.split(" ")[0];
	    String end = union.split(" ")[1];
	    if (cardinalValues.isPresent()) {
		stat.put("east", Double.parseDouble(cardinalValues.get().getEast()));
		stat.put("north", Double.parseDouble(cardinalValues.get().getNorth()));
		stat.put("south", Double.parseDouble(cardinalValues.get().getSouth()));
		stat.put("west", Double.parseDouble(cardinalValues.get().getWest()));

	    }
	    stat.put("begin", begin);
	    stat.put("end", end);
	    if (responseItem.getMin(MetadataElement.ELEVATION_MIN).isPresent()) {
		stat.put("minimimum-elevation", responseItem.getMin(MetadataElement.ELEVATION_MIN).get().getValue());
	    }
	    if (responseItem.getMin(MetadataElement.ELEVATION_MAX).isPresent()) {
		stat.put("maximum-elevation", responseItem.getMin(MetadataElement.ELEVATION_MAX).get().getValue());
	    }

	    statsArray.put(stat);
	}
	return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();
    }

    private JSONArray toJSON(Optional<ComputationResult> propFreq) {
	JSONArray frequencies = new JSONArray();

	if (propFreq.isPresent()) {
	    ComputationResult of = propFreq.get();
	    List<TermFrequencyItem> fitems = of.getFrequencyItems();
	    for (TermFrequencyItem fitem : fitems) {
		JSONObject frequency = new JSONObject();
		frequency.put("term", fitem.getTerm());
		frequency.put("label", fitem.getLabel());
		frequency.put("count", fitem.getFreq());
		Map<String, String> props = fitem.getNestedProperties();
		if (props != null) {
		    JSONObject properties = new JSONObject();
		    frequency.put("properties", properties);
		    Set<Entry<String, String>> entries = props.entrySet();
		    for (Entry<String, String> entry : entries) {
			String key = entry.getKey();
			String value = entry.getValue();
			properties.put(key, value);
		    }
		}
		frequencies.put(frequency);
	    }
	}
	return frequencies;
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    @Path("/rating-curves")
    public Response getRatingCurves(//
	    @QueryParam("platformId") String platformId, //
	    @QueryParam("view") String viewId, //
	    @QueryParam("token") String token) { //

	if (platformId == null || platformId.isEmpty()) {
	    return Response.serverError().entity(getErrorResponse("platformId parameter is required").toString()).build();
	}

	try {
	    ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	    IDiscoveryExecutor executor = loader.iterator().next();

	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    discoveryMessage.setRequestId("rating-curves-" + UUID.randomUUID().toString());

	    discoveryMessage.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	    discoveryMessage.getResourceSelector().setSubset(ResourceSubset.CORE_EXTENDED);
	    discoveryMessage.setPage(new Page(1, 100));
	    discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	    discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	    discoveryMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	    Set<Bond> operands = new HashSet<>();

	    // Filter for downloadable datasets
	    ResourcePropertyBond accessBond = BondFactory.createIsExecutableBond(true);
	    operands.add(accessBond);

	    ResourcePropertyBond downBond = BondFactory.createIsDownloadableBond(true);
	    operands.add(downBond);

	    // Filter for RATING_CURVE data type
	    ResourcePropertyBond ratingCurveBond = BondFactory.createIsRatingCurveBond(true);
	    operands.add(ratingCurveBond);

	    // Filter by platform identifier
	    SimpleValueBond platformBond = BondFactory.createSimpleValueBond(//
		    BondOperator.EQUAL, //
		    MetadataElement.UNIQUE_PLATFORM_IDENTIFIER, //
		    platformId);
	    operands.add(platformBond);

	    Bond bond = null;
	    switch (operands.size()) {
	    case 0:
		break;
	    case 1:
		bond = operands.iterator().next();
		break;
	    default:
		bond = BondFactory.createAndBond(operands);
	    }

	    // Set view if provided
	    if (viewId != null && !viewId.isEmpty()) {
		try {
		    View view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId).get();
		    discoveryMessage.setView(view);
		} catch (GSException e) {
		    GSLoggerFactory.getLogger(getClass()).warn("View not found: " + viewId, e);
		}
	    }

	    discoveryMessage.setPermittedBond(bond);
	    discoveryMessage.setUserBond(bond);
	    discoveryMessage.setNormalizedBond(bond);

	    ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	    List<GSResource> resources = resultSet.getResultsList();

	    if (resources.isEmpty()) {
		return Response.serverError().entity(getErrorResponse("No rating curves found for platform: " + platformId).toString())
			.build();
	    }

	    // Perform access request for the first rating curve found
	    GSResource resource = resources.get(0);
	    String onlineId = resource.getHarmonizedMetadata().getCoreMetadata().getOnline().getIdentifier();
	    
	    if (onlineId == null || onlineId.isEmpty()) {
		return Response.serverError().entity(getErrorResponse("Online ID not found for rating curve").toString()).build();
	    }

	    // Execute access request
	    ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	    IAccessExecutor accessExecutor = accessLoader.iterator().next();

	    AccessMessage accessMessage = new AccessMessage();
	    accessMessage.setOnlineId(onlineId);
	    accessMessage.setSources(discoveryMessage.getSources());
	    accessMessage.setCurrentUser(discoveryMessage.getCurrentUser().orElse(null));
	    accessMessage.setDataBaseURI(discoveryMessage.getDataBaseURI());

	    DataDescriptor descriptor = new DataDescriptor();
	    descriptor.setDataFormat(DataFormat.WATERML_2_0());
	    descriptor.setDataType(DataType.RATING_CURVE);
	    descriptor.setCRS(CRS.EPSG_4326());
	    accessMessage.setTargetDataDescriptor(descriptor);

	    ResultSet<DataObject> accessResult = accessExecutor.retrieve(accessMessage);

	    if (accessResult.getResultsList().isEmpty()) {
		return Response.serverError().entity(getErrorResponse("Unable to retrieve rating curve data").toString()).build();
	    }

	    DataObject dataObject = accessResult.getResultsList().get(0);
	    File dataFile = dataObject.getFile();

	    // Read the XML file content
	    String xmlContent;
	    try (FileInputStream stream = new FileInputStream(dataFile)) {
		xmlContent = IOUtils.toString(stream, StandardCharsets.UTF_8);
		stream.close();
		dataFile.delete();
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error("Error reading rating curve file", e);
		return Response.serverError().entity(getErrorResponse("Error reading rating curve data: " + e.getMessage()).toString())
			.build();
	    }

	    return Response.ok(xmlContent, MediaType.APPLICATION_XML).build();

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error retrieving rating curves", e);
	    return Response.serverError().entity(getErrorResponse("Error retrieving rating curves: " + e.getMessage()).toString())
		    .build();
	}
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/data-stats")
    public Response dataCacheStats(//
	    @QueryParam("view") String view) { //

	JSONObject output = new JSONObject();
	if (view == null || view.isEmpty()) {
	    return Response.serverError().entity(getErrorResponse("view parameter not specified").toString()).build();
	}
	View v;
	try {
	    v = DiscoveryRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), view).get();
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return Response.serverError().entity(getErrorResponse(e.getMessage()).toString()).build();
	}
	List<GSSource> sources = ConfigurationWrapper.getViewSources(v);
	List<String> sourceIdentifiers = new ArrayList<String>();
	for (GSSource source : sources) {
	    sourceIdentifiers.add(source.getUniqueIdentifier());
	}
	DataCacheConnector dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();
	if (dataCacheConnector == null) {
	    try {
		dataCacheConnector = DataCacheConnectorFactory.newDefaultDataCacheConnector();
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
		return Response.serverError().entity(getErrorResponse("error init data cache connector").toString()).build();

	    }
	}
	Map<String, Long> datasetsInDatabase = null;
	try {
	    datasetsInDatabase = getDatasetsInDatabase(sources, view);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return Response.serverError().entity(getErrorResponse("error counting datasets").toString()).build();
	}

	Map<String, SourceCacheStats> stats = null;
	try {
	    stats = dataCacheConnector.getCacheStatsPerSource(sourceIdentifiers);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return Response.serverError().entity(getErrorResponse("error counting cached datasets").toString()).build();
	}

	JSONArray sourcesArray = new JSONArray();

	for (GSSource source : sources) {
	    JSONObject jsonSource = new JSONObject();
	    jsonSource.put("name", source.getLabel());
	    jsonSource.put("id", source.getUniqueIdentifier());
	    Long dbCount = datasetsInDatabase.get(source.getUniqueIdentifier());
	    if (dbCount == null) {
		dbCount = 0l;
	    }
	    jsonSource.put("datasetsInDatabase", dbCount);
	    SourceCacheStats sourceStats = stats.get(source.getUniqueIdentifier());

	    try {
		Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());
		SourceStorageWorker worker = database.getWorker(source.getUniqueIdentifier());
		HarvestingProperties harvestingProperties = worker.getHarvestingProperties();
		String lastHarvesting = harvestingProperties.getEndHarvestingTimestamp();
		jsonSource.put("lastHarvesting", lastHarvesting);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    if (sourceStats != null) {
		Long cacheCount = sourceStats.getUniqueDatasetCount();
		if (cacheCount == null) {
		    cacheCount = 0l;
		}
		jsonSource.put("datasetsInCache", cacheCount);

		Long valuesCount = sourceStats.getRecordCount();
		if (valuesCount == null) {
		    valuesCount = 0l;
		}
		jsonSource.put("valuesInCache", valuesCount);

		if (valuesCount > 0) {
		    jsonSource.put("oldestInsert", ISO8601DateTimeUtils.getISO8601DateTime(sourceStats.getOldestInsert()));
		    jsonSource.put("newestInsert", ISO8601DateTimeUtils.getISO8601DateTime(sourceStats.getNewestInsert()));
		    jsonSource.put("avgAgeHours", sourceStats.getAverageAgeHours());
		}
	    }

	    sourcesArray.put(jsonSource);
	}
	output.put("sources", sourcesArray);

	output.put("status", "success");
	return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();
    }

    private Map<String, Long> getDatasetsInDatabase(List<GSSource> sources, String view) throws Exception {
	Map<String, Long> ret = new HashMap<String, Long>();

	StatisticsMessage statisticsMessage = new StatisticsMessage();
	statisticsMessage.setSources(sources);
	statisticsMessage.setDataBaseURI(ConfigurationWrapper.getStorageInfo());

	WebRequestTransformer.setView(//
		view, //
		statisticsMessage.getDataBaseURI(), //
		statisticsMessage);
	statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);
	Page page = new Page();
	page.setStart(1);
	page.setSize(1000);
	statisticsMessage.setPage(page);
	statisticsMessage.countDistinct(//
		Arrays.asList(//
			MetadataElement.ONLINE_ID//
		));
	ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
	IStatisticsExecutor executor = loader.iterator().next();

	StatisticsResponse response = executor.compute(statisticsMessage);
	List<ResponseItem> items = response.getItems();
	for (ResponseItem responseItem : items) {
	    String id = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
	    String countString = responseItem.getCountDistinct(MetadataElement.ONLINE_ID).get().getValue();
	    Long count = Long.parseLong(countString);
	    ret.put(id, count);
	}
	return ret;
    }

    private JSONObject getErrorResponse(String error) {
	JSONObject ret = new JSONObject();
	ret.put("status", "error");
	ret.put("message", error);
	return ret;
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);

	if (loginResponse.isSuccess()) {

	    return Response.ok(loginResponse).build();

	} else {

	    return Response.serverError().entity(loginResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    private LoginResponse getLoginResponse(LoginRequest request) {

	try {

	    UserFinder uf = UserFinder.create();
	    List<GSUser> users = uf.getUsers(false);

	    for (GSUser user : users) {

		String firstName = null;
		String email = null;
		String lastName = null;

		List<GSProperty> properties = user.getProperties();

		for (GSProperty<?> prop : properties) {
		    if (prop.getName().equals("firstName")) {
			firstName = prop.getValue().toString();
		    }
		    if (prop.getName().equals("lastName")) {
			firstName = prop.getValue().toString();
		    }
		    if (prop.getName().equals("email")) {
			email = prop.getValue().toString();
		    }
		}

		if (request.getApiKey().equals(user.getUri()) && request.getEmail().equals(email)) {

		    LoginResponse response = new LoginResponse(//
			    true, //
			    "Login successful", //
			    user.getStringPropertyValue("firstName").get(), //
			    user.getStringPropertyValue("lastName").get(), //
			    request.getEmail(), //
			    request.getApiKey());

		    Optional<String> perm = user.getStringPropertyValue("permissions");
		    if (perm.isPresent()) {
		    response.setPermissions(perm.get());
		    }
		    
		    response.setUser(user);

		    List<String> adminUsers = ConfigurationWrapper.getAdminUsers();

		    if (adminUsers != null) {
			for (String adminUser : adminUsers) {
			    if (user.getUri().equals(adminUser) || request.getEmail().equals(adminUser)) {
				response.setAdmin(true);
			    }
			}
		    }

		    return response;
		}
	    }
	    LoginResponse response = new LoginResponse(false, "Invalid credentials", null, null, null, null);
	    return response;

	} catch (Exception ex) {
	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    LoginResponse resp = new LoginResponse(false, "Server error: " + ex.getMessage(), null, null, null, null);
	    return resp;
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/listUsers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);
	BasicResponse listResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		listResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    HashMap<String, List<GSUser>> usersByRole = new HashMap<String, List<GSUser>>();
		    GSUser adminUser = null;
		    for (GSUser user : users) {
			if (request.getApiKey().equals(user.getUri())) {
			    adminUser = user;
			}
			String role = user.getRole();
			List<GSUser> list = usersByRole.get(role);
			if (list == null) {
			    list = new ArrayList<GSUser>();
			    usersByRole.put(role, list);
			}
			list.add(user);
		    }
		    if (adminUser == null) {
			listResponse.setSuccess(false);
			listResponse.setMessage("admin user not found");
			return Response.serverError().entity(listResponse).build();
		    }
		    List<GSUser> userList = usersByRole.get(adminUser.getRole());
		    for (GSUser user : userList) {
			listResponse.getUsers().add(user);
		    }
		    return Response.ok(listResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    listResponse.setSuccess(false);
		    listResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(listResponse).build();
		}

	    } else {
		listResponse.setSuccess(false);
		listResponse.setMessage("not authorized");
		return Response.serverError().entity(listResponse).build();
	    }
	} else {
	    listResponse.setSuccess(false);
	    listResponse.setMessage("not authenticated");
	    return Response.serverError().entity(listResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/updateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UpdateUserRequest request) {
	LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getApiKey());
	LoginResponse loginResponse = getLoginResponse(loginRequest);
	BasicResponse basicResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		basicResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    GSUser targetUser = null;
		    for (GSUser user : users) {
			if (request.getUserIdentifier().equals(user.getUri())) {
			    targetUser = user;
			}
		    }
		    if (targetUser == null) {
			basicResponse.setSuccess(false);
			basicResponse.setMessage("target user not found");
			return Response.serverError().entity(basicResponse).build();
		    }
		    targetUser.setPropertyValue(request.getPropertyName(), request.getPropertyValue());
		    uf.getUsersWriter().store(targetUser);

		    return Response.ok(basicResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    basicResponse.setSuccess(false);
		    basicResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(basicResponse).build();
		}

	    } else {
		basicResponse.setSuccess(false);
		basicResponse.setMessage("not authorized");
		return Response.serverError().entity(basicResponse).build();
	    }
	} else {
	    basicResponse.setSuccess(false);
	    basicResponse.setMessage("not authenticated");
	    return Response.serverError().entity(basicResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @DELETE
    @Path("/deleteUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(UpdateUserRequest request) {
	LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getApiKey());
	LoginResponse loginResponse = getLoginResponse(loginRequest);
	BasicResponse basicResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		basicResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    GSUser targetUser = null;
		    for (GSUser user : users) {
			if (request.getUserIdentifier().equals(user.getUri())) {
			    targetUser = user;
			}
		    }
		    if (targetUser == null) {
			basicResponse.setSuccess(false);
			basicResponse.setMessage("target user not found");
			return Response.serverError().entity(basicResponse).build();
		    }
		    uf.getUsersWriter().removeUser(request.getUserIdentifier());

		    return Response.ok(basicResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    basicResponse.setSuccess(false);
		    basicResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(basicResponse).build();
		}

	    } else {
		basicResponse.setSuccess(false);
		basicResponse.setMessage("not authorized");
		return Response.serverError().entity(basicResponse).build();
	    }
	} else {
	    basicResponse.setSuccess(false);
	    basicResponse.setMessage("not authenticated");
	    return Response.serverError().entity(basicResponse).build();
	}
    }

    @SuppressWarnings("rawtypes")
    @POST
    @Path("/modifyUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyUser(LoginRequest request) {
	LoginResponse loginResponse = getLoginResponse(request);
	BasicResponse listResponse = new BasicResponse();
	if (loginResponse.isSuccess()) {
	    if (loginResponse.isAdmin()) {
		listResponse.setSuccess(true);

		try {
		    UserFinder uf = UserFinder.create();
		    List<GSUser> users = uf.getUsers(false);
		    HashMap<String, List<GSUser>> usersByRole = new HashMap<String, List<GSUser>>();
		    GSUser adminUser = null;
		    for (GSUser user : users) {
			if (request.getApiKey().equals(user.getUri())) {
			    adminUser = user;
			}
			String role = user.getRole();
			List<GSUser> list = usersByRole.get(role);
			if (list == null) {
			    list = new ArrayList<GSUser>();
			    usersByRole.put(role, list);
			}
			list.add(user);
		    }
		    if (adminUser == null) {
			listResponse.setSuccess(false);
			listResponse.setMessage("admin user not found");
			return Response.serverError().entity(listResponse).build();
		    }
		    List<GSUser> userList = usersByRole.get(adminUser.getRole());
		    for (GSUser user : userList) {
			listResponse.getUsers().add(user);
		    }
		    return Response.ok(listResponse).build();
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    listResponse.setSuccess(false);
		    listResponse.setMessage("error retrieving users");
		    return Response.serverError().entity(listResponse).build();
		}

	    } else {
		listResponse.setSuccess(false);
		listResponse.setMessage("not authorized");
		return Response.serverError().entity(listResponse).build();
	    }
	} else {
	    listResponse.setSuccess(false);
	    listResponse.setMessage("not authenticated");
	    return Response.serverError().entity(listResponse).build();
	}
    }
}
