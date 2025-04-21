package eu.essi_lab.workflow.processor.grid;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;

import org.gdal.gdal.gdal;
import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.validator.netcdf.NetCDF4GridValidator;
import eu.essi_lab.validator.netcdf.classic.NetCDF3GridValidator;
import eu.essi_lab.workflow.processor.CapabilityElement;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.ResamplingCapability;
import eu.essi_lab.workflow.processor.SubsettingCapability;
import eu.essi_lab.workflow.processor.TargetHandler;
import eu.essi_lab.workflow.processor.grid.GDALConstants.Implementation;
import ucar.ma2.Array;
import ucar.nc2.dataset.NetcdfDataset;

public class GDAL_NetCDF_CRS_Converter_ProcessorTest {

	org.slf4j.Logger logger = GSLoggerFactory.getLogger(this.getClass());

	@Test
	public void testDifferentImplementations2() throws Exception {

		File input = new File("/tmp/netcdf-connector-Monthly_T2m.nc");
		FileInputStream stream = new FileInputStream(input);

		DataObject object = new DataObject();
		object.setFileFromStream(stream, "input.nc");
		NetCDF4GridValidator validator = new NetCDF4GridValidator();
		DataDescriptor descriptor = validator.readDataAttributes(object);
		object.setDataDescriptor(descriptor);
		File inputFile = object.getFile();
		inputFile.deleteOnExit();

		
		GDAL_NetCDF_CRS_Converter_Processor processor = new GDAL_NetCDF_CRS_Converter_Processor();
//	    gdalwarp /tmp/GDAL_To_NetCDF_Processor6754669303119625683.nc -of netCDF -dstnodata 9.96921E36 -t_srs EPSG:3857 -tr 10881.194678935599 10895.255990744385 -te -4803464.401378906 3393144.756018512 6861176.294440056 8296009.951853484  /tmp/GDAL_NetCDF_CRS_Converter_Processor5482421176845898518.nc

		DataDescriptor target = new DataDescriptor();
		target.setCRS(CRS.EPSG_3857());
		target.setDataFormat(DataFormat.NETCDF_3());
		target.setEPSG3857SpatialDimensions(-4798023.804039438, 3398592.3840138842, 6855735.697100588, 8290562.323858112);
		target.getFirstSpatialDimension().getContinueDimension().setResolution(10881.194678935599);
		target.getSecondSpatialDimension().getContinueDimension().setResolution(10895.255990744385);
		ProcessorCapabilities capabilities = new ProcessorCapabilities();
		capabilities.setCRSCapability(CapabilityElement.anyFromCRS(CRS.EPSG_3857()));
		capabilities.setResamplingCapability(ResamplingCapability.SPATIAL_RESAMPLING());
		capabilities.setSubsettingCapability(SubsettingCapability.SPATIAL_SUBSETTING());

		TargetHandler handler = new TargetHandler(null, target, capabilities);

		GDALConstants.IMPLEMENTATION = Implementation.RUNTIME;

		DataObject result = processor.process(object, handler);
		File outputFile2 = result.getFile();
		System.out.println(outputFile2.getAbsolutePath());
		
		outputFile2.deleteOnExit();

		
	}

}
