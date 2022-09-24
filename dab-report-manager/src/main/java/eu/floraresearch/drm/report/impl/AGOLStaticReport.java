/**
 * 
 */
package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 */
public class AGOLStaticReport extends DefaultReport {

    /**
     * 
     */
    public AGOLStaticReport() {
    }

    @Override
    public int getCategory_2_Value() throws Exception {

	return ConfigReader.getInstance().readAGOLStaticRecords();
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readAGOLStaticRecords();
    }

    @Override
    public String getComments() {

	return "Current API implementation do not allows to get more than 10000 records, so we left the number of granules provided in the last report (obtained using the old implementation of the API)";
    }

}
