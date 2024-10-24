package eu.essi_lab.accessor.ana;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BasinsAndStationsTest {
	
	@Test
	public void testMethods() throws Exception {
		InputStream stationsList = BasinDocumentTest.class.getClassLoader().getResourceAsStream("stationList.xml");
		StationListDocument station = new StationListDocument(stationsList);
		
		InputStream basinsIs = BasinDocumentTest.class.getClassLoader().getResourceAsStream("basin.xml");
		BasinDocument basins = new BasinDocument(basinsIs);
		
		LinkedHashSet<String> basinIds = basins.getBasinIdentifiers();
		Assert.assertTrue(basinIds.size() == 9);
		for(String s: basinIds) {
			LinkedHashSet<String> subBasin = basins.getSubBasinIdentifiers(s);
			//System.out.println("Number of SubBasin for identifier: " + s + " . SIZE=" + subBasin.size());
			for(String sub: subBasin) {
				List<StationDocument> res = station.getStations(s, sub);
				if(res == null) {
					System.out.println("NULL");
					continue;
				}
				//System.out.println("Number of Station for identifier: " + s + " . SUBBASIN=" + sub + " . SIZE=" + res.size());
			}
		}		
		
	}


}
