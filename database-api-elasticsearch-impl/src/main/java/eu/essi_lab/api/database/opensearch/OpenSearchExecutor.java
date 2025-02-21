/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.ArrayList;

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

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.LatLonGeoLocation;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.TopLeftBottomRightGeoBounds;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.FiltersAggregation;
import org.opensearch.client.opensearch._types.aggregations.FiltersBucket;
import org.opensearch.client.opensearch._types.aggregations.GeoCentroidAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.GeoBoundingBoxQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OpenSearchExecutor implements DatabaseExecutor {

    private Database database;
    private OpenSearchClient client;
    private OpenSearchWrapper wrapper;
    private OpenSearchFinder finder;

    @Override
    public boolean supports(StorageInfo dbUri) {
	return OpenSearchDatabase.isSupported(dbUri);
    }

    @Override
    public void setDatabase(Database database) {
	this.database = database;
	if (database instanceof OpenSearchDatabase) {
	    OpenSearchDatabase osd = (OpenSearchDatabase) database;
	    this.client = osd.getClient();
	    this.wrapper = new OpenSearchWrapper(client);
	    this.finder = new OpenSearchFinder();
	    finder.setDatabase(database);
	}

    }

    @Override
    public Database getDatabase() {

	return database;
    }

    @Override
    public void clearDeletedRecords() throws GSException {

    }

    @Override
    public int countDeletedRecords() throws GSException {

	return 0;
    }

    @Override
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	return null;
    }

    @Override
    public List<String> retrieveEiffelIds(DiscoveryMessage message, int start, int count) throws GSException {

	return null;
    }

    @Override
    public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled) throws GSException {

	return null;
    }

    @Override
    public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count) throws GSException {

	return null;
    }

    @Override
    public List<WMSClusterResponse> execute(WMSClusterRequest request) throws GSException {

	// GET data-folder-index/_search
	// {
	// "size": 0,
	// "query": {
	// "bool": {
	// "must": [
	// {
	// "range": {
	// "tmpExtentBegin_date": {
	// "gte": "2024-01-01T00:00:00",
	// "lte": "2024-12-31T23:59:59"
	// }
	// }
	// }
	// ]
	// }
	// },
	// "aggs": {
	// "regions": {
	// "filters": {
	// "filters": {
	// "region_1": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 90.0, "lon": -180.0 }, "bottom_right": {
	// "lat": 0.0, "lon": 180.0 } } } },
	// "region_2": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 0.0, "lon": -180.0 }, "bottom_right": {
	// "lat": -90.0, "lon": 180.0 } } } }
	// }
	// },
	// "aggs": {
	// "record_count": { "value_count": { "field": "_id" } },
	// "top_providers": {
	// "terms": {
	// "field": "sourceId_keyword",
	// "size": 5
	// }
	// },
	// "centroid": {
	// "geo_centroid": { "field": "centroid" }
	// }
	//
	//
	// }
	// }
	// }
	// }
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<Bond>bonds = new ArrayList<Bond>();
//	bonds.add(request.getConstraints());
//	bonds.add(BondFactory.)
//	message.setUserBond();
//	message.setView(request.getView());
//	Query query = finder.buildQuery(message, false);
//
//	// Bounding box queries for regions
//	List<SpatialExtent> extents = request.getExtents();
//	List<Entry<String, Query>> entries = new ArrayList<Map.Entry<String, Query>>();
//	for (int i = 0; i < extents.size(); i++) {
//	    SpatialExtent extent = extents.get(i);
//	    Query regionQuery = Query.of(q -> q.geoBoundingBox(g -> g.field("bbox")//
//		    .boundingBox(bb -> bb.tlbr(new TopLeftBottomRightGeoBounds.Builder()//
//			    .topLeft(tl -> tl.latlon(ll -> ll.lat(extent.getNorth()).lon(extent.getWest())))//
//			    .bottomRight(br -> br.latlon(ll -> ll.lat(extent.getSouth()).lon(extent.getEast()))).build()))));
//	    SimpleEntry<String, Query> entry = new SimpleEntry<String, Query>("region" + i, regionQuery);
//	    entries.add(entry);
//	}
//
//	Buckets<Query> regionBuckets = Buckets.of(b -> b.keyed(Map.ofEntries(entries.toArray(new Entry[] {}))));
//	// Filters aggregation for regions
//	Aggregation regionsAggregation = Aggregation.of(a -> a.filters(f -> f.filters(regionBuckets) //
//	).aggregations("record_count", ag -> ag.valueCount(vc -> vc.field("_id")))//
//		.aggregations("top_providers", ag -> ag.terms(t -> t.field("sourceId_keyword").size(request.getMaxTermFrequencyItems())))//
//		.aggregations("centroid", ag -> ag.geoCentroid(gc -> gc.field("centroid"))));
//
//	// Build the search request
//	SearchRequest searchRequest = SearchRequest//
//		.of(s -> s.index(DataFolderMapping.get().getIndex()).size(0).query(query).aggregations("regions", regionsAggregation));
//
//	// Execute search request
//	try {
//	    SearchResponse<Void> response = client.search(searchRequest, Void.class);
//
//	    Map<String, Aggregate> aggregations = response.aggregations();
//	    Aggregate regionsAgg = aggregations.get("regions");
//
//	    if (regionsAgg == null || regionsAgg.isFilters() == false) {
//		System.out.println("No region aggregation found!");
//		GSLoggerFactory.getLogger(getClass()).error("No region aggregation found!");
//	    }
//
//	    // Extract buckets (regions)
//	    List<FiltersBucket> responseRegionBuckets = regionsAgg.filters().buckets().array();
//	    List<WMSClusterResponse> ret = new ArrayList<DatabaseExecutor.WMSClusterResponse>();
//
//	    for (FiltersBucket regionBucket : responseRegionBuckets) {
//		WMSClusterResponse regionResponse = new WMSClusterResponse();
//		String regionName = regionBucket.toString(); // e.g., "region_1" or "region_2"
//		long recordCount = regionBucket.docCount(); // Total records in the region
//		regionResponse.setTotalCount((int) recordCount);
//		regionResponse.setStationsCount((int) recordCount);
//		
//		System.out.println("Region: " + regionName);
//		System.out.println("  Record Count: " + recordCount);
//
//		// Get top providers aggregation
//		Aggregate topProvidersAgg = regionBucket.aggregations().get("top_providers");
//		if (topProvidersAgg != null && topProvidersAgg.isSterms()) {
//		    List<StringTermsBucket> topProviders = topProvidersAgg.sterms().buckets().array();
//		    System.out.println("  Top Providers:");
//		    for (StringTermsBucket providerBucket : topProviders) {
//			System.out.println("    " + providerBucket.key() + ": " + providerBucket.docCount());
//		    }
//		    
//		}
//
//		// Get centroid aggregation
//		Aggregate centroidAgg = regionBucket.aggregations().get("centroid");
//		if (centroidAgg != null && centroidAgg.isGeoCentroid()) {
//		    GeoCentroidAggregate centroid = centroidAgg.geoCentroid();
//		    if (centroid.location() != null) {
//			SpatialExtent avgBbox = new SpatialExtent(centroid.location().latlon().lat(), centroid.location().latlon().lon(),
//				centroid.location().latlon().lat(), centroid.location().latlon().lon());
//			regionResponse.setAvgBbox(avgBbox);
//		    } else {
//			GSLoggerFactory.getLogger(getClass()).error("no centroid available");
//		    }
//
//		}
//
//		ret.add(regionResponse);
//	    }
//
//	    return ret;
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    GSLoggerFactory.getLogger(getClass()).error(e);
//	}
	//
	return null;

    }

}
