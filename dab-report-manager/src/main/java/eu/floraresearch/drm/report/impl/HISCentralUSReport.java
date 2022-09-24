package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class HISCentralUSReport extends DefaultReport {

//    @Override
//    public int getCategory_1_Value() throws Exception {
//
//	if (ConfigReader.getInstance().readViewIdentifier() != null) {
//
//	    return 0;
//	}
//
//	return 13229445;
//    }
//
//    @Override
//    public int getCategory_2_Value() throws Exception {
//
//	return 0;
//    }
//
//    @Override
//    public int getCategory_3_Value() throws Exception {
//
//	return getCategory_1_Value();
//    }
//
//    @Override
//    public String getComments() {
//
//	if (ConfigReader.getInstance().readViewIdentifier() != null) {
//
//	    return "No records are available because the view constraints are not supported by the service";
//	}
//
//	return "This number is estimated, in fact the HIS service does not provide this information (number of all available resources). "
//		+ "See \"Number of Results\" section at http://essi-lab.eu/do/view/GIcat/HYDRODetails";
//    }
    
    @Override
    public int getCategory_1_Value() throws Exception {

	return ConfigReader.getInstance().readHISCentralStaticGranules();
    }

    @Override
    public int getCategory_2_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readHISCentralStaticGranules();
    }

    @Override
    public String getComments() {

	return "This number is estimated, in fact the HIS service does not provide this information (number of all available resources). "
		+ "See \"Number of Results\" section at http://essi-lab.eu/do/view/GIcat/HYDRODetails";
    }
}
