package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class SeaDataNetReport extends DefaultReport {

    @Override
    public int getCategory_3_Value() {

	return ConfigReader.getInstance().readSeaDataNetStaticGranules();
    }
    
    @Override
    public String getComments() {

	return "The estimate of Granules is from http://www.seadatanet.org/content/download/17887/116313/file/ESSI2-5-SDN2-EMODNET-Bathymetry-SchaapApril2013.pdf";
    }
}
