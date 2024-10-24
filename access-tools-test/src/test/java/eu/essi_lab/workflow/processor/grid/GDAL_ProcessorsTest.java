package eu.essi_lab.workflow.processor.grid;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.validator.geotiff.GeoTIFFValidator;
import eu.essi_lab.validator.netcdf.classic.NetCDF3GridValidator;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.TargetHandler;

public abstract class GDAL_ProcessorsTest {

    private DataObject geotiff = null;
    private DataObject netcdf1 = null;
    private DataObject netcdf2 = null;
    private GeoTIFFValidator geotiffValidator;
    private NetCDF3GridValidator netCDFValidator;

    @Before
    public void init() throws IOException {
	this.geotiff = getInput();
	this.geotiffValidator = new GeoTIFFValidator();
	this.netCDFValidator = new NetCDF3GridValidator();

    }

    public void test() throws Exception {

	// the first processor under test converts to NetCDF
	GDAL_To_NetCDF_Processor processor1 = new GDAL_To_NetCDF_Processor();

	DataDescriptor descriptor = geotiffValidator.readDataAttributes(geotiff);
	CRS originalCRS = descriptor.getCRS();
	this.netcdf1 = processor1.process(geotiff, null);
	netcdf1.getFile().deleteOnExit();
	descriptor.setDataFormat(DataFormat.NETCDF());
	netcdf1.setDataDescriptor(descriptor);
	assertResult(netCDFValidator.validate(netcdf1));

	// the second processor under test converts the NetCDF CRS to 3857
	GDAL_NetCDF_CRS_Converter_Processor processor2 = new GDAL_NetCDF_CRS_Converter_Processor();

	CRS targetCRS = CRS.EPSG_3857();
	ProcessorCapabilities capabilities = new ProcessorCapabilities();
	capabilities.setCRSCapability(CapabilityElement.anyFromCRS(targetCRS));

	TargetHandler target = new TargetHandler(null, null, capabilities);
	this.netcdf2 = processor2.process(netcdf1, target);
	netcdf2.getFile().deleteOnExit();
	// to check that the result has the desired CRS
	descriptor.setCRS(targetCRS);
	if (!originalCRS.equals(targetCRS)) {
	    // if CRS changes, not possible to do the check with the original spatial dimensions
	    descriptor.setSpatialDimensions(null);
	}

	netcdf2.setDataDescriptor(descriptor);
	assertResult(netCDFValidator.validate(netcdf2));

    }

    @After
    public void after() {
	if (geotiff != null) {
	    geotiff.getFile().delete();
	}
	if (netcdf1 != null) {
	    netcdf1.getFile().delete();
	}
	if (netcdf2 != null) {
	    netcdf2.getFile().delete();
	}
    }

    private void assertResult(ValidationMessage result) {
	if (!result.getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL)) {
	    System.out.println(result.getResult());
	    System.out.println(result.getError());
	    System.out.println(result.getErrorCode());
	}
	assertTrue(result.getResult().equals(ValidationResult.VALIDATION_SUCCESSFUL));
    }

    private DataObject getInput() throws IOException {
	DataObject ret = new DataObject();
	InputStream stream = GDAL_To_NetCDF_Processor.class.getClassLoader().getResourceAsStream(getDatasetPath());
	ret.setFileFromStream(stream, "gtnt.tif");
	GeoTIFFValidator validator = new GeoTIFFValidator();
	DataDescriptor descriptor = validator.readDataAttributes(ret);
	ret.setDataDescriptor(descriptor);
	ret.getFile().deleteOnExit();
	return ret;
    }

    public abstract String getDatasetPath();
}
