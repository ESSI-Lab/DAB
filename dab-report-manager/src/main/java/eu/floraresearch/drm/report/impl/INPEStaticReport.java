package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class INPEStaticReport extends DefaultReport {
    
    @Override
    public int getCategory_2_Value() throws Exception {

	return getCategory_1_Value(); 
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readINPEStaticGranules(); 
    }

    @Override
    public int getCategory_4_Value() throws Exception {

	return ConfigReader.getInstance().readINPEStaticGranules(); 
    }

    /**
     * @return
     */
    @Override
    public String getComments() {

	return "The number of granules is obtained by issuing a request with no constraint to each collection, and summing the total number of records";
    }
}
