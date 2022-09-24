package eu.floraresearch.drm.report.impl;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import eu.essi_lab.model.GSSource;
import eu.floraresearch.drm.report.Report;

public class FakeReport implements Report {

    private int total;
    private int gdc;
    private Random rg;

    private static final int MIN = 1_000_000;
    private static final int MAX = 10_000_000;

    public FakeReport() {

	init(null, null);
    }

    @Override
    public void init(String sourceID, List<GSSource> list) {

	rg = new Random();
	gdc = rg.nextInt(MAX - MIN + 1) + MIN;
	total = gdc + 10_000_000;
    }

    @Override
    public String getShortName() {

	return UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public int getCategory_2_Value() throws Exception {

	return gdc;
    }

    @Override
    public int getCategory_1_Value() throws Exception {

	return total;
    }

    @Override
    public int getCategory_3_Value() throws Exception {

	return total;
    }

    @Override
    public int getCategory_4_Value() throws Exception {

	return gdc;
    }

    @Override
    public String getCompleteName() {

	return UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public String getComments() {

	return UUID.randomUUID().toString().substring(0, 5);
    }

    @Override
    public String getCategory_5_Value() throws Exception {

	return "";
    }

    @Override
    public int getCategory_6_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_7_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_8_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_9_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_10_Value() throws Exception {

	return 0;
    }

    @Override
    public int getCategory_11_Value() throws Exception {

	return 0;
    }

    @Override
    public int[] refineRecordsCount() {

	return new int[] { total, gdc };
    }

    @Override
    public boolean underTest() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void setUnderTest(boolean value) {
	// TODO Auto-generated method stub

    }

    @Override
    public int getCategory_12_Value() throws Exception {
	// TODO Auto-generated method stub
	return 0;
    }

}
