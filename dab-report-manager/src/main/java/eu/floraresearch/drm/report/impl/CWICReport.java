package eu.floraresearch.drm.report.impl;

import eu.floraresearch.drm.ConfigReader;

public class CWICReport extends DefaultReport {

//    @Override
//    public int getCategory_2_Value() throws Exception {
//
//	return getCategory_1_Value();
//    }
//
//    @Override
//    public int getCategory_3_Value() throws Exception {
//
//	if (ConfigReader.getInstance().readViewIdentifier() != null) {
//
//	    return (int) ((double) 280000000 / 4325) * getCategory_1_Value();
//	}
//
//	return ConfigReader.getInstance().readCWICStaticGranules();
//    }
//
//    @Override
//    public int getCategory_4_Value() throws Exception {
//
//	if (ConfigReader.getInstance().readViewIdentifier() != null) {
//
//	    return (int) ((double) 280000000 / 4325) * getCategory_1_Value();
//	}
//
//	return ConfigReader.getInstance().readCWICStaticGranules();
//    }
//
//    @Override
//    public String getComments() {
//
//	return "Declared by Provider";
//    }
    
    @Override
    public int getCategory_3_Value() throws Exception {

	return ConfigReader.getInstance().readCWICStaticGranules();
    }
    
    @Override
    public int getCategory_4_Value() throws Exception {

	return ConfigReader.getInstance().readCWICStaticGranules();
    }

    @Override
    public String getComments() {

	return "Declared by Provider";
    }
}
