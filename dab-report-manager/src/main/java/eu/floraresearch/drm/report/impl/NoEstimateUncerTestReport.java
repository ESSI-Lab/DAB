package eu.floraresearch.drm.report.impl;


public class NoEstimateUncerTestReport extends DefaultReport {

    @Override
    public int getCategory_3_Value() {

	return 0;
    }
    
    @Override
    public int getCategory_2_Value() {

	return 0;
    }
    
    @Override
    public int getCategory_1_Value() {

	return 0;
    }
    
    @Override
    public int getCategory_4_Value() {

	return 0;
    }
    
    @Override
    public String getComments() {

	return "This source is under testing and no estimate is presently available";
    }
    
}
