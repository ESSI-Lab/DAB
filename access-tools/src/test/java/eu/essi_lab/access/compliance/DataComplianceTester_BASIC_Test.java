package eu.essi_lab.access.compliance;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.DataDownloader;
import eu.essi_lab.access.compliance.DataComplianceTester.DataComplianceTest;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;

public class DataComplianceTester_BASIC_Test {

    @Test
    public void GRID_Test() throws GSException {

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getTargetDescriptor(dataDescriptor));

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertTrue(report.getWorkflowsCount() > 0);
	    Assert.assertTrue(report.getWorkflowsLength() <= 3);
	}

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_DATA_COMPLIANCE_1.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());
	}

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_DATA_COMPLIANCE_2.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());
	}

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_DATA_COMPLIANCE_3.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());
	}

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_DATA_COMPLIANCE_4.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());
	}

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.GRID);
	    dataDescriptor.setCRS(CRS.EPSG_3857());
	    dataDescriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());

	    setGRID_TestDimensions(dataDescriptor);

	    //
	    // 4326 to 3857 CRS Converter not yet available
	    //
	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester //
		    .test(DataComplianceTest.BASIC, //
			    dataDescriptor, //
			    dataDescriptor, //
			    DataComplianceLevel.GRID_BASIC_DATA_COMPLIANCE.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertTrue(report.getWorkflowsCount() > 0);
	}
    }

    @Test
    public void TIMESERIES_Test() throws GSException {

	{

	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.TIME_SERIES);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.WATERML_1_1());

	    setTIMESERIES_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester.test(//
		    DataComplianceTest.BASIC, //
		    dataDescriptor, //
		    dataDescriptor, //
		    DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(2, report.getWorkflowsCount());
	    Assert.assertEquals(2, report.getWorkflowsLength());

	}

	{
	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.TIME_SERIES);
	    dataDescriptor.setCRS(CRS.EPSG_4326());
	    dataDescriptor.setDataFormat(DataFormat.WATERML_1_1());

	    setTIMESERIES_TestDimensions(dataDescriptor);

	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester.test(//
		    DataComplianceTest.BASIC, //
		    dataDescriptor, //
		    dataDescriptor, //
		    DataComplianceLevel.TIME_SERIES_DATA_COMPLIANCE_1.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());

	}

	{
	    DataDescriptor dataDescriptor = new DataDescriptor();
	    dataDescriptor.setDataType(DataType.TIME_SERIES);
	    dataDescriptor.setCRS(CRS.EPSG_3857());
	    dataDescriptor.setDataFormat(DataFormat.WATERML_1_1());

	    setTIMESERIES_TestDimensions(dataDescriptor);

	    //
	    // 4326 to 3857 CRS Converter not yet available
	    //
	    DataComplianceTester tester = new DataComplianceTester(createDownloader(dataDescriptor));

	    DataComplianceReport report = tester.test(//
		    DataComplianceTest.BASIC, //
		    dataDescriptor, //
		    dataDescriptor, //
		    DataComplianceLevel.TIME_SERIES_BASIC_DATA_COMPLIANCE.getTargetDescriptor(dataDescriptor));//

	    GSLoggerFactory.getLogger(getClass()).info(report.toString());

	    Assert.assertEquals(0, report.getWorkflowsCount());

	}
    }

    private void setGRID_TestDimensions(DataDescriptor dataDescriptor) {

	dataDescriptor.setEPSG4326SpatialDimensions(10.0, 10.0, -10.0, -10.0);

	ContinueDimension d1 = dataDescriptor.getSpatialDimensions().get(0).getContinueDimension();
	d1.setResolution(1.);

	ContinueDimension d2 = dataDescriptor.getSpatialDimensions().get(1).getContinueDimension();
	d2.setResolution(1.);

	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);

	timeDimension.setType(DimensionType.TIME);

	dataDescriptor.setTemporalDimension(timeDimension);
    }

    private void setTIMESERIES_TestDimensions(DataDescriptor dataDescriptor) {

	ContinueDimension timeDimension = new ContinueDimension("time");
	timeDimension.setUom(Unit.SECOND);
	timeDimension.setLower(5);
	timeDimension.setUpper(10);
	timeDimension.setResolution(1.0);

	dataDescriptor.setTemporalDimension(timeDimension);
    }

    private DataDownloader createDownloader(DataDescriptor descriptor) {

	return new DataDownloader() {

	    @Override
	    public Provider getProvider() {

		return null;
	    }
	    
	    @Override
	    public boolean canConnect() throws GSException {
		return true;
	    }

	    @Override
	    public List<DataDescriptor> getRemoteDescriptors() throws GSException {

		return Arrays.asList(descriptor);
	    }

	    public List<DataDescriptor> getPreviewRemoteDescriptors() throws GSException {

		return Arrays.asList(descriptor);
	    }

	    @Override
	    public File download(DataDescriptor descriptor) throws GSException {

		return null;
	    }

	    @Override
	    public Online getOnline() {

		return new Online();
	    }

	    @Override
	    public boolean canDownload() {

		return false;
	    }
	};
    }
}
