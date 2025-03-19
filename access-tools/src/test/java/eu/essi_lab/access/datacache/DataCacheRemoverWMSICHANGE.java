package eu.essi_lab.access.datacache;

import java.net.URL;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.access.datacache.opensearch.OpenSearchConnector;

public class DataCacheRemoverWMSICHANGE {

    public static void main(String[] args) throws Exception {
	DataCacheConnector connector;

	String dbname = "datacache1";
	// String sourceIdentifier = "acronet";
	// String theme = "i-change";

	// connector= DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_DOCKERHUB_1_3,
	// new URL("http://localhost:9200"), "admin", "admin", dbname);
	connector = DataCacheConnectorFactory.newDataCacheConnector(DataConnectorType.OPEN_SEARCH_AWS_1_3,
		new URL(System.getProperty("dataCacheHost")), System.getProperty("dataCacheUser"), System.getProperty("dataCachePassword"),
		dbname);

	connector.configure(OpenSearchConnector.FLUSH_INTERVAL_MS, "1000");
	connector.configure(OpenSearchConnector.MAX_BULK_SIZE, "1000");
	connector.configure(OpenSearchConnector.CACHED_DAYS, "0");

	connector.deleteStation("ED1D342AC8B20375DA9CD0D19BB7EEAF3E358374");
	connector.deleteStation("439803A65293F4C708AE441FEF3608ED6FDFFB6F");
//	connector.deleteStations(sourceIdentifier, theme);
//	connector.clearStations(); // this deletes all the indeX!!
	
	Thread.sleep(2000);
	connector.close();
    }

}
