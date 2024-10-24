package eu.essi_lab.shared.driver.es.stats;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.TotalHits;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.geometry.Geometry;
import org.opensearch.geometry.Rectangle;
import org.opensearch.index.query.AbstractQueryBuilder;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.GeoShapeQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

public class ElasticSearchClientTest {
    public static void main(String[] args) throws Exception {
	ElasticsearchClient elasticClient = new ElasticsearchClient("https://country-test.es.us-central1.gcp.cloud.es.io:9243", "elastic",
		"1xV8rxtnTHnIHJOhALmXp6DK");

	RestHighLevelClient client = elasticClient.getClient();

	double max = 10;
	double averageSearch = 0;
	double averageCount = 0;
	for (int i = 0; i < max; i++) {
	    long took = System.currentTimeMillis();

	    CountRequest countRequest = new CountRequest("newgeoss");

	    SearchRequest searchRequest = new SearchRequest("newgeoss");
	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

	    AbstractQueryBuilder<?> query = QueryBuilders.boolQuery();

	    // keywords
	    ((BoolQueryBuilder) query).must(QueryBuilders.matchPhraseQuery("keyword_value", "temperature"));

	    // bbox
	    double south = -90;
	    double west = -180;
	    double north = south+Math.random() * 180;
	    double east = west+Math.random() * 360;
	    Geometry geometry = new Rectangle(west, east, north, south);
	    GeoShapeQueryBuilder bboxQuery = QueryBuilders.geoWithinQuery("shape", geometry);
	    ((BoolQueryBuilder) query).must(bboxQuery);

	    // temporal extent
	    RangeQueryBuilder timeQueryStart = QueryBuilders.rangeQuery("normalized_time_start").gt("1950-01-01T00:00:00Z");
	    ((BoolQueryBuilder) query).must(timeQueryStart);

	    sourceBuilder.query(query);
	    sourceBuilder.from(0);
	    sourceBuilder.size(10);
	    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

	    countRequest.query(query);

	    searchRequest.source(sourceBuilder);

	    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	    SearchHits searchHits = searchResponse.getHits();

	    SearchHit[] hits = searchHits.getHits();
	    for (SearchHit hit : hits) {
		Map<String, Object> map = hit.getSourceAsMap();
		System.out.println(map.get("title") + " " + map.get("sourceId"));
		// Set<Entry<String, Object>> entries = map.entrySet();
		// List<Entry<String, Object>> sorted = new ArrayList<>(entries);
		// sorted.sort(new Comparator<Entry<String, Object>>() {
		//
		// @Override
		// public int compare(Entry<String, Object> o1, Entry<String, Object> o2) {
		// return o1.getKey().compareTo(o2.getKey());
		// }
		// });
		// for (Entry<String, Object> entry : sorted) {
		// System.out.print(entry.getKey()+": "+entry.getValue().toString()+" ");
		// }
		// System.out.println();
	    }

	    TotalHits totalHits = searchHits.getTotalHits();

	    took = System.currentTimeMillis() - took;
	    averageSearch += took / max;

	    System.out.println("Total hits: " + totalHits.value + " took " + took + " ms");

	    took = System.currentTimeMillis();

	    CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
	    long count = countResponse.getCount();

	    took = System.currentTimeMillis() - took;
	    averageCount += took / max;

	    System.out.println("Count: " + count + " took " + took + " ms");

	}
	System.out.println("Average search: " + averageSearch + " ms");
	System.out.println("Average count: " + averageCount + " ms");

	elasticClient.close();

    }
}
