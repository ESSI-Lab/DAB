//package eu.essi_lab.access.datacache;
//
//import java.net.URL;
//
//import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
//import eu.essi_lab.access.datacache.opensearch.OpenSearchConnector;
//
//public class DataCacheRemover {
//
//    public static void main(String[] args) throws Exception {
//	DataCacheConnector connector;
//
//	String dbname = "datacache1";
//	String sourceId = "argentina-ina";
//
//	// connector= DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_DOCKERHUB_1_3,
//	// new URL("http://localhost:9200"), "admin", "admin", dbname);
//	connector = DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_AWS_1_3,
//		new URL(System.getProperty("dataCacheHost")), System.getProperty("dataCacheUser"), System.getProperty("dataCachePassword"),
//		dbname);
//
//	connector.configure(OpenSearchConnector.FLUSH_INTERVAL_MS, "1000");
//	connector.configure(OpenSearchConnector.MAX_BULK_SIZE, "1000");
//	connector.configure(OpenSearchConnector.CACHED_DAYS, "0");
//
//	 connector.deleteBefore(null, sourceId);
//	 connector.deleteStations(sourceId);
//	 while ((long) connector.count() != 0) {
//	 Thread.sleep(1000);
//	
//	 }
////	connector.deleteStations(null);
//	
//	Thread.sleep(2000);
//	connector.close();
//    }
//
//}
