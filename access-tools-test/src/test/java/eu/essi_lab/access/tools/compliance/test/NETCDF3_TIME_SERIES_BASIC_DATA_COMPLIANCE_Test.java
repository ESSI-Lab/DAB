package eu.essi_lab.access.tools.compliance.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.beust.jcommander.internal.Lists;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.compliance.DataComplianceLevel;
import eu.essi_lab.access.compliance.DataComplianceReport;
import eu.essi_lab.access.compliance.DataComplianceTester;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class NETCDF3_TIME_SERIES_BASIC_DATA_COMPLIANCE_Test {

    @Test
    public void test() {

	{
	    // WorkflowBuilder.enableLogs(true);
	    // WorkflowBuilder.enableDeepLogs(false);

	    DataDownloader downloader = createDownloader();

	    DataComplianceTester tester = new DataComplianceTester(downloader);

	    try {

		DataDescriptor descriptor = downloader.getRemoteDescriptors().get(0);

		DataComplianceReport report = tester.test(//
			DataComplianceTest.EXECUTION, //
			descriptor, //
			descriptor, //
			DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE);

		// -------------------------------
		//
		// BASIC
		//

		Assert.assertEquals(1, report.getWorkflowsCount());
		Assert.assertEquals(1, report.getWorkflowsLength());

		// -------------------------------
		//
		// DOWNLOAD
		//
		Assert.assertTrue(report.isDownloadable().get());

		// -------------------------------
		//
		// VALIDATION
		//
		Assert.assertTrue(report.getValidationMessage().get().getResult() == ValidationResult.VALIDATION_SUCCESSFUL);

		// -------------------------------
		//
		// EXECUTION
		//
		Optional<ValidationMessage> executionResult = report.getExecutionResult();

		Assert.assertTrue(executionResult.isPresent());
		Assert.assertTrue(executionResult.get().getResult() == ValidationResult.VALIDATION_SUCCESSFUL);

		GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    } catch (GSException ex) {

		if (!ex.getErrorInfoList().isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).error(ex.getErrorInfoList().get(0).getErrorDescription());
		}

		fail("Exception thrown");
	    }
	}
    }

    private DataDownloader createDownloader() {

	return new DataDownloader() {

	    @Override
	    public boolean canConnect() throws GSException {
		return true;
	    }

	    @Override
	    public Provider getProvider() {

		return new ESSILabProvider();
	    }

	    @Override
	    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

		DataDescriptor descriptor = new DataDescriptor();
		descriptor.setCRS(CRS.EPSG_4326());
		descriptor.setDataFormat(DataFormat.NETCDF_3());
		descriptor.setDataType(DataType.TIME_SERIES);

		// --------------------------------------------

		List<DataDimension> otherDimensions = Lists.newArrayList();

		ContinueDimension vertical = new ContinueDimension(null);
		vertical.setDatum(new Datum("NGVD29"));
		vertical.setLower(1345.0);
		vertical.setType(DimensionType.VERTICAL);
		vertical.setUom(Unit.METRE);
		vertical.setUpper(1345.0);

		otherDimensions.add(vertical);

		descriptor.setOtherDimensions(otherDimensions);

		// --------------------------------------------

		List<DataDimension> spatialDimensions = Lists.newArrayList();

		ContinueDimension lat = new ContinueDimension("Latitude");
		lat.setLower(41.718473);
		lat.setType(DimensionType.COLUMN);
		lat.setUom(Unit.DEGREE);
		lat.setUpper(41.718473);

		ContinueDimension lon = new ContinueDimension("Longitude");
		lon.setLower(-111.946402);
		lon.setType(DimensionType.ROW);
		lon.setUom(Unit.DEGREE);
		lon.setUpper(-111.946402);

		spatialDimensions.add(lat);
		spatialDimensions.add(lon);

		descriptor.setSpatialDimensions(spatialDimensions);

		// --------------------------------------------

		ContinueDimension time = new ContinueDimension("time");
		time.setDatum(Datum.UNIX_EPOCH_TIME());
		time.setLower(1123180200000l);
		time.setResolution(1800000l);
		time.setType(DimensionType.TIME);
		time.setUom(Unit.MILLI_SECOND);
		time.setUpper(1123216200000l);

		descriptor.setTemporalDimension(time);

		return Arrays.asList(descriptor);
	    }

	    @Override
	    public File download(DataDescriptor descriptor) throws GSException {

		InputStream stream = getClass().getClassLoader()
			.getResourceAsStream("eu/essi_lab/access/tools/compliance/test/WML-to-NC-reduced.nc");

		try {
		    File tmpFile = File.createTempFile("access-tools-test", ".nc");
		    tmpFile.deleteOnExit();
		    FileOutputStream fos = new FileOutputStream(tmpFile);
		    IOUtils.copy(stream, fos);
		    fos.close();
		    return tmpFile;
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}

	    }

	    @Override
	    public Online getOnline() {

		return new Online();
	    }

	    @Override
	    public boolean canDownload() {

		return true;
	    }
	};
    }
}
