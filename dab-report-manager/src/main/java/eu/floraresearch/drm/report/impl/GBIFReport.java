package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class GBIFReport extends DefaultReport {

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readGBIFStaticGranules();
    }

    @Override
    public String getComments() {

	return "Number of species discoverable. For each species, it is then possible to find related occurrencies";
//	return "The number of granules is obtained by issuing a request with no constraint to each collection, and summing the total number of records";
    }

}
