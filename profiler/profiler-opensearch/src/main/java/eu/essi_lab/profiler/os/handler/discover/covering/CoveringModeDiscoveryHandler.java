package eu.essi_lab.profiler.os.handler.discover.covering;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.ResultSet;
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
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.handler.DiscoveryHandler;

/**
 * @author Fabrizio
 */
public class CoveringModeDiscoveryHandler extends DiscoveryHandler<String> {

    /**
     * 
     */
    public static final String COVERING_MODE_VIEW_ID = "coveringModeView";

    /**
     * 
     */
    private static final GeometryFactory FACTORY = JTSFactoryFinder.getGeometryFactory();

    private DecimalFormat format;
    private int coveringTreshold;
    private int skippedQueriesCount;
    private int failedQueriesCount;

    private List<String> polygons;
    private List<String> ids;
    private List<SpatialExtent> extentsToPartition;

    //
    //
    //
    private double globalCoveringPercentage;
    private double coveringPercentageRatio;
    //
    //
    //

    public CoveringModeDiscoveryHandler() {

	super();

	format = new DecimalFormat();
	format.setMaximumFractionDigits(3);
    }

    @Override
    public Response handleMessageRequest(DiscoveryMessage message) throws GSException {

	String rid = message.getWebRequest().getRequestId();

	Optional<WebRequest> owr = Optional.ofNullable(message.getWebRequest());

	GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check STARTED");
	PerformanceLogger pl = new PerformanceLogger(PerformanceLogger.PerformancePhase.MESSAGE_AUTHORIZATION, rid, owr);

	boolean authorized = getExecutor().isAuthorized(message);

	pl.logPerformance(GSLoggerFactory.getLogger(getClass()));
	GSLoggerFactory.getLogger(getClass()).info("[2/2] Message authorization check ENDED");

	GSLoggerFactory.getLogger(getClass()).info("Message authorization {}", (authorized ? "approved" : "denied"));

	if (!authorized) {

	    return handleNotAuthorizedRequest((DiscoveryMessage) message);
	}

	if (CoveringModeOptionsReader.isViewOnlyEnabled()) {
	    return super.handleMessageRequest(message);
	}

	return handleCoveringModeDiscovery(message);
    }

