package eu.essi_lab.accessor.waf;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.workflow.processor.DescriptorUtils;
import eu.essi_lab.workflow.processor.ProcessorCapabilities;
import eu.essi_lab.workflow.processor.TargetHandler;
import eu.essi_lab.workflow.processor.grid.GDAL_NetCDF_CRS_Converter_Processor;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDataset.Enhance;
import ucar.nc2.dt.GridDataset;
import ucar.unidata.geoloc.LatLonRect;

public class CRSConverterTest {

    private static Logger logger = GSLoggerFactory.getLogger(GDAL_NetCDF_CRS_Converter_Processor.class);

    public static void main(String[] args) throws Exception {

	//
	//
	//

	// String ncPath = "D:\\Desktop\\nc1.nc";
	String ncPath = "/home/boldrini/nc1.nc";

	NetcdfDataset dataset = NetcdfDataset.openDataset(ncPath);
	List<CoordinateSystem> systems = dataset.getCoordinateSystems();
	//
	//
	//

	Double minx = null;
	Double maxx = null;
	Double miny = null;
	Double maxy = null;
	b1: for (CoordinateSystem system : systems) {
	    List<CoordinateAxis> axes = system.getCoordinateAxes();
	    for (CoordinateAxis axe : axes) {
		double min = axe.getMinValue();
		double max = axe.getMaxValue();
		if (axe.getAxisType().equals(AxisType.GeoX)) {
		    minx = min;
		    maxx = max;
		}
		if (axe.getAxisType().equals(AxisType.GeoY)) {
		    miny = min;
		    maxy = max;
		}
		if (minx != null & miny != null & maxx != null && maxy != null) {
		    break b1;
		}
	    }
	}

	Variable var = dataset.findVariable(null, "LU_INDEX");
	List<Dimension> dims = var.getDimensions();
	for (Dimension dim : dims) {
	    Variable dimVar = dataset.findVariable(dim.getFullName());
	    // dataset.ge
	    // System.out.println();
	}

	DataObject dataObject = new DataObject();
	dataObject.setFile(new File(ncPath));

	DataDescriptor source = new DataDescriptor();
	source.setDataType(DataType.GRID);
	source.setDataFormat(DataFormat.NETCDF_3());
	// source.setCRS(CRS.EPSG_3857());

	dataObject.setDataDescriptor(source);

	//
	//
	//

	DataDescriptor target = new DataDescriptor();

	target.setDataType(DataType.GRID);
	target.setDataFormat(DataFormat.NETCDF_3());
	target.setCRS(CRS.EPSG_4326());

	NetcdfDataset netCDFdataset = NetcdfDataset.openDataset(ncPath);
	GridDataset gds = ucar.nc2.dt.grid.GridDataset.open(//
		netCDFdataset.getLocation(), //
		EnumSet.of(Enhance.ScaleMissing, Enhance.CoordSystems, Enhance.ConvertEnums));

	@SuppressWarnings("deprecation")
	LatLonRect datasetBbox = gds.getBoundingBox();

	double west = datasetBbox.getLonMin();
	double south = datasetBbox.getLatMin();
	double east = datasetBbox.getLonMax();
	double north = datasetBbox.getLatMax();

	target.setEPSG4326SpatialDimensions(north, east, south, west);

	netCDFdataset.close();

	//
	//
	//

	ProcessorCapabilities capabilities = DescriptorUtils.fromTargetDescriptor(source, target);

	TargetHandler handler = new TargetHandler(source, target, capabilities);

	GDAL_NetCDF_CRS_Converter_Processor processor = new GDAL_NetCDF_CRS_Converter_Processor();
	DataObject result = processor.process(dataObject, handler);

	File file = result.getFile();
	System.out.println(file);
    }

    
}
