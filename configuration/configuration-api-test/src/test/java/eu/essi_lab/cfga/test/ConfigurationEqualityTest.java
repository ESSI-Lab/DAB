package eu.essi_lab.cfga.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ConfigurationEqualityTest {

    @Test
    public void test(){
	
	Configuration config1 = new Configuration();
	
	Configuration config2 = new Configuration();
	
	Assert.assertEquals(config1,config2);
	
	//
	//
	//
	
	Setting setting = new Setting();
	
	config1.put(setting);
	
	Assert.assertNotEquals(config1,config2);
	
	//
	//
	//
	
	config2.put(setting);
	
	Assert.assertEquals(config1,config2);
	
	//
	//
	//
	
	setting.setName("name");
	config1.replace(setting);
	
	Assert.assertNotEquals(config1,config2);

	//
	//
	//
	
 	config2.replace(setting);
	
	Assert.assertEquals(config1,config2);    
	    
	//
	//
	//
	
	Setting setting2 = new Setting();
	config1.put(setting2);

	Setting setting3 = new Setting();
	config2.put(setting3);

	Assert.assertNotEquals(config1,config2);	
    }
}
