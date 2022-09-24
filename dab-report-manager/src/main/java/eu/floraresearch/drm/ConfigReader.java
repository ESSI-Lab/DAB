package eu.floraresearch.drm;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class ConfigReader {

    public static final String EXCLUDED = "EXCLUDED";
    public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

    private static final String CURRENT_CONFIG = "conf/current-config.properties";

    private static ConfigReader INSTANCE;
    private static final String UNDERTEST_KEY = "undertest";

    private Properties prop;

    public enum ReportsSorting {

	BY_NAME, //
	BY_TOTAL_COUNT_ASC, //
	BY_TOTAL_COUNT_DES//
    }

    public static ConfigReader getInstance() {

	if (INSTANCE == null) {
	    INSTANCE = new ConfigReader();
	}

	return INSTANCE;
    }

    private ConfigReader() {

	init();
    }

    private void init() {

	prop = new Properties();
	try {
	    prop.load(getClass().getClassLoader().getResourceAsStream(CURRENT_CONFIG));

	    String currentConf = prop.getProperty("current-config");
	    prop.load(getClass().getClassLoader().getResourceAsStream(currentConf));

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public String readAPIKey() {

	return prop.getProperty("apiKey", null);
    }

    public String readViewIdentifier() {

	return prop.getProperty("viewId", null);
    }

    public String readDABEndpoint() {

	String dabEndpoint = prop.getProperty("dabEndpoint", "http://admin:topsecret@localhost:8082/gi-cat/").trim();

	if (!dabEndpoint.endsWith("/")) {
	    dabEndpoint += "/";
	}
	return dabEndpoint;
    }

    public String readWindowTitle() {

	return prop.getProperty("windowTitle", "DAB-Report-").trim();
    }

    public String readGraphicTitle() {

	return prop.getProperty("graphTitle", "BROKERED CAPACITIES").trim();
    }

    public String readCategory_Label(int category) {

	return prop.getProperty("category_" + category + "_label").trim();
    }

    public boolean readShowCategory(int category) {

	return Boolean.valueOf(prop.getProperty("showCategory_" + category, "true").trim());
    }

    public boolean readShowGraph() {

	return Boolean.valueOf(prop.getProperty("showGraph", "true").trim());
    }

    public String readNewFromDate() {

	return prop.getProperty("newFromDate", "").trim();
    }

    public boolean readShowComments() {

	return Boolean.valueOf(prop.getProperty("showComments", "true").trim());
    }

    public int readGraphWidth() {

	return Integer.valueOf(prop.getProperty("graphWidth", "GDC").trim());
    }

    public int readGraphHeight() {

	return Integer.valueOf(prop.getProperty("graphHeight", "GDC").trim());
    }

    public boolean readAddGraphMap() {

	return Boolean.valueOf(prop.getProperty("addGraphMap", "false").trim());
    }

    public boolean readUseLogScale() {

	return Boolean.valueOf(prop.getProperty("useLogScale", "true").trim());
    }

    public String readHeaderManager() {

	return prop.getProperty("headerManager", "eu.floraresearch.drm.DefaultLogoTableManager").trim();
    }

    public int readUOSLogoMargin() {

	return Integer.valueOf(prop.getProperty("uosLogoMargin", "1200").trim());
    }

    public boolean readShowUOSPublishedTag() {

	return Boolean.valueOf(prop.getProperty("showUOSPublishedTag", "true").trim());
    }

    public int readFedEOStaticGranules() {

	return Integer.valueOf(prop.getProperty("FedEO-static-granules").trim());
    }

    public int readRASAQMStaticTotal() {

	return Integer.valueOf(prop.getProperty("RASAQM-static-total").trim());
    }

    public int readESRIStaticGranules() {

	return Integer.valueOf(prop.getProperty("ESRI-static-total").trim());
    }

    public int readSeaDataNetStaticGranules() {

	return Integer.valueOf(prop.getProperty("SeaDataNet-static-granules").trim());
    }

    public int readCWICStaticGranules() {

	return Integer.valueOf(prop.getProperty("CWIC-static-granules").trim());
    }

    public int readUSGSStaticRecords() {

	return Integer.valueOf(prop.getProperty("USGS-static-records").trim());
    }

    public int readOBISStaticGranules() {

	return Integer.valueOf(prop.getProperty("OBIS-static-granules").trim());
    }

    public int readChinaGeossStaticGranules() {

	return Integer.valueOf(prop.getProperty("CHINAGEOSS-static-granules").trim());
    }

    public int readSentinelStaticGranules() {

	return Integer.valueOf(prop.getProperty("SENTINEL-static-granules").trim());
    }

    public int readLandsat8StaticGranules() {

	return Integer.valueOf(prop.getProperty("LANDSAT8-static-granules").trim());
    }

    /**
     * @return
     */
    public int readAGOLStaticRecords() {

	return Integer.valueOf(prop.getProperty("AGOL-static-granules").trim());
    }

    /**
     * @return
     */
    public int readHISCentralStaticGranules() {

	return Integer.valueOf(prop.getProperty("HIS-static-granules").trim());
    }

    /**
     * @return
     */
    public int readGBIFStaticGranules() {

	return Integer.valueOf(prop.getProperty("GBIF-static-granules").trim());
    }

    /**
     * @return
     */
    public int readINPEStaticGranules() {

	return Integer.valueOf(prop.getProperty("INPE-static-granules").trim());
    }

    public ReportsSorting readReportsSorting() {

	return ReportsSorting.valueOf(prop.getProperty("reportsSorting", ReportsSorting.BY_NAME.toString()).trim());
    }

    public String readReport(String sourceID) {

	Set<Object> keySet = prop.keySet();
	for (Object object : keySet) {
	    String key = (String) object;
	    if (key.equals(sourceID)) {
		return prop.getProperty(key);
	    }
	}

	return null;
    }

    public boolean readUnderTest(String sourceID) {
	Set<Object> keySet = prop.keySet();
	for (Object object : keySet) {
	    String key = (String) object;
	    if (key.contains(sourceID) && key.contains(UNDERTEST_KEY)) {
		try {
		    return Boolean.valueOf(((String) prop.getProperty(key)));
		} catch (Throwable t) {
		    return false;
		}
	    }
	}

	return false;
    }

}
