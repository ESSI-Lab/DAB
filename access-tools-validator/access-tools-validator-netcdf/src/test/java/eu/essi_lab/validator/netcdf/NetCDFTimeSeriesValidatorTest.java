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

public class NetCDFTimeSeriesValidatorTest extends DataValidatorTestUtils {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() {
	this.validator = new NetCDF4TimeSeriesValidator();
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
	descriptor.setDataType(DataType.TIME_SERIES);
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
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_3857());
	test("", descriptor, false, DataValidatorErrorCode.UNSUPPORTED_DESCRIPTOR);
    }

    @Test
    public void testCorruptedNetCDF() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
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
	descriptor.setDataType(DataType.TIME_SERIES);
	test("time-series-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	test("time-series-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	test("time-series-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	test("time-series-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidation4() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("time-series-1.nc", descriptor, true, null);
    }

    @Test
    public void testValidationFailedBadDescriptor1() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.518473, -111.946402); // changed spatial dimensions
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("time-series-1.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor2() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1342, 1345); // changed vertical dimension
	descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7))));
	test("time-series-1.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    @Test
    public void testValidationFailedBadDescriptor3() throws Exception {
	DataDescriptor descriptor = new DataDescriptor();
	descriptor.setDataFormat(DataFormat.NETCDF());
	descriptor.setDataType(DataType.TIME_SERIES);
	descriptor.setCRS(CRS.EPSG_4326());
	descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
	descriptor.setVerticalDimension(1345, 1345);
	descriptor.setTemporalDimension(new Date(1123180200001l), new Date(1123180200000l + (long) (8.64 * Math.pow(10, 7)))); // changed
															       // temporal
															       // dimension
	test("time-series-1.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    }

    // @Test
    // public void testValidationFailedBadDocument1() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-4.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }
    //
    // @Test
    // public void testValidationFailedBadDocument2() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-5.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }
    //
    // @Test
    // public void testValidationFailedBadDocument3() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-6.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }
    // @Test
    // public void testValidationFailedBadDocument4() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-7.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }
    //
    // @Test
    // public void testValidationFailedBadDocument5() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-8.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }
    //
    // @Test
    // public void testValidationFailedBadDocument6() throws Exception {
    // DataDescriptor descriptor = new DataDescriptor();
    // descriptor.setDataFormat(DataFormat.NETCDF);
    // descriptor.setDataType(DataType.TIME_SERIES);
    // descriptor.setCRS(CRS.EPSG_4326());
    // descriptor.setEPSG4326SpatialDimensions(41.718473, -111.946402);
    // descriptor.setVerticalDimension(1345, 1345);
    // descriptor.setTemporalDimension(new Date(1123180200000l), new Date(1123180200000l + (long) (8.64 * Math.pow(10,
    // 7))));
    // test("wml-9.nc", descriptor, false, DataValidatorErrorCode.DESCRIPTOR_MISMATCH);
    // }

}
