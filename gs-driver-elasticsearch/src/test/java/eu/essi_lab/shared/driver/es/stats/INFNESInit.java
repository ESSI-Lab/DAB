package eu.essi_lab.shared.driver.es.stats;

public class INFNESInit {

    public static void main(String[] args) throws Exception {
	ElasticsearchClient client = null;
	client = new ElasticsearchClient("http://localhost:9200","admin","admin");
	client.setDbName("dab");
	long c = client.count();
	System.out.println(c);

    }
}
