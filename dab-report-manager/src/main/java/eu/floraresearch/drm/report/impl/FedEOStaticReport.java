package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class FedEOStaticReport extends DefaultReport {

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readFedEOStaticGranules();
    }

    @Override
    public String getComments() {

	return "Current number of granules is takem from the previous report, the computation is in progress and will be set soon. The number of granules is obtained by issuing a request with no constraint to each collection, and summing the total number of records";
    }
}
