/**
 * 
 */
package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 *
 */
public class SentinelStaticReport extends DefaultReport{

    /**
     * 
     */
    public SentinelStaticReport() {
    }
  
    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readSentinelStaticGranules();
    }

    @Override
    public int getCategory_4_Value() throws Exception {

	return ConfigReader.getInstance().readSentinelStaticGranules();
    }
}
