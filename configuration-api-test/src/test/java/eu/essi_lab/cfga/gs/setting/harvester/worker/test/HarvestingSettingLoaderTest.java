package eu.essi_lab.cfga.gs.setting.harvester.worker.test;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;

/**
 * @author Fabrizio
 */
public class HarvestingSettingLoaderTest {

    /**
     * 
     */
    @Test
    public void loadTest() {

	HarvestingSetting setting1 = HarvestingSettingLoader.load();
	HarvestingSetting setting2 = HarvestingSettingLoader.load();

	//
	// they have the same content, but different identifiers
	//
	Assert.assertNotEquals(setting1, setting2);
	Assert.assertNotEquals(setting1.getIdentifier(), setting2.getIdentifier());

	//
	//
	//
	setting1.setIdentifier("id");
	setting2.setIdentifier("id");

	Assert.assertEquals(setting1, setting2);
	Assert.assertEquals(setting1.getIdentifier(), setting2.getIdentifier());
    }

    @Test
    public void loadWithContentTest() {

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("defaultOAISource");
	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceLabel("DAB OAIPMH Service");
	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting()
		.setSourceEndpoint("http://gs-service-production.geodab.eu/gs-service/services/oaipmh");
	
	//
	//
	//
	
	HarvestingSetting loadedSetting = HarvestingSettingLoader.load(oaiSetting.getObject());
	
	
	//
	// they have exactly the same content
	//
	Assert.assertEquals(oaiSetting, loadedSetting);
	Assert.assertEquals(oaiSetting.getIdentifier(), loadedSetting.getIdentifier());
	
	//
	// content are cloned
	//
	
	JSONObject oaiSettingObject = oaiSetting.getObject();
	JSONObject loadedSettingObject = loadedSetting.getObject();
	
	String id1 = oaiSettingObject.getString("settingId");
	String id2 = loadedSettingObject.getString("settingId");

	Assert.assertEquals(id1, id2);
	
	// changing to one object do not alter the other
	oaiSettingObject.put("settingId","id");
	Assert.assertEquals(oaiSettingObject.getString("settingId"), "id");

	Assert.assertEquals(id2, loadedSettingObject.getString("settingId"));	
    }
}
