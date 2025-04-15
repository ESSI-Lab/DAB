//package eu.essi_lab.access.datacache;
//
//import java.net.URL;
//
//import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
//import eu.essi_lab.access.datacache.opensearch.OpenSearchConnector;
//
//public class DataCacheRemoverWMSICHANGE {
//
//    public static void main(String[] args) throws Exception {
//	DataCacheConnector connector;
//
//	String dbname = "datacache1";
//	// String sourceIdentifier = "acronet";
//	// String theme = "i-change";
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
//	connector.deleteStation("0AFC342750AB0A1EB4AB6EE1859F45D29ADC4641");
//	connector.deleteStation("7A46E8629E1E37C8DB6BDEFB1C06C1C55C6060A1");
////	connector.deleteStations(sourceIdentifier, theme);
////	connector.clearStations(); // this deletes all the indeX!!
//	
//	Thread.sleep(2000);
//	connector.close();
//    }
//
//}
