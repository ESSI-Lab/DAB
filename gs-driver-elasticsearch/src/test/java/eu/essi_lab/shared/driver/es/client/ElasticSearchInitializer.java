package eu.essi_lab.shared.driver.es.client;

import java.io.IOException;

import eu.essi_lab.shared.driver.es.stats.ElasticsearchClient;

public class ElasticSearchInitializer {
    public static void main(String[] args) throws IOException {
	ElasticsearchClient client = new ElasticsearchClient("https://hiscentral-test.cloud.cnaf.infn.it/opensearch/", "", "");
	client.setDbName("dab");
	client.init("request");
    }
}