    /**
     * @param message
     * @return
     * @throws GSException
     */
    private Response handleCoveringModeDiscovery(DiscoveryMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Handling covering mode discovery query STARTED");

	//
	// this is for test purpose
	//
	message.setRequestTimeout(1200);

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("[1/5] Executing contains query STARTED");

	ResultSet<GSResource> userQueryResponse = getExecutor().retrieve(message);

	CountSet containsQueryCountSet = userQueryResponse.getCountResponse();

	GSLoggerFactory.getLogger(getClass()).info("[1/5] Executing contains query ENDED");

	if (containsQueryCountSet.getCount() == 0) {

	    GSLoggerFactory.getLogger(getClass()).info("Contains query has 0 results, nothing else to do");

	    ResultSet<String> mappedResponse = getMessageResponseMapper().map(message, userQueryResponse);

	    Response response = getMessageResponseFormatter().format(message, mappedResponse);

	    publish(message, mappedResponse);

	    GSLoggerFactory.getLogger(getClass()).info("Handling covering mode discovery query ENDED");

	    return response;
	}

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("[2/5] Retrieving query bounding box STARTED");

	DiscoveryBondParser parser = new DiscoveryBondParser(message);

	GetSpatialExtentBondHandler bondHandler = new GetSpatialExtentBondHandler();

	parser.parse(bondHandler);

	Optional<SpatialExtent> extent = bondHandler.getExtent();

	if (!extent.isPresent()) {

	    ResultSet<String> mappedResponse = getMessageResponseMapper().map(message, userQueryResponse);

	    Response response = getMessageResponseFormatter().format(message, mappedResponse);

	    publish(message, mappedResponse);

	    GSLoggerFactory.getLogger(getClass()).info("No query bounding box found, covering mode discovery query ENDED");

	    return response;
	}

	GSLoggerFactory.getLogger(getClass()).info("Extent found: {}", extent);

	GSLoggerFactory.getLogger(getClass()).info("[2/5] Retrieving query bounding box ENDED");

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("[3/5] Extent partitioning STARTED");

	double partitionSize = CoveringModeOptionsReader.getPartitionSize();// the size of each partition side

	GSLoggerFactory.getLogger(getClass()).info("Initial partition size: {}", partitionSize);

	GSLoggerFactory.getLogger(getClass()).info("[3/5] Extent partitioning ENDED");

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("[4/5] Executing partitions query STARTED");

	ids = new ArrayList<>();
	polygons = new ArrayList<>();
	extentsToPartition = new ArrayList<>();
	skippedQueriesCount = 0;
	failedQueriesCount = 0;

	//
	//
	//

	globalCoveringPercentage = 0;

	//
	//
	//

	boolean temporalConstraintEnabled = CoveringModeOptionsReader.isTemporalConstraintEnabled();

	if (temporalConstraintEnabled) {

	    GSLoggerFactory.getLogger(getClass()).info("Temporal constraint enabled");

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Temporal constraint disabled");
	}

	coveringTreshold = CoveringModeOptionsReader.getCoveringThreshold();

	GSLoggerFactory.getLogger(getClass()).info("Covering threshold: {}%", coveringTreshold);

	Optional<String> productType = CoveringModeOptionsReader.getProductType();

	if (productType.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Product type: {}", productType.get());
	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Product type not set");
	}

	int maxIterations = CoveringModeOptionsReader.getMaxIterations();
	GSLoggerFactory.getLogger(getClass()).info("Max iterations: {}", maxIterations);

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("[4/5] Executing partitions query ENDED");

	//
	//
	//

	List<SpatialExtent> partitions = getPartitions(extent.get(), partitionSize);

	GSLoggerFactory.getLogger(getClass()).info("Number of initial partitions: {}", partitions.size());

	coveringPercentageRatio = countPartitions(extent.get(), partitionSize);

	int iterationsCount = 1;

	while (iterationsCount <= maxIterations) {

	    GSLoggerFactory.getLogger(getClass()).info("Iteration [{}/{}] STARTED", iterationsCount, maxIterations);

	    extentsToPartition.clear();

	    executePartionsQueries(partitions, message, productType, temporalConstraintEnabled);

	    if (globalCoveringPercentage >= coveringTreshold) {

		GSLoggerFactory.getLogger(getClass()).info("Global covering treshold reached, exit!");
		break;
	    }

	    //
	    //
	    //

	    partitionSize /= 2;

	    GSLoggerFactory.getLogger(getClass()).info("Updated partition size: {}", partitionSize);

	    partitions.clear();

	    GSLoggerFactory.getLogger(getClass()).info("Number of extents to partition again: {}", extentsToPartition.size());

	    for (SpatialExtent spatialExtent : extentsToPartition) {

		List<SpatialExtent> subPartitions = getPartitions(spatialExtent, partitionSize);

		partitions.addAll(subPartitions);
	    }

	    coveringPercentageRatio = countPartitions(extent.get(), partitionSize);

	    GSLoggerFactory.getLogger(getClass()).info("Next partitions to process: {}", partitions.size());

	    GSLoggerFactory.getLogger(getClass()).info("Iteration [{}/{}] ENDED", iterationsCount, maxIterations);

	    iterationsCount++;
	}

	GSLoggerFactory.getLogger(getClass()).info("[5/5] Executing identifiers query STARTED");

	List<Bond> idsBondOperands = ids.//
		stream().//
		map(id -> BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id)).//
		collect(Collectors.toList());

	Bond idsBond = idsBondOperands.isEmpty() ? BondFactory.createOrBond() : // empty bond in case of no identifiers
		idsBondOperands.size() > 1 ? BondFactory.createOrBond(idsBondOperands) : //
			idsBondOperands.get(0);

	message.setUserBond(idsBond);

	message.setPage(
		new Page(message.getPage().getStart(), CoveringModeOptionsReader.getPageSize().orElse(message.getPage().getSize())));

	ResultSet<GSResource> idsQueryResponse = getExecutor().retrieve(message);

	ResultSet<String> mappedResponse = getMessageResponseMapper().map(message, idsQueryResponse);

	Response response = getMessageResponseFormatter().format(message, mappedResponse);

	publish(message, mappedResponse);

	GSLoggerFactory.getLogger(getClass()).info("[5/5] Executing identifiers query ENDED");

	//
	//
	//

	int executedQueries = partitions.size() - skippedQueriesCount;

	// String skippedPercent = format.format((((double) skippedQueriesCount) / partitions.size()) * 100);
	String failedPercent = format.format((((double) failedQueriesCount) / executedQueries) * 100);
	String succeededPercent = format.format((((double) ids.size()) / executedQueries) * 100);

