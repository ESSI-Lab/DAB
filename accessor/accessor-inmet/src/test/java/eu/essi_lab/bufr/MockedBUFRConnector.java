package eu.essi_lab.bufr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import eu.essi_lab.bufr.datamodel.BUFRCollection;
import eu.essi_lab.lib.utils.GSLoggerFactory;

public class MockedBUFRConnector extends BUFRConnector {

    @Override
    public void getBUFRFiles() {
	try {
	    String sourceURL = getSourceURL();
	    String country = sourceURL.substring(sourceURL.lastIndexOf('/') + 1);
	    GSLoggerFactory.getLogger(getClass()).trace("Get INMET BUFR files STARTED");
	    localFolder = MockedBUFRDownloader.download();
	    Map<String, BUFRCollection> c = aggregateRecordsByStation(localFolder);
	    collections.put(country, c);
	    stationIdentifiers = new ArrayList<String>(c.keySet());
	    GSLoggerFactory.getLogger(getClass()).trace("Get INMET BUFR files ENDED");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
