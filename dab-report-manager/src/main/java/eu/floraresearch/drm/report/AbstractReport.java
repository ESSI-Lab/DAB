package eu.floraresearch.drm.report;

import java.io.InputStream;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.floraresearch.drm.ConfigReader;

public abstract class AbstractReport implements Report {

    private static final String DELETED_RECORDS_TEMPLATE_PATH = "template/deletedRecords.xml";
    private static final String NEW_RECORDS_TEMPLATE_PATH = "template/newRecords.xml";
    private static final String ALL_RECORDS_TEMPLATE_PATH = "template/allRecords.xml";
    private static final String VALID_RECORDS_TEMPLATE_PATH = "template/validRecords.xml";
    private static final String REPAIRED_RECORDS_TEMPLATE_PATH = "template/repairedRecords.xml";
    private static final String NOT_VALIDATED_RECORDS_TEMPLATE_PATH = "template/notValidatedRecords.xml";

    private static final InputStream DELETED_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(DELETED_RECORDS_TEMPLATE_PATH);
    private static final InputStream NEW_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(NEW_RECORDS_TEMPLATE_PATH);

    private static final InputStream ALL_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(ALL_RECORDS_TEMPLATE_PATH);

    private static final InputStream VALID_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(VALID_RECORDS_TEMPLATE_PATH);

    private static final InputStream REPAIRED_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(REPAIRED_RECORDS_TEMPLATE_PATH);

    private static final InputStream NOT_VALIDATED_RECORDS_STREAM = AbstractReport.class.getClassLoader()
	    .getResourceAsStream(NOT_VALIDATED_RECORDS_TEMPLATE_PATH);

    protected static String deletedRecordsTemplate;
    protected static String newRecordsTemplate;
    protected static String allRecordsTemplate;
    protected static String validRecordsTemplate;
    protected static String repairedRecordsTemplate;
    protected static String notValidatedRecordsTemplate;

    static {
	try {
	    deletedRecordsTemplate = IOStreamUtils.asUTF8String(DELETED_RECORDS_STREAM);
	    newRecordsTemplate = IOStreamUtils.asUTF8String(NEW_RECORDS_STREAM);
	    allRecordsTemplate = IOStreamUtils.asUTF8String(ALL_RECORDS_STREAM);
	    validRecordsTemplate = IOStreamUtils.asUTF8String(VALID_RECORDS_STREAM);
	    repairedRecordsTemplate = IOStreamUtils.asUTF8String(REPAIRED_RECORDS_STREAM);
	    notValidatedRecordsTemplate = IOStreamUtils.asUTF8String(NOT_VALIDATED_RECORDS_STREAM);

	} catch (Exception e) {
	}
    }

    protected GSSource source;

    protected boolean test = false;

    public AbstractReport() {
    }

    public int[] refineRecordsCount() {

	int records = 0;
	int gdcRecords = 0;

	try {
	    records = getCategory_3_Value();
	    gdcRecords = getCategory_4_Value();

	    if (records == gdcRecords) {
		gdcRecords = records;
		records = 0;
	    } else {

		records = records - gdcRecords;
	    }

	    // CommonLogger.getInstance().main(this, "Records: " + records);
	    // CommonLogger.getInstance().main(this, "GDC records: " + gdcRecords);

	    // CommonLogger.getInstance().main(this, "---");

	    // if (records < 0) {
	    // cleanCache();
	    // CommonLogger.getInstance().main(this, "Retrying..");
	    // return refineRecordsCount();
	    // }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return new int[] { records, gdcRecords };
    }

    protected abstract void cleanCache();

    public void init(String sourceID, List<GSSource> list) {

	for (GSSource source : list) {

	    if (source.getUniqueIdentifier().equals(sourceID)) {

		this.source = source;
	    }
	}

	setUnderTest(ConfigReader.getInstance().readUnderTest(sourceID));
    }

    public void setUnderTest(boolean value) {
	test = value;
    }

    public boolean underTest() {
	return test;
    }

    @Override
    public String getCompleteName() {

	return source.getLabel();
    }

    @Override
    public String getCategory_5_Value() throws Exception {

	return "PROTOCOL"; // source.getProtocol();
    }
}
