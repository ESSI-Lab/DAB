package eu.essi_lab.validator.netcdf;

import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataType;

public class NetCDFTrajectoryValidatorTest extends DataValidatorTestUtils {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new NetCDF4TrajectoryValidator();
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
	descriptor.setDataFormat(DataFormat.WATERML_1_1());
	descriptor.setDataType(DataType.TRAJECTORY);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testNullDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedDataType() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.GRID);
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testUnsupportedCRS() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setCRS(CRS.EPSG_3857());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testCorruptedNetCDF() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	test("time-series-0.nc", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    }

    // @Test
    // public void testInvalidNetCDF() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // test("wml-2.nc", descriptor, false, DataValidatorErrorCode.DECODING_ERROR);
    // }

    @Test
    public void testDontCareValidation() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	test("trajectory-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setCRS(CRS.EPSG_4326());
	test("trajectory-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(44.48202133178711, 8.749199867248535, 44.44683074951172, 8.73902416229248);
	test("trajectory-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(44.48202133178711, 8.749199867248535, 44.44683074951172, 8.73902416229248);
	descriptor.setVerticalDimension(167.47999572753906, 621.3599853515625);
	test("trajectory-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TRAJECTORY);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(44.48202133178711, 8.749199867248535, 44.44683074951172, 8.73902416229248);
	descriptor.setVerticalDimension(167.47999572753906, 621.3599853515625);
	descriptor.setTemporalDimension(new Date(1653490803000l), new Date(1653493110000l));
	test("trajectory-1.nc", descriptor, true, null);
    }

  

}
