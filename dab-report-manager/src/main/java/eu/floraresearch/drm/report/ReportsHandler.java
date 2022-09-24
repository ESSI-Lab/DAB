package eu.floraresearch.drm.report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Node;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.floraresearch.drm.ConfigReader;
import eu.floraresearch.drm.ConfigReader.ReportsSorting;
import eu.floraresearch.drm.report.impl.FakeReport;

public class ReportsHandler {

    private static final ReportsHandler INSTANCE = new ReportsHandler();

    private static JAXBContext CONTEXT = null;
    static {

	try {
	    CONTEXT = JAXBContext.newInstance(GSSource.class);
	} catch (JAXBException e) {
	}
    }

    public static ReportsHandler getInstance() {

	return INSTANCE;
    }

    private ReportsHandler() {
    }

    public List<Report> createFakeReports() {

	ArrayList<Report> out = new ArrayList<>();
	for (int i = 0; i < 20; i++) {

	    out.add(new FakeReport());
	}

	return out;
    }

    public List<Report> getReports() throws Exception {

	ArrayList<Report> out = new ArrayList<>();

	String query = ConfigReader.getInstance().readDABEndpoint()
		+ "services/essi/view/geoss/opensearch/query?reqID=expand_svrw3ivlcw9&si=1&ct=500&parents=ROOT";

	Downloader downloader = new Downloader();
	InputStream stream = downloader.downloadStream(query).get();

	XMLDocumentReader reader = new XMLDocumentReader(stream);
	List<Node> entries = reader.evaluateOriginalNodesList("//*:entry");

	GSLoggerFactory.getLogger(getClass()).info("Sources count: " + entries.size());

	List<GSSource> sources = new ArrayList<>();

	for (Node node : entries) {

	    GSSource source = new GSSource();
	    try {
		source.setLabel(reader.evaluateString(node, "./*:title/text()"));
		source.setUniqueIdentifier(reader.evaluateString(node, "./*:id/text()"));

		sources.add(source);

	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	for (int i = 0; i < sources.size(); i++) {

	    GSSource source = sources.get(i);

	    GSLoggerFactory.getLogger(getClass()).info("----------------");

	    String id = source.getUniqueIdentifier();

	    GSLoggerFactory.getLogger(getClass())
		    .info("Retrieving report [" + (i + 1) + " / " + entries.size() + "] " + source.getLabel() + "...");

	    Report report = ReportFactory.createReport(id, sources);

	    if (report != null) {

		GSLoggerFactory.getLogger(getClass()).info("Report " + sources.get(i).getLabel() + " retrieved");

		out.add(report);

		try {

		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_1_Label() + ": " +
		    // String.valueOf(report.getCategory_1_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_2_Label() + ": " +
		    // String.valueOf(report.getCategory_2_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_3_Label() + ": " +
		    // String.valueOf(report.getCategory_3_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_4_Label() + ": " +
		    // String.valueOf(report.getCategory_4_Value()));
		    //
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_5_Label() + ": " +
		    // String.valueOf(report.getCategory_5_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_6_Label() + ": " +
		    // String.valueOf(report.getCategory_6_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_7_Label() + ": " +
		    // String.valueOf(report.getCategory_7_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_8_Label() + ": " +
		    // String.valueOf(report.getCategory_8_Value()));
		    //
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_9_Label() + ": " +
		    // String.valueOf(report.getCategory_9_Value()));
		    //
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_10_Label() + ": " +
		    // String.valueOf(report.getCategory_10_Value()));
		    // GSLoggerFactory.getLogger(getClass()).info(
		    // ConfigReader.getInstance().readCategory_11_Label() + ": " +
		    // String.valueOf(report.getCategory_11_Value()));

		} catch (Exception e) {
		    e.printStackTrace();
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("Skipping report " + sources.get(i).getLabel());
	    }

	}

	Report[] array = out.toArray(new Report[] {});

	final ReportsSorting sorting = ConfigReader.getInstance().readReportsSorting();

	Arrays.sort(array, new Comparator<Report>() {

	    @Override
	    public int compare(Report o1, Report o2) {

		try {

		    switch (sorting) {
		    case BY_NAME:
			return o1.getCompleteName().compareTo(o2.getCompleteName());

		    case BY_TOTAL_COUNT_ASC:
			return o1.getCategory_3_Value() < o2.getCategory_3_Value() ? -1
				: o1.getCategory_3_Value() > o2.getCategory_3_Value() ? 1 : 0;

		    case BY_TOTAL_COUNT_DES:

			return o1.getCategory_3_Value() < o2.getCategory_3_Value() ? 1
				: o1.getCategory_3_Value() > o2.getCategory_3_Value() ? -1 : 0;
		    }
		} catch (Exception ex) {
		}

		return 0;
	    }
	});

	return Arrays.asList(array);
    }

    public static void main(String[] args) throws Exception {

	String query = "http://gs-service-production.geodab.eu/gs-service/services/essi/view/geoss/opensearch/query?reqID=expand_svrw3ivlcw9&si=1&ct=500&parents=ROOT";

	Downloader downloader = new Downloader();
	InputStream stream = downloader.downloadStream(query).get();

	XMLDocumentReader reader = new XMLDocumentReader(stream);
	List<Node> entries = reader.evaluateOriginalNodesList("//*:entry");

	List<GSSource> sources = new ArrayList<>();

	for (Node node : entries) {

	    GSSource source = new GSSource();
	    try {
		source.setLabel(reader.evaluateString(node, "./*:title/text()"));
		source.setUniqueIdentifier(reader.evaluateString(node, "./*:id/text()"));

		sources.add(source);

	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

    }
}
