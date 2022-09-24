package eu.floraresearch.drm.report;

import java.util.List;

import eu.essi_lab.model.GSSource;
import eu.floraresearch.drm.ConfigReader;

/**
 * @author Fabrizio
 */
public class ReportFactory {

    /**
     * @param sourceID
     * @param list
     * @return
     */
    public static Report createReport(String sourceID, List<GSSource> list) {

	String report = ConfigReader.getInstance().readReport(sourceID);
	if (report == null) {
	    // return null;
	    report = "eu.floraresearch.drm.report.impl.DefaultReport";
	}

	report = report.trim();

	if (report.equals(ConfigReader.EXCLUDED)) {
	    return null;
	}

	if (report.equals(ConfigReader.NOT_IMPLEMENTED)) {
	    return null;
	}

	try {
	    Class<?> clazz = Class.forName(report);
	    Object tempObject = clazz.newInstance();

	    Report r = (Report) tempObject;
	    r.init(sourceID, list);

	    return r;

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }
}
