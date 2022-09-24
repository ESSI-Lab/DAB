package eu.floraresearch.drm.report.impl.legacy;

import eu.floraresearch.drm.ConfigReader;
import eu.floraresearch.drm.report.impl.DefaultReport;

public class RASAQMStaticReport extends DefaultReport{
    
    @Override
    public int getCategory_1_Value() throws Exception {

	return ConfigReader.getInstance().readRASAQMStaticTotal();
    }
    
    @Override
    public int getCategory_2_Value() throws Exception {

	return ConfigReader.getInstance().readRASAQMStaticTotal();
    }
}
