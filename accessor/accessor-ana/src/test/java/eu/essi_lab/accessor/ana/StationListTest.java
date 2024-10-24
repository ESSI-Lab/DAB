package eu.essi_lab.accessor.ana;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StationListTest {
	
	@Test
	public void testBasinDocumentMethods() throws Exception {
		InputStream is = BasinDocumentTest.class.getClassLoader().getResourceAsStream("stationList.xml");
		StationListDocument doc = new StationListDocument(is);
		
		LinkedHashSet<String> identifiers = doc.listIdentifiers();
		int size = identifiers.size();
		Assert.assertTrue(size > 0);
		
		
		StationDocument stationDoc = doc.getStation("00161002");
		
		String altitude = stationDoc.getAltitude();
		String basin = stationDoc.getBasin();
		String city = stationDoc.getCity();
		String lat = stationDoc.getLatitude();
		String lon = stationDoc.getLongitude();
		String riverCode = stationDoc.getRiverCode();
		String riverName = stationDoc.getRiverName();
		String stationCode = stationDoc.getStationCode();
		String stationName = stationDoc.getStationName();
		String origin = stationDoc.getOrigin();
		String subbasin = stationDoc.getSubBasin();
		
		
		Assert.assertEquals("0.00", altitude);
		Assert.assertEquals("1", basin);
		Assert.assertEquals("BARCELOS-AM", city);
		Assert.assertEquals("-1.45890", lat);
		Assert.assertEquals("-61.63310", lon);
		Assert.assertEquals("0", riverCode);
		Assert.assertEquals("", riverName);
		Assert.assertEquals("00161002", stationCode);
		Assert.assertEquals("MOURA", stationName);
		Assert.assertEquals("RHN", origin);
		Assert.assertEquals("14", subbasin);
		
		List<StationDocument> listStations = doc.getStations("1", "14");
		
		
		int sizeList = listStations.size();
		Assert.assertTrue(sizeList > 0);
		
		
	}


}
