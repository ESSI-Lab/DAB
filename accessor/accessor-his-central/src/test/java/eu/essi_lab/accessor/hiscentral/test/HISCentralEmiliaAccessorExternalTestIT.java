package eu.essi_lab.accessor.hiscentral.test;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaAccessor;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector.EMILIA_LEVEL;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnector.EMILIA_TIMERANGE;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaConnectorSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class HISCentralEmiliaAccessorExternalTestIT {

    /**
     * @throws GSException
     */
    @Test
    public void listMetadataFormatsTest() throws GSException {

	HISCentralEmiliaAccessor accessor = new HISCentralEmiliaAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("SIR_EMILIA", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(HISCentralEmiliaConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint(HISCentralEmiliaConnector.BASE_URL);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	HISCentralEmiliaConnector connector = accessor.getConnector();

	Assert.assertEquals(HISCentralEmiliaConnector.class, connector.getClass());

	HISCentralEmiliaConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	//
	//
	//

	List<String> listMetadataFormats = accessor.listMetadataFormats();
	listMetadataFormats.sort(String::compareTo);

	Assert.assertEquals(1, listMetadataFormats.size());

	Assert.assertEquals(CommonNameSpaceContext.HISCENTRAL_EMILIA_NS_URI, listMetadataFormats.get(0));
    }

    /**
     * @throws GSException
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void listRecordsTest() throws GSException, UnsupportedEncodingException, JAXBException {

	HISCentralEmiliaAccessor accessor = new HISCentralEmiliaAccessor();

	//
	// Accessor setting must be HARVESTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.HARVESTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("SIR_EMILIA", accessorSetting.getConfigurableType());

	//
	// The connector setting needs the source endpoint
	//

	HarvestedConnectorSetting harvestedConnectorSetting = accessorSetting.getHarvestedConnectorSetting();

	Assert.assertEquals(HISCentralEmiliaConnectorSetting.class, harvestedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();
	gsSourceSetting.setSourceEndpoint(HISCentralEmiliaConnector.BASE_URL);

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	HISCentralEmiliaConnector connector = accessor.getConnector();

	Assert.assertEquals(HISCentralEmiliaConnector.class, connector.getClass());

	HISCentralEmiliaConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(harvestedConnectorSetting, oaiConnectorSetting);

	oaiConnectorSetting.setMaxRecords(1); // max 1 records

	//
	//
	//

	Downloader d = new Downloader();
	Optional<String> result = d.downloadOptionalString(HISCentralEmiliaConnector.BASE_URL);
	
	Map<String, Integer> map = new HashMap<String, Integer>();
	Map<Integer, Integer> timeRangeMap = new HashMap<Integer, Integer>();
	Map<Integer, Integer> levelMap = new HashMap<Integer, Integer>();
	EMILIA_TIMERANGE[] tr_values = EMILIA_TIMERANGE.values();
	EMILIA_LEVEL[] lev_values = EMILIA_LEVEL.values();
	if(result.isPresent()) {
	    String lines[] = result.get().split("\\r?\\n");
	    for(int i = 0; i < lines.length;i++) {
		//System.out.println(i);
		JSONObject json = new JSONObject(lines[i]);
		//test if vars is always the first element for reading: LAT, LON, ALT, STATION NAME....
		JSONArray jArray = json.optJSONArray("data");
//		if(jArray != null && jArray.length() > 0) {
//		    JSONObject js = (JSONObject) jArray.get(0);
//		    assertTrue(js.has("vars"));
//		}
		String name = null;
		for(int k = 0; k < jArray.length() ; k++) {
		    String variable = null;
		    int timeRange = -9999;
		    int level = -9999;
		    if(k==0) {
			JSONObject js = (JSONObject) jArray.get(0);
			assertTrue(js.has("vars"));
			JSONObject jsonStation = js.getJSONObject("vars");
			name = jsonStation.optJSONObject("B01019").optString("v");
			continue;
		    }
			
		    
		    JSONObject obj = (JSONObject) jArray.get(k);
		    JSONObject propertiesObject = obj.optJSONObject("vars");
		    if(propertiesObject != null) {
			JSONArray names = propertiesObject.names();
			Iterator<String> it = propertiesObject.keys();
			while(it.hasNext()) {
			    variable = it.next();
			    if(map.containsKey(variable)) {
				map.put(variable, map.get(variable) + 1);
			    }else {
				map.put(variable, 1);
			    }
			}
		    }
		    JSONArray timeObj = obj.optJSONArray("timerange");
		    String interpolation;
		    if(timeObj != null) {
			timeRange = (Integer) timeObj.get(0);
			 for(EMILIA_TIMERANGE et: tr_values) {
				if(et.getCode() == timeRange) {
				    interpolation = et.getLabel();
				    break;
				}
			    }
			if(timeRangeMap.containsKey(timeRange)) {
			    timeRangeMap.put(timeRange, timeRangeMap.get(timeRange) + 1);
			} else {
			    timeRangeMap.put(timeRange, 1); 
			}
		    }
		    
		    JSONArray levelObj = obj.optJSONArray("level");
		    String lv;
		    if(levelObj != null) {
			level = (Integer) levelObj.get(0);
			for(EMILIA_LEVEL lev: lev_values) {
			    if(lev.getCode() == level) {
				lv = lev.getLabel();
			    }
			}
			if(levelMap.containsKey(level)) {
			    levelMap.put(level, levelMap.get(level) + 1);
			} else {
			    levelMap.put(level, 1); 
			}
		    }
		  
		    System.out.println(name+":" +variable + ":" + timeRange + ":" + level);
		}
		
		
	    }   
	    
	    System.out.println("VAR MAP");
	    
	    map.entrySet().forEach((entry) -> System.out.println(entry.getKey() + " : " + entry.getValue()));
	    
	    System.out.println("LEVEL MAP");
	    
	    levelMap.entrySet().forEach((entry) -> System.out.println(entry.getKey() + " : " + entry.getValue()));
	    
	    System.out.println("TIMERANGE MAP");
	    timeRangeMap.entrySet().forEach((entry) -> System.out.println(entry.getKey() + " : " + entry.getValue()));
	    
	    
	    
	}

    }
}
