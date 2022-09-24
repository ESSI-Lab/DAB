/**
 * 
 */
package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 */
public class ChinaGeossStaticReport extends DefaultReport {

    /**
     * 
     */
    public ChinaGeossStaticReport() {
    }
   
    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readChinaGeossStaticGranules();
    }
    
    @Override
    public int getCategory_4_Value() throws Exception {

	return ConfigReader.getInstance().readChinaGeossStaticGranules();
    }
}
