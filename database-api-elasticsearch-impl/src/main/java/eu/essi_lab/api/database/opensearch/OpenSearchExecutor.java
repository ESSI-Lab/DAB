/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.util.ArrayList;
import java.util.HashMap;

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
import java.util.Set;

import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.LatLonGeoLocation;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.TopLeftBottomRightGeoBounds;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.BucketSortAggregation;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.FiltersAggregation;
import org.opensearch.client.opensearch._types.aggregations.FiltersBucket;
import org.opensearch.client.opensearch._types.aggregations.GeoCentroidAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.query_dsl.GeoBoundingBoxQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMapType;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.index.IndexedResourceProperty;
import eu.essi_lab.model.index.jaxb.BoundingBox;
import eu.essi_lab.model.index.jaxb.CardinalValues;
import eu.essi_lab.model.index.jaxb.DisjointValues;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import jakarta.json.JsonObject;

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
	public JSONObject executePartitionsQuery(DiscoveryMessage message, boolean temporalConstraintEnabled)
			throws GSException {

		return null;
	}

	@Override
	public List<String> getIndexValues(DiscoveryMessage message, MetadataElement element, int start, int count)
			throws GSException {

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
		// "region_1": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 90.0,
		// "lon": -180.0 }, "bottom_right": {
		// "lat": 0.0, "lon": 180.0 } } } },
		// "region_2": { "geo_bounding_box": { "bbox": { "top_left": { "lat": 0.0,
		// "lon": -180.0 }, "bottom_right": {
		// "lat": -90.0, "lon": 180.0 } } } }
		// }
		// },
		// "aggs": {
		// "average_centroid": {
		// "geo_centroid": { "field": "centroid" }
		// },
		// "providers": {
		// "terms": {
		// "field": "sourceId_keyword",
		// "size": 10,
		// "order": { "unique_location_count": "desc" }
		// },
		// "aggs": {
		// "unique_location_count": {
		// "cardinality": { "field": "uniquePlatformId_keyword" }
		// },
		//
		// "top_5_filter": {
		// "bucket_sort": {
		// "sort": [{ "unique_location_count": { "order": "desc" } }],
		// "size": 5
		// }
		// }
		// }
		// }
		// }
		// }
		// }
		// }

		DiscoveryMessage message = new DiscoveryMessage();
		List<Bond> bonds = new ArrayList<Bond>();
		if (request.getConstraints() != null) {
			bonds.add(request.getConstraints());
		}
		if (request.getView() != null && request.getView().getBond() != null) {
//			message.setSources(ConfigurationWrapper.getViewSources(request.getView()));
			bonds.add(request.getView().getBond());
		}
		switch (bonds.size()) {
		case 0:
			// nothing to do
			break;
		case 1:
			message.setUserBond(bonds.get(0));
			message.setPermittedBond(bonds.get(0));
			break;
		default:
			message.setUserBond(BondFactory.createAndBond(bonds));
			message.setPermittedBond(BondFactory.createAndBond(bonds));
			break;
		}

		Query query = bonds.isEmpty() ? null : finder.buildQuery(message, true);

		// Bounding box queries for regions
		List<SpatialExtent> extents = request.getExtents();
		Map<String, SpatialExtent> extentMap = new HashMap<>();
		List<Entry<String, Query>> entries = new ArrayList<Map.Entry<String, Query>>();
		for (int i = 0; i < extents.size(); i++) {
			SpatialExtent extent = extents.get(i);
			String name = "region" + i;
			if (extent.getName() != null) {
				name = extent.getName();
			}
			extentMap.put(name, extent);
			Query regionQuery = Query.of(q -> q.geoBoundingBox(g -> g.field("bbox")//
					.boundingBox(bb -> bb.tlbr(new TopLeftBottomRightGeoBounds.Builder()//
							.topLeft(tl -> tl.latlon(ll -> ll.lat(extent.getNorth()).lon(extent.getWest())))//
							.bottomRight(br -> br.latlon(ll -> ll.lat(extent.getSouth()).lon(extent.getEast())))
							.build()))));
			SimpleEntry<String, Query> entry = new SimpleEntry<String, Query>(name, regionQuery);
			entries.add(entry);
		}

		Buckets<Query> regionBuckets = Buckets.of(b -> b.keyed(Map.ofEntries(entries.toArray(new Entry[] {}))));
		// Filters aggregation for regions
		Aggregation regionsAggregation = Aggregation.of(a -> a.filters(f -> f.filters(regionBuckets) //
		).aggregations(Map.of( //
				"average_centroid", Aggregation.of(gc -> gc//
						.geoCentroid(geo -> geo.field("centroid"))//
				), //
				"total_unique_location_count", Aggregation.of(c -> c//
						.cardinality(card -> card.field("uniquePlatformId_keyword"))//
				), //
				"providers", Aggregation.of(t -> t//
						.terms(term -> term//
								.field("sourceId_keyword")//
								.size(request.getMaxTermFrequencyItems())//
								.order(Map.of("unique_location_count", SortOrder.Desc))//
						)//
						.aggregations(Map.of(//
								"unique_location_count", Aggregation.of(c -> c//
										.cardinality(card -> card.field("uniquePlatformId_keyword"))//
								), //
								"top_5_filter", Aggregation.of(bs -> bs//
										.bucketSort(BucketSortAggregation.of(b -> b//
												.sort(List.of(//
														SortOptions.of(s -> s//
																.field(f -> f.field("unique_location_count")
																		.order(SortOrder.Desc))//
														)//
												))//
												.size(request.getMaxTermFrequencyItems())//
										)//
										)//
								))//
						)), //

				"distinct_location_samples", Aggregation.of(agg -> agg //
						.terms(t -> t //
								.field("uniquePlatformId_keyword") //
								.size(request.getMaxResults()) //
						) //
						.aggregations("sample_record", subAgg -> subAgg //
								.topHits(th -> th //
										.size(1) //
										.source(src -> src //
												.filter(flt -> flt.includes("uniquePlatformId", "sourceId", "centroid"))//
										)//
								)//
						)//

				)//

		)

		));

		// Build the search request
		SearchRequest searchRequest = SearchRequest//
				.of(s -> s.index(DataFolderMapping.get().getIndex()).size(0).query(query).aggregations("regions",
						regionsAggregation));

		// Execute search request
		try {

			SearchResponse<Void> response = client.search(searchRequest, Void.class);

			Map<String, Aggregate> aggregations = response.aggregations();
			Aggregate regionsAgg = aggregations.get("regions");

			if (regionsAgg == null || regionsAgg.isFilters() == false) {
				GSLoggerFactory.getLogger(getClass()).error("No region aggregation found!");
			}

			// Extract buckets (regions)
			Set<Entry<String, FiltersBucket>> responseRegionBuckets = regionsAgg.filters().buckets().keyed().entrySet();
			List<WMSClusterResponse> ret = new ArrayList<DatabaseExecutor.WMSClusterResponse>();

			for (Entry<String, FiltersBucket> entry : responseRegionBuckets) {
				String name = entry.getKey();
				SpatialExtent extent = extentMap.get(name);
				FiltersBucket region = entry.getValue();
				WMSClusterResponse regionResponse = new WMSClusterResponse();
				if (extent != null) {
					regionResponse.setBbox(extent);
				}
				// String regionName = regionBucket.toString(); // e.g., "region_1" or
				// "region_2"
				// long recordCount = regionBucket.docCount(); // Total records in the region
				// regionResponse.setTotalCount((int) recordCount);
				// regionResponse.setStationsCount((int) recordCount);

				// System.out.println("Region: " + regionName);
				// System.out.println(" Record Count: " + recordCount);

				GeoCentroidAggregate centroidAgg = region.aggregations().get("average_centroid").geoCentroid();
				if (centroidAgg != null && centroidAgg.location() != null) {
					double lat = centroidAgg.location().latlon().lat();
					double lon = centroidAgg.location().latlon().lon();
					SpatialExtent avgBbox = new SpatialExtent(lat, lon, lat, lon);
					regionResponse.setAvgBbox(avgBbox);
				}

				regionResponse.setTotalCount((int) region.docCount());
				Aggregate uniqueCountAgg = region.aggregations().get("total_unique_location_count");
				long totalStationCount = uniqueCountAgg.cardinality().value();
				regionResponse.setStationsCount((int) totalStationCount);
				// Get top providers aggregation
				if (totalStationCount < request.getMaxResults()) {
					StringTermsAggregate distinctSamplesAgg = region.aggregations().get("distinct_location_samples")
							.sterms();
					List<Dataset> datasets = new ArrayList<Dataset>();

					for (StringTermsBucket distinctBucket : distinctSamplesAgg.buckets().array()) {
						String uniqueLocationId = distinctBucket.key();
						List<Hit<JsonData>> sampleRecords = distinctBucket.aggregations().get("sample_record").topHits()
								.hits().hits();
						for (Hit<JsonData> record : sampleRecords) {
							JsonObject json = record.source().toJson().asJsonObject();
							String platformId = json.getJsonArray("uniquePlatformId").getString(0);
							String sourceId = json.getJsonArray("sourceId").getString(0);
							String centroid = json.getJsonString("centroid").toString();
							centroid = centroid.substring(centroid.indexOf("(") + 1).replace(")", "").replace("\"","").trim();
							String[] split = centroid.split(" ");
							String lat = split[1];
							String lon = split[0];
							Dataset dataset = new Dataset();
							dataset.getIndexesMetadata().write(new IndexedElement(
									MetadataElement.UNIQUE_PLATFORM_IDENTIFIER.getName(), platformId));
							dataset.getIndexesMetadata()
									.write(new IndexedResourceProperty(ResourceProperty.SOURCE_ID, sourceId));

							BoundingBox boxIndexMetadata = new BoundingBox();
							CardinalValues valuesMetadata = new CardinalValues();
							valuesMetadata.setEast(lon);
							valuesMetadata.setWest(lon);
							valuesMetadata.setNorth(lat);
							valuesMetadata.setSouth(lat);
							boxIndexMetadata.addCardinalValues(valuesMetadata);
							dataset.getIndexesMetadata().write(new IndexedMetadataElement(boxIndexMetadata) {
								@Override
								public void defineValues(GSResource resource) {
								}
							});

							dataset.getIndexesMetadata().write(
									new IndexedElement(MetadataElement.BOUNDING_BOX.getName(), uniqueLocationId));
							// String title = .t("title").asText();
							// String identifier = record.source().get("identifier").asText();
							datasets.add(dataset);

						}

					}
					if (!datasets.isEmpty()) {
						regionResponse.setDatasets(datasets);
						ret.add(regionResponse);
					}
				} else {
					Aggregate providersAgg = region.aggregations().get("providers");

					if (providersAgg != null && providersAgg.isSterms()) {
						List<StringTermsBucket> providers = providersAgg.sterms().buckets().array();
						TermFrequencyMapType mapType = new TermFrequencyMapType();
						for (StringTermsBucket provider : providers) {
							String sourceId = provider.key();
							long timeseriesCount = provider.docCount();
							long stationsCount = provider.aggregations().get("unique_location_count").cardinality()
									.value();
							TermFrequencyItem item = new TermFrequencyItem();
							item.setTerm(sourceId);
							item.setDecodedTerm(sourceId);
							item.setFreq((int) stationsCount);
							item.setLabel("source");
							mapType.getSourceId().add(item);
						}

						TermFrequencyMap tfm = new TermFrequencyMap(mapType);

						regionResponse.setMap(tfm);
					}
					ret.add(regionResponse);
				}
			}

			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			GSLoggerFactory.getLogger(getClass()).error(e);
		}
		//
		return null;

	}

}
