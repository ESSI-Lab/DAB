package eu.essi_lab.accessor.waf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.imageio.netcdf.GeoToolsNetCDFReader;
import org.geotools.imageio.netcdf.utilities.NetCDFCRSUtilities;
import org.junit.Test;

import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.data.AxisOrder;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.Datum;
import eu.essi_lab.model.resource.data.DimensionType;
import eu.essi_lab.model.resource.data.Unit;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;
import eu.essi_lab.netcdf.timeseries.NetCDFUtils;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * @author Fabrizio
 */
public class NetCDFTest {

    /**
     * 
     */
    private static final String NC_FILE_NAME = "genova_era5.nc";

    /**
     * TO BE IMPROVED
     * 
     * @throws GSException
     * @throws JAXBException
     * @throws URISyntaxException
     * @throws IOException
     * @throws Exception 
     * @throws NoSuchAuthorityCodeException 
     */
    @Test
    public void test() throws GSException, JAXBException, URISyntaxException, IOException, NoSuchAuthorityCodeException, Exception {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	
	CRS crs = null;
	
	DataDescriptor ret = new DataDescriptor();

	File file = new File(getClass().getClassLoader().getResource(NC_FILE_NAME).toURI());

	NetcdfDataset ncDataset = NetcdfDataset.openDataset(file.getAbsolutePath());

	String fti = ncDataset.getFileTypeId();
	if (fti != null) {
	    switch (fti) {
	    case "NetCDF-4":
		ret.setDataFormat(DataFormat.NETCDF_4());
		break;
	    case "NetCDF-3":
	    default:
		ret.setDataFormat(DataFormat.NETCDF_3());
		break;
	    }
	}

	List<Variable> mainVariables = NetCDFUtils.getGeographicVariables(ncDataset);
	
	
	 if (mainVariables.isEmpty() || // no georeferenced variables
		    mainVariables.size() > 1 // multiple georeferenced variables, leading to ambiguity: which variable
					     // should be
	    // validated?
	    // TODO: add in the data descriptor the name parameter, identifying a particular variable / sub dataset
	    ) {
		//throw new IllegalArgumentException(GRIDS_MISMATCH);
	    } else {
		ret.setDataType(DataType.GRID);
	    }

	    CoordinateReferenceSystem decodedCRS = GeoToolsNetCDFReader.extractCRS(ncDataset, mainVariables.get(0));

	    if (decodedCRS != null) {
		// this workaround is because by default GeoTools NetcdfUtils uses OGC CRS 84 for NetCDF latitude
		// longitude crs
		if (decodedCRS.equals(NetCDFCRSUtilities.WGS84)) {
		    decodedCRS = org.geotools.referencing.CRS.decode("EPSG:4326");
		}

		crs = CRS.fromGeoToolsCRS(decodedCRS);

		ret.setCRS(crs);
	    }

	    List<CoordinateAxis> axes = ncDataset.getCoordinateAxes();

	    ContinueDimension spatialEast = null;
	    ContinueDimension spatialNorth = null;

	    for (CoordinateAxis axe : axes) {
		ContinueDimension dimension = new ContinueDimension(NetCDFUtils.getAxisName(axe));
		double min = axe.getMinValue();
		double max = axe.getMaxValue();
		long size = axe.getSize();
		Double resolution = NetCDFUtils.readMinimumResolution(axe.read());
		Unit uom = Unit.fromIdentifier(NetCDFUtils.getAxisUnit(axe));
		Datum datum = null;

		AxisType type = axe.getAxisType();
		if (type == null) {
		    ret.getOtherDimensions().add(dimension);
		} else {
		    switch (type) {
		    case GeoX:
		    case Lon:
			spatialEast = dimension;
			dimension.setType(DimensionType.ROW);
			datum = crs.getDatum();
			break;
		    case GeoY:
		    case Lat:
			spatialNorth = dimension;
			dimension.setType(DimensionType.COLUMN);
			datum = crs.getDatum();
			break;
		    case GeoZ:
			dimension.setType(DimensionType.VERTICAL);
			datum = Datum.SEA_LEVEL_DATUM_1929();
			break;
		    case Time:
			CoordinateAxis1DTime timeAxis = CoordinateAxis1DTime.factory(ncDataset, axe, null);
			dimension.setType(DimensionType.TIME);
			ret.setTemporalDimension(dimension);
			Double resT = NetCDFUtils.readResolution(axe.read());
			// it means it has a regular time grid (although time units may not be milliseconds), so we are
			// authorized to divide
			if (resT != null && size != 0) {
			    long milliSeconds = timeAxis.getCalendarDateRange().getDurationInSecs() * 1000;
			    resolution = (double) milliSeconds / (double) (size - 1);
			    min = timeAxis.getCalendarDateRange().getStart().getMillis();
			    max = timeAxis.getCalendarDateRange().getEnd().getMillis();
			    uom = Unit.MILLI_SECOND;
			    datum = Datum.UNIX_EPOCH_TIME();
			}
			break;
		    default:
			ret.getOtherDimensions().add(dimension);
			break;
		    }
		}

		dimension.setLower(min);
		dimension.setUpper(max);
		dimension.setSize(size);
		dimension.setResolution(resolution);
		dimension.setDatum(datum);
		dimension.setUom(uom);
	    }
	    List<DataDimension> spatialDimensions = new ArrayList<>();
	    if (crs != null && crs.getAxisOrder().equals(AxisOrder.NORTH_EAST)) {
		spatialDimensions.add(spatialNorth);
		spatialDimensions.add(spatialEast);
	    } else {
		spatialDimensions.add(spatialEast);
		spatialDimensions.add(spatialNorth);
	    }
	    ret.setSpatialDimensions(spatialDimensions);
	    
	   // DataDimension temporal = dataObject.getDataDescriptor().getTemporalDimension();
	    //if(temporal != null)	    
	    //	ret.setTemporalDimension(temporal);

	
	// GSSource source = new GSSource();
	//
	// GSResource resource = mapper.map(originalMetadata, source);
	//
	// String title = resource.getHarmonizedMetadata().getCoreMetadata().getTitle();
	//
	// Assert.assertTrue(title.contains("OUTPUT FROM WRF V3.8.1 MODEL"));

	// System.out.println(resource.asString(true));
    }
}
