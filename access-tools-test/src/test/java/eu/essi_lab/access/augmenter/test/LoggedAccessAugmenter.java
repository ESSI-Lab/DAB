package eu.essi_lab.access.augmenter.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.DataDownloaderFactory;
import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.access.compliance.wrapper.ReportWrapper;
import eu.essi_lab.access.compliance.wrapper.ReportsMetadataHandler;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;

public class LoggedAccessAugmenter extends AccessAugmenter {

    private String downloadFolder;

    /**
     * @param folder
     */
    public void setDownloadFolder(String folder) {

	this.downloadFolder = folder;
    }

    /**
     * Tests a single online resource.
     * 
     * @param resource
     * @param online
     * @throws GSException
     */
    @SuppressWarnings("incomplete-switch")
    @Override
    protected void augment(GSResource resource, String onlineId, ReportsMetadataHandler handler) throws GSException {

	Online online = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution()
		.getDistributionOnline(onlineId);

	DataDownloader downloader = DataDownloaderFactory.getDataDownloader(resource, onlineId);
	DataComplianceTester tester = new DataComplianceTester(downloader);

	List<DataDescriptor> descriptors = downloader.getPreviewRemoteDescriptors();

	DataComplianceLevel level = null;

	for (DataDescriptor descriptor : descriptors) {

	    DataType dataType = descriptor.getDataType();
	    switch (dataType) {
	    case GRID:

		level = DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE;
		break;
	    case TIME_SERIES:

		level = DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE;
		break;
	    case POINT:
		break;
	    case PROFILE:
		break;
	    case TIME_SERIES_PROFILE:
		break;
	    case TRAJECTORY:
		break;
	    case TRAJECTORY_PROFILE:
		break;
	    }

	    if (level != null) {

		DataComplianceReport report = tester.test( //
			DataComplianceTest.EXECUTION, //
			descriptor, //
			descriptor, //
			level);

		DataComplianceTest lastSucceededTest = report.getLastSucceededTest();

		String folder = null;

		switch (lastSucceededTest) {
		case DOWNLOAD:
		    folder = downloadFolder + File.separator + "VALIDATION-ERROR";
		    break;
		case VALIDATION:
		    folder = downloadFolder + File.separator + "EXECUTION-ERROR";
		    break;
		case EXECUTION:
		    folder = downloadFolder + File.separator + "OK";
		    break;
		}

		File folderFile = new File(folder);

		if (!folderFile.exists()) {
		    folderFile.mkdir();
		}

		try {

		    long time = System.currentTimeMillis();

		    // -----------------

		    FileOutputStream dataOutput = new FileOutputStream(new File(folder + File.separator + online.getName() + "-DATA.xml"));
		    DataObject downloadedData = report.getDownloadedData();
		    File dataFile = downloadedData.getFile();		    
		    InputStream stream = new FileInputStream(dataFile);
		    IOUtils.copy(stream, dataOutput);
		    dataFile.delete();
		    
		    // -----------------

		    ReportWrapper reportWrapper = new ReportWrapper(report);
		    InputStream reportStream = reportWrapper.asStream(false);
		    FileOutputStream reportOutput = new FileOutputStream(
			    new File(folder + File.separator + online.getName() + "-REPORT.xml"));
		    IOUtils.copy(reportStream, reportOutput);

		    // -----------------

		    InputStream onlineStream = IOUtils.toInputStream("Online identifier: " + onlineId + "\nLinkage: " + online.getLinkage()
			    + "\nProtocol: " + online.getProtocol() + "\nName:  " + online.getName());
		    FileOutputStream onlineOutput = new FileOutputStream(
			    new File(folder + File.separator + online.getName() + "-ONLINE.xml"));
		    IOUtils.copy(onlineStream, onlineOutput);

		} catch (Exception ex) {

		    ex.printStackTrace();
		}

		handler.addReport(report);
	    }
	}
    }
}
