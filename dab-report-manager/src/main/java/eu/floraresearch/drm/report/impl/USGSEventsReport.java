package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class USGSEventsReport extends DefaultReport {

    @Override
    public int getCategory_1_Value() throws Exception {

	return ConfigReader.getInstance().readUSGSStaticRecords();
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readUSGSStaticRecords();
    }

    public String getComments() {
 
	return "In the last report, the number of records and elements was related to events occurred from 1900-01-01. The current API implementation do not allows a request with a temporal offset so wide, so we left the number of granules provided in the last report. For performance reasons, queries submitted to the service from the GEOSS Web Portal (unless otherwise specified by the user) include only events of the last 30 days";
    }
}
