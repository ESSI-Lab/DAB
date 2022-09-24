/**
 * 
 */
package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 */
public class OBISStaticReport extends DefaultReport {

    /**
     * 
     */
    public OBISStaticReport() {
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readOBISStaticGranules(); 
    }
    
    @Override
    public String getComments() {

	return "The number of granules is obtained by issuing a request with no constraint to each collection, and summing the total number of records";
    }

}
