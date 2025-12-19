package eu.essi_lab.shared.driver.es.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.core.rest.*;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

public class ESSearch {

    /**
     * @return
     */
    private static String getUser() {

	return System.getProperty("els.main.user");
    }

    /**
     * @return
     */
    private static String getPassword() {

	return System.getProperty("els.main.password");
    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	credentialsProvider.setCredentials(AuthScope.ANY, new org.apache.http.auth.UsernamePasswordCredentials(
		getUser(), getPassword()));

	RestHighLevelClient client = new RestHighLevelClient(
		RestClient.builder(new HttpHost("localhost", 9200, "http")).setHttpClientConfigCallback(new HttpClientConfigCallback() {
		    @Override
		    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
			return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		    }
		}));

	SearchRequest searchRequest = new SearchRequest("wof-discovery-message");
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	sourceBuilder.query(QueryBuilders.matchAllQuery());
	sourceBuilder.from(0);
	sourceBuilder.size(10);
	sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
	TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_keyword").field("DISCOVERY_MESSAGE_title.keyword");
	sourceBuilder.aggregation(aggregation);
	searchRequest.source(sourceBuilder);

	SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);

	RestStatus status = searchResponse.status();

	TimeValue took = searchResponse.getTook();
	System.out.println(status.toString() + " took " + took.toString());

	Aggregations aggregations = searchResponse.getAggregations();
	Aggregation aggregationResult = aggregations.asList().get(0);
	if (aggregationResult instanceof ParsedStringTerms) {
	    ParsedStringTerms pst = (ParsedStringTerms) aggregationResult;
	    List<? extends Bucket> buckets = pst.getBuckets();
	    for (Bucket bucket : buckets) {
		System.out.println(bucket.getKey() + " " + bucket.getDocCount());
	    }
	}
	client.close();
    }
}
