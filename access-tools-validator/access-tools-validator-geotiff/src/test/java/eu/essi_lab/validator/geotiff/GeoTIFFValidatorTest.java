package eu.essi_lab.validator.geotiff;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;
import eu.essi_lab.model.resource.data.dimension.ContinueDimension;
import eu.essi_lab.model.resource.data.dimension.DataDimension;

public class GeoTIFFValidatorTest extends DataValidatorTestUtils {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new GeoTIFFValidator();
    }

    @Test
    public void testNullDescriptor() throws Exception {
	DataDescriptor descriptor = null;
	test("", descriptor, false, DataValidatorErrorCode.INVALID_DESCRIPTOR);
    }

    @Test
    public void testNullFormat() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	test("", descriptor, false, DataValidatorErrorCode.INVALID_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedFormat1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.GML_3_1());
	descriptor.setDataType(DataType.GRID);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedFormat2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF_4());
	descriptor.setDataType(DataType.GRID);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testNullDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.TIME_SERIES);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedDimensions() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	List<DataDimension> dimensions = new ArrayList<>();
	ContinueDimension dimension = new ContinueDimension("pressure");
	dimensions.add(dimension);
	descriptor.setOtherDimensions(dimensions);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testCorruptedTIF() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	test("example51-bad.tif", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    // @Test
    // public void testInvalidNetCDF() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // test("wml-2.tif", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    // }

    @Test
    public void testDontCareValidation() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	test("example51.tif", descriptor, true, null);
    }

    @Test
    public void testValidation1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	descriptor.setCRS(CRS.EPSG_4326());
	Double n = 27.0;
	Double e = 55.0;
	Double s = 10.0;
	Double w = 20.0;
	descriptor.setEPSG4326SpatialDimensions(n, e, s, w);
	ContinueDimension latDimension = descriptor.getSpatialDimensions().get(0).getContinueDimension();
	ContinueDimension lonDimension = descriptor.getSpatialDimensions().get(1).getContinueDimension();
	latDimension.setSize(18l);
	latDimension.setResolution(1.0);
	lonDimension.setSize(36l);
	lonDimension.setResolution(1.0);
	test("example51.tif", descriptor, true, null);
    }

    @Test
    public void testValidation2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	descriptor.setCRS(CRS.EPSG_4326());
	// Double n = 27.0;
	// Double e = 55.0;
	// Double s = 10.0;
	// Double w = 20.0;
	// descriptor.setEPSG4326SpatialDimensions(n, e, s, w);
	// ContinueDimension latDimension = descriptor.getSpatialDimensions().get(0).getContinueDimension();
	// ContinueDimension lonDimension = descriptor.getSpatialDimensions().get(1).getContinueDimension();
	// latDimension.setSize(18l);
	// latDimension.setResolution(1.0);
	// lonDimension.setSize(36l);
	// lonDimension.setResolution(1.0);
	test("example51.tif", descriptor, true, null);
    }

    @Test
    public void testValidationB1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	descriptor.setCRS(CRS.EPSG_4326());
	Double res1 = 0.027930205841924398;
	Double res2 = 0.027921142002989542;

	Double n = -3.600661 - res1 / 2.0;
	Double s = -36.1114206 + res1 / 2.0;

	Double e = -36.7188349 - res2 / 2.0;
	Double w = -55.3980789 + res2 / 2.0;
	descriptor.setEPSG4326SpatialDimensions(n, e, s, w);
	ContinueDimension latDimension = descriptor.getSpatialDimensions().get(0).getContinueDimension();
	ContinueDimension lonDimension = descriptor.getSpatialDimensions().get(1).getContinueDimension();
	latDimension.setSize(1164l);
	latDimension.setResolution(res1);
	lonDimension.setSize(669l);
	lonDimension.setResolution(res2);

	test("wcs100-eox-coverage.tif", descriptor, true, null);
    }

    @Test
    public void testValidation57() throws Exception {

	// String wkt = "PROJCS[\"unnamed\", \n" +
	// " GEOGCS[\"WGS 84\", \n" +
	// " DATUM[\"World Geodetic System 1984\", \n" +
	// " SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n" +
	// " AUTHORITY[\"EPSG\",\"6326\"]], \n" +
	// " PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n" +
	// " UNIT[\"degree\", 0.017453292519943295], \n" +
	// " AXIS[\"Geodetic latitude\", NORTH], \n" +
	// " AXIS[\"Geodetic longitude\", EAST], \n" +
	// " AUTHORITY[\"EPSG\",\"4326\"]], \n" +
	// " PROJECTION[\"Lambert_Conformal_Conic_1SP\"], \n" +
	// " PARAMETER[\"central_meridian\", 118.0], \n" +
	// " PARAMETER[\"latitude_of_origin\", 25.0], \n" +
	// " PARAMETER[\"scale_factor\", 1.0], \n" +
	// " PARAMETER[\"false_easting\", 0.0], \n" +
	// " PARAMETER[\"false_northing\", 0.0], \n" +
	// " UNIT[\"m*1000.0\", 1000.0], \n" +
	// " AXIS[\"Easting\", EAST], \n" +
	// " AXIS[\"Northing\", NORTH]]";
	//
	// CRS crs = CRS.fromWKT(wkt);

	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.IMAGE_GEOTIFF());
	descriptor.setDataType(DataType.GRID);
	// descriptor.setCRS(crs);
	Double maxy = 227.0;
	Double maxx = 305.0;
	Double miny = 0.0;
	Double minx = 0.0;

	ContinueDimension xDimension = new ContinueDimension("x");
	xDimension.setSize(306l);
	xDimension.setResolution(1.0);
	xDimension.setLower(minx);
	xDimension.setUpper(maxx);

	ContinueDimension yDimension = new ContinueDimension("y");
	yDimension.setSize(228l);
	yDimension.setResolution(1.0);
	yDimension.setLower(miny);
	yDimension.setUpper(maxy);

	List<DataDimension> spatialDimensions = new ArrayList<>();
	spatialDimensions.add(xDimension);
	spatialDimensions.add(yDimension);
	descriptor.setSpatialDimensions(spatialDimensions);
	// descriptor.setTemporalDimension(new Date(631152000000l), new Date(631411200000l));
	// descriptor.getTemporalDimension().getContinueDimension().setResolution(86400000);
	// descriptor.getTemporalDimension().getContinueDimension().setSize(4l);
	test("example57.tif", descriptor, true, null);
    }

}