	GSLoggerFactory.getLogger(getClass()).info("----------------------------");
	// GSLoggerFactory.getLogger(getClass()).info("Total queries: {}", partitions.size());
	// GSLoggerFactory.getLogger(getClass()).info("Skipped queries: {}", skippedQueriesCount);
	// GSLoggerFactory.getLogger(getClass()).info("Skipped queries/total queries: {}%", skippedPercent);
	// GSLoggerFactory.getLogger(getClass()).info("");
	GSLoggerFactory.getLogger(getClass()).info("Executed queries: {}", executedQueries);
	GSLoggerFactory.getLogger(getClass()).info("Failed queries: {}", failedQueriesCount);
	GSLoggerFactory.getLogger(getClass()).info("Failed queries/executed queries: {}%", failedPercent);
	GSLoggerFactory.getLogger(getClass()).info("Succeded queries/executed queries: {}%", succeededPercent);
	GSLoggerFactory.getLogger(getClass()).info("");

	GSLoggerFactory.getLogger(getClass()).info("Resources to retrieve: {}", ids.size());
	GSLoggerFactory.getLogger(getClass()).info("Iterations: {}", iterationsCount);
	GSLoggerFactory.getLogger(getClass()).info("Partitions size: {}", partitionSize);
	GSLoggerFactory.getLogger(getClass()).info("Global covering percentage: {}%", format.format(globalCoveringPercentage));

