/**
 * 
 */
package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 *
 */
public class Landsat8StaticReport extends DefaultReport{

    /**
     * 
     */
    public Landsat8StaticReport() {
    }
    
    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readLandsat8StaticGranules();
    }
    
    @Override
    public int getCategory_4_Value() throws Exception {

	return ConfigReader.getInstance().readLandsat8StaticGranules();
    }
}