	GSLoggerFactory.getLogger(getClass()).info("----------------------------");

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).info("Handling covering mode discovery query ENDED");

	return response;
    }

    /**
     * @param partitions
     * @param message
     * @param productType
     * @param db
     * @param temporalConstraintEnabled
     */
    private void executePartionsQueries(//
	    List<SpatialExtent> partitions, //
	    DiscoveryMessage message, //
	    Optional<String> productType, //
	    boolean temporalConstraintEnabled) {

	for (int i = 0; i < partitions.size() && globalCoveringPercentage < coveringTreshold; i++) {

	    GSLoggerFactory.getLogger(getClass()).info("Executing partition query [{}/{}] STARTED", i + 1, partitions.size());

	    SpatialExtent currentPartition = partitions.get(i);

	    // if (isCovered(currentPartition, polygons)) {
	    //
	    // skippedQueriesCount++;
	    //
	    // GSLoggerFactory.getLogger(getClass()).info("Current extent partition already covered, skipping partition
	    // query");
	    // GSLoggerFactory.getLogger(getClass()).info("Skipped partition queries: [{}/{}]", skippedQueriesCount,
	    // partitions.size());
	    //
	    // continue;
	    // }

	    SpatialBond spatialBond = new SpatialBond();
	    spatialBond.setOperator(BondOperator.CONTAINED);
	    spatialBond.setPropertyValue(currentPartition);
	    spatialBond.setProperty(MetadataElement.BOUNDING_BOX);

	    //
	    // it is for sure an AND bond
	    //
	    LogicalBond logical = (LogicalBond) message.getPermittedBond().clone();
	    logical.getOperands().add(spatialBond);

	    if (productType.isPresent()) {

		logical.getOperands().add(//
			BondFactory.createSimpleValueBond(//
				BondOperator.EQUAL, //
				MetadataElement.PRODUCT_TYPE, //
				productType.get()));
	    }

	    message.setUserBond(logical);

	    try {

		DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getDatabaseURI());

		JSONObject jsonObject = executor.executePartitionsQuery(message, temporalConstraintEnabled);

		String identifier = jsonObject.getString("id");

		if (!identifier.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).info("Partition query succeeded");

		    // String box = jsonObject.getString("box").trim();

		    String polygon = jsonObject.getString("polygon").trim();

		    LinearRing polygonRing = asLinearRing(polygon);

		    LinearRing partitionRing = asLinearRing(currentPartition);

		    boolean covers = covers(polygonRing, partitionRing);

		    double coveringPercentage = getPolygonCoveringPercentage(polygonRing, partitionRing);

		    if (covers || coveringPercentage >= coveringTreshold) {

			GSLoggerFactory.getLogger(getClass()).info("Polygon covering check SUCCEEDED");

			GSLoggerFactory.getLogger(getClass()).info("Polygon covering percentage: {}%", format.format(coveringPercentage));

			//
			//
			//

			globalCoveringPercentage += computeLocalCoveringPercentage(coveringPercentage);

			GSLoggerFactory.getLogger(getClass()).info("Global covering percentage: {}%",
				format.format(globalCoveringPercentage));

			polygons.add(polygon);

			if (!ids.contains(identifier)) {
			    ids.add(identifier);
			}

		    } else {

			extentsToPartition.add(currentPartition);

			GSLoggerFactory.getLogger(getClass()).info("Polygon covering check FAILED");
		    }

		} else {

		    failedQueriesCount++;

		    extentsToPartition.add(currentPartition);

		    GSLoggerFactory.getLogger(getClass()).info("Partition query failed");
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Executing partition query [{}/{}] ENDED", i + 1, partitions.size());

	}

    }

    /**
     * @param extent
     * @param partitionSize
     * @return
     */
    private List<SpatialExtent> getPartitions(SpatialExtent extent, double partitionSize) {

	double south = extent.getSouth();
	double west = extent.getWest();
	double north = extent.getNorth();
	double east = extent.getEast();

	List<SpatialExtent> partitions = new ArrayList<>();

	DecimalFormat decimalFormat = new DecimalFormat();
	decimalFormat.setMaximumFractionDigits(3);

	for (double x = west; x < east; x += partitionSize) {

	    for (double y = north; y > south; y -= partitionSize) {

		double w = Double.valueOf(decimalFormat.format(x));
		double n = Double.valueOf(decimalFormat.format(y));

		double e = Double.valueOf(decimalFormat.format(Math.min(x + partitionSize, east)));
		double s = Double.valueOf(decimalFormat.format(Math.max(y - partitionSize, south)));

		SpatialExtent partition = new SpatialExtent(s, w, n, e);
		partitions.add(partition);
	    }
	}

	return partitions;
    }

    /**
     * @param extent
     * @param partitionSize
     * @return
     */
    private int countPartitions(SpatialExtent extent, double partitionSize) {

	int count = 0;

	double south = extent.getSouth();
	double west = extent.getWest();
	double north = extent.getNorth();
	double east = extent.getEast();

	for (double x = west; x < east; x += partitionSize) {
	    for (double y = north; y > south; y -= partitionSize) {

		count++;
	    }
	}

	return count;
    }

    /**
     * Returns true if the given extent is covered by at least one of the provided polygons
     * 
     * @return
     */
    private boolean isCovered(SpatialExtent extent, List<String> polygons) {

	return polygons.//
		stream().//
		anyMatch(polygon -> {

		    LinearRing polygonRing = asLinearRing(polygon);

		    LinearRing partitionRing = asLinearRing(extent);

		    return coversOrOverTreshold(polygonRing, partitionRing);
		});
    }

    /**
     * True if polygon covers the given bbox or the covering percentage is gt or equals to the covering treshold
     * 
     * @param extent1
     * @param extent2
     * @return
     */
    private boolean coversOrOverTreshold(LinearRing polygonRing, LinearRing bboxRing) {

	boolean covers = covers(polygonRing, bboxRing);

	double coveringPercentage = getPolygonCoveringPercentage(polygonRing, bboxRing);

	if (covers || coveringPercentage >= coveringTreshold) {

	    GSLoggerFactory.getLogger(getClass()).info("Polygon covering check SUCCEEDED");

	    GSLoggerFactory.getLogger(getClass()).info("Polygon covering percentage: {}%", format.format(coveringPercentage));
	}

	return covers;
    }

    /**
     * @param coveringPercentage
     * @return
     */
    private double computeLocalCoveringPercentage(double coveringPercentage) {

	return coveringPercentage * (1 / coveringPercentageRatio);
    }

    /**
     * True if polygon covers the given bbox
     * 
     * @param extent1
     * @param extent2
     * @return
     */
    private boolean covers(LinearRing polygon, LinearRing bbox) {

	Polygon polygonPolygin = FACTORY.createPolygon(polygon);
	Polygon bboxPolygon = FACTORY.createPolygon(bbox);

	return polygonPolygin.contains(bboxPolygon);
    }

    /**
     * @param polygon
     * @return
     */
    private LinearRing asLinearRing(String polygon) {

	if (polygon.startsWith("POLYGON ((")) {

	    polygon = polygon.replace("POLYGON ((", "");
	    polygon = polygon.replace("))", "");

	} else if (polygon.startsWith("MULTIPOLYGON (((")) {

	    polygon = polygon.replace("MULTIPOLYGON (((", "");
	    polygon = polygon.replace(")))", "");
	}

	Coordinate[] polygonCoordinates = Arrays.asList(polygon.split(",")).//
		stream().//
		map(p -> new CoordinateXY(//

			Double.valueOf(Double.valueOf(p.trim().split(" ")[0])), //
			Double.valueOf(Double.valueOf(p.trim().split(" ")[1]))))
		.//

		collect(Collectors.toList()).//
		toArray(new Coordinate[] {});

	return FACTORY.createLinearRing(polygonCoordinates);
    }

    /**
     * @param bbox
     * @return
     */
    private LinearRing asLinearRing(SpatialExtent extent) {

	double west = Double.valueOf(extent.getWest());
	double south = Double.valueOf(extent.getSouth());
	double east = Double.valueOf(extent.getEast());
	double north = Double.valueOf(extent.getNorth());

	Coordinate[] bboxCoordinates = { //
		new Coordinate(west, north), //
		new Coordinate(east, north), //
		new Coordinate(east, south), //
		new Coordinate(west, south), //
		new Coordinate(west, north)//
	};

	return FACTORY.createLinearRing(bboxCoordinates);
    }

    /**
     * Computes the intersection between polygon and bbox, and returns the percent ratio between the intersection area
     * and the bbox area
     * 
     * @param polygon
     * @param bbox
     * @return
     */
    private double getPolygonCoveringPercentage(LinearRing polygon, LinearRing bbox) {

	Polygon polygonPolygin = FACTORY.createPolygon(polygon);
	Polygon bboxPolygon = FACTORY.createPolygon(bbox);

	Geometry intersection = polygonPolygin.intersection(bboxPolygon);
	double intersectionArea = intersection.getArea();

	double bboxArea = bboxPolygon.getArea();

	return (intersectionArea / bboxArea) * 100;
    }

    /**
     * @author Fabrizio
     */
    private class GetSpatialExtentBondHandler implements DiscoveryBondHandler {

	private SpatialExtent extent;

	/**
	 * @return
	 */
	public Optional<SpatialExtent> getExtent() {

	    return Optional.ofNullable(extent);
	}

	@Override
	public void startLogicalBond(LogicalBond bond) {
	}

	@Override
	public void separator() {
	}

	@Override
	public void nonLogicalBond(Bond bond) {
	}

	@Override
	public void endLogicalBond(LogicalBond bond) {
	}

	@Override
	public void viewBond(ViewBond bond) {
	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {
	}

	@Override
	public void customBond(QueryableBond<String> bond) {
	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {
	}

	@Override
	public void spatialBond(SpatialBond bond) {

	    extent = (SpatialExtent) bond.getPropertyValue();
	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	}

    }

    public static void main(String[] args) {

	GeometryFactory factory = new GeometryFactory(); // default

	Coordinate[] coordinates1 = { //
		new CoordinateXY(0, 0), //
		new CoordinateXY(1000, 0), //
		new CoordinateXY(1000, 1000), //
		new CoordinateXY(0, 1000), //
		new CoordinateXY(0, 0) //
	};

	Coordinate[] coordinates2 = { //
		new CoordinateXY(500, 500), //
		new CoordinateXY(1000, 500), //
		new CoordinateXY(600, 600), //
		new CoordinateXY(500, 600), //
		new CoordinateXY(500, 500) //
	};

	LinearRing linearRing1 = factory.createLinearRing(coordinates1);
	LinearRing linearRing2 = factory.createLinearRing(coordinates2);

	Polygon polygon1 = factory.createPolygon(linearRing1);
	Polygon polygon2 = factory.createPolygon(linearRing2);

	System.out.println(polygon1.contains(polygon2));

	DecimalFormat format = new DecimalFormat();
	format.setMaximumFractionDigits(3);
	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
	decimalFormatSymbols.setDecimalSeparator('.');
	format.setDecimalFormatSymbols(decimalFormatSymbols);

	String format2 = format.format(Double.valueOf("46.9012405527728"));

	//
	//
	//

	String polygon = "POLYGON ((1.6865105890409362 46.9012405527728,1.708999870250291 45.958258688896564,3.062854710000743 45.965160008316964,4.353573186296003 45.95716081381799,5.6428630674928995 45.93460549534843,6.993002166545218 45.895735251937936,6.99669740896237 45.94586934246653,7.835955516975941 45.95962253249568,7.81660262114173 46.85958232134775,7.816648466915317 46.859583083848264,7.815676492112574 46.90264972028736,7.814710439787223 46.947573655919015,7.8146626177331475 46.94757285941649,7.796352728158914 47.75885454625439,7.796401702235196 47.75885537254857,7.79537544066238 47.802156579589926,7.794354988891075 47.84737115234881,7.794303851784195 47.847370288362285,7.772994464211592 48.74648044472784,6.280641504771698 48.7209148694678,6.28153026004501 48.70398989067294,5.852395315191537 48.71690691814323,5.852438708473069 48.71769475117851,5.785628476048435 48.718916610212496,5.718815262801493 48.720927698149374,5.7187738701218445 48.72013928077133,4.493246837277693 48.742552333714336,4.493269475577204 48.7433372246506,4.426775055184787 48.74376800299893,4.360279246283665 48.74498411169783,4.360258615969194 48.74419892399428,2.999727904106206 48.7530130044756,2.9997326349939772 47.852940629928966,1.662865290445675 47.84591482730969,1.6865105890409362 46.9012405527728))";

	polygon = polygon.replace("POLYGON ((", "");
	polygon = polygon.replace("))", "");

	Coordinate[] polygonCoordinates = Arrays.asList(polygon.split(",")).//
		stream().//
		map(p -> new CoordinateXY(//

			Double.valueOf(format.format(Double.valueOf(p.split(" ")[0]))), //
			Double.valueOf(format.format(Double.valueOf(p.split(" ")[1])))))
		.//

		collect(Collectors.toList()).//
		toArray(new Coordinate[] {});

	LinearRing polygonRing = factory.createLinearRing(polygonCoordinates);

	// SpatialExtent spatialExtent1 = new SpatialExtent(45.8957, 1.6629, 48.753, 7.836);
	//
	// CoordinateXY[] bboxCoordinates1 = new CoordinateXY[] { //
	//
	// new CoordinateXY(spatialExtent1.getWest(), spatialExtent1.getNorth()), //
	// new CoordinateXY(spatialExtent1.getEast(), spatialExtent1.getNorth()), //
	// new CoordinateXY(spatialExtent1.getEast(), spatialExtent1.getSouth()), //
	// new CoordinateXY(spatialExtent1.getWest(), spatialExtent1.getSouth()), //
	// new CoordinateXY(spatialExtent1.getWest(), spatialExtent1.getNorth()),//
	// };
	//
	// LinearRing polygonRing = factory.createLinearRing(bboxCoordinates1);

	//
	//
	//

	// west, south, east, north
	// "4.834 44.558 5.834 45.558";
	String bbox = "1.6629 45.8957 7.836 48.753";

	SpatialExtent spatialExtent = new SpatialExtent(45.895, 1.662, 48.753, 7.836);

	// minx, maxx, miny, maxy
	// Env[1.6629 : 7.836, 45.8957 : 48.753]
	// Env[1.6628 : 7.835, 45.8957 : 48.753]

	CoordinateXY[] bboxCoordinates = new CoordinateXY[] { //

		new CoordinateXY(spatialExtent.getWest(), spatialExtent.getNorth()), //
		new CoordinateXY(spatialExtent.getEast(), spatialExtent.getNorth()), //
		new CoordinateXY(spatialExtent.getEast(), spatialExtent.getSouth()), //
		new CoordinateXY(spatialExtent.getWest(), spatialExtent.getSouth()), //
		new CoordinateXY(spatialExtent.getWest(), spatialExtent.getNorth()),//
	};

	LinearRing bboxRing = factory.createLinearRing(bboxCoordinates);

	//
	//
	//

	Polygon polygonPolygin = factory.createPolygon(polygonRing);
	Polygon bboxPolygon = factory.createPolygon(bboxRing);

	//
	//
	//

	double polygonArea = polygonPolygin.getArea();
	double bboxArea = bboxPolygon.getArea();

	System.out.println(polygonArea);
	System.out.println(bboxArea);

	double covering = (polygonArea / bboxArea) * 100;

	System.out.println(covering);

	//
	//
	//

	boolean contains = bboxPolygon.contains(polygonPolygin);

	//
	// minx , maxx , miny, maxy
	//
	Envelope bboxEnvelope = bboxPolygon.getEnvelopeInternal();
	Envelope polygonEnvelope = polygonPolygin.getEnvelopeInternal();

	// Env[1.662 : 7.836, 45.895 : 48.753]
	// Env[1.663 : 7.836, 45.896 : 48.753]

	System.out.println(contains);

	//
	//
	//

	/// polygon
	Envelope envelope1 = new Envelope(5.463, 11.711, 43.201, 46.054);

	// partition
	Envelope envelope2 = new Envelope(6.245, 7.245, 44.043, 45.043);

	boolean contains2 = envelope1.contains(envelope2);

	System.out.println(contains2);

    }

}
